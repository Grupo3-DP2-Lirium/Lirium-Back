package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.Documentary;
import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.enums.DocumentaryStatus;
import org.example.springboot_backend.repository.DocumentaryRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.service.storage.AzureBlobStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentaryProcessingService {

    @Autowired
    private DocumentaryRepository documentaryRepository;

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private AzureBlobStorageService azureStorageService;

    @Autowired
    private AIClassificationService aiService;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${documentary.temp.path:/tmp/documentaries}")
    private String tempPath;

    @Value("${documentary.fonts.path:/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf}")
    private String fontsPath;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgressTransactional(UUID id, int progress, DocumentaryStatus status) {
        Documentary d = documentaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));
        if (status != null) d.setStatus(status);
        d.setProgress(progress);
        d.setUpdatedDate(LocalDateTime.now());
        documentaryRepository.saveAndFlush(d);
    }

    /**
     * Procesa el documental de manera asíncrona - VERSIÓN REAL
     */
    @Async
    public void processDocumentary(UUID documentaryId) {
        Path tempDir = null;
        List<Path> downloadedFiles = new ArrayList<>();
        Path outputVideo = null;

        try {
            System.out.println("🎬 Starting REAL documentary generation: " + documentaryId);

            Documentary documentary = documentaryRepository.findById(documentaryId)
                    .orElseThrow(() -> new RuntimeException("Documentary not found"));

            updateProgressTransactional(documentaryId, 5, DocumentaryStatus.PROCESSING);

            // 👇 CAMBIO: Obtener el proxy desde ApplicationContext
            DocumentaryProcessingService proxy = applicationContext.getBean(DocumentaryProcessingService.class);
            List<Memory> memories = proxy.loadMemoriesWithFiles(documentary.getMemoryIds());

            System.out.println("📝 Found " + memories.size() + " memories");
            updateProgressTransactional(documentaryId, 10, null);

            // 2. Crear directorio temporal
            tempDir = Paths.get(tempPath, documentaryId.toString());
            Files.createDirectories(tempDir);
            System.out.println("📁 Created temp directory: " + tempDir);

            // 3. Descargar archivos de Azure
            System.out.println("⬇️ Downloading media files from Azure...");
            downloadedFiles = downloadMediaFilesReal(memories, tempDir, documentaryId);
            updateProgressTransactional(documentaryId, 30, null);

            // 4. Generar video con FFmpeg
            System.out.println("🎥 Generating video with FFmpeg...");

            documentary = documentaryRepository.findById(documentaryId)
                    .orElseThrow(() -> new RuntimeException("Documentary not found"));

            outputVideo = generateVideoWithFFmpeg(memories, downloadedFiles, tempDir, documentary);
            updateProgressTransactional(documentaryId, 80, null);

            // 5. Subir video a Azure
            System.out.println("☁️ Uploading video to Azure...");
            String videoUrl = uploadVideoToAzureReal(outputVideo, documentary);
            updateProgressTransactional(documentaryId, 95, null);

            // 6. Actualizar documentary con resultado final
            documentary = documentaryRepository.findById(documentaryId)
                    .orElseThrow(() -> new RuntimeException("Documentary not found"));

            documentary.setVideoUrl(videoUrl);
            documentary.setProcessingCompleted(LocalDateTime.now());

            if (Files.exists(outputVideo)) {
                documentary.setVideoSize(Files.size(outputVideo));
                documentary.setVideoDuration(memories.size() * documentary.getDurationPerMemory() + 10);
            }

            // 👇 PRIMERO guardar todos los datos
            documentaryRepository.save(documentary);

            // 👇 LUEGO actualizar status a COMPLETED
            updateProgressTransactional(documentaryId, 100, DocumentaryStatus.COMPLETED);

            System.out.println("✅ Documentary completed successfully!");
            System.out.println("   - Video URL: " + videoUrl);
            System.out.println("   - Size: " + (documentary.getVideoSize() / 1024 / 1024) + " MB");
            System.out.println("   - Duration: " + documentary.getVideoDuration() + " seconds");

        } catch (Exception e) {
            System.err.println("❌ Documentary generation failed: " + e.getMessage());
            e.printStackTrace();

            updateProgressTransactional(documentaryId, 0, DocumentaryStatus.FAILED);

            Documentary documentary = documentaryRepository.findById(documentaryId).orElse(null);
            if (documentary != null) {
                documentary.setErrorMessage(e.getMessage());
                documentary.setProcessingCompleted(LocalDateTime.now());
                documentaryRepository.save(documentary);
            }

        } finally {
            cleanupTempFiles(downloadedFiles, outputVideo, tempDir);
        }
    }

    /**
     * Carga memories con files en contexto transaccional
     */
    @Transactional(readOnly = true)
    public List<Memory> loadMemoriesWithFiles(String memoryIdsString) {
        System.out.println("📦 Loading memories with files in transaction...");

        String[] memoryIdStrings = memoryIdsString.split(",");
        List<UUID> memoryIds = Arrays.stream(memoryIdStrings)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<Memory> memories = memoryRepository.findAllById(memoryIds);

        System.out.println("📦 Loaded " + memories.size() + " memories, initializing files...");

        // Inicializar files mientras la sesión está activa
        for (Memory memory : memories) {
            if (memory.getFiles() != null) {
                int fileCount = memory.getFiles().size();
                System.out.println("   ✅ Memory " + memory.getIdMemory() + " has " + fileCount + " files");
            }
        }

        List<Memory> sortedMemories = memories.stream()
                .sorted(Comparator.comparing(m ->
                        m.getPhotoDate() != null ? m.getPhotoDate() : java.time.LocalDate.MIN))
                .collect(Collectors.toList());

        System.out.println("✅ Memories loaded and initialized successfully");
        return sortedMemories;
    }






    /**
     * Descarga los archivos de media desde Azure (REAL)
     */
    private List<Path> downloadMediaFilesReal(List<Memory> memories, Path tempDir, UUID documentaryId) throws IOException {
        List<Path> downloadedFiles = new ArrayList<>();

        int index = 0;
        for (Memory memory : memories) {
            if (!memory.hasFiles()) {
                System.out.println("⚠️ Memory " + memory.getIdMemory() + " has no files, skipping");
                continue;
            }

            File file = memory.getFiles().get(0);
            String blobPath = file.getStoragePath().replace("\\", "/");
            String extension = getFileExtension(file.getFileName());
            Path localFile = tempDir.resolve(String.format("media_%03d%s", index, extension));

            try {
                System.out.println("⬇️ Downloading: " + blobPath);
                azureStorageService.downloadBlobToFile(blobPath, localFile);
                downloadedFiles.add(localFile);
                index++;

                int progressIncrement = (int) ((20.0 / memories.size()) * (index));
                updateProgressTransactional(documentaryId, 10 + progressIncrement, null);

            } catch (Exception e) {
                System.err.println("❌ Failed to download: " + blobPath + " - " + e.getMessage());
            }
        }

        if (downloadedFiles.isEmpty()) {
            throw new RuntimeException("No media files could be downloaded");
        }

        System.out.println("✅ Downloaded " + downloadedFiles.size() + " files");
        return downloadedFiles;
    }
    /**
     * Genera el video usando FFmpeg (REAL)
     */
    private Path generateVideoWithFFmpeg(List<Memory> memories, List<Path> mediaFiles,
                                         Path tempDir, Documentary documentary) throws IOException, InterruptedException {

        Path outputVideo = tempDir.resolve("documentary_output.mp4");

        // Crear archivo de lista para concatenación
        Path fileListPath = createFileListForConcat(mediaFiles, memories, tempDir, documentary);

        //Descargar música si existe
        Path musicPath = null;
        if (documentary.getMusicTrack() != null && !documentary.getMusicTrack().isEmpty()) {
            musicPath = downloadMusicFromAzure(documentary.getMusicTrack(), tempDir);
        }

        // Construir comando FFmpeg con música
        List<String> command = buildFFmpegCommandWithMusic(fileListPath, musicPath, outputVideo, documentary);

        System.out.println("🎬 FFmpeg command: " + String.join(" ", command));

        // Ejecutar FFmpeg
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Leer output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("FFmpeg: " + line);

                if (line.contains("frame=")) {
                    int currentProgress = documentary.getProgress();
                    if (currentProgress < 75) {
                        int current = documentary.getProgress();
                        updateProgressTransactional(documentary.getIdDocumentary(), Math.min(75, current + 1), null);
                    }
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code: " + exitCode);
        }

        if (!Files.exists(outputVideo)) {
            throw new RuntimeException("Output video was not created");
        }

        System.out.println("✅ Video generated: " + outputVideo + " (" + (Files.size(outputVideo) / 1024 / 1024) + " MB)");

        // Limpiar música temporal
        if (musicPath != null) {
            Files.deleteIfExists(musicPath);
        }

        return outputVideo;
    }

    /**
     * Construye el comando de FFmpeg con música de fondo
     */
    private List<String> buildFFmpegCommandWithMusic(Path fileListPath, Path musicPath,
                                                     Path outputVideo, Documentary documentary) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-i");
        command.add(fileListPath.toString());

        // ✨ AGREGAR MÚSICA COMO SEGUNDO INPUT
        if (musicPath != null && Files.exists(musicPath)) {
            command.add("-stream_loop");
            command.add("-1"); // Loop infinito de la música
            command.add("-i");
            command.add(musicPath.toString());

            // Mapear video del primer input
            command.add("-map");
            command.add("0:v");

            // Mapear audio del segundo input (música)
            command.add("-map");
            command.add("1:a");

            // Ajustar volumen de la música (30% = -10dB)
            command.add("-filter:a");
            command.add("volume=0.3");

            // Usar duración del video (shorter)
            command.add("-shortest");

            // Codec de audio
            command.add("-c:a");
            command.add("aac");
            command.add("-b:a");
            command.add("192k");
        }

        // Copiar video sin re-encodear
        command.add("-c:v");
        command.add("copy");

        command.add("-y");
        command.add(outputVideo.toString());

        return command;
    }

    /**
     * Crea archivo de lista para concatenación en FFmpeg
     */
    private Path createFileListForConcat(List<Path> mediaFiles, List<Memory> memories,
                                         Path tempDir, Documentary documentary) throws IOException, InterruptedException {
        Path fileListPath = tempDir.resolve("filelist.txt");
        Path processedDir = tempDir.resolve("processed");
        Files.createDirectories(processedDir);

        List<String> fileListContent = new ArrayList<>();

        // ✨ Generar narraciones para cada recuerdo
        List<String> narraciones = generarNarraciones(memories, documentary);

        for (int i = 0; i < mediaFiles.size(); i++) {
            Path inputFile = mediaFiles.get(i);
            Memory memory = memories.get(i);
            String narracion = narraciones.get(i);
            Path processedFile = processedDir.resolve("processed_" + i + ".mp4");

            // Procesar cada archivo con narración generada
            processIndividualFileWithNarration(inputFile, processedFile, memory, narracion, documentary);

            fileListContent.add("file '" + processedFile.toAbsolutePath().toString().replace("\\", "/") + "'");
        }

        Files.write(fileListPath, fileListContent);
        System.out.println("📝 Created file list: " + fileListPath);
        return fileListPath;
    }


    /**
     * ACTUALIZADO: Genera narraciones considerando el enfoque y tono del documental
     */
    private List<String> generarNarraciones(List<Memory> memories, Documentary documentary) {
        List<String> narraciones = new ArrayList<>();

        String nombrePersona = documentary.getMemorial().getName();
        String narrativeFocus = documentary.getNarrativeFocus();
        String emotionalTone = documentary.getEmotionalTone() != null ? documentary.getEmotionalTone() : "nostalgic";
        int total = memories.size();

        System.out.println("📝 Generando " + total + " narraciones con IA...");
        System.out.println("   🎯 Enfoque narrativo: " + (narrativeFocus != null ? narrativeFocus : "sin especificar"));
        System.out.println("   🎭 Tono emocional: " + emotionalTone);

        for (int i = 0; i < memories.size(); i++) {
            Memory memory = memories.get(i);

            // Determinar posición en la historia
            String posicion;
            if (i == 0) {
                posicion = "primer recuerdo del documental";
            } else if (i == total - 1) {
                posicion = "último recuerdo del documental";
            } else {
                posicion = String.format("recuerdo %d de %d", i + 1, total);
            }

            try {
                // ✨ ACTUALIZADO: Pasar narrativeFocus y emotionalTone
                String narracion = aiService.generarNarracionRecuerdo(
                        nombrePersona,
                        memory.getTitle(),
                        memory.getDescription(),
                        memory.getPhotoDate(),
                        posicion,
                        narrativeFocus,
                        emotionalTone
                );
                narraciones.add(narracion);

                // Pequeña pausa para no saturar la API
                Thread.sleep(500);

            } catch (Exception e) {
                System.err.println("⚠️ Error generando narración para recuerdo " + (i+1) + ": " + e.getMessage());
                // Fallback: usar título o descripción
                String fallback = memory.getTitle() != null ? memory.getTitle() :
                        (memory.getDescription() != null ? memory.getDescription() : "Un momento especial");
                narraciones.add(fallback);
            }
        }

        System.out.println("✅ Narraciones generadas exitosamente");
        return narraciones;
    }


    /**
     * Procesa un archivo individual con narración generada por IA
     */
    private void processIndividualFileWithNarration(Path inputFile, Path outputFile,
                                                    Memory memory, String narracion,
                                                    Documentary documentary) throws IOException, InterruptedException {

        String resolution = getResolutionDimensions(documentary.getResolution());
        String styleFilter = getStyleFilter(documentary.getStyleFilter());
        int duration = documentary.getDurationPerMemory();

        // La narración se divide en líneas para mejor legibilidad
        String textoNarracion = wrapText(narracion, 80); // Máximo 50 caracteres por línea
        textoNarracion = escapeFFmpegText(textoNarracion);

        // Fecha (opcional)
        String date = memory.getPhotoDate() != null ?
                memory.getPhotoDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        // Construir filtros
        StringBuilder filters = new StringBuilder();

        String fileType = getFileType(inputFile);

        filters.append("scale=").append(resolution).append(",setsar=1");

        if (styleFilter != null) {
            filters.append(",").append(styleFilter);
        }

        String fontPath = escapeFontPathForFFmpeg(fontsPath);

        // Subtítulo - Más legible con fondo y mejor posicionado
        filters.append(",drawtext=fontfile='").append(fontPath).append("':")
                .append("text='").append(textoNarracion).append("':")
                .append("fontsize=30:")  // Tamaño más pequeño para que quepa mejor
                .append("fontcolor=white:")
                .append("x=(w-text_w)/2:")  // Centrado horizontal
                .append("y=h-h/5:")  // Posicionado en el quinto inferior de la pantalla
                .append("box=1:")  // Caja de fondo
                .append("boxcolor=black@0.6:")  // Fondo negro semi-transparente
                .append("boxborderw=20:")  // Padding interno
                .append("line_spacing=5");  // Espaciado entre líneas

        // Fecha pequeña (opcional, más arriba para no interferir)
        if (!date.isEmpty()) {
            filters.append(",drawtext=fontfile='").append(fontPath).append("':")
                    .append("text='").append(escapeFFmpegText(date)).append("':")
                    .append("fontsize=20:")
                    .append("fontcolor=white@0.9:")
                    .append("x=(w-text_w)/2:")
                    .append("y=20:")
                    .append("box=1:")
                    .append("boxcolor=black@0.4:")
                    .append("boxborderw=10");
        }

        // Comando FFmpeg
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);

        if (fileType.equals("image")) {
            command.add("-loop");
            command.add("1");
        }

        command.add("-i");
        command.add(inputFile.toString());
        command.add("-vf");
        command.add(filters.toString());
        command.add("-t");
        command.add(String.valueOf(duration));
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("fast");
        command.add("-crf");
        command.add("23");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-y");
        command.add(outputFile.toString());

        System.out.println("🎬 Processing: " + inputFile.getFileName());
        System.out.println("   📝 Narración: " + narracion);

        // Ejecutar
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.err.println("❌ FFmpeg failed with exit code: " + exitCode);
            System.err.println("❌ FFmpeg output:\n" + output.toString());
            throw new RuntimeException("FFmpeg failed processing " + inputFile.getFileName() +
                    " (exit code: " + exitCode + ")");
        }

        System.out.println("✅ Processed: " + outputFile.getFileName());
    }

    /**
     * Divide texto largo en múltiples líneas para que quepa en pantalla
     * @param text Texto original
     * @param maxCharsPerLine Máximo de caracteres por línea
     * @return Texto con saltos de línea
     */
    private String wrapText(String text, int maxCharsPerLine) {
        if (text == null || text.length() <= maxCharsPerLine) {
            return text;
        }

        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        int lineLength = 0;

        for (String word : words) {
            // Si agregar esta palabra excede el límite, hacer salto de línea
            if (lineLength + word.length() + 1 > maxCharsPerLine && lineLength > 0) {
                // formato %{line_h} en lugar de \n
                wrapped.append("\n");  // Salto de línea real, no escapado
                lineLength = 0;
            }

            // Agregar palabra
            if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            wrapped.append(word);
            lineLength += word.length();
        }

        return wrapped.toString();
    }

    /**
     * Construye el comando final de FFmpeg para concatenar
     */
    private List<String> buildFFmpegCommand(Path fileListPath, Path outputVideo, Documentary documentary) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-i");
        command.add(fileListPath.toString());
        command.add("-c");
        command.add("copy");
        command.add("-y");
        command.add(outputVideo.toString());

        return command;
    }

    /**
     * Sube el video generado a Azure (REAL)
     */
    private String uploadVideoToAzureReal(Path videoPath, Documentary documentary) throws IOException {
        String folder = String.format("documentaries/%s", documentary.getMemorial().getIdMemorial());
        String fileName = String.format("documentary_%s.mp4", documentary.getIdDocumentary());
        String blobPath = folder + "/" + fileName;

        System.out.println("☁️ Uploading to Azure: " + blobPath);
        String videoUrl = azureStorageService.uploadFileToBlob(videoPath, blobPath);

        System.out.println("✅ Video uploaded: " + videoUrl);
        return videoUrl;
    }

    /**
     * Limpia archivos temporales
     */
    private void cleanupTempFiles(List<Path> downloadedFiles, Path outputVideo, Path tempDir) {
        try {
            System.out.println("🧹 Cleaning up temporary files...");

            // Eliminar archivos descargados
            for (Path file : downloadedFiles) {
                Files.deleteIfExists(file);
            }

            // Eliminar video de salida
            if (outputVideo != null) {
                Files.deleteIfExists(outputVideo);
            }

            // Eliminar directorio procesado
            if (tempDir != null) {
                Path processedDir = tempDir.resolve("processed");
                if (Files.exists(processedDir)) {
                    Files.walk(processedDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    // Ignorar errores
                                }
                            });
                }

                // Eliminar archivo de lista
                Files.deleteIfExists(tempDir.resolve("filelist.txt"));

                // Eliminar directorio temporal
                try {
                    Files.delete(tempDir);
                } catch (IOException e) {
                    // Directorio no vacío, ignorar
                }
            }

            System.out.println("✅ Cleanup completed");

        } catch (IOException e) {
            System.err.println("⚠️ Error during cleanup: " + e.getMessage());
        }
    }

    // Métodos auxiliares



    private void updateProgress(Documentary documentary, int progress) {
        documentary.setProgress(progress);
        documentaryRepository.save(documentary);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".jpg";
    }

    private String getFileType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".webp")) {
            return "image";
        }
        return "video";
    }

    private String getResolutionDimensions(String resolution) {
        return switch (resolution) {
            case "480p" -> "854:480";
            case "1080p" -> "1920:1080";
            default -> "1280:720";
        };
    }

    private String getStyleFilter(String style) {
        return switch (style) {
            case "sepia" -> "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131";
            case "bw" -> "hue=s=0";
            case "vibrant" -> "eq=saturation=1.5:contrast=1.1";
            case "warm" -> "colorbalance=rs=0.2:gs=-0.1:bs=-0.2";
            default -> null;
        };
    }

    private String escapeFFmpegText(String text) {
        if (text == null) return "";

        // NO escapar\n, solo escapar caracteres especiales de FFmpeg
        return text.replace("'", "'\\''")
                .replace(":", "\\:")
                .replace("%", "\\%")
                .replace(",", "\\,");  // También escapar comas
    }

    private String escapeFontPathForFFmpeg(String path) {
        if (path == null) return "";
        // Normaliza separadores y escapa SOLO el "C:" -> "C\:"
        String p = path.replace("\\", "/");
        return p.replaceFirst("^(?i)([a-z]):", "$1\\\\:");
    }

    /**
     * Descarga el archivo de música desde Azure
     */
    private Path downloadMusicFromAzure(String musicBlobPath, Path tempDir) throws IOException {
        try {
            Path musicPath = tempDir.resolve("background_music.mp3");

            System.out.println("🎵 Downloading music: " + musicBlobPath);
            azureStorageService.downloadBlobToFile(musicBlobPath, musicPath);

            System.out.println("✅ Music downloaded: " + musicPath);
            return musicPath;

        } catch (Exception e) {
            System.err.println("⚠️ Failed to download music, continuing without it: " + e.getMessage());
            return null; // Continuar sin música si falla
        }
    }

}
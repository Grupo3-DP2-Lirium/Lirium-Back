package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.Capsule;
import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.enums.CapsuleFilter;
import org.example.springboot_backend.enums.CapsuleStatus;
import org.example.springboot_backend.repository.CapsuleRepository;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CapsuleProcessingService {

    @Autowired
    private CapsuleRepository capsuleRepository;

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private AzureBlobStorageService azureStorageService;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${documentary.temp.path:/tmp/documentaries}")
    private String tempPath;

    @Value("${documentary.fonts.path:/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf}")
    private String fontsPath;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgressTransactional(UUID id, int progress, CapsuleStatus status) {
        Capsule capsule = capsuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));
        if (status != null) capsule.setStatus(status);
        capsule.setProgress(progress);
        capsule.setUpdatedDate(LocalDateTime.now());
        capsuleRepository.saveAndFlush(capsule);
    }

    /**
     * Procesa la cápsula de manera asíncrona - VIDEO VERTICAL 9:16
     */
    @Async
    public void processCapsule(UUID capsuleId) {
        Path tempDir = null;
        List<Path> downloadedFiles = new ArrayList<>();
        Path outputVideo = null;

        try {
            System.out.println("🎬 Starting CAPSULE generation (vertical 9:16): " + capsuleId);

            Capsule capsule = capsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new RuntimeException("Capsule not found"));

            updateProgressTransactional(capsuleId, 5, CapsuleStatus.PROCESSING);

            // Cargar memories con files
            CapsuleProcessingService proxy = applicationContext.getBean(CapsuleProcessingService.class);
            List<Memory> memories = proxy.loadMemoriesWithFiles(capsule.getMemoryIds());

            System.out.println("📝 Found " + memories.size() + " memories for capsule");
            updateProgressTransactional(capsuleId, 10, null);

            // Crear directorio temporal
            tempDir = Paths.get(tempPath, "capsules", capsuleId.toString());
            Files.createDirectories(tempDir);
            System.out.println("📁 Created temp directory: " + tempDir);

            // Descargar archivos de Azure
            System.out.println("⬇️ Downloading media files from Azure...");
            downloadedFiles = downloadMediaFiles(memories, tempDir, capsuleId);
            updateProgressTransactional(capsuleId, 30, null);

            // Generar video VERTICAL con FFmpeg
            System.out.println("🎥 Generating VERTICAL video (9:16) with FFmpeg...");
            capsule = capsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new RuntimeException("Capsule not found"));

            outputVideo = generateVerticalVideoWithFFmpeg(memories, downloadedFiles, tempDir, capsule);
            updateProgressTransactional(capsuleId, 80, null);

            // Subir video a Azure
            System.out.println("☁️ Uploading capsule video to Azure...");
            String videoUrl = uploadVideoToAzure(outputVideo, capsule);
            updateProgressTransactional(capsuleId, 95, null);

            // Actualizar capsule con resultado final
            capsule = capsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new RuntimeException("Capsule not found"));

            capsule.setVideoUrl(videoUrl);
            capsule.setProcessingCompleted(LocalDateTime.now());

            if (Files.exists(outputVideo)) {
                capsule.setVideoSize(Files.size(outputVideo));

                // Calcular duración: 5 segundos por recuerdo + 3 segundos intro
                int totalDuration = (memories.size() * 5) + 3;
                capsule.setVideoDuration(Math.min(totalDuration, 60)); // Max 60 segundos
            }

            capsuleRepository.save(capsule);
            updateProgressTransactional(capsuleId, 100, CapsuleStatus.COMPLETED);

            System.out.println("✅ Capsule completed successfully!");
            System.out.println("   - Video URL: " + videoUrl);
            System.out.println("   - Size: " + (capsule.getVideoSize() / 1024 / 1024) + " MB");
            System.out.println("   - Duration: " + capsule.getVideoDuration() + " seconds");

        } catch (Exception e) {
            System.err.println("❌ Capsule generation failed: " + e.getMessage());
            e.printStackTrace();

            updateProgressTransactional(capsuleId, 0, CapsuleStatus.FAILED);

            Capsule capsule = capsuleRepository.findById(capsuleId).orElse(null);
            if (capsule != null) {
                capsule.setErrorMessage(e.getMessage());
                capsule.setProcessingCompleted(LocalDateTime.now());
                capsuleRepository.save(capsule);
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

        // Mantener el orden original de los IDs
        Map<UUID, Memory> memoryMap = memories.stream()
                .collect(Collectors.toMap(Memory::getIdMemory, m -> m));

        List<Memory> orderedMemories = memoryIds.stream()
                .map(memoryMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("✅ Memories loaded and ordered successfully");
        return orderedMemories;
    }

    /**
     * Descarga TODOS los archivos de las memorias seleccionadas
     */
    private List<Path> downloadMediaFiles(List<Memory> memories, Path tempDir, UUID capsuleId) throws IOException {
        List<Path> downloadedFiles = new ArrayList<>();
        int totalFiles = memories.stream()
                .filter(Memory::hasFiles)
                .mapToInt(m -> m.getFiles().size())
                .sum();

        System.out.println("📦 Total archivos a procesar: " + totalFiles);

        int fileIndex = 0;

        for (Memory memory : memories) {
            if (!memory.hasFiles()) {
                System.out.println("⚠️ Memory " + memory.getIdMemory() + " has no files, skipping");
                continue;
            }

            // PROCESAR TODOS LOS ARCHIVOS DE LA MEMORIA
            for (File file : memory.getFiles()) {
                String blobPath = file.getStoragePath().replace("\\", "/");
                String extension = getFileExtension(file.getFileName());
                Path localFile = tempDir.resolve(String.format("media_%03d%s", fileIndex, extension));

                try {
                    System.out.println("⬇️ Downloading: " + blobPath + " (" + file.getFileName() + ")");
                    azureStorageService.downloadBlobToFile(blobPath, localFile);
                    downloadedFiles.add(localFile);
                    fileIndex++;

                    // Actualizar progreso basado en archivos totales
                    int progressIncrement = (int) ((20.0 / totalFiles) * fileIndex);
                    updateProgressTransactional(capsuleId, 10 + progressIncrement, null);

                } catch (Exception e) {
                    System.err.println("❌ Failed to download: " + blobPath + " - " + e.getMessage());
                }
            }
        }

        if (downloadedFiles.isEmpty()) {
            throw new RuntimeException("No media files could be downloaded");
        }

        System.out.println("✅ Downloaded " + downloadedFiles.size() + " files from " + memories.size() + " memories");
        return downloadedFiles;
    }

    /**
     * 🎬 GENERA VIDEO VERTICAL 9:16 con título al inicio
     */
    private Path generateVerticalVideoWithFFmpeg(List<Memory> memories, List<Path> mediaFiles,
                                                 Path tempDir, Capsule capsule)
            throws IOException, InterruptedException {

        Path outputVideo = tempDir.resolve("capsule_output.mp4");

        // 1. Crear video de título (intro 3 segundos)
        Path titleVideo = createTitleIntro(capsule.getTitle(), tempDir);

        // 2. Procesar cada media file con filtro
        Path processedDir = tempDir.resolve("processed");
        Files.createDirectories(processedDir);

        List<Path> processedFiles = new ArrayList<>();
        processedFiles.add(titleVideo); // Agregar intro al inicio

        for (int i = 0; i < mediaFiles.size(); i++) {
            Path inputFile = mediaFiles.get(i);
            Path processedFile = processedDir.resolve("processed_" + i + ".mp4");

            processIndividualFileVertical(inputFile, processedFile, capsule);
            processedFiles.add(processedFile);

            // Actualizar progreso
            int progress = 30 + (int) ((45.0 / mediaFiles.size()) * (i + 1));
            updateProgressTransactional(capsule.getIdCapsule(), progress, null);
        }

        // 3. Crear archivo de lista para concatenación
        Path fileListPath = createFileListForConcat(processedFiles, tempDir);

        // 4. Descargar música si existe
        Path musicPath = null;
        if (capsule.getMusicTrack() != null && !capsule.getMusicTrack().isEmpty()) {
            musicPath = downloadMusicFromAzure(capsule.getMusicTrack(), tempDir);
        }

        // 5. Concatenar con música
        concatenateWithMusic(fileListPath, musicPath, outputVideo, capsule);

        if (!Files.exists(outputVideo)) {
            throw new RuntimeException("Output video was not created");
        }

        System.out.println("✅ Capsule video generated: " + outputVideo +
                " (" + (Files.size(outputVideo) / 1024 / 1024) + " MB)");

        // Limpiar música temporal
        if (musicPath != null) {
            Files.deleteIfExists(musicPath);
        }

        return outputVideo;
    }

    /**
     * 🎨 Crea video de introducción con título (3 segundos) ---
     */
    private Path createTitleIntro(String title, Path tempDir) throws IOException, InterruptedException {
        Path titleVideo = tempDir.resolve("title_intro.mp4");

        String escapedTitle = escapeFFmpegText(title);
        String fontPath = escapeFontPathForFFmpeg(fontsPath);

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-f");
        command.add("lavfi");
        command.add("-i");
        command.add("color=c=black:s=1080x1920:d=3");
        command.add("-vf");
        command.add(String.format(
                "drawtext=fontfile='%s':text='%s':fontsize=60:fontcolor=white:" +
                        "x=(w-text_w)/2:y=(h-text_h)/2:box=0",
                fontPath, escapedTitle
        ));
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("ultrafast"); // Cambio
        command.add("-crf");
        command.add("28"); //Cambio
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-y");
        command.add(titleVideo.toString());

        System.out.println("🎬 Creating title intro: " + title);
        System.out.println("📝 FFmpeg command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Thread para output
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg: " + line);
                }
            } catch (IOException e) {
                System.err.println("⚠️ Error reading output: " + e.getMessage());
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();

        System.out.println("⏳ Waiting for title creation (max 60s)...");
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);

        if (!finished) {
            System.err.println("❌ Title creation TIMEOUT!");
            process.destroyForcibly();
            throw new RuntimeException("Failed to create title intro (timeout)");
        }

        outputThread.join(5000);

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            System.err.println("❌ FFmpeg failed creating title: exit code " + exitCode);
            throw new RuntimeException("Failed to create title intro");
        }

        if (!Files.exists(titleVideo)) {
            throw new RuntimeException("Title video not created");
        }

        System.out.println("✅ Title intro created (" + (Files.size(titleVideo) / 1024) + " KB)");
        return titleVideo;
    }

    /**
     * 🎨 Procesa archivo individual para formato VERTICAL 9:16 con filtro
     */
    private void processIndividualFileVertical(Path inputFile, Path outputFile, Capsule capsule)
            throws IOException, InterruptedException {

        String filterChain = buildVerticalFilterChain(capsule.getFilter());
        String fileType = getFileType(inputFile);

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);

        if (fileType.equals("image")) {
            command.add("-loop");
            command.add("1");
        }

        command.add("-i");
        command.add(inputFile.toString());
        command.add("-vf");
        command.add(filterChain);
        command.add("-t");
        command.add("5");
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("ultrafast"); // Cambio crítico
        command.add("-crf");
        command.add("28"); // Menos calidad = más rápido
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-y");
        command.add(outputFile.toString());

        System.out.println("🎬 Processing vertical: " + inputFile.getFileName());
        System.out.println("📝 FFmpeg command: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Thread para leer output sin bloquear
        StringBuilder ffmpegOutput = new StringBuilder();
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg: " + line);
                    ffmpegOutput.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("⚠️ Error reading FFmpeg output: " + e.getMessage());
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();

        //Timeout de 2 minutos
        System.out.println("⏳ Waiting for FFmpeg (max 120s)...");
        boolean finished = process.waitFor(120, TimeUnit.SECONDS);

        if (!finished) {
            System.err.println("❌ FFmpeg TIMEOUT after 120 seconds!");
            System.err.println("📝 Last output:\n" + ffmpegOutput.toString());
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            throw new RuntimeException("FFmpeg timeout processing " + inputFile.getFileName());
        }

        // Esperar al thread de output
        outputThread.join(5000);

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            System.err.println("❌ FFmpeg failed with exit code: " + exitCode);
            System.err.println("📝 Output:\n" + ffmpegOutput.toString());
            throw new RuntimeException("FFmpeg failed processing " + inputFile.getFileName()
                    + " (exit code: " + exitCode + ")");
        }

        // Verificar que el archivo se creó
        if (!Files.exists(outputFile)) {
            throw new RuntimeException("Output file not created: " + outputFile);
        }

        long fileSize = Files.size(outputFile);
        System.out.println("✅ Processed: " + outputFile.getFileName() +
                " (" + (fileSize / 1024) + " KB)");
    }

    /**
     * 🎨 Construye cadena de filtros para VIDEO VERTICAL 9:16 con efectos
     */
    private String buildVerticalFilterChain(CapsuleFilter filter) {
        // Resolución vertical 1080x1920 (9:16)
        StringBuilder filterChain = new StringBuilder();

        // 1. Escalar y crop para 9:16 con zoom suave
        filterChain.append("scale=1080:1920:force_original_aspect_ratio=increase,")
                .append("crop=1080:1920,")
                .append("setsar=1,")
                .append("zoompan=z='min(zoom+0.0015,1.5)':d=125:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=1080x1920");

        // 2. Aplicar filtro de color según selección
        String colorFilter = getColorFilter(filter);
        if (colorFilter != null) {
            filterChain.append(",").append(colorFilter);
        }

        return filterChain.toString();
    }

    /**
     * 🎨 Retorna filtro de color según el CapsuleFilter
     */
    private String getColorFilter(CapsuleFilter filter) {
        return switch (filter) {
            case VIVID -> "eq=saturation=1.8:contrast=1.3:brightness=0.05"; // Colores ultra vibrantes
            case DRAMATIC -> "eq=contrast=1.5:brightness=-0.1:saturation=0.8,curves=all='0/0 0.5/0.4 1/1'"; // Alto contraste
            case YELLOW -> "colorbalance=rs=0.3:gs=0.1:bs=-0.3,eq=saturation=1.2"; // Tonos cálidos/amarillos
            case MONO -> "hue=s=0"; // Blanco y negro
            case SILVERTONE -> "colorchannelmixer=.3:.4:.3:0:.3:.4:.3:0:.3:.4:.3"; // Tonos plateados/azulados
            case NATURAL -> null; // Sin filtro
        };
    }

    /**
     * Crea archivo de lista para concatenación
     */
    private Path createFileListForConcat(List<Path> processedFiles, Path tempDir) throws IOException {
        Path fileListPath = tempDir.resolve("filelist.txt");
        List<String> fileListContent = new ArrayList<>();

        for (Path file : processedFiles) {
            fileListContent.add("file '" + file.toAbsolutePath().toString().replace("\\", "/") + "'");
        }

        Files.write(fileListPath, fileListContent);
        System.out.println("📝 Created file list with " + processedFiles.size() + " files");
        return fileListPath;
    }

    /**
     * Concatena videos con música de fondo
     */
    private void concatenateWithMusic(Path fileListPath, Path musicPath, Path outputVideo, Capsule capsule)
            throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-i");
        command.add(fileListPath.toString());

        // Agregar música si existe
        if (musicPath != null && Files.exists(musicPath)) {
            command.add("-stream_loop");
            command.add("-1"); // Loop música
            command.add("-i");
            command.add(musicPath.toString());

            command.add("-map");
            command.add("0:v");
            command.add("-map");
            command.add("1:a");

            command.add("-filter:a");
            command.add("volume=0.3"); // Música al 30%

            command.add("-shortest");

            command.add("-c:a");
            command.add("aac");
            command.add("-b:a");
            command.add("192k");
        }

        command.add("-c:v");
        command.add("copy");
        command.add("-y");
        command.add(outputVideo.toString());

        System.out.println("🎵 Concatenating with music...");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("FFmpeg: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg concat failed with exit code: " + exitCode);
        }

        System.out.println("✅ Concatenation completed");
    }

    /**
     * Sube el video a Azure
     */
    private String uploadVideoToAzure(Path videoPath, Capsule capsule) throws IOException {
        String folder = String.format("capsules/%s", capsule.getMemorial().getIdMemorial());
        String fileName = String.format("capsule_%s.mp4", capsule.getIdCapsule());
        String blobPath = folder + "/" + fileName;

        System.out.println("☁️ Uploading to Azure: " + blobPath);
        String videoUrl = azureStorageService.uploadFileToBlob(videoPath, blobPath);

        System.out.println("✅ Video uploaded: " + videoUrl);
        return videoUrl;
    }

    /**
     * Descarga música desde Azure
     */
    private Path downloadMusicFromAzure(String musicBlobPath, Path tempDir) throws IOException {
        try {
            Path musicPath = tempDir.resolve("background_music.mp3");
            System.out.println("🎵 Downloading music: " + musicBlobPath);
            azureStorageService.downloadBlobToFile(musicBlobPath, musicPath);
            System.out.println("✅ Music downloaded");
            return musicPath;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to download music: " + e.getMessage());
            return null;
        }
    }

    /**
     * Limpia archivos temporales
     */
    private void cleanupTempFiles(List<Path> downloadedFiles, Path outputVideo, Path tempDir) {
        try {
            System.out.println("🧹 Cleaning up temporary files...");

            for (Path file : downloadedFiles) {
                Files.deleteIfExists(file);
            }

            if (outputVideo != null) {
                Files.deleteIfExists(outputVideo);
            }

            if (tempDir != null) {
                Path processedDir = tempDir.resolve("processed");
                if (Files.exists(processedDir)) {
                    Files.walk(processedDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    // Ignorar
                                }
                            });
                }

                Files.deleteIfExists(tempDir.resolve("filelist.txt"));
                Files.deleteIfExists(tempDir.resolve("title_intro.mp4"));

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

    // ==================== HELPERS ====================

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

    private String escapeFFmpegText(String text) {
        if (text == null) return "";
        return text.replace("'", "'\\''")
                .replace(":", "\\:")
                .replace("%", "\\%")
                .replace(",", "\\,");
    }

    private String escapeFontPathForFFmpeg(String path) {
        if (path == null) return "";
        String p = path.replace("\\", "/");
        return p.replaceFirst("^(?i)([a-z]):", "$1\\\\:");
    }
}
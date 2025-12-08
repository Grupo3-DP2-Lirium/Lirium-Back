package org.example.springboot_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot_backend.entity.Memory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIClassificationService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterUrl;

    @Value("${openrouter.model:mistralai/mistral-7b-instruct}")
    private String modelName;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final List<String> CATS = List.of(
            "familia","amigos","pareja","infancia","juventud","adultez","viajes","celebraciones",
            "trabajo","comunidad","arte_cultura","fe_espiritualidad","salud_bienestar",
            "despedidas_duelo","legado","otros"
    );
    private static final List<String> MOMS = List.of(
            "amor_afecto","gratitud","nostalgia","alegria","tristeza","orgullo",
            "superacion","reflexion","fe","paz","asombro","cotidiano"
    );

    // Correcciones suaves de etiquetas fuera de vocabulario
    private static final Map<String,String> NORMALIZE = Map.ofEntries(
            Map.entry("naturaleza", "cotidiano"),
            Map.entry("educacion", "trabajo"),
            Map.entry("vocacion", "trabajo"),
            Map.entry("amistad", "amigos"),
            Map.entry("espiritualidad", "fe_espiritualidad")
    );

    private static final Pattern JSON_BLOCK = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);

    /**
     * Clasifica un recuerdo y devuelve:
     * {"categorias":[...], "momentos":[...]}
     */
    public Map<String, List<String>> clasificar(String titulo, String descripcion) {
        // Guardrail: si no hay nada, cae al fallback
        if ((titulo == null || titulo.isBlank()) && (descripcion == null || descripcion.isBlank())) {
            System.out.println("ℹ️ Sin título ni descripción, usando fallback");
            return fallback();
        }

        String prompt = buildPrompt(titulo, descripcion);

        // Request body para OpenRouter
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", 120);
        payload.put("stream", false);
        payload.put("messages", List.of(Map.of("role","user","content", prompt)));

        try {
            System.out.println("🤖 Llamando a OpenRouter para clasificar...");
            String text = postOpenRouter(payload);
            System.out.println("✅ Respuesta de OpenRouter recibida: " + text);

            // Extrae el primer {...} aunque venga con ```json ... ```
            String jsonText = extractJson(text);
            if (jsonText == null) {
                System.err.println("⚠️ No se pudo extraer JSON de la respuesta");
                return fallback();
            }

            Map<String, Object> obj = MAPPER.readValue(jsonText, new TypeReference<>() {});
            List<String> rawCats = toStringList(obj.get("categorias"));
            List<String> rawMoms = toStringList(obj.get("momentos"));

            System.out.println("📋 Categorías raw: " + rawCats);
            System.out.println("📋 Momentos raw: " + rawMoms);

            // Normaliza, valida contra listas y limita a 3
            List<String> cats = normalizeAndClamp(rawCats, CATS, "otros", 3);
            List<String> moms = normalizeAndClamp(rawMoms, MOMS, "cotidiano", 3);

            // Asegura al menos 1
            if (cats.isEmpty()) cats = List.of("otros");
            if (moms.isEmpty()) moms = List.of("cotidiano");

            Map<String, List<String>> out = new HashMap<>();
            out.put("categorias", cats);
            out.put("momentos", moms);

            System.out.println("✅ Clasificación exitosa - Cats: " + cats + ", Moms: " + moms);
            return out;

        } catch (Exception e) {
            System.err.println("❌ Error clasificando con IA: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            return fallback();
        }
    }

    /* ==================== Helpers ==================== */

    private String postOpenRouter(Map<String, Object> payload) {
        // Configurar RestTemplate con timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 segundos para conectar
        factory.setReadTimeout(15000);    // 15 segundos para leer respuesta

        RestTemplate rest = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(payload, headers);

        try {
            System.out.println("🤖 Enviando request a OpenRouter...");
            ResponseEntity<Map> response = rest.exchange(openRouterUrl, HttpMethod.POST, entity, Map.class);
            System.out.println("✅ Response recibido: " + response.getStatusCode());

            // Navega el JSON: choices[0].message.content
            Map body = response.getBody();
            if (body == null) throw new RuntimeException("Respuesta vacía de OpenRouter");

            List choices = (List) body.get("choices");
            if (choices == null || choices.isEmpty()) throw new RuntimeException("Sin choices en respuesta IA");

            Map choice0 = (Map) choices.get(0);
            Map message = (Map) choice0.get("message");
            if (message == null) throw new RuntimeException("Sin message en respuesta IA");

            Object content = message.get("content");
            if (content == null) throw new RuntimeException("Sin content en respuesta IA");

            return content.toString();

        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.err.println("❌ Timeout o error de conexión con OpenRouter: " + e.getMessage());
            throw new RuntimeException("Timeout al llamar a OpenRouter: " + e.getMessage(), e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("❌ Error HTTP del cliente (4xx): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error HTTP al llamar a OpenRouter: " + e.getMessage(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            System.err.println("❌ Error HTTP del servidor (5xx): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("Error del servidor OpenRouter: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Error inesperado en postOpenRouter: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al llamar a OpenRouter: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String titulo, String descripcion) {
        return """
        Analiza el siguiente recuerdo conmemorativo de una persona que ya no está y clasifícalo.
        Devuelve SOLO un JSON minificado válido en UNA línea:
        {"categorias":["<1 a 3 de LISTA_CATEGORIAS>"],"momentos":["<1 a 3 de LISTA_MOMENTOS>"]}

        LISTA_CATEGORIAS=["familia","amigos","pareja","infancia","juventud","adultez","viajes","celebraciones","trabajo","comunidad","arte_cultura","fe_espiritualidad","salud_bienestar","despedidas_duelo","legado","otros"]
        LISTA_MOMENTOS=["amor_afecto","gratitud","nostalgia","alegria","tristeza","orgullo","superacion","reflexion","fe","paz","asombro","cotidiano"]

        Reglas:
        - Elige 1 a 3 etiquetas por lista, sin inventar nuevas.
        - Si dudas: "otros" o "cotidiano", solo si no tiene ninguna etiqueta.
        - Responde solo el JSON (sin markdown, sin comentarios).

        Título: "%s"
        Descripción: "%s"
        """.formatted(sanitize(titulo), sanitize(descripcion));
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ").trim();
    }

    private String extractJson(String text) {
        if (text == null) return null;
        String cleaned = text.replace("```json", "").replace("```", "").trim();
        Matcher m = JSON_BLOCK.matcher(cleaned);
        return m.find() ? m.group(0) : null;
    }

    private List<String> toStringList(Object any) {
        if (any == null) return List.of();
        if (any instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) if (o != null) out.add(o.toString());
            return out;
        }
        // si vino un string único
        return List.of(any.toString());
    }

    private List<String> normalizeAndClamp(List<String> items, List<String> allowed, String fallback, int max) {
        Set<String> acc = new LinkedHashSet<>();
        for (String raw : items) {
            if (raw == null) continue;
            String k = raw.trim().toLowerCase().replace(" ", "_");
            if (NORMALIZE.containsKey(k)) k = NORMALIZE.get(k);
            if (!allowed.contains(k)) k = fallback;
            acc.add(k);
            if (acc.size() >= max) break;
        }
        return new ArrayList<>(acc);
    }

    private Map<String, List<String>> fallback() {
        return Map.of("categorias", List.of("otros"), "momentos", List.of("cotidiano"));
    }


    /**
     * Determina si un recuerdo debe aparecer en la línea de tiempo.
     * Considera el título, descripción, y si tiene fecha (photoDate).
     *
     * @return true si debe ir en línea de tiempo, false si es contenido atemporal
     */
    public boolean debeIrEnLineaTiempo(String titulo, String descripcion, LocalDate photoDate) {
        // Si no tiene título ni descripción, usar solo la fecha
        if ((titulo == null || titulo.isBlank()) && (descripcion == null || descripcion.isBlank())) {
            // Si tiene fecha específica, probablemente va en timeline
            return photoDate != null;
        }

        // Si tiene fecha específica, es un fuerte indicador de que va en timeline
        boolean tieneFechaEspecifica = photoDate != null;

        String prompt = buildTimelinePrompt(titulo, descripcion, tieneFechaEspecifica);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("temperature", 0.2); // Más determinista
        payload.put("max_tokens", 50);
        payload.put("stream", false);
        payload.put("messages", List.of(Map.of("role","user","content", prompt)));

        try {
            System.out.println("⏰ Evaluando si va en línea de tiempo...");
            String text = postOpenRouter(payload);
            System.out.println("✅ Respuesta timeline recibida: " + text);

            String jsonText = extractJson(text);
            if (jsonText == null) {
                // Fallback: si tiene fecha, probablemente va en timeline
                System.out.println("⚠️ No se pudo extraer JSON, usando fallback basado en fecha");
                return tieneFechaEspecifica;
            }

            Map<String, Object> obj = MAPPER.readValue(jsonText, new TypeReference<>() {});
            Boolean result = extractBoolean(obj.get("esLineaTiempo"));

            System.out.println("✅ Decisión timeline: " + result);
            return result != null ? result : tieneFechaEspecifica;

        } catch (Exception e) {
            System.err.println("⚠️ Error evaluando timeline: " + e.getMessage());
            // Fallback: si tiene fecha, va en timeline
            return tieneFechaEspecifica;
        }
    }

    /* ==================== Helpers ==================== */

    private String buildTimelinePrompt(String titulo, String descripcion, boolean tieneFecha) {
        String fechaInfo = tieneFecha ? " (El recuerdo TIENE una fecha específica asociada)" : " (El recuerdo NO tiene fecha específica)";

        return """
    Determina si este recuerdo debe aparecer en una LÍNEA DE TIEMPO cronológica de una persona que ya no está.
    Devuelve SOLO un JSON minificado: {"esLineaTiempo": true/false}
    
    Criterios para esLineaTiempo = TRUE (eventos con momento específico):
    - Cumpleaños, aniversarios, graduaciones
    - Viajes, vacaciones, excursiones
    - Bodas, bautizos, celebraciones
    - Logros específicos (primer trabajo, ascenso, premio)
    - Eventos familiares o sociales
    - Momentos únicos o hitos importantes
    - Cualquier evento que sucedió en un momento específico del tiempo
    
    Criterios para esLineaTiempo = FALSE (contenido atemporal):
    - Gustos musicales, canciones favoritas
    - Películas, libros o series favoritas
    - Frases célebres, citas, reflexiones generales
    - Cartas o mensajes sin contexto temporal específico
    - Rasgos de personalidad o características generales
    - Consejos, valores o filosofía de vida
    - Comida favorita, hobbies generales
    - Descripciones generales sin evento específico
    
    %s
    
    Título: "%s"
    Descripción: "%s"
    
    Responde SOLO: {"esLineaTiempo": true} o {"esLineaTiempo": false}
    """.formatted(fechaInfo, sanitize(titulo), sanitize(descripcion));
    }

    private Boolean extractBoolean(Object value) {
        if (value == null) return true; // Por defecto true

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String str = value.toString().toLowerCase().trim();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "sí".equals(str);
    }

    /* ==================== PARA DOCUMENTALES ==================== */

    /**
     * Genera narración considerando el enfoque narrativo y tono emocional del usuario
     *
     * @param nombrePersona Nombre de la persona fallecida
     * @param titulo Título del recuerdo
     * @param descripcion Descripción del recuerdo
     * @param fecha Fecha del recuerdo (opcional)
     * @param posicion Posición en la línea de tiempo
     * @param narrativeFocus Enfoque narrativo definido por el usuario (ej: "enfocarse en su niñez y momentos familiares")
     * @param emotionalTone Tono emocional (nostalgic, joyful, formal, inspiring)
     * @return Texto narrativo de 1-2 oraciones para usar como subtítulo
     */
    public String generarNarracionRecuerdo(String nombrePersona, String titulo,
                                           String descripcion, LocalDate fecha,
                                           String posicion, String narrativeFocus,
                                           String emotionalTone) {

        if (titulo == null || titulo.isBlank()) {
            return "Un momento especial en la vida de " + nombrePersona;
        }

        String fechaTexto = fecha != null ?
                fecha.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES")))
                : "en algún momento de su vida";

        // Determinar estilo de narración según el tono emocional
        String estiloTono = switch (emotionalTone != null ? emotionalTone.toLowerCase() : "nostalgic") {
            case "joyful" -> "alegre y celebratorio, destacando la felicidad";
            case "formal" -> "respetuoso y elegante, con tono solemne";
            case "inspiring" -> "inspirador y motivacional, resaltando logros";
            default -> "nostálgico y emotivo, con calidez";
        };

        String prompt = buildNarrationPromptWithFocus(nombrePersona, titulo, descripcion, fechaTexto,
                posicion, narrativeFocus, estiloTono);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 80);
        payload.put("stream", false);
        payload.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            System.out.println("📝 Generando narración para: " + titulo);
            String text = postOpenRouter(payload);

            String jsonText = extractJson(text);
            if (jsonText == null) {
                return titulo;
            }

            Map<String, Object> obj = MAPPER.readValue(jsonText, new TypeReference<>() {});
            String narracion = obj.get("narracion") != null ? obj.get("narracion").toString() : titulo;

            narracion = narracion.trim()
                    .replace("\"", "")
                    .replace("\n", " ");

            if (narracion.length() > 150) {
                narracion = safeTrim(narracion, 150);
            }

            System.out.println("✅ Narración generada: " + narracion);
            return narracion;

        } catch (Exception e) {
            System.err.println("⚠️ Error generando narración: " + e.getMessage());
            return titulo;
        }
    }

    private String safeTrim(String text, int maxChars) {
        if (text == null) return null;
        text = text.trim();
        if (text.length() <= maxChars) return text;
        // Busca el último espacio antes de maxChars
        int lastSpace = text.lastIndexOf(' ', maxChars);
        if (lastSpace <= 0) {
            // si no hay espacio, corta en maxChars (muy improbable)
            return text.substring(0, maxChars).trim();
        }
        String trimmed = text.substring(0, lastSpace).trim();
        // Asegura que termine en punto si es una oración
        if (!trimmed.endsWith(".") && !trimmed.endsWith("!") && !trimmed.endsWith("?")) {
            trimmed = trimmed + ".";
        }
        return trimmed;
    }


    /**
     * Prompt actualizado con enfoque narrativo del usuario
     */
    private String buildNarrationPromptWithFocus(String nombrePersona, String titulo,
                                                 String descripcion, String fechaTexto,
                                                 String posicion, String narrativeFocus,
                                                 String estiloTono) {

        String enfoqueTexto = narrativeFocus != null && !narrativeFocus.isBlank()
                ? "\n\nENFOQUE DEL USUARIO: " + narrativeFocus + "\n(Considera este enfoque al crear la narración)"
                : "";

        return """
    Crea una narración emotiva y concisa (1-2 oraciones máximo 100 caracteres) para un documental 
    conmemorativo de %s. Esta narración aparecerá como subtítulo en el video.
    
    ESTILO REQUERIDO: %s
    
    La narración debe:
    - Ser emotiva pero no melodramática
    - Estar en tercera persona
    - Capturar la esencia del momento
    - Ser breve y directa (máximo 2 oraciones cortas)
    - No usar comillas ni caracteres especiales
    - Conectar emocionalmente con quien la lee
    - Seguir el estilo de tono indicado
    %s
    
    Contexto del momento:
    - Título: "%s"
    - Descripción: "%s"
    - Fecha: %s
    - Posición en la historia: %s
    
    Ejemplos según tono:
    
    Nostálgico:
    - "En 1985, Jorge descubrió su pasión por la música que marcaría toda su vida"
    - "Aquellos veranos en la playa, donde todo parecía eterno"
    
    Alegre:
    - "María celebró su graduación rodeada de todos los que amaba"
    - "Un día lleno de risas y momentos inolvidables"
    
    Formal:
    - "Don Roberto fue reconocido por su destacada trayectoria profesional"
    - "Un legado de dedicación y excelencia que perdura"
    
    Inspirador:
    - "Contra todo pronóstico, Ana cumplió su sueño de convertirse en doctora"
    - "Su determinación cambió el destino de toda su familia"
    
    MUY IMPORTANTE:
    - Máximo 100 caracteres
    
    Responde SOLO con JSON: {"narracion": "tu texto aquí"}
    """.formatted(
                sanitize(nombrePersona),
                estiloTono,
                enfoqueTexto,
                sanitize(titulo),
                sanitize(descripcion != null ? descripcion : ""),
                fechaTexto,
                posicion
        );
    }

    /* ==================== PARA CÁPSULAS ==================== */

    /**
     * Selecciona los recuerdos más relevantes para una cápsula basándose en el prompt del usuario
     *
     * @param userPrompt Prompt libre del usuario (ej: "Cumpleaños 80 de Lupi", "Navidad 2023")
     * @param nombrePersona Nombre de la persona del memorial
     * @param allMemories Todos los recuerdos disponibles del memorial
     * @param maxMemories Máximo de recuerdos a seleccionar (8-12)
     * @return Lista de recuerdos seleccionados, ordenados por relevancia
     */
    public List<Memory> seleccionarRecuerdosParaCapsula(String userPrompt, String nombrePersona,
                                                        List<Memory> allMemories, int maxMemories) {

        System.out.println("🤖 Seleccionando recuerdos para cápsula con prompt: " + userPrompt);
        System.out.println("📦 Total memories disponibles: " + allMemories.size());

        if (allMemories.isEmpty()) {
            return List.of();
        }

        // SIEMPRE usar IA para filtrar por relevancia, incluso si hay pocas memorias
        // La IA debe decidir qué memorias son relevantes al prompt

        try {
            // Crear resumen de cada recuerdo
            List<Map<String, String>> memorySummaries = new ArrayList<>();
            for (int i = 0; i < allMemories.size(); i++) {
                Memory m = allMemories.get(i);
                Map<String, String> summary = new HashMap<>();
                summary.put("index", String.valueOf(i));
                summary.put("title", m.getTitle() != null ? m.getTitle() : "Sin título");
                summary.put("description", m.getDescription() != null ?
                        (m.getDescription().length() > 100 ?
                                m.getDescription().substring(0, 100) + "..." :
                                m.getDescription()) :
                        "Sin descripción");
                summary.put("date", m.getPhotoDate() != null ? m.getPhotoDate().toString() : "Sin fecha");
                memorySummaries.add(summary);
            }

            // Ajustar maxMemories si hay menos disponibles
            int effectiveMax = Math.min(maxMemories, allMemories.size());

            String prompt = buildMemorySelectionPrompt(userPrompt, nombrePersona, memorySummaries, effectiveMax);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("temperature", 0.3);
            payload.put("max_tokens", 200);
            payload.put("stream", false);
            payload.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            String text = postOpenRouter(payload);
            String jsonText = extractJson(text);

            if (jsonText == null) {
                System.err.println("⚠️ No se pudo extraer JSON, usando selección por fecha");
                return selectMemoriesByDateFallback(allMemories, effectiveMax);
            }

            Map<String, Object> obj = MAPPER.readValue(jsonText, new TypeReference<>() {});
            List<Integer> selectedIndices = extractIntegerList(obj.get("indices"));

            if (selectedIndices == null || selectedIndices.isEmpty()) {
                System.err.println("⚠️ No se obtuvieron índices. No generando cápsula.");
                //return selectMemoriesByDateFallback(allMemories, effectiveMax);
                return List.of();
            }

            // Seleccionar memories según los índices
            List<Memory> selectedMemories = new ArrayList<>();
            for (Integer index : selectedIndices) {
                if (index >= 0 && index < allMemories.size()) {
                    selectedMemories.add(allMemories.get(index));
                }
            }

            // Si no se seleccionó ninguno, usar fallback
            if (selectedMemories.isEmpty()) {
                System.err.println("⚠️ No se seleccionaron recuerdos válidos, usando fallback");
                return selectMemoriesByDateFallback(allMemories, effectiveMax);
            }

            System.out.println("✅ Seleccionados " + selectedMemories.size() + " recuerdos con IA de " +
                    allMemories.size() + " disponibles");
            return selectedMemories;

        } catch (Exception e) {
            System.err.println("⚠️ Error en selección de recuerdos: " + e.getMessage());
            e.printStackTrace();
            return selectMemoriesByDateFallback(allMemories,
                    Math.min(maxMemories, allMemories.size()));
        }
    }

    private String buildMemorySelectionPrompt(String userPrompt, String nombrePersona,
                                              List<Map<String, String>> memorySummaries, int maxMemories) {
        StringBuilder memoriesText = new StringBuilder();
        for (Map<String, String> summary : memorySummaries) {
            memoriesText.append(String.format("[%s] Título: %s | Descripción: %s | Fecha: %s\n",
                    summary.get("index"),
                    summary.get("title"),
                    summary.get("description"),
                    summary.get("date")));
        }

        return """
        Eres un asistente que ayuda a crear cápsulas de video emotivas sobre %s.
        
        El usuario quiere crear una cápsula sobre: "%s"
        
        De la siguiente lista de recuerdos, selecciona SOLO los más relevantes que coincidan con el tema.
        
        RECUERDOS DISPONIBLES:
        %s
        
        INSTRUCCIONES CRÍTICAS:
        - Selecciona ÚNICAMENTE los recuerdos que estén directamente relacionados con el tema solicitado
        - Si el prompt menciona un evento específico (cumpleaños, boda, etc.), selecciona SOLO ese tipo de eventos
        - Si ningún recuerdo coincide bien con el tema, selecciona el más cercano temáticamente
        - NO incluyas recuerdos irrelevantes solo por rellenar
        - Máximo %d recuerdos (puedes seleccionar menos si no hay suficientes relevantes)
        
        Criterios de selección:
        1. Relevancia directa al tema en título o descripción (PRIORIDAD MÁXIMA)
        2. Si el prompt menciona una fecha/época, prioriza recuerdos de ese periodo
        3. Si el prompt menciona un tipo de evento, filtra solo ese tipo
        4. Coherencia temática entre los recuerdos seleccionados
        5. Prefiere recuerdos con fechas específicas sobre los que no tienen fecha
        
        
        IMPORTANTE:
            - Si NO hay recuerdos relacionados con el tema, responde: {"indices": []}
            - SOLO selecciona recuerdos que realmente coincidan con el tema
            - Es MEJOR devolver lista vacía que seleccionar recuerdos irrelevantes
            - El usuario prefiere un error claro que un video con contenido equivocado
           
        Ejemplos:
        - Prompt "viajes" pero solo hay cumpleaños: {"indices": []}
        - Prompt "cumpleaños" y hay índice 0,3,5: {"indices": [0,3,5]}
        - Prompt "música" pero solo hay deportes: {"indices": []}
       
        Responde SOLO con JSON: {"indices": [lista o vacío]}
        """.formatted(nombrePersona, userPrompt, memoriesText.toString(), maxMemories, maxMemories);
            }



    private List<Integer> extractIntegerList(Object value) {
        if (value == null) return null;

        if (value instanceof List<?> list) {
            List<Integer> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                } else if (item instanceof String) {
                    try {
                        result.add(Integer.parseInt(item.toString()));
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ No se pudo parsear: " + item);
                    }
                }
            }
            return result;
        }

        return null;
    }

    /**
     * Fallback: selecciona recuerdos más recientes con fecha
     */
    private List<Memory> selectMemoriesByDateFallback(List<Memory> allMemories, int maxMemories) {
        System.out.println("📅 Using date-based fallback selection");

        return allMemories.stream()
                .sorted((m1, m2) -> {
                    if (m1.getPhotoDate() == null && m2.getPhotoDate() == null) return 0;
                    if (m1.getPhotoDate() == null) return 1;
                    if (m2.getPhotoDate() == null) return -1;
                    return m2.getPhotoDate().compareTo(m1.getPhotoDate());
                })
                .limit(maxMemories)
                .collect(Collectors.toList());
    }

}

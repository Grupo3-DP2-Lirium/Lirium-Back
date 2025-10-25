package org.example.springboot_backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        - Si dudas: "otros" o "cotidiano".
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

}

package com.velvet.sakura.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    private final HttpClient httpClient;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String generateInterpretation(String question, String pastCard, String pastMeaning,
                                          String presentCard, String presentMeaning,
                                          String futureCard, String futureMeaning) {
        try {
            log.info("Llamando a Groq con el modelo: '{}' y API Key configurada: {}", model, apiKey != null && !apiKey.isBlank());

            boolean hasQuestion = question != null && !question.isBlank();

            String systemPrompt = """
                Eres una adivina mística especializada en el tarot de Cardcaptor Sakura.
                Tu tarea es reinterpretar, con un tono cálido, evocador y ligeramente misterioso,
                el significado YA DADO de tres cartas (pasado, presente, futuro).

                Reglas estrictas:
                - NO inventes nuevos significados para las cartas: usa exclusivamente los significados
                  que se te proporcionan como base real.
                - Responde en español, en 3 párrafos cortos (uno por carta), y termina con una frase
                  de cierre breve a modo de consejo.
                - Mantén un tono cercano a Cardcaptor Sakura: mágico, esperanzador, nunca fatalista.
                """;

            String userPrompt;
            if (hasQuestion) {
                userPrompt = String.format("""
                    Pregunta de la persona: "%s"

                    Carta del Pasado: %s
                    Significado: %s

                    Carta del Presente: %s
                    Significado: %s

                    Carta del Futuro: %s
                    Significado: %s

                    Interpreta esta tirada conectando los significados directamente con la pregunta formulada.
                    """, question, pastCard, pastMeaning, presentCard, presentMeaning, futureCard, futureMeaning);
            } else {
                userPrompt = String.format("""
                    La persona no ha formulado ninguna pregunta concreta: quiere una lectura libre
                    de lo que las cartas tienen que decirle sobre su momento actual en general.

                    Carta del Pasado: %s
                    Significado: %s

                    Carta del Presente: %s
                    Significado: %s

                    Carta del Futuro: %s
                    Significado: %s

                    Interpreta esta tirada de forma libre, como una reflexión general sobre su presente,
                    sin asumir un tema o pregunta específica.
                    """, pastCard, pastMeaning, presentCard, presentMeaning, futureCard, futureMeaning);
            }

            var body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", 0.9);

            var messages = mapper.createArrayNode();
            var systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            var userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            body.set("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                log.error("Error devuelto por Groq [HTTP {}]: {}", response.statusCode(), response.body());
                throw new RuntimeException(
                        "Groq respondió con estado " + response.statusCode() + ": " + response.body());
            }

            JsonNode json = mapper.readTree(response.body());
            return json.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            log.error("Excepción al intentar llamar a la API de Groq: ", e);
            throw new RuntimeException("No se pudo generar la interpretación", e);
        }
    }
}
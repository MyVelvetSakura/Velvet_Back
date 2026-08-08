package com.velvet.sakura.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private OpenAIServiceImpl openAIService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAIService, "apiKey", "fake-api-key-para-test");
        ReflectionTestUtils.setField(openAIService, "model", "llama-3.3-70b-versatile");
    }

    @Test
    void generateInterpretation_conRespuestaExitosaDeGroq_devuelveElTextoGenerado() throws Exception {
        String groqResponseJson = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "Una interpretación mágica conectada a tu pregunta."
                      }
                    }
                  ]
                }
                """;

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(groqResponseJson);

        String result = openAIService.generateInterpretation(
                "¿Cómo será mi semana?",
                "Viento", "Simboliza el intelecto",
                "Brillo", "Simboliza la iluminación",
                "Flote", "Simboliza una visión general");

        assertThat(result).isEqualTo("Una interpretación mágica conectada a tu pregunta.");
    }

    @Test
    void generateInterpretation_conPreguntaVacia_construyeLaPeticionIgualmenteYDevuelveTexto() throws Exception {
        String groqResponseJson = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "Una lectura libre de lo que dicen las cartas."
                      }
                    }
                  ]
                }
                """;

        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(groqResponseJson);

        String result = openAIService.generateInterpretation(
                "",
                "Viento", "Simboliza el intelecto",
                "Brillo", "Simboliza la iluminación",
                "Flote", "Simboliza una visión general");

        assertThat(result).isEqualTo("Una lectura libre de lo que dicen las cartas.");
    }

    @Test
    void generateInterpretation_conEstadoHttpDeError_lanzaRuntimeException() throws Exception {
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpResponse.body()).thenReturn("{\"error\":\"Invalid API key\"}");

        assertThatThrownBy(() -> openAIService.generateInterpretation(
                "¿Pregunta?", "Viento", "Significado", "Brillo", "Significado", "Flote", "Significado"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo generar la interpretación");
    }

    @Test
    void generateInterpretation_cuandoLaLlamadaHttpFalla_lanzaRuntimeException() throws Exception {
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("Timeout de red"));

        assertThatThrownBy(() -> openAIService.generateInterpretation(
                "¿Pregunta?", "Viento", "Significado", "Brillo", "Significado", "Flote", "Significado"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo generar la interpretación");
    }
}
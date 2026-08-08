package com.velvet.sakura.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velvet.sakura.dto.request.CreateReadingRequest;
import com.velvet.sakura.dto.request.UpdateNameRequest;
import com.velvet.sakura.dto.response.ReadingResponse;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.security.CustomUserDetailsService;
import com.velvet.sakura.security.JwtService;
import com.velvet.sakura.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private ReadingService readingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void create_conDatosValidos_devuelve201YLaLecturaCreada() throws Exception {
        CreateReadingRequest request = new CreateReadingRequest();
        request.setUserId(10L);
        request.setName("Mi primera tirada");
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);
        request.setDeckType(DeckType.SAKURA);
        request.setQuestion("¿Cómo será mi semana?");
        request.setInterpretation("Una interpretación de prueba");

        ReadingResponse response = new ReadingResponse(
                1L, 10L, LocalDateTime.now(), "Mi primera tirada",
                1L, 2L, 3L, DeckType.SAKURA,
                "¿Cómo será mi semana?", "Una interpretación de prueba"
        );

        when(readingService.createReading(any())).thenReturn(response);

        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mi primera tirada"))
                .andExpect(jsonPath("$.deckType").value("SAKURA"));
    }

    @Test
    void create_sinNombre_devuelve400PorValidacion() throws Exception {
        CreateReadingRequest request = new CreateReadingRequest();
        request.setUserId(10L);
        request.setName(""); // @NotBlank falla
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);
        request.setDeckType(DeckType.SAKURA);

        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_sinCartaDePasado_devuelve400PorValidacion() throws Exception {
        CreateReadingRequest request = new CreateReadingRequest();
        request.setUserId(10L);
        request.setName("Tirada incompleta");
        request.setPastCardId(null); // @NotNull falla
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);
        request.setDeckType(DeckType.SAKURA);

        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getByUserId_devuelveLaListaDeLecturasDelUsuario() throws Exception {
        ReadingResponse reading1 = new ReadingResponse(
                1L, 10L, LocalDateTime.now(), "Tirada 1",
                1L, 2L, 3L, DeckType.SAKURA, null, null
        );
        ReadingResponse reading2 = new ReadingResponse(
                2L, 10L, LocalDateTime.now(), "Tirada 2",
                4L, 5L, 6L, DeckType.CLOW, null, null
        );

        when(readingService.findByUserId(10L)).thenReturn(List.of(reading1, reading2));

        mockMvc.perform(get("/api/readings").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Tirada 1"))
                .andExpect(jsonPath("$[1].name").value("Tirada 2"));
    }

    @Test
    void getByUserId_sinLecturas_devuelveListaVacia() throws Exception {
        when(readingService.findByUserId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/readings").param("userId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }


    @Test
    void getById_conLecturaExistente_devuelveLaLecturaCompleta() throws Exception {
        ReadingResponse response = new ReadingResponse(
                1L, 10L, LocalDateTime.now(), "Mi tirada",
                1L, 2L, 3L, DeckType.SAKURA,
                "¿Pregunta?", "Interpretación"
        );

        when(readingService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/readings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.interpretation").value("Interpretación"));
    }

    @Test
    void getById_conLecturaInexistente_devuelve404() throws Exception {
        when(readingService.findById(999L)).thenThrow(new ResourceNotFoundException("Lectura no encontrada"));

        mockMvc.perform(get("/api/readings/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateName_conLecturaExistente_devuelve200ConElNuevoNombre() throws Exception {
        UpdateNameRequest request = new UpdateNameRequest();
        request.setName("Nombre actualizado");

        ReadingResponse response = new ReadingResponse(
                1L, 10L, LocalDateTime.now(), "Nombre actualizado",
                1L, 2L, 3L, DeckType.SAKURA, null, null
        );

        when(readingService.updateName(eq(1L), eq("Nombre actualizado"))).thenReturn(response);

        mockMvc.perform(patch("/api/readings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre actualizado"));
    }


    @Test
    void delete_conLecturaExistente_devuelve200() throws Exception {
        mockMvc.perform(delete("/api/readings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_conLecturaInexistente_devuelve404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Lectura no encontrada"))
                .when(readingService).deleteReading(999L);

        mockMvc.perform(delete("/api/readings/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteAllByUser_conUsuarioValido_devuelve200() throws Exception {
        mockMvc.perform(delete("/api/readings").param("userId", "10"))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(readingService).deleteAllByUserId(10L);
    }
}
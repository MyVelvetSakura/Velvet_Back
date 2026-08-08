package com.velvet.sakura.controller;

import com.velvet.sakura.dto.response.CardResponse;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.security.CustomUserDetailsService;
import com.velvet.sakura.security.JwtService;
import com.velvet.sakura.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getDeck_conParametroSakura_devuelveLasCartasDeEseMazo() throws Exception {
        CardResponse viento = new CardResponse(1L, "viento", "Viento", "Simboliza el intelecto",
                "http://img/viento.jpg", "http://img/reverso.jpg");

        when(cardService.findByDeckType(DeckType.SAKURA)).thenReturn(List.of(viento));

        mockMvc.perform(get("/api/cards").param("deckType", "SAKURA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spanishName").value("Viento"));
    }

    @Test
    void getDeck_sinParametroDeckType_usaSakuraPorDefecto() throws Exception {
        CardResponse viento = new CardResponse(1L, "viento", "Viento", "Simboliza el intelecto",
                "http://img/viento.jpg", "http://img/reverso.jpg");

        when(cardService.findByDeckType(DeckType.SAKURA)).thenReturn(List.of(viento));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getDeck_conMazoClow_devuelveSoloCartasClow() throws Exception {
        CardResponse vientoClow = new CardResponse(2L, "viento", "Viento", "Simboliza el intelecto",
                "http://img/viento-clow.jpg", "http://img/reverso-clow.jpg");

        when(cardService.findByDeckType(DeckType.CLOW)).thenReturn(List.of(vientoClow));

        mockMvc.perform(get("/api/cards").param("deckType", "CLOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardImageUrl").value("http://img/viento-clow.jpg"));
    }

    @Test
    void getById_conCartaExistente_devuelveLaCartaCompleta() throws Exception {
        CardResponse sombra = new CardResponse(3L, "sombra", "Sombra", "Indica el sigilo",
                "http://img/sombra.jpg", "http://img/reverso.jpg");

        when(cardService.findById(3L)).thenReturn(sombra);

        mockMvc.perform(get("/api/cards/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spanishName").value("Sombra"))
                .andExpect(jsonPath("$.meaning").value("Indica el sigilo"));
    }

    @Test
    void getById_conCartaInexistente_devuelve404() throws Exception {
        when(cardService.findById(999L)).thenThrow(new ResourceNotFoundException("Carta no encontrada con id 999"));

        mockMvc.perform(get("/api/cards/999"))
                .andExpect(status().isNotFound());
    }
}
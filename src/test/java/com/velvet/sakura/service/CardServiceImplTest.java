package com.velvet.sakura.service;

import com.velvet.sakura.dto.response.CardResponse;
import com.velvet.sakura.entity.Card;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void findByDeckType_conMazoSakura_devuelveSoloCartasDeEseMazo() {
        Card viento = Card.builder()
                .id(1L).code("viento").spanishName("Viento")
                .meaning("Simboliza el intelecto")
                .cardImageUrl("http://img/viento-sakura.jpg")
                .reverseImageUrl("http://img/reverso-sakura.jpg")
                .deckType(DeckType.SAKURA)
                .build();

        when(cardRepository.findByDeckType(DeckType.SAKURA)).thenReturn(List.of(viento));

        List<CardResponse> result = cardService.findByDeckType(DeckType.SAKURA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).spanishName()).isEqualTo("Viento");
        assertThat(result.get(0).code()).isEqualTo("viento");
    }

    @Test
    void findByDeckType_sinCartasParaEseMazo_devuelveListaVacia() {
        when(cardRepository.findByDeckType(DeckType.CLOW)).thenReturn(List.of());

        List<CardResponse> result = cardService.findByDeckType(DeckType.CLOW);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_conCartaExistente_devuelveLaCartaCompleta() {
        Card sombra = Card.builder()
                .id(3L).code("sombra").spanishName("Sombra")
                .meaning("Indica el sigilo")
                .cardImageUrl("http://img/sombra.jpg")
                .reverseImageUrl("http://img/reverso.jpg")
                .deckType(DeckType.SAKURA)
                .build();

        when(cardRepository.findById(3L)).thenReturn(Optional.of(sombra));

        CardResponse response = cardService.findById(3L);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.spanishName()).isEqualTo("Sombra");
        assertThat(response.meaning()).isEqualTo("Indica el sigilo");
    }

    @Test
    void findById_conCartaInexistente_lanzaResourceNotFoundException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
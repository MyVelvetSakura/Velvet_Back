package com.velvet.sakura.repository;

import com.velvet.sakura.entity.Card;
import com.velvet.sakura.entity.DeckType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CardRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private CardRepository cardRepository;

    @Test
    void findByDeckType_devuelveSoloLasCartasDelMazoIndicado() {
        Card vientoSakura = Card.builder()
                .code("viento").spanishName("Viento").meaning("Simboliza el intelecto")
                .cardImageUrl("http://img/viento-sakura.jpg")
                .reverseImageUrl("http://img/reverso-sakura.jpg")
                .deckType(DeckType.SAKURA)
                .build();

        Card vientoClow = Card.builder()
                .code("viento").spanishName("Viento").meaning("Simboliza el intelecto")
                .cardImageUrl("http://img/viento-clow.jpg")
                .reverseImageUrl("http://img/reverso-clow.jpg")
                .deckType(DeckType.CLOW)
                .build();

        cardRepository.save(vientoSakura);
        cardRepository.save(vientoClow);

        List<Card> sakuraCards = cardRepository.findByDeckType(DeckType.SAKURA);
        List<Card> clowCards = cardRepository.findByDeckType(DeckType.CLOW);

        assertThat(sakuraCards).hasSize(1);
        assertThat(sakuraCards.get(0).getCardImageUrl()).isEqualTo("http://img/viento-sakura.jpg");
        assertThat(clowCards).hasSize(1);
        assertThat(clowCards.get(0).getCardImageUrl()).isEqualTo("http://img/viento-clow.jpg");
    }

    @Test
    void findByDeckType_sinCartasParaEseMazo_devuelveListaVacia() {
        List<Card> result = cardRepository.findByDeckType(DeckType.CLOW);

        assertThat(result).isEmpty();
    }

    @Test
    void save_conCodeYDeckTypeDuplicados_lanzaExcepcionPorConstraintUnique() {
        Card carta1 = Card.builder()
                .code("viento").spanishName("Viento").meaning("Significado")
                .cardImageUrl("http://img/1.jpg").reverseImageUrl("http://img/r1.jpg")
                .deckType(DeckType.SAKURA)
                .build();
        cardRepository.saveAndFlush(carta1);

        Card carta2 = Card.builder()
                .code("viento").spanishName("Viento (duplicado)").meaning("Otro significado")
                .cardImageUrl("http://img/2.jpg").reverseImageUrl("http://img/r2.jpg")
                .deckType(DeckType.SAKURA)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> cardRepository.saveAndFlush(carta2)
        );
    }
}
package com.velvet.sakura.repository;

import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.entity.Reading;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReadingRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ReadingRepository readingRepository;

    @Test
    void findByUserId_devuelveSoloLasLecturasDeEseUsuario() {
        Reading readingUsuario1 = Reading.builder()
                .userId(1L).name("Tirada A").date(LocalDateTime.now())
                .pastCardId(1L).presentCardId(2L).futureCardId(3L)
                .deckType(DeckType.SAKURA)
                .build();

        Reading readingUsuario2 = Reading.builder()
                .userId(2L).name("Tirada B").date(LocalDateTime.now())
                .pastCardId(4L).presentCardId(5L).futureCardId(6L)
                .deckType(DeckType.CLOW)
                .build();

        readingRepository.save(readingUsuario1);
        readingRepository.save(readingUsuario2);

        List<Reading> result = readingRepository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tirada A");
    }

    @Test
    void findByUserId_sinLecturas_devuelveListaVacia() {
        List<Reading> result = readingRepository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_conPreguntaEInterpretacionLargas_persisteElTextoCompletoComoTIPO_TEXT() {
        String textoLargo = "a".repeat(2000);

        Reading reading = Reading.builder()
                .userId(1L).name("Tirada larga").date(LocalDateTime.now())
                .pastCardId(1L).presentCardId(2L).futureCardId(3L)
                .deckType(DeckType.SAKURA)
                .question(textoLargo)
                .interpretation(textoLargo)
                .build();

        Reading saved = readingRepository.saveAndFlush(reading);
        readingRepository.flush();

        Reading found = readingRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getQuestion()).hasSize(2000);
        assertThat(found.getInterpretation()).hasSize(2000);
    }
}
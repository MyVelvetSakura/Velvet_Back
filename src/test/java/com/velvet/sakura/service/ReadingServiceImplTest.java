package com.velvet.sakura.service;

import com.velvet.sakura.dto.request.CreateReadingRequest;
import com.velvet.sakura.dto.response.ReadingResponse;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.entity.Reading;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReadingServiceImplTest {

    @Mock private ReadingRepository readingRepository;
    @Mock private ProgressService progressService;

    @InjectMocks
    private ReadingServiceImpl readingService;

    private Reading reading;

    @BeforeEach
    void setUp() {
        reading = Reading.builder()
                .id(1L)
                .userId(10L)
                .name("Mi primera tirada")
                .pastCardId(1L)
                .presentCardId(2L)
                .futureCardId(3L)
                .deckType(DeckType.SAKURA)
                .question("¿Cómo será mi semana?")
                .interpretation("Una interpretación de prueba")
                .build();
    }


    @Test
    void createReading_conDatosValidos_guardaLaLecturaYRegistraElProgreso() {
        CreateReadingRequest request = new CreateReadingRequest();
        request.setUserId(10L);
        request.setName("Mi primera tirada");
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);
        request.setDeckType(DeckType.SAKURA);
        request.setQuestion("¿Cómo será mi semana?");
        request.setInterpretation("Una interpretación de prueba");

        when(readingRepository.save(any(Reading.class))).thenReturn(reading);

        ReadingResponse response = readingService.createReading(request);

        assertThat(response.name()).isEqualTo("Mi primera tirada");
        assertThat(response.deckType()).isEqualTo(DeckType.SAKURA);
        assertThat(response.question()).isEqualTo("¿Cómo será mi semana?");
        assertThat(response.interpretation()).isEqualTo("Una interpretación de prueba");

        verify(progressService).registerReadingCompleted(
                eq(10L), eq("SAKURA"), eq(1L), eq(2L), eq(3L)
        );
    }

    @Test
    void createReading_sinPreguntaNiInterpretacion_seGuardaIgualmente() {
        CreateReadingRequest request = new CreateReadingRequest();
        request.setUserId(10L);
        request.setName("Tirada libre");
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);
        request.setDeckType(DeckType.CLOW);

        Reading savedWithoutQuestion = Reading.builder()
                .id(2L)
                .userId(10L)
                .name("Tirada libre")
                .pastCardId(1L)
                .presentCardId(2L)
                .futureCardId(3L)
                .deckType(DeckType.CLOW)
                .build();

        when(readingRepository.save(any(Reading.class))).thenReturn(savedWithoutQuestion);

        ReadingResponse response = readingService.createReading(request);

        assertThat(response.question()).isNull();
        assertThat(response.interpretation()).isNull();
        verify(progressService).registerReadingCompleted(
                eq(10L), eq("CLOW"), eq(1L), eq(2L), eq(3L)
        );
    }


    @Test
    void findByUserId_devuelveTodasLasLecturasDelUsuario() {
        Reading otraLectura = Reading.builder()
                .id(2L).userId(10L).name("Otra tirada")
                .pastCardId(4L).presentCardId(5L).futureCardId(6L)
                .deckType(DeckType.SAKURA)
                .build();

        when(readingRepository.findByUserId(10L)).thenReturn(List.of(reading, otraLectura));

        List<ReadingResponse> result = readingService.findByUserId(10L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReadingResponse::name)
                .containsExactlyInAnyOrder("Mi primera tirada", "Otra tirada");
    }

    @Test
    void findByUserId_sinLecturas_devuelveListaVacia() {
        when(readingRepository.findByUserId(99L)).thenReturn(List.of());

        List<ReadingResponse> result = readingService.findByUserId(99L);

        assertThat(result).isEmpty();
    }


    @Test
    void updateName_conLecturaExistente_actualizaYDevuelveElNuevoNombre() {
        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(readingRepository.save(any(Reading.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingResponse response = readingService.updateName(1L, "Nombre actualizado");

        assertThat(response.name()).isEqualTo("Nombre actualizado");
        verify(readingRepository).save(reading);
    }

    @Test
    void updateName_conLecturaInexistente_lanzaResourceNotFoundException() {
        when(readingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingService.updateName(99L, "Nombre"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrada");

        verify(readingRepository, never()).save(any());
    }


    @Test
    void deleteReading_conLecturaExistente_laElimina() {
        when(readingRepository.existsById(1L)).thenReturn(true);

        readingService.deleteReading(1L);

        verify(readingRepository).deleteById(1L);
    }

    @Test
    void deleteReading_conLecturaInexistente_lanzaResourceNotFoundException() {
        when(readingRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> readingService.deleteReading(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(readingRepository, never()).deleteById(any());
    }


    @Test
    void deleteAllByUserId_conVariasLecturas_lasEliminaTodas() {
        Reading otraLectura = Reading.builder()
                .id(2L).userId(10L).name("Otra tirada")
                .pastCardId(4L).presentCardId(5L).futureCardId(6L)
                .deckType(DeckType.SAKURA)
                .build();

        List<Reading> readings = List.of(reading, otraLectura);
        when(readingRepository.findByUserId(10L)).thenReturn(readings);

        readingService.deleteAllByUserId(10L);

        verify(readingRepository).deleteAll(readings);
    }

    @Test
    void deleteAllByUserId_sinLecturas_llamaDeleteAllConListaVacia() {
        when(readingRepository.findByUserId(10L)).thenReturn(List.of());

        readingService.deleteAllByUserId(10L);

        verify(readingRepository).deleteAll(List.of());
    }


    @Test
    void findById_conLecturaExistente_devuelveLaLecturaCompleta() {
        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));

        ReadingResponse response = readingService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Mi primera tirada");
        assertThat(response.question()).isEqualTo("¿Cómo será mi semana?");
    }

    @Test
    void findById_conLecturaInexistente_lanzaResourceNotFoundException() {
        when(readingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
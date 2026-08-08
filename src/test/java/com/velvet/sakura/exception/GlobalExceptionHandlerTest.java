package com.velvet.sakura.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_conResourceNotFoundException_devuelve404ConElMensaje() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Cuenta no encontrada");

        ResponseEntity<String> response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Cuenta no encontrada");
    }

    @Test
    void handleBadRequest_conIllegalArgumentException_devuelve400ConElMensaje() {
        IllegalArgumentException exception = new IllegalArgumentException("El nombre ya está registrado");

        ResponseEntity<String> response = handler.handleBadRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("El nombre ya está registrado");
    }

    @Test
    void handleConflict_conIllegalStateException_devuelve409ConElMensaje() {
        IllegalStateException exception = new IllegalStateException("Debes verificar tu cuenta antes de iniciar sesión.");

        ResponseEntity<String> response = handler.handleConflict(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Debes verificar tu cuenta antes de iniciar sesión.");
    }

    @Test
    void handleNotFound_conMensajeVacio_devuelve404ConBodyVacio() {
        ResourceNotFoundException exception = new ResourceNotFoundException("");

        ResponseEntity<String> response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEmpty();
    }
}
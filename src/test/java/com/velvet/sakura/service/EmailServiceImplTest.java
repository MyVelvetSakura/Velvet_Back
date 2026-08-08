package com.velvet.sakura.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage realMimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");
        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new Properties());
        realMimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
    }

    @Test
    void sendVerificationEmail_construyeYEnviaElCorreoConElAsuntoYDestinatarioCorrectos() throws Exception {
        when(templateEngine.process(eq("email-template"), any(Context.class)))
                .thenReturn("<html><body>Verifica tu cuenta</body></html>");

        emailService.sendVerificationEmail("nueva@gmail.com", "NuevaUsuaria", "token-abc-123");

        verify(mailSender).send(realMimeMessage);
        assertThat(realMimeMessage.getSubject()).isEqualTo("Verifica tu cuenta en Velvet Sakura");
        assertThat(realMimeMessage.getAllRecipients()[0].toString()).isEqualTo("nueva@gmail.com");
    }

    @Test
    void sendVerificationEmail_pasaElEnlaceDeVerificacionCorrectoALaPlantilla() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email-template"), contextCaptor.capture()))
                .thenReturn("<html></html>");

        emailService.sendVerificationEmail("nueva@gmail.com", "NuevaUsuaria", "token-abc-123");

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("link"))
                .isEqualTo("http://localhost:5173/verify-account?token=token-abc-123");
        assertThat(capturedContext.getVariable("title").toString())
                .contains("NuevaUsuaria");
    }

    @Test
    void sendPasswordResetEmail_pasaElCodigoCorrectoALaPlantillaSinEnlace() throws Exception {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email-template"), contextCaptor.capture()))
                .thenReturn("<html></html>");

        emailService.sendPasswordResetEmail("ragnarok1@gmail.com", "Ragnarok1", "483920");

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("code")).isEqualTo("483920");

        verify(mailSender).send(realMimeMessage);
        assertThat(realMimeMessage.getSubject()).isEqualTo("Tu código de recuperación de Velvet Sakura");
    }

    @Test
    void sendSecurityAlertEmail_incluyeLaIpYLaUbicacionEnElCuerpoDelMensaje() throws Exception {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email-template"), contextCaptor.capture()))
                .thenReturn("<html></html>");

        emailService.sendSecurityAlertEmail("ragnarok1@gmail.com", "Ragnarok1", "203.0.113.1", "Madrid, España");

        Context capturedContext = contextCaptor.getValue();
        String body = capturedContext.getVariable("body").toString();

        assertThat(body).contains("203.0.113.1");
        assertThat(body).contains("Madrid, España");

        assertThat(realMimeMessage.getSubject()).contains("Alerta de seguridad");
    }

    @Test
    void sendAccountDeletionEmail_pasaElEnlaceDeConfirmacionCorrecto() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email-template"), contextCaptor.capture()))
                .thenReturn("<html></html>");

        emailService.sendAccountDeletionEmail("ragnarok1@gmail.com", "Ragnarok1", "token-delete-456");

        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("link"))
                .isEqualTo("http://localhost:5173/confirm-delete-account?token=token-delete-456");
    }
}
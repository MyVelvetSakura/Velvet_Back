package com.velvet.sakura.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;
    private final RestClient restClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailServiceImpl(TemplateEngine templateEngine, 
                            @Value("${resend.api.key}") String apiKey) {
        this.templateEngine = templateEngine;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "verify-account?token=" + token;

        Context context = new Context();
        context.setVariable("preheader", "Verifica tu cuenta");
        context.setVariable("title", "¡Bienvenid@ a Velvet Sakura, " + name + "!");
        context.setVariable("body",
                "Gracias por registrarte. Antes de empezar a echar tus cartas, necesitamos que confirmes que esta cuenta es tuya.");
        context.setVariable("link", link);
        context.setVariable("buttonText", "Verificar mi cuenta");
        context.setVariable("footerNote",
                "Este enlace caduca en 24 horas. Si no has sido tú quien se registró, puedes ignorar este correo.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "Verifica tu cuenta en Velvet Sakura", html);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String code) {
        Context context = new Context();
        context.setVariable("preheader", "Tu código de recuperación");
        context.setVariable("title", "Hola, " + name);
        context.setVariable("body", "Usa este código para crear una nueva contraseña. Caduca en 5 minutos.");
        context.setVariable("code", code);
        context.setVariable("footerNote", "Si no has sido tú, ignora este correo.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "Tu código de recuperación de Velvet Sakura", html);
    }

    @Override
    public void sendAccountDeletionEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/confirm-delete-account?token=" + token;

        Context context = new Context();
        context.setVariable("preheader", "Confirma la eliminación de tu cuenta");
        context.setVariable("title", "Hola, " + name);
        context.setVariable("body",
                "Has solicitado eliminar tu cuenta de Velvet Sakura. Esta acción es permanente y borrará todas tus lecturas guardadas. Si estás segura, confirma con el botón de abajo.");
        context.setVariable("link", link);
        context.setVariable("buttonText", "Eliminar mi cuenta definitivamente");
        context.setVariable("footerNote",
                "Este enlace caduca en 1 hora. Si no has sido tú, ignora este correo y tu cuenta seguirá intacta.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "Confirma la eliminación de tu cuenta en Velvet Sakura", html);
    }

    @Override
    public void sendSecurityAlertEmail(String toEmail, String name, String ip, String location) {
        Context context = new Context();
        context.setVariable("preheader", "Alerta de seguridad en tu cuenta");
        context.setVariable("title", "Hola, " + name);
        context.setVariable("body",
                "Hemos detectado 3 intentos fallidos de inicio de sesión en tu cuenta.\n\n" +
                        "Si no has sido tú, te recomendamos cambiar tu contraseña cuanto antes.\n\n" +
                        "Dirección IP: " + ip + "\n" +
                        "Ubicación aproximada: " + location);
        context.setVariable("link", frontendUrl + "/forgot-password");
        context.setVariable("buttonText", "Cambiar mi contraseña");
        context.setVariable("footerNote", "Si reconoces esta actividad, puedes ignorar este correo.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "⚠️ Alerta de seguridad en tu cuenta de Velvet Sakura", html);
    }

    private void send(String toEmail, String subject, String html) {
        try {
            Map<String, Object> requestBody = Map.of(
                "from", "Velvet Sakura <onboarding@resend.dev>",
                "to", List.of(toEmail),
                "subject", subject,
                "html", html
            );

            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo vía Resend API", e);
        }
    }
}
package com.velvet.sakura.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/verify-account?token=" + token;

        Context context = new Context();
        context.setVariable("preheader", "Verifica tu cuenta");
        context.setVariable("title", "¡Bienvenida a Velvet Sakura, " + name + "!");
        context.setVariable("body", "Gracias por registrarte. Antes de empezar a echar tus cartas, necesitamos que confirmes que esta cuenta es tuya.");
        context.setVariable("link", link);
        context.setVariable("buttonText", "Verificar mi cuenta");
        context.setVariable("footerNote", "Este enlace caduca en 24 horas. Si no has sido tú quien se registró, puedes ignorar este correo.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "Verifica tu cuenta en Velvet Sakura", html);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("preheader", "Recupera tu contraseña");
        context.setVariable("title", "Hola, " + name);
        context.setVariable("body", "Has solicitado crear una nueva contraseña para tu cuenta en Velvet Sakura. Haz click en el botón para continuar.");
        context.setVariable("link", link);
        context.setVariable("buttonText", "Crear nueva contraseña");
        context.setVariable("footerNote", "Este enlace caduca en 1 hora. Si no has sido tú, puedes ignorar este correo con total tranquilidad.");

        String html = templateEngine.process("email-template", context);
        send(toEmail, "Recupera tu contraseña en Velvet Sakura", html);
    }

    private void send(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("No se pudo enviar el correo", e);
        }
    }

    @Override
public void sendAccountDeletionEmail(String toEmail, String name, String token) {
    String link = frontendUrl + "/confirm-delete-account?token=" + token;

    Context context = new Context();
    context.setVariable("preheader", "Confirma la eliminación de tu cuenta");
    context.setVariable("title", "Hola, " + name);
    context.setVariable("body", "Has solicitado eliminar tu cuenta de Velvet Sakura. Esta acción es permanente y borrará todas tus lecturas guardadas. Si estás segura, confirma con el botón de abajo.");
    context.setVariable("link", link);
    context.setVariable("buttonText", "Eliminar mi cuenta definitivamente");
    context.setVariable("footerNote", "Este enlace caduca en 1 hora. Si no has sido tú, ignora este correo y tu cuenta seguirá intacta.");

    String html = templateEngine.process("email-template", context);
    send(toEmail, "Confirma la eliminación de tu cuenta en Velvet Sakura", html);
}
}
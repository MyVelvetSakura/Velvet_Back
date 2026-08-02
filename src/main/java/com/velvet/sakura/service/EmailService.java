package com.velvet.sakura.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String name, String token);

    void sendPasswordResetEmail(String toEmail, String name, String token);

    void sendAccountDeletionEmail(String toEmail, String name, String token);

    void sendSecurityAlertEmail(String toEmail, String name, String ip, String location);
}

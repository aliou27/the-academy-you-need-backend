package com.theacademyyouneed.the_academy_you_need_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends the email verification link to a newly registered user.
     */
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String subject = "Confirmez votre adresse email — The Academy You Need";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Bienvenue, %s ! 👋</h2>
                    <p>Merci de vous être inscrit sur <strong>The Academy You Need</strong>.</p>
                    <p>Cliquez sur le bouton ci-dessous pour confirmer votre adresse email :</p>
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s"
                           style="background-color: #3498db; color: white; padding: 14px 28px;
                                  text-decoration: none; border-radius: 6px; font-size: 16px;">
                            Confirmer mon email
                        </a>
                    </div>
                    <p style="color: #666; font-size: 14px;">
                        Ce lien expire dans <strong>24 heures</strong>.<br>
                        Si vous n'avez pas créé de compte, ignorez cet email.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                    <p style="color: #999; font-size: 12px;">The Academy You Need</p>
                </div>
                """.formatted(firstName != null ? firstName : "there", verifyUrl);

        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Sends a password reset link.
     * Ready to use in Phase 2.
     */
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String subject = "Réinitialisation de mot de passe — The Academy You Need";

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Réinitialisation de mot de passe</h2>
                    <p>Bonjour %s,</p>
                    <p>Vous avez demandé à réinitialiser votre mot de passe.</p>
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s"
                           style="background-color: #e74c3c; color: white; padding: 14px 28px;
                                  text-decoration: none; border-radius: 6px; font-size: 16px;">
                            Réinitialiser mon mot de passe
                        </a>
                    </div>
                    <p style="color: #666; font-size: 14px;">
                        Ce lien expire dans <strong>1 heure</strong>.<br>
                        Si vous n'avez pas fait cette demande, ignorez cet email.
                    </p>
                </div>
                """.formatted(firstName != null ? firstName : "", resetUrl);

        sendHtmlEmail(toEmail, subject, html);
    }

    // ─────────────────────────────────────────────
    //  Internal helper
    // ─────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = isHtml
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email sending failed");
        }
    }
}
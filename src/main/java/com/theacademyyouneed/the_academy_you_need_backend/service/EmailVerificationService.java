package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.entity.VerificationToken;
import com.theacademyyouneed.the_academy_you_need_backend.entity.VerificationToken.TokenType;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import com.theacademyyouneed.the_academy_you_need_backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // EMAIL VERIFICATION

    public void sendVerificationEmail(User user) {
        String rawToken = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(rawToken)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), rawToken);
        log.info("Verification email sent to: {}", user.getEmail());
    }

    public String verifyEmail(String rawToken) {
        VerificationToken token = findValidToken(rawToken, TokenType.EMAIL_VERIFICATION);

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for: {}", user.getEmail());
        return user.getEmail();
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if ((boolean) user.isEmailVerified()) {
            throw new RuntimeException("Cet email est déjà vérifié");
        }

        sendVerificationEmail(user);
    }

    // PASSWORD RESET

    public void sendPasswordResetEmail(String email) {
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            String rawToken = UUID.randomUUID().toString();

            VerificationToken resetToken = VerificationToken.builder()
                    .user(user)
                    .token(rawToken)
                    .type(TokenType.PASSWORD_RESET)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();

            tokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), rawToken);
            log.info("Password reset email sent to: {}", user.getEmail());
        });
    }

    public void resetPassword(String rawToken, String newPassword) {
        VerificationToken token = findValidToken(rawToken, TokenType.PASSWORD_RESET);

        token.setUsed(true);
        tokenRepository.save(token);

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());
    }

    // SHARED HELPER

    private VerificationToken findValidToken(String rawToken, TokenType expectedType) {
        VerificationToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("Lien invalide ou inexistant"));

        if (token.isUsed()) {
            throw new RuntimeException("Ce lien a déjà été utilisé");
        }

        if (token.isExpired()) {
            throw new RuntimeException("Ce lien a expiré. Faites une nouvelle demande.");
        }

        if (token.getType() != expectedType) {
            throw new RuntimeException("Type de lien invalide");
        }

        return token;
    }
}
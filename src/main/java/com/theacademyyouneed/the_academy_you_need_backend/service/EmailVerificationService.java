package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.entity.VerificationToken;
import com.theacademyyouneed.the_academy_you_need_backend.entity.VerificationToken.TokenType;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import com.theacademyyouneed.the_academy_you_need_backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // ─────────────────────────────────────────────
    //  SEND VERIFICATION EMAIL
    // ─────────────────────────────────────────────

    /**
     * Creates a token and sends the verification email.
     * Called right after registration in AuthService.
     */
    public void sendVerificationEmail(User user) {
        // UUID token — cryptographically random, unguessable
        String rawToken = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(rawToken)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName(),
                rawToken
        );

        log.info("Verification email sent to: {}", user.getEmail());
    }

    // ─────────────────────────────────────────────
    //  VERIFY TOKEN (when user clicks the link)
    // ─────────────────────────────────────────────

    /**
     * Validates the token and marks the user's email as verified.
     * Returns the user's email on success (used to auto-login after verification).
     */
    public String verifyEmail(String rawToken) {
        VerificationToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("Lien de vérification invalide"));

        if (token.isUsed()) {
            throw new RuntimeException("Ce lien a déjà été utilisé");
        }

        if (token.isExpired()) {
            throw new RuntimeException("Ce lien a expiré. Demandez un nouveau lien.");
        }

        if (token.getType() != TokenType.EMAIL_VERIFICATION) {
            throw new RuntimeException("Type de token invalide");
        }

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        // Verify the user
        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());

        return user.getEmail();
    }

    // ─────────────────────────────────────────────
    //  RESEND VERIFICATION EMAIL
    // ─────────────────────────────────────────────

    /**
     * Lets a user request a new verification email if theirs expired.
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if ((boolean) user.isEmailVerified()) {
            throw new RuntimeException("Cet email est déjà vérifié");
        }

        sendVerificationEmail(user);
    }
}
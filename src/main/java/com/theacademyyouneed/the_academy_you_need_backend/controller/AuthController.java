package com.theacademyyouneed.the_academy_you_need_backend.controller;

import com.theacademyyouneed.the_academy_you_need_backend.dto.*;
import com.theacademyyouneed.the_academy_you_need_backend.service.AuthService;
import com.theacademyyouneed.the_academy_you_need_backend.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request.getEmail(), request.getPassword()));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email vérifié avec succès. Vous pouvez vous connecter."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email requis"));
        }
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Email de vérification renvoyé."));
    }

    /**
     * Step 1 — POST /api/auth/forgot-password
     * Body: { "email": "user@example.com" }
     * Always returns 200 even if email not found (security best practice).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        emailVerificationService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un lien de réinitialisation a été envoyé."
        ));
    }

    /**
     * Step 2 — POST /api/auth/reset-password
     * Body: { "token": "xxx", "newPassword": "NewPass123!" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        emailVerificationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe réinitialisé avec succès. Vous pouvez vous connecter."
        ));
    }
}
package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.dto.AuthResponse;
import com.theacademyyouneed.the_academy_you_need_backend.dto.RegisterRequest;
import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import com.theacademyyouneed.the_academy_you_need_backend.security.Jwtutil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final Jwtutil jwtUtil;

    // ── Ajoutés ici grâce à @RequiredArgsConstructor ────────────────
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // ────────────────────────────────────────────────────────────────

    /**
     * Inscription d'un nouvel utilisateur + connexion automatique (retourne un token)
     */
    public AuthResponse register(RegisterRequest request) {

        // Normalisation de l'email (évite doublons admin@test.com / Admin@test.com)
        String email = request.getEmail().toLowerCase().trim();

        // Vérifie unicité
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Crée l'utilisateur
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : null)
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .role(User.Role.USER)
                .emailVerified(false)
                .build();

        // Sauvegarde
        user = userRepository.save(user);

        // Génère le token (avec l'email comme subject)
        String token = jwtUtil.generateToken(user.getEmail());

        // Rôle avec prefixe ROLE_ (cohérent avec Spring Security)
        String role = "ROLE_" + user.getRole().name();

        return new AuthResponse(token, user.getEmail(), role);
    }

    /**
     * Authentification (login) – ta méthode existante, légèrement améliorée
     */
    public AuthResponse authenticate(String email, String password) {
        try {
            // Authentifie via Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // Charge les détails de l'utilisateur
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Génère le token
        String token = jwtUtil.generateToken(userDetails.getUsername());

        // Récupère le rôle principal (avec ROLE_)
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return new AuthResponse(token, email, role);
    }
}
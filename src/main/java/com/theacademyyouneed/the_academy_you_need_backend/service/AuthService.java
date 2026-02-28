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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService; // ← NEW

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : null)
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .role(User.Role.USER)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // ✅ Send verification email after saving
        emailVerificationService.sendVerificationEmail(user);

        // Return token — user is logged in but email not yet verified
        // Your frontend should show a "please verify your email" banner
        String token = jwtUtil.generateToken(user.getEmail());
        String role = "ROLE_" + user.getRole().name();

        return new AuthResponse(token, user.getEmail(), role);
    }

    public AuthResponse authenticate(String email, String password) {
        // ── Optional: block unverified users from logging in ──────────
        // Uncomment this block once email flow is tested end-to-end:
        //
        // User user = userRepository.findByEmail(email)
        //     .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
        // if (!user.isEmailVerified()) {
        //     throw new RuntimeException("Veuillez vérifier votre email avant de vous connecter");
        // }
        // ──────────────────────────────────────────────────────────────

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails.getUsername());

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return new AuthResponse(token, email, role);
    }
}
package com.theacademyyouneed.the_academy_you_need_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive Basic Auth (le truc qui demande username/password en popup ou header)
                .httpBasic(httpBasic -> httpBasic.disable())

                // Autorise tout le monde sur tes endpoints API pour le moment
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()     // ← TOUS tes /api/... publics
                        .anyRequest().authenticated()               // le reste (si tu ajoutes pages web) protégé
                )

                // Optionnel : si tu veux garder une page login pour futur dashboard
                .formLogin(form -> form.disable())              // ou .permitAll() si tu veux la garder

                // Désactive CSRF pour les tests API (attention : réactive-le en prod pour POST/PUT/DELETE)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
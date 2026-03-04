package com.theacademyyouneed.the_academy_you_need_backend.config;

import com.theacademyyouneed.the_academy_you_need_backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive TOUT ce qui peut créer une session ou un cookie
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // Autorisations explicites
                .authorizeHttpRequests(auth -> auth
                        // Public total (login, register, verify, swagger, etc.)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//HttpMethod.GET,
                        // GET sur content → public (listing + détail)
                        .requestMatchers("/api/content/**").permitAll()

                        // POST, PUT, DELETE sur content → ADMIN seulement
                        .requestMatchers(HttpMethod.POST, "/api/content").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/content/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/content/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")
                        // Autres règles que tu avais
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/courses/**").hasAnyRole("USER", "ADMIN")

                        // Tout le reste → nécessite juste d’être connecté (pas forcément ADMIN)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Filtre JWT en premier
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
package com.theacademyyouneed.the_academy_you_need_backend.config;

import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                // Create admin user
                User admin = User.builder()
                        .email("admin@test.com")
                        .password(encoder.encode("password123"))
                        .firstName("Admin")
                        .lastName("User")
                        .role(User.Role.ADMIN)
                        .emailVerified(true)
                        .build();
                // Create regular user
                User regular = User.builder()
                        .email("user@test.com")
                        .password(encoder.encode("password123"))
                        .firstName("Regular")
                        .lastName("User")
                        .role(User.Role.USER)
                        .emailVerified(true)
                        .build();

                userRepository.save(admin);
                userRepository.save(regular);

                log.info(" Sample data loaded:");
                log.info("   - admin@test.com / password123 (ADMIN)");
                log.info("   - user@test.com / password123 (USER)");
            }
        };
    }
}
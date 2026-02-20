package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.dto.UserDTO;
import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toDTO(user);
    }

    public UserDTO createUser(String email, String password, String firstName, String lastName) {
        // Validate required fields
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(User.Role.USER)
                .emailVerified(false)
                .build();

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    public UserDTO updateUser(Long id, String firstName, String lastName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);

        User updated = userRepository.save(user);
        return toDTO(updated);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                (Boolean) user.isEmailVerified(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }

    public UserDTO registerUser(UserDTO userDTO) {
        if (!userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder().build();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        user = userRepository.save(user);   // ← ultra important !

        return toDTO(user);
    }

}
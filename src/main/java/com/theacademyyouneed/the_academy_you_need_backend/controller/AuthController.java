package com.theacademyyouneed.the_academy_you_need_backend.controller;

import com.theacademyyouneed.the_academy_you_need_backend.dto.LoginRequest;
import com.theacademyyouneed.the_academy_you_need_backend.dto.AuthResponse;
import com.theacademyyouneed.the_academy_you_need_backend.dto.UserDTO;
import com.theacademyyouneed.the_academy_you_need_backend.service.AuthService;
import com.theacademyyouneed.the_academy_you_need_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }



    // Optionnel : endpoint register (inscription) – on peut l'ajouter plus tard
    // @PostMapping("/register")
    // public ResponseEntity<?> register(@RequestBody RegisterRequest request) { ... }
}
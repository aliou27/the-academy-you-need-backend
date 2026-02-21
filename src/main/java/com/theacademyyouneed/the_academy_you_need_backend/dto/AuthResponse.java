package com.theacademyyouneed.the_academy_you_need_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private String password;
    private String message;

    public AuthResponse(String token) {
        this.token = token;
        this.email = email;
        this.role = role;
    }
}
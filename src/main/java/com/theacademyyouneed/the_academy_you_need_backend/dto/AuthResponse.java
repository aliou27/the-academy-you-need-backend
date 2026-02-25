package com.theacademyyouneed.the_academy_you_need_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AuthResponse {
    private String token;
    private String email;
    private String role;
}
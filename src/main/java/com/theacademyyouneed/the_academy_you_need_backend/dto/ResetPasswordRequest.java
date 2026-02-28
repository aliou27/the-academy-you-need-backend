package com.theacademyyouneed.the_academy_you_need_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Token requis")
    private String token;

    @NotBlank(message = "Mot de passe requis")
    @Size(min = 8, message = "Minimum 8 caractères")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).*$",
            message = "Doit contenir 1 majuscule, 1 chiffre, 1 caractère spécial"
    )
    private String newPassword;
}
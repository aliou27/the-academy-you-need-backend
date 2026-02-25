// dto/RegisterRequest.java
package com.theacademyyouneed.the_academy_you_need_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email requis")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Mot de passe requis")
    @Size(min = 8, message = "Minimum 8 caractères")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).*$",
            message = "Doit contenir 1 majuscule, 1 chiffre, 1 caractère spécial"
    )
    private String password;

    @NotBlank(message = "Prénom requis")
    @Size(min = 2, max = 50, message = "Entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Lettres uniquement")
    private String firstName;

    @NotBlank(message = "Nom requis")
    @Size(min = 2, max = 50, message = "Entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Lettres uniquement")
    private String lastName;
}
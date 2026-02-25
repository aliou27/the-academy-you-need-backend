// dto/RegisterRequest.java
package com.theacademyyouneed.the_academy_you_need_backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    // Optionnel pour l'instant, tu pourras ajouter plus tard
    private String firstName;
    private String lastName;
}
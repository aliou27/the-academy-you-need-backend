package com.theacademyyouneed.the_academy_you_need_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String password;        // null en sortie, utilisé uniquement en entrée
    private String firstName;
    private String lastName;
    private String role;
    private boolean emailVerified;
    private String createdAt;
    private String updatedAt;
}
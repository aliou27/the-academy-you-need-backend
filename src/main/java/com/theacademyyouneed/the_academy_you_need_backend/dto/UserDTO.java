package com.theacademyyouneed.the_academy_you_need_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean emailVerified;
    private String createdAt;
    private String updatedAt;
    private String password;

    public UserDTO(Long id, String email, String firstName, String lastName, String s, Boolean emailVerified, String s1, String s2) {
    }

    public @Nullable CharSequence getPassword() {
        return password;
    }
}

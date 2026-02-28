package com.theacademyyouneed.the_academy_you_need_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {

    private Long id;

    @NotBlank(message = "Titre requis")
    private String title;

    private String description;

    @NotNull(message = "Type requis")
    private String type;             // VIDEO, AUDIO, PDF, BOOK, POST, LINK

    private String accessLevel;      // FREE, PREMIUM — defaults to FREE

    private String fileUrl;          // set by server after upload
    private String thumbnailUrl;
    private String cloudinaryPublicId;

    private Integer durationSeconds;
    private Integer pageCount;
    private String externalUrl;      // for LINK type

    private List<Long> categoryIds;  // categories to attach

    private boolean published;

    private String authorEmail;      // set by server
    private String createdAt;
    private String updatedAt;
}
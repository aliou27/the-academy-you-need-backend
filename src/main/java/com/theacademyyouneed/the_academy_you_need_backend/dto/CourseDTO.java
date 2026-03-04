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
public class CourseDTO {

    private Long id;

    @NotBlank(message = "Titre requis")
    private String title;

    private String description;
    private String thumbnailUrl;

    @NotNull(message = "Niveau requis")
    private String level;

    private String accessLevel;
    private List<Long> contentIds;
    private Boolean published;
    private String authorEmail;
    private Integer totalContents;
    private String createdAt;
    private String updatedAt;
    private Boolean enrolled;
    private Boolean completed;
    private Integer progressPercent;
}
package com.theacademyyouneed.the_academy_you_need_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Builder.Default
    private int progressSeconds = 0;

    private LocalDateTime lastAccessedAt;
}
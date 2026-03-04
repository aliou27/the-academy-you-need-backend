package com.theacademyyouneed.the_academy_you_need_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    private LocalDateTime completedAt;
}
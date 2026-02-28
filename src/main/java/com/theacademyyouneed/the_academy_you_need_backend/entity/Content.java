package com.theacademyyouneed.the_academy_you_need_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.FREE;

    @Column(length = 1000)
    private String fileUrl;              // Cloudinary URL

    @Column(length = 1000)
    private String thumbnailUrl;

    @Column(length = 500)
    private String cloudinaryPublicId;   // needed to delete from Cloudinary

    private Integer durationSeconds;     // VIDEO / AUDIO

    private Integer pageCount;           // PDF / BOOK

    @Column(length = 1000)
    private String externalUrl;          // LINK type

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "content_categories",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ContentType {
        VIDEO, AUDIO, PDF, BOOK, POST, LINK
    }

    public enum AccessLevel {
        FREE, PREMIUM
    }
}
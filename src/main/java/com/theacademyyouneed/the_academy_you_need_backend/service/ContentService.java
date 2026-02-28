package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.dto.ContentDTO;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Category;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Content;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Content.AccessLevel;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Content.ContentType;
import com.theacademyyouneed.the_academy_you_need_backend.entity.User;
import com.theacademyyouneed.the_academy_you_need_backend.repository.CategoryRepository;
import com.theacademyyouneed.the_academy_you_need_backend.repository.ContentRepository;
import com.theacademyyouneed.the_academy_you_need_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContentService {

    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // ─────────────────────────────────────────────
    //  PUBLIC — listing (no auth required)
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ContentDTO> getPublishedContent(String type, String accessLevel, Pageable pageable) {
        Page<Content> page;

        if (type != null && accessLevel != null) {
            page = contentRepository.findByPublishedTrueAndTypeAndAccessLevel(
                    ContentType.valueOf(type.toUpperCase()),
                    AccessLevel.valueOf(accessLevel.toUpperCase()),
                    pageable);
        } else if (type != null) {
            page = contentRepository.findByPublishedTrueAndType(
                    ContentType.valueOf(type.toUpperCase()), pageable);
        } else if (accessLevel != null) {
            page = contentRepository.findByPublishedTrueAndAccessLevel(
                    AccessLevel.valueOf(accessLevel.toUpperCase()), pageable);
        } else {
            page = contentRepository.findByPublishedTrue(pageable);
        }

        return page.map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ContentDTO getById(Long id) {
        Content content = findOrThrow(id);
        if (!content.isPublished()) {
            throw new RuntimeException("Contenu introuvable");
        }
        return toDTO(content);
    }

    // ─────────────────────────────────────────────
    //  ADMIN — create / update / delete
    // ─────────────────────────────────────────────

    /**
     * Creates content with an optional file upload.
     * For POST/LINK types, file is null — no upload needed.
     */
    public ContentDTO createContent(ContentDTO dto, MultipartFile file, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        Content content = Content.builder()
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .type(ContentType.valueOf(dto.getType().toUpperCase()))
                .accessLevel(dto.getAccessLevel() != null
                        ? AccessLevel.valueOf(dto.getAccessLevel().toUpperCase())
                        : AccessLevel.FREE)
                .externalUrl(dto.getExternalUrl())
                .durationSeconds(dto.getDurationSeconds())
                .pageCount(dto.getPageCount())
                .author(author)
                .published(dto.isPublished())
                .build();

        // Upload file to Cloudinary if provided
        if (file != null && !file.isEmpty()) {
            String folder = dto.getType().toLowerCase() + "s"; // "videos", "audios", etc.
            Map<String, String> uploaded = cloudinaryService.upload(file, folder);
            content.setFileUrl(uploaded.get("url"));
            content.setCloudinaryPublicId(uploaded.get("publicId"));
        }

        // Attach categories
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(
                    categoryRepository.findAllById(dto.getCategoryIds()));
            content.setCategories(categories);
        }

        Content saved = contentRepository.save(content);
        log.info("Content created: {} ({})", saved.getTitle(), saved.getType());
        return toDTO(saved);
    }

    public ContentDTO updateContent(Long id, ContentDTO dto, MultipartFile file) {
        Content content = findOrThrow(id);

        if (dto.getTitle() != null) content.setTitle(dto.getTitle().trim());
        if (dto.getDescription() != null) content.setDescription(dto.getDescription());
        if (dto.getExternalUrl() != null) content.setExternalUrl(dto.getExternalUrl());
        if (dto.getDurationSeconds() != null) content.setDurationSeconds(dto.getDurationSeconds());
        if (dto.getPageCount() != null) content.setPageCount(dto.getPageCount());
        if (dto.getAccessLevel() != null)
            content.setAccessLevel(AccessLevel.valueOf(dto.getAccessLevel().toUpperCase()));
        content.setPublished(dto.isPublished());

        // Replace file if new one provided
        if (file != null && !file.isEmpty()) {
            // Delete old file from Cloudinary
            if (content.getCloudinaryPublicId() != null) {
                cloudinaryService.delete(content.getCloudinaryPublicId());
            }
            String folder = content.getType().name().toLowerCase() + "s";
            Map<String, String> uploaded = cloudinaryService.upload(file, folder);
            content.setFileUrl(uploaded.get("url"));
            content.setCloudinaryPublicId(uploaded.get("publicId"));
        }

        // Update categories
        if (dto.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(
                    categoryRepository.findAllById(dto.getCategoryIds()));
            content.setCategories(categories);
        }

        return toDTO(contentRepository.save(content));
    }

    public void deleteContent(Long id) {
        Content content = findOrThrow(id);

        // Clean up Cloudinary file
        if (content.getCloudinaryPublicId() != null) {
            cloudinaryService.delete(content.getCloudinaryPublicId());
        }

        contentRepository.delete(content);
        log.info("Content deleted: {}", id);
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private Content findOrThrow(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenu introuvable avec id: " + id));
    }

    private ContentDTO toDTO(Content c) {
        ContentDTO dto = new ContentDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setType(c.getType().name());
        dto.setAccessLevel(c.getAccessLevel().name());
        dto.setFileUrl(c.getFileUrl());
        dto.setThumbnailUrl(c.getThumbnailUrl());
        dto.setDurationSeconds(c.getDurationSeconds());
        dto.setPageCount(c.getPageCount());
        dto.setExternalUrl(c.getExternalUrl());
        dto.setPublished(c.isPublished());
        dto.setAuthorEmail(c.getAuthor() != null ? c.getAuthor().getEmail() : null);
        dto.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        dto.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
        return dto;
    }
}
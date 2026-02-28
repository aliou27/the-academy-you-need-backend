package com.theacademyyouneed.the_academy_you_need_backend.controller;

import com.theacademyyouneed.the_academy_you_need_backend.dto.ContentDTO;
import com.theacademyyouneed.the_academy_you_need_backend.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // ─────────────────────────────────────────────
    //  PUBLIC ENDPOINTS (no auth required)
    // ─────────────────────────────────────────────

    /**
     * GET /api/content?type=VIDEO&accessLevel=FREE&page=0&size=10
     * Lists all published content with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<ContentDTO>> getContent(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String accessLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(contentService.getPublishedContent(type, accessLevel, pageable));
    }

    /**
     * GET /api/content/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getById(id));
    }

    // ─────────────────────────────────────────────
    //  ADMIN ENDPOINTS (auth required)
    // ─────────────────────────────────────────────

    /**
     * POST /api/content
     * Multipart: "data" (JSON ContentDTO) + optional "file" (the actual file)
     *
     * Example with file:
     *   form-data: data={"title":"Lesson 1","type":"VIDEO","accessLevel":"FREE"}
     *              file=<your video file>
     *
     * Example without file (POST or LINK type):
     *   form-data: data={"title":"My Post","type":"POST","description":"..."}
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ContentDTO> createContent(
            @RequestPart("data") @Valid ContentDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ContentDTO created = contentService.createContent(dto, file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/content/{id}
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ContentDTO> updateContent(
            @PathVariable Long id,
            @RequestPart("data") ContentDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return ResponseEntity.ok(contentService.updateContent(id, dto, file));
    }

    /**
     * DELETE /api/content/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
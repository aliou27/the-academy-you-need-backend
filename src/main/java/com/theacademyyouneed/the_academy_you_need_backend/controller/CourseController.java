package com.theacademyyouneed.the_academy_you_need_backend.controller;

import com.theacademyyouneed.the_academy_you_need_backend.dto.CourseDTO;
import com.theacademyyouneed.the_academy_you_need_backend.service.CourseService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ─────────────────────────────────────────────
    //  PUBLIC
    // ─────────────────────────────────────────────

    /**
     * GET /api/courses?level=A1&accessLevel=FREE&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getCourses(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String accessLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(courseService.getPublishedCourses(level, accessLevel, pageable));
    }

    /**
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(courseService.getCourseById(id, email));
    }

    // ─────────────────────────────────────────────
    //  ADMIN
    // ─────────────────────────────────────────────

    /**
     * POST /api/courses
     * Body: { "title": "...", "level": "A1", "contentIds": [1, 2, 3] }
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(
            @RequestBody @Valid CourseDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        CourseDTO created = courseService.createCourse(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO dto) {

        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    /**
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    //  ENROLLMENT & PROGRESS (authenticated users)
    // ─────────────────────────────────────────────

    /**
     * POST /api/courses/{id}/enroll
     */
    @PostMapping("/{id}/enroll")
    public ResponseEntity<Map<String, String>> enroll(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        courseService.enrollUser(id, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Inscription au cours réussie"));
    }

    /**
     * GET /api/courses/my — user's enrolled courses
     */
    @GetMapping("/my")
    public ResponseEntity<List<CourseDTO>> myCourses(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(courseService.getUserEnrollments(userDetails.getUsername()));
    }

    /**
     * POST /api/courses/{courseId}/progress/{contentId}/complete
     */
    @PostMapping("/{courseId}/progress/{contentId}/complete")
    public ResponseEntity<Map<String, String>> markComplete(
            @PathVariable Long courseId,
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        courseService.markContentComplete(courseId, contentId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Contenu marqué comme terminé"));
    }

    /**
     * POST /api/courses/{courseId}/progress/{contentId}
     * Body: { "progressSeconds": 120 }
     */
    @PostMapping("/{courseId}/progress/{contentId}")
    public ResponseEntity<Map<String, String>> updateProgress(
            @PathVariable Long courseId,
            @PathVariable Long contentId,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        int seconds = body.getOrDefault("progressSeconds", 0);
        courseService.updateProgress(contentId, seconds, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Progression mise à jour"));
    }
}
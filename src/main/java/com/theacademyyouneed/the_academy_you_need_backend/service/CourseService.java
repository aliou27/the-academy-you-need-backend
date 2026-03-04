package com.theacademyyouneed.the_academy_you_need_backend.service;

import com.theacademyyouneed.the_academy_you_need_backend.dto.CourseDTO;
import com.theacademyyouneed.the_academy_you_need_backend.entity.*;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Course.AccessLevel;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Course.Level;
import com.theacademyyouneed.the_academy_you_need_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    @Transactional(readOnly = true)
    public Page<CourseDTO> getPublishedCourses(String level, String accessLevel, Pageable pageable) {
        Page<Course> page;

        if (level != null && accessLevel != null) {
            page = courseRepository.findByPublishedTrueAndLevelAndAccessLevel(
                    Level.valueOf(level.toUpperCase()),
                    AccessLevel.valueOf(accessLevel.toUpperCase()),
                    pageable);
        } else if (level != null) {
            page = courseRepository.findByPublishedTrueAndLevel(
                    Level.valueOf(level.toUpperCase()), pageable);
        } else if (accessLevel != null) {
            page = courseRepository.findByPublishedTrueAndAccessLevel(
                    AccessLevel.valueOf(accessLevel.toUpperCase()), pageable);
        } else {
            page = courseRepository.findByPublishedTrue(pageable);
        }

        return page.map(c -> toDTO(c, null));
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id, String userEmail) {
        Course course = findOrThrow(id);
        if (!course.isPublished()) throw new RuntimeException("Cours introuvable");
        return toDTO(course, userEmail);
    }

    public CourseDTO createCourse(CourseDTO dto, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("Auteur introuvable"));

        Course course = Course.builder()
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .level(Level.valueOf(dto.getLevel().toUpperCase()))
                .accessLevel(dto.getAccessLevel() != null
                        ? AccessLevel.valueOf(dto.getAccessLevel().toUpperCase())
                        : AccessLevel.FREE)
                .author(author)
                .published(Boolean.TRUE.equals(dto.getPublished()))
                .build();

        if (dto.getContentIds() != null && !dto.getContentIds().isEmpty()) {
            List<Content> contents = new ArrayList<>();
            for (Long contentId : dto.getContentIds()) {
                contents.add(contentRepository.findById(contentId)
                        .orElseThrow(() -> new RuntimeException("Contenu introuvable: " + contentId)));
            }
            course.setContents(contents);
        }

        Course saved = courseRepository.save(course);
        log.info("Course created: {}", saved.getTitle());
        return toDTO(saved, authorEmail);
    }

    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course course = findOrThrow(id);

        if (dto.getTitle() != null) course.setTitle(dto.getTitle().trim());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        if (dto.getLevel() != null) course.setLevel(Level.valueOf(dto.getLevel().toUpperCase()));
        if (dto.getAccessLevel() != null)
            course.setAccessLevel(AccessLevel.valueOf(dto.getAccessLevel().toUpperCase()));
        if (dto.getPublished() != null) course.setPublished(dto.getPublished());

        if (dto.getContentIds() != null) {
            List<Content> contents = new ArrayList<>();
            for (Long contentId : dto.getContentIds()) {
                contents.add(contentRepository.findById(contentId)
                        .orElseThrow(() -> new RuntimeException("Contenu introuvable: " + contentId)));
            }
            course.setContents(contents);
        }

        return toDTO(courseRepository.save(course), null);
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Cours introuvable: " + id);
        }
        courseRepository.deleteById(id);
        log.info("Course deleted: {}", id);
    }

    public void enrollUser(Long courseId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Course course = findOrThrow(courseId);

        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw new RuntimeException("Vous êtes déjà inscrit à ce cours");
        }

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);
        log.info("User {} enrolled in course {}", userEmail, courseId);
    }

    public void markContentComplete(Long courseId, Long contentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserProgress progress = userProgressRepository
                .findByUserIdAndContentId(user.getId(), contentId)
                .orElse(UserProgress.builder()
                        .user(user)
                        .content(contentRepository.findById(contentId)
                                .orElseThrow(() -> new RuntimeException("Contenu introuvable")))
                        .build());

        progress.setCompleted(true);
        progress.setLastAccessedAt(LocalDateTime.now());
        userProgressRepository.save(progress);

        checkCourseCompletion(courseId, user);
    }

    public void updateProgress(Long contentId, int progressSeconds, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenu introuvable"));

        UserProgress progress = userProgressRepository
                .findByUserIdAndContentId(user.getId(), contentId)
                .orElse(UserProgress.builder().user(user).content(content).build());

        progress.setProgressSeconds(progressSeconds);
        progress.setLastAccessedAt(LocalDateTime.now());

        if (content.getDurationSeconds() != null && content.getDurationSeconds() > 0) {
            double percent = (double) progressSeconds / content.getDurationSeconds();
            if (percent >= 0.9) progress.setCompleted(true);
        }

        userProgressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getUserEnrollments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return enrollmentRepository.findByUserId(user.getId())
                .stream()
                .map(e -> toDTO(e.getCourse(), userEmail))
                .toList();
    }

    private void checkCourseCompletion(Long courseId, User user) {
        Course course = findOrThrow(courseId);
        List<Content> contents = course.getContents();
        if (contents.isEmpty()) return;

        long completedCount = contents.stream()
                .filter(c -> userProgressRepository
                        .findByUserIdAndContentId(user.getId(), c.getId())
                        .map(UserProgress::isCompleted)
                        .orElse(false))
                .count();

        if (completedCount == contents.size()) {
            enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                    .ifPresent(e -> {
                        e.setCompleted(true);
                        e.setCompletedAt(LocalDateTime.now());
                        enrollmentRepository.save(e);
                        log.info("User {} completed course {}", user.getEmail(), courseId);
                    });
        }
    }

    private Course findOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours introuvable: " + id));
    }

    private CourseDTO toDTO(Course c, String userEmail) {
        CourseDTO dto = new CourseDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setThumbnailUrl(c.getThumbnailUrl());
        dto.setLevel(c.getLevel().name());
        dto.setAccessLevel(c.getAccessLevel().name());
        dto.setPublished(c.isPublished());
        dto.setTotalContents(c.getContents().size());
        dto.setAuthorEmail(c.getAuthor() != null ? c.getAuthor().getEmail() : null);
        dto.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        dto.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);

        if (userEmail != null) {
            userRepository.findByEmail(userEmail).ifPresent(user -> {
                enrollmentRepository.findByUserIdAndCourseId(user.getId(), c.getId())
                        .ifPresent(e -> {
                            dto.setEnrolled(true);
                            dto.setCompleted(e.isCompleted());

                            if (!c.getContents().isEmpty()) {
                                long done = c.getContents().stream()
                                        .filter(content -> userProgressRepository
                                                .findByUserIdAndContentId(user.getId(), content.getId())
                                                .map(UserProgress::isCompleted)
                                                .orElse(false))
                                        .count();
                                dto.setProgressPercent((int) (done * 100 / c.getContents().size()));
                            }
                        });
            });
        }

        return dto;
    }
}
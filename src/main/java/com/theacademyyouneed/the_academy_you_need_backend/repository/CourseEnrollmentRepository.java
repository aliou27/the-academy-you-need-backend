package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // All courses a user is enrolled in
    List<CourseEnrollment> findByUserId(Long userId);

    // Last accessed course for "continue where you left off"
    @Query("SELECT e FROM CourseEnrollment e WHERE e.user.id = :userId " +
            "AND e.completed = false ORDER BY e.enrolledAt DESC")
    List<CourseEnrollment> findActiveEnrollmentsByUserId(Long userId);
}
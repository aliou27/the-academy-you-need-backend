package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.Course;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Course.Level;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Course.AccessLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByPublishedTrue(Pageable pageable);

    Page<Course> findByPublishedTrueAndLevel(Level level, Pageable pageable);

    Page<Course> findByPublishedTrueAndAccessLevel(AccessLevel accessLevel, Pageable pageable);

    Page<Course> findByPublishedTrueAndLevelAndAccessLevel(
            Level level, AccessLevel accessLevel, Pageable pageable);
}
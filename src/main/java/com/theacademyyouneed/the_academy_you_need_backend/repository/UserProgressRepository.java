package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndContentId(Long userId, Long contentId);

    List<UserProgress> findByUserId(Long userId);

    List<UserProgress> findByUserIdAndCompletedTrue(Long userId);
}
// ── ContentRepository.java ──────────────────────────────────────────────────
package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.Content;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Content.AccessLevel;
import com.theacademyyouneed.the_academy_you_need_backend.entity.Content.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // All published content (public listing)
    Page<Content> findByPublishedTrue(Pageable pageable);

    // Filter by type (VIDEO, AUDIO, etc.)
    Page<Content> findByPublishedTrueAndType(ContentType type, Pageable pageable);

    // Filter by access level
    Page<Content> findByPublishedTrueAndAccessLevel(AccessLevel accessLevel, Pageable pageable);

    // Filter by type AND access level
    Page<Content> findByPublishedTrueAndTypeAndAccessLevel(
            ContentType type, AccessLevel accessLevel, Pageable pageable);
}

// ── CategoryRepository.java ─────────────────────────────────────────────────
// Create this as a SEPARATE FILE in the repository package


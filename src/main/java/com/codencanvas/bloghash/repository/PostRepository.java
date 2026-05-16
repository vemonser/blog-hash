package com.codencanvas.bloghash.repository;


import com.codencanvas.bloghash.domain.post.Post;
import com.codencanvas.bloghash.domain.post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
 
    // ── Single Post ──────────────────────────────────────────────
    Optional<Post> findBySlug(String slug);
 
    boolean existsBySlug(String slug);
 
    // ── Feed (Published Posts) ───────────────────────────────────
    // @SQLRestriction بيضيف is_deleted = false تلقائياً
    Page<Post> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);
 
    // ── By Author ────────────────────────────────────────────────
    Page<Post> findByAuthorUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
 
    // Published posts فقط للبروفايل العام
    Page<Post> findByAuthorUsernameAndStatusOrderByPublishedAtDesc(
        String username, PostStatus status, Pageable pageable
    );
 
    // ── By Tag ───────────────────────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN p.tags t
        WHERE t.slug = :tagSlug
          AND p.status = 'PUBLISHED'
        ORDER BY p.publishedAt DESC
        """)
    Page<Post> findPublishedByTagSlug(
        @Param("tagSlug") String tagSlug,
        Pageable pageable
    );
 
    // ── By Category ───────────────────────────────────────────────
    Page<Post> findByCategorySlugAndStatusOrderByPublishedAtDesc(
        String categorySlug, PostStatus status, Pageable pageable
    );
 
    // ── Counts (للـ enrichment) ───────────────────────────────────
    long countByAuthorId(UUID authorId);
}
 

package com.codencanvas.bloghash.repository;

import com.codencanvas.bloghash.domain.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Top-level comments فقط (parentId = null)
    // @SQLRestriction بيضيف is_deleted = false تلقائياً
    Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findDirectChildren(@Param("parentId") UUID parentId, Pageable pageable);

    // كل children تحت comment معين بـ Materialized Path
    @Query("""
            SELECT c FROM Comment c
            WHERE c.path LIKE :pathPrefix
              AND c.id != :rootId
            ORDER BY c.path ASC
            """)
    List<Comment> findAllDescendants(
            @Param("pathPrefix") String pathPrefix, // "uuid."
            @Param("rootId") UUID rootId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :parentId AND c.deleted = false")
    long countByParentId(@Param("parentId") UUID parentId);

    // Count non-deleted comments لـ post
    long countByPostIdAndDeletedFalse(UUID postId);

    long countByAuthorId(UUID authorId);
}

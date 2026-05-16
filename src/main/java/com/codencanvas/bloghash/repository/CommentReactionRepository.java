package com.codencanvas.bloghash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codencanvas.bloghash.domain.comment.CommentReaction;
import com.codencanvas.bloghash.domain.comment.CommentReactionId;

import java.util.UUID;
 
public interface CommentReactionRepository extends JpaRepository<CommentReaction, CommentReactionId> {
 
    boolean existsByIdUserIdAndIdCommentId(UUID userId, UUID commentId);
 
    long countByIdCommentId(UUID commentId);
 
    @Modifying
    @Query("DELETE FROM CommentReaction cr WHERE cr.id.userId = :userId AND cr.id.commentId = :commentId")
    void deleteByUserIdAndCommentId(@Param("userId") UUID userId, @Param("commentId") UUID commentId);
}
 
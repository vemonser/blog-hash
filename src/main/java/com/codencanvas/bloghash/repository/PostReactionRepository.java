package com.codencanvas.bloghash.repository;

import com.codencanvas.bloghash.domain.post.PostReaction;
import com.codencanvas.bloghash.domain.post.PostReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
 

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, PostReactionId> {
 
    boolean existsByIdUserIdAndIdPostId(UUID userId, UUID postId);
 
    long countByIdPostId(UUID postId);
 
    @Modifying
    @Query("DELETE FROM PostReaction pr WHERE pr.id.userId = :userId AND pr.id.postId = :postId")
    void deleteByUserIdAndPostId(@Param("userId") UUID userId, @Param("postId") UUID postId);
}
 
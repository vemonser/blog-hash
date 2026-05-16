package com.codencanvas.bloghash.dto.response.comment;

import java.time.Instant;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

public record CommentResponse(
    UUID                id,
    String              content,
    UserSummaryResponse author,
    UUID                parentId,   // null = top-level
    String              path,       // للـ tree building في الـ frontend
    int                 depth,
    int                 likesCount,
    int                 repliesCount,
    boolean             deleted,    // true لو content = "[deleted]"
    boolean             likedByCurrentUser,
    Instant             createdAt,
    Instant             updatedAt
) {}
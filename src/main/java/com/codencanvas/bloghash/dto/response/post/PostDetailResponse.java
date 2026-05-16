package com.codencanvas.bloghash.dto.response.post;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

public record PostDetailResponse(
    UUID                id,
    String              slug,
    String              title,
    String              contentMarkdown,
    String              toc,           // JSON string للـ Table of Contents
    String              coverImageUrl,
    String              status,
    UserSummaryResponse author,
    CategoryResponse    category,
    Set<TagResponse>    tags,
    Integer             readingTimeMins,
    Integer             wordCount,
    int                 likesCount,
    int                 commentsCount,
    boolean             likedByCurrentUser,  // computed للـ authenticated user
    Instant             publishedAt,
    Instant             createdAt,
    Instant             updatedAt
) {}
 
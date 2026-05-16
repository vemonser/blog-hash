package com.codencanvas.bloghash.dto.response.post;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;


public record PostSummaryResponse(
    UUID                id,
    String              slug,
    String              title,
    String              coverImageUrl,
    String              status,
    UserSummaryResponse author,
    CategoryResponse    category,
    Set<TagResponse>    tags,
    Integer             readingTimeMins,
    Integer             wordCount,
    int                 likesCount,     
    int                 commentsCount,  
    Instant             publishedAt,
    Instant             createdAt
) {
    
}
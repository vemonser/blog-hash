package com.codencanvas.bloghash.dto.response.post;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostDetailResponse {
    private final UUID id;
    private final String slug;
    private final String title;
    private final String contentMarkdown;
    private final String toc;
    private final String coverImageUrl;
    private final String status;
    private final UserSummaryResponse author;
    private final CategoryResponse category;
    private final Set<TagResponse> tags;
    private final Integer readingTimeMins;
    private final Integer wordCount;
    private final int likesCount;
    private final int commentsCount;
    private final boolean likedByCurrentUser;
    private final Instant publishedAt;
    private final Instant createdAt;
    private final Instant updatedAt;


    public PostDetailResponse withCounts(int newLikesCount, int newCommentsCount, boolean newLikedByCurrentUser) {
        return PostDetailResponse.builder()
                .id(this.id)
                .slug(this.slug)
                .title(this.title)
                .contentMarkdown(this.contentMarkdown)
                .toc(this.toc)
                .coverImageUrl(this.coverImageUrl)
                .status(this.status)
                .author(this.author)
                .category(this.category)
                .tags(this.tags)
                .readingTimeMins(this.readingTimeMins)
                .wordCount(this.wordCount)
                .likesCount(newLikesCount)
                .commentsCount(newCommentsCount)
                .likedByCurrentUser(newLikedByCurrentUser)
                .publishedAt(this.publishedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

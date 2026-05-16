package com.codencanvas.bloghash.domain.post;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.codencanvas.bloghash.domain.common.SoftDeletableEntity;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_slug", columnList = "slug"),
        @Index(name = "idx_posts_author_id", columnList = "author_id"),
        @Index(name = "idx_posts_status", columnList = "status"),
        @Index(name = "idx_posts_category_id", columnList = "category_id"),
        @Index(name = "idx_posts_published_at", columnList = "published_at")
})

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends SoftDeletableEntity {
    // ════════════════════════════════════════════════
    // OWNERSHIP
    // ════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ════════════════════════════════════════════════
    // CONTENT
    // ════════════════════════════════════════════════

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 400, updatable = false)
    private String slug;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "TEXT")
    private String contentMarkdown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "toc", columnDefinition = "jsonb")
    private String toc;

    // ════════════════════════════════════════════════
    // MEDIA
    // ════════════════════════════════════════════════

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "cover_image_public_id", length = 255)
    private String coverImagePublicId;

    // ════════════════════════════════════════════════
    // STATUS & METADATA
    // ════════════════════════════════════════════════

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "reading_time_mins")
    private Integer readingTimeMins;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // ════════════════════════════════════════════════
    // RELATIONSHIPS
    // ════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_post_tags_post")), inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "fk_post_tags_tag")))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ════════════════════════════════════════════════
    // BUSINESS METHODS
    // ════════════════════════════════════════════════

    public void publish() {
        this.status = PostStatus.PUBLISHED;
        if (this.publishedAt == null) { // Set once only
            this.publishedAt = Instant.now();
        }
    }

    public void archive() {
        this.status = PostStatus.ARCHIVED;
    }

    public void backToDraft() {
        this.status = PostStatus.DRAFT;
    }

    public void updateContent(String title,
            String contentMarkdown,
            String toc,
            int wordCount,
            int readingTimeMins) {
        this.title = title;
        this.contentMarkdown = contentMarkdown;
        this.toc = toc;
        this.wordCount = wordCount;
        this.readingTimeMins = readingTimeMins;
    }

    public void updateCoverImage(String url, String publicId) {
        this.coverImageUrl = url;
        this.coverImagePublicId = publicId;
    }

    public void removeCoverImage() {
        this.coverImageUrl = null;
        this.coverImagePublicId = null;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public void replaceTags(Set<Tag> newTags) {
        this.tags = newTags;
    }

    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public boolean isDraft() {
        return this.status == PostStatus.DRAFT;
    }

    public boolean isArchived() {
        return this.status == PostStatus.ARCHIVED;
    }
}

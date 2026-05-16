package com.codencanvas.bloghash.domain.comment;

import java.util.UUID;

import com.codencanvas.bloghash.domain.common.SoftDeletableEntity;
import com.codencanvas.bloghash.domain.post.Post;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post_id", columnList = "post_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_parent_id", columnList = "parent_id"),
        // Index على path عشان LIKE 'prefix%' queries تشتغل بسرعة
        @Index(name = "idx_comments_path", columnList = "path")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends SoftDeletableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "path", nullable = false, length = 2000)
    private String path;

    @Column(name = "depth", nullable = false)
    @Builder.Default
    private int depth = 0;

    @Version
    @Column(name = "version")
    private Long version;

    // ════════════════════════════════════════════════
    // BUSINESS METHODS
    // ════════════════════════════════════════════════
    @Override
    public void softDelete() {
        super.softDelete();
        this.content = "[deleted]";
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void assignPath(String path) {
        this.path = path;
    }

    public String buildChildPath(java.util.UUID childId) {
        return this.path + "." + childId.toString();
    }

    public boolean isTopLevel() {
        return this.parent == null;
    }

    public boolean isReply() {
        return this.parent != null;
    }

    @PrePersist
    void buildPath() {
        UUID id = this.getId();
        if (this.parent == null) {
            this.path = id.toString();
            this.depth = 0;
        } else {
            this.path = this.parent.getPath() + "." + id.toString();
            this.depth = this.parent.getDepth() + 1;
        }
    }

}

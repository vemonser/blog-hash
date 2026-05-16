-- ─────────────────────────────────────────────────────────────────
-- COMMENTS — Materialized Path Pattern
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE comments
(
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
 
    post_id    UUID        NOT NULL,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
 
    author_id  UUID        NOT NULL,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id),

    parent_id  UUID,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE SET NULL,
 
    content    TEXT        NOT NULL,
 
-- ═══════════════════════════════════════════════════
-- MATERIALIZED PATH — قلب الـ tree structure
-- ═══════════════════════════════════════════════════

    path       VARCHAR(2000) NOT NULL,
 
    depth      INTEGER       NOT NULL DEFAULT 0,
 
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,

    version    BIGINT        NOT NULL DEFAULT 0,
 
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_comments_not_self_parent CHECK (id != parent_id),
    CONSTRAINT chk_comments_depth          CHECK (depth >= 0)
);
 

CREATE INDEX idx_comments_post_id   ON comments (post_id);
CREATE INDEX idx_comments_author_id ON comments (author_id);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);
CREATE INDEX idx_comments_path
    ON comments (path text_pattern_ops);
 
CREATE INDEX idx_comments_active
    ON comments (post_id, created_at)
    WHERE is_deleted = FALSE;
 
-- ─────────────────────────────────────────────────────────────────
-- COMMENT_REACTIONS
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE comment_reactions
(
    user_id    UUID        NOT NULL,
    comment_id UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 
    CONSTRAINT pk_comment_reactions        PRIMARY KEY (user_id, comment_id),
    CONSTRAINT fk_comment_reactions_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE CASCADE,
    CONSTRAINT fk_comment_reactions_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE
);
 
CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions (comment_id);
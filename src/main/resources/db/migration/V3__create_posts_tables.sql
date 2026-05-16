-- ─────────────────────────────────────────────────────────────────
-- CATEGORIES (hierarchical)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE categories
(
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
 
    parent_id   UUID,
    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL,
 
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
 
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT uk_categories_name UNIQUE (name)
);

-- ─────────────────────────────────────────────────────────────────
-- TAGS (flat)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE tags
(
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(50) NOT NULL,
 
    slug       VARCHAR(60) NOT NULL,
 
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 
    CONSTRAINT uk_tags_slug UNIQUE (slug),
    CONSTRAINT uk_tags_name UNIQUE (name)
);


-- ─────────────────────────────────────────────────────────────────
-- POSTS
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE posts
(
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
 
    author_id             UUID         NOT NULL,
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id) REFERENCES users (id),
 
    title                 VARCHAR(300) NOT NULL,
 
    slug                  VARCHAR(400) NOT NULL,
 
    content_markdown      TEXT         NOT NULL,
 
    toc                   JSONB,
 
    cover_image_url       VARCHAR(500),
    cover_image_public_id VARCHAR(255),
 
    status                VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
 
    word_count            INTEGER,
    reading_time_mins     INTEGER,
 
    published_at          TIMESTAMPTZ,
 
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at            TIMESTAMPTZ,
 
    version               BIGINT       NOT NULL DEFAULT 0,
 
    category_id           UUID,
    CONSTRAINT fk_posts_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
 
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
 
    CONSTRAINT uk_posts_slug   UNIQUE (slug),
    CONSTRAINT chk_posts_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_posts_slug         ON posts (slug);
CREATE INDEX idx_posts_author_id    ON posts (author_id);
CREATE INDEX idx_posts_category_id  ON posts (category_id);
CREATE INDEX idx_posts_published_at ON posts (published_at);

CREATE INDEX idx_posts_published_active
    ON posts (status, published_at DESC)
    WHERE is_deleted = FALSE;

-- ─────────────────────────────────────────────────────────────────
-- POST_TAGS (Junction Table)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE post_tags
(
    post_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
 
    CONSTRAINT pk_post_tags    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag  FOREIGN KEY (tag_id)  REFERENCES tags (id)  ON DELETE CASCADE
);
 
CREATE INDEX idx_post_tags_tag_id ON post_tags (tag_id);

-- ─────────────────────────────────────────────────────────────────
-- POST_REACTIONS (Likes)
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE post_reactions
(
    user_id    UUID        NOT NULL,
    post_id    UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 
    CONSTRAINT pk_post_reactions     PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_post_reactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_reactions_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);
 
CREATE INDEX idx_post_reactions_post_id ON post_reactions (post_id);

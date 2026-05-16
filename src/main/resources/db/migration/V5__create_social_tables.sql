-- ─────────────────────────────────────────────────────────────────
-- NOTIFICATIONS
-- ─────────────────────────────────────────────────────────────────
CREATE TABLE notifications
(
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),

    recipient_id UUID       NOT NULL,
    CONSTRAINT fk_notif_recipient
        FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE,

    actor_id    UUID,
    CONSTRAINT fk_notif_actor
        FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL,

    type        VARCHAR(50) NOT NULL,

    entity_type VARCHAR(20) NOT NULL,

    entity_id   UUID        NOT NULL,

    group_key   VARCHAR(255),

    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMPTZ,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notif_type
        CHECK (type IN (
            'POST_LIKED',
            'POST_COMMENTED',
            'COMMENT_REPLIED',
            'COMMENT_LIKED',
            'NEW_FOLLOWER',
            'NEW_POST_FROM_FOLLOWED'
        )),
    CONSTRAINT chk_notif_entity_type
        CHECK (entity_type IN ('POST', 'COMMENT', 'USER'))
);


CREATE INDEX idx_notif_recipient_read_created
    ON notifications (recipient_id, is_read, created_at DESC);

CREATE INDEX idx_notif_group_key ON notifications (group_key)
    WHERE group_key IS NOT NULL;  

-- ─────────────────────────────────────────────────────────────────
-- NOTIFICATION PREFERENCES
-- ─────────────────────────────────────────────────────────────────

CREATE TABLE notification_preferences
(
    user_id  UUID        NOT NULL,
    type     VARCHAR(50) NOT NULL,

    in_app   BOOLEAN     NOT NULL DEFAULT TRUE,
    email    BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_notif_prefs PRIMARY KEY (user_id, type),
    CONSTRAINT fk_notif_prefs_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_notif_prefs_type
        CHECK (type IN (
            'POST_LIKED',
            'POST_COMMENTED',
            'COMMENT_REPLIED',
            'COMMENT_LIKED',
            'NEW_FOLLOWER',
            'NEW_POST_FROM_FOLLOWED'
        ))
);

-- ─────────────────────────────────────────────────────────────────
-- FOLLOWERS
-- ─────────────────────────────────────────────────────────────────

CREATE TABLE followers
(
    follower_id  UUID        NOT NULL,

    following_id UUID        NOT NULL,

    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_followers PRIMARY KEY (follower_id, following_id),
    CONSTRAINT fk_followers_follower
        FOREIGN KEY (follower_id)  REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_followers_following
        FOREIGN KEY (following_id) REFERENCES users (id) ON DELETE CASCADE,


    CONSTRAINT chk_followers_no_self_follow CHECK (follower_id != following_id)
);

CREATE INDEX idx_followers_following_id ON followers (following_id);

CREATE INDEX idx_followers_follower_id ON followers (follower_id);
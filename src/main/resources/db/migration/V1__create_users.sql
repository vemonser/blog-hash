CREATE TABLE users
    (
        id                    UUID PRIMARY KEY DEFAULT gen_random_uuid()     ,
        username              VARCHAR(50) NOT NULL                           ,
        email                 VARCHAR(255) NOT NULL                          ,
        password_hash         VARCHAR(255)                                   ,
        role                  VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'       ,
        is_enabled            BOOLEAN NOT NULL DEFAULT FALSE                 , -- email verification
        is_locked             BOOLEAN NOT NULL DEFAULT FALSE                 , -- brute force protection
        failed_login_attempts INT NOT NULL DEFAULT 0                         ,
        lock_time             TIMESTAMPTZ                                    ,
        avatar_url            VARCHAR(500)                                   ,
        avatar_public_id      VARCHAR(255)                                   ,
        bio                   VARCHAR(500)                                   ,
        created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()             ,
        updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()             ,
        CONSTRAINT uk_users_email UNIQUE (email)                             ,
        CONSTRAINT uk_users_username UNIQUE (username)                       ,
        CONSTRAINT chk_users_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
        CONSTRAINT chk_users_username_length CHECK (LENGTH(username) >= 3)   ,
        CREATE INDEX idx_users_email ON users (email); CREATE INDEX idx_users_username ON users (username);

)
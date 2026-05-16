
-- =========================================
-- Refresh Tokens
-- =========================================
CREATE TABLE refresh_tokens
    (
        id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id    UUID NOT NULL                             ,
        token_hash VARCHAR(255) NOT NULL                     ,
        family_id  UUID NOT NULL                             ,
        expires_at TIMESTAMPTZ NOT NULL                      ,
        revoked_at TIMESTAMPTZ                               ,
        version    BIGINT NOT NULL DEFAULT 0                 ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()        ,
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()        ,
        CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON
        DELETE
            CASCADE,
            CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash) );
CREATE INDEX idx_refresh_active
ON refresh_tokens
    (
        user_id
    )
WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_family_id
ON refresh_tokens
    (
        family_id
    )
;
-- =========================================
-- Email Verification Tokens
-- =========================================
CREATE TABLE email_verification_tokens
    (
        id         UUID PRIMARY KEY DEFAULT gen_random_uuid()   ,
        user_id    UUID NOT NULL                                ,
        token_hash VARCHAR(255) NOT NULL                        ,
        expires_at TIMESTAMPTZ NOT NULL                         ,
        used_at    TIMESTAMPTZ                                  ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()           ,
        CONSTRAINT uk_email_verif_token_hash UNIQUE (token_hash),
        CONSTRAINT fk_email_verif_user FOREIGN KEY (user_id) REFERENCES users(id) ON
        DELETE
            CASCADE );
CREATE INDEX idx_email_verif_user_id
ON email_verification_tokens
    (
        user_id
    )
;
-- =========================================
-- OAuth2 Accounts
-- =========================================
CREATE TABLE oauth2_accounts
    (
        id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id                     UUID NOT NULL                             ,
        provider oauth_provider NOT NULL                                      ,
        provider_user_id            VARCHAR(255) NOT NULL                     ,
        provider_email              VARCHAR(255)                              ,
        created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()        ,
        CONSTRAINT fk_oauth2_user FOREIGN KEY (user_id) REFERENCES users(id) ON
        DELETE
            CASCADE                                                                  ,
            CONSTRAINT uk_oauth2_provider_user_id UNIQUE (provider, provider_user_id),
            CONSTRAINT chk_oauth2_provider        CHECK (provider IN ('GOOGLE', 'GITHUB'))
    )
CREATE INDEX idx_oauth2_user_id
ON oauth2_accounts
    (
        user_id
    );
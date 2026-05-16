package com.codencanvas.bloghash.domain.auth;

import java.time.Instant;
import java.util.UUID;

import com.codencanvas.bloghash.domain.common.BaseEntity;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash"),
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_family", columnList = "family_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "family_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // ════════════════════════════════════════════════
    //              BUSINESS METHODS
    // ════════════════════════════════════════════════
 
    public void revoke() {
        this.revokedAt = Instant.now();
    }
 
    public boolean isRevoked() {
        return this.revokedAt != null;
    }
 
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }

}

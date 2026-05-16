package com.codencanvas.bloghash.domain.auth;

import java.time.Instant;

import com.codencanvas.bloghash.domain.common.BaseEntity;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_email_verif_token_hash", columnList = "token_hash"),
        @Index(name = "idx_email_verif_user_id", columnList = "user_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailVerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "used_at")
    private Instant usedAt;

    // Business methods
    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !isUsed() && !isExpired();
    }
}

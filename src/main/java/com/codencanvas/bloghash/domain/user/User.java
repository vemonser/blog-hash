package com.codencanvas.bloghash.domain.user;

import java.time.Instant;

import com.codencanvas.bloghash.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
}, indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

        // ════════════════════════════════════════════════
        // IDENTITY
        // ════════════════════════════════════════════════

        @Column(name = "username", nullable = false, unique = true, length = 50)
        private String username;

        @Column(name = "email", nullable = false, unique = true, length = 255)
        private String email;

        @Column(name = "password_hash", length = 255)
        private String passwordHash;

        // ════════════════════════════════════════════════========
        // ROLES & PERMISSIONS
        // ════════════════════════════════════════════════

        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, length = 20)
        @Builder.Default
        private UserRole role = UserRole.ROLE_USER;

        // ════════════════════════════════════════════════
        // ACCOUNT STATUS FLAGS
        // ════════════════════════════════════════════════
        @Column(name = "is_enabled", nullable = false)
        @Builder.Default
        private boolean enabled = false;

        @Column(name = "is_locked", nullable = false)
        @Builder.Default
        private boolean accountLocked = false;

        @Column(name = "failed_login_attempts", nullable = false)
        @Builder.Default
        private int failedLoginAttempts = 0;

        @Column(name = "lock_time")
        private Instant lockTime;

        // ════════════════════════════════════════════════
        // PROFILE
        // ════════════════════════════════════════════════

        @Column(name = "avatar_url", length = 500)
        private String avatarUrl;

        @Column(name = "avatar_public_id", length = 255)
        private String avatarPublicId;

        @Column(name = "bio", length = 500)
        private String bio;

        // ════════════════════════════════════════════════
        // BUSINESS METHODS
        // ════════════════════════════════════════════════
        public void activate() {
                this.enabled = true;
        }

        public void lock() {
                this.accountLocked = true;
                this.lockTime = Instant.now();
        }

        public void unlock() {
                this.accountLocked = false;
                this.lockTime = null;
                this.failedLoginAttempts = 0;
        }

        public void incrementFailedAttempts() {
                this.failedLoginAttempts++;
        }

        public void resetFailedAttempts() {
                this.failedLoginAttempts = 0;
        }

        public void updateAvatar(String avatarUrl, String avatarPublicId) {
                this.avatarUrl = avatarUrl;
                this.avatarPublicId = avatarPublicId;
        }

        public void updateBio(String bio) {
                this.bio = bio;
        }

        public void changePassword(String newPasswordHash) {
                this.passwordHash = newPasswordHash;
        }

        // ════════════════════════════════════════════════
        // QUERY HELPERS
        // ════════════════════════════════════════════════
        public boolean hasLocalPassword() {
                return this.passwordHash != null;
        }
}

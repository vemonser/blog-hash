package com.codencanvas.bloghash.domain.auth;

import com.codencanvas.bloghash.domain.common.BaseEntity;
import com.codencanvas.bloghash.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth2_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_oauth2_provider_user_id", columnNames = { "provider", "provider_user_id" })
}, indexes = {
        @Index(name = "idx_oauth2_user_id", columnList = "user_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2Account extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private OAuth2Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

        @Column(name = "provider_email", length = 255)
    private String providerEmail;

}

package com.codencanvas.bloghash.security.service;

import com.codencanvas.bloghash.config.properties.JwtProperties;
import com.codencanvas.bloghash.domain.auth.RefreshToken;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.exception.InvalidTokenException;
import com.codencanvas.bloghash.exception.TokenExpiredException;
import com.codencanvas.bloghash.exception.TokenReuseDetectedException;
import com.codencanvas.bloghash.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String create(User user) {
        return createWithFamily(user, UUID.randomUUID());  
    }

    @Transactional
    public String createWithFamily(User user, UUID familyId) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .familyId(familyId)
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(token);
        log.debug("Refresh token created for user: {}, family: {}", user.getId(), familyId);

        return rawToken;
    }

    @Transactional
    public RotationResult rotateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        // ── Reuse Detection ──────────────────────────────────────
        if (stored.isRevoked()) {
            log.warn(
                    "SECURITY: Refresh token reuse detected! User: {}, Family: {}",
                    stored.getUser().getId(),
                    stored.getFamilyId());
            // Revoke entire family → force re-login
            revokeFamily(stored.getFamilyId());
            throw new TokenReuseDetectedException();
        }
        // ── Expiry Check ─────────────────────────────────────────
        if (stored.isExpired()) {
            throw new TokenExpiredException();
        }
        // ── Rotation ─────────────────────────────────────────────
        stored.revoke();  
        refreshTokenRepository.save(stored);

        String newRawToken = createWithFamily(stored.getUser(), stored.getFamilyId());

        return new RotationResult(stored.getUser(), newRawToken);
    }

    @Transactional
    public void revokeFamily(UUID familyId) {
        refreshTokenRepository.revokeAllByFamilyId(familyId);
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.debug("All refresh tokens revoked for user: {}", userId);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    public record RotationResult(User user, String newRawToken) {
    }
}

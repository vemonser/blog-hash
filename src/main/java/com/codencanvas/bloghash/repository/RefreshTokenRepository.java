package com.codencanvas.bloghash.repository;

import com.codencanvas.bloghash.domain.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
 
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revokedAt = :now
        WHERE rt.familyId = :familyId AND rt.revokedAt IS NULL
        """)
    void revokeAllByFamilyId(@Param("familyId") UUID familyId, @Param("now") Instant now);

    default void revokeAllByFamilyId(UUID familyId) {
        revokeAllByFamilyId(familyId, Instant.now());
    }

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revokedAt = :now
        WHERE rt.user.id = :userId AND rt.revokedAt IS NULL
        """)
    void revokeAllByUserId(@Param("userId") UUID userId,@Param("now") Instant now);

    default void revokeAllByUserId(UUID userId) {
        revokeAllByUserId(userId, Instant.now());
    }
}


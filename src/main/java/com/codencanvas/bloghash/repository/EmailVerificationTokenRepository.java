package com.codencanvas.bloghash.repository;

import com.codencanvas.bloghash.domain.auth.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
 
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
 
    @Modifying
    @Query("DELETE FROM EmailVerificationToken evt WHERE evt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
 
package com.codencanvas.bloghash.repository;
 
import com.codencanvas.bloghash.domain.auth.OAuth2Account;
import com.codencanvas.bloghash.domain.auth.OAuth2Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
 

@Repository
public interface OAuth2AccountRepository extends JpaRepository<OAuth2Account, UUID> {
 
    Optional<OAuth2Account> findByProviderAndProviderUserId(
        OAuth2Provider provider,
        String providerUserId
    );
}
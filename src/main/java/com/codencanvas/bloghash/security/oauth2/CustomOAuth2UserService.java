package com.codencanvas.bloghash.security.oauth2;

import com.codencanvas.bloghash.domain.auth.OAuth2Account;
import com.codencanvas.bloghash.domain.auth.OAuth2Provider;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.repository.OAuth2AccountRepository;
import com.codencanvas.bloghash.repository.UserRepository;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2AccountRepository oAuth2AccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId()
                .toUpperCase(); 
        OAuth2Provider provider = OAuth2Provider.valueOf(registrationId);

        String providerId = extractProviderId(oAuth2User, provider);
        String email = extractEmail(oAuth2User, provider, providerId);
        String displayName = oAuth2User.getAttribute("name") != null
                ? oAuth2User.getAttribute("name")
                : extractUsername(oAuth2User, provider);
        String avatarUrl = extractAvatarUrl(oAuth2User, provider);

        User user = processOAuth2User(provider, providerId, email, displayName, avatarUrl);

        log.info("OAuth2 login: provider={}, email={}, userId={}", provider, email, user.getId());

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User processOAuth2User(
            OAuth2Provider provider,
            String providerId,
            String email,
            String displayName,
            String avatarUrl) {

        // Scenario 2: Returning OAuth2 user
        return oAuth2AccountRepository
                .findByProviderAndProviderUserId(provider, providerId)
                .map(OAuth2Account::getUser)
                .orElseGet(() -> {

                    // Scenario 1 & 3: New OAuth2 user or account linking
                    User user = userRepository.findByEmail(email)
                            .orElseGet(() -> createNewOAuth2User(email, displayName, avatarUrl));

                    // Create the OAuth2Account link
                    OAuth2Account account = OAuth2Account.builder()
                            .user(user)
                            .provider(provider)
                            .providerUserId(providerId)
                            .providerEmail(email)
                            .build();
                    oAuth2AccountRepository.save(account);

                    return user;
                });
    }

    private User createNewOAuth2User(
            String email,
            String displayName,
            String avatarUrl) {
        String username = generateUniqueUsername(displayName);
        User user = User.builder()
                .email(email)
                .username(username)
                .avatarUrl(avatarUrl)
                .build();
        user.activate();
        return userRepository.save(user);
    }

    // ── Extraction Helpers ───────────────────────────────────────

    private String extractProviderId(OAuth2User user, OAuth2Provider provider) {
        return switch (provider) {
            case GOOGLE -> user.getAttribute("sub");
            case GITHUB -> String.valueOf(user.getAttribute("id"));
        };
    }

    private String extractEmail(OAuth2User user, OAuth2Provider provider, String providerId) {
        String email = user.getAttribute("email");
        if (email != null)
            return email;

        return switch (provider) {
            case GITHUB -> "github_" + providerId + "@placeholder.bloghash.com";
            case GOOGLE -> throw new OAuth2AuthenticationException(
                    "Email not provided by Google. Please check your account settings");
        };
    }

    private String extractAvatarUrl(OAuth2User user, OAuth2Provider provider) {
        return switch (provider) {
            case GOOGLE -> user.getAttribute("picture");
            case GITHUB -> user.getAttribute("avatar_url");
        };
    }

    private String extractUsername(OAuth2User user, OAuth2Provider provider) {
        return switch (provider) {
            case GITHUB -> user.getAttribute("login"); 
            case GOOGLE -> "user";
        };
    }

    private String generateUniqueUsername(String displayName) {
        String base = displayName
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "") 
                .substring(0, Math.min(displayName.length(), 20));

        if (base.isEmpty())
            base = "user";

        String candidate = base;
        int attempts = 0;

        while (userRepository.existsByUsername(candidate) && attempts < 10) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            candidate = base + "_" + suffix;
            attempts++;
        }

        return candidate;
    }
}

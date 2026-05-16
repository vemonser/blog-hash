package com.codencanvas.bloghash.security.principal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.codencanvas.bloghash.domain.user.User;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final User user;

    @Setter
    private Map<String, Object> attributes;

    private UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(user, new HashMap<>());
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return new UserPrincipal(user, attributes);
    }

    public UUID getId() {
        return user.getId();
    }

    public String getRole() {
        return user.getRole().name();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // Argon2id hashed password
    }

    @Override
    public String getUsername() {
        // Spring Security بيستخدم getUsername() كـ "identifier"
        // بنرجع الـ email لأنه unique ومش بيتغير
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    // ════════════════════════════════════════════════════════════
    // OAuth2User Implementation
    // ════════════════════════════════════════════════════════════

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}

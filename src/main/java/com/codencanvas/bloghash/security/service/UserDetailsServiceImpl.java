package com.codencanvas.bloghash.security.service;

import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.exception.ResourceNotFoundException;
import com.codencanvas.bloghash.repository.UserRepository;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + usernameOrEmail));
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
        return UserPrincipal.create(user);
    }

}

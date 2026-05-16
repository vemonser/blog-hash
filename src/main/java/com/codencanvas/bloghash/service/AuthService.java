package com.codencanvas.bloghash.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

 import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codencanvas.bloghash.auth.service.EmailVerificationService;
import com.codencanvas.bloghash.config.properties.AppSecurityProperties;
import com.codencanvas.bloghash.config.properties.JwtProperties;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.dto.request.auth.LoginRequest;
import com.codencanvas.bloghash.dto.request.auth.RefreshTokenRequest;
import com.codencanvas.bloghash.dto.request.auth.RegisterRequest;
import com.codencanvas.bloghash.dto.response.auth.AuthResponse;
import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;
import com.codencanvas.bloghash.exception.AccountNotVerifiedException;
import com.codencanvas.bloghash.exception.AccountLockedException;
import com.codencanvas.bloghash.exception.EmailAlreadyExistsException;
import com.codencanvas.bloghash.exception.InvalidCredentialsException;
import com.codencanvas.bloghash.exception.ResourceNotFoundException;
import com.codencanvas.bloghash.exception.UsernameAlreadyTakenException;
import com.codencanvas.bloghash.mapper.UserMapper;
import com.codencanvas.bloghash.repository.UserRepository;
import com.codencanvas.bloghash.security.jwt.JwtService;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.security.service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper userMapper;
    private final AppSecurityProperties securityProperties;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyTakenException();
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        // ── Create User ──────────────────────────────────────────
        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(hashedPassword)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.email());

        // ── Send Verification Email ───────────────────────────────
        emailVerificationService.sendVerificationEmail(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verifyEmail(token);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User"));

        if (user.isEnabled()) {
            return;
        }

        emailVerificationService.resendVerificationEmail(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // ── Step 1: Find User ─────────────────────────────────────
        User user = userRepository
                .findByEmailOrUsername(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for identifier: {}", request.usernameOrEmail());
                    return new InvalidCredentialsException();
                });
        // ── Step 2: Email Verification Check ─────────────────────
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException();
        }

        // ── Step 3: Account Lockout Check ────────────────────────
        if (user.isAccountLocked()) {
            if (isLockExpired(user)) {
                user.unlock();
                userRepository.save(user);
                log.info("Account auto-unlocked for user: {}", user.getEmail());
            } else {
                long remainingMinutes = getRemainingLockMinutes(user);
                throw new AccountLockedException(remainingMinutes);
            }
        }

        // ── Step 4: Password Verification ────────────────────────
        boolean passwordMatches = user.hasLocalPassword()
                && passwordEncoder.matches(request.password(), user.getPasswordHash());

        if (!passwordMatches) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        }

        // ── Step 5: Success ───────────────────────────────────────
        user.resetFailedAttempts();
        userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.create(user);

        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshTokenService.RotationResult result = refreshTokenService.rotateToken(request.refreshToken());

        User user = result.user();
        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = result.newRawToken();

        log.debug("Token refreshed for user: {}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public void logout(String accessToken, String rawRefreshToken) {
 
        // ── Blacklist the Access Token ────────────────────────────
        long ttlMillis = jwtService.getRemainingTtlMillis(accessToken);
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "revoked",
                ttlMillis,
                TimeUnit.MILLISECONDS
            );
        }
 
        // ── Revoke Refresh Token ──────────────────────────────────
        // لو rawRefreshToken موجود في الـ request
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = refreshTokenService.hashToken(rawRefreshToken);
            refreshTokenService.revokeAllForUser(
                // نجيب user ID من الـ JWT (بدل DB query)
                jwtService.extractUserId(accessToken)
            );
        }
 
        log.info("User logged out, token blacklisted");
    }
 
    // ════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════
 
    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
 
        if (user.getFailedLoginAttempts() >= securityProperties.maxLoginAttempts()) {
            user.lock();
            log.warn("Account locked due to {} failed attempts: {}",
                securityProperties.maxLoginAttempts(), user.getEmail());
        }
 
        userRepository.save(user);
    }
 
    private boolean isLockExpired(User user) {
        if (user.getLockTime() == null) return true;
        Instant unlockTime = user.getLockTime()
            .plusSeconds(securityProperties.lockAccountDurationMinutes() * 60L);
        return Instant.now().isAfter(unlockTime);
    }
 
    private long getRemainingLockMinutes(User user) {
        if (user.getLockTime() == null) return 0;
        Instant unlockTime   = user.getLockTime()
            .plusSeconds(securityProperties.lockAccountDurationMinutes() * 60L);
        long    remainingSecs = unlockTime.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(1, remainingSecs / 60); // minimum 1 دقيقة
    }
 
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        UserSummaryResponse userResponse = userMapper.toSummaryResponse(user);
        return AuthResponse.of(
            accessToken,
            refreshToken,
            jwtProperties.accessTokenExpiration(),
            userResponse
        );
    }
}
 

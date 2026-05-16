package com.codencanvas.bloghash.controller;

 import com.codencanvas.bloghash.dto.request.auth.*;
import com.codencanvas.bloghash.dto.response.auth.AuthResponse;
import com.codencanvas.bloghash.dto.response.auth.UserProfileResponse;
import com.codencanvas.bloghash.dto.response.common.ApiResponse;

import com.codencanvas.bloghash.mapper.UserMapper;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, logout, token management")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful! Please check your email to verify your account."));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully. You can now login."));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "If this email is registered and unverified, you will receive a new verification link."));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username or email + password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) RefreshTokenRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String accessToken = authorizationHeader.substring("Bearer ".length());
        String refreshToken = request != null ? request.refreshToken() : null;

        authService.logout(accessToken, refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse profile = userMapper.toProfileResponse(principal.getUser());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}

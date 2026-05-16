package com.codencanvas.bloghash.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
 
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
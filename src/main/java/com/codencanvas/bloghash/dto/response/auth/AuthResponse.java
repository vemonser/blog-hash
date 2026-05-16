package com.codencanvas.bloghash.dto.response.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long   accessTokenExpiresIn,  // milliseconds
    String tokenType,             // "Bearer"
    UserSummaryResponse user
) {
    // Factory method عشان مش نكتب "Bearer" في كل حتة
    public static AuthResponse of(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserSummaryResponse user
    ) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}
 
package com.codencanvas.bloghash.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    Resource privateKey,
    Resource publicKey,
    long accessTokenExpiration,
    long refreshTokenExpiration
) {}
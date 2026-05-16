package com.codencanvas.bloghash.config.properties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
    int maxLoginAttempts,
    int lockAccountDurationMinutes
) {}
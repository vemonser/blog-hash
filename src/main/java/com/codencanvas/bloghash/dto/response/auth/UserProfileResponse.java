package com.codencanvas.bloghash.dto.response.auth;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
    UUID    id,
    String  username,
    String  email,
    String  avatarUrl,
    String  bio,
    String  role,
    boolean enabled,
    Instant createdAt,
    int     postsCount,     
    int     followersCount, 
    int     followingCount  
) {}
 
 
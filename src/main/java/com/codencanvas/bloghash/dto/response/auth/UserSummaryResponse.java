package com.codencanvas.bloghash.dto.response.auth;

import java.util.UUID;

public record UserSummaryResponse(
    UUID    id,
    String  username,
    String  avatarUrl,
    String  bio
) {}
 
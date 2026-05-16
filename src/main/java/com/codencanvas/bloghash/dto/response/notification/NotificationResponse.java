package com.codencanvas.bloghash.dto.response.notification;

import java.time.Instant;
import java.util.UUID;

import com.codencanvas.bloghash.dto.response.auth.UserSummaryResponse;

public record NotificationResponse(
    UUID                id,
    String              type,
    UserSummaryResponse actor,    // null لو deleted user أو system notification
    String              entityType,
    UUID                entityId,
    String              message,  // "Ahmed liked your post" — generated في Service
    boolean             read,
    Instant             createdAt
) {}
 
package com.codencanvas.bloghash.dto.response.post;

import java.util.UUID;

public record TagResponse(UUID id, String name, String slug) {}

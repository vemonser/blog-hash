package com.codencanvas.bloghash.dto.response.post;

import java.util.UUID;

public record CategoryResponse(
    UUID   id,
    String name,
    String slug,
    // Parent category (null لو root)
    // مش CategoryResponse كاملة عشان مش نعمل recursion لا نهائي
    UUID   parentId,
    String parentName
) {}
package com.codencanvas.bloghash.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
public record UpdateCommentRequest(
    @NotBlank(message = "Content cannot be empty")
    @Size(min = 1, max = 5000)
    String content
) {}
 
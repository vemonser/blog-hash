package com.codencanvas.bloghash.dto.request.post;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
 
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 300, message = "Title must be between 5 and 300 characters")
    String title,
 
    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content is too short")
    String contentMarkdown,
 
    // Optional: post بيبدأ كـ DRAFT
    UUID categoryId,
 
    // Optional: maximum 10 tags per post
    @Size(max = 10, message = "Cannot add more than 10 tags")
    Set<String> tags  
) {}
 
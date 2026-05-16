package com.codencanvas.bloghash.dto.request.post;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
 
    @Size(min = 5, max = 300)
    String title,        
 
    @Size(min = 10)
    String contentMarkdown,
 
    UUID categoryId,
 
    @Size(max = 10)
    Set<String> tags,
 
    String status
) {}
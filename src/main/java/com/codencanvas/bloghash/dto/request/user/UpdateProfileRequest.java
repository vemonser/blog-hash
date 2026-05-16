package com.codencanvas.bloghash.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
 
    // Optional: لو null مش هنغير
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    String username,
 
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    String bio
) {}
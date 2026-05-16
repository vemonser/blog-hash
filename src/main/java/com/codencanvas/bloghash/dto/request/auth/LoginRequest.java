package com.codencanvas.bloghash.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
 
    @NotBlank(message = "Username or email is required")
    String usernameOrEmail,   // يقبل الاتنين
 
    @NotBlank(message = "Password is required")
    String password
) {}
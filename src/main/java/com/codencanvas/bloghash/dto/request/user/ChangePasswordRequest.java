package com.codencanvas.bloghash.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
 
    @NotBlank(message = "Current password is required")
    String currentPassword,
 
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72)
    String newPassword,
 
    @NotBlank(message = "Please confirm your new password")
    String confirmPassword
) {
    // Compact constructor للـ validation الـ cross-field
    public ChangePasswordRequest {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must differ from current password");
        }
    }
}
package com.codencanvas.bloghash.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codencanvas.bloghash.dto.request.user.ChangePasswordRequest;
import com.codencanvas.bloghash.dto.request.user.UpdateProfileRequest;
import com.codencanvas.bloghash.dto.response.auth.UserProfileResponse;
import com.codencanvas.bloghash.dto.response.common.ApiResponse;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {
 
    private final UserService userService;
 
    // ── Public Profile ────────────────────────────────────────────
    @GetMapping("/{username}")
    @Operation(summary = "Get public user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
        @PathVariable String username,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
            userService.getProfile(username, currentUserId)
        ));
    }
 
    // ── My Profile ────────────────────────────────────────────────
    @GetMapping("/me")
    @Operation(summary = "Get my full profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.getMyProfile(principal)
        ));
    }
 
    // ── Update Profile ────────────────────────────────────────────
    @PatchMapping("/me")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.updateProfile(request, principal)
        ));
    }
 
    // ── Change Password ───────────────────────────────────────────
    @PatchMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.changePassword(request, principal);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
 
    // ── Avatar ────────────────────────────────────────────────────
    @PostMapping("/me/avatar")
    @Operation(summary = "Upload avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.uploadAvatar(file, principal)
        ));
    }
 
    // ── Follow ────────────────────────────────────────────────────
    @PostMapping("/{username}/follow")
    @Operation(summary = "Follow or unfollow a user")
    public ResponseEntity<ApiResponse<Boolean>> toggleFollow(
        @PathVariable String username,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean following = userService.toggleFollow(username, principal);
        String  message   = following ? "Now following " + username : "Unfollowed " + username;
        return ResponseEntity.ok(ApiResponse.success(message, following));
    }
 
    @GetMapping("/{username}/following-status")
    @Operation(summary = "Check if you follow a user")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
        @PathVariable String username,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            userService.isFollowing(username, principal)
        ));
    }
}

package com.codencanvas.bloghash.controller;

import com.codencanvas.bloghash.dto.request.post.*;
import com.codencanvas.bloghash.dto.response.common.*;
import com.codencanvas.bloghash.dto.response.post.*;

import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get published posts feed")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        var result = postService.getPublishedPosts(page, size, tag, category, author, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my posts (all statuses)")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.getMyPosts(principal, page, size)));
    }

    // ── Single Post ───────────────────────────────────────────────
    @GetMapping("/{slug}")
    @Operation(summary = "Get post by slug")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(
                ApiResponse.success(postService.getPostBySlug(slug, currentUserId)));
    }

    // ── Create ────────────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post (saves as DRAFT)")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PostDetailResponse post = postService.createPost(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", post));
    }

    // ── Update ────────────────────────────────────────────────────
    @PatchMapping("/{slug}")
    @Operation(summary = "Update post (partial update)")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable String slug,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.updatePost(slug, request, principal)));
    }

    // ── Publish ───────────────────────────────────────────────────
    @PostMapping("/{slug}/publish")
    @Operation(summary = "Publish a draft post")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> publishPost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success("Post published successfully", postService.publishPost(slug, principal)));
    }

    // ── Archive ───────────────────────────────────────────────────
    @PostMapping("/{slug}/archive")
    @Operation(summary = "Archive a published post")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> archivePost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.archivePost(slug, principal)));
    }

    // ── Delete ────────────────────────────────────────────────────
    @DeleteMapping("/{slug}")
    @Operation(summary = "Delete a post (soft delete)")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        postService.deletePost(slug, principal);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    // ── Cover Image ───────────────────────────────────────────────
    @PostMapping("/{slug}/cover-image")
    @Operation(summary = "Upload or replace cover image")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> uploadCover(
            @PathVariable String slug,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.uploadCoverImage(slug, file, principal)));
    }

    @DeleteMapping("/{slug}/cover-image")
    @Operation(summary = "Remove cover image")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<PostDetailResponse>> removeCover(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(postService.removeCoverImage(slug, principal)));
    }

    // ── Like / Unlike ─────────────────────────────────────────────
    @PostMapping("/{slug}/like")
    @Operation(summary = "Toggle like on a post")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ApiResponse<Long>> toggleLike(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal principal) {
        long newCount = postService.toggleLike(slug, principal);
        return ResponseEntity.ok(ApiResponse.success("Like toggled", newCount));
    }

}

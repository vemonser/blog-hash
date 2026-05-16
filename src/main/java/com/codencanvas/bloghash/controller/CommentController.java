package com.codencanvas.bloghash.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codencanvas.bloghash.dto.request.comment.CreateCommentRequest;
import com.codencanvas.bloghash.dto.request.comment.UpdateCommentRequest;
import com.codencanvas.bloghash.dto.response.comment.CommentResponse;
import com.codencanvas.bloghash.dto.response.common.ApiResponse;
import com.codencanvas.bloghash.dto.response.common.PageResponse;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    // ── Get Post Comments ─────────────────────────────────────────
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get top-level comments for a post")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getPostComments(postId, page, size, currentUserId)));
    }

    // ── Get Replies ───────────────────────────────────────────────
    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "Get replies to a comment (all depths)")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getReplies(
            @PathVariable UUID commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID currentUserId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                commentService.getReplies(commentId, page, size, currentUserId)));
    }

    // ── Create ────────────────────────────────────────────────────
    @PostMapping("/comments")
    @Operation(summary = "Create a comment or reply")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommentResponse comment = commentService.createComment(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment posted", comment));
    }

    // ── Update ────────────────────────────────────────────────────
    @PatchMapping("/comments/{commentId}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                commentService.updateComment(commentId, request, principal)));
    }

    // ── Delete ────────────────────────────────────────────────────
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.deleteComment(commentId, principal);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }

    // ── Like ──────────────────────────────────────────────────────
    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "Toggle like on a comment")
    public ResponseEntity<ApiResponse<Long>> toggleLike(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = commentService.toggleLike(commentId, principal);
        return ResponseEntity.ok(ApiResponse.success("Like toggled", count));
    }
}

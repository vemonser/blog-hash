package com.codencanvas.bloghash.service;

import com.codencanvas.bloghash.domain.comment.Comment;
import com.codencanvas.bloghash.domain.comment.CommentReaction;
import com.codencanvas.bloghash.domain.comment.CommentReactionId;
import com.codencanvas.bloghash.domain.post.Post;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.dto.request.comment.CreateCommentRequest;
import com.codencanvas.bloghash.dto.request.comment.UpdateCommentRequest;
import com.codencanvas.bloghash.dto.response.comment.CommentResponse;
import com.codencanvas.bloghash.dto.response.common.PageResponse;
import com.codencanvas.bloghash.exception.*;
import com.codencanvas.bloghash.exception.base.BlogHashException;
import com.codencanvas.bloghash.mapper.CommentMapper;
import com.codencanvas.bloghash.repository.*;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, UserPrincipal principal) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post"));

        if (!post.isPublished())
            throw new ResourceNotFoundException("Post");

        User user = loadUser(principal.getId());
        Comment parent = null;

        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment"));

            // تأكد إن الـ parent على نفس الـ post
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new BlogHashException("Parent comment does not belong to this post", 400);
            }

            // لا يجوز الرد على comment محذوف
            if (parent.isDeleted()) {
                throw new BlogHashException("Cannot reply to a deleted comment", 400);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .parent(parent)
                .content(request.content())
                .path("TEMP") // @PrePersist بيبنيها تلقائياً
                .build();

        commentRepository.save(comment); // @PrePersist → path + depth
        log.debug("Comment created by {} on post {}", user.getEmail(), post.getSlug());

        return enrichComment(comment, principal.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getPostComments(
            UUID postId, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository
                .findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable);

        return PageResponse.from(
                comments.map(c -> enrichComment(c, currentUserId)));
    }

    // ── Read (Replies) ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getReplies(
            UUID parentId, int page, int size, UUID currentUserId) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment"));

        // جيب كل descendants بـ Materialized Path
        String pathPrefix = parent.getPath() + ".";
        var allDescendants = commentRepository
                .findAllDescendants(pathPrefix + "%", parentId);

        // Paginate in memory (acceptable للـ depth المحدود)
        int start = page * size;
        int end = Math.min(start + size, allDescendants.size());

        var slice = start >= allDescendants.size()
                ? java.util.List.<Comment>of()
                : allDescendants.subList(start, end);

        var responses = slice.stream()
                .map(c -> enrichComment(c, currentUserId))
                .toList();

        return new PageResponse<>(
                responses, page, size,
                allDescendants.size(),
                (int) Math.ceil((double) allDescendants.size() / size),
                page == 0,
                end >= allDescendants.size());
    }

    // ── Update ────────────────────────────────────────────────────
    @Transactional
    public CommentResponse updateComment(
            UUID commentId, UpdateCommentRequest request, UserPrincipal principal) {
        Comment comment = loadCommentAndCheckOwnership(commentId, principal);
        comment.updateContent(request.content());
        commentRepository.save(comment);
        return enrichComment(comment, principal.getId());
    }

    // ── Delete ────────────────────────────────────────────────────
    @Transactional
    public void deleteComment(UUID commentId, UserPrincipal principal) {
        Comment comment = loadCommentAndCheckOwnership(commentId, principal);
        comment.softDelete(); // content → "[deleted]"
        commentRepository.save(comment);
    }

    // ── Like ──────────────────────────────────────────────────────
    @Transactional
    public long toggleLike(UUID commentId, UserPrincipal principal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment"));

        UUID userId = principal.getId();

        if (commentReactionRepository.existsByIdUserIdAndIdCommentId(userId, commentId)) {
            commentReactionRepository.deleteByUserIdAndCommentId(userId, commentId);
        } else {
            User user = loadUser(userId);
            CommentReaction reaction = CommentReaction.builder()
                    .id(new CommentReactionId(userId, commentId))
                    .user(user)
                    .comment(comment)
                    .build();
            commentReactionRepository.save(reaction);
        }

        return commentReactionRepository.countByIdCommentId(commentId);
    }

    // ── Private ───────────────────────────────────────────────────
    private Comment loadCommentAndCheckOwnership(UUID commentId, UserPrincipal principal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment"));

        boolean isOwner = comment.getAuthor().getId().equals(principal.getId());
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new BlogHashException("You don't have permission to modify this comment", 403);
        }

        return comment;
    }

    private CommentResponse enrichComment(Comment comment, UUID currentUserId) {
        CommentResponse base = commentMapper.toResponse(comment);
        long likes = commentReactionRepository.countByIdCommentId(comment.getId());
        long replies = commentRepository.countByParentId(comment.getId());
        boolean liked = currentUserId != null &&
                commentReactionRepository.existsByIdUserIdAndIdCommentId(currentUserId, comment.getId());

        return new CommentResponse(
                base.id(), base.content(), base.author(),
                base.parentId(), base.path(), base.depth(),
                (int) likes, (int) replies, base.deleted(),
                liked, base.createdAt(), base.updatedAt());
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }
}

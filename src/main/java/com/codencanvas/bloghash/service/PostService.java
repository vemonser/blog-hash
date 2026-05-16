package com.codencanvas.bloghash.service;

import com.codencanvas.bloghash.domain.post.*;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.dto.request.post.*;
import com.codencanvas.bloghash.dto.response.common.PageResponse;
import com.codencanvas.bloghash.dto.response.post.*;
import com.codencanvas.bloghash.exception.*;
import com.codencanvas.bloghash.exception.base.BlogHashException;
import com.codencanvas.bloghash.mapper.PostMapper;
import com.codencanvas.bloghash.repository.*;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.CloudinaryService;
import com.codencanvas.bloghash.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final PostMapper postMapper;
    private final SlugGenerator slugGenerator;
    private final TocGenerator tocGenerator;
    private final ContentAnalyzer contentAnalyzer;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, UserPrincipal principal) {
        User user = loadUser(principal.getId());

        String slug = slugGenerator.generateUniqueSlug(request.title());
        String toc = tocGenerator.generate(request.contentMarkdown());
        int wordCount = contentAnalyzer.countWords(request.contentMarkdown());
        int readingTime = contentAnalyzer.calculateReadingTimeMins(request.contentMarkdown());
        Category category = resolveCategory(request.categoryId());
        Set<Tag> tags = resolveTags(request.tags());

        Post post = Post.builder()
                .author(user)
                .title(request.title())
                .slug(slug)
                .contentMarkdown(request.contentMarkdown())
                .toc(toc)
                .wordCount(wordCount)
                .readingTimeMins(readingTime)
                .category(category)
                .tags(tags)
                .build();

        postRepository.save(post);
        log.info("Post created: {} by user: {}", slug, user.getEmail());

        return enrichDetail(post, principal.getId());
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostBySlug(String slug, UUID currentUserId) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post"));

        // Guests بيشوفوا published posts بس
        // Owner والـ Admin بيشوفوا draft و archived كمان
        if (!post.isPublished()) {
            boolean isOwner = currentUserId != null &&
                    post.getAuthor().getId().equals(currentUserId);
            if (!isOwner)
                throw new ResourceNotFoundException("Post");
        }

        return enrichDetail(post, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getPublishedPosts(
            int page, int size, String tag, String category, String author, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts;

        if (tag != null) {
            posts = postRepository.findPublishedByTagSlug(tag, pageable);
        } else if (category != null) {
            posts = postRepository.findByCategorySlugAndStatusOrderByPublishedAtDesc(
                    category, PostStatus.PUBLISHED, pageable);
        } else if (author != null) {
            posts = postRepository.findByAuthorUsernameAndStatusOrderByPublishedAtDesc(
                    author, PostStatus.PUBLISHED, pageable);
        } else {
            posts = postRepository.findByStatusOrderByPublishedAtDesc(
                    PostStatus.PUBLISHED, pageable);
        }

        Page<PostSummaryResponse> mapped = posts.map(p -> enrichSummary(p, currentUserId));
        return PageResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostSummaryResponse> getMyPosts(
            UserPrincipal principal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository
                .findByAuthorUsernameOrderByCreatedAtDesc(
                        principal.getUser().getUsername(), pageable);

        return PageResponse.from(posts.map(p -> enrichSummary(p, principal.getId())));
    }

    @Transactional
    public PostDetailResponse updatePost(
            String slug, UpdatePostRequest request, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);

        if (request.title() != null || request.contentMarkdown() != null) {
            String newTitle = request.title() != null
                    ? request.title()
                    : post.getTitle();
            String newContent = request.contentMarkdown() != null
                    ? request.contentMarkdown()
                    : post.getContentMarkdown();

            String newToc = tocGenerator.generate(newContent);
            int newWordCount = contentAnalyzer.countWords(newContent);
            int newReadingTime = contentAnalyzer.calculateReadingTimeMins(newContent);

            post.updateContent(newTitle, newContent, newToc, newWordCount, newReadingTime);
        }

        if (request.categoryId() != null) {
            post.updateCategory(resolveCategory(request.categoryId()));
        }

        if (request.tags() != null) {
            post.replaceTags(resolveTags(request.tags()));
        }

        if (request.status() != null) {
            applyStatusChange(post, request.status());
        }

        postRepository.save(post);
        return enrichDetail(post, principal.getId());
    }

    @Transactional
    public PostDetailResponse publishPost(String slug, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);

        if (post.isPublished()) {
            throw new BlogHashException("Post is already published", 400);
        }

        post.publish();
        postRepository.save(post);
        log.info("Post published: {}", slug);

        return enrichDetail(post, principal.getId());
    }

    @Transactional
    public PostDetailResponse archivePost(String slug, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);
        post.archive();
        postRepository.save(post);
        return enrichDetail(post, principal.getId());
    }

    // ════════════════════════════════════════════════════════════
    // DELETE
    // ════════════════════════════════════════════════════════════

    @Transactional
    public void deletePost(String slug, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);

        // حذف الصورة من Cloudinary أولاً
        if (post.getCoverImagePublicId() != null) {
            cloudinaryService.delete(post.getCoverImagePublicId());
        }

        post.softDelete();
        postRepository.save(post);
        log.info("Post soft-deleted: {}", slug);
    }

    // ════════════════════════════════════════════════════════════
    // COVER IMAGE
    // ════════════════════════════════════════════════════════════

    @Transactional
    public PostDetailResponse uploadCoverImage(
            String slug, MultipartFile file, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);

        // حذف الصورة القديمة لو موجودة
        if (post.getCoverImagePublicId() != null) {
            cloudinaryService.delete(post.getCoverImagePublicId());
        }

        CloudinaryService.UploadResult result = cloudinaryService.upload(file, "bloghash/posts/covers");

        post.updateCoverImage(result.url(), result.publicId());
        postRepository.save(post);

        return enrichDetail(post, principal.getId());
    }

    @Transactional
    public PostDetailResponse removeCoverImage(String slug, UserPrincipal principal) {
        Post post = loadPostAndCheckOwnership(slug, principal);

        if (post.getCoverImagePublicId() != null) {
            cloudinaryService.delete(post.getCoverImagePublicId());
            post.removeCoverImage();
            postRepository.save(post);
        }

        return enrichDetail(post, principal.getId());
    }

    @Transactional
    public long toggleLike(String slug, UserPrincipal principal) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post"));

        if (!post.isPublished())
            throw new ResourceNotFoundException("Post");

        UUID userId = principal.getId();
        UUID postId = post.getId();

        if (reactionRepository.existsByIdUserIdAndIdPostId(userId, postId)) {
            // Already liked → unlike
            reactionRepository.deleteByUserIdAndPostId(userId, postId);
        } else {
            // Not liked → like
            User user = loadUser(userId);
            PostReaction reaction = PostReaction.builder()
                    .id(new PostReactionId(userId, postId))
                    .user(user)
                    .post(post)
                    .build();
            reactionRepository.save(reaction);
        }

        return reactionRepository.countByIdPostId(postId);
    }

    // ════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════

    private Post loadPostAndCheckOwnership(String slug, UserPrincipal principal) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post"));

        boolean isOwner = post.getAuthor().getId().equals(principal.getId());
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new BlogHashException("You don't have permission to modify this post", 403);
        }

        return post;
    }

    private PostDetailResponse enrichDetail(Post post, UUID currentUserId) {
        PostDetailResponse base = postMapper.toDetailResponse(post);
        int likes = (int) reactionRepository.countByIdPostId(post.getId());
        int comments = (int) commentRepository.countByPostIdAndDeletedFalse(post.getId());
        boolean liked = currentUserId != null &&
                reactionRepository.existsByIdUserIdAndIdPostId(currentUserId, post.getId());

        return base.withCounts(likes, comments, liked);
    }

    private PostSummaryResponse enrichSummary(Post post, UUID currentUserId) {
        PostSummaryResponse base = postMapper.toSummaryResponse(post);
        int likes = (int) reactionRepository.countByIdPostId(post.getId());
        int comments = (int) commentRepository.countByPostIdAndDeletedFalse(post.getId());
        boolean liked = currentUserId != null &&
                reactionRepository.existsByIdUserIdAndIdPostId(currentUserId, post.getId());

        return base.withCounts(likes, comments, liked);
    }

    /**
     * Find or create tags by slug.
     * Race condition edge case: لو اتنين بيضيفوا نفس الـ tag في نفس الوقت
     * → DataIntegrityViolationException → نـ catch ونـ find الموجود.
     */
    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty())
            return new HashSet<>();

        return tagNames.stream()
                .map(name -> {
                    String slug = name.toLowerCase()
                            .replaceAll("[^a-z0-9]", "-")
                            .replaceAll("-+", "-")
                            .replaceAll("^-|-$", "");

                    return tagRepository.findBySlug(slug)
                            .orElseGet(() -> {
                                try {
                                    return tagRepository.save(
                                            Tag.builder().name(name).slug(slug).build());
                                } catch (Exception e) {
                                    return tagRepository.findBySlug(slug).orElseThrow();
                                }
                            });
                })
                .collect(Collectors.toSet());
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null)
            return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category"));
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    private void applyStatusChange(Post post, String status) {
        switch (status.toUpperCase()) {
            case "PUBLISHED" -> post.publish();
            case "ARCHIVED" -> post.archive();
            case "DRAFT" -> post.backToDraft();
            default -> throw new BlogHashException("Invalid status: " + status, 400);
        }
    }

}

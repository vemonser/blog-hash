package com.codencanvas.bloghash.service;

import com.codencanvas.bloghash.domain.social.Follower;
import com.codencanvas.bloghash.domain.social.FollowerId;
import com.codencanvas.bloghash.domain.user.User;
import com.codencanvas.bloghash.dto.request.user.*;
import com.codencanvas.bloghash.dto.response.auth.UserProfileResponse;
import com.codencanvas.bloghash.dto.response.user.*;
import com.codencanvas.bloghash.exception.*;
import com.codencanvas.bloghash.exception.base.BlogHashException;
import com.codencanvas.bloghash.mapper.UserMapper;
import com.codencanvas.bloghash.repository.*;
import com.codencanvas.bloghash.security.principal.UserPrincipal;
import com.codencanvas.bloghash.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
 
    private final UserRepository      userRepository;
    private final FollowerRepository  followerRepository;
    private final PostRepository      postRepository;
    private final CommentRepository   commentRepository;
    private final UserMapper          userMapper;
    private final PasswordEncoder     passwordEncoder;
    private final CloudinaryService   cloudinaryService;
 
    // ── Get Profile ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username, UUID currentUserId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User"));
 
        return buildProfileResponse(user);
    }
 
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UserPrincipal principal) {
        return buildProfileResponse(principal.getUser());
    }
 
    // ── Update Profile ────────────────────────────────────────────
    @Transactional
    public UserProfileResponse updateProfile(
        UpdateProfileRequest request, UserPrincipal principal
    ) {
        User user = principal.getUser();
 
        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new UsernameAlreadyTakenException();
            }
        }
 
        if (request.bio() != null) {
            user.updateBio(request.bio());
        }
 
        userRepository.save(user);
        return buildProfileResponse(user);
    }
 
    // ── Change Password ───────────────────────────────────────────
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserPrincipal principal) {
        User user = principal.getUser();
 
        if (!user.hasLocalPassword()) {
            throw new BlogHashException(
                "OAuth2 accounts cannot set a password this way. Use your provider's settings.", 400
            );
        }
 
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
 
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }
 
    // ── Avatar ────────────────────────────────────────────────────
    @Transactional
    public UserProfileResponse uploadAvatar(MultipartFile file, UserPrincipal principal) {
        User user = principal.getUser();
 
        if (user.getAvatarPublicId() != null) {
            cloudinaryService.delete(user.getAvatarPublicId());
        }
 
        CloudinaryService.UploadResult result =
            cloudinaryService.upload(file, "bloghash/users/avatars");
 
        user.updateAvatar(result.url(), result.publicId());
        userRepository.save(user);
 
        return buildProfileResponse(user);
    }
 
    // ── Follow / Unfollow ─────────────────────────────────────────
    /**
     * Toggle follow: لو following → unfollow، لو مش following → follow.
     *
     * Edge cases:
     *   - مش تـ follow نفسك
     *   - User مش موجود → 404
     */
    @Transactional
    public boolean toggleFollow(String targetUsername, UserPrincipal principal) {
        if (targetUsername.equals(principal.getUser().getUsername())) {
            throw new BlogHashException("You cannot follow yourself", 400);
        }
 
        User target = userRepository.findByUsername(targetUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User"));
 
        UUID followerId  = principal.getId();
        UUID followingId = target.getId();
        FollowerId id    = new FollowerId(followerId, followingId);
 
        if (followerRepository.existsById(id)) {
            followerRepository.deleteById(id);
            return false; // unfollowed
        } else {
            User follower = principal.getUser();
            followerRepository.save(
                Follower.builder()
                    .id(id)
                    .follower(follower)
                    .following(target)
                    .build()
            );
            return true; // followed
        }
    }
 
    @Transactional(readOnly = true)
    public boolean isFollowing(String targetUsername, UserPrincipal principal) {
        User target = userRepository.findByUsername(targetUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User"));
 
        return followerRepository.existsById(
            new FollowerId(principal.getId(), target.getId())
        );
    }
 
    // ── Private ───────────────────────────────────────────────────
    private UserProfileResponse buildProfileResponse(User user) {
        long postsCount     = postRepository.countByAuthorId(user.getId());
        long followersCount = followerRepository.countByIdFollowingId(user.getId());
        long followingCount = followerRepository.countByIdFollowerId(user.getId());
 
        UserProfileResponse base = userMapper.toProfileResponse(user);
 
        // Records مش بيدعموا setter → بنعمل instance جديد
        return new UserProfileResponse(
            base.id(), base.username(), base.email(),
            base.avatarUrl(), base.bio(), base.role(),
            base.enabled(), base.createdAt(),
            (int) postsCount, (int) followersCount, (int) followingCount
        );
    }
}

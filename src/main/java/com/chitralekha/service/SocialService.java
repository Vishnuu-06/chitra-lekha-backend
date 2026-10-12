package com.chitralekha.service;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.dto.PostSummaryDto;
import com.chitralekha.entity.*;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.integration.ContentNormalizationService;
import com.chitralekha.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SocialService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ContentRepository contentRepository;
    private final UserContentActivityRepository activityRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final ContentListRepository listRepository;
    private final ContentNormalizationService normalizationService;
    private final AuthService authService;

    public SocialService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository,
            FollowRepository followRepository,
            UserRepository userRepository,
            ProfileRepository profileRepository,
            ContentRepository contentRepository,
            UserContentActivityRepository activityRepository,
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            ContentListRepository listRepository,
            ContentNormalizationService normalizationService,
            AuthService authService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.contentRepository = contentRepository;
        this.activityRepository = activityRepository;
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.listRepository = listRepository;
        this.normalizationService = normalizationService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<PostSummaryDto> getFeed(int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 30));
        Optional<User> currentUserOpt = authService.getAuthenticatedUserOptional();

        if (currentUserOpt.isPresent()) {
            User current = currentUserOpt.get();
            List<Long> followeeIds = followRepository.findFolloweeIds(current);
            if (!followeeIds.isEmpty()) {
                List<User> targetUsers = new ArrayList<>(userRepository.findAllById(followeeIds));
                targetUsers.add(current); // include own posts
                Page<Post> feedPosts = postRepository.findByUserInOrderByCreatedAtDesc(targetUsers, pageRequest);
                if (feedPosts.hasContent()) {
                    return feedPosts.getContent().stream().map(this::toPostDto).toList();
                }
            }
        }

        // Discovery feed if not following anyone or no posts from followees
        return postRepository.findAllByOrderByCreatedAtDesc(pageRequest)
                .getContent().stream()
                .map(this::toPostDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostSummaryDto> getUserPosts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return postRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toPostDto)
                .toList();
    }

    @Transactional
    public PostSummaryDto createPost(Long contentId, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Post body cannot be empty");
        }
        User user = authService.getCurrentUser();
        Content content = null;
        if (contentId != null) {
            content = contentRepository.findById(contentId).orElse(null);
        }

        Post post = postRepository.save(new Post(user, content, body.trim()));
        return toPostDto(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        User user = authService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    @Transactional
    public boolean toggleLikePost(Long postId) {
        User user = authService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return false;
        } else {
            postLikeRepository.save(new PostLike(post, user));
            return true;
        }
    }

    @Transactional
    public PostComment addComment(Long postId, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment body cannot be empty");
        }
        User user = authService.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        return postCommentRepository.save(new PostComment(post, user, body.trim()));
    }

    public List<Map<String, Object>> getPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        return postCommentRepository.findByPostOrderByCreatedAtAsc(post).stream().map(c -> {
            Profile profile = profileRepository.findByUserId(c.getUser().getId()).orElse(null);
            return Map.<String, Object>of(
                    "id", c.getId(),
                    "body", c.getBody(),
                    "author", Map.of(
                            "id", c.getUser().getId(),
                            "username", c.getUser().getUsername(),
                            "displayName", profile != null ? profile.getDisplayName() : c.getUser().getUsername(),
                            "avatarUrl", profile != null && profile.getAvatarUrl() != null ? profile.getAvatarUrl() : ""
                    ),
                    "createdAt", c.getCreatedAt().toString()
            );
        }).toList();
    }

    @Transactional
    public boolean toggleFollow(String username) {
        User current = authService.getCurrentUser();
        User target = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (current.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        Optional<Follow> existing = followRepository.findByFollowerAndFollowee(current, target);
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return false;
        } else {
            followRepository.save(new Follow(current, target));
            return true;
        }
    }

    public Map<String, Object> getUserProfileData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new Profile(user, user.getUsername(), "", "", "", "", "", ""));

        long followersCount = followRepository.countByFollowee(user);
        long followingCount = followRepository.countByFollower(user);
        long ratingsCount = ratingRepository.findByUserOrderByCreatedAtDesc(user).size();
        long reviewsCount = reviewRepository.countByUser(user);
        long listsCount = listRepository.countByOwner(user);
        long watchedMovies = activityRepository.findByUserAndContentType(user, "MOVIE").stream()
                .filter(a -> "WATCHED".equalsIgnoreCase(a.getStatus())).count();
        long readBooks = activityRepository.findByUserAndContentType(user, "BOOK").stream()
                .filter(a -> "READ".equalsIgnoreCase(a.getStatus())).count();

        boolean isFollowing = false;
        Optional<User> current = authService.getAuthenticatedUserOptional();
        if (current.isPresent() && !current.get().getId().equals(user.getId())) {
            isFollowing = followRepository.existsByFollowerAndFollowee(current.get(), user);
        }

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                ),
                "profile", Map.of(
                        "displayName", profile.getDisplayName(),
                        "bio", profile.getBio() != null ? profile.getBio() : "",
                        "avatarUrl", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : "",
                        "favoriteMovies", profile.getFavoriteMovies() != null ? profile.getFavoriteMovies() : "",
                        "favoriteShows", profile.getFavoriteShows() != null ? profile.getFavoriteShows() : "",
                        "favoriteAnime", profile.getFavoriteAnime() != null ? profile.getFavoriteAnime() : "",
                        "favoriteBooks", profile.getFavoriteBooks() != null ? profile.getFavoriteBooks() : ""
                ),
                "stats", Map.of(
                        "followersCount", followersCount,
                        "followingCount", followingCount,
                        "ratingsCount", ratingsCount,
                        "reviewsCount", reviewsCount,
                        "listsCount", listsCount,
                        "watchedMoviesCount", watchedMovies,
                        "readBooksCount", readBooks
                ),
                "isFollowing", isFollowing,
                "isSelf", current.isPresent() && current.get().getId().equals(user.getId())
        );
    }

    @Transactional
    public Profile updateProfile(Map<String, String> payload) {
        User user = authService.getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new Profile(user, user.getUsername(), "", "", "", "", "", ""));

        if (payload.containsKey("displayName")) profile.setDisplayName(payload.get("displayName").trim());
        if (payload.containsKey("bio")) profile.setBio(payload.get("bio").trim());
        if (payload.containsKey("avatarUrl")) profile.setAvatarUrl(payload.get("avatarUrl").trim());
        if (payload.containsKey("favoriteMovies")) profile.setFavoriteMovies(payload.get("favoriteMovies").trim());
        if (payload.containsKey("favoriteShows")) profile.setFavoriteShows(payload.get("favoriteShows").trim());
        if (payload.containsKey("favoriteAnime")) profile.setFavoriteAnime(payload.get("favoriteAnime").trim());
        if (payload.containsKey("favoriteBooks")) profile.setFavoriteBooks(payload.get("favoriteBooks").trim());

        return profileRepository.save(profile);
    }

    public List<Map<String, Object>> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return followRepository.findByFollowee(user).stream()
                .map(f -> toUserMap(f.getFollower()))
                .toList();
    }

    public List<Map<String, Object>> getFollowing(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return followRepository.findByFollower(user).stream()
                .map(f -> toUserMap(f.getFollowee()))
                .toList();
    }

    public List<Map<String, Object>> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query.trim().toLowerCase()))
                .limit(10)
                .map(this::toUserMap)
                .toList();
    }

    private Map<String, Object> toUserMap(User u) {
        Profile p = profileRepository.findByUserId(u.getId()).orElse(null);
        return Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "displayName", p != null ? p.getDisplayName() : u.getUsername(),
                "avatarUrl", p != null && p.getAvatarUrl() != null ? p.getAvatarUrl() : "",
                "bio", p != null && p.getBio() != null ? p.getBio() : ""
        );
    }

    private PostSummaryDto toPostDto(Post post) {
        User user = post.getUser();
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String displayName = profile != null ? profile.getDisplayName() : user.getUsername();
        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;
        Content content = post.getContent();
        return new PostSummaryDto(
                post.getId(),
                user.getId(),
                user.getUsername(),
                displayName,
                avatarUrl,
                content == null ? null : normalizationService.toDto(content),
                post.getBody(),
                post.getCreatedAt()
        );
    }
}

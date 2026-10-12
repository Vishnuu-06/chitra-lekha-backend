package com.chitralekha.controller;

import com.chitralekha.dto.PostSummaryDto;
import com.chitralekha.entity.PostComment;
import com.chitralekha.entity.Profile;
import com.chitralekha.service.SocialService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FeedController {
    private final SocialService socialService;

    public FeedController(SocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostSummaryDto>> getFeed(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(socialService.getFeed(page, size));
    }

    @GetMapping("/users/{username}/posts")
    public ResponseEntity<List<PostSummaryDto>> getUserPosts(@PathVariable String username) {
        return ResponseEntity.ok(socialService.getUserPosts(username));
    }

    @GetMapping("/users/{username}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(socialService.getUserProfileData(username));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<Profile> updateProfile(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(socialService.updateProfile(payload));
    }

    @PostMapping("/users/{username}/follow")
    public ResponseEntity<Map<String, Object>> toggleFollow(@PathVariable String username) {
        boolean following = socialService.toggleFollow(username);
        return ResponseEntity.ok(Map.of("following", following));
    }

    @GetMapping("/users/{username}/followers")
    public ResponseEntity<List<Map<String, Object>>> getFollowers(@PathVariable String username) {
        return ResponseEntity.ok(socialService.getFollowers(username));
    }

    @GetMapping("/users/{username}/following")
    public ResponseEntity<List<Map<String, Object>>> getFollowing(@PathVariable String username) {
        return ResponseEntity.ok(socialService.getFollowing(username));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam("query") String query) {
        return ResponseEntity.ok(socialService.searchUsers(query));
    }

    @PostMapping("/posts")
    public ResponseEntity<PostSummaryDto> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(socialService.createPost(request.getContentId(), request.getBody()));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long postId) {
        socialService.deletePost(postId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLikePost(@PathVariable Long postId) {
        boolean liked = socialService.toggleLikePost(postId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<PostComment> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> payload) {
        String body = payload.getOrDefault("body", "");
        return ResponseEntity.ok(socialService.addComment(postId, body));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(socialService.getPostComments(postId));
    }

    public static class CreatePostRequest {
        private Long contentId;
        @NotBlank(message = "Post body is required")
        private String body;

        public Long getContentId() { return contentId; }
        public void setContentId(Long contentId) { this.contentId = contentId; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
}

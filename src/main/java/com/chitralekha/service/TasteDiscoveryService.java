package com.chitralekha.service;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.Profile;
import com.chitralekha.entity.Rating;
import com.chitralekha.entity.User;
import com.chitralekha.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TasteDiscoveryService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RatingRepository ratingRepository;
    private final FollowRepository followRepository;
    private final UserBlockRepository blockRepository;
    private final AuthService authService;

    public TasteDiscoveryService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            RatingRepository ratingRepository,
            FollowRepository followRepository,
            UserBlockRepository blockRepository,
            AuthService authService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.ratingRepository = ratingRepository;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
        this.authService = authService;
    }

    public List<Map<String, Object>> discoverSimilarUsers() {
        Optional<User> currentOpt = authService.getAuthenticatedUserOptional();
        if (currentOpt.isEmpty()) {
            return getPopularTastemakers(null);
        }

        User current = currentOpt.get();
        List<Long> followedIds = followRepository.findFolloweeIds(current);
        List<Rating> myHighRatings = ratingRepository.findHighlyRatedByUser(current);
        Set<Long> myHighlyRatedContentIds = myHighRatings.stream()
                .map(r -> r.getContent().getId())
                .collect(Collectors.toSet());

        Profile myProfile = profileRepository.findByUserId(current.getId()).orElse(null);

        List<User> allCandidates = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(current.getId()))
                .filter(u -> !followedIds.contains(u.getId()))
                .filter(u -> !blockRepository.isBlockedEitherWay(current, u))
                .toList();

        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (User candidate : allCandidates) {
            List<Rating> candidateRatings = ratingRepository.findHighlyRatedByUser(candidate);
            List<String> sharedTitles = new ArrayList<>();

            for (Rating r : candidateRatings) {
                if (myHighlyRatedContentIds.contains(r.getContent().getId())) {
                    sharedTitles.add(r.getContent().getTitle());
                }
            }

            Profile candidateProfile = profileRepository.findByUserId(candidate.getId()).orElse(null);
            List<String> reasons = new ArrayList<>();

            if (!sharedTitles.isEmpty()) {
                if (sharedTitles.size() == 1) {
                    reasons.add("Both rated " + sharedTitles.get(0) + " highly (7+)");
                } else {
                    reasons.add("Both rated " + sharedTitles.get(0) + " and " + sharedTitles.get(1) + " highly");
                }
            }

            // Genre & favorites overlap check
            if (myProfile != null && candidateProfile != null) {
                if (hasOverlap(myProfile.getFavoriteMovies(), candidateProfile.getFavoriteMovies())) {
                    reasons.add("Shares favorite movies: " + candidateProfile.getFavoriteMovies());
                }
                if (hasOverlap(myProfile.getFavoriteBooks(), candidateProfile.getFavoriteBooks())) {
                    reasons.add("Shares favorite books: " + candidateProfile.getFavoriteBooks());
                }
            }

            // If user has insufficient activity, provide transparent rationale based on candidate's curation
            if (reasons.isEmpty()) {
                if (!candidateRatings.isEmpty()) {
                    Content topPick = candidateRatings.get(0).getContent();
                    reasons.add("Curates " + topPick.getContentType() + " picks including " + topPick.getTitle());
                } else if (candidateProfile != null && candidateProfile.getBio() != null && !candidateProfile.getBio().isEmpty()) {
                    reasons.add("Enthusiast: " + candidateProfile.getBio());
                } else {
                    reasons.add("Active storytelling member");
                }
            }

            Map<String, Object> rec = new HashMap<>();
            rec.put("id", candidate.getId());
            rec.put("username", candidate.getUsername());
            rec.put("displayName", candidateProfile != null ? candidateProfile.getDisplayName() : candidate.getUsername());
            rec.put("avatarUrl", candidateProfile != null && candidateProfile.getAvatarUrl() != null ? candidateProfile.getAvatarUrl() : "");
            rec.put("bio", candidateProfile != null && candidateProfile.getBio() != null ? candidateProfile.getBio() : "");
            rec.put("sharedTitlesCount", sharedTitles.size());
            rec.put("explanation", String.join(". ", reasons));
            recommendations.add(rec);
        }

        // Sort by shared titles count descending
        recommendations.sort((a, b) -> Integer.compare(
                (Integer) b.get("sharedTitlesCount"),
                (Integer) a.get("sharedTitlesCount")
        ));

        return recommendations.stream().limit(8).toList();
    }

    private List<Map<String, Object>> getPopularTastemakers(User exclude) {
        return userRepository.findAll().stream()
                .filter(u -> exclude == null || !u.getId().equals(exclude.getId()))
                .limit(6)
                .map(u -> {
                    Profile p = profileRepository.findByUserId(u.getId()).orElse(null);
                    return Map.<String, Object>of(
                            "id", u.getId(),
                            "username", u.getUsername(),
                            "displayName", p != null ? p.getDisplayName() : u.getUsername(),
                            "avatarUrl", p != null && p.getAvatarUrl() != null ? p.getAvatarUrl() : "",
                            "bio", p != null && p.getBio() != null ? p.getBio() : "",
                            "sharedTitlesCount", 0,
                            "explanation", "Active community member sharing reviews and collections"
                    );
                }).toList();
    }

    private boolean hasOverlap(String s1, String s2) {
        if (s1 == null || s2 == null || s1.trim().isEmpty() || s2.trim().isEmpty()) return false;
        String[] parts1 = s1.toLowerCase().split("[,;]");
        String s2Lower = s2.toLowerCase();
        for (String p : parts1) {
            String clean = p.trim();
            if (!clean.isEmpty() && s2Lower.contains(clean)) return true;
        }
        return false;
    }
}

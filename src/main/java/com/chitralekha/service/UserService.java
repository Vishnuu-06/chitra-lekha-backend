package com.chitralekha.service;

import com.chitralekha.dto.ProfileSummaryDto;
import com.chitralekha.dto.UserSummaryDto;
import com.chitralekha.entity.Profile;
import com.chitralekha.entity.User;
import com.chitralekha.repository.ProfileRepository;
import com.chitralekha.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.authService = authService;
    }

    public UserSummaryDto getCurrentUserSummary() {
        User user = authService.getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElse(new Profile(user, user.getUsername(), "", "", "", "", "", ""));
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile.getDisplayName(),
                profile.getAvatarUrl()
        );
    }

    public ProfileSummaryDto getCurrentUserProfile() {
        User user = authService.getCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new Profile(user, user.getUsername(), "", "", "", "", "", ""));
        return new ProfileSummaryDto(
                profile.getId(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getFavoriteMovies(),
                profile.getFavoriteShows(),
                profile.getFavoriteAnime(),
                profile.getFavoriteBooks()
        );
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

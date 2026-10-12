package com.chitralekha.controller;

import com.chitralekha.dto.AuthRequest;
import com.chitralekha.dto.AuthResponse;
import com.chitralekha.dto.LoginRequest;
import com.chitralekha.dto.UserSummaryDto;
import com.chitralekha.entity.Profile;
import com.chitralekha.entity.User;
import com.chitralekha.repository.ProfileRepository;
import com.chitralekha.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final ProfileRepository profileRepository;

    public AuthController(AuthService authService, ProfileRepository profileRepository) {
        this.authService = authService;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return ResponseEntity.ok(authService.register(request, httpRequest, httpResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Optional<User> userOpt = authService.getAuthenticatedUserOptional();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthenticated"));
        }
        User user = userOpt.get();
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return ResponseEntity.ok(new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getAvatarUrl() : null
        ));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        authService.deleteCurrentUserAccount();
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}

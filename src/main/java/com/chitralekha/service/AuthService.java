package com.chitralekha.service;

import com.chitralekha.dto.AuthRequest;
import com.chitralekha.dto.AuthResponse;
import com.chitralekha.dto.LoginRequest;
import com.chitralekha.entity.Profile;
import com.chitralekha.entity.User;
import com.chitralekha.repository.ProfileRepository;
import com.chitralekha.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public AuthResponse register(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                request.getUsername().trim().toLowerCase(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                true
        );
        User savedUser = userRepository.save(user);

        Profile profile = new Profile(
                savedUser,
                request.getUsername(),
                "New storyteller on Chitra Lekha",
                "",
                "",
                "",
                "",
                ""
        );
        profileRepository.save(profile);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );
        setSecurityContext(authentication, httpRequest, httpResponse);

        return new AuthResponse(
                savedUser.getId(),
                httpRequest != null && httpRequest.getSession(false) != null ? httpRequest.getSession(false).getId() : "",
                savedUser.getUsername(),
                savedUser.getEmail(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String usernameOrEmail = request.getUsernameOrEmail().trim().toLowerCase();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );
        setSecurityContext(authentication, httpRequest, httpResponse);

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String displayName = profile != null ? profile.getDisplayName() : user.getUsername();
        String avatarUrl = profile != null ? profile.getAvatarUrl() : "";

        return new AuthResponse(
                user.getId(),
                httpRequest != null && httpRequest.getSession(false) != null ? httpRequest.getSession(false).getId() : "",
                user.getUsername(),
                user.getEmail(),
                displayName,
                avatarUrl,
                "Login successful"
        );
    }

    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContextHolder.clearContext();
        if (httpRequest != null) {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
    }

    public Optional<User> getAuthenticatedUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            return Optional.empty();
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username));
    }

    public User getCurrentUser() {
        return getAuthenticatedUserOptional()
                .orElseThrow(() -> new IllegalStateException("User is not authenticated"));
    }

    @Transactional
    public void deleteCurrentUserAccount() {
        User user = getCurrentUser();
        SecurityContextHolder.clearContext();
        userRepository.delete(user);
    }

    private void setSecurityContext(Authentication authentication, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        if (httpRequest != null && httpResponse != null) {
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
        }
    }
}

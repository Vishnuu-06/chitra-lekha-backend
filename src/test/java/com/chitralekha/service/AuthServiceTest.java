package com.chitralekha.service;

import com.chitralekha.dto.AuthRequest;
import com.chitralekha.dto.AuthResponse;
import com.chitralekha.entity.Profile;
import com.chitralekha.entity.User;
import com.chitralekha.repository.ProfileRepository;
import com.chitralekha.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                profileRepository,
                passwordEncoder,
                authenticationManager,
                securityContextRepository
        );
    }

    @Test
    void register_ShouldRejectDuplicateUsername() {
        AuthRequest request = new AuthRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.register(request, httpRequest, httpResponse)
        );

        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldRejectDuplicateEmail() {
        AuthRequest request = new AuthRequest();
        request.setUsername("newuser");
        request.setEmail("taken@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.register(request, httpRequest, httpResponse)
        );

        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldHashPasswordAndSaveProfile() {
        AuthRequest request = new AuthRequest();
        request.setUsername("cinephile");
        request.setEmail("cine@example.com");
        request.setPassword("secretPass12");

        when(userRepository.existsByUsername("cinephile")).thenReturn(false);
        when(userRepository.existsByEmail("cine@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secretPass12")).thenReturn("hashed-bcrypt-value");

        User savedUser = new User("cinephile", "cine@example.com", "hashed-bcrypt-value", true);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(profileRepository.save(any(Profile.class))).thenReturn(new Profile(savedUser, "cinephile", "", "", "", "", "", ""));

        AuthResponse response = authService.register(request, httpRequest, httpResponse);

        assertNotNull(response);
        assertEquals("cinephile", response.getUsername());
        assertEquals("cine@example.com", response.getEmail());
        verify(passwordEncoder).encode("secretPass12");
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
    }
}

package com.chitralekha.service;

import com.chitralekha.entity.Conversation;
import com.chitralekha.entity.User;
import com.chitralekha.integration.ContentNormalizationService;
import com.chitralekha.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserBlockRepository blockRepository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ContentListRepository listRepository;
    @Mock
    private NotificationRepository notificationRepository;

    private User currentUser;
    private AuthService testAuthService;
    private ContentNormalizationService testNormalizationService;
    private MessagingService messagingService;

    @BeforeEach
    void setUp() {
        currentUser = new User("alice", "alice@example.com", "pass", true);
        currentUser.setId(1L);

        testAuthService = new AuthService(null, null, null, null, null) {
            @Override
            public User getCurrentUser() {
                return currentUser;
            }
        };

        testNormalizationService = new ContentNormalizationService(null, null, null);

        messagingService = new MessagingService(
                conversationRepository,
                messageRepository,
                userRepository,
                profileRepository,
                blockRepository,
                contentRepository,
                reviewRepository,
                listRepository,
                notificationRepository,
                testNormalizationService,
                testAuthService
        );
    }

    @Test
    void getOrCreateConversation_ShouldRejectSelfMessaging() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(currentUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                messagingService.getOrCreateConversation("alice")
        );

        assertEquals("Cannot start a conversation with yourself", ex.getMessage());
    }

    @Test
    void getOrCreateConversation_ShouldRejectIfBlocked() {
        User bob = new User("bob", "bob@example.com", "pass", true);
        bob.setId(2L);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(blockRepository.isBlockedEitherWay(currentUser, bob)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                messagingService.getOrCreateConversation("bob")
        );

        assertTrue(ex.getMessage().contains("privacy and blocking"));
    }

    @Test
    void getConversationMessages_ShouldEnforceParticipantAuthorization() {
        User bob = new User("bob", "bob@example.com", "pass", true);
        bob.setId(2L);
        User charlie = new User("charlie", "charlie@example.com", "pass", true);
        charlie.setId(3L);

        // Conversation between bob and charlie (currentUser alice is not a participant)
        Conversation conv = new Conversation(bob, charlie);
        conv.setId(100L);

        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conv));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                messagingService.getConversationMessages(100L)
        );

        assertEquals("You are not authorized to view this conversation", ex.getMessage());
    }
}

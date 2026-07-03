package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import com.devdoyen.nemologic.repository.HistoryRepository;
import com.devdoyen.nemologic.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private HistoryRepository historyRepository;
    private StageRepository stageRepository;
    private StageService stageService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        historyRepository = mock(HistoryRepository.class);
        stageRepository = mock(StageRepository.class);
        stageService = mock(StageService.class);
        userService = new UserService(userRepository, historyRepository, stageRepository, stageService);
    }


    @Test
    public void testUserServiceAddXp() {
        User user = new User(1L, "Player1", 200, 2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.addXpToUser(1L, 120);
        assertEquals(3, updatedUser.getLevel());
        assertEquals(320, updatedUser.getXp());
    }

    @Test
    public void testInitialUserStats() {
        User user = new User(1L, "Alice", 0, 1);
        assertEquals(0, user.getXp());
        assertEquals(1, user.getLevel());
    }

    @Test
    public void testAddXpNoLevelUp() {
        User user = new User(1L, "Alice", 0, 1);
        user.addXp(50);
        assertEquals(50, user.getXp());
        assertEquals(1, user.getLevel());
    }

    @Test
    public void testAddXpSingleLevelUp() {
        User user = new User(1L, "Alice", 0, 1);
        user.addXp(120);
        assertEquals(2, user.getLevel());
        assertEquals(120, user.getXp());
    }

    @Test
    public void testAddXpMultipleLevelUps() {
        User user = new User(1L, "Alice", 0, 1);
        user.addXp(350);
        assertEquals(3, user.getLevel());
        assertEquals(350, user.getXp());
    }

    @Test
    public void testFindOrCreateByOauthId() {
        when(userRepository.findByOauthId("google-uid-123")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(4L);
            return u;
        });

        // 1. Create case
        User user = userService.findOrCreateByOauthId("google-uid-123", "John Doe", "john@example.com", "https://pic.url");
        assertNotNull(user);
        assertEquals(4L, user.getId());
        assertEquals("google-uid-123", user.getOauthId());
        assertEquals("John Doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("https://pic.url", user.getProfileImageUrl());

        // 2. Fetch/Update case
        when(userRepository.findByOauthId("google-uid-123")).thenReturn(Optional.of(user));
        User existingUser = userService.findOrCreateByOauthId("google-uid-123", "John Doe Changed", "john@example.com", "https://pic.url");
        assertEquals("John Doe Changed", existingUser.getUsername());
    }
}

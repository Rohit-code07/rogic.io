package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import com.devdoyen.nemologic.repository.HistoryRepository;
import com.devdoyen.nemologic.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devdoyen.nemologic.model.History;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.dto.GuestClearRequest;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
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

    @Test
    public void testSyncGuestHistorySuccessful() {
        User user = new User(1L, "Alice", 0, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage5 = new Stage(5L, "Easy Stage", 5, 5, new int[][]{{1}});
        Stage stage10 = new Stage(10L, "Hard Stage", 10, 10, new int[][]{{1}});
        when(stageRepository.findById(5L)).thenReturn(Optional.of(stage5));
        when(stageRepository.findById(10L)).thenReturn(Optional.of(stage10));

        when(historyRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<GuestClearRequest> clears = new ArrayList<>();
        clears.add(new GuestClearRequest(5L, 45));
        clears.add(new GuestClearRequest(10L, 120));

        User updatedUser = userService.syncGuestHistory(1L, clears);

        // Verify total XP earned: EASY(50) + HARD(200) = 250 XP
        assertEquals(250, updatedUser.getXp());
        // Verify stage records updated
        verify(stageService, times(1)).recordClear(5L, 45);
        verify(stageService, times(1)).recordClear(10L, 120);
        // Verify histories saved
        verify(historyRepository, times(2)).save(any(History.class));
    }

    @Test
    public void testSyncGuestHistoryIgnoresDuplicates() {
        User user = new User(1L, "Alice", 0, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage5 = new Stage(5L, "Easy Stage", 5, 5, new int[][]{{1}});
        when(stageRepository.findById(5L)).thenReturn(Optional.of(stage5));

        // User already cleared stage 5
        History existingHistory = new History(user, stage5, java.time.LocalDateTime.now(), 50, 45);
        when(historyRepository.findByUserId(1L)).thenReturn(Collections.singletonList(existingHistory));

        List<GuestClearRequest> clears = new ArrayList<>();
        clears.add(new GuestClearRequest(5L, 30)); // Duplicate clear of stage 5

        User updatedUser = userService.syncGuestHistory(1L, clears);

        // Verify no extra XP is awarded
        assertEquals(0, updatedUser.getXp());
        // Verify stage record and history are not saved
        verify(stageService, never()).recordClear(anyLong(), anyInt());
        verify(historyRepository, never()).save(any(History.class));
    }
}

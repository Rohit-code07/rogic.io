package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.dto.HistoryResponse;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/ranking")
    public List<User> getRanking() {
        return userService.getGlobalRanking();
    }

    @PostMapping("/{id}/clear")
    public User clearStage(
            @PathVariable Long id,
            @RequestParam String difficulty,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Integer elapsedTime,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }

        int xpReward;
        switch (difficulty.toUpperCase()) {
            case "EASY":
                xpReward = 50;
                break;
            case "NORMAL":
                xpReward = 100;
                break;
            case "HARD":
                xpReward = 200;
                break;
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
        return userService.clearStageWithHistory(id, stageId, xpReward, elapsedTime);
    }

    @GetMapping("/{id}/history")
    public List<HistoryResponse> getUserHistory(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        User targetUser = userService.getUserById(id);
        String sub = jwt.getClaimAsString("sub");
        if (targetUser.getOauthId() == null || !targetUser.getOauthId().equals(sub)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User ID does not match token identity");
        }
        return userService.getUserHistory(id);
    }
}

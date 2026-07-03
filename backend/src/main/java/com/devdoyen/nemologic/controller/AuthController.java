package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/me")
    public User getOrCreateMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        String oauthId = jwt.getClaimAsString("sub");
        String name = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("email");
        String picture = jwt.getClaimAsString("picture");

        // Cognito custom fields fallback or defaults
        if (name == null) {
            name = jwt.getClaimAsString("cognito:username");
        }

        return userService.findOrCreateByOauthId(oauthId, name, email, picture);
    }
}

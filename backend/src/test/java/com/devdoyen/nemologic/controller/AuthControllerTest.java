package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void getOrCreateMeShouldCreateNewUserIfNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/me")
                .with(jwt().jwt(builder -> builder
                        .claim("sub", "google-oauth-12345")
                        .claim("name", "John Doe")
                        .claim("email", "john@example.com")
                        .claim("picture", "https://example.com/john.png")
                )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.profileImageUrl", is("https://example.com/john.png")))
                .andExpect(jsonPath("$.oauthId", is("google-oauth-12345")))
                .andExpect(jsonPath("$.xp", is(0)))
                .andExpect(jsonPath("$.level", is(1)));
    }

    @Test
    public void getOrCreateMeShouldReturnExistingUserIfFound() throws Exception {
        // Save initial user
        User existing = new User(null, "John Old", 150, 2, "google-oauth-12345", "john@example.com", "https://example.com/john.png");
        userRepository.save(existing);

        mockMvc.perform(post("/api/auth/me")
                .with(jwt().jwt(builder -> builder
                        .claim("sub", "google-oauth-12345")
                        .claim("name", "John Doe") // Username gets updated
                        .claim("email", "john@example.com")
                        .claim("picture", "https://example.com/john.png")
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("John Doe"))) // Updated name
                .andExpect(jsonPath("$.xp", is(150))) // Retains XP
                .andExpect(jsonPath("$.level", is(2)));
    }

    @Test
    public void getOrCreateMeWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

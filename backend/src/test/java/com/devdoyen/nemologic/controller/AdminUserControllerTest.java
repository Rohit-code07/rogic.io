package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.devdoyen.nemologic.security.AdminSessionManager sessionManager;

    private String adminToken;

    @BeforeEach
    public void setUp() {
        adminToken = java.util.UUID.randomUUID().toString();
        sessionManager.registerToken(adminToken, "admin");

        // Seed some test users if db is empty
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "TestUser1", 100, 1, "test-oauth-1", "user1@example.com", null));
            userRepository.save(new User(null, "TestUser2", 250, 2, "test-oauth-2", "user2@example.com", null));
        }
    }

    @Test
    public void testGetAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    public void testGetAllUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}

package com.spotiapp.backend.controller;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.model.UserProfile;
import com.spotiapp.backend.repository.UserRepository;
import com.spotiapp.backend.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Test
    @WithMockUser
    void testGetPublicProfile_WithSpecialCharacters() throws Exception {
        // Arrange
        String username = "user.name_123";
        when(profileService.getFullProfile(username)).thenReturn(Map.of(
            "username", username,
            "displayName", "Test User"
        ));

        // Act & Assert
        mockMvc.perform(get("/api/profiles/" + username))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.displayName").value("Test User"));
    }

    @Test
    @WithMockUser
    void testGetPublicProfile_NotFound() throws Exception {
        // Arrange
        String username = "missing_user";
        when(profileService.getFullProfile(username)).thenThrow(new RuntimeException("Not found"));

        // Act & Assert
        mockMvc.perform(get("/api/profiles/" + username))
            .andExpect(status().isNotFound());
    }
}

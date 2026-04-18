package com.spotiapp.backend.controller;

import com.spotiapp.backend.security.JwtUtil;
import com.spotiapp.backend.service.SpotifyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SpotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.spotiapp.backend.repository.UserRepository userRepository;

    @MockBean
    private SpotifyService spotifyService;

    @Test
    void shouldBlockAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/spotify/playlists"))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnPlaylistsWithValidToken() throws Exception {
        // Arrange
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("spotify_testuser", "spot@test.com", "pass");
        userRepository.save(testUser);

        String token = jwtUtil.generateToken("spotify_testuser");
        Cookie authCookie = new Cookie("auth-token", token);


        Map<String, Object> mockPlaylists = Map.of(
                "items", new Object[] { Map.of("name", "Test Playlist") }
        );
        when(spotifyService.getUserPlaylists("spotify_testuser")).thenReturn(mockPlaylists);

        // Act & Assert
        mockMvc.perform(get("/api/spotify/playlists").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Test Playlist"));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnPlaylistTracks() throws Exception {
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("s_user2", "s2@test.com", "pass");
        userRepository.save(testUser);
        Cookie authCookie = new Cookie("auth-token", jwtUtil.generateToken("s_user2"));

        Map<String, Object> mockTracks = Map.of("items", new Object[] { Map.of("track", Map.of("name", "Song A")) });
        when(spotifyService.getPlaylistTracks("s_user2", "playlist123")).thenReturn(mockTracks);

        mockMvc.perform(get("/api/spotify/playlists/playlist123/tracks").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].track.name").value("Song A"));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnStatus() throws Exception {
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("s_user3", "s3@test.com", "pass");
        userRepository.save(testUser);
        Cookie authCookie = new Cookie("auth-token", jwtUtil.generateToken("s_user3"));

        when(spotifyService.getStatus("s_user3")).thenReturn(Map.of("product", "premium"));

        mockMvc.perform(get("/api/spotify/status").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product").value("premium"));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnToken() throws Exception {
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("s_user4", "s4@test.com", "pass");
        userRepository.save(testUser);
        Cookie authCookie = new Cookie("auth-token", jwtUtil.generateToken("s_user4"));

        when(spotifyService.getToken("s_user4")).thenReturn("real_token_123");

        mockMvc.perform(get("/api/spotify/token").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("real_token_123"));
    }
}

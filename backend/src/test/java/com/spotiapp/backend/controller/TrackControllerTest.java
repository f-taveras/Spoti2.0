package com.spotiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotiapp.backend.model.Track;
import com.spotiapp.backend.repository.TrackRepository;
import com.spotiapp.backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TrackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.spotiapp.backend.repository.UserRepository userRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        trackRepository.deleteAll();
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldCacheTrackSuccessfully() throws Exception {
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("track_user", "track@test.com", "pass");
        userRepository.save(testUser);
        Cookie authCookie = new Cookie("auth-token", jwtUtil.generateToken("track_user"));

        Track track = new Track("spotify_123", "Cool Song", "Cool Artist", "http://image.url");
        String json = objectMapper.writeValueAsString(track);

        mockMvc.perform(post("/api/tracks/cache")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotifyId").value("spotify_123"))
                .andExpect(jsonPath("$.name").value("Cool Song"));
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void shouldReturnBadRequestForMissingSpotifyId() throws Exception {
        com.spotiapp.backend.model.User testUser = new com.spotiapp.backend.model.User("track_user2", "track2@test.com", "pass");
        userRepository.save(testUser);
        Cookie authCookie = new Cookie("auth-token", jwtUtil.generateToken("track_user2"));

        Track track = new Track("", "Cool Song", "Cool Artist", "http://image.url");
        String json = objectMapper.writeValueAsString(track);

        mockMvc.perform(post("/api/tracks/cache")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}

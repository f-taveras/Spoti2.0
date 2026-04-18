package com.spotiapp.backend.service;

import com.spotiapp.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifyServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @InjectMocks
    private SpotifyService spotifyService;

    private User validSpotifyUser;

    @BeforeEach
    void setUp() {
        validSpotifyUser = new User();
        validSpotifyUser.setUsername("testuser");
        validSpotifyUser.setSpotifyAccessToken("valid-token");
    }

    @Test
    void shouldFetchPlaylistsWhenTokenIsValid() {
        // Arrange
        String url = "https://api.spotify.com/v1/me/playlists";
        Map<String, Object> mockResponse = Map.of(
                "items", new Object[] {
                        Map.of("id", "1", "name", "My Playlist")
                }
        );

        when(userService.findByUsername("testuser")).thenReturn(validSpotifyUser);
        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        Map<String, Object> result = spotifyService.getUserPlaylists("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("items"));
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoSpotifyToken() {
        // Arrange
        validSpotifyUser.setSpotifyAccessToken(null);
        when(userService.findByUsername("testuser")).thenReturn(validSpotifyUser);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            spotifyService.getUserPlaylists("testuser");
        });
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldFetchPlaylistTracksWhenTokenIsValid() {
        String url = "https://api.spotify.com/v1/playlists/play123/tracks";
        Map<String, Object> mockResponse = Map.of("items", new Object[]{});
        when(userService.findByUsername("testuser")).thenReturn(validSpotifyUser);
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, Object> result = spotifyService.getPlaylistTracks("testuser", "play123");
        assertNotNull(result);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    }

    @Test
    void shouldFetchStatusWhenTokenIsValid() {
        String url = "https://api.spotify.com/v1/me";
        Map<String, Object> mockResponse = Map.of("product", "premium");
        when(userService.findByUsername("testuser")).thenReturn(validSpotifyUser);
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        Map<String, Object> result = spotifyService.getStatus("testuser");
        assertEquals("premium", result.get("product"));
    }

    @Test
    void shouldReturnTokenWhenExists() {
        when(userService.findByUsername("testuser")).thenReturn(validSpotifyUser);
        String token = spotifyService.getToken("testuser");
        assertEquals("valid-token", token);
    }
}

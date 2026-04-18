package com.spotiapp.backend.service;

import com.spotiapp.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SpotifyService {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> getUserPlaylists(String username) {
        return fetchFromSpotify("https://api.spotify.com/v1/me/playlists", username);
    }

    public Map<String, Object> getPlaylistTracks(String username, String playlistId) {
        return fetchFromSpotify("https://api.spotify.com/v1/playlists/" + playlistId + "/items", username);
    }

    public Map<String, Object> getStatus(String username) {
        return fetchFromSpotify("https://api.spotify.com/v1/me", username);
    }

    public String getToken(String username) {
        User user = userService.findByUsername(username);
        String token = user.getSpotifyAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("User does not have a connected Spotify account");
        }
        return token;
    }

    // --- Private Helper ---
    private Map<String, Object> fetchFromSpotify(String url, String username) {
        User user = userService.findByUsername(username);

        String token = user.getSpotifyAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("User does not have a connected Spotify account");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return response.getBody();
    }
}

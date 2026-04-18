package com.spotiapp.backend.controller;

import com.spotiapp.backend.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/playlists")
    public ResponseEntity<?> getPlaylists(Authentication authentication) {
        String username = authentication.getName();
        try {
            Map<String, Object> playlists = spotifyService.getUserPlaylists(username);
            return ResponseEntity.ok(playlists);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/playlists/{id}/tracks")
    public ResponseEntity<?> getPlaylistTracks(@org.springframework.web.bind.annotation.PathVariable String id, Authentication authentication) {
        String username = authentication.getName();
        try {
            Map<String, Object> tracks = spotifyService.getPlaylistTracks(username, id);
            return ResponseEntity.ok(tracks);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(Authentication authentication) {
        String username = authentication.getName();
        try {
            Map<String, Object> status = spotifyService.getStatus(username);
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken(Authentication authentication) {
        String username = authentication.getName();
        try {
            String token = spotifyService.getToken(username);
            return ResponseEntity.ok(Map.of("access_token", token));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

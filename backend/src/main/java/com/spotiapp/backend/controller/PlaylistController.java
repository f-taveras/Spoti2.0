package com.spotiapp.backend.controller;

import com.spotiapp.backend.model.CachedPlaylist;
import com.spotiapp.backend.repository.CachedPlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private CachedPlaylistRepository cachedPlaylistRepository;

    @PostMapping("/cache")
    public ResponseEntity<?> cachePlaylist(@RequestBody CachedPlaylist cacheRequest) {
        if (cacheRequest.getSpotifyId() == null || cacheRequest.getSpotifyId().isBlank()) {
            return ResponseEntity.badRequest().body("Spotify ID is required");
        }

        Optional<CachedPlaylist> existing = cachedPlaylistRepository.findBySpotifyId(cacheRequest.getSpotifyId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get()); // Already cached
        }

        CachedPlaylist saved = cachedPlaylistRepository.save(cacheRequest);
        return ResponseEntity.ok(saved);
    }
}

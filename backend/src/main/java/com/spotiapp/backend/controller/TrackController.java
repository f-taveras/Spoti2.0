package com.spotiapp.backend.controller;

import com.spotiapp.backend.model.Track;
import com.spotiapp.backend.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackRepository trackRepository;

    @PostMapping("/cache")
    public ResponseEntity<?> cacheTrack(@RequestBody Track cacheRequest) {
        if (cacheRequest.getSpotifyId() == null || cacheRequest.getSpotifyId().isBlank()) {
            return ResponseEntity.badRequest().body("Spotify ID is required");
        }

        Optional<Track> existing = trackRepository.findBySpotifyId(cacheRequest.getSpotifyId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get()); // Already cached
        }

        Track saved = trackRepository.save(cacheRequest);
        return ResponseEntity.ok(saved);
    }
}

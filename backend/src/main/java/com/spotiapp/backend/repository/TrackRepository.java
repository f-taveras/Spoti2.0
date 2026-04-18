package com.spotiapp.backend.repository;

import com.spotiapp.backend.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findBySpotifyId(String spotifyId);
}

package com.spotiapp.backend.repository;

import com.spotiapp.backend.model.CachedPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CachedPlaylistRepository extends JpaRepository<CachedPlaylist, Long> {
    Optional<CachedPlaylist> findBySpotifyId(String spotifyId);
}

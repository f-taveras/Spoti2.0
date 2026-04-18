package com.spotiapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cached_playlists")
public class CachedPlaylist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String spotifyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ownerName;

    @Column(length = 1000)
    private String coverArtUrl;

    public CachedPlaylist() {}

    public CachedPlaylist(String spotifyId, String name, String ownerName, String coverArtUrl) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.ownerName = ownerName;
        this.coverArtUrl = coverArtUrl;
    }

    public Long getId() { return id; }
    public String getSpotifyId() { return spotifyId; }
    public String getName() { return name; }
    public String getOwnerName() { return ownerName; }
    public String getCoverArtUrl() { return coverArtUrl; }
}

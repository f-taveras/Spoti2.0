package com.spotiapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String spotifyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String artistName;

    @Column(length = 1000)
    private String albumArtUrl;

    public Track() {
    }

    public Track(String spotifyId, String name, String artistName, String albumArtUrl) {
        this.spotifyId = spotifyId;
        this.name = name;
        this.artistName = artistName;
        this.albumArtUrl = albumArtUrl;
    }

    public Long getId() {
        return id;
    }

    public String getSpotifyId() {
        return spotifyId;
    }
    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }
    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }
}

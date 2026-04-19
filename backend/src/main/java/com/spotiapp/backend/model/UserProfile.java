package com.spotiapp.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long id; // Same as User ID

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String displayName;

    @Column(length = 1000)
    private String bio;

    private String location;

    private String avatarUrl;

    @ElementCollection
    @CollectionTable(name = "user_genres", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "genre")
    private List<String> favoriteGenres = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_artists", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "artist")
    private List<String> favoriteArtists = new ArrayList<>();

    // ── Constructors ───────────────────────────────────────────────────────────

    public UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public List<String> getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(List<String> favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public List<String> getFavoriteArtists() { return favoriteArtists; }
    public void setFavoriteArtists(List<String> favoriteArtists) { this.favoriteArtists = favoriteArtists; }
}

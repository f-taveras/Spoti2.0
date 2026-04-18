package com.spotiapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)          // nullable — Spotify users may not expose their email
    private String email;

    @Column(nullable = true)        // nullable — Spotify users have no password
    private String password;

    @Column(unique = true)          // nullable — only set for Spotify users
    private String spotifyId;

    @Column(length = 2048)          // tokens can be large
    private String spotifyAccessToken;

    @Column(length = 2048)
    private String spotifyRefreshToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(nullable = false)
    private String role = "USER";

    // ── Constructors ───────────────────────────────────────────────────────────

    public User() {}

    /** Used when registering with username + password (LOCAL). */
    public User(String username, String email, String password) {
        this.username     = username;
        this.email        = email;
        this.password     = password;
        this.authProvider = AuthProvider.LOCAL;
        this.role         = "USER";
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId()                       { return id; }

    public String getUsername()               { return username; }
    public void   setUsername(String u)       { this.username = u; }

    public String getEmail()                  { return email; }
    public void   setEmail(String e)          { this.email = e; }

    public String getPassword()               { return password; }
    public void   setPassword(String p)       { this.password = p; }

    public String getSpotifyId()              { return spotifyId; }
    public void   setSpotifyId(String id)     { this.spotifyId = id; }

    public String getSpotifyAccessToken()             { return spotifyAccessToken; }
    public void   setSpotifyAccessToken(String token) { this.spotifyAccessToken = token; }

    public String getSpotifyRefreshToken()             { return spotifyRefreshToken; }
    public void   setSpotifyRefreshToken(String token) { this.spotifyRefreshToken = token; }

    public AuthProvider getAuthProvider()                    { return authProvider; }
    public void         setAuthProvider(AuthProvider ap)     { this.authProvider = ap; }

    public String getRole()                   { return role; }
    public void   setRole(String r)           { this.role = r; }
}

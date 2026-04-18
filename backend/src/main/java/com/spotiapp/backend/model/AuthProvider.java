package com.spotiapp.backend.model;

/**
 * Tracks how a user account was created.
 * LOCAL  — registered with username + password.
 * SPOTIFY — created via Spotify OAuth2 login.
 */
public enum AuthProvider {
    LOCAL,
    SPOTIFY
}

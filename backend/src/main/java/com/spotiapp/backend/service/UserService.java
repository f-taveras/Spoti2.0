package com.spotiapp.backend.service;

import com.spotiapp.backend.dto.RegisterRequest;
import com.spotiapp.backend.model.AuthProvider;
import com.spotiapp.backend.model.User;
import com.spotiapp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired private UserRepository  userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Spring Security ────────────────────────────────────────────────────────

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                // Spotify users have no password — return "" to avoid NPE
                user.getPassword() != null ? user.getPassword() : "",
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    // ── Local auth ─────────────────────────────────────────────────────────────

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        // Initialize Profile and Reputation
        user.setProfile(new com.spotiapp.backend.model.UserProfile(user));
        user.setReputation(new com.spotiapp.backend.model.UserReputation(user));

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ── Spotify OAuth2 ─────────────────────────────────────────────────────────

    /**
     * Find an existing user by Spotify ID, link Spotify to an existing local account
     * if the email matches, or create a brand-new Spotify-only user.
     */
    public User findOrCreateSpotifyUser(String spotifyId, String displayName, String email, String profileImageUrl) {

        // 1. Already have a Spotify-linked account?
        Optional<User> bySpotifyId = userRepository.findBySpotifyId(spotifyId);
        if (bySpotifyId.isPresent()) {
            User existing = bySpotifyId.get();
            // Update profile info if changed
            if (existing.getProfile() != null) {
                existing.getProfile().setAvatarUrl(profileImageUrl);
                existing.getProfile().setDisplayName(displayName);
            }
            return userRepository.save(existing);
        }

        // 2. Existing local account with same email → link it
        if (email != null && !email.isBlank()) {
            Optional<User> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                User existing = byEmail.get();
                existing.setSpotifyId(spotifyId);
                if (existing.getProfile() != null) {
                    existing.getProfile().setAvatarUrl(profileImageUrl);
                    existing.getProfile().setDisplayName(displayName);
                }
                return userRepository.save(existing);
            }
        }

        // 3. Brand new user — derive a unique username from the Spotify display name
        String username = generateUniqueUsername(displayName, spotifyId);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setSpotifyId(spotifyId);
        user.setAuthProvider(AuthProvider.SPOTIFY);
        user.setRole("USER");

        // Initialize Profile and Reputation
        com.spotiapp.backend.model.UserProfile profile = new com.spotiapp.backend.model.UserProfile(user);
        profile.setDisplayName(displayName);
        profile.setAvatarUrl(profileImageUrl);
        user.setProfile(profile);

        user.setReputation(new com.spotiapp.backend.model.UserReputation(user));

        return userRepository.save(user);
    }

    public User findBySpotifyId(String spotifyId) {
        return userRepository.findBySpotifyId(spotifyId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found for Spotify ID: " + spotifyId));
    }

    public void updateSpotifyTokens(String spotifyId, String accessToken, String refreshToken) {
        User user = findBySpotifyId(spotifyId);
        user.setSpotifyAccessToken(accessToken);
        if (refreshToken != null) {
            user.setSpotifyRefreshToken(refreshToken);
        }
        userRepository.save(user);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Converts a Spotify display name to a valid, unique username.
     * Falls back progressively: displayName → displayName_partOfId → spotifyId
     */
    private String generateUniqueUsername(String displayName, String spotifyId) {
        String base = (displayName != null && !displayName.isBlank())
                ? displayName.trim().toLowerCase().replaceAll("[^a-z0-9]", "_").replaceAll("_+", "_")
                : "spotify_user";

        if (!userRepository.existsByUsername(base)) return base;

        String withId = base + "_" + spotifyId.substring(0, Math.min(6, spotifyId.length()));
        if (!userRepository.existsByUsername(withId)) return withId;

        // Final guaranteed-unique fallback
        return spotifyId;
    }
}

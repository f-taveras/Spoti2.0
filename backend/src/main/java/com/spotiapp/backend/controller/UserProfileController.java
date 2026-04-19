package com.spotiapp.backend.controller;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.model.UserProfile;
import com.spotiapp.backend.repository.UserProfileRepository;
import com.spotiapp.backend.repository.UserRepository;
import com.spotiapp.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    @Autowired private ProfileService profileService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    @GetMapping("/{username}")
    public ResponseEntity<?> getPublicProfile(@PathVariable String username) {
        try {
            return ResponseEntity.ok(profileService.getFullProfile(username));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        UserProfile profile = user.getProfile();
        
        if (profile == null) {
            profile = new UserProfile(user);
            user.setProfile(profile);
        }

        if (updates.containsKey("displayName")) profile.setDisplayName((String) updates.get("displayName"));
        if (updates.containsKey("bio")) profile.setBio((String) updates.get("bio"));
        if (updates.containsKey("location")) profile.setLocation((String) updates.get("location"));
        if (updates.containsKey("avatarUrl")) profile.setAvatarUrl((String) updates.get("avatarUrl"));
        
        if (updates.containsKey("favoriteGenres")) {
            profile.setFavoriteGenres((java.util.List<String>) updates.get("favoriteGenres"));
        }
        if (updates.containsKey("favoriteArtists")) {
            profile.setFavoriteArtists((java.util.List<String>) updates.get("favoriteArtists"));
        }

        userProfileRepository.save(profile);
        return ResponseEntity.ok(profileService.getFullProfile(username));
    }
}

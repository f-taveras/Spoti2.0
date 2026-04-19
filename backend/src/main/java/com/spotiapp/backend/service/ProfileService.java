package com.spotiapp.backend.service;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.repository.FollowerRepository;
import com.spotiapp.backend.repository.PostRepository;
import com.spotiapp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileService {

    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private FollowerRepository followerRepository;

    public Map<String, Object> getFullProfile(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        
        if (user.getProfile() != null) {
            profile.put("displayName", user.getProfile().getDisplayName());
            profile.put("bio", user.getProfile().getBio());
            profile.put("location", user.getProfile().getLocation());
            profile.put("avatarUrl", user.getProfile().getAvatarUrl());
            profile.put("favoriteGenres", user.getProfile().getFavoriteGenres());
            profile.put("favoriteArtists", user.getProfile().getFavoriteArtists());
        }

        if (user.getReputation() != null) {
            profile.put("tasteScore", user.getReputation().getTasteScore());
            profile.put("curatorScore", user.getReputation().getCuratorScore());
            profile.put("signalScore", user.getReputation().getSignalScore());
        }

        // Stats (Computed)
        profile.put("postCount", postRepository.countByUser(user));
        profile.put("followersCount", followerRepository.countByFollowing(user));
        profile.put("followingCount", followerRepository.countByFollower(user));

        return profile;
    }
}

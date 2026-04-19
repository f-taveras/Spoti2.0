package com.spotiapp.backend.controller;

import com.spotiapp.backend.model.Follower;
import com.spotiapp.backend.model.User;
import com.spotiapp.backend.repository.FollowerRepository;
import com.spotiapp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired private UserRepository userRepository;
    @Autowired private FollowerRepository followerRepository;

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> toggleFollow(@PathVariable String username, Authentication authentication) {
        String followerName = authentication.getName();
        if (followerName.equals(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot follow yourself"));
        }

        User followerUser = userRepository.findByUsername(followerName).orElseThrow();
        User followedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Follower> existing = followerRepository.findByFollowerAndFollowing(followerUser, followedUser);

        if (existing.isPresent()) {
            followerRepository.delete(existing.get());
            return ResponseEntity.ok(Map.of("message", "Unfollowed", "following", false));
        } else {
            Follower f = new Follower(followerUser, followedUser);
            followerRepository.save(f);
            return ResponseEntity.ok(Map.of("message", "Followed", "following", true));
        }
    }

    @GetMapping("/is-following/{username}")
    public ResponseEntity<?> isFollowing(@PathVariable String username, Authentication authentication) {
        String followerName = authentication.getName();
        User followerUser = userRepository.findByUsername(followerName).orElseThrow();
        User followedUser = userRepository.findByUsername(username).orElse(null);
        
        if (followedUser == null) return ResponseEntity.ok(Map.of("following", false));

        boolean following = followerRepository.findByFollowerAndFollowing(followerUser, followedUser).isPresent();
        return ResponseEntity.ok(Map.of("following", following));
    }
}

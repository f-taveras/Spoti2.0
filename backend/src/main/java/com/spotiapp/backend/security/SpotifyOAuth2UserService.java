package com.spotiapp.backend.security;

import com.spotiapp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Called by Spring Security after successfully exchanging the Spotify
 * authorization code for an access token. Responsible for finding or
 * creating a local User from the Spotify profile attributes.
 */
@Service
public class SpotifyOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch the Spotify user profile (calls GET https://api.spotify.com/v1/me)
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attrs = oAuth2User.getAttributes();
        String spotifyId    = (String) attrs.get("id");
        String displayName  = (String) attrs.get("display_name");
        String email        = (String) attrs.get("email");

        // Extract profile image URL
        String profileImageUrl = null;
        Object imagesObj = attrs.get("images");
        if (imagesObj instanceof java.util.List) {
            java.util.List<?> images = (java.util.List<?>) imagesObj;
            if (!images.isEmpty()) {
                Object firstImage = images.get(0);
                if (firstImage instanceof Map) {
                    profileImageUrl = (String) ((Map<?, ?>) firstImage).get("url");
                }
            }
        }

        // Ensure the user exists in our database with their latest profile info
        userService.findOrCreateSpotifyUser(spotifyId, displayName, email, profileImageUrl);

        return oAuth2User;
    }
}

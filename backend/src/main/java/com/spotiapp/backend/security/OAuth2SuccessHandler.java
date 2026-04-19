package com.spotiapp.backend.security;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Invoked by Spring Security after a successful Spotify OAuth2 login.
 * Issues our own JWT as an HttpOnly cookie, then redirects the browser
 * back to the React frontend home page.
 */
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired private JwtUtil                       jwtUtil;
    @Autowired private UserService                   userService;
    @Autowired private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String spotifyId = (String) attrs.get("id");

        // Retrieve the local User we created in SpotifyOAuth2UserService
        User user = userService.findBySpotifyId(spotifyId);

        // Capture Spotify tokens to save to db
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName());

        if (client != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;
            userService.updateSpotifyTokens(spotifyId, accessToken, refreshToken);
        }

        // Generate our own JWT and set it as an HttpOnly cookie
        String token = jwtUtil.generateToken(user.getUsername());

        Cookie cookie = new Cookie("auth-token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86_400); // 24 hours
        // cookie.setSecure(true); // Enable in production with HTTPS
        response.addCookie(cookie);

        // Dynamically redirect to whichever host initiated the login
        // Send the browser to the React home page
        response.sendRedirect(frontendUrl + "/home");
    }
}

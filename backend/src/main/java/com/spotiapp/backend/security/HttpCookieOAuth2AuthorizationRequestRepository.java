package com.spotiapp.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Stores the OAuth2 authorization request (which contains the CSRF state parameter)
 * in an HttpOnly cookie instead of the HTTP session. This keeps our architecture
 * fully stateless while still safely completing the OAuth2 exchange.
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String COOKIE_NAME    = "oauth2_auth_request";
    private static final int   COOKIE_MAX_AGE = 180; // 3 minutes — enough to complete Spotify login

    // ── AuthorizationRequestRepository contract ────────────────────────────────

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return findCookie(request, COOKIE_NAME)
                .map(c -> deserialize(c.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, COOKIE_NAME);
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, serialize(authorizationRequest));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest saved = loadAuthorizationRequest(request);
        deleteCookie(request, response, COOKIE_NAME);
        return saved;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Optional<Cookie> findCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        if (request.getCookies() == null) return;
        Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .forEach(c -> {
                    c.setValue("");
                    c.setPath("/");
                    c.setMaxAge(0);
                    response.addCookie(c);
                });
    }

    @SuppressWarnings("deprecation")
    private String serialize(OAuth2AuthorizationRequest request) {
        // OAuth2AuthorizationRequest implements Serializable — safe to use Java serialization here.
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(request));
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    private OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(value));
    }
}

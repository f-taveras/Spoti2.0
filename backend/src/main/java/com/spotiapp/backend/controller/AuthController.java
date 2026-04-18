package com.spotiapp.backend.controller;

import com.spotiapp.backend.dto.AuthResponse;
import com.spotiapp.backend.dto.LoginRequest;
import com.spotiapp.backend.dto.RegisterRequest;
import com.spotiapp.backend.model.User;
import com.spotiapp.backend.security.JwtUtil;
import com.spotiapp.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;

    // ── Register ───────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        User user  = userService.register(request);
        String token = jwtUtil.generateToken(user.getUsername());
        addAuthCookie(response, token);

        return ResponseEntity.ok(
                new AuthResponse(user.getUsername(), user.getEmail(), "Registration successful"));
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails principal = (UserDetails) auth.getPrincipal();
        User user = userService.findByUsername(principal.getUsername());
        String token = jwtUtil.generateToken(principal.getUsername());
        addAuthCookie(response, token);

        return ResponseEntity.ok(
                new AuthResponse(user.getUsername(), user.getEmail(), "Login successful"));
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("auth-token", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    // ── Current user ───────────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(
                new AuthResponse(user.getUsername(), user.getEmail(), "Authenticated"));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private void addAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("auth-token", token);
        cookie.setHttpOnly(true);           // Not accessible via JS (XSS protection)
        cookie.setPath("/");                // Sent on every request
        cookie.setMaxAge(86_400);           // 24 hours
        // cookie.setSecure(true);          // Uncomment when running HTTPS in production
        response.addCookie(cookie);
    }
}

package com.spotiapp.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotiapp.backend.dto.LoginRequest;
import com.spotiapp.backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the auth endpoints using the real Spring context
 * and an H2 in-memory database. Each test class run gets a fresh context
 * via @DirtiesContext to avoid stale data between runs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Test 1: Successful registration ───────────────────────────────────────

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testregister");
        request.setEmail("testregister@spoti.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testregister"))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(cookie().exists("auth-token"))
                .andExpect(cookie().httpOnly("auth-token", true));
    }

    // ── Test 2: Login with valid credentials ──────────────────────────────────

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Register first so the user exists in H2
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("testlogin");
        reg.setEmail("testlogin@spoti.com");
        reg.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Now attempt login
        LoginRequest login = new LoginRequest();
        login.setUsername("testlogin");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(cookie().exists("auth-token"));
    }

    // ── Test 3: Protected route is blocked without a token ────────────────────
    // Spring Security returns 403 Forbidden (Access Denied) when there is
    // no authentication at all — 401 is only for failed authentication attempts.

    @Test
    void shouldBlockProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isForbidden());
    }
}

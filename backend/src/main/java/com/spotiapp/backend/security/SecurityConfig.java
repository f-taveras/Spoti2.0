package com.spotiapp.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired private JwtAuthFilter                                    jwtAuthFilter;
    @Autowired private SpotifyOAuth2UserService                         spotifyOAuth2UserService;
    @Autowired private OAuth2SuccessHandler                             oAuth2SuccessHandler;
    @Autowired private HttpCookieOAuth2AuthorizationRequestRepository   cookieAuthRepo;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .authorizeHttpRequests(auth -> auth
                // ── Public endpoints ───────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/profiles/**").permitAll()

                // ── Everything else requires authentication ────────────────────
                .anyRequest().authenticated()
            )

            // Stateless — JWT cookie handles every authenticated request
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Return 403 for unauthenticated API calls instead of redirecting to OAuth2 login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiAuthEntryPoint())
            )

            // Allow H2 console iframes in dev
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // ── Spotify OAuth2 login ───────────────────────────────────────────
            .oauth2Login(oauth2 -> oauth2
                // Store the OAuth2 state in a cookie (keeps us stateless)
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestRepository(cookieAuthRepo)
                )
                // Our custom service loads/creates the user from Spotify profile
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(spotifyOAuth2UserService)
                )
                // After success: set JWT cookie → redirect to React frontend
                .successHandler(oAuth2SuccessHandler)
                // If authentication fails (e.g., user cancels or is not whitelisted by dev), return to frontend
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect(frontendUrl + "?error=access_denied");
                })
            )

            // ── JWT filter for all other requests ──────────────────────────────
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://127.0.0.1:5173",
            "http://localhost:5173",
            "http://192.168.1.131:5173",
            frontendUrl
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Returns 403 Forbidden for unauthenticated requests to /api/** instead of
     * redirecting to the OAuth2 login page (which is the Spring Security default
     * when oauth2Login is configured).
     */
    @Bean
    public AuthenticationEntryPoint apiAuthEntryPoint() {
        return (request, response, authException) -> {
            System.err.println("API AUTH FAILURE: " + authException.getMessage() + " for path: " + request.getRequestURI());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Access denied\"}");
        };
    }
}

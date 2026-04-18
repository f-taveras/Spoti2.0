package com.spotiapp.backend.dto;

public class AuthResponse {

    private final String username;
    private final String email;
    private final String message;

    public AuthResponse(String username, String email, String message) {
        this.username = username;
        this.email    = email;
        this.message  = message;
    }

    public String getUsername() { return username; }
    public String getEmail()    { return email; }
    public String getMessage()  { return message; }
}

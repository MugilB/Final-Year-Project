package com.securevoting.dto;

import java.util.List;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username; // This is now voter_id (kept as username for API compatibility)
    private String email;
    private List<String> roles;

    // FIX: This constructor now matches the call in AuthController.
    // It no longer requires the 'id'.
    public AuthResponse(String accessToken, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    // Getters and Setters

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
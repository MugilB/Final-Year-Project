package com.securevoting.dto;

import javax.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    private String username; // This is now voter_id (kept as username for API compatibility)

    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
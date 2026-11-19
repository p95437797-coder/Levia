package com.levia.social.data.model;

public class RegisterRequest {
    public final String email;
    public final String username;
    public final String displayName;
    public final String password;

    public RegisterRequest(String email, String username, String displayName, String password) {
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.password = password;
    }
}

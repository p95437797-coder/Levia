package com.levia.social.data.model;

public class ResetPasswordRequest {
    public String token;
    public String newPassword;
    public ResetPasswordRequest(String token, String newPassword) { this.token = token; this.newPassword = newPassword; }
}

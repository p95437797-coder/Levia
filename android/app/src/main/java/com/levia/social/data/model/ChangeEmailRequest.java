package com.levia.social.data.model;

public class ChangeEmailRequest {
    public String password;
    public String newEmail;
    public ChangeEmailRequest(String password, String newEmail) { this.password = password; this.newEmail = newEmail; }
}

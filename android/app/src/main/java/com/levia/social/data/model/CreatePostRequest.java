package com.levia.social.data.model;
public class CreatePostRequest {
    public String body, category;
    public boolean commentsEnabled = true;
    public CreatePostRequest(String body, String category) { this.body = body; this.category = category; }
}

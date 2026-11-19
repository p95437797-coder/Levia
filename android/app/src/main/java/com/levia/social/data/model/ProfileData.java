package com.levia.social.data.model;
public class ProfileData {
    public String id, username, displayName, bio, avatarUrl, createdAt;
    public boolean isPrivate, isVerifiedCreator;
    public Counts counts;
    public Relationship relationship;
    public boolean canViewPosts;
    public static class Counts { public int posts, followers, following; }
    public static class Relationship { public boolean isSelf, isFollowing, hasRequested, followsYou; }
}

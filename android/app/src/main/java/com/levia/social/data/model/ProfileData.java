package com.levia.social.data.model;

public class ProfileData {
    public String id, username, displayName, bio, avatarUrl, createdAt;
    public boolean isPrivate, isVerifiedCreator, canViewPosts;
    public Counts counts;
    public Relationship relationship;
    public static class Counts { public int posts, followers, following; }
    public static class Relationship {
        public boolean isSelf, isFollowing, hasRequested, followsYou;
    }
}

package com.levia.social.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.levia.social.R;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.Comment;
import com.levia.social.data.model.CommentPage;
import com.levia.social.data.model.CommentRequest;
import com.levia.social.data.model.ConnectionPage;
import com.levia.social.data.model.ConnectionUser;
import com.levia.social.data.model.CreatePostRequest;
import com.levia.social.data.model.LikeResult;
import com.levia.social.data.model.MeData;
import com.levia.social.data.model.Post;
import com.levia.social.data.model.PostPage;
import com.levia.social.data.model.ProfileData;
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.data.model.User;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** LEVIA community home. Authentication and session refresh remain handled by ApiClient. */
public class MainActivity extends AppCompatActivity {
    private static final int BG = Color.rgb(11, 9, 16);
    private static final int SURFACE = Color.rgb(24, 19, 33);
    private static final int SURFACE_2 = Color.rgb(34, 26, 47);
    private static final int PURPLE = Color.rgb(139, 92, 246);
    private static final int LAVENDER = Color.rgb(205, 185, 255);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(164, 154, 180);

    private LinearLayout content, bottomNav;
    private TokenStore store;
    private String activeTab = "Home";
    private String currentUsername = "";
    private ProfileData myProfile;
    private final List<Post> visiblePosts = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = new TokenStore(this);
        content = findViewById(R.id.contentContainer);
        bottomNav = findViewById(R.id.bottomNavigation);
        findViewById(R.id.securityButton).setOnClickListener(v -> startActivity(new Intent(this, AccountSecurityActivity.class)));
        findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
        buildBottomNav();
        loadMeThenHome();
    }

    private void loadMeThenHome() {
        ApiClient.get(this).me().enqueue(new Callback<ApiResponse<MeData>>() {
            @Override public void onResponse(Call<ApiResponse<MeData>> call, Response<ApiResponse<MeData>> r) {
                if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null && r.body().data.user != null) {
                    currentUsername = r.body().data.user.username == null ? "" : r.body().data.user.username;
                    showHome();
                } else if (r.code() == 401) goToLogin();
                else { toast(ApiErrorParser.message(r)); showHome(); }
            }
            @Override public void onFailure(Call<ApiResponse<MeData>> call, Throwable t) {
                toast(ApiErrorParser.networkMessage(t)); showHome();
            }
        });
    }

    private void buildBottomNav() {
        bottomNav.removeAllViews();
        String[][] items = {{"Home", "⌂"}, {"Search", "⌕"}, {"Create", "+"}, {"Profile", "◉"}};
        for (String[] item : items) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(4, 6, 4, 4);
            TextView icon = new TextView(this);
            icon.setText(item[1]); icon.setTextSize(21); icon.setGravity(Gravity.CENTER);
            icon.setTextColor(activeTab.equals(item[0]) ? LAVENDER : MUTED);
            TextView label = new TextView(this);
            label.setText(item[0]); label.setTextSize(10); label.setGravity(Gravity.CENTER);
            label.setTextColor(activeTab.equals(item[0]) ? LAVENDER : MUTED);
            cell.addView(icon); cell.addView(label);
            cell.setOnClickListener(v -> navigate(item[0]));
            bottomNav.addView(cell, new LinearLayout.LayoutParams(0, dp(60), 1));
        }
    }

    private void navigate(String tab) {
        activeTab = tab; buildBottomNav();
        if ("Home".equals(tab)) showHome();
        else if ("Search".equals(tab)) showSearch();
        else if ("Create".equals(tab)) showCreate();
        else showProfile();
    }

    private void resetContent() { content.removeAllViews(); }

    private void showHome() {
        activeTab = "Home"; buildBottomNav(); resetContent();
        LinearLayout hero = card();
        TextView eyebrow = text("YOUR COMMUNITY", 10, LAVENDER, true);
        TextView title = text("A little space\nfor big thoughts.", 27, WHITE, true);
        TextView subtitle = text("Read, write and connect with people who see the world differently.", 13, MUTED, false);
        MaterialButton write = button("Write a post  +");
        write.setOnClickListener(v -> navigate("Create"));
        hero.addView(eyebrow); hero.addView(title, marginParams(0, 8, 0, 0));
        hero.addView(subtitle, marginParams(0, 8, 0, 0)); hero.addView(write, marginParams(0, 16, 0, 0));
        content.addView(hero, marginParams(0, 0, 0, 18));
        sectionTitle("Your feed", "Fresh thoughts from the LEVIA community");
        TextView loading = text("Loading posts…", 13, MUTED, false); content.addView(loading);
        ApiClient.get(this).feed(25).enqueue(new Callback<ApiResponse<PostPage>>() {
            @Override public void onResponse(Call<ApiResponse<PostPage>> call, Response<ApiResponse<PostPage>> r) {
                if (!isFinishing() && activeTab.equals("Home")) {
                    content.removeView(loading);
                    if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null && r.body().data.items != null) {
                        visiblePosts.clear(); visiblePosts.addAll(r.body().data.items);
                        renderPosts(visiblePosts);
                        if (visiblePosts.isEmpty()) emptyState("It's quiet here", "Be the first to share a thought with the community.");
                    } else emptyState("Couldn't load the feed", ApiErrorParser.message(r));
                }
            }
            @Override public void onFailure(Call<ApiResponse<PostPage>> call, Throwable t) {
                if (!isFinishing() && activeTab.equals("Home")) { content.removeView(loading); emptyState("You're offline?", ApiErrorParser.networkMessage(t)); }
            }
        });
    }

    private void showCreate() {
        activeTab = "Create"; buildBottomNav(); resetContent();
        sectionTitle("Create something", "Your words deserve a place to land.");
        LinearLayout panel = card();
        TextView label = text("WHAT'S ON YOUR MIND?", 10, LAVENDER, true); panel.addView(label);
        EditText body = edit("Write your thought, story or quote…", 2000);
        body.setMinLines(7); body.setGravity(Gravity.TOP | Gravity.START); body.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        panel.addView(body, marginParams(0, 12, 0, 14));
        TextView categoryLabel = text("Choose a category", 12, MUTED, true); panel.addView(categoryLabel);
        Spinner category = new Spinner(this);
        String[] categories = {"thought", "poetry", "shayari", "daily_thought", "quote", "love", "sadness", "motivation", "friendship", "life", "attitude", "funny", "other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        category.setAdapter(adapter); panel.addView(category, marginParams(0, 6, 0, 14));
        MaterialButton publish = button("Publish post"); panel.addView(publish);
        publish.setOnClickListener(v -> {
            String value = body.getText().toString().trim();
            if (value.isEmpty()) { body.setError("Write something first"); return; }
            publish.setEnabled(false); publish.setText("Publishing…");
            ApiClient.get(this).createPost(new CreatePostRequest(value, String.valueOf(category.getSelectedItem())))
                    .enqueue(new Callback<ApiResponse<Post>>() {
                        @Override public void onResponse(Call<ApiResponse<Post>> c, Response<ApiResponse<Post>> r) {
                            if (r.isSuccessful() && r.body() != null && r.body().success) { toast("Your post is live ✨"); navigate("Home"); }
                            else { publish.setEnabled(true); publish.setText("Publish post"); toast(ApiErrorParser.message(r)); }
                        }
                        @Override public void onFailure(Call<ApiResponse<Post>> c, Throwable t) { publish.setEnabled(true); publish.setText("Publish post"); toast(ApiErrorParser.networkMessage(t)); }
                    });
        });
        content.addView(panel);
        TextView note = text("Posts are public unless your account is private. Keep your community kind and respectful.", 11, MUTED, false);
        content.addView(note, marginParams(4, 12, 4, 0));
    }

    private void showSearch() {
        activeTab = "Search"; buildBottomNav(); resetContent();
        sectionTitle("Explore LEVIA", "Find people and discover thoughts.");
        LinearLayout searchRow = new LinearLayout(this); searchRow.setOrientation(LinearLayout.HORIZONTAL); searchRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText query = edit("Search people or posts", 100); query.setSingleLine(true);
        searchRow.addView(query, new LinearLayout.LayoutParams(0, dp(52), 1));
        MaterialButton go = button("Search"); LinearLayout.LayoutParams goParams = new LinearLayout.LayoutParams(-2, dp(52));
        goParams.leftMargin = dp(8); searchRow.addView(go, goParams);
        content.addView(searchRow, marginParams(0, 0, 0, 16));
        LinearLayout results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL); content.addView(results);
        go.setOnClickListener(v -> runSearch(query.getText().toString().trim(), results));
        query.setOnEditorActionListener((v, actionId, event) -> { go.performClick(); return true; });
        emptyStateInto(results, "Start exploring", "Search by a username, name or a word from a post.");
    }

    private void runSearch(String q, LinearLayout results) {
        if (q.isEmpty()) { toast("Enter something to search"); return; }
        results.removeAllViews();
        LinearLayout people = new LinearLayout(this); people.setOrientation(LinearLayout.VERTICAL);
        LinearLayout posts = new LinearLayout(this); posts.setOrientation(LinearLayout.VERTICAL);
        results.addView(people); results.addView(posts);
        people.addView(text("Searching for people…", 12, MUTED, false));
        posts.addView(text("Searching posts…", 12, MUTED, false));
        ApiClient.get(this).searchUsers(q, 10).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override public void onResponse(Call<ApiResponse<List<User>>> c, Response<ApiResponse<List<User>>> r) {
                if (!activeTab.equals("Search")) return;
                people.removeAllViews(); sectionInto(people, "People");
                if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                    if (r.body().data.isEmpty()) people.addView(text("No people found.", 12, MUTED, false));
                    else for (User user : r.body().data) renderUser(people, user);
                } else people.addView(text(ApiErrorParser.message(r), 12, MUTED, false));
            }
            @Override public void onFailure(Call<ApiResponse<List<User>>> c, Throwable t) {
                if (activeTab.equals("Search")) { people.removeAllViews(); sectionInto(people, "People"); people.addView(text(ApiErrorParser.networkMessage(t), 12, MUTED, false)); }
            }
        });
        ApiClient.get(this).searchPosts(q, 20).enqueue(new Callback<ApiResponse<PostPage>>() {
            @Override public void onResponse(Call<ApiResponse<PostPage>> c, Response<ApiResponse<PostPage>> r) {
                if (!activeTab.equals("Search")) return;
                posts.removeAllViews(); sectionInto(posts, "Posts");
                if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                    if (r.body().data.items == null || r.body().data.items.isEmpty()) posts.addView(text("No matching posts.", 12, MUTED, false));
                    else for (Post post : r.body().data.items) renderPost(posts, post);
                } else posts.addView(text(ApiErrorParser.message(r), 12, MUTED, false));
            }
            @Override public void onFailure(Call<ApiResponse<PostPage>> c, Throwable t) {
                if (activeTab.equals("Search")) { posts.removeAllViews(); sectionInto(posts, "Posts"); posts.addView(text(ApiErrorParser.networkMessage(t), 12, MUTED, false)); }
            }
        });
    }

    private void showProfile() {
        activeTab = "Profile"; buildBottomNav(); resetContent();
        sectionTitle("Your profile", "Your corner of the community.");
        TextView loading = text("Loading profile…", 13, MUTED, false); content.addView(loading);
        ApiClient.get(this).myProfile().enqueue(new Callback<ApiResponse<ProfileData>>() {
            @Override public void onResponse(Call<ApiResponse<ProfileData>> c, Response<ApiResponse<ProfileData>> r) {
                if (!activeTab.equals("Profile")) return;
                content.removeView(loading);
                if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                    myProfile = r.body().data; currentUsername = myProfile.username;
                    renderProfile(myProfile);
                } else emptyState("Profile unavailable", ApiErrorParser.message(r));
            }
            @Override public void onFailure(Call<ApiResponse<ProfileData>> c, Throwable t) { if (activeTab.equals("Profile")) { content.removeView(loading); emptyState("Couldn't load profile", ApiErrorParser.networkMessage(t)); } }
        });
    }

    private void renderProfile(ProfileData profile) {
        LinearLayout panel = card(); panel.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView avatar = text(initial(profile.displayName), 28, WHITE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(circle(PURPLE));
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(dp(76), dp(76)); avp.bottomMargin = dp(12); panel.addView(avatar, avp);
        panel.addView(text(safe(profile.displayName, "LEVIA member"), 20, WHITE, true));
        panel.addView(text("@" + safe(profile.username, "username"), 12, LAVENDER, false), marginParams(0, 4, 0, 8));
        if (profile.bio != null && !profile.bio.trim().isEmpty()) panel.addView(text(profile.bio, 13, MUTED, false), marginParams(4, 0, 4, 12));
        LinearLayout stats = new LinearLayout(this); stats.setGravity(Gravity.CENTER); stats.setOrientation(LinearLayout.HORIZONTAL);
        int posts = profile.counts == null ? 0 : profile.counts.posts;
        int followers = profile.counts == null ? 0 : profile.counts.followers;
        int following = profile.counts == null ? 0 : profile.counts.following;
        stats.addView(stat("Posts", posts), new LinearLayout.LayoutParams(0, dp(54), 1));
        LinearLayout followersStat = stat("Followers", followers);
        followersStat.setOnClickListener(v -> showConnections(profile.username, true));
        stats.addView(followersStat, new LinearLayout.LayoutParams(0, dp(54), 1));
        LinearLayout followingStat = stat("Following", following);
        followingStat.setOnClickListener(v -> showConnections(profile.username, false));
        stats.addView(followingStat, new LinearLayout.LayoutParams(0, dp(54), 1));
        panel.addView(stats, marginParams(0, 8, 0, 12));
        MaterialButton edit = button("Edit profile"); edit.setOnClickListener(v -> editProfile(profile)); panel.addView(edit, fullWidth());
        content.addView(panel, marginParams(0, 0, 0, 18));
        sectionTitle("Your posts", "Everything you've shared.");
        ApiClient.get(this).userPosts(profile.username, 30).enqueue(new Callback<ApiResponse<PostPage>>() {
            @Override public void onResponse(Call<ApiResponse<PostPage>> c, Response<ApiResponse<PostPage>> r) {
                if (!activeTab.equals("Profile")) return;
                if (r.isSuccessful() && r.body() != null && r.body().data != null && r.body().data.items != null) {
                    if (r.body().data.items.isEmpty()) emptyState("Your story starts here", "Create your first post and it will appear here.");
                    else for (Post post : r.body().data.items) renderPost(content, post);
                } else toast(ApiErrorParser.message(r));
            }
            @Override public void onFailure(Call<ApiResponse<PostPage>> c, Throwable t) { if (activeTab.equals("Profile")) toast(ApiErrorParser.networkMessage(t)); }
        });
    }

    private void showConnections(String username, boolean followers) {
        String title = followers ? "Followers" : "Following";
        Call<ApiResponse<ConnectionPage>> call = followers
                ? ApiClient.get(this).followers(username, 50)
                : ApiClient.get(this).following(username, 50);
        call.enqueue(new Callback<ApiResponse<ConnectionPage>>() {
            @Override public void onResponse(Call<ApiResponse<ConnectionPage>> c, Response<ApiResponse<ConnectionPage>> r) {
                if (!r.isSuccessful() || r.body() == null || r.body().data == null || r.body().data.items == null) { toast(ApiErrorParser.message(r)); return; }
                LinearLayout list = new LinearLayout(MainActivity.this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(8), dp(4), dp(8), dp(4));
                if (r.body().data.items.isEmpty()) list.addView(text("No accounts yet.", 13, MUTED, false));
                for (ConnectionUser user : r.body().data.items) {
                    TextView row = text(safe(user.displayName, "LEVIA member") + "  ·  @" + safe(user.username, "member"), 13, WHITE, false);
                    row.setPadding(dp(8), dp(12), dp(8), dp(12)); list.addView(row);
                }
                ScrollView scroll = new ScrollView(MainActivity.this); scroll.addView(list);
                new AlertDialog.Builder(MainActivity.this).setTitle(title).setView(scroll).setPositiveButton("Close", null).show();
            }
            @Override public void onFailure(Call<ApiResponse<ConnectionPage>> c, Throwable t) { toast(ApiErrorParser.networkMessage(t)); }
        });
    }

    private void editProfile(ProfileData profile) {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(4), dp(4), dp(4), dp(4));
        EditText name = edit("Display name", 100); name.setText(safe(profile.displayName, ""));
        EditText bio = edit("Bio", 300); bio.setText(safe(profile.bio, ""));
        EditText avatar = edit("Profile image URL (https://)", 500); avatar.setText(safe(profile.avatarUrl, ""));
        form.addView(name); form.addView(bio, marginParams(0, 8, 0, 0)); form.addView(avatar, marginParams(0, 8, 0, 0));
        new AlertDialog.Builder(this).setTitle("Edit profile").setView(form)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", (d, w) -> {
                    Map<String, Object> fields = new HashMap<>(); fields.put("displayName", name.getText().toString().trim());
                    fields.put("bio", bio.getText().toString().trim());
                    String url = avatar.getText().toString().trim(); fields.put("avatarUrl", url.isEmpty() ? null : url);
                    ApiClient.get(this).updateProfile(fields).enqueue(new Callback<ApiResponse<ProfileData>>() {
                        @Override public void onResponse(Call<ApiResponse<ProfileData>> c, Response<ApiResponse<ProfileData>> r) { if (r.isSuccessful()) { toast("Profile updated"); showProfile(); } else toast(ApiErrorParser.message(r)); }
                        @Override public void onFailure(Call<ApiResponse<ProfileData>> c, Throwable t) { toast(ApiErrorParser.networkMessage(t)); }
                    });
                }).show();
    }

    private void renderPosts(List<Post> posts) { for (Post post : posts) renderPost(content, post); }

    private void renderPost(LinearLayout target, Post post) {
        LinearLayout panel = card();
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setOrientation(LinearLayout.HORIZONTAL);
        TextView avatar = text(initial(post.displayName), 15, WHITE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(circle(SURFACE_2));
        top.addView(avatar, new LinearLayout.LayoutParams(dp(38), dp(38)));
        LinearLayout author = new LinearLayout(this); author.setOrientation(LinearLayout.VERTICAL);
        author.addView(text(safe(post.displayName, "LEVIA member"), 13, WHITE, true));
        author.addView(text("@" + safe(post.username, "member") + "  ·  " + categoryLabel(post.category), 10, MUTED, false));
        LinearLayout.LayoutParams authorParams = new LinearLayout.LayoutParams(0, -2, 1); authorParams.leftMargin = dp(10); top.addView(author, authorParams);
        if (post.isPinned) top.addView(text("PINNED", 9, LAVENDER, true));
        panel.addView(top);
        TextView body = text(safe(post.body, ""), 15, Color.rgb(239, 234, 247), false);
        body.setLineSpacing(dp(3), 1f); panel.addView(body, marginParams(0, 16, 0, 16));
        View line = new View(this); line.setBackgroundColor(Color.rgb(55, 44, 68)); panel.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
        LinearLayout actions = new LinearLayout(this); actions.setGravity(Gravity.CENTER_VERTICAL); actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView like = actionText((post.likedByMe ? "♥  " : "♡  ") + post.likeCount);
        like.setTextColor(post.likedByMe ? Color.rgb(255, 115, 164) : MUTED);
        like.setOnClickListener(v -> toggleLike(post, like));
        TextView comments = actionText("◌  " + post.commentCount + " comments");
        comments.setOnClickListener(v -> openComments(post));
        actions.addView(like, new LinearLayout.LayoutParams(0, dp(42), 1));
        actions.addView(comments, new LinearLayout.LayoutParams(0, dp(42), 1));
        panel.addView(actions);
        target.addView(panel, marginParams(0, 0, 0, 14));
    }

    private void toggleLike(Post post, TextView label) {
        boolean wasLiked = post.likedByMe;
        Call<ApiResponse<LikeResult>> call = wasLiked ? ApiClient.get(this).unlikePost(post.id) : ApiClient.get(this).likePost(post.id);
        label.setEnabled(false);
        call.enqueue(new Callback<ApiResponse<LikeResult>>() {
            @Override public void onResponse(Call<ApiResponse<LikeResult>> c, Response<ApiResponse<LikeResult>> r) {
                label.setEnabled(true);
                if (r.isSuccessful() && r.body() != null && r.body().data != null) {
                    post.likedByMe = r.body().data.liked; post.likeCount = r.body().data.likeCount;
                    label.setText((post.likedByMe ? "♥  " : "♡  ") + post.likeCount);
                    label.setTextColor(post.likedByMe ? Color.rgb(255, 115, 164) : MUTED);
                } else toast(ApiErrorParser.message(r));
            }
            @Override public void onFailure(Call<ApiResponse<LikeResult>> c, Throwable t) { label.setEnabled(true); toast(ApiErrorParser.networkMessage(t)); }
        });
    }

    private void openComments(Post post) {
        LinearLayout layout = new LinearLayout(this); layout.setPadding(dp(4), dp(4), dp(4), dp(4)); layout.setOrientation(LinearLayout.VERTICAL);
        TextView loading = text("Loading comments…", 12, MUTED, false); layout.addView(loading);
        EditText input = edit("Write a comment…", 1000); layout.addView(input, marginParams(0, 12, 0, 8));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Comments").setView(layout).setNegativeButton("Close", null)
                .setPositiveButton("Send", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String body = input.getText().toString().trim(); if (body.isEmpty()) { input.setError("Write a comment"); return; }
            ApiClient.get(this).addComment(post.id, new CommentRequest(body)).enqueue(new Callback<ApiResponse<Comment>>() {
                @Override public void onResponse(Call<ApiResponse<Comment>> c, Response<ApiResponse<Comment>> r) { if (r.isSuccessful()) { post.commentCount++; dialog.dismiss(); toast("Comment added"); if (activeTab.equals("Home")) showHome(); else if (activeTab.equals("Profile")) showProfile(); } else toast(ApiErrorParser.message(r)); }
                @Override public void onFailure(Call<ApiResponse<Comment>> c, Throwable t) { toast(ApiErrorParser.networkMessage(t)); }
            });
        }));
        dialog.show();
        ApiClient.get(this).comments(post.id).enqueue(new Callback<ApiResponse<CommentPage>>() {
            @Override public void onResponse(Call<ApiResponse<CommentPage>> c, Response<ApiResponse<CommentPage>> r) {
                layout.removeView(loading);
                if (r.isSuccessful() && r.body() != null && r.body().data != null && r.body().data.items != null) {
                    if (r.body().data.items.isEmpty()) layout.addView(text("No comments yet. Start the conversation.", 12, MUTED, false), 0);
                    else for (Comment comment : r.body().data.items) { TextView row = text(safe(comment.displayName, comment.username) + "\n" + comment.body, 12, WHITE, false); row.setPadding(0, dp(6), 0, dp(6)); layout.addView(row, 0); }
                } else layout.addView(text(ApiErrorParser.message(r), 12, MUTED, false), 0);
            }
            @Override public void onFailure(Call<ApiResponse<CommentPage>> c, Throwable t) { layout.removeView(loading); layout.addView(text(ApiErrorParser.networkMessage(t), 12, MUTED, false), 0); }
        });
    }

    private void renderUser(LinearLayout target, User user) {
        LinearLayout panel = card(); panel.setOrientation(LinearLayout.HORIZONTAL); panel.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar = text(initial(user.displayName), 15, WHITE, true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(circle(SURFACE_2)); panel.addView(avatar, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout names = new LinearLayout(this); names.setOrientation(LinearLayout.VERTICAL);
        names.addView(text(safe(user.displayName, "LEVIA member"), 13, WHITE, true)); names.addView(text("@" + safe(user.username, "member"), 11, MUTED, false));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, -2, 1); np.leftMargin = dp(10); panel.addView(names, np);
        MaterialButton follow = button("Follow"); follow.setTextSize(11); follow.setMinHeight(dp(38)); follow.setPadding(dp(12), 0, dp(12), 0);
        follow.setOnClickListener(v -> { follow.setEnabled(false); ApiClient.get(this).follow(user.id).enqueue(new Callback<ApiResponse<Map<String,Object>>>() {
            @Override public void onResponse(Call<ApiResponse<Map<String,Object>>> c, Response<ApiResponse<Map<String,Object>>> r) { follow.setEnabled(true); if (r.isSuccessful()) {
                String status = r.body() != null && r.body().data != null && r.body().data.get("status") != null ? String.valueOf(r.body().data.get("status")) : "requested";
                follow.setText("following".equals(status) ? "Following" : "Requested"); follow.setEnabled(false);
            } else toast(ApiErrorParser.message(r)); }
            @Override public void onFailure(Call<ApiResponse<Map<String,Object>>> c, Throwable t) { follow.setEnabled(true); toast(ApiErrorParser.networkMessage(t)); }
        }); });
        panel.addView(follow); target.addView(panel, marginParams(0, 0, 0, 10));
    }

    private void sectionTitle(String title, String subtitle) {
        content.addView(text(title, 22, WHITE, true));
        content.addView(text(subtitle, 12, MUTED, false), marginParams(0, 4, 0, 16));
    }
    private void sectionInto(LinearLayout target, String title) { target.addView(text(title, 16, LAVENDER, true), marginParams(0, 14, 0, 10)); }
    private void emptyState(String title, String subtitle) { emptyStateInto(content, title, subtitle); }
    private void emptyStateInto(LinearLayout target, String title, String subtitle) {
        LinearLayout panel = card(); panel.setGravity(Gravity.CENTER); TextView t = text(title, 16, WHITE, true); t.setGravity(Gravity.CENTER);
        TextView s = text(subtitle, 12, MUTED, false); s.setGravity(Gravity.CENTER);
        panel.addView(t); panel.addView(s, marginParams(4, 6, 4, 0)); target.addView(panel, marginParams(0, 8, 0, 12));
    }
    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16), dp(16), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.rgb(34, 25, 48), SURFACE});
        bg.setCornerRadius(dp(20)); bg.setStroke(dp(1), Color.rgb(59, 43, 79)); box.setBackground(bg); return box;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view;
    }
    private TextView actionText(String value) { TextView v = text(value, 12, MUTED, true); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private EditText edit(String hint, int max) {
        EditText e = new EditText(this); e.setHint(hint); e.setHintTextColor(MUTED); e.setTextColor(WHITE); e.setTextSize(14);
        e.setPadding(dp(12), dp(10), dp(12), dp(10)); e.setBackground(roundRect(SURFACE_2, Color.rgb(68, 52, 89), 12));
        e.setMaxLines(8); e.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(max)}); return e;
    }
    private MaterialButton button(String label) {
        MaterialButton b = new MaterialButton(this); b.setText(label); b.setTextAllCaps(false); b.setTextColor(WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PURPLE)); b.setCornerRadius(dp(13)); b.setMinHeight(dp(48));
        b.setInsetTop(0); b.setInsetBottom(0); return b;
    }
    private LinearLayout stat(String label, int count) {
        LinearLayout col = new LinearLayout(this); col.setGravity(Gravity.CENTER); col.setOrientation(LinearLayout.VERTICAL);
        col.addView(text(String.valueOf(count), 18, WHITE, true)); col.addView(text(label, 10, MUTED, false)); return col;
    }
    private GradientDrawable circle(int color) { GradientDrawable d = new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(color); d.setStroke(dp(1), Color.rgb(83, 62, 112)); return d; }
    private GradientDrawable roundRect(int color, int stroke, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); d.setStroke(dp(1), stroke); return d; }
    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(dp(left), dp(top), dp(right), dp(bottom)); return p; }
    private LinearLayout.LayoutParams fullWidth() { return new LinearLayout.LayoutParams(-1, -2); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private String initial(String name) { String s = safe(name, "L").trim(); return s.isEmpty() ? "L" : s.substring(0, 1).toUpperCase(); }
    private String safe(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    private String categoryLabel(String category) { return category == null ? "thought" : category.replace('_', ' '); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    private void logout() {
        String refresh = store.getRefreshToken(); store.clear();
        if (refresh != null) ApiClient.get(this).logout(new RefreshRequest(refresh)).enqueue(new Callback<ApiResponse<Object>>() {
            @Override public void onResponse(Call<ApiResponse<Object>> c, Response<ApiResponse<Object>> r) { }
            @Override public void onFailure(Call<ApiResponse<Object>> c, Throwable t) { }
        });
        goToLogin();
    }
    private void goToLogin() {
        store.clear(); Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(i); finish();
    }
}

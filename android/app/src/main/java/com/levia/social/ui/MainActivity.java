package com.levia.social.ui;

import android.content.Intent;
<<<<<<< HEAD
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
=======
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
import androidx.appcompat.app.AppCompatActivity;

import com.levia.social.R;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.MeData;
<<<<<<< HEAD
import com.levia.social.data.model.ProfileData;
import com.levia.social.data.model.SearchUser;
import com.levia.social.data.model.FollowResult;
import com.levia.social.data.model.ConnectionPage;
import com.levia.social.data.model.ConnectionUser;
=======
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.List;
import java.util.Map;

=======
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

<<<<<<< HEAD
/** LEVIA shell. Feed and media creation stay honest until corresponding backend APIs exist. */
public class MainActivity extends AppCompatActivity {
    private static final int BG = Color.rgb(10, 9, 18);
    private static final int PANEL = Color.rgb(24, 19, 38);
    private static final int PANEL2 = Color.rgb(34, 26, 52);
    private static final int PURPLE = Color.rgb(151, 102, 255);
    private static final int LAVENDER = Color.rgb(211, 193, 255);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(166, 157, 187);

    private LinearLayout root, header, body, nav;
    private TextView welcomeText, usernameText;
    private TokenStore store;
    private ProfileData myProfile;
    private String currentTab = "Home";
    private EditText searchInput;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new TokenStore(this);
        buildShell();
        loadMe();
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18), dp(12), dp(18), dp(8));
        setContentView(root);

        header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(54)));

        TextView brand = text("LEVIA", 24, LAVENDER, true);
        brand.setLetterSpacing(.12f);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView search = action("⌕");
        header.addView(search);
        search.setOnClickListener(v -> showTab("Search"));
        TextView notifications = action("♧");
        header.addView(notifications);
        notifications.setOnClickListener(v -> Toast.makeText(this,"Notifications will appear when a notifications API is available.",Toast.LENGTH_SHORT).show());
        TextView security = action("⚙");
        header.addView(security);
        security.setOnClickListener(v -> startActivity(new Intent(this, AccountSecurityActivity.class)));
        TextView logout = action("↗");
        header.addView(logout);
        logout.setOnClickListener(v -> logout());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, dp(12), 0, dp(18));
        scroll.addView(body);

        nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, dp(8), 0, 0);
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(58)));
        for (String tab : new String[]{"Home", "Search", "Create", "Profile"}) {
            TextView item = text(tab, 13, MUTED, true);
            item.setGravity(Gravity.CENTER);
            nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
            item.setOnClickListener(v -> showTab(tab));
        }
        welcomeText = text("", 22, WHITE, true);
        usernameText = text("", 14, MUTED, false);
        showTab("Home");
    }

    private void showTab(String tab) {
        currentTab = tab;
        body.removeAllViews();
        for (int i=0;i<nav.getChildCount();i++) {
            TextView t=(TextView)nav.getChildAt(i);
            boolean active=t.getText().toString().equals(tab);
            t.setTextColor(active ? LAVENDER : MUTED);
            t.setBackground(active ? rounded(PANEL2, 18, PURPLE) : rounded(BG, 18, BG));
        }
        if ("Home".equals(tab)) renderHome();
        else if ("Search".equals(tab)) renderSearch();
        else if ("Profile".equals(tab)) loadProfile();
        else renderCreateInfo();
        AlphaAnimation fade = new AlphaAnimation(0f,1f);
        fade.setDuration(220);
        body.startAnimation(fade);
    }

    private void renderHome() {
        addEyebrow("YOUR SPACE");
        TextView title = text("A little more you.", 27, WHITE, true);
        body.addView(title);
        body.addView(text("A community for ideas, creativity and connection.", 14, MUTED, false));
        LinearLayout stories = panel();
        stories.setPadding(dp(16),dp(13),dp(16),dp(13));
        body.addView(stories,matchWrap(18));
        stories.addView(text("STORIES",11,LAVENDER,true));
        stories.addView(text("Story sharing is not connected yet.",13,MUTED,false));
        LinearLayout hero = panel();
        hero.setPadding(dp(18),dp(20),dp(18),dp(20));
        body.addView(hero, matchWrap(22));
        TextView orb = text("✦", 32, LAVENDER, true);
        hero.addView(orb);
        hero.addView(text("Your feed starts here", 19, WHITE, true));
        TextView note=text("When people you follow share something, their posts will appear here. LEVIA does not show sample posts as real content.",14,MUTED,false);
        hero.addView(note);
        TextView search=button("Discover people");
        hero.addView(search, matchWrap(18));
        search.setOnClickListener(v -> showTab("Search"));
        LinearLayout quick=panel();
        quick.setPadding(dp(16),dp(16),dp(16),dp(16));
        body.addView(quick,matchWrap(14));
        quick.addView(text("Your community",17,WHITE,true));
        quick.addView(text("Find creators, follow profiles and build your own circle.",13,MUTED,false));
        TextView profile=button("View my profile");
        quick.addView(profile,matchWrap(14));
        profile.setOnClickListener(v -> showTab("Profile"));
    }

    private void renderSearch() {
        addEyebrow("DISCOVER");
        body.addView(text("Find your people",26,WHITE,true));
        body.addView(text("Search by username or display name.",14,MUTED,false));
        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setTextColor(WHITE);
        searchInput.setHintTextColor(MUTED);
        searchInput.setHint("Search people…");
        searchInput.setPadding(dp(16),0,dp(16),0);
        searchInput.setBackground(rounded(PANEL,16,Color.rgb(76,57,105)));
        LinearLayout.LayoutParams ip=matchWrap(18); ip.height=dp(54);
        body.addView(searchInput,ip);
        TextView status=text("Start typing to discover users.",14,MUTED,false);
        body.addView(status,matchWrap(12));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count){
                String q=s.toString().trim();
                if(q.length()<2){status.setText("Enter at least 2 characters.");return;}
                status.setText("Searching…");
                searchUsers(q,status);
            }
            @Override public void afterTextChanged(Editable e){}
        });
    }

    private void searchUsers(String query, TextView status) {
        ApiClient.get(this).searchUsers(query,20).enqueue(new Callback<ApiResponse<List<SearchUser>>>() {
            @Override public void onResponse(Call<ApiResponse<List<SearchUser>>> c,Response<ApiResponse<List<SearchUser>>> r) {
                if(!"Search".equals(currentTab)) return;
                // Remove prior results while retaining search input and status.
                while(body.getChildCount()>5) body.removeViewAt(5);
                if(r.isSuccessful() && r.body()!=null && r.body().success && r.body().data!=null&&r.body().data.user!=null) {
                    List<SearchUser> users=r.body().data;
                    status.setText(users.isEmpty()?"No people found. Try another name.":users.size()+" people found");
                    for(SearchUser u:users) addSearchRow(u);
                } else status.setText(ApiErrorParser.message(r));
            }
            @Override public void onFailure(Call<ApiResponse<List<SearchUser>>> c,Throwable t) {
                if("Search".equals(currentTab)) status.setText(ApiErrorParser.networkMessage(t));
            }
        });
    }

    private void addSearchRow(final SearchUser u) {
        LinearLayout row=panel();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),dp(12),dp(14),dp(12));
        body.addView(row,matchWrap(10));
        TextView avatar=text(initial(u.displayName),18,LAVENDER,true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(rounded(PANEL2,30,PURPLE));
        row.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(44)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(0,-2,1); ilp.leftMargin=dp(12);
        row.addView(info,ilp);
        info.addView(text(u.displayName==null?u.username:u.displayName,15,WHITE,true));
        info.addView(text("@"+u.username,12,MUTED,false));
        TextView follow=button("Follow");
        row.addView(follow);
        follow.setOnClickListener(v -> {
            follow.setEnabled(false);
            ApiClient.get(this).followUser(u.id).enqueue(new Callback<ApiResponse<FollowResult>>() {
                @Override public void onResponse(Call<ApiResponse<FollowResult>> c,Response<ApiResponse<FollowResult>> r) {
                    follow.setEnabled(true);
                    if(r.isSuccessful() && r.body()!=null && r.body().success && r.body().data!=null) {
                        String state=r.body().data.status;
                        follow.setText("requested".equals(state)?"Requested":"Following");
                        Toast.makeText(MainActivity.this,"requested".equals(state)?"Follow request sent":"Following @"+u.username,Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(MainActivity.this,ApiErrorParser.message(r),Toast.LENGTH_LONG).show();
                }
                @Override public void onFailure(Call<ApiResponse<FollowResult>> c,Throwable t) {
                    follow.setEnabled(true);
                    Toast.makeText(MainActivity.this,ApiErrorParser.networkMessage(t),Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void loadProfile() {
        addEyebrow("YOUR PROFILE");
        body.addView(text("Your profile",26,WHITE,true));
        TextView loading=text("Loading your profile…",14,MUTED,false);
        body.addView(loading,matchWrap(12));
        ApiClient.get(this).myProfile().enqueue(new Callback<ApiResponse<ProfileData>>() {
            @Override public void onResponse(Call<ApiResponse<ProfileData>> c,Response<ApiResponse<ProfileData>> r) {
                if(!"Profile".equals(currentTab))return;
                body.removeAllViews();
                addEyebrow("YOUR PROFILE");
                if(r.isSuccessful()&&r.body()!=null&&r.body().success&&r.body().data!=null) {
                    myProfile=r.body().data; renderProfile(myProfile);
                } else body.addView(text(ApiErrorParser.message(r),14,MUTED,false));
            }
            @Override public void onFailure(Call<ApiResponse<ProfileData>> c,Throwable t) {
                if("Profile".equals(currentTab))loading.setText(ApiErrorParser.networkMessage(t));
            }
        });
    }

    private void renderProfile(ProfileData p) {
        LinearLayout card=panel(); card.setPadding(dp(18),dp(20),dp(18),dp(20));
        body.addView(card,matchWrap(10));
        TextView avatar=text(initial(p.displayName),28,LAVENDER,true);
        avatar.setGravity(Gravity.CENTER); avatar.setBackground(rounded(PANEL2,50,PURPLE));
        card.addView(avatar,new LinearLayout.LayoutParams(dp(72),dp(72)));
        card.addView(text(p.displayName==null?"LEVIA member":p.displayName,21,WHITE,true));
        card.addView(text("@"+p.username,14,LAVENDER,false));
        card.addView(text(p.bio==null||p.bio.trim().isEmpty()?"Add a bio to tell people about yourself.":p.bio,14,MUTED,false));
        LinearLayout stats=new LinearLayout(this); stats.setPadding(0,dp(18),0,dp(8));
        card.addView(stats,matchWrap(0));
        int posts=p.counts==null?0:p.counts.posts, followers=p.counts==null?0:p.counts.followers, following=p.counts==null?0:p.counts.following;
        stats.addView(stat(posts,"Posts",null),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(stat(followers,"Followers",()->loadConnections(false)),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(stat(following,"Following",()->loadConnections(true)),new LinearLayout.LayoutParams(0,-2,1));
        TextView edit=button("Edit profile");
        card.addView(edit,matchWrap(12)); edit.setOnClickListener(v->editProfile(p));
        TextView postsLabel=text("POSTS",12,LAVENDER,true); postsLabel.setPadding(0,dp(18),0,dp(8)); body.addView(postsLabel);
        LinearLayout empty=panel(); empty.setPadding(dp(18),dp(22),dp(18),dp(22)); body.addView(empty,matchWrap(8));
        empty.addView(text("No posts yet",17,WHITE,true));
        empty.addView(text("Your published posts will appear here when the posts API is available.",13,MUTED,false));
    }

    private View stat(int value,String label,Runnable action) {
        LinearLayout col=new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setGravity(Gravity.CENTER);
        col.setPadding(dp(5),dp(7),dp(5),dp(7));
        col.addView(text(String.valueOf(value),18,WHITE,true)); col.addView(text(label,11,MUTED,false));
        if(action!=null) col.setOnClickListener(v->action.run());
        return col;
    }

    private void loadConnections(boolean following) {
        if(myProfile==null || myProfile.username==null) return;
        Call<ApiResponse<ConnectionPage>> call=following
                ?ApiClient.get(this).following(myProfile.username,50)
                :ApiClient.get(this).followers(myProfile.username,50);
        call.enqueue(new Callback<ApiResponse<ConnectionPage>>() {
            @Override public void onResponse(Call<ApiResponse<ConnectionPage>> c,Response<ApiResponse<ConnectionPage>> r) {
                if(r.isSuccessful()&&r.body()!=null&&r.body().success&&r.body().data!=null) {
                    List<ConnectionUser> users=r.body().data.items;
                    StringBuilder list=new StringBuilder();
                    if(users!=null) for(ConnectionUser u:users) {
                        if(list.length()>0) list.append("\\n");
                        list.append(u.displayName==null?u.username:u.displayName).append("  @").append(u.username);
                    }
                    if(list.length()==0) list.append("No ").append(following?"subscriptions":"subscribers").append(" yet.");
                    new AlertDialog.Builder(MainActivity.this).setTitle(following?"Following":"Followers").setMessage(list.toString()).setPositiveButton("Close",null).show();
                } else Toast.makeText(MainActivity.this,ApiErrorParser.message(r),Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ApiResponse<ConnectionPage>> c,Throwable t) {
                Toast.makeText(MainActivity.this,ApiErrorParser.networkMessage(t),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editProfile(ProfileData p) {
        LinearLayout box=new LinearLayout(this); box.setPadding(dp(20),dp(8),dp(20),0); box.setOrientation(LinearLayout.VERTICAL);
        EditText name=new EditText(this); name.setSingleLine(true); name.setText(p.displayName); name.setHint("Display name"); box.addView(name);
        EditText bio=new EditText(this); bio.setText(p.bio); bio.setHint("Bio"); bio.setMinLines(2); box.addView(bio);
        new AlertDialog.Builder(this).setTitle("Edit profile").setView(box).setNegativeButton("Cancel",null)
            .setPositiveButton("Save",(d,w)->{
                Map<String,Object> fields=new HashMap<>();
                fields.put("displayName",name.getText().toString().trim());
                fields.put("bio",bio.getText().toString().trim());
                ApiClient.get(this).updateMyProfile(fields).enqueue(new Callback<ApiResponse<ProfileData>>() {
                    @Override public void onResponse(Call<ApiResponse<ProfileData>> c,Response<ApiResponse<ProfileData>> r) {
                        if(r.isSuccessful()&&r.body()!=null&&r.body().success){Toast.makeText(MainActivity.this,"Profile updated",Toast.LENGTH_SHORT).show();loadProfile();}
                        else Toast.makeText(MainActivity.this,ApiErrorParser.message(r),Toast.LENGTH_LONG).show();
                    }
                    @Override public void onFailure(Call<ApiResponse<ProfileData>> c,Throwable t){Toast.makeText(MainActivity.this,ApiErrorParser.networkMessage(t),Toast.LENGTH_LONG).show();}
                });
            }).show();
    }

    private void renderCreateInfo() {
        addEyebrow("CREATE");
        body.addView(text("Share something meaningful.",25,WHITE,true));
        LinearLayout c=panel(); c.setPadding(dp(18),dp(20),dp(18),dp(20)); body.addView(c,matchWrap(18));
        c.addView(text("Post creation is not connected yet",18,WHITE,true));
        c.addView(text("The current backend does not expose a posts upload endpoint. This screen stays an honest placeholder rather than pretending a post was published.",14,MUTED,false));
    }

    private void addEyebrow(String s) {
        TextView t=text(s,11,LAVENDER,true); t.setLetterSpacing(.18f); t.setPadding(0,dp(8),0,dp(10)); body.addView(t);
    }
    private LinearLayout panel() {
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(rounded(PANEL,20,Color.rgb(53,39,76))); return l;
    }
    private TextView text(String s,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private TextView action(String s) {
        TextView t=text(s,20,LAVENDER,true); t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(PANEL,16,Color.rgb(65,48,91)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(42),dp(42)); p.leftMargin=dp(7); t.setLayoutParams(p); return t;
    }
    private TextView button(String s) {
        TextView t=text(s,14,WHITE,true); t.setGravity(Gravity.CENTER); t.setPadding(dp(16),dp(12),dp(16),dp(12));
        t.setBackground(rounded(PURPLE,14,PURPLE)); return t;
    }
    private GradientDrawable rounded(int color,int radius,int stroke) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        d.setStroke(dp(1),stroke); return d;
    }
    private LinearLayout.LayoutParams matchWrap(int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); if(top>0)p.topMargin=dp(top); return p;
    }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private String initial(String s){return s==null||s.trim().isEmpty()?"L":s.trim().substring(0,1).toUpperCase();}

    private void loadMe() {
        ApiClient.get(this).me().enqueue(new Callback<ApiResponse<MeData>>() {
            @Override public void onResponse(Call<ApiResponse<MeData>> c,Response<ApiResponse<MeData>> r) {
                if(r.isSuccessful()&&r.body()!=null&&r.body().success&&r.body().data!=null&&r.body().data.user!=null) {
                    welcomeText.setText("Welcome, "+r.body().data.user.displayName);
                    usernameText.setText("@"+r.body().data.user.username);
                } else if(r.code()==401) goToLogin();
                else Toast.makeText(MainActivity.this,ApiErrorParser.message(r),Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ApiResponse<MeData>> c,Throwable t) {
                Toast.makeText(MainActivity.this,ApiErrorParser.networkMessage(t),Toast.LENGTH_LONG).show();
=======
/** Placeholder home: proves the session works. The feed arrives in the next slice. */
public class MainActivity extends AppCompatActivity {
    private TextView welcomeText, usernameText;
    private TokenStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = new TokenStore(this);
        welcomeText = findViewById(R.id.welcomeText);
        usernameText = findViewById(R.id.usernameText);

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { logout(); }
        });
        findViewById(R.id.securityButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountSecurityActivity.class));
            }
        });
        loadMe();
    }

    private void loadMe() {
        ApiClient.get(this).me().enqueue(new Callback<ApiResponse<MeData>>() {
            @Override
            public void onResponse(Call<ApiResponse<MeData>> call, Response<ApiResponse<MeData>> r) {
                if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                    welcomeText.setText("Welcome, " + r.body().data.user.displayName);
                    usernameText.setText("@" + r.body().data.user.username);
                } else if (r.code() == 401) {
                    goToLogin();           // session no longer valid
                } else {
                    Toast.makeText(MainActivity.this, ApiErrorParser.message(r), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MeData>> call, Throwable t) {
                Toast.makeText(MainActivity.this, ApiErrorParser.networkMessage(t), Toast.LENGTH_LONG).show();
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
            }
        });
    }

    private void logout() {
<<<<<<< HEAD
        String refresh=store.getRefreshToken(); store.clear();
        if(refresh!=null) ApiClient.get(this).logout(new RefreshRequest(refresh)).enqueue(new Callback<ApiResponse<Object>>() {
            @Override public void onResponse(Call<ApiResponse<Object>> c,Response<ApiResponse<Object>> r){}
            @Override public void onFailure(Call<ApiResponse<Object>> c,Throwable t){}
        });
        goToLogin();
    }
    private void goToLogin() {
        store.clear(); Intent i=new Intent(this,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(i);
=======
        String refresh = store.getRefreshToken();
        store.clear();
        if (refresh != null) {
            // Best effort: revoke the session on the server, then leave regardless of the result.
            ApiClient.get(this).logout(new RefreshRequest(refresh)).enqueue(new Callback<ApiResponse<Object>>() {
                @Override public void onResponse(Call<ApiResponse<Object>> c, Response<ApiResponse<Object>> r) { }
                @Override public void onFailure(Call<ApiResponse<Object>> c, Throwable t) { }
            });
        }
        goToLogin();
    }

    private void goToLogin() {
        store.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
    }
}

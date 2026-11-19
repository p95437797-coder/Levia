package com.levia.social.ui;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
=======
import android.os.Handler;
import android.os.Looper;
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e

import androidx.appcompat.app.AppCompatActivity;

import com.levia.social.R;
<<<<<<< HEAD
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.MeData;
import com.levia.social.net.ApiClient;
import com.levia.social.util.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private boolean routed = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View logo=findViewById(R.id.splashLogo);
        AlphaAnimation a=new AlphaAnimation(0f,1f); a.setDuration(650); logo.startAnimation(a);

        if (!new TokenStore(this).isLoggedIn()) { route(LoginActivity.class); return; }
        // Validate the saved session against the existing API before opening the app.
        ApiClient.get(this).me().enqueue(new Callback<ApiResponse<MeData>>() {
            @Override public void onResponse(Call<ApiResponse<MeData>> c, Response<ApiResponse<MeData>> r) {
                if (r.isSuccessful() && r.body()!=null && r.body().success) route(MainActivity.class);
                else { new TokenStore(SplashActivity.this).clear(); route(LoginActivity.class); }
            }
            @Override public void onFailure(Call<ApiResponse<MeData>> c, Throwable t) {
                // Keep an existing session usable when the service is temporarily unreachable.
                route(MainActivity.class);
            }
        });
    }

    private void route(Class<?> target) {
        if (routed || isFinishing()) return;
        routed=true;
        Intent i=new Intent(this,target);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
=======
import com.levia.social.util.TokenStore;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final boolean loggedIn = new TokenStore(this).isLoggedIn();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,
                        loggedIn ? MainActivity.class : LoginActivity.class));
                finish();
            }
        }, 1000);
>>>>>>> 3f88e13d33c3fc6e86f87fc1afc4a9b9ca0f4c0e
    }
}

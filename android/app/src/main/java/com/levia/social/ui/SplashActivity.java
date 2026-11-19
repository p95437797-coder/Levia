package com.levia.social.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.levia.social.R;
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
    }
}

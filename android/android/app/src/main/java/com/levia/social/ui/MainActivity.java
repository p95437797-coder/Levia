package com.levia.social.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.levia.social.R;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.MeData;
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            }
        });
    }

    private void logout() {
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
    }
}

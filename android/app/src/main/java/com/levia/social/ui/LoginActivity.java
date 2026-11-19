package com.levia.social.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.levia.social.R;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.AuthData;
import com.levia.social.data.model.LoginRequest;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progress;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.progress);
        errorText = findViewById(R.id.errorText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { attemptLogin(); }
        });
        findViewById(R.id.goForgot).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
        findViewById(R.id.goRegister).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void attemptLogin() {
        String email = String.valueOf(emailInput.getText()).trim();
        String password = String.valueOf(passwordInput.getText());
        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter your email and password.");
            return;
        }
        setLoading(true);
        ApiClient.get(this).login(new LoginRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> r) {
                        setLoading(false);
                        if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                            new TokenStore(LoginActivity.this).saveTokens(r.body().data.tokens);
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        } else {
                            showError(ApiErrorParser.message(r));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        setLoading(false);
                        showError(ApiErrorParser.networkMessage(t));
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        if (loading) errorText.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        errorText.setText(msg);
        errorText.setVisibility(View.VISIBLE);
    }
}

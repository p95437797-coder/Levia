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
import com.levia.social.data.model.RegisterRequest;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText displayNameInput, usernameInput, emailInput, passwordInput;
    private MaterialButton registerButton;
    private ProgressBar progress;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        displayNameInput = findViewById(R.id.displayNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.progress);
        errorText = findViewById(R.id.errorText);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { attemptRegister(); }
        });
        findViewById(R.id.goLogin).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
    }

    private void attemptRegister() {
        String name = String.valueOf(displayNameInput.getText()).trim();
        String username = String.valueOf(usernameInput.getText()).trim().toLowerCase(Locale.ROOT);
        String email = String.valueOf(emailInput.getText()).trim();
        String password = String.valueOf(passwordInput.getText());

        if (name.isEmpty()) { showError("Enter a display name."); return; }
        if (!username.matches("^[a-z0-9_.]{3,30}$")) {
            showError("Username must be 3-30 characters: letters, numbers, _ or .");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Enter a valid email address.");
            return;
        }
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            showError("Password needs 8+ characters with at least one letter and one number.");
            return;
        }

        setLoading(true);
        ApiClient.get(this).register(new RegisterRequest(email, username, name, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> r) {
                        setLoading(false);
                        if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                            new TokenStore(RegisterActivity.this).saveTokens(r.body().data.tokens);
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
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
        registerButton.setEnabled(!loading);
        if (loading) errorText.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        errorText.setText(msg);
        errorText.setVisibility(View.VISIBLE);
    }
}

package com.levia.social.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.levia.social.R;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.ChangeEmailRequest;
import com.levia.social.data.model.ChangePasswordRequest;
import com.levia.social.data.model.EmailData;
import com.levia.social.data.model.PasswordRequest;
import com.levia.social.data.model.SessionItem;
import com.levia.social.data.model.SessionsData;
import com.levia.social.net.ApiClient;
import com.levia.social.net.ApiErrorParser;
import com.levia.social.util.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Account security: change password/email, manage login sessions, log out everywhere,
 * deactivate or delete the account. Layout and the Retrofit contract are described in docs/PHASE2.md.
 */
public class AccountSecurityActivity extends AppCompatActivity {

    private TextInputEditText currentPasswordInput, newPasswordInput, emailPasswordInput, newEmailInput;
    private TextView errorText, sessionsEmptyText;
    private View sessionsProgress;
    private LinearLayout sessionsContainer;

    /** Callback that routes every failure to the inline error line. */
    private abstract class ApiCb<T> implements Callback<ApiResponse<T>> {
        abstract void onOk(T data);

        @Override
        public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> r) {
            if (r.isSuccessful() && r.body() != null && r.body().success) onOk(r.body().data);
            else showError(ApiErrorParser.message(r));
        }

        @Override
        public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
            showError(ApiErrorParser.networkMessage(t));
        }
    }

    private interface PasswordAction { void run(String password); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_security);

        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        emailPasswordInput = findViewById(R.id.emailPasswordInput);
        newEmailInput = findViewById(R.id.newEmailInput);
        errorText = findViewById(R.id.errorText);
        sessionsEmptyText = findViewById(R.id.sessionsEmptyText);
        sessionsProgress = findViewById(R.id.sessionsProgress);
        sessionsContainer = findViewById(R.id.sessionsContainer);

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        findViewById(R.id.changePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changePassword(); }
        });
        findViewById(R.id.changeEmailButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeEmail(); }
        });
        findViewById(R.id.logoutAllButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmLogoutAll(); }
        });
        findViewById(R.id.deactivateButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmDeactivate(); }
        });
        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmDelete(); }
        });

        loadSessions();
    }

    // ---------- change password / email ----------

    private void changePassword() {
        hideError();
        String current = String.valueOf(currentPasswordInput.getText());
        String next = String.valueOf(newPasswordInput.getText());
        if (current.isEmpty() || next.isEmpty()) { showError(getString(R.string.as_fill_fields)); return; }
        if (!isStrongEnough(next)) { showError(getString(R.string.as_weak_password)); return; }
        // The server revokes every session on a password change, so we must sign in again.
        ApiClient.get(this).changePassword(new ChangePasswordRequest(current, next))
                .enqueue(new ApiCb<Object>() {
                    @Override void onOk(Object data) {
                        AuthFlow.signOut(AccountSecurityActivity.this, getString(R.string.as_password_changed));
                    }
                });
    }

    private void changeEmail() {
        hideError();
        String password = String.valueOf(emailPasswordInput.getText());
        String email = String.valueOf(newEmailInput.getText()).trim();
        if (password.isEmpty() || email.isEmpty()) { showError(getString(R.string.as_fill_fields)); return; }
        ApiClient.get(this).changeEmail(new ChangeEmailRequest(password, email))
                .enqueue(new ApiCb<EmailData>() {
                    @Override void onOk(EmailData data) {
                        emailPasswordInput.setText("");
                        newEmailInput.setText("");
                        Toast.makeText(AccountSecurityActivity.this, R.string.as_email_updated, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------- sessions ----------

    private void loadSessions() {
        sessionsProgress.setVisibility(View.VISIBLE);
        sessionsEmptyText.setVisibility(View.GONE);
        sessionsContainer.removeAllViews();
        String refresh = new TokenStore(this).getRefreshToken();
        ApiClient.get(this).sessions(refresh).enqueue(new Callback<ApiResponse<SessionsData>>() {
            @Override
            public void onResponse(Call<ApiResponse<SessionsData>> call, Response<ApiResponse<SessionsData>> r) {
                sessionsProgress.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                    bindSessions(r.body().data);
                } else {
                    showError(ApiErrorParser.message(r));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<SessionsData>> call, Throwable t) {
                sessionsProgress.setVisibility(View.GONE);
                showError(ApiErrorParser.networkMessage(t));
            }
        });
    }

    private void bindSessions(SessionsData data) {
        sessionsContainer.removeAllViews();
        if (data.sessions == null || data.sessions.isEmpty()) {
            sessionsEmptyText.setVisibility(View.VISIBLE);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (final SessionItem s : data.sessions) {
            View row = inflater.inflate(R.layout.item_session, sessionsContainer, false);
            ((TextView) row.findViewById(R.id.sessionTitle)).setText(deviceName(s.deviceInfo));
            ((TextView) row.findViewById(R.id.sessionSubtitle))
                    .setText(getString(R.string.as_last_active, shortTime(s.lastActiveAt)));
            row.findViewById(R.id.sessionCurrentLabel).setVisibility(s.isCurrent ? View.VISIBLE : View.GONE);
            row.findViewById(R.id.sessionLogoutButton).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { revoke(s); }
            });
            sessionsContainer.addView(row);
        }
    }

    private void revoke(final SessionItem s) {
        hideError();
        ApiClient.get(this).revokeSession(s.id).enqueue(new ApiCb<Object>() {
            @Override void onOk(Object data) {
                if (s.isCurrent) AuthFlow.signOut(AccountSecurityActivity.this, getString(R.string.as_signed_out));
                else loadSessions();
            }
        });
    }

    private String deviceName(String ua) {
        if (ua == null || ua.isEmpty()) return getString(R.string.as_unknown_device);
        return ua.toLowerCase().contains("okhttp") ? getString(R.string.as_app_device)
                : getString(R.string.as_other_device);
    }

    /** "2026-11-19T05:58:20.896Z" -> "2026-11-19 05:58 UTC" (no java.time: minSdk 21). */
    private static String shortTime(String iso) {
        if (iso == null) return "";
        String s = iso.replace('T', ' ');
        if (s.length() > 16) s = s.substring(0, 16);
        return s + " UTC";
    }

    // ---------- sensitive actions ----------

    private void confirmLogoutAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.as_logout_all_title)
                .setMessage(R.string.as_logout_all_msg)
                .setNegativeButton(R.string.as_cancel, null)
                .setPositiveButton(R.string.as_confirm, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        ApiClient.get(AccountSecurityActivity.this).logoutAll().enqueue(new ApiCb<Object>() {
                            @Override void onOk(Object data) {
                                AuthFlow.signOut(AccountSecurityActivity.this, getString(R.string.as_signed_out));
                            }
                        });
                    }
                }).show();
    }

    private void confirmDeactivate() {
        askPassword(R.string.as_deactivate_title, R.string.as_deactivate_msg, new PasswordAction() {
            @Override public void run(String password) {
                ApiClient.get(AccountSecurityActivity.this).deactivate(new PasswordRequest(password))
                        .enqueue(new ApiCb<Object>() {
                            @Override void onOk(Object data) {
                                AuthFlow.signOut(AccountSecurityActivity.this, getString(R.string.as_deactivated));
                            }
                        });
            }
        });
    }

    private void confirmDelete() {
        askPassword(R.string.as_delete_title, R.string.as_delete_msg, new PasswordAction() {
            @Override public void run(String password) {
                ApiClient.get(AccountSecurityActivity.this).deleteAccount(new PasswordRequest(password))
                        .enqueue(new ApiCb<Object>() {
                            @Override void onOk(Object data) {
                                AuthFlow.signOut(AccountSecurityActivity.this, getString(R.string.as_deleted));
                            }
                        });
            }
        });
    }

    /** Confirmation dialog that also collects the current password. */
    private void askPassword(int titleRes, int messageRes, final PasswordAction action) {
        hideError();
        final EditText input = new EditText(this);
        input.setHint(R.string.as_password_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout box = new LinearLayout(this);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setView(box)
                .setNegativeButton(R.string.as_cancel, null)
                .setPositiveButton(R.string.as_confirm, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        String password = input.getText().toString();
                        if (TextUtils.isEmpty(password)) { showError(getString(R.string.as_fill_fields)); return; }
                        action.run(password);
                    }
                }).show();
    }

    // ---------- helpers ----------

    /** Mirrors the server policy: 8-72 characters with at least one letter and one digit. */
    private static boolean isStrongEnough(String p) {
        if (p.length() < 8 || p.length() > 72) return false;
        boolean letter = false, digit = false;
        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (Character.isLetter(c)) letter = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return letter && digit;
    }

    private void showError(String msg) {
        errorText.setText(msg);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}

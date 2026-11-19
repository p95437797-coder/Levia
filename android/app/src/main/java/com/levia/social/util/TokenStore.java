package com.levia.social.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.levia.social.data.model.Tokens;

/**
 * Keeps the login session in the app's private storage.
 * Note: plain SharedPreferences for now; hardening with Android Keystore is a planned task.
 */
public class TokenStore {
    private static final String FILE = "levia_session";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public void saveTokens(Tokens t) {
        prefs.edit().putString(KEY_ACCESS, t.accessToken).putString(KEY_REFRESH, t.refreshToken).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getRefreshToken() != null;
    }
}

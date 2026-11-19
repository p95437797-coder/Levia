package com.levia.social.ui;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.levia.social.util.TokenStore;

/** Shared helper: after the server revokes the session, wipe local tokens and return to Login. */
public final class AuthFlow {
    private AuthFlow() {}

    public static void signOut(Activity activity, String message) {
        new TokenStore(activity).clear();
        if (message != null) Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}

package com.levia.social.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;

public final class ApiErrorParser {
    private ApiErrorParser() {}

    /** Turns a failed HTTP response into a message the user can read. */
    public static String message(Response<?> r) {
        try {
            if (r.errorBody() != null) {
                JsonElement root = new JsonParser().parse(r.errorBody().string());
                if (root.isJsonObject()) {
                    JsonObject err = root.getAsJsonObject().getAsJsonObject("error");
                    if (err != null && err.has("message")) return err.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
            // fall through to the generic message
        }
        return "Something went wrong (HTTP " + r.code() + ")";
    }

    public static String networkMessage(Throwable t) {
        return "Cannot reach the server. Check your internet and try again.";
    }
}

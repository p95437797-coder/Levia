package com.levia.social.net;

import android.content.Context;

import com.levia.social.BuildConfig;
import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.data.model.Tokens;
import com.levia.social.util.TokenStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Builds the single Retrofit client: adds the access token and refreshes it automatically on 401. */
public final class ApiClient {
    private static volatile ApiService service;

    private ApiClient() {}

    public static ApiService get(Context context) {
        if (service == null) {
            synchronized (ApiClient.class) {
                if (service == null) {
                    service = build(context.getApplicationContext());
                }
            }
        }
        return service;
    }

    // 60 s timeouts: the free Render server can take ~50 s to wake up.
    private static OkHttpClient.Builder baseClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
    }

    private static Retrofit.Builder retrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client);
    }

    private static ApiService build(Context appContext) {
        final TokenStore store = new TokenStore(appContext);
        // Plain client (no authenticator) used only for the refresh call, to avoid loops.
        final ApiService refreshService = retrofit(baseClient().build()).build().create(ApiService.class);

        OkHttpClient client = baseClient()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request req = chain.request();
                        String token = store.getAccessToken();
                        if (token != null && req.header("Authorization") == null) {
                            req = req.newBuilder().header("Authorization", "Bearer " + token).build();
                        }
                        return chain.proceed(req);
                    }
                })
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String sent = response.request().header("Authorization");
                        if (sent == null) return null;            // e.g. wrong password on login: not a session problem
                        if (responseCount(response) >= 2) return null;
                        synchronized (ApiClient.class) {
                            String current = store.getAccessToken();
                            if (current != null && !("Bearer " + current).equals(sent)) {
                                // Another request already refreshed the token.
                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + current).build();
                            }
                            String refresh = store.getRefreshToken();
                            if (refresh == null) return null;
                            retrofit2.Response<ApiResponse<Tokens>> r =
                                    refreshService.refresh(new RefreshRequest(refresh)).execute();
                            if (r.isSuccessful() && r.body() != null && r.body().success && r.body().data != null) {
                                store.saveTokens(r.body().data);
                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + r.body().data.accessToken).build();
                            }
                            if (r.code() == 401 || r.code() == 403) store.clear();
                            return null;
                        }
                    }
                })
                .build();
        return retrofit(client).build().create(ApiService.class);
    }

    private static int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) count++;
        return count;
    }
}

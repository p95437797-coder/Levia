package com.levia.social.net;

import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.AuthData;
import com.levia.social.data.model.LoginRequest;
import com.levia.social.data.model.MeData;
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.data.model.RegisterRequest;
import com.levia.social.data.model.Tokens;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/v1/auth/register")
    Call<ApiResponse<AuthData>> register(@Body RegisterRequest body);

    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthData>> login(@Body LoginRequest body);

    @POST("api/v1/auth/refresh")
    Call<ApiResponse<Tokens>> refresh(@Body RefreshRequest body);

    @POST("api/v1/auth/logout")
    Call<ApiResponse<Object>> logout(@Body RefreshRequest body);

    @GET("api/v1/me")
    Call<ApiResponse<MeData>> me();
}

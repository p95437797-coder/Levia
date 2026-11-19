package com.levia.social.net;

import com.levia.social.data.model.ApiResponse;
import com.levia.social.data.model.AuthData;
import com.levia.social.data.model.ChangeEmailRequest;
import com.levia.social.data.model.ChangePasswordRequest;
import com.levia.social.data.model.EmailData;
import com.levia.social.data.model.ForgotPasswordRequest;
import com.levia.social.data.model.LoginRequest;
import com.levia.social.data.model.MeData;
import com.levia.social.data.model.PasswordRequest;
import com.levia.social.data.model.RefreshRequest;
import com.levia.social.data.model.RegisterRequest;
import com.levia.social.data.model.ResetPasswordRequest;
import com.levia.social.data.model.SessionsData;
import com.levia.social.data.model.Tokens;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    @POST("api/v1/auth/forgot-password")
    Call<ApiResponse<Object>> forgotPassword(@Body ForgotPasswordRequest body);

    @POST("api/v1/auth/reset-password")
    Call<ApiResponse<Object>> resetPassword(@Body ResetPasswordRequest body);

    @POST("api/v1/auth/change-password")
    Call<ApiResponse<Object>> changePassword(@Body ChangePasswordRequest body);

    @POST("api/v1/auth/logout-all")
    Call<ApiResponse<Object>> logoutAll();

    @GET("api/v1/account/sessions")
    Call<ApiResponse<SessionsData>> sessions(@Header("x-refresh-token") String refreshToken);

    @DELETE("api/v1/account/sessions/{id}")
    Call<ApiResponse<Object>> revokeSession(@Path("id") String id);

    @PATCH("api/v1/account/email")
    Call<ApiResponse<EmailData>> changeEmail(@Body ChangeEmailRequest body);

    @POST("api/v1/account/deactivate")
    Call<ApiResponse<Object>> deactivate(@Body PasswordRequest body);

    @POST("api/v1/account/delete")
    Call<ApiResponse<Object>> deleteAccount(@Body PasswordRequest body);
}

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
import com.levia.social.data.model.ProfileData;
import com.levia.social.data.model.SearchUser;
import com.levia.social.data.model.FollowResult;
import com.levia.social.data.model.ConnectionPage;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    @GET("api/v1/profile/me")
    Call<ApiResponse<ProfileData>> myProfile();

    @PATCH("api/v1/profile/me")
    Call<ApiResponse<ProfileData>> updateMyProfile(@Body Map<String, Object> fields);

    @GET("api/v1/users/search")
    Call<ApiResponse<List<SearchUser>>> searchUsers(@Query("q") String query, @Query("limit") int limit);

    @POST("api/v1/users/{userId}/follow")
    Call<ApiResponse<FollowResult>> followUser(@Path("userId") String userId);

    @DELETE("api/v1/users/{userId}/follow")
    Call<ApiResponse<FollowResult>> unfollowUser(@Path("userId") String userId);

    @GET("api/v1/users/{username}/followers")
    Call<ApiResponse<ConnectionPage>> followers(@Path("username") String username, @Query("limit") int limit);

    @GET("api/v1/users/{username}/following")
    Call<ApiResponse<ConnectionPage>> following(@Path("username") String username, @Query("limit") int limit);

}

package com.levia.social.data.model;

public class ApiResponse<T> {
    public boolean success;
    public T data;
    public ApiError error;
}

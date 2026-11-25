package com.wesker.order.result;

public record  Result<T>(int code, String msg, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(1, null, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(0, message, null);
    }
}

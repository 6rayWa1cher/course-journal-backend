package com.a6raywa1cher.coursejournalbackend.utils;

public final class CommonUtils {
    public static <T> T coalesce(T a, T b) {
        return a != null ? a : b;
    }

    public static <T> T coalesce(T a, T b, T c) {
        return a != null ? a : coalesce(b, c);
    }

    public static <T> T coalesce(T a, T b, T c, T d) {
        return a != null ? a : coalesce(b, c, d);
    }

    public static <T> T coalesce(T a, T b, T c, T d, T e) {
        return a != null ? a : coalesce(b, c, d, e);
    }
}

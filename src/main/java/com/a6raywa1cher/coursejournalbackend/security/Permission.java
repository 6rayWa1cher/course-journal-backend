package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.User;

public final class Permission {
    public static String getPermissionForCourse(long id, ActionType type) {
        return "COURSE_%s_%s".formatted(id, type);
    }

    public static String getPermissionForCourse(Course course, ActionType type) {
        return getPermissionForCourse(course.getId(), type);
    }

    public static String getPermissionForUser(long id, ActionType type) {
        return "USER_%s_%s".formatted(id, type);
    }

    public static String getPermissionForUser(User user, ActionType type) {
        return getPermissionForUser(user.getId(), type);
    }
}

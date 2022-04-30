package com.a6raywa1cher.coursejournalbackend.security;

import com.a6raywa1cher.coursejournalbackend.model.AuthUser;
import com.a6raywa1cher.coursejournalbackend.model.Course;
import com.a6raywa1cher.coursejournalbackend.model.Employee;
import com.a6raywa1cher.coursejournalbackend.model.Group;

public final class Permission {
    public static String getPermissionForCourse(long id, ActionType type) {
        return "COURSE_%s_%s".formatted(id, type);
    }

    public static String getPermissionForCourse(Course course, ActionType type) {
        return getPermissionForCourse(course.getId(), type);
    }

    public static String getPermissionForEmployee(long id, ActionType type) {
        return "EMPLOYEE_%s_%s".formatted(id, type);
    }

    public static String getPermissionForEmployee(Employee employee, ActionType type) {
        return getPermissionForEmployee(employee.getId(), type);
    }

    public static String getPermissionForAuthUser(long id, ActionType type) {
        return "AUTHUSER_%s_%s".formatted(id, type);
    }

    public static String getPermissionForAuthUser(AuthUser authUser, ActionType type) {
        return getPermissionForAuthUser(authUser.getId(), type);
    }

    public static String getPermissionForGroup(long id, ActionType type) {
        return "GROUP_%s_%s".formatted(id, type);
    }

    public static String getPermissionForGroup(Group group, ActionType type) {
        return getPermissionForGroup(group.getId(), type);
    }
}

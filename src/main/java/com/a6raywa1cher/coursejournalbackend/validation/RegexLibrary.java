package com.a6raywa1cher.coursejournalbackend.validation;

public final class RegexLibrary {
    public static final String USERNAME = "^[a-zA-Z0-9.]{5,25}$";

    public static final String COMMON_NAME = "^.{1,250}$";

    public static final String CRITERIA_NAME = "^[^$]{1,150}$";
}

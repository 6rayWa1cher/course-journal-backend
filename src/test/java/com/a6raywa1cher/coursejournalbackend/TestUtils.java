package com.a6raywa1cher.coursejournalbackend;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;

public final class TestUtils {
    public static String basic(String login, String password) {
        String encoded = Base64.getEncoder().encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    public static String jwt(String accessToken) {
        return "Bearer " + accessToken;
    }

    public static final class DateMatcher extends BaseMatcher<String> {
        private final ZonedDateTime date;

        public DateMatcher(ZonedDateTime date) {
            this.date = date;
        }

        @Override
        public boolean matches(Object actual) {
            return ZonedDateTime.parse((String) actual).isEqual(date);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(date);
        }
    }
}

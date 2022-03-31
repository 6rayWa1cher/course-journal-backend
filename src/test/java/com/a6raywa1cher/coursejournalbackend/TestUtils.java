package com.a6raywa1cher.coursejournalbackend;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    public static String ctbearer(String accessToken) {
        return "ctbearer " + accessToken;
    }

    public static long getIdFromResult(MvcResult mvcResult) throws Exception {
        return JsonPath.<Integer>read(mvcResult.getResponse().getContentAsString(), "$.id");
    }

    public static final class DateMatcher extends BaseMatcher<String> {
        private final ZonedDateTime date;

        public DateMatcher(ZonedDateTime date) {
            this.date = date;
        }

        @Override
        public boolean matches(Object actual) {
            return Duration.between(date, ZonedDateTime.parse((String) actual)).toMillis() < 1;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(date);
        }
    }

    public static final class GreaterThanMatcher extends BaseMatcher<Integer> {
        private final int value;

        public GreaterThanMatcher(int value) {
            this.value = value;
        }

        @Override
        public boolean matches(Object actual) {
            return ((int) actual) > value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(value);
        }
    }

    public static final class NotEqualsMatcher extends BaseMatcher<Integer> {
        private final int value;

        public NotEqualsMatcher(int value) {
            this.value = value;
        }

        @Override
        public boolean matches(Object actual) {
            return ((int) actual) != value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(value);
        }
    }
}

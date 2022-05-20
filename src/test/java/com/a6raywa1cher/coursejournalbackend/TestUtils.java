package com.a6raywa1cher.coursejournalbackend;

import com.a6raywa1cher.coursejournalbackend.model.AttendanceType;
import com.a6raywa1cher.coursejournalbackend.model.UserRole;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class TestUtils {
    public static String basic(String login, String password) {
        String encoded = Base64.getEncoder().encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    public static String jwt(String accessToken) {
        return "Bearer " + accessToken;
    }

    public static AttendanceType randomAttendanceType() {
        int pick = new Random().nextInt(AttendanceType.values().length);
        return AttendanceType.values()[pick];
    }

    public static UserRole randomUserRole() {
        int pick = new Random().nextInt(UserRole.values().length);
        return UserRole.values()[pick];
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

    public static final class GreaterThanMatcher extends BaseMatcher<Double> {
        private final double value;

        public GreaterThanMatcher(double value) {
            this.value = value;
        }

        @Override
        public boolean matches(Object actual) {
            return ((double) actual) > value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(value);
        }
    }

    public static final class NotEqualsMatcher extends BaseMatcher<Double> {
        private final double value;

        public NotEqualsMatcher(double value) {
            this.value = value;
        }

        @Override
        public boolean matches(Object actual) {
            return Math.abs((double) actual - value) > 0.00001;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(value);
        }
    }

    public static final class ContainsAllIdsMatcher extends BaseMatcher<JSONArray> {
        private final Set<Integer> expected;

        public ContainsAllIdsMatcher(List<Long> expected) {
            this.expected = expected.stream()
                    .map(Math::toIntExact)
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean matches(Object actual) {
            Set<Integer> actualList = Arrays.stream(((JSONArray) actual).toArray())
                    .map(o -> (int) o)
                    .collect(Collectors.toSet());
            return actualList.equals(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(String.join(",", expected.stream().map(l -> Long.toString(l)).toList()));
        }
    }
}

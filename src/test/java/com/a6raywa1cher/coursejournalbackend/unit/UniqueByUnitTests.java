package com.a6raywa1cher.coursejournalbackend.unit;

import com.a6raywa1cher.coursejournalbackend.validation.UniqueBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.validation.annotation.Validated;

import javax.validation.*;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UniqueByUnitTests {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void singleKey__valid() {
        SingleKeyTestClass obj = new SingleKeyTestClass();
        obj.setList(List.of(new SingleKeyHolder(1), new SingleKeyHolder(2)));

        Set<ConstraintViolation<SingleKeyTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(0);
    }

    @Test
    void singleKey__overlap() {
        SingleKeyTestClass obj = new SingleKeyTestClass();
        obj.setList(List.of(new SingleKeyHolder(1), new SingleKeyHolder(2), new SingleKeyHolder(1)));

        Set<ConstraintViolation<SingleKeyTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("List contains intersected values");
    }

    @Test
    void doubleKey__valid() {
        DoubleKeyTestClass obj = new DoubleKeyTestClass();
        obj.setList(List.of(
                new DoubleKeyHolder(1, "a"),
                new DoubleKeyHolder(1, "b"),
                new DoubleKeyHolder(2, "a"),
                new DoubleKeyHolder(2, "b")
        ));

        Set<ConstraintViolation<DoubleKeyTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(0);
    }

    @Test
    void doubleKey__overlap() {
        DoubleKeyTestClass obj = new DoubleKeyTestClass();
        obj.setList(List.of(
                new DoubleKeyHolder(1, "a"),
                new DoubleKeyHolder(1, "b"),
                new DoubleKeyHolder(2, "a"),
                new DoubleKeyHolder(2, "b"),
                new DoubleKeyHolder(2, "b")
        ));

        Set<ConstraintViolation<DoubleKeyTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("List contains intersected values");
    }

    @Test
    void twoKeys__valid() {
        TwoKeysTestClass obj = new TwoKeysTestClass();
        obj.setList(List.of(
                new DoubleKeyHolder(1, "a"),
                new DoubleKeyHolder(2, "b")
        ));

        Set<ConstraintViolation<TwoKeysTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(0);
    }

    @Test
    void twoKeys__overlap() {
        TwoKeysTestClass obj = new TwoKeysTestClass();
        obj.setList(List.of(
                new DoubleKeyHolder(1, "a"),
                new DoubleKeyHolder(2, "b"),
                new DoubleKeyHolder(1, "b"),
                new DoubleKeyHolder(2, "a")
        ));

        Set<ConstraintViolation<TwoKeysTestClass>> violations = validator.validate(obj);

        assertThat(violations).hasSize(2);
        var iterator = violations.iterator();
        assertThat(iterator.next().getMessage()).isEqualTo("List contains intersected values");
        assertThat(iterator.next().getMessage()).isEqualTo("List contains intersected values");
    }

    @Test
    void combined_valid() {
        SingleKeyTestClass obj1 = new SingleKeyTestClass();
        obj1.setList(List.of(new SingleKeyHolder(1), new SingleKeyHolder(2)));

        TwoKeysTestClass obj2 = new TwoKeysTestClass();
        obj2.setList(List.of(
                new DoubleKeyHolder(1, "a"),
                new DoubleKeyHolder(2, "b")
        ));

        Set<ConstraintViolation<SingleKeyTestClass>> violations1 = validator.validate(obj1);

        assertThat(violations1).hasSize(0);

        Set<ConstraintViolation<TwoKeysTestClass>> violations2 = validator.validate(obj2);

        assertThat(violations2).hasSize(0);
    }

    @Data
    @AllArgsConstructor
    @Validated
    public static class SingleKeyHolder {
        private Integer key;
    }

    @Data
    @AllArgsConstructor
    @Validated
    public static class DoubleKeyHolder {
        private Integer key1;

        private String key2;
    }

    @Data
    public static class SingleKeyTestClass {
        @UniqueBy(fields = "key", clazz = SingleKeyHolder.class)
        @Valid
        private List<SingleKeyHolder> list;
    }

    @Data
    public static class DoubleKeyTestClass {
        @UniqueBy(fields = {"key1", "key2"}, clazz = DoubleKeyHolder.class)
        @Valid
        private List<DoubleKeyHolder> list;
    }

    @Data
    public static class TwoKeysTestClass {
        @UniqueBy(fields = "key1", clazz = DoubleKeyHolder.class)
        @UniqueBy(fields = "key2", clazz = DoubleKeyHolder.class)
        @Valid
        private List<DoubleKeyHolder> list;
    }
}

package com.a6raywa1cher.coursejournalbackend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

@Data
@Builder
@AllArgsConstructor
public class ObjectRequestContext<T, J> {
    private final T request;

    private final Function<String, ResultMatcher[]> matchersSupplier;

    private final J data;

    public ObjectRequestContext(T request) {
        this.request = request;
        this.matchersSupplier = prefix -> new ResultMatcher[0];
        this.data = null;
    }

    public ObjectRequestContext(T request, Function<String, ResultMatcher[]> matchersSupplier) {
        this.request = request;
        this.matchersSupplier = matchersSupplier;
        this.data = null;
    }

    public ObjectRequestContext(T request, J data) {
        this.request = request;
        this.matchersSupplier = prefix -> new ResultMatcher[0];
        this.data = data;
    }

    public ObjectRequestContext(T request, J data, Function<String, ResultMatcher[]> matchersSupplier) {
        this.request = request;
        this.matchersSupplier = matchersSupplier;
        this.data = data;
    }

    public ResultMatcher[] getMatchers() {
        return getMatchers("$");
    }

    public ResultMatcher[] getMatchers(String prefix) {
        return matchersSupplier.apply(prefix);
    }
}

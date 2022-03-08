package com.a6raywa1cher.coursejournalbackend;

import lombok.Data;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Data
public class RequestContext<T> {
    private final T request;

    private final Function<String, ResultMatcher[]> matchersSupplier;

    private final Map<String, String> data;

    public RequestContext(T request) {
        this.request = request;
        this.matchersSupplier = prefix -> new ResultMatcher[0];
        this.data = Collections.emptyMap();
    }

    public RequestContext(T request, Function<String, ResultMatcher[]> matchersSupplier) {
        this.request = request;
        this.matchersSupplier = matchersSupplier;
        this.data = Collections.emptyMap();
    }

    public RequestContext(T request, Map<String, String> data) {
        this.request = request;
        this.matchersSupplier = prefix -> new ResultMatcher[0];
        this.data = data;
    }

    public RequestContext(T request, Map<String, String> data, Function<String, ResultMatcher[]> matchersSupplier) {
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

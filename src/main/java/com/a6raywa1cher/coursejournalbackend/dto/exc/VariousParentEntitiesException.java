package com.a6raywa1cher.coursejournalbackend.dto.exc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VariousParentEntitiesException extends RuntimeException {
    public VariousParentEntitiesException(Map<Long, List<Long>> sourceMap) {
        super("Required a single parent, got: " + toSourceString(sourceMap));
    }

    public VariousParentEntitiesException(List<Long> sourceList) {
        super("Required a single parent, got: " +
                String.join(",", sourceList.stream().map(id -> Long.toString(id)).toList())
        );
    }

    private static String toSourceString(Map<Long, List<Long>> sourceMap) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var entry : sourceMap.entrySet()) {
            if (!first) sb.append(", ");
            first = false;

            sb.append(entry.getKey()).append(" (");
            sb.append(String.join(",", entry.getValue().stream().map(id -> Long.toString(id)).toList()));
            sb.append(")");
        }
        return sb.toString();
    }
}

package com.a6raywa1cher.coursejournalbackend.validation;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class UniqueByValidator implements ConstraintValidator<UniqueBy, List<?>> {
    private final Map<GetterCacheKey, List<Method>> gettersCache = new HashMap<>();
    private List<Method> getters;
    private Class<?> typeClass;

    private List<Method> getGettersForClass(GetterCacheKey key) {
        return getGettersForClass(key.typeClass(), key.fields());
    }

    private List<Method> getGettersForClass(Class<?> typeClass, List<String> fields) {
        return fields.stream()
                .map(field -> Objects.requireNonNull(
                        BeanUtils.getPropertyDescriptor(typeClass, field),
                        () -> "Field %s doesn't exist for class %s or doesn't have a public getter".formatted(field, typeClass.getName())
                ))
                .map(PropertyDescriptor::getReadMethod)
                .toList();
    }

    @Override
    public void initialize(UniqueBy constraintAnnotation) {
        typeClass = constraintAnnotation.clazz();
        List<String> fields = Arrays.asList(constraintAnnotation.fields());
        GetterCacheKey key = new GetterCacheKey(typeClass, Collections.unmodifiableList(fields));
        getters = gettersCache.computeIfAbsent(key, this::getGettersForClass);
    }

    private List<Object> getKeysForObject(Object o) {
        Assert.isAssignable(typeClass, o.getClass());
        List<Object> keys = new ArrayList<>(getters.size());
        try {
            for (Method getter : getters) {
                Object key = getter.invoke(o);
                keys.add(key);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        List<List<Object>> keysForEveryObject = value.stream()
                .filter(Objects::nonNull)
                .map(this::getKeysForObject)
                .toList();
        Set<List<Object>> uniqueKeysForEveryObject = new HashSet<>(keysForEveryObject);
        return keysForEveryObject.size() == uniqueKeysForEveryObject.size();
    }

    private record GetterCacheKey(Class<?> typeClass, List<String> fields) {
    }
}

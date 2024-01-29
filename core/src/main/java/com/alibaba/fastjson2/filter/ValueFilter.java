package com.alibaba.fastjson2.filter;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ValueFilter
        extends Filter {
    Object apply(Object object, String name, Object value);

    static ValueFilter compose(ValueFilter before, ValueFilter after) {
        return (object, name, value) ->
                after.apply(
                        object,
                        name,
                        before.apply(object, name, value)
                );
    }

    static ValueFilter of(final String name, Function function) {
        return (object, fieldName, fieldValue)
                -> name == null || name.equals(fieldName)
                ? function.apply(fieldValue)
                : fieldValue;
    }

    static ValueFilter of(final String name, Map map) {
        return (object, fieldName, fieldValue) -> {
            if (name == null || name.equals(fieldName)) {
                Object o = map.get(fieldValue);
                if (o != null || map.containsKey(fieldValue)) {
                    return o;
                }
            }
            return fieldValue;
        };
    }

    static ValueFilter of(Predicate<String> nameMatcher, Function function) {
        return (object, fieldName, fieldValue)
                -> nameMatcher == null || nameMatcher.test(fieldName)
                ? function.apply(fieldValue)
                : fieldValue;
    }
}

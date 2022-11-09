package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.util.BeanUtils;

import java.util.function.Function;

public interface NameFilter
        extends Filter {
    String process(Object object, String name, Object value);

    static NameFilter of(PropertyNamingStrategy namingStrategy) {
        return (object, name, value) -> BeanUtils.fieldName(name, namingStrategy.name());
    }

    static NameFilter compose(NameFilter before, NameFilter after) {
        return (object, name, value) ->
                after.process(
                        object,
                        before.process(object, name, value),
                        value
                );
    }

    static NameFilter of(Function<String, String> function) {
        return (object, name, value) -> function.apply(name);
    }
}

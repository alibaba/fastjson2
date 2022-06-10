package com.alibaba.fastjson2.filter;

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
}

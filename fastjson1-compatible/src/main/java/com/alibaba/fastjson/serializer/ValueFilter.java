package com.alibaba.fastjson.serializer;

public interface ValueFilter
        extends SerializeFilter, com.alibaba.fastjson2.filter.ValueFilter {
    Object process(Object object, String name, Object value);

    @Override
    default Object apply(Object object, String name, Object value) {
        return process(object, name, value);
    }

    static ValueFilter compose(ValueFilter before, ValueFilter after) {
        return (object, name, value) ->
                after.process(
                        object,
                        name,
                        before.process(object, name, value)
                );
    }
}

package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;

public interface PropertyPreFilter
        extends com.alibaba.fastjson2.filter.PropertyPreFilter, SerializeFilter {
    default boolean process(JSONWriter writer, Object source, String name) {
        JSONSerializer serializer = JSONSerializer.getJSONSerializer(writer);
        serializer.setContext(new SerialContext(writer, serializer.context, source, name, 0, 0));
        return apply(serializer, source, name);
    }

    boolean apply(JSONSerializer serializer, Object object, String name);
}

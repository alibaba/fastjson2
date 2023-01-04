package com.alibaba.fastjson2.adapter.jackson.databind.jsontype;

import com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeInfo;
import com.alibaba.fastjson2.adapter.jackson.databind.DatabindContext;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;

public interface TypeIdResolver {
    String idFromValue(Object value);

    String idFromValueAndType(Object value, Class<?> suggestedType);

    JavaType typeFromId(DatabindContext context, String id);

    JsonTypeInfo.Id getMechanism();
}

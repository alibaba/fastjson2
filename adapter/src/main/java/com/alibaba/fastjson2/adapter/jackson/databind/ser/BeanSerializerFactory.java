package com.alibaba.fastjson2.adapter.jackson.databind.ser;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonMappingException;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer;
import com.alibaba.fastjson2.adapter.jackson.databind.SerializerProvider;

public class BeanSerializerFactory {
    public static final BeanSerializerFactory instance = new BeanSerializerFactory();

    public JsonSerializer<Object> createSerializer(
            SerializerProvider prov,
            JavaType origType
    ) throws JsonMappingException {
        throw new JSONException("TODO");
    }
}

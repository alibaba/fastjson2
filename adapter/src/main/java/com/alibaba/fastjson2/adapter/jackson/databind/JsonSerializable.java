package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

public interface JsonSerializable {
    void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException;

    void serializeWithType(
            JsonGenerator gen,
            SerializerProvider serializers,
            TypeSerializer typeSer
    ) throws IOException;
}

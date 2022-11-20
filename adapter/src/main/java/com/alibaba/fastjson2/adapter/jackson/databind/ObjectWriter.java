package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectWriter {
    final SerializationConfig config;
    final Type objectType;
    final Class objectClass;
    final JsonFactory jsonFactory;

    public ObjectWriter(
            ObjectMapper mapper,
            SerializationConfig config,
            Type objectType
    ) {
        this.config = config;
        this.objectType = objectType;
        this.objectClass = TypeUtils.getClass(objectType);
        this.jsonFactory = mapper.factory;
    }

    public String writeValueAsString(Object value)
            throws IOException {
        JSONWriter jsonWriter = jsonFactory.createJSONWriter();
        JSONWriter.Context context = jsonWriter.getContext();
        context.getObjectWriter(objectType, objectClass)
                .write(jsonWriter, value, null, null, 0L);
        return jsonWriter.toString();
    }
}

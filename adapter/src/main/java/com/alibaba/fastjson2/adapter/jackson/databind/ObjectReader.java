package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectReader {
    final ObjectMapper mapper;
    final DeserializationConfig config;
    final Type objectType;
    final Class objectClass;
    final JsonFactory jsonFactory;

    public ObjectReader(ObjectMapper mapper, DeserializationConfig config, Type objectType) {
        this.mapper = mapper;
        this.config = config;
        this.objectType = objectType;
        this.objectClass = TypeUtils.getClass(objectType);
        this.jsonFactory = mapper.factory;
    }

    public <T> T readValue(String src, Class<T> valueType) throws IOException {
        JSONReader jsonReader = this.jsonFactory.createJSONReader(src);
        JSONReader.Context context = jsonReader.getContext();
        com.alibaba.fastjson2.reader.ObjectReader objectReader = context.getObjectReader(objectType);

        if (jsonReader.isCSV()) {
            return (T) objectReader.readFromCSV(jsonReader, null, null, 0L);
        }

        Object object = objectReader
                .readObject(jsonReader, null, null, 0L);
        return (T) object;
    }
}

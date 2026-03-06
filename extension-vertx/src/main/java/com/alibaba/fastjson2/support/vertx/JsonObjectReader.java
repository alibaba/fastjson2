package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonObjectReader implements ObjectReader<JsonObject> {
    public static final JsonObjectReader INSTANCE = new JsonObjectReader();

    @Override
    public JsonObject readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Map<String, Object> map = jsonReader.read(Map.class);
        return new JsonObject(map);
    }
}

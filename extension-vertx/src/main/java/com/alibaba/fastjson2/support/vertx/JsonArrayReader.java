package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import io.vertx.core.json.JsonArray;

import java.lang.reflect.Type;
import java.util.List;

public class JsonArrayReader implements ObjectReader<JsonArray> {

    public static final JsonArrayReader INSTANCE = new JsonArrayReader();

    @Override
    public JsonArray readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        List<Object> list = jsonReader.read(List.class);
        return new JsonArray(list);
    }
}

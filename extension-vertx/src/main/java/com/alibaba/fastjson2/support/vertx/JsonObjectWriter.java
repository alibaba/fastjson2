package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Type;

public class JsonObjectWriter implements ObjectWriter<JsonObject> {

    public static final JsonObjectWriter INSTANCE = new JsonObjectWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JsonObject jsonObject = (JsonObject) object;
        jsonWriter.write(jsonObject.getMap());
    }
}

package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import io.vertx.core.json.JsonArray;

import java.lang.reflect.Type;

public class JsonArrayWriter implements ObjectWriter<JsonArray> {
    public static final JsonArrayWriter INSTANCE = new JsonArrayWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JsonArray jsonArray = (JsonArray) object;
        jsonWriter.write(jsonArray.getList());
    }
}

package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.Map;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserCompatible;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringKeyAsString;

final class ObjectWriterImplMapEntry
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplMapEntry INSTANCE = new ObjectWriterImplMapEntry();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Map.Entry entry = (Map.Entry) object;
        if (entry == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.startArray(2);
        Object key = entry.getKey();
        long contextFeatures = jsonWriter.context.getFeatures();
        if ((contextFeatures & (WriteNonStringKeyAsString.mask | BrowserCompatible.mask)) != 0) {
            jsonWriter.writeAny(key == null ? "null" : key.toString());
        } else {
            jsonWriter.writeAny(key);
        }
        jsonWriter.writeAny(entry.getValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Map.Entry entry = (Map.Entry) object;
        if (entry == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.startObject();
        Object key = entry.getKey();
        long contextFeatures = jsonWriter.context.getFeatures();
        if ((contextFeatures & (WriteNonStringKeyAsString.mask | BrowserCompatible.mask)) != 0) {
            jsonWriter.writeAny(key == null ? "null" : key.toString());
        } else {
            jsonWriter.writeAny(key);
        }
        jsonWriter.writeColon();
        jsonWriter.writeAny(entry.getValue());
        jsonWriter.endObject();
    }
}

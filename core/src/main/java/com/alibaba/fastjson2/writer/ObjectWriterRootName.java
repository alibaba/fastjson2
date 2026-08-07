package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

public final class ObjectWriterRootName<T>
        extends ObjectWriterAdapter<T> {
    final String rootName;
    public ObjectWriterRootName(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            String rootName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        super(objectClass, typeKey, typeName, features, fieldWriters);
        this.rootName = rootName;
    }

    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        jsonWriter.writeName(rootName);
        super.writeJSONB(jsonWriter, object, fieldName, fieldType, features);
        jsonWriter.endObject();
    }

    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        jsonWriter.writeName(rootName);
        jsonWriter.writeColon();
        super.write(jsonWriter, object, fieldName, fieldType, features);
        jsonWriter.endObject();
    }

    public JSONObject toJSONObject(T object, long features) {
        return JSONObject.of(
                rootName,
                super.toJSONObject(object, features));
    }
}

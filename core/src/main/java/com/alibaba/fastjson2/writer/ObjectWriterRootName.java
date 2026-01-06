package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;

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

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        jsonWriter.writeName(rootName);
        super.writeJSONB(jsonWriter, object, fieldName, fieldType, features);
        jsonWriter.endObject();
    }

    @Override
    public void writeUTF8(JSONWriterUTF8 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        jsonWriter.writeName(rootName);
        jsonWriter.writeColon();
        super.writeUTF8(jsonWriter, object, fieldName, fieldType, features);
        jsonWriter.endObject();
    }

    @Override
    public void writeUTF16(JSONWriterUTF16 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();
        jsonWriter.writeName(rootName);
        jsonWriter.writeColon();
        super.writeUTF16(jsonWriter, object, fieldName, fieldType, features);
        jsonWriter.endObject();
    }

    public JSONObject toJSONObject(T object, long features) {
        return JSONObject.of(
                rootName,
                super.toJSONObject(object, features));
    }
}

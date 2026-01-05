package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.util.List;

public class ObjectWriter2<T>
        extends ObjectWriterAdapter<T> {
    public final FieldWriter fieldWriter0;
    public final FieldWriter fieldWriter1;

    public ObjectWriter2(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        super(objectClass, typeKey, typeName, features, fieldWriters);
        this.fieldWriter0 = fieldWriters.get(0);
        this.fieldWriter1 = fieldWriters.get(1);
    }

    @Override
    protected void writeFieldsJSONB(JSONWriterJSONB jsonWriter, Object object) {
        fieldWriter0.writeJSONB(jsonWriter, object);
        fieldWriter1.writeJSONB(jsonWriter, object);
    }

    @Override
    protected void writeFieldsUTF8(JSONWriterUTF8 jsonWriter, Object object) {
        fieldWriter0.writeUTF8(jsonWriter, object);
        fieldWriter1.writeUTF8(jsonWriter, object);
    }

    @Override
    protected void writeFieldsUTF16(JSONWriterUTF16 jsonWriter, Object object) {
        fieldWriter0.writeUTF16(jsonWriter, object);
        fieldWriter1.writeUTF16(jsonWriter, object);
    }

    @Override
    public final FieldWriter getFieldWriter(long hashCode) {
        if (hashCode == fieldWriter0.hashCode) {
            return fieldWriter0;
        }

        if (hashCode == fieldWriter1.hashCode) {
            return fieldWriter1;
        }

        return null;
    }
}

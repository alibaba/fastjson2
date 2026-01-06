package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.util.List;

@SuppressWarnings("ALL")
public class ObjectWriter10<T>
        extends ObjectWriterAdapter<T> {
    public final FieldWriter fieldWriter0;
    public final FieldWriter fieldWriter1;
    public final FieldWriter fieldWriter2;
    public final FieldWriter fieldWriter3;
    public final FieldWriter fieldWriter4;
    public final FieldWriter fieldWriter5;
    public final FieldWriter fieldWriter6;
    public final FieldWriter fieldWriter7;
    public final FieldWriter fieldWriter8;
    public final FieldWriter fieldWriter9;

    public ObjectWriter10(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        super(objectClass, typeKey, typeName, features, fieldWriters);
        this.fieldWriter0 = fieldWriters.get(0);
        this.fieldWriter1 = fieldWriters.get(1);
        this.fieldWriter2 = fieldWriters.get(2);
        this.fieldWriter3 = fieldWriters.get(3);
        this.fieldWriter4 = fieldWriters.get(4);
        this.fieldWriter5 = fieldWriters.get(5);
        this.fieldWriter6 = fieldWriters.get(6);
        this.fieldWriter7 = fieldWriters.get(7);
        this.fieldWriter8 = fieldWriters.get(8);
        this.fieldWriter9 = fieldWriters.get(9);
    }

    @Override
    protected void writeFieldsJSONB(JSONWriterJSONB jsonWriter, Object object) {
        fieldWriter0.writeJSONB(jsonWriter, object);
        fieldWriter1.writeJSONB(jsonWriter, object);
        fieldWriter2.writeJSONB(jsonWriter, object);
        fieldWriter3.writeJSONB(jsonWriter, object);
        fieldWriter4.writeJSONB(jsonWriter, object);
        fieldWriter5.writeJSONB(jsonWriter, object);
        fieldWriter6.writeJSONB(jsonWriter, object);
        fieldWriter7.writeJSONB(jsonWriter, object);
        fieldWriter8.writeJSONB(jsonWriter, object);
        fieldWriter9.writeJSONB(jsonWriter, object);
    }

    @Override
    protected void writeFieldsUTF8(JSONWriterUTF8 jsonWriter, Object object) {
        fieldWriter0.writeUTF8(jsonWriter, object);
        fieldWriter1.writeUTF8(jsonWriter, object);
        fieldWriter2.writeUTF8(jsonWriter, object);
        fieldWriter3.writeUTF8(jsonWriter, object);
        fieldWriter4.writeUTF8(jsonWriter, object);
        fieldWriter5.writeUTF8(jsonWriter, object);
        fieldWriter6.writeUTF8(jsonWriter, object);
        fieldWriter7.writeUTF8(jsonWriter, object);
        fieldWriter8.writeUTF8(jsonWriter, object);
        fieldWriter9.writeUTF8(jsonWriter, object);
    }

    @Override
    protected void writeFieldsUTF16(JSONWriterUTF16 jsonWriter, Object object) {
        fieldWriter0.writeUTF16(jsonWriter, object);
        fieldWriter1.writeUTF16(jsonWriter, object);
        fieldWriter2.writeUTF16(jsonWriter, object);
        fieldWriter3.writeUTF16(jsonWriter, object);
        fieldWriter4.writeUTF16(jsonWriter, object);
        fieldWriter5.writeUTF16(jsonWriter, object);
        fieldWriter6.writeUTF16(jsonWriter, object);
        fieldWriter7.writeUTF16(jsonWriter, object);
        fieldWriter8.writeUTF16(jsonWriter, object);
        fieldWriter9.writeUTF16(jsonWriter, object);
    }
}

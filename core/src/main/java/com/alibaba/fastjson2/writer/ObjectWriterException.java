package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

public class ObjectWriterException extends ObjectWriterAdapter {

    public ObjectWriterException(Class objectType, long features, List<FieldWriter> fieldWriters) {
        super(objectType, null, null, features, fieldWriters);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        writeClassInfo(jsonWriter);

        List<FieldWriter> fieldWriters = getFieldWriters();
        int size = fieldWriters.size();
        jsonWriter.startObject();
        for (FieldWriter fw : fieldWriters) {
            fw.write(jsonWriter, object);
        }
        jsonWriter.endObject();
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isJSONB()) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        List<FieldWriter> fieldWriters = getFieldWriters();

        jsonWriter.startObject();

        writeTypeInfo(jsonWriter);

        for (FieldWriter fieldWriter : fieldWriters) {
            fieldWriter.write(jsonWriter, object);
        }

        jsonWriter.endObject();
    }
}

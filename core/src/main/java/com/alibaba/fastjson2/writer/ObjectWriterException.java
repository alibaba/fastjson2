package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

public class ObjectWriterException
        extends ObjectWriterAdapter<Exception> {
    public ObjectWriterException(Class objectType, long features, List<FieldWriter> fieldWriters) {
        super(objectType, null, null, features, fieldWriters);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        writeClassInfo(jsonWriter);

        int size = fieldWriters.size();
        jsonWriter.startObject();
        for (int i = 0; i < size; ++i) {
            FieldWriter fw = fieldWriters.get(i);
            fw.write(jsonWriter, object);
        }
        jsonWriter.endObject();
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.jsonb) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        jsonWriter.startObject();

        writeTypeInfo(jsonWriter);

        for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.write(jsonWriter, object);
        }

        jsonWriter.endObject();
    }
}

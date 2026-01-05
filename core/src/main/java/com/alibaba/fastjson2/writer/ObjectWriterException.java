package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

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
    public void writeUTF8(JSONWriterUTF8 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        jsonWriter.startObject();

        if ((jsonWriter.getFeatures(features)
                & (JSONWriter.Feature.WriteClassName.mask | JSONWriter.Feature.WriteThrowableClassName.mask)) != 0
        ) {
            jsonWriter.writeNameRaw(nameWithColonUTF8);
        }

        for (FieldWriter fieldWriter : fieldWriters) {
            fieldWriter.writeUTF8(jsonWriter, object);
        }

        jsonWriter.endObject();
    }

    @Override
    public void writeUTF16(JSONWriterUTF16 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        jsonWriter.startObject();

        if ((jsonWriter.getFeatures(features)
                & (JSONWriter.Feature.WriteClassName.mask | JSONWriter.Feature.WriteThrowableClassName.mask)) != 0
        ) {
            jsonWriter.writeNameRaw(nameWithColonUTF16);
        }

        for (FieldWriter fieldWriter : fieldWriters) {
            fieldWriter.writeUTF16(jsonWriter, object);
        }

        jsonWriter.endObject();
    }
}

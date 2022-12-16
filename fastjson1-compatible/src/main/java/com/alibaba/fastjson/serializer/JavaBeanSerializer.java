package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class JavaBeanSerializer
        implements ObjectSerializer {
    private final ObjectWriter raw;

    public JavaBeanSerializer(ObjectWriter raw) {
        this.raw = raw;
    }

    @Override
    public void write(
            JSONSerializer serializer,
            Object object,
            Object fieldName,
            Type fieldType,
            int features
    ) throws IOException {
        JSONWriter jsonWriter = serializer.out.raw;
        raw.write(jsonWriter, object, fieldName, fieldType, 0L);
    }

    public Object getFieldValue(Object object, String key) {
        FieldWriter fieldWriter = raw.getFieldWriter(key);
        if (fieldWriter == null) {
            return null;
        }
        return fieldWriter.getFieldValue(object);
    }
}

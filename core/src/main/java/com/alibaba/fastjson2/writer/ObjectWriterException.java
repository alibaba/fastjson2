package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.List;

public class ObjectWriterException
        implements ObjectWriter<Exception> {
    public ObjectWriterException(Class objectType, long features, List<Object> fieldWriters) {
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        throw new UnsupportedOperationException();
    }
}

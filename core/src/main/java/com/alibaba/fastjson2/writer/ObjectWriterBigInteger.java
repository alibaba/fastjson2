package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigInteger;

final class ObjectWriterBigInteger
        implements ObjectWriter {
    static final ObjectWriterBigInteger INSTANCE = new ObjectWriterBigInteger(0L);

    final long features;

    public ObjectWriterBigInteger(long features) {
        this.features = features;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeBigInt((BigInteger) object, features);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeBigInt((BigInteger) object, features);
    }
}

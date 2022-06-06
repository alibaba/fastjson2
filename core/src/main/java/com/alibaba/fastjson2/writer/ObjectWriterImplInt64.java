package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplInt64
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt64 INSTANCE = new ObjectWriterImplInt64(null);

    final Class defineClass;

    public ObjectWriterImplInt64(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        long longValue = ((Long) object).longValue();
        jsonWriter.writeInt64(longValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        long longValue = ((Number) object).longValue();
        jsonWriter.writeInt64(longValue);

        if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
            if (((jsonWriter.getFeatures() | features) & JSONWriter.Feature.WriteClassName.mask) != 0
                    && fieldType != Short.class && fieldType != short.class) {
                jsonWriter.writeRaw('L');
            }
        }
    }
}

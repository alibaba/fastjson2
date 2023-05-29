package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class ObjectWriterImplInt64
        extends ObjectWriterPrimitiveImpl {
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

        long longValue = (Long) object;
        jsonWriter.writeInt64(longValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        long i = ((Number) object).longValue();
        jsonWriter.writeInt64(i);

        if (i >= Integer.MIN_VALUE
                && i <= Integer.MAX_VALUE
                && (features & WriteClassName.mask) != 0
        ) {
            long contextFeatures = jsonWriter.getFeatures();
            if ((contextFeatures & WriteClassName.mask) == 0) {
                boolean writeAsString = (contextFeatures & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                        || ((contextFeatures & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));
                if (!writeAsString) {
                    jsonWriter.writeRaw('L');
                }
            }
        }
    }
}

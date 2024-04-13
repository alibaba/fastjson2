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

        long longValue = ((Long) object).longValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(longValue);
        } else {
            jsonWriter.writeInt64(longValue);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        long longValue = ((Number) object).longValue();

        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(longValue);
            return;
        }

        jsonWriter.writeInt64(longValue);

        if (longValue >= Integer.MIN_VALUE
                && longValue <= Integer.MAX_VALUE
                && (features & WriteClassName.mask) != 0
        ) {
            long contextFeatures = jsonWriter.getFeatures();
            if ((contextFeatures & WriteClassName.mask) == 0) {
                boolean writeAsString = (contextFeatures & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
                if (!writeAsString) {
                    jsonWriter.writeRaw('L');
                }
            }
        }
    }
}

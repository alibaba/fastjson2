package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;

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
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        long longValue = (Long) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(longValue);
            return;
        }
        jsonWriter.writeInt64(longValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        long value = ((Number) object).longValue();

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }

        jsonWriter.writeInt64(value);

        if (value >= Integer.MIN_VALUE
                && value <= Integer.MAX_VALUE
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

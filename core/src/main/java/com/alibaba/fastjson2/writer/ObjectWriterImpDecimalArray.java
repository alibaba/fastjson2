package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import static com.alibaba.fastjson2.JSONWriter.Feature.NullAsDefaultValue;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNullListAsEmpty;

final class ObjectWriterImpDecimalArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImpDecimalArray INSTANCE = new ObjectWriterImpDecimalArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[BigDecimal");

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            if (jsonWriter.isEnabled(NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) {
                jsonWriter.startArray();
                jsonWriter.endArray();
            } else {
                jsonWriter.writeNull();
            }
            return;
        }

        BigDecimal[] array = (BigDecimal[]) object;

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            jsonWriter.writeDecimal(array[i], 0, null);
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            final long JSONB_TYPE_HASH = -2138534155605614069L; // Fnv.hashCode64("[BigDecimal");
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        BigDecimal[] array = (BigDecimal[]) object;

        jsonWriter.startArray(array.length);
        for (int i = 0; i < array.length; i++) {
            jsonWriter.writeDecimal(array[i], 0, null);
        }
    }
}

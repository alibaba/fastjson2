package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class ObjectWriterImplInt64Array
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt64Array INSTANCE = new ObjectWriterImplInt64Array();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[Long");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[Long");

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

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;

        Long[] array = (Long[]) object;

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Long item = array[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            long longValue = item;
            if (writeAsString) {
                jsonWriter.writeString(longValue);
            } else {
                jsonWriter.writeInt64(longValue);
            }
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
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;

        Long[] array = (Long[]) object;

        jsonWriter.startArray(array.length);
        for (Long item : array) {
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            long longValue = item;
            if (writeAsString) {
                jsonWriter.writeString(longValue);
            } else {
                jsonWriter.writeInt64(longValue);
            }
        }
    }
}

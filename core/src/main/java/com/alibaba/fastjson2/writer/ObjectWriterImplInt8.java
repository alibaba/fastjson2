package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_NUMBER_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NON_STRING_KEY_AS_STRING;

final class ObjectWriterImplInt8
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt8 INSTANCE = new ObjectWriterImplInt8();

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        byte byteValue = (Byte) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(byteValue);
            return;
        }

        jsonWriter.writeInt8(byteValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        byte byteValue = (Byte) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(byteValue);
            return;
        }

        jsonWriter.writeInt8(byteValue);
        long features2 = jsonWriter.getFeatures(features);
        if ((features2 & MASK_WRITE_CLASS_NAME) != 0
                && (features2 & MASK_WRITE_NON_STRING_KEY_AS_STRING) == 0
                && (features2 & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                && fieldType != Byte.class && fieldType != byte.class) {
            jsonWriter.writeRaw('B');
        }
    }
}

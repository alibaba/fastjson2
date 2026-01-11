package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_NUMBER_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NON_STRING_KEY_AS_STRING;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NON_STRING_VALUE_AS_STRING;

final class ObjectWriterImplFloat
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplFloat INSTANCE = new ObjectWriterImplFloat(null);

    private final DecimalFormat format;

    public ObjectWriterImplFloat(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        float value = (Float) object;
        if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeFloat(value);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (format != null) {
            String str = format.format(object);
            jsonWriter.writeRaw(str);
            return;
        }

        float value = (Float) object;
        if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
            jsonWriter.writeString(value);
            return;
        }

        jsonWriter.writeFloat(value);

        long features2 = jsonWriter.getFeatures(features);
        if ((features2 & MASK_WRITE_CLASS_NAME) != 0
                && (features2 & MASK_WRITE_NON_STRING_KEY_AS_STRING) == 0
                && (features2 & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                && fieldType != Float.class && fieldType != float.class) {
            jsonWriter.writeRaw('F');
        }
    }
}

package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_NUMBER_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_CLASS_NAME;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NON_STRING_KEY_AS_STRING;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NON_STRING_VALUE_AS_STRING;

final class ObjectWriterImplDouble
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplDouble INSTANCE = new ObjectWriterImplDouble(null);

    private final DecimalFormat format;

    public ObjectWriterImplDouble(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        double value = (Double) object;
        if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeDouble(value);
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        DecimalFormat decimalFormat = this.format;
        if (decimalFormat == null) {
            String format = jsonWriter.getContext().getDateFormat();
            if (format != null && format.indexOf('#') != -1) {
                decimalFormat = new DecimalFormat(format);
            }
        }

        if (decimalFormat != null) {
            String str = decimalFormat.format(object);
            jsonWriter.writeRaw(str);
            return;
        }

        double value = (Double) object;
        if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
            jsonWriter.writeString(value);
            return;
        }

        jsonWriter.writeDouble(value);
        long features2 = jsonWriter.getFeatures(features);
        if ((features2 & MASK_WRITE_CLASS_NAME) != 0
                && (features2 & MASK_WRITE_NON_STRING_KEY_AS_STRING) == 0
                && (features2 & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                && fieldType != Double.class && fieldType != double.class) {
            jsonWriter.writeRaw('D');
        }
    }
}

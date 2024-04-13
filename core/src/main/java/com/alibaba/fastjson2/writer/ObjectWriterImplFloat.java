package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

final class ObjectWriterImplFloat
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplFloat INSTANCE = new ObjectWriterImplFloat(null);

    private final DecimalFormat format;

    public ObjectWriterImplFloat(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        float value = ((Float) object).floatValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
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

        float floatValue = ((Float) object).floatValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(floatValue);
            return;
        }
        jsonWriter.writeFloat(floatValue);

        if (((jsonWriter.getFeatures() | features) & JSONWriter.Feature.WriteClassName.mask) != 0
                && fieldType != Float.class && fieldType != float.class) {
            jsonWriter.writeRaw('F');
        }
    }
}

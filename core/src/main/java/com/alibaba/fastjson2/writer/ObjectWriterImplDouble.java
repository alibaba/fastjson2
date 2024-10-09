package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

final class ObjectWriterImplDouble
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplDouble INSTANCE = new ObjectWriterImplDouble(null);

    private final DecimalFormat format;

    public ObjectWriterImplDouble(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        double value = ((Double) object).doubleValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
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
            if (format != null && format.indexOf("#") != -1) {
                decimalFormat = new DecimalFormat(format);
            }
        }

        if (decimalFormat != null) {
            String str = decimalFormat.format(object);
            jsonWriter.writeRaw(str);
            return;
        }

        double value = ((Double) object).doubleValue();
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }

        jsonWriter.writeDouble(value);
        long features2 = jsonWriter.getFeatures(features);
        if ((features2 & JSONWriter.Feature.WriteClassName.mask) != 0
                && (features2 & JSONWriter.Feature.WriteNonStringKeyAsString.mask) == 0
                && (features2 & JSONWriter.Feature.NotWriteNumberClassName.mask) == 0
                && fieldType != Double.class && fieldType != double.class) {
            jsonWriter.writeRaw('D');
        }
    }
}

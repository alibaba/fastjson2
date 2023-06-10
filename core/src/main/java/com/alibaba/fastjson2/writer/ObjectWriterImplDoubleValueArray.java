package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

final class ObjectWriterImplDoubleValueArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplDoubleValueArray INSTANCE = new ObjectWriterImplDoubleValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[D");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[D");

    final DecimalFormat format;

    private final Function<Object, double[]> function;

    public ObjectWriterImplDoubleValueArray(DecimalFormat format) {
        this.format = format;
        this.function = null;
    }

    public ObjectWriterImplDoubleValueArray(Function<Object, double[]> function, DecimalFormat format) {
        this.function = function;
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        double[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (double[]) object;
        }
        jsonWriter.writeDouble(array);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        double[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (double[]) object;
        }

        if (format == null) {
            jsonWriter.writeDouble(array);
        } else {
            jsonWriter.writeDouble(array, format);
        }
    }
}

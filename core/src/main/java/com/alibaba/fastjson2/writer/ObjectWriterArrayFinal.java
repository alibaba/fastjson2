package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;

final class ObjectWriterArrayFinal
        extends ObjectWriterBaseModule.PrimitiveImpl {
    public static final ObjectWriterArrayFinal FLOAT_ARRAY = new ObjectWriterArrayFinal(Float.class, null);
    public static final ObjectWriterArrayFinal DOUBLE_ARRAY = new ObjectWriterArrayFinal(Double.class, null);
    public static final ObjectWriterArrayFinal DECIMAL_ARRAY = new ObjectWriterArrayFinal(BigDecimal.class, null);

    final byte[] typeNameBytes;
    final long typeNameHash;
    final Class itemClass;
    volatile ObjectWriter itemObjectWriter;
    public final DecimalFormat format;
    public boolean refDetect;

    public ObjectWriterArrayFinal(Class itemClass, DecimalFormat format) {
        this.itemClass = itemClass;
        this.format = format;

        String typeName = '[' + TypeUtils.getTypeName((Class) itemClass);
        typeNameBytes = JSONB.toBytes(typeName);
        typeNameHash = Fnv.hashCode64(typeName);
        refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
    }

    public ObjectWriter getItemObjectWriter(JSONWriter jsonWriter) {
        ObjectWriter itemObjectWriter = this.itemObjectWriter;
        if (itemObjectWriter == null) {
            if (itemClass == Float.class) {
                if (format != null) {
                    itemObjectWriter = new ObjectWriterImplFloat(format);
                } else {
                    itemObjectWriter = ObjectWriterImplFloat.INSTANCE;
                }
            } else if (itemClass == Double.class) {
                if (format != null) {
                    itemObjectWriter = new ObjectWriterImplDouble(format);
                } else {
                    itemObjectWriter = ObjectWriterImplDouble.INSTANCE;
                }
            } else if (itemClass == BigDecimal.class) {
                if (format != null) {
                    itemObjectWriter = new ObjectWriterImplBigDecimal(format);
                } else {
                    itemObjectWriter = ObjectWriterImplBigDecimal.INSTANCE;
                }
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(this.itemClass);
            }
            this.itemObjectWriter = itemObjectWriter;
        }
        return itemObjectWriter;
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.jsonb) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();
        if (refDetect) {
            refDetect = this.refDetect;
        }

        Object[] list = (Object[]) object;

        jsonWriter.startArray();
        for (int i = 0; i < list.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            ObjectWriter itemObjectWriter = getItemObjectWriter(jsonWriter);
            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.write(jsonWriter, item, i, this.itemClass, features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();
        if (refDetect) {
            refDetect = this.refDetect;
        }

        Object[] list = (Object[]) object;

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(typeNameBytes, typeNameHash);
        }

        jsonWriter.startArray(list.length);
        for (int i = 0; i < list.length; i++) {
            Object item = list[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            ObjectWriter itemObjectWriter = getItemObjectWriter(jsonWriter);

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemClass, 0);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
    }
}

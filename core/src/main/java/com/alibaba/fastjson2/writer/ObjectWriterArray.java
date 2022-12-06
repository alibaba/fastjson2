package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;

final class ObjectWriterArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterArray INSTANCE = new ObjectWriterArray(Object.class);

    final byte[] typeNameBytes;
    final long typeNameHash;
    final Type itemType;
    volatile ObjectWriter itemObjectWriter;

    public ObjectWriterArray(Type itemType) {
        this.itemType = itemType;

        if (itemType == Object.class) {
            typeNameBytes = JSONB.toBytes("[O");
            typeNameHash = Fnv.hashCode64("[0");
        } else {
            String typeName = '[' + TypeUtils.getTypeName((Class) itemType);
            typeNameBytes = JSONB.toBytes(typeName);
            typeNameHash = Fnv.hashCode64(typeName);
        }
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

        Object[] list = (Object[]) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
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
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;

                if (refDetect) {
                    refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.write(jsonWriter, item, i, this.itemType, features);

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

        Object[] list = (Object[]) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

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

            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;

                if (refDetect) {
                    refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, 0);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
    }
}

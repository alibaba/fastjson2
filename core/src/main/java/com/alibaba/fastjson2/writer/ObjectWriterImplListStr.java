package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.writer.ObjectWriterImplList.CLASS_SUBLIST;

final class ObjectWriterImplListStr
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplListStr INSTANCE = new ObjectWriterImplListStr();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        List list = (List) object;

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String item = (String) list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        Type fieldItemType = null;
        Class fieldItemClass = null;
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                fieldItemType = actualTypeArguments[0];
            }

            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }

        if (fieldItemType instanceof Class) {
            fieldItemClass = (Class) fieldItemType;
        }

        Class<?> objectClass = object.getClass();
        if (jsonWriter.isWriteTypeInfo(object, fieldClass, features)) {
            jsonWriter.writeTypeName(
                    TypeUtils.getTypeName(
                            objectClass == CLASS_SUBLIST ? ArrayList.class : objectClass
                    )
            );
        }

        List list = (List) object;

        final int size = list.size();
        jsonWriter.startArray(size);
        for (int i = 0; i < size; i++) {
            String item = (String) list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();
    }
}

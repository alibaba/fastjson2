package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.JSONB.Constants.BC_ARRAY_FIX_0;

final class ObjectWriterImplList
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplList
            INSTANCE = new ObjectWriterImplList(null, null, null, null, 0);

    static final Class CLASS_SUBLIST = new ArrayList().subList(0, 0).getClass();
    static final String TYPE_NAME_ARRAY_LIST = TypeUtils.getTypeName(ArrayList.class);
    static final byte[] TYPE_NAME_JSONB_ARRAY_LIST = JSONB.toBytes(TYPE_NAME_ARRAY_LIST);
    static final long TYPE_NAME_HASH_ARRAY_LIST = Fnv.hashCode64(TYPE_NAME_ARRAY_LIST);

    final Class defineClass;
    final Type defineType;
    final Class itemClass;
    final Type itemType;
    final long features;
    final boolean itemClassRefDetect;
    volatile ObjectWriter itemClassWriter;

    public ObjectWriterImplList(
            Class defineClass,
            Type defineType,
            Class itemClass,
            Type itemType,
            long features
    ) {
        this.defineClass = defineClass;
        this.defineType = defineType;
        this.itemClass = itemClass;
        this.itemType = itemType;
        this.features = features;
        this.itemClassRefDetect = itemClass != null && !ObjectWriterProvider.isNotReferenceDetect(itemClass);
    }

    @Override
    public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        List list = (List) object;
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        jsonWriter.startArray(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.writeArrayMappingJSONB(jsonWriter, item, i, this.itemType, this.features | features);
        }
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
        } else if (fieldType == this.defineType) {
            fieldClass = this.itemClass;
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
            if (objectClass == CLASS_SUBLIST || objectClass == ArrayList.class) {
                jsonWriter.writeTypeName(TYPE_NAME_JSONB_ARRAY_LIST, TYPE_NAME_HASH_ARRAY_LIST);
            } else {
                String typeName = TypeUtils.getTypeName(objectClass);
                jsonWriter.writeTypeName(typeName);
            }
        }

        List list = (List) object;
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        int size = list.size();
        if (size == 0) {
            jsonWriter.writeRaw(BC_ARRAY_FIX_0);
            return;
        }

        boolean beanToArray = jsonWriter.isBeanToArray();
        if (beanToArray) {
            jsonWriter.startArray(size);
            for (int i = 0; i < size; i++) {
                Object item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass == previousClass) {
                    itemObjectWriter = previousObjectWriter;
                } else {
                    itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                    previousClass = itemClass;
                    previousObjectWriter = itemObjectWriter;
                }

                itemObjectWriter.writeArrayMappingJSONB(jsonWriter, item, i, fieldItemClass, features);
            }
            jsonWriter.endArray();
            return;
        }

        JSONWriter.Context context = jsonWriter.getContext();

        jsonWriter.startArray(size);
        for (int i = 0; i < size; i++) {
            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();

            boolean refDetect = (itemClass == this.itemClass)
                    ? this.itemClassRefDetect && jsonWriter.isRefDetect()
                    : jsonWriter.isRefDetect(item);

            ObjectWriter itemObjectWriter;

            if (itemClass == this.itemClass && itemClassWriter != null) {
                itemObjectWriter = itemClassWriter;
            } else if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = context.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
                if (itemClass == this.itemClass) {
                    this.itemClassWriter = itemObjectWriter;
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

            itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, this.features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        List list = (List) object;
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        if (jsonWriter.isJSONB()) {
            jsonWriter.startArray(list.size());
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass == previousClass) {
                    itemObjectWriter = previousObjectWriter;
                } else {
                    itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                    previousClass = itemClass;
                    previousObjectWriter = itemObjectWriter;
                }

                itemObjectWriter.writeJSONB(jsonWriter, item, i, itemType, features);
            }
            return;
        }

        JSONWriter.Context context = jsonWriter.getContext();

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();

            boolean refDetect = (itemClass == this.itemClass)
                    ? this.itemClassRefDetect && jsonWriter.isRefDetect()
                    : jsonWriter.isRefDetect(item);

            ObjectWriter itemObjectWriter;

            if (itemClass == this.itemClass && itemClassWriter != null) {
                itemObjectWriter = itemClassWriter;
            } else if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = context.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
                if (itemClass == this.itemClass) {
                    this.itemClassWriter = itemObjectWriter;
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

            itemObjectWriter.write(jsonWriter, item, i, this.itemType, this.features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }
}

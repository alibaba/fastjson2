package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

final class FieldReaderListStrMethod<T>
        extends FieldReaderObjectMethod<T>
        implements FieldReaderList<T, Object> {
    final long fieldClassHash;

    FieldReaderListStrMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, JSONSchema schema, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, method);
        this.fieldClassHash = Fnv.hashCode64(TypeUtils.getTypeName(fieldClass));
    }

    @Override
    public Type getItemType() {
        return String.class;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        List value;
        if (jsonReader.isJSONB()) {
            Class listType = fieldClass;
            ObjectReader objectReader = jsonReader.checkAutoType(fieldClass, fieldClassHash, features);
            if (objectReader != null) {
                listType = objectReader.getObjectClass();
            }

            int itemCnt = jsonReader.startArray();
            if (itemCnt == -1) {
                value = null;
            } else if (listType == Collection.class
                    || listType == AbstractCollection.class
                    || listType == List.class
                    || listType == AbstractList.class
                    || listType == ArrayList.class
                    || listType == ObjectReaderImplList.CLASS_ARRAYS_LIST) {
                value = new ArrayList(itemCnt);
            } else if (listType == LinkedList.class) {
                value = new LinkedList();
            } else if (listType == JSONArray.class) {
                value = new JSONArray(itemCnt);
            } else {
                switch (listType.getName()) {
                    case "com.google.common.collect.Lists$TransformingRandomAccessList":
                        value = new ArrayList();
                        break;
                    case "com.google.common.collect.Lists.TransformingSequentialList":
                        value = new LinkedList();
                        break;
                    default:
                        try {
                            value = (List) listType.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new JSONException(jsonReader.info("create instance error " + listType), e);
                        }
                        break;
                }
            }

            for (int i = 0; i < itemCnt; ++i) {
                value.add(jsonReader.readString());
            }
        } else if (jsonReader.current() == '[') {
            List list = createList();
            jsonReader.next();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                list.add(jsonReader.readString());

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }
            accept(object, list);

            jsonReader.nextIfMatch(',');

            value = list;
        } else if (jsonReader.isString()) {
            List list = createList();
            list.add(jsonReader.readString());
            accept(object, list);
            jsonReader.nextIfMatch(',');
            value = list;
        } else if (jsonReader.nextIfNull()) {
            value = null;
        } else {
            throw new JSONException(jsonReader.info("json format error"));
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            method.invoke(object, value);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }
}

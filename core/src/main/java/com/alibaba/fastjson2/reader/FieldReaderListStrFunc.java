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
import java.util.function.BiConsumer;

final class FieldReaderListStrFunc<T>
        extends FieldReaderImpl<T> {
    final BiConsumer<T, List> function;
    final long fieldClassHash;

    FieldReaderListStrFunc(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method, BiConsumer<T, List> function) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
        this.fieldClassHash = Fnv.hashCode64(TypeUtils.getTypeName(fieldClass));
    }

    @Override
    public Type getItemType() {
        return String.class;
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        return ObjectReaderImplString.INSTANCE;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (List) value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
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
                    || listType == ArrayList.class) {
                value = new ArrayList(itemCnt);
            } else if (listType == LinkedList.class) {
                value = new LinkedList();
            } else if (listType == JSONArray.class) {
                value = new JSONArray(itemCnt);
            } else {
                try {
                    value = (List) listType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new JSONException(jsonReader.info("create instance error " + listType), e);
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
            jsonReader.nextIfMatch(',');

            value = list;
        } else {
            throw new JSONException(jsonReader.info("json format error"));
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        return value;
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
                    || listType == ArrayList.class) {
                value = new ArrayList(itemCnt);
            } else if (listType == LinkedList.class) {
                value = new LinkedList();
            } else if (listType == JSONArray.class) {
                value = new JSONArray(itemCnt);
            } else {
                try {
                    value = (List) listType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new JSONException(jsonReader.info("create instance error " + listType), e);
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
        } else {
            throw new JSONException(jsonReader.info("json format error"));
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            function.accept(object, value);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    public List<String> createList() {
        return new ArrayList<>();
    }
}

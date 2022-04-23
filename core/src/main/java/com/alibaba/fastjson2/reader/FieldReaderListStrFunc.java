package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;

final class FieldReaderListStrFunc<T> extends FieldReaderImpl<T>
        implements FieldListStrReader<T> {
    final Method method;
    final BiConsumer<T, List> function;
    final long fieldClassHash;

    FieldReaderListStrFunc(
            String fieldName
            , Type fieldType
            , Class fieldClass
            , int ordinal
            , Method method
            , BiConsumer<T, List> function) {
        super(fieldName, fieldType, fieldClass, ordinal, 0, null);
        this.method = method;
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
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (List) value);
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
                    throw new JSONException("create instance error " + listType, e);
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
        } else {
            throw new JSONException("json format error : " + jsonReader.current());
        }


        try {
            function.accept(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}

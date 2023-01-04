package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class ObjectReaderImplGenericArray
        implements ObjectReader {
    final Type arrayType;
    final Class arrayClass;
    final Type itemType;
    final Class<?> componentClass;
    ObjectReader itemObjectReader;
    final String arrayClassName;
    final long arrayClassNameHash;

    public ObjectReaderImplGenericArray(GenericArrayType genericType) {
        this.arrayType = genericType;
        this.arrayClass = TypeUtils.getClass(arrayType);
        this.itemType = genericType.getGenericComponentType();
        this.componentClass = TypeUtils.getMapping(itemType);
        this.arrayClassName = "[" + TypeUtils.getTypeName(this.componentClass);
        this.arrayClassNameHash = Fnv.hashCode64(arrayClassName);
    }

    @Override
    public Object createInstance(long features) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        return null;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != arrayClassNameHash) {
                String typeName = jsonReader.getString();
                throw new JSONException("not support input typeName " + typeName);
            }
        }

        int entryCnt = jsonReader.startArray();

        if (entryCnt > 0 && itemObjectReader == null) {
            itemObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(itemType);
        }

        Object array = Array.newInstance(componentClass, entryCnt);

        for (int i = 0; i < entryCnt; ++i) {
            Object item = itemObjectReader.readJSONBObject(jsonReader, itemType, null, 0);
            Array.set(array, i, item);
        }

        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (itemObjectReader == null) {
            itemObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(itemType);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        char ch = jsonReader.current();
        if (ch == '"') {
            String str = jsonReader.readString();
            if (str.isEmpty()) {
                return null;
            }
            throw new JSONException(jsonReader.info());
        }

        List<Object> list = new ArrayList<>();
        if (ch != '[') {
            throw new JSONException(jsonReader.info());
        }
        jsonReader.next();

        for (; ; ) {
            if (jsonReader.nextIfMatch(']')) {
                break;
            }

            Object item;
            if (itemObjectReader != null) {
                item = itemObjectReader.readObject(jsonReader, itemType, null, 0);
            } else {
                if (itemType == String.class) {
                    item = jsonReader.readString();
                } else {
                    throw new JSONException(jsonReader.info("TODO : " + itemType));
                }
            }

            list.add(item);

            if (jsonReader.nextIfMatch(',')) {
                continue;
            }
        }

        jsonReader.nextIfMatch(',');

        Object array = Array.newInstance(componentClass, list.size());

        for (int i = 0; i < list.size(); ++i) {
            Array.set(array, i, list.get(i));
        }

        return array;
    }
}

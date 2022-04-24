package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public final class FieldReaderListInt64 implements ObjectReader {
    final Class listType;
    final Class instanceType;
    final long instanceTypeHash;

    public FieldReaderListInt64(Class listType, Class instanceType) {
        this.listType = listType;
        this.instanceType = instanceType;
        this.instanceTypeHash = Fnv.hashCode64(TypeUtils.getTypeName(instanceType));
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType == ArrayList.class) {
            return new ArrayList<>();
        }

        if (instanceType == LinkedList.class) {
            return new LinkedList();
        }

        try {
            return instanceType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JSONException("create list error, type " + instanceType);
        }
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        return null;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        Class listType = this.listType;

        ObjectReader objectReader = jsonReader.checkAutoType(listType, instanceTypeHash, features);
        if (objectReader != null) {
            listType = objectReader.getObjectClass();
        }

        Collection list;
        if (listType == ArrayList.class) {
            list = new ArrayList();
        } else if (listType == JSONArray.class) {
            list = new JSONArray();
        } else if (listType != null && listType != this.listType) {
            try {
                list = (Collection) listType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create instance error " + listType, e);
            }
        } else {
            list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
        }

        int entryCnt = jsonReader.startArray();
        for (int i = 0; i < entryCnt; ++i) {
            list.add(jsonReader.readInt64());
        }

        return list;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isString()) {
            Collection list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
            String str = jsonReader.readString();
            if (str.indexOf(',') != -1) {
                String[] items = str.split(",");
                for (String item : items) {
                    list.add(
                            Long.parseLong(item));
                }
            } else {
                list.add(
                        Long.parseLong(str));
            }
            jsonReader.nextIfMatch(',');
            return list;
        }

        if (jsonReader.current() != '[') {
            throw new JSONException("format error : " + jsonReader.current());
        }
        jsonReader.next();


        Collection list = (Collection) createInstance(jsonReader.getContext().getFeatures() | features);
        for (; ; ) {
            if (jsonReader.nextIfMatch(']')) {
                break;
            }

            list.add(
                    jsonReader.readInt64());
        }

        jsonReader.nextIfMatch(',');

        return list;
    }
}

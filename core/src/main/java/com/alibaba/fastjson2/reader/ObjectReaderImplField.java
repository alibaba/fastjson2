package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public class ObjectReaderImplField
        implements ObjectReader {
    static final long HASH_DECLARING_CLASS = Fnv.hashCode64("declaringClass");
    static final long HASH_NAME = Fnv.hashCode64("name");

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public Object readArrayMappingJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCount = jsonReader.startArray();
        if (entryCount != 2) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        String declaringClassName = jsonReader.readString();
        String methodName = jsonReader.readString();

        return getField(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName);
    }

    @Override
    public Object readArrayMappingObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        boolean arrayStart = jsonReader.nextIfMatch('[');
        if (!arrayStart) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        String declaringClassName = jsonReader.readString();
        String methodName = jsonReader.readString();

        boolean arrayEnd = jsonReader.nextIfMatch(']');
        if (!arrayEnd) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        jsonReader.nextIfMatch(',');

        return getField(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        boolean objectStart = jsonReader.nextIfObjectStart();
        if (!objectStart) {
            if (jsonReader.isSupportBeanArray(features)) {
                if (jsonReader.isJSONB()) {
                    return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
                } else {
                    return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
                }
            }
            throw new JSONException("not support input " + jsonReader.info());
        }

        String methodName = null, declaringClassName = null;
        List<String> paramTypeNames = null;

        for (;;) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long nameHashCode = jsonReader.readFieldNameHashCode();
            if (nameHashCode == HASH_DECLARING_CLASS) {
                declaringClassName = jsonReader.readString();
            } else if (nameHashCode == HASH_NAME) {
                methodName = jsonReader.readString();
            } else {
                jsonReader.skipValue();
            }
        }

        if (!jsonReader.isJSONB()) {
            jsonReader.nextIfMatch(',');
        }

        return getField(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName);
    }

    private Field getField(long features,
                           String methodName,
                           String declaringClassName) {
        Class declaringClass;

        boolean supportClassForName = (features & JSONReader.Feature.SupportClassForName.mask) != 0;
        if (supportClassForName) {
            declaringClass = TypeUtils.loadClass(declaringClassName);
        } else {
            throw new JSONException("ClassForName not support");
        }

        try {
            return declaringClass.getDeclaredField(methodName);
        } catch (NoSuchFieldException e) {
            throw new JSONException("method not found", e);
        }
    }
}

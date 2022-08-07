package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class ObjectReaderImplMethod
        implements ObjectReader<Method> {
    static final long HASH_DECLARING_CLASS = Fnv.hashCode64("declaringClass");
    static final long HASH_NAME = Fnv.hashCode64("name");
    static final long HASH_PARAMETER_TYPES = Fnv.hashCode64("parameterTypes");

    @Override
    public Method readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public Method readArrayMappingJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCount = jsonReader.startArray();
        if (entryCount != 3) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        String declaringClassName = jsonReader.readString();
        String methodName = jsonReader.readString();
        List<String> paramTypeNames = jsonReader.readArray(String.class);

        return getMethod(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName, paramTypeNames);
    }

    @Override
    public Method readArrayMappingObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        boolean arrayStart = jsonReader.nextIfMatch('[');
        if (!arrayStart) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        String declaringClassName = jsonReader.readString();
        String methodName = jsonReader.readString();
        List<String> paramTypeNames = jsonReader.readArray(String.class);

        boolean arrayEnd = jsonReader.nextIfMatch(']');
        if (!arrayEnd) {
            throw new JSONException("not support input " + jsonReader.info());
        }

        jsonReader.nextIfMatch(',');

        return getMethod(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName, paramTypeNames);
    }

    @Override
    public Method readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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
            } else if (nameHashCode == HASH_PARAMETER_TYPES) {
                paramTypeNames = jsonReader.readArray(String.class);
            } else {
                jsonReader.skipValue();
            }
        }

        if (!jsonReader.isJSONB()) {
            jsonReader.nextIfMatch(',');
        }

        return getMethod(jsonReader.getContext().getFeatures() | features, methodName, declaringClassName, paramTypeNames);
    }

    private Method getMethod(long features,
                             String methodName,
                             String declaringClassName,
                             List<String> paramTypeNames) {
        Class declaringClass;

        boolean supportClassForName = (features & JSONReader.Feature.SupportClassForName.mask) != 0;
        if (supportClassForName) {
            declaringClass = TypeUtils.loadClass(declaringClassName);
        } else {
            throw new JSONException("ClassForName not support");
        }

        Class[] paramTypes;
        if (paramTypeNames == null) {
            paramTypes = new Class[0];
        } else {
            paramTypes = new Class[paramTypeNames.size()];
            for (int i = 0; i < paramTypeNames.size(); i++) {
                String paramTypeName = paramTypeNames.get(i);
                paramTypes[i] = TypeUtils.loadClass(paramTypeName);
            }
        }

        try {
            return declaringClass.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new JSONException("method not found", e);
        }
    }
}

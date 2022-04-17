package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.JSONB;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public interface FieldReader<T> extends Comparable<FieldReader> {
    Type getFieldType();
    default Class getFieldClass() {
        return TypeUtils.getMapping(getFieldType());
    }

    default ObjectReader getObjectReader(JSONReader jsonReader) {
        return jsonReader.getObjectReader(getFieldType());
    }

    default int ordinal() {
        return 0;
    }

    default long getFeatures() {
        return 0;
    }

    default String getFormat() {
        return null;
    }

    String getFieldName();

    default long getFieldNameHash() {
        return Fnv.hashCode64(getFieldName());
    }

    default boolean isReadOnly() {
        return false;
    }

    default Method getMethod() {
        return null;
    }

    default Field getField() {
        return null;
    }

    default int compareTo(FieldReader o) {
        int ordinal0 = this.ordinal();
        int ordinal1 = o.ordinal();
        if (ordinal0 < ordinal1) {
            return -1;
        }
        if (ordinal0 > ordinal1) {
            return 1;
        }

        int cmp = this.getFieldName().compareTo(o.getFieldName());
        if (cmp != 0) {
            return cmp;
        }

        cmp = (isReadOnly() == o.isReadOnly()) ? 0 : (isReadOnly() ? 1 : -1);
        if (cmp != 0) {
            return cmp;
        }

        Field thisField = getField();
        Field otherField = o.getField();
        if (thisField != null && otherField != null) {
            Class<?> thisFieldDeclaringClass = thisField.getDeclaringClass();
            Class<?> otherFieldDeclaringClass = otherField.getDeclaringClass();

            for (Class superClass = thisFieldDeclaringClass.getSuperclass();
                 superClass != null && superClass != Object.class;
                 superClass = superClass.getSuperclass()) {

                if (superClass == otherFieldDeclaringClass) {
                    return 1;
                }
            }

            for (Class superClass = otherFieldDeclaringClass.getSuperclass();
                 superClass != null && superClass != Object.class;
                 superClass = superClass.getSuperclass()) {

                if (superClass == thisFieldDeclaringClass) {
                    return -1;
                }
            }
        }

        return cmp;
    }

    default Enum getEnumByHashCode(long hashCode) {
        throw new UnsupportedOperationException();
    }

    default Enum getEnumByOrdinal(int ordinal) {
        throw new UnsupportedOperationException();
    }

    default Type getItemType() {
        return null;
    }

    default ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        throw new UnsupportedOperationException();
    }

    default ObjectReader getItemObjectReader(JSONReader jsonReader) {
        return getItemObjectReader(jsonReader.getContext());
    }

    default void accept(T object, boolean value) {
        accept(object, Boolean.valueOf(value));
    }

    default void accept(T object, byte value) {
        accept(object, Byte.valueOf(value));
    }

    default void accept(T object, short value) {
        accept(object, Short.valueOf(value));
    }

    default void accept(T object, int value) {
        accept(object, Integer.valueOf(value));
    }

    default void accept(T object, long value) {
        accept(object, Long.valueOf(value));
    }

    default void accept(T object, char value) {
        accept(object, Character.valueOf(value));
    }

    default void accept(T object, float value) {
        accept(object, Float.valueOf(value));
    }

    default void accept(T object, double value) {
        accept(object, Double.valueOf(value));
    }

    default void accept(T object, Object value) {
    }

    void readFieldValue(JSONReader jsonReader, T object);
    default void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    default Object readFieldValue(JSONReader jsonReader) {
        throw new JSONException("TODO : " + this.getClass());
    }

    default void addResolveTask(JSONReader jsonReader, Object object, String reference) {
        jsonReader.addResolveTask(this, object, JSONPath.of(reference));
    }

    default void addResolveTask(JSONReader jsonReader, List object, int i, String reference) {
        jsonReader.addResolveTask(object, i, JSONPath.of(reference));
    }

    default ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        long features = getFeatures();
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();

            boolean isSupportAutoType = jsonReader.isSupportAutoType(features);
            if (!isSupportAutoType) {
                throw new JSONException("autoType not support input " + jsonReader.getString());
            }

            ObjectReader autoTypeObjectReader = jsonReader.getContext().getObjectReaderAutoType(typeHash);
            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = jsonReader.getContext().getObjectReaderAutoType(typeName, getFieldClass(), features);
            }

            if (autoTypeObjectReader instanceof ObjectReaderImplList) {
                ObjectReaderImplList listReader = (ObjectReaderImplList) autoTypeObjectReader;
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + jsonReader.getString());
            }

            return autoTypeObjectReader;
        }
        return null;
    }
}

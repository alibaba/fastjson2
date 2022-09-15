package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public interface FieldReader<T>
        extends Comparable<FieldReader> {
    Type getFieldType();

    default Class getFieldClass() {
        return TypeUtils.getMapping(getFieldType());
    }

    default boolean isFieldClassSerializable() {
        return false;
    }

    default ObjectReader getObjectReader(JSONReader jsonReader) {
        return jsonReader.getObjectReader(getFieldType());
    }

    default ObjectReader getObjectReader(JSONReader.Context context) {
        return context.getObjectReader(getFieldType());
    }

    default int ordinal() {
        return 0;
    }

    default ObjectReader getInitReader() {
        return null;
    }

    default boolean isUnwrapped() {
        return (getFeatures() & FieldInfo.UNWRAPPED_MASK) != 0;
    }

    JSONSchema getSchema();

    default void processExtra(JSONReader jsonReader, Object object) {
        jsonReader.skipValue();
    }

    default long getFeatures() {
        return 0;
    }

    default String getFormat() {
        return null;
    }

    default Object getDefaultValue() {
        return null;
    }

    default void setDefault(T object) {
        Object defaultValue = getDefaultValue();
        if (defaultValue != null) {
            accept(object, defaultValue);
        }
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

    default Member getFieldOrMethod() {
        Field field = getField();
        if (field != null) {
            return field;
        }
        return getMethod();
    }

    @Override
    default int compareTo(FieldReader o) {
        String thisFieldName = this.getFieldName();
        String otherFieldName = o.getFieldName();

        int nameCompare = thisFieldName.compareTo(otherFieldName);
        if (nameCompare != 0) {
            int thisOrdinal = this.ordinal();
            int otherOrdinal = o.ordinal();
            if (thisOrdinal < otherOrdinal) {
                return -1;
            }
            if (thisOrdinal > otherOrdinal) {
                return 1;
            }

            return nameCompare;
        }

        int cmp = (isReadOnly() == o.isReadOnly()) ? 0 : (isReadOnly() ? 1 : -1);
        if (cmp != 0) {
            return cmp;
        }

        Member thisMember = this.getFieldOrMethod();
        Member otherMember = o.getFieldOrMethod();
        if (thisMember != null && otherMember != null && thisMember.getClass() != otherMember.getClass()) {
            Class otherDeclaringClass = otherMember.getDeclaringClass();
            Class thisDeclaringClass = thisMember.getDeclaringClass();
            if (thisDeclaringClass != otherDeclaringClass && thisDeclaringClass != null && otherDeclaringClass != null) {
                if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                    return 1;
                } else if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                    return -1;
                }
            }
        }

        Field thisField = getField();
        Field otherField = o.getField();
        if (thisField != null && otherField != null) {
            Class<?> thisFieldDeclaringClass = thisField.getDeclaringClass();
            Class<?> otherFieldDeclaringClass = otherField.getDeclaringClass();

            for (Class superClass = thisFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == otherFieldDeclaringClass) {
                    return 1;
                }
            }

            for (Class superClass = otherFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == thisFieldDeclaringClass) {
                    return -1;
                }
            }
        }

        Method thisMethod = getMethod();
        Method otherMethod = o.getMethod();
        if (thisMethod != null && otherMethod != null) {
            Class<?> thisFieldDeclaringClass = thisMethod.getDeclaringClass();
            Class<?> otherFieldDeclaringClass = otherMethod.getDeclaringClass();

            for (Class superClass = thisFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == otherFieldDeclaringClass) {
                    return 1;
                }
            }

            for (Class superClass = otherFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == thisFieldDeclaringClass) {
                    return -1;
                }
            }

            if (thisMethod.getParameterCount() == 1 && otherMethod.getParameterCount() == 1) {
                Class<?> thisParamType = thisMethod.getParameterTypes()[0];
                Class<?> otherParamType = otherMethod.getParameterTypes()[0];

                if (thisParamType.isAssignableFrom(otherParamType)) {
                    return 1;
                }

                if (otherParamType.isAssignableFrom(thisParamType)) {
                    return -1;
                }

                if (thisParamType.isEnum() && (otherParamType == Integer.class || otherParamType == int.class)) {
                    return 1;
                }

                if (otherParamType.isEnum() && (thisParamType == Integer.class || thisParamType == int.class)) {
                    return -1;
                }
            }
        }

        ObjectReader thisInitReader = this.getInitReader();
        ObjectReader otherInitReader = o.getInitReader();
        if (thisInitReader != null && otherInitReader == null) {
            return -1;
        }
        if (thisInitReader == null && otherInitReader != null) {
            return 1;
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

    default Class getItemClass() {
        return TypeUtils.getClass(getItemType());
    }

    default long getItemClassHash() {
        Class itemClass = getItemClass();
        if (itemClass == null) {
            return 0;
        }
        return Fnv.hashCode64(itemClass.getName());
    }

    default ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        return ctx.getObjectReader(Object.class);
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
        throw new JSONException(jsonReader.info("TODO : " + this.getClass()));
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
                throw new JSONException(jsonReader.info("autoType not support input " + jsonReader.getString()));
            }

            ObjectReader autoTypeObjectReader = jsonReader.getContext().getObjectReaderAutoType(typeHash);
            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = jsonReader.getContext().getObjectReaderAutoType(typeName, getFieldClass(), features);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + jsonReader.getString());
            }

            return autoTypeObjectReader;
        }
        return null;
    }
}

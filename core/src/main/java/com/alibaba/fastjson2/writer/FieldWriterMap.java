package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Function;

final class FieldWriterMap
        extends FieldWriterObject {
    protected final Class<?> contentAs;
    protected Type contentAsFieldType;
    volatile ObjectWriter mapWriter;
    private final Type keyType;
    private final Type valueType;
    final boolean valueTypeRefDetect;
    volatile ObjectWriter valueWriter;

    protected FieldWriterMap(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
        Type keyType = null, valueType = null;
        Type contentAsFieldType = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (actualTypeArguments.length == 2) {
                keyType = actualTypeArguments[0];
                valueType = actualTypeArguments[1];
            }
        }
        if (keyType == null) {
            keyType = Object.class;
        }
        if (valueType == null) {
            valueType = Object.class;
        }
        if (contentAs != null) {
            contentAsFieldType = new ParameterizedTypeImpl(fieldClass, String.class, contentAs);
        }
        this.contentAs = contentAs;
        this.contentAsFieldType = contentAsFieldType;
        this.keyType = keyType;
        this.valueType = valueType;
        this.valueTypeRefDetect = !ObjectWriterProvider.isNotReferenceDetect(TypeUtils.getClass(valueType));
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        Class<?> contentAs = this.contentAs;
        if (contentAs == null || !fieldClass.isAssignableFrom(valueClass)) {
            return super.getObjectWriter(jsonWriter, valueClass);
        }

        ObjectWriter valueWriter = this.valueWriter;
        if (valueWriter != null) {
            return valueWriter;
        }

        Type fieldType = this.fieldType;
        Type valueType = this.valueType;
        long features = this.features;
        if (contentAs != null) {
            valueType = contentAs;
            fieldType = contentAsFieldType;
            features |= FieldInfo.CONTENT_AS;
        }
        valueWriter = new ObjectWriterImplMap(keyType, valueType, format, valueClass, fieldType, features);
        this.mapWriter = valueWriter;
        return valueWriter;
    }
}

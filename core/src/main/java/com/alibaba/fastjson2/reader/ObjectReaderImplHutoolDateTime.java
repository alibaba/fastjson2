package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;

final class ObjectReaderImplHutoolDateTime
        extends ObjectReaderImplDate {
    private final Class<?> objectClass;
    private final Constructor<?> constructor;

    ObjectReaderImplHutoolDateTime(Class<?> objectClass, String format, Locale locale) {
        super(format, locale);
        this.objectClass = objectClass;
        try {
            this.constructor = objectClass.getConstructor(long.class);
        } catch (NoSuchMethodException e) {
            throw new JSONException("create Hutool DateTime reader error", e);
        }
    }

    @Override
    public Class<?> getObjectClass() {
        return objectClass;
    }

    @Override
    public Object readObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
        return createDateTime((Date) super.readObject(reader, fieldType, fieldName, features));
    }

    @Override
    public Object readJSONBObject(JSONReader reader, Type fieldType, Object fieldName, long features) {
        return createDateTime((Date) super.readJSONBObject(reader, fieldType, fieldName, features));
    }

    private Object createDateTime(Date date) {
        if (date == null) {
            return null;
        }
        try {
            return constructor.newInstance(date.getTime());
        } catch (ReflectiveOperationException e) {
            throw new JSONException("create Hutool DateTime error", e);
        }
    }
}

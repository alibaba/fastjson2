package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

class ObjectReaderImplOptional
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplOptional INSTANCE = new ObjectReaderImplOptional(null, null, null);

    final String format;
    final Locale locale;

    final Type itemType;
    final Class itemClass;
    ObjectReader itemObjectReader;

    static ObjectReaderImplOptional of(Type type, String format, Locale locale) {
        if (type == null) {
            return INSTANCE;
        }

        return new ObjectReaderImplOptional(type, format, locale);
    }

    public ObjectReaderImplOptional(Type type, String format, Locale locale) {
        super(Optional.class);

        Type itemType = null;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
            }
        }

        this.itemType = itemType;
        this.itemClass = TypeUtils.getClass(itemType);
        this.format = format;
        this.locale = locale;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Object value;
        if (itemType == null) {
            value = jsonReader.readAny();
        } else {
            if (itemObjectReader == null) {
                ObjectReader formattedObjectReader = null;
                if (format != null) {
                    formattedObjectReader = FieldReader.createFormattedObjectReader(itemType, itemClass, format, locale);
                }
                if (formattedObjectReader == null) {
                    itemObjectReader = jsonReader.getObjectReader(itemType);
                } else {
                    itemObjectReader = formattedObjectReader;
                }
            }
            value = itemObjectReader.readJSONBObject(jsonReader, itemType, fieldName, 0);
        }

        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        Object value;
        if (itemType == null) {
            value = jsonReader.readAny();
        } else {
            if (itemObjectReader == null) {
                ObjectReader formattedObjectReader = null;
                if (format != null) {
                    formattedObjectReader = FieldReader.createFormattedObjectReader(itemType, itemClass, format, locale);
                }
                if (formattedObjectReader == null) {
                    itemObjectReader = jsonReader.getObjectReader(itemType);
                } else {
                    itemObjectReader = formattedObjectReader;
                }
            }
            value = itemObjectReader.readObject(jsonReader, itemType, fieldName, 0);
        }

        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}

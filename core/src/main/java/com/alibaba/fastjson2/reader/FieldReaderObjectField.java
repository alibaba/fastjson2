package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

class FieldReaderObjectField<T> extends FieldReaderImpl<T> {
    protected final Field field;
    protected ObjectReader fieldObjectReader;

    FieldReaderObjectField(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType == null ? field.getType() : fieldType, fieldClass, ordinal, features, format);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    static ObjectReader createFormattedObjectReader(Type fieldType, Class fieldClass, String format) {
        if (format != null && !format.isEmpty()) {
            String typeName = fieldType.getTypeName();
            switch (typeName) {
                case "java.sql.Time":
                    return JdbcSupport.createTimeReader(format);
                case "java.sql.Timestamp":
                    return JdbcSupport.createTimestampReader(format);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader(format);
                case "byte[]":
                case "[B":
                    if ("base64".equals(format)) {
                        return ObjectReaderBaseModule.Base64Impl.INSTANCE;
                    }
                    break;
                default:
                    if (Calendar.class.isAssignableFrom(fieldClass)) {
                        if (format == null) {
                            return ObjectReaderBaseModule.CalendarImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.CalendarImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.CalendarImpl(format);
                        }
                    }

                    if (fieldClass == ZonedDateTime.class) {
                        if (format == null) {
                            return ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.ZonedDateTimeImpl(format);
                        }
                    }

                    if (fieldClass == LocalDateTime.class) {
                        if (format == null) {
                            return ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.LocalDateTimeImpl(format);
                        }
                    }

                    if (fieldClass == Optional.class) {
                        if (fieldType instanceof ParameterizedType) {
                            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
                            if (actualTypeArguments.length == 1) {
                                Type paramType = actualTypeArguments[0];
                                Class<?> paramClass = TypeUtils.getClass(paramType);
                                return createFormattedObjectReader(paramType, paramClass, format);
                            }
                        }
                        return ObjectReaderBaseModule.OptionalImpl.INSTANCE;
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }

        ObjectReader formattedObjectReader = createFormattedObjectReader(fieldType, fieldClass, format);
        if (formattedObjectReader != null) {
            return  reader = formattedObjectReader;
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplMap.of(fieldType, fieldClass, features);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplList.of(fieldType, fieldClass, features);
        }

        return reader = jsonReader.getObjectReader(fieldType);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (!fieldClassSerializable && (jsonReader.getContext().getFeatures() & JSONReader.Feature.IgnoreNoneSerializable.mask) != 0) {
            jsonReader.skipValue();
            return;
        }

        if (fieldObjectReader == null) {
            fieldObjectReader = getObjectReader(jsonReader);
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if (reference.equals("..")) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        int offset = jsonReader.getOffset();
        try {
            Object value;
            if (jsonReader.nextIfNull()) {
                if (fieldClass == OptionalInt.class) {
                    value = OptionalInt.empty();
                } else if (fieldClass == OptionalLong.class) {
                    value = OptionalLong.empty();
                } else if (fieldClass == OptionalDouble.class) {
                    value = OptionalDouble.empty();
                } else if (fieldClass == Optional.class) {
                    value = Optional.empty();
                } else {
                    value = null;
                }
            } else if (jsonReader.isJSONB()) {
                if (fieldClass == Object.class) {
                    ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, features);
                    if (autoTypeObjectReader != null) {
                        value = autoTypeObjectReader.readJSONBObject(jsonReader, features);
                    } else {
                        value = jsonReader.readAny();
                    }
                } else {
                    value = fieldObjectReader.readJSONBObject(jsonReader, features);
                }
            } else {
                value = fieldObjectReader.readObject(jsonReader, features);
            }
            accept(object, value);
        } catch (Exception | IllegalAccessError ex) {
            throw new JSONException("read field '" + field.getDeclaringClass().getName() + "." + field.getName() + "' error, offset " + offset, ex);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader.getContext().getObjectReader(fieldType);
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if (reference.equals("..")) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        Object value = fieldObjectReader.readJSONBObject(jsonReader, features);
        accept(object, value);
    }

    @Override
    public void accept(T object, boolean value) {
        try {
            field.setBoolean(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, byte value) {
        try {
            field.setByte(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, short value) {
        try {
            field.setShort(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            field.setInt(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            field.setLong(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, float value) {
        try {
            field.setFloat(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, double value) {
        try {
            field.setDouble(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, char value) {
        try {
            field.setChar(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }
        return jsonReader.isJSONB()
                ? fieldObjectReader.readJSONBObject(jsonReader, features)
                : fieldObjectReader.readObject(jsonReader, features);
    }

    public Object readFieldValueJSONB(JSONReader jsonReader) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }
        return fieldObjectReader.readJSONBObject(jsonReader, features);
    }
}

package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
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

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }

        if (format != null && !format.isEmpty()) {
            String typeName = fieldType.getTypeName();
            switch (typeName) {
                case "java.sql.Time":
                    return reader = JdbcSupport.createTimeReader(format);
                case "java.sql.Timestamp":
                    return reader = JdbcSupport.createTimestampReader(format);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader(format);
                default:
                    break;
            }
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplMap.of(fieldType, fieldClass, features);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplList.of(fieldType, fieldClass, features);
        }

        if (byte[].class == fieldClass && "base64".equals(format)) {
            return reader = ObjectReaderBaseModule.Base64Impl.INSTANCE;
        }

        if (fieldClass == Calendar.class) {
            if (format == null) {
                return reader = ObjectReaderBaseModule.CalendarImpl.INSTANCE;
            }

            switch (format) {
                case "unixtime":
                    return reader = ObjectReaderBaseModule.CalendarImpl.INSTANCE_UNIXTIME;
                default:
                    return reader = new ObjectReaderBaseModule.CalendarImpl(format);
            }
        }

        if (fieldClass == ZonedDateTime.class) {
            if (format == null) {
                return reader = ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE;
            }

            switch (format) {
                case "unixtime":
                    return reader = ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE_UNIXTIME;
                default:
                    return reader = new ObjectReaderBaseModule.ZonedDateTimeImpl(format);
            }
        }

        if (fieldClass == LocalDateTime.class) {
            if (format == null) {
                return reader = ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE;
            }

            switch (format) {
                case "unixtime":
                    return reader = ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE_UNIXTIME;
                default:
                    return reader = new ObjectReaderBaseModule.LocalDateTimeImpl(format);
            }
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

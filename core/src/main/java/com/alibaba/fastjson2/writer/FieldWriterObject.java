package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JdbcSupport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

abstract class FieldWriterObject<T> extends FieldWriterImpl<T> {
    volatile Class initValueClass;
    volatile ObjectWriter initObjectWriter;
    final boolean unwrapped;
    final boolean array;

    protected FieldWriterObject(String name, int ordinal, long features, String format, Type fieldType, Class fieldClass) {
        super(name, ordinal, features, format, fieldType, fieldClass);
        this.unwrapped = "unwrapped".equals(format);

        if (fieldClass == Currency.class) {
            this.initValueClass = fieldClass;
            this.initObjectWriter = ObjectWriterImplCurrency.INSTANCE_FOR_FIELD;
        }

        array = fieldClass.isArray()
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldClass == AtomicLongArray.class
                || fieldClass == AtomicIntegerArray.class;
    }

    @Override
    public boolean unwrapped() {
        return unwrapped;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (initValueClass == null || initObjectWriter == ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
            initValueClass = valueClass;
            if (Map.class.isAssignableFrom(valueClass)) {
                if (fieldClass.isAssignableFrom(valueClass)) {
                    return initObjectWriter = ObjectWriterImplMap.of(fieldType, valueClass);
                } else {
                    return initObjectWriter = ObjectWriterImplMap.of(valueClass);
                }
            } else {
                if (Calendar.class.isAssignableFrom(valueClass)) {
                    if (format == null || format.isEmpty()) {
                        return initObjectWriter = ObjectWriterImplCalendar.INSTANCE;
                    }
                    switch (format) {
                        case "unixtime":
                            return initObjectWriter = ObjectWriterImplCalendar.INSTANCE_UNIXTIME;
                        default:
                            return initObjectWriter = new ObjectWriterImplCalendar(format);
                    }
                }

                if (ZonedDateTime.class.isAssignableFrom(valueClass)) {
                    if (format == null || format.isEmpty()) {
                        return initObjectWriter = ObjectWriterImplZonedDateTime.INSTANCE;
                    } else {
                        switch (format) {
                            case "unixtime":
                                return initObjectWriter = ObjectWriterImplZonedDateTime.INSTANCE_UNIXTIME;
                            default:
                                return initObjectWriter = new ObjectWriterImplZonedDateTime(format);
                        }
                    }
                }

                if (LocalDateTime.class.isAssignableFrom(valueClass)) {
                    if (format == null || format.isEmpty()) {
                        return initObjectWriter = ObjectWriterImplLocalDateTime.INSTANCE;
                    } else {
                        switch (format) {
                            case "unixtime":
                                return initObjectWriter = ObjectWriterImplLocalDateTime.INSTANCE_UNIXTIME;
                            default:
                                return initObjectWriter = new ObjectWriterImplLocalDateTime(format);
                        }
                    }
                }

                String className = valueClass.getName();
                switch (className) {
                    case "java.sql.Time":
                        return JdbcSupport.createTimeWriter(format);
                    case "java.sql.Timestamp":
                        return JdbcSupport.createTimestampWriter(format);
                    default:
                        return initObjectWriter = jsonWriter.getObjectWriter(valueClass);
                }
            }
        } else {
            if (initValueClass == valueClass) {
                if (initObjectWriter == null) {
                    if (Map.class.isAssignableFrom(valueClass)) {
                        if (fieldClass.isAssignableFrom(valueClass)) {
                            initObjectWriter = ObjectWriterImplMap.of(fieldType, valueClass);
                        } else {
                            initObjectWriter = ObjectWriterImplMap.of(valueClass);
                        }
                    } else {
                        initObjectWriter = jsonWriter.getObjectWriter(valueClass);
                    }
                }
                return initObjectWriter;
            } else {
                if (Map.class.isAssignableFrom(valueClass)) {
                    if (fieldClass.isAssignableFrom(valueClass)) {
                        return ObjectWriterImplMap.of(fieldType, valueClass);
                    } else {
                        return ObjectWriterImplMap.of(valueClass);
                    }
                } else {
                    return jsonWriter.getObjectWriter(valueClass);
                }
            }
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        if (!fieldClassSerializable && (jsonWriter.getFeatures(features) & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
            return false;
        }

        Object value;
        try {
            value = getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0
                    && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) == 0
            ) {
                writeFieldName(jsonWriter);
                if (array) {
                    jsonWriter.writeArrayNull();
                } else {
                    jsonWriter.writeNull();
                }
                return true;
            } else {
                return false;
            }
        }

        boolean refDetect = jsonWriter.isRefDetect(value);
        if (refDetect) {
            if (value == object) {
                writeFieldName(jsonWriter);
                jsonWriter.writeReference("..");
                return true;
            }

            String refPath = jsonWriter.setPath(name, value);
            if (refPath != null) {
                writeFieldName(jsonWriter);
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return true;
            }
        }

        Class<?> valueClass = value.getClass();
        if (valueClass == byte[].class) {
            writeBinary(jsonWriter, (byte[]) value);
            return true;
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, valueClass);
        if (valueWriter == null) {
            throw new JSONException("get objectWriter error : " + valueClass);
        }

        if (unwrapped) {
            if (value instanceof Map) {
                for (Iterator<Map.Entry> it = ((Map) value).entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = it.next();
                    String entryKey = entry.getKey().toString();
                    Object entryValue = entry.getValue();
                    if (entryValue == null) {
                        long features = this.features | jsonWriter.getFeatures();
                        if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                            continue;
                        }
                    }

                    jsonWriter.writeName(entryKey);
                    jsonWriter.writeColon();
                    if (entryValue == null) {
                        jsonWriter.writeNull();
                    } else {
                        Class<?> entryValueClass = entryValue.getClass();
                        ObjectWriter entryValueWriter = jsonWriter.getObjectWriter(entryValueClass);
                        entryValueWriter.write(jsonWriter, entryValue);
                    }
                }

                if (refDetect) {
                    jsonWriter.popPath(value);
                }
                return true;
            }

            if (valueWriter instanceof ObjectWriterAdapter) {
                ObjectWriterAdapter writerAdapter = (ObjectWriterAdapter) valueWriter;
                List<FieldWriter> fieldWriters = writerAdapter.getFieldWriters();
                for (FieldWriter fieldWriter : fieldWriters) {
                    fieldWriter.write(jsonWriter, value);
                }
                return true;
            }
        }

        writeFieldName(jsonWriter);
        boolean jsonb = jsonWriter.isJSONB();
        if ((features & JSONWriter.Feature.BeanToArray.mask) != 0) {
            if (jsonb) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, name, fieldType, features);
            } else {
                valueWriter.writeArrayMapping(jsonWriter, value, name, fieldType, features);
            }
        } else {
            if (jsonb) {
                valueWriter.writeJSONB(jsonWriter, value, name, fieldType, features);
            } else {
                valueWriter.write(jsonWriter, value, name, fieldType, features);
            }
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Object value = getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        Class<?> valueClass = value.getClass();
        ObjectWriter valueWriter;
        if (initValueClass == null) {
            initValueClass = valueClass;
            initObjectWriter = valueWriter = jsonWriter.getObjectWriter(valueClass);
        } else {
            if (initValueClass == valueClass) {
                valueWriter = initObjectWriter;
            } else {
                valueWriter = jsonWriter.getObjectWriter(valueClass);
            }
        }
        if (valueWriter == null) {
            throw new JSONException("get value writer error, valueType : " + valueClass);
        }

        boolean refDetect = jsonWriter.isRefDetect() && !ObjectWriterProvider.isNotReferenceDetect(valueClass);

        if (refDetect) {
            if (value == object) {
                jsonWriter.writeReference("..");
                return;
            }

            String refPath = jsonWriter.setPath(name, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        if (jsonWriter.isJSONB()) {
            if (jsonWriter.isBeanToArray()) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, name, fieldClass, features);
            } else {
                valueWriter.writeJSONB(jsonWriter, value, name, fieldClass, features);
            }
        } else {
            valueWriter.write(jsonWriter, value, name, fieldClass, features);
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}

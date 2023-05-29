package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.BeanUtils.SUPER;

public class FieldWriterObject<T>
        extends FieldWriter<T> {
    volatile Class initValueClass;
    final boolean unwrapped;
    final boolean array;
    final boolean number;

    static final AtomicReferenceFieldUpdater<FieldWriterObject, Class> initValueClassUpdater = AtomicReferenceFieldUpdater.newUpdater(
            FieldWriterObject.class,
            Class.class,
            "initValueClass"
    );

    protected FieldWriterObject(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
        this.unwrapped = (features & FieldInfo.UNWRAPPED_MASK) != 0;

        if (fieldClass == Currency.class) {
            this.initValueClass = fieldClass;
            this.initObjectWriter = ObjectWriterImplCurrency.INSTANCE_FOR_FIELD;
        }

        array = fieldClass.isArray()
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldClass == AtomicLongArray.class
                || fieldClass == AtomicIntegerArray.class;
        number = Number.class.isAssignableFrom(fieldClass);
    }

    @Override
    public ObjectWriter getInitWriter() {
        return initObjectWriter;
    }

    @Override
    public boolean unwrapped() {
        return unwrapped;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        final Class initValueClass = this.initValueClass;
        if (initValueClass == null || initObjectWriter == ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
            return getObjectWriterVoid(jsonWriter, valueClass);
        } else {
            boolean typeMatch = initValueClass == valueClass
                    || (initValueClass == Map.class && initValueClass.isAssignableFrom(valueClass));
            if (!typeMatch && initValueClass.isPrimitive()) {
                typeMatch = typeMatch(initValueClass, valueClass);
            }

            if (typeMatch) {
                ObjectWriter objectWriter;
                if (initObjectWriter == null) {
                    objectWriter = getObjectWriterTypeMatch(jsonWriter, valueClass);
                } else {
                    objectWriter = initObjectWriter;
                }
                return objectWriter;
            } else {
                return getObjectWriterTypeNotMatch(jsonWriter, valueClass);
            }
        }
    }

    private ObjectWriter getObjectWriterVoid(JSONWriter jsonWriter, Class valueClass) {
        ObjectWriter formattedWriter = null;
        if (BeanUtils.isExtendedMap(valueClass) && SUPER.equals(fieldName)) {
            JSONWriter.Context context = jsonWriter.context;
            boolean fieldBased = ((features | context.getFeatures()) & JSONWriter.Feature.FieldBased.mask) != 0;
            formattedWriter = context.provider.getObjectWriter(fieldType, fieldClass, fieldBased);
            if (initObjectWriter == null) {
                boolean success = initValueClassUpdater.compareAndSet(this, null, valueClass);
                if (success) {
                    initObjectWriterUpdater.compareAndSet(this, null, formattedWriter);
                }
            }
            return formattedWriter;
        }

        if (format == null) {
            JSONWriter.Context context = jsonWriter.context;
            boolean fieldBased = ((features | context.getFeatures()) & JSONWriter.Feature.FieldBased.mask) != 0;
            formattedWriter = context.provider.getObjectWriterFromCache(valueClass, valueClass, fieldBased);
        }

        final DecimalFormat decimalFormat = this.decimalFormat;
        if (valueClass == Float[].class) {
            if (decimalFormat != null) {
                formattedWriter = new ObjectWriterArrayFinal(Float.class, decimalFormat);
            } else {
                formattedWriter = ObjectWriterArrayFinal.FLOAT_ARRAY;
            }
        } else if (valueClass == Double[].class) {
            if (decimalFormat != null) {
                formattedWriter = new ObjectWriterArrayFinal(Double.class, decimalFormat);
            } else {
                formattedWriter = ObjectWriterArrayFinal.DOUBLE_ARRAY;
            }
        } else if (valueClass == float[].class) {
            if (decimalFormat != null) {
                formattedWriter = new ObjectWriterImplFloatValueArray(decimalFormat);
            } else {
                formattedWriter = ObjectWriterImplFloatValueArray.INSTANCE;
            }
        } else if (valueClass == double[].class) {
            if (decimalFormat != null) {
                formattedWriter = new ObjectWriterImplDoubleValueArray(decimalFormat);
            } else {
                formattedWriter = ObjectWriterImplDoubleValueArray.INSTANCE;
            }
        }

        if (formattedWriter == null) {
            formattedWriter = FieldWriter.getObjectWriter(fieldType, fieldClass, format, null, valueClass);
        }

        if (formattedWriter == null) {
            boolean success = initValueClassUpdater.compareAndSet(this, null, valueClass);
            formattedWriter = jsonWriter.getObjectWriter(valueClass);
            if (success) {
                initObjectWriterUpdater.compareAndSet(this, null, formattedWriter);
            }
        } else {
            if (initObjectWriter == null) {
                boolean success = initValueClassUpdater.compareAndSet(this, null, valueClass);
                if (success) {
                    initObjectWriterUpdater.compareAndSet(this, null, formattedWriter);
                }
            }
        }
        return formattedWriter;
    }

    static boolean typeMatch(Class initValueClass, Class valueClass) {
        return (initValueClass == int.class && valueClass == Integer.class)
                || (initValueClass == long.class && valueClass == Long.class)
                || (initValueClass == boolean.class && valueClass == Boolean.class)
                || (initValueClass == short.class && valueClass == Short.class)
                || (initValueClass == byte.class && valueClass == Byte.class)
                || (initValueClass == float.class && valueClass == Float.class)
                || (initValueClass == double.class && valueClass == Double.class)
                || (initValueClass == char.class && valueClass == Character.class);
    }

    private ObjectWriter getObjectWriterTypeNotMatch(JSONWriter jsonWriter, Class valueClass) {
        if (Map.class.isAssignableFrom(valueClass)) {
            if (fieldClass.isAssignableFrom(valueClass)) {
                return ObjectWriterImplMap.of(fieldType, valueClass);
            } else {
                return ObjectWriterImplMap.of(valueClass);
            }
        } else {
            ObjectWriter objectWriter = null;
            if (format != null) {
                objectWriter = FieldWriter.getObjectWriter(fieldType, fieldClass, format, null, valueClass);
            }
            if (objectWriter == null) {
                objectWriter = jsonWriter.getObjectWriter(valueClass);
            }
            return objectWriter;
        }
    }

    private ObjectWriter getObjectWriterTypeMatch(JSONWriter jsonWriter, Class valueClass) {
        ObjectWriter objectWriter;
        if (Map.class.isAssignableFrom(valueClass)) {
            if (fieldClass.isAssignableFrom(valueClass)) {
                objectWriter = ObjectWriterImplMap.of(fieldType, valueClass);
            } else {
                objectWriter = ObjectWriterImplMap.of(valueClass);
            }
        } else {
            objectWriter = jsonWriter.getObjectWriter(valueClass);
        }
        initObjectWriterUpdater.compareAndSet(this, null, objectWriter);
        return objectWriter;
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();

        if (!fieldClassSerializable && (features & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
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

        // (features & JSONWriter.Feature.WriteNullNumberAsZero.mask) != 0
        if (value == null) {
            if ((features & WriteNulls.mask) != 0 && (features & NotWriteDefaultValue.mask) == 0) {
                writeFieldName(jsonWriter);
                if (array) {
                    jsonWriter.writeArrayNull();
                } else if (number) {
                    jsonWriter.writeNumberNull();
                } else if (fieldClass == Appendable.class
                        || fieldClass == StringBuffer.class
                        || fieldClass == StringBuilder.class) {
                    jsonWriter.writeStringNull();
                } else {
                    jsonWriter.writeNull();
                }
                return true;
            } else {
                if ((features & (WriteNullNumberAsZero.mask | NullAsDefaultValue.mask)) != 0 && number) {
                    writeFieldName(jsonWriter);
                    jsonWriter.writeInt32(0);
                    return true;
                } else if ((features & (WriteNullBooleanAsFalse.mask | NullAsDefaultValue.mask)) != 0
                        && (fieldClass == Boolean.class || fieldClass == AtomicBoolean.class)
                ) {
                    writeFieldName(jsonWriter);
                    jsonWriter.writeBool(false);
                    return true;
                }
                return false;
            }
        }

        if (value == object) {
            if (fieldClass == Throwable.class
                    && field != null
                    && field.getDeclaringClass() == Throwable.class
            ) {
                return false;
            }
        }

        if ((features & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0 && !(value instanceof Serializable)) {
            return false;
        }

        boolean refDetect = jsonWriter.isRefDetect(value);
        if (refDetect) {
            if (value == object) {
                writeFieldName(jsonWriter);
                jsonWriter.writeReference("..");
                return true;
            }

            String refPath = jsonWriter.setPath(this, value);
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
                for (Map.Entry entry : (Iterable<Map.Entry>) ((Map) value).entrySet()) {
                    String entryKey = entry.getKey().toString();
                    Object entryValue = entry.getValue();
                    if (entryValue == null) {
                        if ((features & WriteNulls.mask) == 0) {
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
                List<FieldWriter> fieldWriters = writerAdapter.fieldWriters;
                for (FieldWriter fieldWriter : fieldWriters) {
                    fieldWriter.write(jsonWriter, value);
                }
                return true;
            }
        }

        writeFieldName(jsonWriter);
        boolean jsonb = jsonWriter.jsonb;
        if ((this.features & JSONWriter.Feature.BeanToArray.mask) != 0) {
            if (jsonb) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, fieldName, fieldType, this.features);
            } else {
                valueWriter.writeArrayMapping(jsonWriter, value, fieldName, fieldType, this.features);
            }
        } else {
            if (jsonb) {
                valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldType, this.features);
            } else {
                valueWriter.write(jsonWriter, value, fieldName, fieldType, this.features);
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
            valueWriter = jsonWriter.getObjectWriter(valueClass);
            initObjectWriterUpdater.compareAndSet(this, null, valueWriter);
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

            String refPath = jsonWriter.setPath(fieldName, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        if (jsonWriter.jsonb) {
            if (jsonWriter.isBeanToArray()) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, fieldName, fieldClass, features);
            } else {
                valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldClass, features);
            }
        } else {
            valueWriter.write(jsonWriter, value, fieldName, fieldClass, features);
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}

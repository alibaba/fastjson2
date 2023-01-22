package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;

class FieldReaderObjectField<T>
        extends FieldReaderObject<T> {
    FieldReaderObjectField(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Object defaultValue,
            JSONSchema schema,
            Field field
    ) {
        super(
                fieldName, fieldType == null ? field.getType() : fieldType,
                fieldClass,
                ordinal,
                features,
                format,
                null,
                defaultValue,
                schema,
                null,
                field,
                null
        );
    }

    @Override
    public void accept(T object, boolean value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == boolean.class) {
            UnsafeUtils.putBoolean(object, fieldOffset, value);
            return;
        }

        try {
            field.setBoolean(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, byte value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == byte.class) {
            UnsafeUtils.putByte(object, fieldOffset, value);
            return;
        }

        try {
            field.setByte(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, short value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == short.class) {
            UnsafeUtils.putShort(object, fieldOffset, value);
            return;
        }

        try {
            field.setShort(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == int.class) {
            UnsafeUtils.putInt(object, fieldOffset, value);
            return;
        }

        try {
            field.setInt(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == long.class) {
            UnsafeUtils.putLong(object, fieldOffset, value);
            return;
        }

        try {
            field.setLong(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, float value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == float.class) {
            UnsafeUtils.putFloat(object, fieldOffset, value);
            return;
        }

        try {
            field.setFloat(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, double value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == double.class) {
            UnsafeUtils.putDouble(object, fieldOffset, value);
            return;
        }

        try {
            field.setDouble(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, char value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (fieldOffset != -1 && fieldClass == char.class) {
            UnsafeUtils.putChar(object, fieldOffset, value);
            return;
        }

        try {
            field.setChar(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (value == null) {
            if ((features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
                return;
            }
        } else {
            if (fieldClass.isPrimitive()) {
                acceptPrimitive(object, value);
                return;
            }

            if (!fieldClass.isInstance(value)) {
                if (value instanceof String) {
                    String str = (String) value;
                    if (fieldClass == LocalDate.class) {
                        if (format != null) {
                            value = LocalDate.parse(str, DateTimeFormatter.ofPattern(format));
                        } else {
                            value = DateUtils.parseLocalDate(str);
                        }
                    } else if (fieldClass == java.util.Date.class) {
                        if (format != null) {
                            value = DateUtils.parseDate(str, format, DEFAULT_ZONE_ID);
                        } else {
                            value = DateUtils.parseDate(str);
                        }
                    }
                }

                if (!fieldClass.isInstance(value)) {
                    throw new JSONException("set " + fieldName + " error, not support type " + value.getClass());
                }
            }
        }

        if (fieldOffset != -1) {
            UnsafeUtils.putObject(object, fieldOffset, value);
            return;
        }

        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    final void acceptPrimitive(T object, Object value) {
        if (fieldClass == int.class) {
            if (value instanceof Number) {
                int intValue = ((Number) value).intValue();
                accept(object, intValue);
                return;
            }
        } else if (fieldClass == long.class) {
            if (value instanceof Number) {
                long longValue = ((Number) value).longValue();
                accept(object, longValue);
                return;
            }
        } else if (fieldClass == float.class) {
            if (value instanceof Number) {
                float floatValue = ((Number) value).floatValue();
                accept(object, floatValue);
                return;
            }
        } else if (fieldClass == double.class) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                accept(object, doubleValue);
                return;
            }
        } else if (fieldClass == short.class) {
            if (value instanceof Number) {
                short shortValue = ((Number) value).shortValue();
                accept(object, shortValue);
                return;
            }
        } else if (fieldClass == byte.class) {
            if (value instanceof Number) {
                byte byteValue = ((Number) value).byteValue();
                accept(object, byteValue);
                return;
            }
        } else if (fieldClass == char.class) {
            if (value instanceof Character) {
                char charValue = ((Character) value).charValue();
                accept(object, charValue);
                return;
            }
        } else if (fieldClass == boolean.class) {
            if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                accept(object, booleanValue);
                return;
            }
        }
        throw new JSONException("set " + fieldName + " error, type not support " + value.getClass());
    }
}

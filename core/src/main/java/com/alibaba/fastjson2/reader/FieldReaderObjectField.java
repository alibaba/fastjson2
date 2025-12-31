package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

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
            Locale locale,
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
                locale,
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

        propertyAccessor.setBoolean(object, value);
    }

    @Override
    public void accept(T object, byte value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setByte(object, value);
    }

    @Override
    public void accept(T object, short value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setShort(object, value);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setInt(object, value);
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setLong(object, value);
    }

    @Override
    public void accept(T object, float value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setFloat(object, value);
    }

    @Override
    public void accept(T object, double value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setDouble(object, value);
    }

    @Override
    public void accept(T object, char value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setChar(object, value);
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

            if (fieldType != fieldClass
                    && Map.class.isAssignableFrom(fieldClass)
                    && value instanceof Map
                    && fieldClass != Map.class
            ) {
                ObjectReader objectReader = getObjectReader(JSONFactory.createReadContext());
                value = objectReader.createInstance((Map) value);
            } else if (!fieldClass.isInstance(value)) {
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
                    value = TypeUtils.cast(value, fieldType);
                }
            }
        }

        propertyAccessor.setObject(object, value);
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
                char charValue = (Character) value;
                accept(object, charValue);
                return;
            }
        } else if (fieldClass == boolean.class) {
            if (value instanceof Boolean) {
                boolean booleanValue = (Boolean) value;
                accept(object, booleanValue);
                return;
            }
        }
        throw new JSONException("set " + fieldName + " error, type not support " + value.getClass());
    }
}

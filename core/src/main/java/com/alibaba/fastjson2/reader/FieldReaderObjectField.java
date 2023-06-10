package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

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
                null,
                field,
                null
        );
    }

    @Override
    public void accept(T object, boolean value) {
        if (fieldOffset != -1 && fieldClass == boolean.class) {
            JDKUtils.UNSAFE.putBoolean(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == byte.class) {
            JDKUtils.UNSAFE.putByte(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == short.class) {
            JDKUtils.UNSAFE.putShort(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == int.class) {
            JDKUtils.UNSAFE.putInt(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == long.class) {
            JDKUtils.UNSAFE.putLong(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == float.class) {
            JDKUtils.UNSAFE.putFloat(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == double.class) {
            JDKUtils.UNSAFE.putDouble(object, fieldOffset, value);
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
        if (fieldOffset != -1 && fieldClass == char.class) {
            JDKUtils.UNSAFE.putChar(object, fieldOffset, value);
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
                    if (fieldClass == java.util.Date.class) {
                        if (format != null) {
                            value = DateUtils.parseDate(str, format, ZoneId.DEFAULT_ZONE_ID);
                        } else {
                            long millis = DateUtils.parseMillis(str, ZoneId.DEFAULT_ZONE_ID);
                            if (millis == 0) {
                                value = null;
                            } else {
                                value = new Date(millis);
                            }
                        }
                    }
                }

                if (!fieldClass.isInstance(value)) {
                    value = TypeUtils.cast(value, fieldType);
                }
            }
        }

        if (fieldOffset != -1) {
            JDKUtils.UNSAFE.putObject(object, fieldOffset, value);
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

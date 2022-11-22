package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FieldReaderObject<T>
        extends FieldReader<T> {
    protected ObjectReader initReader;
    protected BiConsumer function;

    public FieldReaderObject(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field);
        this.function = function;
    }

    @Override
    public ObjectReader getInitReader() {
        return initReader;
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }

        ObjectReader formattedObjectReader = createFormattedObjectReader(fieldType, fieldClass, format, null);
        if (formattedObjectReader != null) {
            return reader = formattedObjectReader;
        }

        if (fieldClass != null && Map.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplMap.of(fieldType, fieldClass, features);
        } else if (fieldClass != null && Collection.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplList.of(fieldType, fieldClass, features);
        }

        return reader = jsonReader.getObjectReader(fieldType);
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (reader != null) {
            return reader;
        }

        ObjectReader formattedObjectReader = createFormattedObjectReader(fieldType, fieldClass, format, null);
        if (formattedObjectReader != null) {
            return reader = formattedObjectReader;
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplMap.of(fieldType, fieldClass, features);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            return reader = ObjectReaderImplList.of(fieldType, fieldClass, features);
        }

        return reader = context.getObjectReader(fieldType);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (!fieldClassSerializable) {
            long contextFeatures = jsonReader.getContext().getFeatures();
            if ((contextFeatures & JSONReader.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonReader.skipValue();
                return;
            } else if ((contextFeatures & JSONReader.Feature.ErrorOnNoneSerializable.mask) != 0) {
                throw new JSONException("not support none-Serializable");
            }
        }

        ObjectReader objectReader;
        if (this.initReader != null) {
            objectReader = this.initReader;
        } else {
            ObjectReader formattedObjectReader = createFormattedObjectReader(fieldType, fieldClass, format, locale);
            if (formattedObjectReader != null) {
                objectReader = this.initReader = formattedObjectReader;
            } else {
                objectReader = this.initReader = jsonReader.getContext().getObjectReader(fieldType);
            }
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if ("..".equals(reference)) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

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
                        value = autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                    } else {
                        value = jsonReader.readAny();
                    }
                } else {
                    value = objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                }
            } else {
                value = objectReader.readObject(jsonReader, fieldType, fieldName, features);
            }
            accept(object, value);

            if (noneStaticMemberClass) {
                BeanUtils.setNoneStaticMemberClassParent(value, object);
            }
        } catch (JSONSchemaValidException ex) {
            throw ex;
        } catch (Exception | IllegalAccessError ex) {
            Member member = this.field != null ? this.field : this.method;
            String message;
            if (member != null) {
                message = "read field '" + member.getDeclaringClass().getName() + "." + member.getName();
            } else {
                message = "read field " + fieldName + " error";
            }
            throw new JSONException(jsonReader.info(message), ex);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        if (!fieldClassSerializable && jsonReader.getType() != JSONB.Constants.BC_TYPED_ANY) {
            long contextFeatures = jsonReader.getContext().getFeatures();
            if ((contextFeatures & JSONReader.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonReader.skipValue();
                return;
            } else if ((contextFeatures & JSONReader.Feature.ErrorOnNoneSerializable.mask) != 0) {
                throw new JSONException("not support none-Serializable");
            }
        }

        if (initReader == null) {
            initReader = jsonReader.getContext().getObjectReader(fieldType);
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if ("..".equals(reference)) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        Object value = initReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        accept(object, value);
    }

    @Override
    public void accept(T object, boolean value) {
        accept(object, Boolean.valueOf(value));
    }

    @Override
    public void accept(T object, byte value) {
        accept(object, Byte.valueOf(value));
    }

    @Override
    public void accept(T object, short value) {
        accept(object, Short.valueOf(value));
    }

    @Override
    public void accept(T object, int value) {
        accept(object, Integer.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        accept(object, Long.valueOf(value));
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Float.valueOf(value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Double.valueOf(value));
    }

    @Override
    public void accept(T object, char value) {
        accept(object, Character.valueOf(value));
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (fieldClass == char.class) {
            if (value instanceof String) {
                String str = (String) value;
                if (str.length() > 0) {
                    value = str.charAt(0);
                } else {
                    value = '\0';
                }
            }
        }

        if (function != null) {
            function.accept(object, value);
            return;
        }

        try {
            if (method != null) {
                method.invoke(object, value);
                return;
            }
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (initReader == null) {
            initReader = getObjectReader(jsonReader);
        }

        Object object = jsonReader.isJSONB()
                ? initReader.readJSONBObject(jsonReader, fieldType, fieldName, features)
                : initReader.readObject(jsonReader, fieldType, fieldName, features);

        Function builder = initReader.getBuildFunction();
        if (builder != null) {
            object = builder.apply(object);
        }

        return object;
    }
}

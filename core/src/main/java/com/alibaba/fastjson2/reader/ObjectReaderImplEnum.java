package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;

public final class ObjectReaderImplEnum
        implements ObjectReader {
    final Method createMethod;
    final Type createMethodParamType;
    final Member valueField;
    final Type valueFieldType;

    final Class enumClass;
    final long typeNameHash;
    private final Enum[] enums;
    private final Enum[] ordinalEnums;
    private final long[] enumNameHashCodes;
    private String[] stringValues;
    private long[] intValues;

    public ObjectReaderImplEnum(
            Class enumClass,
            Method createMethod,
            Member valueField,
            Enum[] enums,
            Enum[] ordinalEnums,
            long[] enumNameHashCodes
    ) {
        this.enumClass = enumClass;
        this.createMethod = createMethod;
        this.valueField = valueField;
        Type valueFieldType = null;
        if (valueField instanceof Field) {
            valueFieldType = ((Field) valueField).getType();
        } else if (valueField instanceof Method) {
            valueFieldType = ((Method) valueField).getReturnType();
        }
        this.valueFieldType = valueFieldType;

        if (valueFieldType != null) {
            if (valueFieldType == String.class) {
                stringValues = new String[enums.length];
            } else {
                intValues = new long[enums.length];
            }

            for (int i = 0; i < enums.length; i++) {
                Enum e = enums[i];
                try {
                    Object fieldValue;
                    if (valueField instanceof Field) {
                        fieldValue = ((Field) valueField).get(e);
                    } else {
                        fieldValue = ((Method) valueField).invoke(e);
                    }

                    if (valueFieldType == String.class) {
                        stringValues[i] = (String) fieldValue;
                    } else if (fieldValue instanceof Number) {
                        intValues[i] = ((Number) fieldValue).longValue();
                    }
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }

        Type createMethodParamType = null;
        if (createMethod != null && createMethod.getParameterCount() == 1) {
            createMethodParamType = createMethod.getParameterTypes()[0];
        }
        this.createMethodParamType = createMethodParamType;

        this.typeNameHash = Fnv.hashCode64(TypeUtils.getTypeName(enumClass));
        this.enums = enums;
        this.ordinalEnums = ordinalEnums;
        this.enumNameHashCodes = enumNameHashCodes;
    }

    @Override
    public Class getObjectClass() {
        return enumClass;
    }

    public Enum getEnumByHashCode(long hashCode) {
        if (enums == null) {
            return null;
        }

        int enumIndex = Arrays.binarySearch(this.enumNameHashCodes, hashCode);

        if (enumIndex < 0) {
            return null;
        }

        return enums[enumIndex];
    }

    public Enum getEnum(String name) {
        if (name == null) {
            return null;
        }
        return getEnumByHashCode(Fnv.hashCode64(name));
    }

    public Enum getEnumByOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= ordinalEnums.length) {
            throw new JSONException("No enum ordinal " + enumClass.getCanonicalName() + "." + ordinal);
        }
        return ordinalEnums[ordinal];
    }

    public Enum of(int intValue) {
        Enum enumValue = null;
        if (valueField == null) {
            enumValue = getEnumByOrdinal(intValue);
        } else {
            try {
                if (valueField instanceof Field) {
                    for (Enum e : enums) {
                        if (((Field) valueField).getInt(e) == intValue) {
                            enumValue = e;
                            break;
                        }
                    }
                } else {
                    Method valueMethod = (Method) valueField;
                    for (Enum e : enums) {
                        if (((Number) valueMethod.invoke(e)).intValue() == intValue) {
                            enumValue = e;
                            break;
                        }
                    }
                }
            } catch (Exception error) {
                throw new JSONException("parse enum error, class " + enumClass.getName() + ", value " + intValue, error);
            }
        }

        if (enumValue == null) {
            throw new JSONException("None enum ordinal or value " + intValue);
        }
        return enumValue;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        byte type = jsonReader.getType();
        if (type == BC_TYPED_ANY) {
            ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(enumClass, 0L, features);
            if (autoTypeObjectReader != null) {
                if (autoTypeObjectReader != this) {
                    return autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                }
            } else {
                throw new JSONException(jsonReader.info("not support enumType : " + jsonReader.getString()));
            }
        }

        Enum fieldValue;
        boolean isInt = (type >= BC_INT32_NUM_MIN && type <= BC_INT32);
        if (isInt) {
            int ordinal;
            if (type <= BC_INT32_NUM_MAX) {
                ordinal = type;
                jsonReader.next();
            } else {
                ordinal = jsonReader.readInt32Value();
            }

            fieldValue = getEnumByOrdinal(ordinal);
        } else if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        } else {
            fieldValue = getEnumByHashCode(
                    jsonReader.readValueHashCode()
            );
            if (fieldValue == null) {
                long nameHash = jsonReader.getNameHashCodeLCase();
                fieldValue = getEnumByHashCode(nameHash);
            }
        }
        return fieldValue;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (createMethodParamType != null) {
            Object paramValue = jsonReader.read(createMethodParamType);
            try {
                return createMethod.invoke(null, paramValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException(jsonReader.info("create enum error, enumClass " + enumClass.getName() + ", paramValue " + paramValue), e);
            }
        }

        Enum fieldValue = null;
        if (jsonReader.isInt()) {
            int intValue = jsonReader.readInt32Value();
            if (valueField == null) {
                fieldValue = getEnumByOrdinal(intValue);
            } else {
                if (intValues != null) {
                    for (int i = 0; i < intValues.length; i++) {
                        if (intValues[i] == intValue) {
                            fieldValue = enums[i];
                            break;
                        }
                    }
                }

                if (fieldValue == null && jsonReader.isEnabled(JSONReader.Feature.ErrorOnEnumNotMatch)) {
                    throw new JSONException(jsonReader.info("parse enum error, class " + enumClass.getName() + ", " + valueField.getName() + " " + intValue));
                }
            }
        } else if (jsonReader.nextIfNullOrEmptyString()) {
            fieldValue = null;
        } else if (valueFieldType != null && valueFieldType == String.class && jsonReader.isString()) {
            String str = jsonReader.readString();
            for (int i = 0; i < stringValues.length; i++) {
                if (str.equals(stringValues[i])) {
                    fieldValue = enums[i];
                    break;
                }
            }
        } else {
            long hashCode = jsonReader.readValueHashCode();
            fieldValue = getEnumByHashCode(hashCode);
            if (hashCode == Fnv.MAGIC_HASH_CODE) {
                return null;
            }

            if (fieldValue == null) {
                fieldValue = getEnumByHashCode(
                        jsonReader.getNameHashCodeLCase()
                );
            }

            if (fieldValue == null && jsonReader.isEnabled(JSONReader.Feature.ErrorOnEnumNotMatch)) {
                String strVal = jsonReader.getString();
                throw new JSONException(jsonReader.info("parse enum error, class " + enumClass.getName() + ", value " + strVal));
            }
        }
        return fieldValue;
    }
}

package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;

final class ObjectReaderImplEnum implements ObjectReader {
    final Method createMethod;
    final Type createMethodParamType;
    final Member valueField;

    final Class enumClass;
    final long typeNameHash;
    protected final Enum[] enums;
    protected final Enum[] ordinalEnums;
    protected long[] enumNameHashCodes;

    public ObjectReaderImplEnum(
            Class enumClass,
            Method createMethod,
            Member valueField,
            Enum[] enums,
            Enum[] ordinalEnums,
            long[] enumNameHashCodes) {
        this.enumClass = enumClass;
        this.createMethod = createMethod;
        this.valueField = valueField;

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

    public Enum<?> valueOf(int ordinal) {
        return ordinalEnums[ordinal];
    }

    public Enum getEnumByOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= ordinalEnums.length) {
            throw new JSONException("No enum ordinal " + enumClass.getCanonicalName() + "." + ordinal);
        }
        return ordinalEnums[ordinal];
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        byte type = jsonReader.getType();
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeNameHash = jsonReader.readTypeHashCode();
            if (typeNameHash != this.typeNameHash) {
                throw new JSONException("not support enumType : " + jsonReader.getString());
            }
        }

        Enum fieldValue;
        boolean isInt = (type >= BC_INT32_NUM_MIN && type <= BC_INT32);
        if (isInt) {
            int ordinal = jsonReader.readInt32Value();
            fieldValue = getEnumByOrdinal(ordinal);
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
    public Object readObject(JSONReader jsonReader, long features) {
        if (createMethodParamType != null) {
            Object paramValue = jsonReader.read(createMethodParamType);
            try {
                return createMethod.invoke(null, paramValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create enum error, enumClass " + enumClass.getName() + ", paramValue " + paramValue, e);
            }
        }

        Enum fieldValue = null;
        if (jsonReader.isInt()) {
            int intValue = jsonReader.readInt32Value();
            if (valueField == null) {
                fieldValue = getEnumByOrdinal(intValue);
            } else {
                try {
                    if (valueField instanceof Field) {
                        for (Enum e : enums) {
                            if (((Field) valueField).getInt(e) == intValue) {
                                fieldValue = e;
                                break;
                            }
                        }
                    } else {
                        Method valueMethod = (Method) valueField;
                        for (Enum e : enums) {
                            if (((Number) valueMethod.invoke(e)).intValue() == intValue) {
                                fieldValue = e;
                                break;
                            }
                        }
                    }
                } catch (Exception error) {
                    throw new JSONException("parse enum error, class " + enumClass.getName() + ", value " + intValue, error);
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

            if (fieldValue == null && jsonReader.getContext().isEnable(JSONReader.Feature.ErrorOnEnumNotMatch)) {
                String strVal = jsonReader.getString();
                throw new JSONException("parse enum error, class " + enumClass.getName() + ", value " + strVal);
            }
        }
        return fieldValue;
    }
}

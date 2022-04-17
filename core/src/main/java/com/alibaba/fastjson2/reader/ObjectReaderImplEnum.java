package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import static com.alibaba.fastjson2.JSONB.Constants.*;

import java.util.Arrays;

final class ObjectReaderImplEnum implements ObjectReader {
    final Class enumClass;
    final long typeNameHash;
    protected final Enum[] enums;
    protected final Enum[] ordinalEnums;
    protected long[] enumNameHashCodes;

    final static long HASH_NAME = Fnv.hashCode64("name");
    final static long HASH_ORDINAL = Fnv.hashCode64("ordinal");

    public ObjectReaderImplEnum(Class enumClass, Enum[] enums, Enum[] ordinalEnums, long[] enumNameHashCodes) {
        this.enumClass = enumClass;
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
        Enum fieldValue;
        if (jsonReader.isInt()) {
            fieldValue = getEnumByOrdinal(
                    jsonReader.readInt32Value());
        } else {
            fieldValue = getEnumByHashCode(
                    jsonReader.readValueHashCode()
            );
            if (fieldValue == null) {
                fieldValue = getEnumByHashCode(
                        jsonReader.getNameHashCodeLCase()
                );
            }
        }
        return fieldValue;
    }
}

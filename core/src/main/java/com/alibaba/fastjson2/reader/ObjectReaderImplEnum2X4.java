package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import static com.alibaba.fastjson2.JSONB.Constants.*;

final class ObjectReaderImplEnum2X4 implements ObjectReader {
    final Class enumClass;
    final long typeNameHash;
    protected final Enum enum0;
    protected final Enum enum1;

    protected long enumNameHashCode00;
    protected long enumNameHashCode01;
    protected long enumNameHashCode10;
    protected long enumNameHashCode11;

    public ObjectReaderImplEnum2X4(Class enumClass, Enum[] enums, Enum[] ordinalEnums, long[] enumNameHashCodes) {
        this.enumClass = enumClass;
        this.typeNameHash = Fnv.hashCode64(TypeUtils.getTypeName(enumClass));
        this.enum0 = ordinalEnums[0];
        this.enum1 = ordinalEnums[1];

        int enum0Index = 0, enum1Index = 0;
        for (int i = 0; i < enumNameHashCodes.length; i++) {
            long hash = enumNameHashCodes[i];
            Enum e = enums[i];
            if (e == enum0) {
                if (enum0Index++ == 0) {
                    enumNameHashCode00 = hash;
                } else {
                    enumNameHashCode01 = hash;
                }
            } else if (e == enum1) {
                if (enum1Index++ == 0) {
                    enumNameHashCode10 = hash;
                } else {
                    enumNameHashCode11 = hash;
                }
            }
        }
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
            if (ordinal == 0) {
                fieldValue = enum0;
            } else if (ordinal == 1) {
                fieldValue = enum1;
            } else {
                fieldValue = null;
            }
        } else {
            long hashCode = jsonReader.readValueHashCode();
            if (enumNameHashCode00 == hashCode || enumNameHashCode01 == hashCode) {
                fieldValue = enum0;
            } else if (enumNameHashCode10 == hashCode || enumNameHashCode11 == hashCode) {
                fieldValue = enum1;
            } else {
                long hashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (enumNameHashCode00 == hashCodeLCase || enumNameHashCode01 == hashCodeLCase) {
                    fieldValue = enum0;
                } else if (enumNameHashCode10 == hashCodeLCase || enumNameHashCode11 == hashCodeLCase) {
                    fieldValue = enum1;
                } else {
                    fieldValue = null;
                }
            }
        }
        return fieldValue;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        Enum fieldValue;
        if (jsonReader.isInt()) {
            int ordinal = jsonReader.readInt32Value();
            if (ordinal == 0) {
                fieldValue = enum0;
            } else if (ordinal == 1) {
                fieldValue = enum1;
            } else {
                fieldValue = null;
            }
        } else {
            long hashCode = jsonReader.readValueHashCode();
            if (enumNameHashCode00 == hashCode || enumNameHashCode01 == hashCode) {
                fieldValue = enum0;
            } else if (enumNameHashCode10 == hashCode || enumNameHashCode11 == hashCode) {
                fieldValue = enum1;
            } else {
                long hashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (enumNameHashCode00 == hashCodeLCase || enumNameHashCode01 == hashCodeLCase) {
                    fieldValue = enum0;
                } else if (enumNameHashCode10 == hashCodeLCase || enumNameHashCode11 == hashCodeLCase) {
                    fieldValue = enum1;
                } else {
                    fieldValue = null;
                }
            }
        }
        return fieldValue;
    }
}

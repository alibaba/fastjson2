package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

abstract class FieldWriterEnum
        extends FieldWriter {
    volatile byte[][] valueNameCacheUTF8;
    volatile char[][] valueNameCacheUTF16;

    final Class enumType;
    final Enum[] enumConstants;
    final long[] hashCodes;

    protected FieldWriterEnum(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class<? extends Enum> enumType,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, enumType, enumType, field, method);

        this.enumType = enumType;
        this.enumConstants = enumType.getEnumConstants();
        this.hashCodes = new long[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            hashCodes[i] = Fnv.hashCode64(enumConstants[i].name());
        }
    }

    @Override
    public final void writeEnumJSONB(JSONWriter jsonWriter, Enum e) {
        if (e == null) {
            return;
        }

        long features = this.features | jsonWriter.getFeatures();
        boolean usingOrdinal = (features & (JSONWriter.Feature.WriteEnumUsingToString.mask | JSONWriter.Feature.WriteEnumsUsingName.mask)) == 0;
        boolean usingToString = (features & JSONWriter.Feature.WriteEnumUsingToString.mask) != 0;

        int ordinal = e.ordinal();
        SymbolTable symbolTable = jsonWriter.getSymbolTable();
        if (symbolTable != null && usingOrdinal && !usingToString) {
            int namingOrdinal = symbolTable.getOrdinalByHashCode(hashCodes[ordinal]);
            if (namingOrdinal >= 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeRaw(JSONB.Constants.BC_STR_ASCII);
                jsonWriter.writeInt32(-namingOrdinal);
                return;
            }
        }

        if (usingToString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(e.toString());
            return;
        }

        if (usingOrdinal) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt32(ordinal);
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeString(e.name());
    }

    @Override
    public final void writeEnum(JSONWriter jsonWriter, Enum e) {
        long features = this.features | jsonWriter.getFeatures();
        boolean usingOrdinal = (features & (JSONWriter.Feature.WriteEnumUsingToString.mask | JSONWriter.Feature.WriteEnumsUsingName.mask)) == 0;

        if ((features & JSONWriter.Feature.WriteEnumUsingToString.mask) != 0) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(e.toString());
            return;
        } else if (usingOrdinal) {
            writeFieldName(jsonWriter);
            jsonWriter.writeInt32(e.ordinal());
            return;
        }

        if (jsonWriter.isUTF8()) {
            int ordinal = e.ordinal();
            byte[] bytes = null;
            if (valueNameCacheUTF8 == null) {
                valueNameCacheUTF8 = new byte[enumConstants.length][];
            } else {
                bytes = valueNameCacheUTF8[ordinal];
            }

            if (bytes == null) {
                String name = enumConstants[ordinal].name();
                bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + name.length() + 2);
                bytes[nameWithColonUTF8.length] = '"';
                name.getBytes(0, name.length(), bytes, nameWithColonUTF8.length + 1);
                bytes[bytes.length - 1] = '"';
                valueNameCacheUTF8[ordinal] = bytes;
            }
            jsonWriter.writeNameRaw(bytes);
            return;
        }

        if (jsonWriter.isUTF16()) {
            int ordinal = e.ordinal();
            char[] chars = null;
            if (valueNameCacheUTF16 == null) {
                valueNameCacheUTF16 = new char[enumConstants.length][];
            } else {
                chars = valueNameCacheUTF16[ordinal];
            }

            if (chars == null) {
                String name = enumConstants[ordinal].name();
                chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + name.length() + 2);
                chars[nameWithColonUTF16.length] = '"';
                name.getChars(0, name.length(), chars, nameWithColonUTF16.length + 1);
                chars[chars.length - 1] = '"';
                valueNameCacheUTF16[ordinal] = chars;
            }
            jsonWriter.writeNameRaw(chars);
            return;
        }

        if (jsonWriter.isJSONB()) {
            writeEnumJSONB(jsonWriter, e);
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeString(e.name());
    }
}

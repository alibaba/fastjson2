package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.io.Serializable;
import java.lang.reflect.Type;

abstract class FieldWriterImpl<T> implements FieldWriter<T> {
    final String name;
    final int ordinal;
    final String format;
    final long hashCode;
    final byte[] nameWithColonUTF8;
    final char[] nameWithColonUTF16;
    byte[] nameJSONB;

    final long features;
    final Type fieldType;
    final Class fieldClass;
    final boolean fieldClassSerializable;

    FieldWriterImpl(String name, int ordinal, long features, String format, Type fieldType, Class fieldClass) {
        this.name = name;
        this.ordinal = ordinal;
        this.format = format;
        this.hashCode = Fnv.hashCode64(name);
        this.features = features;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null && Serializable.class.isAssignableFrom(fieldClass);

        int nameLength = name.length();
        int utflen = nameLength + 3;
        for (int i = 0; i < nameLength; ++i) {
            char c = name.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                // skip
            } else if (c > 0x07FF) {
                utflen += 2;
            } else {
                utflen += 1;
            }
        }

        byte[] bytes = new byte[utflen];
        int off = 0;
        bytes[off++] = '"';
        for (int i = 0; i < nameLength; ++i) {
            char c = name.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytes[off++] = (byte) c;
            } else if (c > 0x07FF) {
                // 2 bytes, 11 bits
                bytes[off++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }
        bytes[off++] = '"';
        bytes[off++] = ':';

        nameWithColonUTF8 = bytes;

        nameWithColonUTF16 = new char[nameLength + 3];
        nameWithColonUTF16[0] = '"';
        name.getChars(0, name.length(), nameWithColonUTF16, 1);
        nameWithColonUTF16[nameWithColonUTF16.length - 2] = '"';
        nameWithColonUTF16[nameWithColonUTF16.length - 1] = ':';
    }

    @Override
    public boolean isFieldClassSerializable() {
        return fieldClassSerializable;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void writeEnumJSONB(JSONWriter jsonWriter, Enum e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public Class getFieldClass() {
        return fieldClass;
    }

    @Override
    public Type getFieldType() {
        return fieldType;
    }

    @Override
    public long getFeatures() {
        return features;
    }

    @Override
    public final void writeFieldName(JSONWriter jsonWriter) {
        if (jsonWriter.isJSONB()) {
//                JSONB.SymbolTable symbolTable = jsonWriter.getSymbolTable();
//                if (symbolTable != null) {
//                    int ordinal = symbolTable.getOrdinalByHashCode(hashCode);
//                    if (ordinal >= 0) {
//                        jsonWriter.writeRaw(JSONB.Constants.TYPE_STR_ASCII);
//                        jsonWriter.writeInt32(-ordinal);
//                        return;
//                    }
//                }

            if (nameJSONB == null) {
                nameJSONB = JSONB.toBytes(name);
            }
            jsonWriter.writeNameRaw(nameJSONB, hashCode);
            return;
        }

        if (jsonWriter.isUTF8()) {
            jsonWriter.writeNameRaw(nameWithColonUTF8);
            return;
        }

        if (jsonWriter.isUTF16()) {
            jsonWriter.writeNameRaw(nameWithColonUTF16);
            return;
        }

        jsonWriter.writeName(name);
        jsonWriter.writeColon();
    }

    @Override
    public String toString() {
        return name;
    }
}

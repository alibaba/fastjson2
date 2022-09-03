package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

abstract class FieldWriterImpl<T>
        implements FieldWriter<T> {
    final String name;
    final int ordinal;
    final String format;
    final String label;
    final long hashCode;
    final byte[] nameWithColonUTF8;
    final char[] nameWithColonUTF16;
    byte[] nameJSONB;

    final long features;
    final Type fieldType;
    final Class fieldClass;
    final boolean fieldClassSerializable;
    JSONWriter.Path rootParentPath;

    FieldWriterImpl(String name, int ordinal, long features, String format, String label, Type fieldType, Class fieldClass) {
        this.name = name;
        this.ordinal = ordinal;
        this.format = format;
        this.label = label;
        this.hashCode = Fnv.hashCode64(name);
        this.features = features;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null && (Serializable.class.isAssignableFrom(fieldClass) || !Modifier.isFinal(fieldClass.getModifiers()));

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
    public String getLabel() {
        return label;
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
            if (nameJSONB == null) {
                nameJSONB = JSONB.toBytes(name);
            }
            jsonWriter.writeNameRaw(nameJSONB, hashCode);
            return;
        }

        boolean ueSingleQuotes = jsonWriter.isUseSingleQuotes();

        if (!ueSingleQuotes) {
            if (jsonWriter.isUTF8()) {
                jsonWriter.writeNameRaw(nameWithColonUTF8);
                return;
            }

            if (jsonWriter.isUTF16()) {
                jsonWriter.writeNameRaw(nameWithColonUTF16);
                return;
            }
        }

        jsonWriter.writeName(name);
        jsonWriter.writeColon();
    }

    public JSONWriter.Path getRootParentPath() {
        if (rootParentPath == null) {
            rootParentPath = new JSONWriter.Path(JSONWriter.Path.ROOT, name);
        }
        return rootParentPath;
    }

    @Override
    public String toString() {
        return name;
    }
}

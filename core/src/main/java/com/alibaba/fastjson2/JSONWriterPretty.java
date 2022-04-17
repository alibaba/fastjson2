package com.alibaba.fastjson2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

final class JSONWriterPretty extends JSONWriter {
    final JSONWriter jsonWriter;
    int indent;
    int startObjectOff;

    protected JSONWriterPretty(JSONWriter jsonWriter) {
        super(jsonWriter.context, jsonWriter.charset);
        this.jsonWriter = jsonWriter;
    }

    public void writeComma() {
        write0(',');
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
    }

    @Override
    public void writeInt32(int value) {
        jsonWriter.writeInt32(value);
    }

    @Override
    public void writeInt64(long i) {
        jsonWriter.writeInt64(i);
    }

    @Override
    public void writeFloat(float value) {
        jsonWriter.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        jsonWriter.writeDouble(value);
    }

    @Override
    public void writeDecimal(BigDecimal value) {
        jsonWriter.writeDecimal(value);
    }

    @Override
    public void writeBigInt(BigInteger value, long features) {
        jsonWriter.writeBigInt(value, features);
    }

    @Override
    public void writeUUID(UUID value) {
        jsonWriter.writeUUID(value);
    }

    @Override
    public void writeString(String str) {
        jsonWriter.writeString(str);
    }

    @Override
    public void writeDateTime19(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
    }

    @Override
    public void writeReference(String path) {
        jsonWriter.writeReference(path);
    }

    public void startObject() {
        level++;
        jsonWriter.startObject = true;
        write0('{');
        indent++;
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
        startObjectOff = jsonWriter.off;
    }

    public void endObject() {
        level--;
        indent--;
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
        write0('}');
        jsonWriter.startObject = false;
    }

    public void startArray() {
        level++;
        write0('[');
        indent++;
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
    }

    public void endArray() {
        level++;
        indent--;
        write0('\n');

        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }

        write0(']');
        jsonWriter.startObject = false;
    }

    public void writeRaw(char[] chars) {
        jsonWriter.writeRaw(chars);
    }

    public void writeNameRaw(char[] chars) {
        jsonWriter.writeNameRaw(chars);
    }

    public void writeRaw(byte[] bytes) {
        jsonWriter.writeRaw(bytes);
    }

    public void writeNameRaw(byte[] bytes) {
        jsonWriter.writeNameRaw(bytes);
    }

    public void writeName(String name) {
        if (jsonWriter.startObject) {
            jsonWriter.startObject = false;
        } else {
            writeComma();
        }

        jsonWriter.writeString(name);
    }

    public void writeNameRaw(byte[] bytes, int offset, int len) {
        jsonWriter.writeNameRaw(bytes, offset, len);
    }

    public void writeNameRaw(char[] bytes, int offset, int len) {
        jsonWriter.writeNameRaw(bytes, offset, len);
    }

    @Override
    protected void write0(char ch) {
        jsonWriter.write0(ch);
    }

    @Override
    public void writeRaw(String str) {
        jsonWriter.writeRaw(str);
    }

    public String toString() {
        return jsonWriter.toString();
    }
}

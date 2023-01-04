package com.alibaba.fastjson2;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

final class JSONWriterPretty
        extends JSONWriter {
    final JSONWriter jsonWriter;
    int indent;
    int startObjectOff;

    protected JSONWriterPretty(JSONWriter jsonWriter) {
        super(jsonWriter.context, null, false, jsonWriter.charset);
        this.jsonWriter = jsonWriter;
    }

    @Override
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
    public void writeString(char[] chars, int off, int len, boolean quote) {
        jsonWriter.writeString(chars, off, len, quote);
    }

    @Override
    public void writeLocalDate(LocalDate date) {
        jsonWriter.writeLocalDate(date);
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        jsonWriter.writeLocalDateTime(dateTime);
    }

    @Override
    public void writeDateTime14(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        jsonWriter.writeDateTime14(year, month, dayOfMonth, hour, minute, second);
    }

    @Override
    public void writeDateTime19(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
    }

    @Override
    public void writeDateTimeISO8601(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second,
            int millis,
            int offsetSeconds,
            boolean timeZone
    ) {
        jsonWriter.writeDateTimeISO8601(year, month, dayOfMonth, hour, minute, second, millis, offsetSeconds, timeZone);
    }

    @Override
    public void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        jsonWriter.writeDateYYYMMDD8(year, month, dayOfMonth);
    }

    @Override
    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        jsonWriter.writeDateYYYMMDD10(year, month, dayOfMonth);
    }

    @Override
    public void writeTimeHHMMSS8(int hour, int minute, int second) {
        jsonWriter.writeTimeHHMMSS8(hour, minute, second);
    }

    @Override
    public void writeLocalTime(LocalTime time) {
        jsonWriter.writeLocalTime(time);
    }

    @Override
    public void writeZonedDateTime(ZonedDateTime dateTime) {
        jsonWriter.writeZonedDateTime(dateTime);
    }

    @Override
    public void writeReference(String path) {
        jsonWriter.writeReference(path);
    }

    @Override
    public void startObject() {
        level++;
        jsonWriter.startObject = true;
        startObject = true;
        write0('{');
        indent++;
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
        startObjectOff = jsonWriter.off;
    }

    @Override
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

    @Override
    public void startArray() {
        level++;
        write0('[');
        indent++;
        write0('\n');
        for (int i = 0; i < indent; ++i) {
            write0('\t');
        }
    }

    @Override
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

    @Override
    public void writeRaw(char[] chars, int off, int len) {
        jsonWriter.writeRaw(chars, off, len);
    }

    @Override
    public void writeNameRaw(char[] chars) {
        if (jsonWriter.startObject) {
            jsonWriter.startObject = false;
        } else {
            writeComma();
        }

        jsonWriter.writeRaw(chars, 0, chars.length);
    }

    @Override
    public void writeRaw(byte[] bytes) {
        jsonWriter.writeRaw(bytes);
    }

    @Override
    public void writeNameRaw(byte[] bytes) {
        if (jsonWriter.startObject) {
            jsonWriter.startObject = false;
        } else {
            writeComma();
        }

        jsonWriter.writeRaw(bytes);
    }

    @Override
    public void writeName(String name) {
        if (jsonWriter.startObject) {
            jsonWriter.startObject = false;
        } else {
            writeComma();
        }

        jsonWriter.writeString(name);
    }

    @Override
    public void writeNameRaw(byte[] bytes, int offset, int len) {
        jsonWriter.writeNameRaw(bytes, offset, len);
    }

    @Override
    public void writeNameRaw(char[] bytes, int offset, int len) {
        jsonWriter.writeNameRaw(bytes, offset, len);
    }

    @Override
    public void writeNameAny(Object name) {
        if (jsonWriter.startObject) {
            jsonWriter.startObject = false;
        } else {
            writeComma();
        }

        jsonWriter.writeAny(name);
    }

    @Override
    protected void write0(char ch) {
        jsonWriter.write0(ch);
    }

    @Override
    public void writeRaw(String str) {
        jsonWriter.writeRaw(str);
    }

    @Override
    public void writeColon() {
        jsonWriter.writeColon();
    }

    @Override
    public void writeInt16(short[] value) {
        jsonWriter.writeInt16(value);
    }

    @Override
    public byte[] getBytes() {
        return jsonWriter.getBytes();
    }

    @Override
    public int size() {
        return jsonWriter.size();
    }

    @Override
    public byte[] getBytes(Charset charset) {
        return jsonWriter.getBytes(charset);
    }

    @Override
    public void flushTo(Writer to) {
        jsonWriter.flushTo(to);
    }

    @Override
    public int flushTo(OutputStream to) throws IOException {
        return jsonWriter.flushTo(to);
    }

    @Override
    public int flushTo(OutputStream to, Charset charset) throws IOException {
        return jsonWriter.flushTo(to, charset);
    }

    @Override
    public void writeBase64(byte[] bytes) {
        jsonWriter.writeBase64(bytes);
    }

    @Override
    public void writeHex(byte[] bytes) {
        jsonWriter.writeHex(bytes);
    }

    @Override
    public void writeRaw(char ch) {
        jsonWriter.writeRaw(ch);
    }

    @Override
    public void writeChar(char ch) {
        jsonWriter.writeChar(ch);
    }

    @Override
    public String toString() {
        return jsonWriter.toString();
    }
}

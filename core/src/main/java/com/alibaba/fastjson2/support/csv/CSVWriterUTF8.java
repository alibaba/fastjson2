package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.alibaba.fastjson2.util.IOUtils.DIGITS_K;

final class CSVWriterUTF8
        extends CSVWriter {
    static final byte[] BYTES_TRUE = "true".getBytes();
    static final byte[] BYTES_FALSE = "false".getBytes();
    static final byte[] BYTES_LONG_MIN = "-9223372036854775808".getBytes();

    final OutputStream out;
    final Charset charset;
    byte[] bytes;

    CSVWriterUTF8(
            OutputStream out,
            Charset charset,
            ZoneId zoneId,
            Feature... features
    ) {
        super(zoneId, features);
        this.out = out;
        this.charset = charset;
        this.bytes = new byte[1024 * 64];
    }

    protected void writeDirect(byte[] bytes, int off, int len) {
        try {
            out.write(bytes, off, len);
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeComma() {
        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = ',';
    }

    protected void writeQuote() {
        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = '"';
    }

    public void writeLine() {
        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
    }

    public void writeBoolean(boolean booleanValue) {
        byte[] valueBytes = booleanValue ? BYTES_TRUE : BYTES_FALSE;
        writeRaw(valueBytes);
    }

    public void writeInt64(long longValue) {
        int minCapacity = off + 21;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        off = IOUtils.writeInt64(bytes, off, longValue);
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        if (off + 11 >= this.bytes.length) {
            flush();
        }

        off = IOUtils.writeLocalDate(bytes, off, year, month, dayOfMonth);
    }

    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        if (off + 20 >= this.bytes.length) {
            flush();
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        off = IOUtils.writeLocalDate(bytes, off, year, month, dayOfMonth);
        bytes[off] = ' ';
        int v = DIGITS_K[hour];
        bytes[off + 1] = (byte) (v >> 8);
        bytes[off + 2] = (byte) v;
        bytes[off + 3] = ':';
        v = DIGITS_K[minute];
        bytes[off + 4] = (byte) (v >> 8);
        bytes[off + 5] = (byte) v;
        bytes[off + 6] = ':';
        v = DIGITS_K[second];
        bytes[off + 7] = (byte) (v >> 8);
        bytes[off + 8] = (byte) v;
        this.off = off + 9;
    }

    public void writeString(String value) {
        String str = value;
        byte[] bytes;
        if (JDKUtils.STRING_CODER != null
                && JDKUtils.STRING_VALUE != null
                && JDKUtils.STRING_CODER.applyAsInt(str) == JDKUtils.LATIN1) {
            bytes = JDKUtils.STRING_VALUE.apply(str);
        } else {
            bytes = str.getBytes(charset);
        }
        writeString(bytes);
    }

    public void writeInt32(int intValue) {
        int minCapacity = off + 11;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        off = IOUtils.writeInt32(bytes, off, intValue);
    }

    public void writeDouble(double value) {
        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = DoubleToDecimal.toString(value, this.bytes, off, true);
        off += size;
    }

    public void writeFloat(float value) {
        int minCapacity = off + 15;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = DoubleToDecimal.toString(value, this.bytes, off, true);
        off += size;
    }

    public void flush() {
        try {
            out.write(bytes, 0, off);
            off = 0;
            out.flush();
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeString(byte[] utf8) {
        if (utf8 == null || utf8.length == 0) {
            return;
        }

        final int len = utf8.length;
        int escapeCount = 0;
        boolean comma = false;

        if (utf8[0] == '"') {
            for (int i = 0; i < len; i++) {
                byte ch = utf8[i];
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                byte ch = utf8[i];
                if (ch == ',') {
                    comma = true;
                } else if (ch == '"') {
                    escapeCount++;
                }
            }
            if (!comma) {
                escapeCount = 0;
            }
        }

        if (escapeCount == 0) {
            writeRaw(utf8);
            return;
        }

        if (off + 2 + utf8.length + escapeCount >= bytes.length) {
            flush();
        }

        bytes[off++] = '"';
        for (int i = 0; i < utf8.length; i++) {
            byte ch = utf8[i];
            if (ch == '"') {
                bytes[off++] = '"';
                bytes[off++] = '"';
            } else {
                bytes[off++] = ch;
            }
        }
        bytes[off++] = '"';
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        int strlen = str.length();
        str.getBytes(0, strlen, bytes, off);
        off += strlen;
    }

    public void writeDecimal(long unscaledVal, int scale) {
        if (scale == 0) {
            writeInt64(unscaledVal);
            return;
        }

        if (unscaledVal == Long.MIN_VALUE || scale >= 20 || scale < 0) {
            writeDecimal(BigDecimal.valueOf(unscaledVal, scale));
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        off = IOUtils.writeDecimal(bytes, off, unscaledVal, scale);
    }

    protected void writeRaw(byte[] strBytes) {
        if (strBytes.length + off < bytes.length) {
            System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
            off += strBytes.length;
        } else {
            flush();
            if (strBytes.length >= bytes.length) {
                writeDirect(strBytes, 0, strBytes.length);
            } else {
                System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
                off += strBytes.length;
            }
        }
    }

    protected void writeRaw(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        byte[] strBytes = str.getBytes(charset);
        if (strBytes.length + off < bytes.length) {
            System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
            off += strBytes.length;
        } else {
            flush();
            if (strBytes.length >= bytes.length) {
                writeDirect(strBytes, 0, strBytes.length);
            } else {
                System.arraycopy(strBytes, 0, bytes, off, strBytes.length);
                off += strBytes.length;
            }
        }
    }

    public void writeLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) {
            return;
        }

        off = IOUtils.writeLocalDate(bytes, off, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());
        bytes[off++] = ' ';
        off = IOUtils.writeLocalTime(bytes, off, ldt.toLocalTime());
    }

    @Override
    public void close() throws IOException {
        if (off > 0) {
            flush();
        }

        out.close();
    }

    public String toString() {
        if (out instanceof ByteArrayOutputStream) {
            flush();
            byte[] strBytes = ((ByteArrayOutputStream) out).toByteArray();
            return new String(strBytes, StandardCharsets.UTF_8);
        }
        return super.toString();
    }
}

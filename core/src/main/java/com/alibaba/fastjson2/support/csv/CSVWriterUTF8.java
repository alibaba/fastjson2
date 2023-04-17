package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.RyuDouble;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        if (longValue == Long.MIN_VALUE) {
            writeRaw(BYTES_LONG_MIN);
            return;
        }

        int size = (longValue < 0) ? IOUtils.stringSize(-longValue) + 1 : IOUtils.stringSize(longValue);

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        IOUtils.getChars(longValue, off + size, bytes);
        off += size;
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        if (off + 10 >= this.bytes.length) {
            flush();
        }

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
    }

    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        if (off + 19 >= this.bytes.length) {
            flush();
        }

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
        bytes[off++] = ' ';
        bytes[off++] = (byte) (hour / 10 + '0');
        bytes[off++] = (byte) (hour % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (minute / 10 + '0');
        bytes[off++] = (byte) (minute % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (second / 10 + '0');
        bytes[off++] = (byte) (second % 10 + '0');
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
        if (intValue == Integer.MIN_VALUE) {
            writeRaw("-2147483648");
            return;
        }

        int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        IOUtils.getChars(intValue, off + size, bytes);
        off += size;
    }

    public void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = RyuDouble.toString(value, this.bytes, off);
        off += size;
    }

    public void writeFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 15;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = RyuDouble.toString(value, this.bytes, off);
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

        if (unscaledVal == Long.MIN_VALUE || scale >= 20) {
            writeDecimal(BigDecimal.valueOf(unscaledVal, scale));
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        int size = IOUtils.getDecimalChars(unscaledVal, scale, bytes, off);
        off += size;
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

        writeDateTime19(
                ldt.getYear(),
                ldt.getMonthValue(),
                ldt.getDayOfMonth(),
                ldt.getHour(),
                ldt.getMinute(),
                ldt.getSecond()
        );
        int nanoValue = ldt.getNano();
        if (nanoValue == 0) {
            return;
        }

        int value;
        if (nanoValue % 1000_000 == 0) {
            value = (nanoValue / 1000_000) + 1000;
        } else if (nanoValue % 1000 == 0) {
            value = (nanoValue / 1000) + 1000_000;
        } else {
            value = (nanoValue) + 1000_000_000;
        }
        int off = this.off;
        writeInt32(value);
        bytes[off] = '.';
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

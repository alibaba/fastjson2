package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.RyuDouble;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

final class CSVWriterUTF16
        extends CSVWriter {
    static final char[] BYTES_TRUE = "true".toCharArray();
    static final char[] BYTES_FALSE = "false".toCharArray();
    static final char[] BYTES_LONG_MIN = "-9223372036854775808".toCharArray();

    final Writer out;
    final char[] chars;

    CSVWriterUTF16(
            Writer out,
            ZoneId zoneId,
            Feature... features
    ) {
        super(zoneId, features);
        this.out = out;
        this.chars = new char[1024 * 64];
    }

    protected void writeDirect(char[] bytes, int off, int len) {
        try {
            out.write(bytes, off, len);
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeComma() {
        if (off + 1 == chars.length) {
            flush();
        }
        chars[off++] = ',';
    }

    protected void writeQuote() {
        if (off + 1 == chars.length) {
            flush();
        }
        chars[off++] = '"';
    }

    public void writeLine() {
        if (off + 1 == chars.length) {
            flush();
        }
        chars[off++] = '\n';
    }

    public void writeBoolean(boolean booleanValue) {
        char[] valueBytes = booleanValue ? BYTES_TRUE : BYTES_FALSE;
        writeRaw(valueBytes);
    }

    public void writeInt64(long longValue) {
        if (longValue == Long.MIN_VALUE) {
            writeRaw(BYTES_LONG_MIN);
            return;
        }

        int size = (longValue < 0) ? IOUtils.stringSize(-longValue) + 1 : IOUtils.stringSize(longValue);

        int minCapacity = off + size;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        IOUtils.getChars(longValue, off + size, chars);
        off += size;
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        if (off + 10 >= this.chars.length) {
            flush();
        }

        chars[off++] = (char) (year / 1000 + '0');
        chars[off++] = (char) ((year / 100) % 10 + '0');
        chars[off++] = (char) ((year / 10) % 10 + '0');
        chars[off++] = (char) (year % 10 + '0');
        chars[off++] = '-';
        chars[off++] = (char) (month / 10 + '0');
        chars[off++] = (char) (month % 10 + '0');
        chars[off++] = '-';
        chars[off++] = (char) (dayOfMonth / 10 + '0');
        chars[off++] = (char) (dayOfMonth % 10 + '0');
    }

    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        if (off + 19 >= this.chars.length) {
            flush();
        }

        chars[off++] = (char) (year / 1000 + '0');
        chars[off++] = (char) ((year / 100) % 10 + '0');
        chars[off++] = (char) ((year / 10) % 10 + '0');
        chars[off++] = (char) (year % 10 + '0');
        chars[off++] = '-';
        chars[off++] = (char) (month / 10 + '0');
        chars[off++] = (char) (month % 10 + '0');
        chars[off++] = '-';
        chars[off++] = (char) (dayOfMonth / 10 + '0');
        chars[off++] = (char) (dayOfMonth % 10 + '0');
        chars[off++] = ' ';
        chars[off++] = (char) (hour / 10 + '0');
        chars[off++] = (char) (hour % 10 + '0');
        chars[off++] = ':';
        chars[off++] = (char) (minute / 10 + '0');
        chars[off++] = (char) (minute % 10 + '0');
        chars[off++] = ':';
        chars[off++] = (char) (second / 10 + '0');
        chars[off++] = (char) (second % 10 + '0');
    }

    public void writeString(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        final int len = str.length();
        int escapeCount = 0;
        boolean comma = false;

        if (str.charAt(0) == '"') {
            for (int i = 0; i < len; i++) {
                char ch = str.charAt(i);
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                char ch = str.charAt(i);
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
            str.getChars(0, str.length(), chars, off);
            off += str.length();
            return;
        }

        if (off + 2 + str.length() + escapeCount >= chars.length) {
            flush();
        }

        chars[off++] = '"';
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '"') {
                chars[off++] = '"';
                chars[off++] = '"';
            } else {
                chars[off++] = ch;
            }
        }
        chars[off++] = '"';
    }

    public void writeInt32(int intValue) {
        if (intValue == Integer.MIN_VALUE) {
            writeRaw("-2147483648");
            return;
        }

        int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);

        int minCapacity = off + size;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        IOUtils.getChars(intValue, off + size, chars);
        off += size;
    }

    public void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        int size = RyuDouble.toString(value, this.chars, off);
        off += size;
    }

    public void writeFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return;
        }

        int minCapacity = off + 15;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        int size = RyuDouble.toString(value, this.chars, off);
        off += size;
    }

    public void flush() {
        try {
            out.write(chars, 0, off);
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

        String str = new String(utf8, 0, utf8.length, StandardCharsets.UTF_8);
        writeString(str);
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        int strlen = str.length();
        str.getChars(0, strlen, chars, off);
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
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        int size = IOUtils.getDecimalChars(unscaledVal, scale, chars, off);
        off += size;
    }

    void writeRaw(char[] chars) {
        if (chars.length + off < this.chars.length) {
            System.arraycopy(chars, 0, this.chars, off, chars.length);
            off += chars.length;
        } else {
            flush();
            if (chars.length >= this.chars.length) {
                writeDirect(chars, 0, chars.length);
            } else {
                System.arraycopy(chars, 0, this.chars, off, chars.length);
                off += chars.length;
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
        chars[off] = '.';
    }

    protected void writeRaw(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        if (str.length() + off >= chars.length) {
            flush();
        }
        str.getChars(0, str.length(), this.chars, off);
        off += str.length();
    }

    @Override
    public void close() throws IOException {
        if (off > 0) {
            flush();
        }

        out.close();
    }

    public String toString() {
        if (out instanceof StringWriter) {
            flush();
            return out.toString();
        }
        return super.toString();
    }
}

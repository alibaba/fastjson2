package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.DoubleToDecimal;
import com.alibaba.fastjson2.util.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.alibaba.fastjson2.util.IOUtils.PACKED_DIGITS_UTF16;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_CHAR_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

final class CSVWriterUTF16
        extends CSVWriter {
    static final char[] BYTES_TRUE = "true".toCharArray();
    static final char[] BYTES_FALSE = "false".toCharArray();

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

    void writeDirect(char[] bytes, int off, int len) {
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
        int minCapacity = off + 21;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        off = IOUtils.writeInt64(chars, off, longValue);
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        if (off + 11 >= this.chars.length) {
            flush();
        }

        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
    }

    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        if (off + 20 >= this.chars.length) {
            flush();
        }

        final char[] chars = this.chars;
        int off = this.off;
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
        chars[off] = ' ';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 1) << 1), PACKED_DIGITS_UTF16[hour]);
        chars[off + 3] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 4) << 1), PACKED_DIGITS_UTF16[minute]);
        chars[off + 6] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 7) << 1), PACKED_DIGITS_UTF16[second]);
        this.off = off + 9;
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
        int minCapacity = off + 11;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        off = IOUtils.writeInt32(chars, off, intValue);
    }

    public void writeDouble(double value) {
        int minCapacity = off + 24;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        int size = DoubleToDecimal.toString(value, this.chars, off, true);
        off += size;
    }

    public void writeFloat(float value) {
        int minCapacity = off + 15;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        int size = DoubleToDecimal.toString(value, this.chars, off, true);
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

        if (unscaledVal == Long.MIN_VALUE || scale >= 20 || scale < 0) {
            writeDecimal(BigDecimal.valueOf(unscaledVal, scale));
            return;
        }

        int minCapacity = off + 24;
        if (minCapacity - this.chars.length > 0) {
            flush();
        }

        off = IOUtils.writeDecimal(chars, off, unscaledVal, scale);
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

        off = IOUtils.writeLocalDate(chars, off, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());
        chars[off++] = ' ';
        off = IOUtils.writeLocalTime(chars, off, ldt.toLocalTime());
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

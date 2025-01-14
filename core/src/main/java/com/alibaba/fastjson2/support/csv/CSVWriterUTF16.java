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

import static com.alibaba.fastjson2.util.IOUtils.*;

final class CSVWriterUTF16
        extends CSVWriter {
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
        checkCapacity(1);
        chars[off++] = ',';
    }

    protected void writeQuote() {
        checkCapacity(1);
        chars[off++] = '"';
    }

    public void writeLine() {
        checkCapacity(1);
        chars[off++] = '\n';
    }

    public void writeBoolean(boolean booleanValue) {
        int size = booleanValue ? 4 : 5;
        checkCapacity(size);
        char[] chars = this.chars;
        int off = this.off;
        if (booleanValue) {
            IOUtils.putTrue(chars, off);
        } else {
            IOUtils.putFalse(chars, off);
        }
        this.off = off + size;
    }

    public void writeInt64(long longValue) {
        checkCapacity(20);
        off = IOUtils.writeInt64(chars, off, longValue);
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        checkCapacity(10);
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
    }

    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        checkCapacity(19);

        final char[] chars = this.chars;
        int off = this.off;
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
        chars[off] = ' ';
        writeDigitPair(chars, off + 1, hour);
        chars[off + 3] = ':';
        writeDigitPair(chars, off + 4, minute);
        chars[off + 6] = ':';
        writeDigitPair(chars, off + 7, second);
        this.off = off + 9;
    }

    public void writeString(final String str) {
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
                } else if (ch == '"' || ch == '\n' || ch == '\r') {
                    escapeCount++;
                }
            }
            if (!comma) {
                escapeCount = 0;
            }
        }

        if (escapeCount == 0 && !comma) {
            if (len + off >= chars.length) {
                flush();
                if (len > chars.length) {
                    try {
                        out.write(str);
                    } catch (IOException e) {
                        throw new JSONException("write csv error", e);
                    }
                    return;
                }
            }
            str.getChars(0, len, chars, off);
            off += len;
            return;
        }

        checkCapacity(2 + len + escapeCount);

        // 利用本地局部变量，可以提高遍历速度
        final char[] chars = this.chars;
        final int max = chars.length - 2;
        int off = this.off;
        chars[off++] = '"';
        for (int i = 0; i < len; ) {
            char ch = str.charAt(i++);
            if (ch == '"') {
                chars[off] = '"';
                chars[off + 1] = '"';
                off += 2;
            } else {
                chars[off++] = ch;
            }
            if (off >= max) {
                flush();
                off = this.off;
            }
        }
        chars[off] = '"';
        this.off = off + 1;
    }

    public void writeInt32(int intValue) {
        checkCapacity(11);
        off = IOUtils.writeInt32(chars, off, intValue);
    }

    public void writeDouble(double value) {
        checkCapacity(24);

        int size = DoubleToDecimal.toString(value, this.chars, off, true);
        off += size;
    }

    public void writeFloat(float value) {
        checkCapacity(15);

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

        String str = new String(utf8, StandardCharsets.UTF_8);
        writeString(str);
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        int strlen = str.length();

        checkCapacity(24);

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

        int off = this.off;
        char[] chars = this.chars;
        if (off + 24 > chars.length) {
            flush();
            off = 0;
        }
        this.off = IOUtils.writeDecimal(chars, off, unscaledVal, scale);
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

        // "yyyy-MM-dd HH:mm:ss"
        int off = this.off;
        char[] chars = this.chars;
        if (off + 19 > chars.length) {
            flush();
            off = 0;
        }
        off = IOUtils.writeLocalDate(chars, off, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());
        chars[off++] = ' ';
        this.off = IOUtils.writeLocalTime(chars, off, ldt.toLocalTime());
    }

    protected void writeRaw(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        checkCapacity(str.length());

        str.getChars(0, str.length(), this.chars, off);
        off += str.length();
    }

    void checkCapacity(int incr) {
        if (off + incr >= chars.length) {
            flush();
        }
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

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

import static com.alibaba.fastjson2.internal.Conf.BYTES;

final class CSVWriterUTF8
        extends CSVWriter {
    private static final short DOUBLE_QUOTE_2_LATIN1 = 0x22 | (0x22 << 8);

    final OutputStream out;
    final Charset charset;
    final byte[] bytes;

    CSVWriterUTF8(
            OutputStream out,
            Charset charset,
            ZoneId zoneId,
            Feature... features
    ) {
        super(zoneId, features);
        this.out = out;
        this.charset = charset;
        this.bytes = new byte[1024 * 512];
    }

    void writeDirect(byte[] bytes, int off, int len) {
        try {
            out.write(bytes, off, len);
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
        }
    }

    public void writeComma() {
        checkCapacity(1);
        bytes[off++] = ',';
    }

    protected void writeQuote() {
        checkCapacity(1);
        bytes[off++] = '"';
    }

    public void writeLine() {
        checkCapacity(1);
        bytes[off++] = '\n';
    }

    public void writeBoolean(boolean v) {
        checkCapacity(5);
        this.off = IOUtils.putBoolean(this.bytes, off, v);
    }

    public void writeInt64(long longValue) {
        checkCapacity(20); // -9223372036854775808
        off = IOUtils.writeInt64(bytes, off, longValue);
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        checkCapacity(10);
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
        checkCapacity(19);

        final byte[] bytes = this.bytes;
        int off = this.off;
        off = IOUtils.writeLocalDate(bytes, off, year, month, dayOfMonth);
        bytes[off] = ' ';
        IOUtils.writeLocalTime(bytes, off + 1, hour, minute, second);
        this.off = off + 9;
    }

    public void writeString(String value) {
        byte[] bytes;
        if (JDKUtils.STRING_CODER != null
                && JDKUtils.STRING_VALUE != null
                && JDKUtils.STRING_CODER.applyAsInt(value) == JDKUtils.LATIN1) {
            bytes = JDKUtils.STRING_VALUE.apply(value);
        } else {
            bytes = value.getBytes(charset);
        }
        writeString(bytes);
    }

    public void writeInt32(int intValue) {
        checkCapacity(11); // -2147483648

        off = IOUtils.writeInt32(bytes, off, intValue);
    }

    public void writeDouble(double value) {
        checkCapacity(24);

        off = NumberUtils.writeDouble(this.bytes, off, value, true, false);
    }

    public void writeFloat(float value) {
        checkCapacity(15);
        off = NumberUtils.writeFloat(bytes, off, value, true, false);
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
            for (byte ch : utf8) {
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (byte ch : utf8) {
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
            writeRaw(utf8);
            return;
        }

        checkCapacity(2 + len + escapeCount);

        // 利用本地局部变量，可以提高遍历速度
        final byte[] bytes = this.bytes;
        final int max = bytes.length - 2;
        int off = this.off;

        bytes[off++] = '"';
        for (byte ch : utf8) {
            if (ch == '"') {
                BYTES.putShortUnaligned(bytes, off, DOUBLE_QUOTE_2_LATIN1);
                off += 2;
            } else {
                bytes[off++] = ch;
            }
            if (off >= max) {
                flush();
                off = this.off;
            }
        }
        bytes[off++] = '"';
        this.off = off;
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        String str = value.toString();
        int strlen = str.length();

        checkCapacity(24);

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

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 24 > bytes.length) {
            flush();
            off = 0;
        }
        this.off = IOUtils.writeDecimal(bytes, off, unscaledVal, scale);
    }

    private void writeRaw(byte[] strBytes) {
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
        writeRaw(strBytes);
    }

    public void writeLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) {
            return;
        }

        // "yyyy-MM-dd HH:mm:ss"
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 19 > bytes.length) {
            flush();
            off = 0;
        }
        off = IOUtils.writeLocalDate(bytes, off, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());
        bytes[off++] = ' ';
        this.off = IOUtils.writeLocalTime(bytes, off, ldt.toLocalTime());
    }

    void checkCapacity(int incr) {
        if (off + incr >= bytes.length) {
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
        if (out instanceof ByteArrayOutputStream) {
            flush();
            byte[] strBytes = ((ByteArrayOutputStream) out).toByteArray();
            return new String(strBytes, StandardCharsets.UTF_8);
        }
        return super.toString();
    }
}

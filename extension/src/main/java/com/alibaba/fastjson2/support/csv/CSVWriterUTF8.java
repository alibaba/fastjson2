package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.RyuDouble;
import com.alibaba.fastjson2.util.RyuFloat;

import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static com.alibaba.fastjson2.util.IOUtils.*;

final class CSVWriterUTF8
        extends CSVWriter
        implements Flushable {
    static final byte[] BYTES_TRUE = "true".getBytes();
    static final byte[] BYTES_FALSE = "false".getBytes();

    final OutputStream out;
    final Charset charset;

    byte[] bytes;
    int off;

    public CSVWriterUTF8(OutputStream out, Charset charset, Feature... features) {
        super(features);
        this.out = out;
        this.charset = charset;
        this.bytes = new byte[1024 * 64];
    }

    public void writeRow(Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                if (off + 1 >= bytes.length) {
                    flush();
                }
                bytes[off++] = ',';
            }

            Object value = values[i];
            if (value == null) {
                continue;
            }

            if (value instanceof Optional) {
                Optional optional = (Optional) value;
                if (!optional.isPresent()) {
                    continue;
                }
                value = optional.get();
            }

            if (value instanceof Integer) {
                writeInt32((Integer) value);
            } else if (value instanceof Long) {
                writeInt64((Long) value);
            } else if (value instanceof String) {
                writeString((String) value);
            } else if (value instanceof Boolean) {
                boolean booleanValue = ((Boolean) value).booleanValue();
                byte[] valueBytes = booleanValue ? BYTES_TRUE : BYTES_FALSE;
                writeRaw(valueBytes);
            } else if (value instanceof Float) {
                writeDouble((Float) value);
            } else if (value instanceof Double) {
                writeDouble((Double) value);
            } else if (value instanceof Short) {
                writeInt32((Short) value);
            } else if (value instanceof Byte) {
                writeInt32((Byte) value);
            } else if (value instanceof BigDecimal) {
                writeRaw(value.toString());
            } else if (value instanceof BigInteger) {
                writeRaw(value.toString());
            } else if (value instanceof Date) {
                writeDate((Date) value);
            } else if (value instanceof Instant) {
                writeInstant((Instant) value);
            } else if (value instanceof LocalDate) {
                writeDate((LocalDate) value);
            } else if (value instanceof LocalDateTime) {
                writeDateTime((LocalDateTime) value);
            } else {
                String str = value.toString();
                writeString(str);
            }
        }

        if (off + 1 >= bytes.length) {
            flush();
        }
        bytes[off++] = '\n';
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

    protected void writeDirect(byte[] bytes, int off, int len) {
        try {
            out.write(bytes, off, len);
        } catch (IOException e) {
            throw new JSONException("write csv error", e);
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

    public void writeInt32(int i) {
        if (i == Integer.MIN_VALUE) {
            writeRaw("-2147483648");
            return;
        }

        int size;
        {
            int x = i < 0 ? -i : i;
            if (x <= 9) {
                size = 1;
            } else if (x <= 99) {
                size = 2;
            } else if (x <= 999) {
                size = 3;
            } else if (x <= 9999) {
                size = 4;
            } else if (x <= 99999) {
                size = 5;
            } else if (x <= 999999) {
                size = 6;
            } else if (x <= 9999999) {
                size = 7;
            } else if (x <= 99999999) {
                size = 8;
            } else if (x <= 999999999) {
                size = 9;
            } else {
                size = 10;
            }
            if (i < 0) {
                size++;
            }
        }

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        {
            int index = off + size;
            int q, r, p = index;
            byte sign = 0;

            if (i < 0) {
                sign = '-';
                i = -i;
            }

            while (i >= 65536) {
                q = i / 100;
                // really: r = i - (q * 100);
                r = i - ((q << 6) + (q << 5) + (q << 2));
                i = q;
                bytes[--p] = DigitOnes[r];
                bytes[--p] = DigitTens[r];
            }

            // Fall thru to fast mode for smaller numbers
            // assert(i <= 65536, i);
            for (; ; ) {
                q = (i * 52429) >>> (16 + 3);
                r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
                bytes[--p] = digits[r];
                i = q;
                if (i == 0) {
                    break;
                }
            }
            if (sign != 0) {
                bytes[--p] = sign;
            }
        }
        off += size;
    }

    public void writeInt64(long i) {
        if (i == Long.MIN_VALUE) {
            writeRaw("-9223372036854775808");
            return;
        }

        int size;
        {
            long x = i < 0 ? -i : i;
            if (x <= 9) {
                size = 1;
            } else if (x <= 99L) {
                size = 2;
            } else if (x <= 999L) {
                size = 3;
            } else if (x <= 9999L) {
                size = 4;
            } else if (x <= 99999L) {
                size = 5;
            } else if (x <= 999999L) {
                size = 6;
            } else if (x <= 9999999L) {
                size = 7;
            } else if (x <= 99999999L) {
                size = 8;
            } else if (x <= 999999999L) {
                size = 9;
            } else if (x <= 9999999999L) {
                size = 10;
            } else if (x <= 99999999999L) {
                size = 11;
            } else if (x <= 999999999999L) {
                size = 12;
            } else if (x <= 9999999999999L) {
                size = 13;
            } else if (x <= 99999999999999L) {
                size = 14;
            } else if (x <= 999999999999999L) {
                size = 15;
            } else if (x <= 9999999999999999L) {
                size = 16;
            } else if (x <= 99999999999999999L) {
                size = 17;
            } else if (x <= 999999999999999999L) {
                size = 18;
            } else {
                size = 19;
            }
            if (i < 0) {
                size++;
            }
        }

        int minCapacity = off + size;
        if (minCapacity - this.bytes.length > 0) {
            flush();
        }

        {
            int index = off + size;
            long q;
            int r;
            int charPos = index;
            byte sign = 0;

            if (i < 0) {
                sign = '-';
                i = -i;
            }

            // Get 2 digits/iteration using longs until quotient fits into an int
            while (i > Integer.MAX_VALUE) {
                q = i / 100;
                // really: r = i - (q * 100);
                r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
                i = q;
                bytes[--charPos] = DigitOnes[r];
                bytes[--charPos] = DigitTens[r];
            }

            // Get 2 digits/iteration using ints
            int q2;
            int i2 = (int) i;
            while (i2 >= 65536) {
                q2 = i2 / 100;
                // really: r = i2 - (q * 100);
                r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
                i2 = q2;
                bytes[--charPos] = DigitOnes[r];
                bytes[--charPos] = DigitTens[r];
            }

            // Fall thru to fast mode for smaller numbers
            // assert(i2 <= 65536, i2);
            for (; ; ) {
                q2 = (i2 * 52429) >>> (16 + 3);
                r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
                bytes[--charPos] = digits[r];
                i2 = q2;
                if (i2 == 0) {
                    break;
                }
            }
            if (sign != 0) {
                bytes[--charPos] = sign;
            }
        }
        off += size;
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            return;
        }

        byte[] bytes = str.getBytes(charset);
        writeString(bytes);
    }

    @Override
    protected void writeRaw(char ch) {
        if (ch < 0 || ch > 127) {
            throw new JSONException("unsupported operation");
        }

        if (off + 1 == bytes.length) {
            flush();
        }
        bytes[off++] = (byte) ch;
    }

    private void writeString(byte[] utf8Bytes) {
        if (utf8Bytes == null || utf8Bytes.length == 0) {
            return;
        }

        final int len = utf8Bytes.length;
        int escapeCount = 0;
        boolean comma = false;

        if (utf8Bytes[0] == '"') {
            for (int i = 0; i < len; i++) {
                byte ch = utf8Bytes[i];
                if (ch == '"') {
                    escapeCount++;
                }
            }
        } else {
            for (int i = 0; i < len; i++) {
                byte ch = utf8Bytes[i];
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
            writeRaw(utf8Bytes);
            return;
        }

        if (utf8Bytes[0] == '"') {
            byte[] bytes = new byte[utf8Bytes.length + 2 + escapeCount];
            int j = 0;
            bytes[0] = '"';
            for (int i = off, end = off + len; i < end; i++) {
                byte b = bytes[i];
                bytes[j++] = b;
                if (b == '"') {
                    bytes[j++] = b;
                }
            }
            bytes[j] = '"';
            writeRaw(bytes);
        } else {
            byte[] bytes = new byte[utf8Bytes.length + 2 + escapeCount];
            int j = 0;
            bytes[0] = '"';
            for (int i = off, end = off + len; i < end; i++) {
                byte b = bytes[i];
                bytes[j++] = b;
                if (b == '"') {
                    bytes[j++] = b;
                }
            }
            bytes[j] = '"';
            writeRaw(bytes);
        }
    }

    public void writeFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return;
        }

        if (off + 24 > this.bytes.length) {
            flush();
        }

        int len = RyuFloat.toString(value, bytes, off);
        off += len;
    }

    public void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return;
        }

        if (off + 24 > this.bytes.length) {
            flush();
        }

        int len = RyuDouble.toString(value, bytes, off);
        off += len;
    }

    public void writeBoolean(boolean value) {
        byte[] valueBytes = value ? BYTES_TRUE : BYTES_FALSE;
        writeRaw(valueBytes);
    }

    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            return;
        }

        writeRaw(value.toString());
    }

    public String toString() {
        if (out instanceof ByteArrayOutputStream) {
            flush();
            byte[] strBytes = ((ByteArrayOutputStream) out).toByteArray();
            return new String(strBytes, StandardCharsets.UTF_8);
        }
        return super.toString();
    }

    @Override
    public void close() throws IOException {
        if (off > 0) {
            flush();
        }

        out.close();
    }
}

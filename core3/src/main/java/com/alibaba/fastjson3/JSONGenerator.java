package com.alibaba.fastjson3;

import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.util.BufferPool;
import com.alibaba.fastjson3.util.JDKUtils;

import java.io.Closeable;
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

/**
 * High-performance JSON generator.
 * Uses pre-computed digit lookup tables and bulk memory operations.
 *
 * <pre>
 * try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
 *     generator.startObject();
 *     generator.writeName("key");
 *     generator.writeString("value");
 *     generator.endObject();
 *     String json = generator.toString();
 * }
 * </pre>
 */
public abstract sealed class JSONGenerator implements Closeable, Flushable
        permits JSONGenerator.Char, JSONGenerator.UTF8 {
    // Pre-computed lookup tables for fast number writing (wast-style optimization)
    static final int[] TWO_DIGITS_32 = new int[100];
    static final long[] FOUR_DIGITS_64 = new long[10000];
    static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    // Byte-oriented 2-digit lookup for UTF-8 int writing
    static final byte[] DIGIT_TENS = new byte[100];
    static final byte[] DIGIT_ONES = new byte[100];
    // Packed 2-digit lookup: short[i] = tens_byte | (ones_byte << 8) for Unsafe.putShort
    static final short[] PACKED_DIGITS = new short[100];
    // Pre-computed escape table
    static final char[] ESCAPE_CHARS = new char[128];

    static {
        // Initialize 2-digit lookup table (char-based, for Char)
        for (int i = 0; i < 100; i++) {
            int d0 = i / 10 + '0';
            int d1 = i % 10 + '0';
            TWO_DIGITS_32[i] = (d0 << 16) | d1;
            DIGIT_TENS[i] = (byte) d0;
            DIGIT_ONES[i] = (byte) d1;
            // Little-endian: low byte = tens, high byte = ones
            PACKED_DIGITS[i] = (short) (d0 | (d1 << 8));
        }

        // Initialize 4-digit lookup table
        for (int i = 0; i < 10000; i++) {
            int d0 = i / 1000 + '0';
            int d1 = (i / 100) % 10 + '0';
            int d2 = (i / 10) % 10 + '0';
            int d3 = i % 10 + '0';
            FOUR_DIGITS_64[i] = ((long) d0 << 48) | ((long) d1 << 32) | ((long) d2 << 16) | d3;
        }

        // Initialize escape table
        ESCAPE_CHARS['"'] = '"';
        ESCAPE_CHARS['\\'] = '\\';
        ESCAPE_CHARS['/'] = '/';
        ESCAPE_CHARS['\b'] = 'b';
        ESCAPE_CHARS['\f'] = 'f';
        ESCAPE_CHARS['\n'] = 'n';
        ESCAPE_CHARS['\r'] = 'r';
        ESCAPE_CHARS['\t'] = 't';
    }

    protected final long features;
    protected int count;

    // Filters — null when no filters configured (zero overhead)
    public PropertyFilter[] propertyFilters;
    public ValueFilter[] valueFilters;
    public NameFilter[] nameFilters;

    protected JSONGenerator(long features) {
        this.features = features;
    }

    public void setFilters(PropertyFilter[] pf, ValueFilter[] vf, NameFilter[] nf) {
        this.propertyFilters = pf;
        this.valueFilters = vf;
        this.nameFilters = nf;
    }

    public boolean hasFilters() {
        return propertyFilters != null;
    }

    /**
     * Create a UTF-16 writer (char-based, optimal for String output).
     */
    public static JSONGenerator of() {
        return new Char(0);
    }

    /**
     * Create a writer with features.
     */
    public static JSONGenerator of(WriteFeature... features) {
        return new Char(WriteFeature.of(features));
    }

    /**
     * Create a UTF-8 writer (byte-based, optimal for OutputStream/byte[] output).
     */
    public static JSONGenerator ofUTF8() {
        return new UTF8(0);
    }

    /**
     * Create a UTF-8 writer with features.
     */
    public static JSONGenerator ofUTF8(WriteFeature... features) {
        return new UTF8(WriteFeature.of(features));
    }

    /**
     * Check if feature is enabled.
     */
    public boolean isEnabled(WriteFeature feature) {
        return (features & feature.mask) != 0;
    }

    // ---- Structure tokens ----

    public abstract void startObject();
    public abstract void endObject();
    public abstract void startArray();
    public abstract void endArray();

    // ---- Field name ----

    public abstract void writeName(String name);

    /**
     * Write a pre-encoded field name token ("name":) using bulk copy.
     * This is faster than writeName() as the encoding is pre-computed.
     *
     * @param nameChars pre-encoded as char[] for char-based generators
     * @param nameBytes pre-encoded as byte[] (UTF-8) for byte-based generators
     */
    public abstract void writePreEncodedName(char[] nameChars, byte[] nameBytes);

    /**
     * Write a pre-encoded field name using long[] bulk writes (8 bytes at a time via Unsafe).
     * Default delegates to writePreEncodedName; UTF8 subclass overrides.
     */
    public void writePreEncodedNameLongs(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
        writePreEncodedName(nameChars, nameBytes);
    }

    // ---- Fused name+value writers (single ensureCapacity) ----

    /**
     * Write pre-encoded field name + string value in one fused operation.
     * Default delegates to separate calls; UTF8 subclass overrides for performance.
     */
    public void writeNameString(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
        writePreEncodedName(nameChars, nameBytes);
        writeString(value);
    }

    /**
     * Write pre-encoded field name + int value in one fused operation.
     */
    public void writeNameInt32(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
        writePreEncodedName(nameChars, nameBytes);
        writeInt32(value);
    }

    public void writeNameDouble(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
        writePreEncodedName(nameChars, nameBytes);
        writeDouble(value);
    }

    public void writeNameBool(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
        writePreEncodedName(nameChars, nameBytes);
        writeBool(value);
    }

    // ---- Value writers ----

    public abstract void writeNull();
    public abstract void writeTrue();
    public abstract void writeFalse();
    public abstract void writeBool(boolean val);
    public abstract void writeInt32(int val);
    public abstract void writeInt64(long val);
    public abstract void writeFloat(float val);
    public abstract void writeDouble(double val);
    public abstract void writeDecimal(BigDecimal val);
    public abstract void writeString(String val);
    public abstract void writeRaw(String raw);

    // ---- Name-Value convenience ----

    /**
     * Write name:value pair for string value.
     */
    public void writeNameValue(String name, String value) {
        writeName(name);
        writeString(value);
    }

    /**
     * Write name:value pair for int value.
     */
    public void writeNameValue(String name, int value) {
        writeName(name);
        writeInt32(value);
    }

    /**
     * Write name:value pair for long value.
     */
    public void writeNameValue(String name, long value) {
        writeName(name);
        writeInt64(value);
    }

    /**
     * Write name:value pair for boolean value.
     */
    public void writeNameValue(String name, boolean value) {
        writeName(name);
        writeBool(value);
    }

    // ---- Object serialization ----

    /**
     * Write any Java object as JSON.
     */
    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }
        if (value instanceof String) {
            writeString((String) value);
        } else if (value instanceof Integer) {
            writeInt32((Integer) value);
        } else if (value instanceof Long) {
            writeInt64((Long) value);
        } else if (value instanceof Boolean) {
            writeBool((Boolean) value);
        } else if (value instanceof Double) {
            writeDouble((Double) value);
        } else if (value instanceof Float) {
            writeFloat((Float) value);
        } else if (value instanceof BigDecimal) {
            writeDecimal((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            writeRaw(value.toString());
            writeRaw(",");
        } else if (value instanceof Short) {
            writeInt32((Short) value);
        } else if (value instanceof Byte) {
            writeInt32((Byte) value);
        } else if (value instanceof AtomicInteger ai) {
            writeInt32(ai.intValue());
        } else if (value instanceof AtomicLong al) {
            writeInt64(al.longValue());
        } else if (value instanceof AtomicBoolean ab) {
            writeBool(ab.get());
        } else if (value instanceof AtomicReference<?> ar) {
            writeAny(ar.get());
        } else if (value instanceof AtomicIntegerArray aia) {
            startArray();
            for (int i = 0, len = aia.length(); i < len; i++) {
                writeInt32(aia.get(i));
            }
            endArray();
        } else if (value instanceof AtomicLongArray ala) {
            startArray();
            for (int i = 0, len = ala.length(); i < len; i++) {
                writeInt64(ala.get(i));
            }
            endArray();
        } else if (value instanceof JSONObject) {
            writeJSONObject((JSONObject) value);
        } else if (value instanceof JSONArray) {
            writeJSONArray((JSONArray) value);
        } else if (value instanceof Map<?, ?> map) {
            startObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                writeName(String.valueOf(entry.getKey()));
                writeAny(entry.getValue());
            }
            endObject();
        } else if (value instanceof Collection<?> coll) {
            startArray();
            for (Object item : coll) {
                writeAny(item);
            }
            endArray();
        } else if (value instanceof Object[] arr) {
            startArray();
            for (Object item : arr) {
                writeAny(item);
            }
            endArray();
        } else if (value instanceof Enum<?> e) {
            writeString(e.name());
        } else if (value instanceof LocalDateTime ldt) {
            writeString(ldt.toString());
        } else if (value instanceof LocalDate ld) {
            writeString(ld.toString());
        } else if (value instanceof LocalTime lt) {
            writeString(lt.toString());
        } else if (value instanceof Instant inst) {
            writeString(inst.toString());
        } else if (value instanceof ZonedDateTime zdt) {
            writeString(zdt.toString());
        } else if (value instanceof OffsetDateTime odt) {
            writeString(odt.toString());
        } else if (value instanceof Date date) {
            writeString(date.toInstant().toString());
        } else {
            // Try ObjectWriter-based serialization
            @SuppressWarnings("unchecked")
            ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) ObjectMapper.shared().getObjectWriter(value.getClass());
            if (objectWriter != null) {
                objectWriter.write(this, value, null, null, features);
            } else {
                writeString(value.toString());
            }
        }
    }

    private void writeJSONObject(JSONObject obj) {
        startObject();
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            writeName(entry.getKey());
            writeAny(entry.getValue());
        }
        endObject();
    }

    private void writeJSONArray(JSONArray arr) {
        startArray();
        for (int i = 0, size = arr.size(); i < size; i++) {
            writeAny(arr.get(i));
        }
        endArray();
    }

    // ---- Output ----

    /**
     * Get result as byte array (UTF-8).
     */
    public abstract byte[] toByteArray();

    /**
     * Get the number of bytes/chars written.
     */
    public int size() {
        return count;
    }

    @Override
    public void flush() {
        // default no-op
    }

    @Override
    public void close() {
        // default no-op; subclasses may return buffers to pool
    }

    // ---- Public implementations ----

    public static final class Char extends JSONGenerator {
        private char[] buf;
        private boolean pooled;

        Char(long features) {
            super(features);
            this.buf = BufferPool.borrowCharBuffer();
            this.pooled = true;
        }

        private void ensureCapacity(int minCap) {
            if (minCap > buf.length) {
                int newCap = Math.max(buf.length + (buf.length >> 1), minCap);
                char[] newBuf = new char[newCap];
                System.arraycopy(buf, 0, newBuf, 0, count);
                buf = newBuf;
            }
        }

        @Override
        public void startObject() {
            ensureCapacity(count + 1);
            buf[count++] = '{';
        }

        @Override
        public void endObject() {
            // remove trailing comma if present
            if (count > 0 && buf[count - 1] == ',') {
                count--;
            }
            ensureCapacity(count + 2);
            buf[count++] = '}';
            buf[count++] = ',';
        }

        @Override
        public void startArray() {
            ensureCapacity(count + 1);
            buf[count++] = '[';
        }

        @Override
        public void endArray() {
            if (count > 0 && buf[count - 1] == ',') {
                count--;
            }
            ensureCapacity(count + 2);
            buf[count++] = ']';
            buf[count++] = ',';
        }

        @Override
        public void writeName(String name) {
            int len = name.length();
            ensureCapacity(count + len + 3);
            buf[count++] = '"';
            name.getChars(0, len, buf, count);
            count += len;
            buf[count++] = '"';
            buf[count++] = ':';
        }

        @Override
        public void writePreEncodedName(char[] nameChars, byte[] nameBytes) {
            int len = nameChars.length;
            ensureCapacity(count + len);
            System.arraycopy(nameChars, 0, buf, count, len);
            count += len;
        }

        @Override
        public void writeNull() {
            ensureCapacity(count + 5);
            buf[count++] = 'n';
            buf[count++] = 'u';
            buf[count++] = 'l';
            buf[count++] = 'l';
            buf[count++] = ',';
        }

        @Override
        public void writeTrue() {
            ensureCapacity(count + 5);
            buf[count++] = 't';
            buf[count++] = 'r';
            buf[count++] = 'u';
            buf[count++] = 'e';
            buf[count++] = ',';
        }

        @Override
        public void writeFalse() {
            ensureCapacity(count + 6);
            buf[count++] = 'f';
            buf[count++] = 'a';
            buf[count++] = 'l';
            buf[count++] = 's';
            buf[count++] = 'e';
            buf[count++] = ',';
        }

        @Override
        public void writeBool(boolean val) {
            if (val) {
                writeTrue();
            } else {
                writeFalse();
            }
        }

        @Override
        public void writeInt32(int val) {
            ensureCapacity(count + 12);
            count += writeIntToChars(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeInt64(long val) {
            ensureCapacity(count + 21);
            count += writeLongToChars(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeFloat(float val) {
            String s = Float.toString(val);
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeDouble(double val) {
            String s = Double.toString(val);
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeDecimal(BigDecimal val) {
            if (val == null) {
                writeNull();
                return;
            }
            String s;
            if (isEnabled(WriteFeature.WriteBigDecimalAsPlain)) {
                s = val.toPlainString();
            } else {
                s = val.toString();
            }
            int len = s.length();
            ensureCapacity(count + len + 1);
            s.getChars(0, len, buf, count);
            count += len;
            buf[count++] = ',';
        }

        @Override
        public void writeString(String val) {
            if (val == null) {
                writeNull();
                return;
            }
            int len = val.length();
            ensureCapacity(count + len * 6 + 3); // worst case: all chars escaped as \\uNNNN
            buf[count++] = '"';
            for (int i = 0; i < len; i++) {
                char ch = val.charAt(i);
                if (ch < 128) {
                    char escaped = ESCAPE_CHARS[ch];
                    if (escaped != 0 && ch != '/') {
                        buf[count++] = '\\';
                        buf[count++] = escaped;
                    } else if (ch < 0x20) {
                        // control character
                        buf[count++] = '\\';
                        buf[count++] = 'u';
                        buf[count++] = '0';
                        buf[count++] = '0';
                        buf[count++] = DIGITS[ch >> 4];
                        buf[count++] = DIGITS[ch & 0xF];
                    } else {
                        buf[count++] = ch;
                    }
                } else {
                    buf[count++] = ch;
                }
            }
            buf[count++] = '"';
            buf[count++] = ',';
        }

        @Override
        public void writeRaw(String raw) {
            int len = raw.length();
            ensureCapacity(count + len);
            raw.getChars(0, len, buf, count);
            count += len;
        }

        private int outputCount() {
            // Strip trailing comma from root-level output
            int c = count;
            if (c > 0 && buf[c - 1] == ',') {
                c--;
            }
            return c;
        }

        @Override
        public byte[] toByteArray() {
            int c = outputCount();
            return new String(buf, 0, c).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return new String(buf, 0, outputCount());
        }

        @Override
        public void close() {
            if (pooled) {
                BufferPool.returnCharBuffer(buf);
                buf = null;
                pooled = false;
            }
        }

        // Fast int-to-chars using lookup table
        private static int writeIntToChars(int val, char[] buf, int pos) {
            if (val == Integer.MIN_VALUE) {
                String s = "-2147483648";
                s.getChars(0, 11, buf, pos);
                return 11;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            if (val < 10) {
                buf[pos++] = (char) ('0' + val);
            } else if (val < 100) {
                buf[pos++] = (char) ('0' + val / 10);
                buf[pos++] = (char) ('0' + val % 10);
            } else {
                // General case: write digits in reverse then flip
                int end = pos;
                while (val > 0) {
                    buf[end++] = (char) ('0' + val % 10);
                    val /= 10;
                }
                // Reverse
                int left = pos, right = end - 1;
                while (left < right) {
                    char tmp = buf[left];
                    buf[left] = buf[right];
                    buf[right] = tmp;
                    left++;
                    right--;
                }
                pos = end;
            }
            return pos - start;
        }

        private static int writeLongToChars(long val, char[] buf, int pos) {
            if (val == Long.MIN_VALUE) {
                String s = "-9223372036854775808";
                s.getChars(0, 20, buf, pos);
                return 20;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            if (val < 10) {
                buf[pos++] = (char) ('0' + val);
            } else {
                int end = pos;
                while (val > 0) {
                    buf[end++] = (char) ('0' + (int) (val % 10));
                    val /= 10;
                }
                int left = pos, right = end - 1;
                while (left < right) {
                    char tmp = buf[left];
                    buf[left] = buf[right];
                    buf[right] = tmp;
                    left++;
                    right--;
                }
                pos = end;
            }
            return pos - start;
        }
    }

    public static final class UTF8 extends JSONGenerator {
        // Safety margin: after ensureCapacity, we can write this many bytes without checking
        static final int SAFE_MARGIN = 512;

        private byte[] buf;
        private boolean pooled;

        UTF8(long features) {
            super(features);
            this.buf = BufferPool.borrowByteBuffer();
            this.pooled = true;
        }

        private void ensureCapacity(int needed) {
            int minCap = count + needed + SAFE_MARGIN;
            if (minCap > buf.length) {
                int newCap = Math.max(buf.length + (buf.length >> 1), minCap);
                byte[] newBuf = new byte[newCap];
                System.arraycopy(buf, 0, newBuf, 0, count);
                buf = newBuf;
            }
        }

        // Small token write — no capacity check (covered by SAFE_MARGIN)
        @Override
        public void startObject() {
            buf[count++] = '{';
        }

        @Override
        public void endObject() {
            if (count > 0 && buf[count - 1] == ',') {
                count--;
            }
            buf[count++] = '}';
            buf[count++] = ',';
        }

        @Override
        public void startArray() {
            buf[count++] = '[';
        }

        @Override
        public void endArray() {
            if (count > 0 && buf[count - 1] == ',') {
                count--;
            }
            buf[count++] = ']';
            buf[count++] = ',';
        }

        @Override
        public void writeName(String name) {
            int len = name.length();
            ensureCapacity(len * 3 + 3);
            buf[count++] = '"';
            for (int i = 0; i < len; i++) {
                char ch = name.charAt(i);
                if (ch < 0x80) {
                    buf[count++] = (byte) ch;
                } else if (ch < 0x800) {
                    buf[count++] = (byte) (0xC0 | (ch >> 6));
                    buf[count++] = (byte) (0x80 | (ch & 0x3F));
                } else {
                    buf[count++] = (byte) (0xE0 | (ch >> 12));
                    buf[count++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                    buf[count++] = (byte) (0x80 | (ch & 0x3F));
                }
            }
            buf[count++] = '"';
            buf[count++] = ':';
        }

        @Override
        public void writePreEncodedName(char[] nameChars, byte[] nameBytes) {
            int len = nameBytes.length;
            ensureCapacity(len);
            System.arraycopy(nameBytes, 0, buf, count, len);
            count += len;
        }

        @Override
        public void writePreEncodedNameLongs(long[] nameByteLongs, int nameBytesLen, char[] nameChars, byte[] nameBytes) {
            if (nameByteLongs != null) {
                ensureCapacity(nameBytesLen);
                int pos = count;
                for (long v : nameByteLongs) {
                    JDKUtils.putLongDirect(buf, pos, v);
                    pos += 8;
                }
                count += nameBytesLen;
            } else {
                writePreEncodedName(nameChars, nameBytes);
            }
        }

        /**
         * Write long[] name bytes without ensureCapacity (caller must guarantee space).
         * Unrolled for common field name lengths (1-4 longs = up to 32 bytes).
         */
        private void writeNameLongsNoCheck(long[] nameByteLongs, int nameBytesLen) {
            int pos = count;
            switch (nameByteLongs.length) {
                case 1:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    break;
                case 2:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]);
                    break;
                case 3:
                    JDKUtils.putLongDirect(buf, pos, nameByteLongs[0]);
                    JDKUtils.putLongDirect(buf, pos + 8, nameByteLongs[1]);
                    JDKUtils.putLongDirect(buf, pos + 16, nameByteLongs[2]);
                    break;
                default:
                    for (int i = 0; i < nameByteLongs.length; i++) {
                        JDKUtils.putLongDirect(buf, pos + (i << 3), nameByteLongs[i]);
                    }
                    break;
            }
            count += nameBytesLen;
        }

        // Small token writes — no capacity check (within SAFE_MARGIN)
        // Use Unsafe putInt/putLong for fewer store instructions

        // Pre-packed constants (little-endian x86): "null" = 0x6C6C756E, "true" = 0x65757274, etc.
        private static final int NULL_INT = 'n' | ('u' << 8) | ('l' << 16) | ('l' << 24);
        private static final int TRUE_INT = 't' | ('r' << 8) | ('u' << 16) | ('e' << 24);
        // "false" = 5 bytes: int "fals" + byte 'e'
        private static final int FALS_INT = 'f' | ('a' << 8) | ('l' << 16) | ('s' << 24);

        @Override
        public void writeNull() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, NULL_INT);
                pos += 4;
            } else {
                buf[pos++] = 'n';
                buf[pos++] = 'u';
                buf[pos++] = 'l';
                buf[pos++] = 'l';
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeTrue() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, TRUE_INT);
                pos += 4;
            } else {
                buf[pos++] = 't';
                buf[pos++] = 'r';
                buf[pos++] = 'u';
                buf[pos++] = 'e';
            }
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeFalse() {
            int pos = count;
            if (JDKUtils.UNSAFE_AVAILABLE) {
                JDKUtils.putIntDirect(buf, pos, FALS_INT);
                pos += 4;
            } else {
                buf[pos++] = 'f';
                buf[pos++] = 'a';
                buf[pos++] = 'l';
                buf[pos++] = 's';
            }
            buf[pos++] = 'e';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeBool(boolean val) {
            if (val) {
                writeTrue();
            } else {
                writeFalse();
            }
        }

        @Override
        public void writeInt32(int val) {
            // max 11 digits + comma = 12, within SAFE_MARGIN
            count += writeIntToBytes(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeInt64(long val) {
            // max 20 digits + comma = 21, within SAFE_MARGIN
            count += writeLongToBytes(val, buf, count);
            buf[count++] = ',';
        }

        @Override
        public void writeFloat(float val) {
            String s = Float.toString(val);
            writeNumericString(s);
        }

        @Override
        public void writeDouble(double val) {
            count = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, count, val, true, false);
            buf[count++] = ',';
        }

        private void writeNumericString(String s) {
            // Numeric strings are always Latin-1 and short (< 25 chars), within SAFE_MARGIN
            int coder = JDKUtils.getStringCoder(s);
            if (coder == 0) {
                byte[] value = (byte[]) JDKUtils.getStringValue(s);
                if (value != null) {
                    int len = value.length;
                    System.arraycopy(value, 0, buf, count, len);
                    count += len;
                    buf[count++] = ',';
                    return;
                }
            }
            int len = s.length();
            for (int i = 0; i < len; i++) {
                buf[count++] = (byte) s.charAt(i);
            }
            buf[count++] = ',';
        }

        @Override
        public void writeDecimal(BigDecimal val) {
            if (val == null) {
                writeNull();
                return;
            }
            String s = isEnabled(WriteFeature.WriteBigDecimalAsPlain) ? val.toPlainString() : val.toString();
            writeNumericString(s);
        }

        // ---- Escape check using bit manipulation (wast-style) ----

        /**
         * Check if 8 bytes (as a long) contain no chars that need JSON escaping.
         * Checks: no byte < 0x20 (control), no '"' (0x22), no '\\' (0x5C).
         * Fast path: if all bytes > '"' and none are '\\', return true immediately (3 ops).
         * Full path: complete check for control chars and '"' (wast-style).
         */
        static boolean noEscape8(long v) {
            long hiMask = 0x8080808080808080L;
            long lo = 0x0101010101010101L;
            // Check '\\' (0x5C): XOR with 0xA3 (complement) + add 0x01, high bit set if NOT '\\'
            long notBackslash = (v ^ 0xA3A3A3A3A3A3A3A3L) + lo & hiMask;
            // Fast path: all bytes > '"' (0x22) and not '\\' — covers >99% of ASCII text
            if ((notBackslash & v + 0x5D5D5D5D5D5D5D5DL) == hiMask) {
                return true;
            }
            // Full check: control chars < 0x20, '"', '\\'
            long ctrl = (v - 0x2020202020202020L) & ~v & hiMask;
            long xq = v ^ 0x2222222222222222L;
            long quote = (xq - lo) & ~xq & hiMask;
            return (ctrl | quote | (notBackslash ^ hiMask)) == 0;
        }

        /**
         * Write a Latin-1 string value with progressive-width escape checking.
         */
        private void writeLatinString(byte[] value, int len) {
            ensureCapacity(len * 6 + 3);
            writeLatinStringNoCapCheck(value, len);
        }

        /**
         * Check 4 bytes (as an int) for JSON escape characters.
         */
        static boolean noEscape4(int v) {
            int hiMask = 0x80808080;
            int lo = 0x01010101;
            int ctrl = (v - 0x20202020) & ~v & hiMask;
            int xq = v ^ 0x22222222;
            int quote = (xq - lo) & ~xq & hiMask;
            int xb = v ^ 0x5C5C5C5C;
            int bslash = (xb - lo) & ~xb & hiMask;
            return (ctrl | quote | bslash) == 0;
        }

        /**
         * Write a Latin-1 string value WITHOUT ensureCapacity (caller must guarantee space).
         * Check-and-copy in 8-byte chunks using putLong (single pass, no separate scan).
         */
        private void writeLatinStringNoCapCheck(byte[] value, int len) {
            int pos = count;
            buf[pos++] = '"';

            if (JDKUtils.UNSAFE_AVAILABLE && len > 0) {
                int i = 0;
                // Check-and-copy 8 bytes at a time
                int limit8 = len - 7;
                for (; i < limit8; i += 8) {
                    long v = JDKUtils.getLongDirect(value, i);
                    if (noEscape8(v)) {
                        JDKUtils.putLongDirect(buf, pos, v);
                        pos += 8;
                    } else {
                        // Escape found — switch to byte-by-byte for remainder
                        pos = writeEscapedBytes(value, i, len, pos);
                        buf[pos++] = '"';
                        buf[pos++] = ',';
                        count = pos;
                        return;
                    }
                }
                // Remaining 1-7 bytes: use noEscape4 for 4-byte chunk, then individual
                if (i + 4 <= len) {
                    int v4 = JDKUtils.getIntDirect(value, i);
                    if (noEscape4(v4)) {
                        JDKUtils.putIntDirect(buf, pos, v4);
                        pos += 4;
                        i += 4;
                    }
                }
                for (; i < len; i++) {
                    byte b = value[i];
                    if (b >= 0x20 && b != '"' && b != '\\') {
                        buf[pos++] = b;
                    } else {
                        pos = writeEscapedByte(b, pos);
                        for (i++; i < len; i++) {
                            b = value[i];
                            if (b >= 0x20 && b != '"' && b != '\\') {
                                buf[pos++] = b;
                            } else {
                                pos = writeEscapedByte(b, pos);
                            }
                        }
                        break;
                    }
                }
            } else {
                pos = writeEscapedBytes(value, 0, len, pos);
            }

            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        private int writeEscapedBytes(byte[] src, int from, int end, int pos) {
            for (int i = from; i < end; i++) {
                byte b = src[i];
                if (b >= 0x20 && b != '"' && b != '\\') {
                    buf[pos++] = b;
                } else {
                    pos = writeEscapedByte(b, pos);
                }
            }
            return pos;
        }

        private int writeEscapedByte(byte b, int pos) {
            int ch = b & 0xFF;
            char escaped = ESCAPE_CHARS[ch];
            if (escaped != 0 && ch != '/') {
                buf[pos++] = '\\';
                buf[pos++] = (byte) escaped;
            } else if (ch < 0x20) {
                buf[pos++] = '\\';
                buf[pos++] = 'u';
                buf[pos++] = '0';
                buf[pos++] = '0';
                buf[pos++] = (byte) DIGITS[ch >> 4];
                buf[pos++] = (byte) DIGITS[ch & 0xF];
            } else {
                buf[pos++] = b;
            }
            return pos;
        }

        @Override
        public void writeString(String val) {
            if (val == null) {
                writeNull();
                return;
            }

            if (JDKUtils.getStringCoder(val) == 0) {
                byte[] value = (byte[]) JDKUtils.getStringValue(val);
                writeLatinString(value, value.length);
                return;
            }

            // General path: char-by-char with escaping and UTF-8 encoding
            int len = val.length();
            ensureCapacity(len * 6 + 3);
            int pos = count;
            buf[pos++] = '"';
            for (int i = 0; i < len; i++) {
                char ch = val.charAt(i);
                if (ch < 128) {
                    char escaped = ESCAPE_CHARS[ch];
                    if (escaped != 0 && ch != '/') {
                        buf[pos++] = '\\';
                        buf[pos++] = (byte) escaped;
                    } else if (ch < 0x20) {
                        buf[pos++] = '\\';
                        buf[pos++] = 'u';
                        buf[pos++] = '0';
                        buf[pos++] = '0';
                        buf[pos++] = (byte) DIGITS[ch >> 4];
                        buf[pos++] = (byte) DIGITS[ch & 0xF];
                    } else {
                        buf[pos++] = (byte) ch;
                    }
                } else if (ch < 0x800) {
                    buf[pos++] = (byte) (0xC0 | (ch >> 6));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                } else {
                    buf[pos++] = (byte) (0xE0 | (ch >> 12));
                    buf[pos++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                    buf[pos++] = (byte) (0x80 | (ch & 0x3F));
                }
            }
            buf[pos++] = '"';
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameString(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, String value) {
            // Fast path: Latin-1 string (covers >99% of cases on JDK 9+)
            if (JDKUtils.getStringCoder(value) == 0) {
                byte[] valBytes = (byte[]) JDKUtils.getStringValue(value);
                int valLen = valBytes.length;
                ensureCapacity(nameBytesLen + valLen * 6 + 3);
                if (nameByteLongs != null) {
                    writeNameLongsNoCheck(nameByteLongs, nameBytesLen);
                } else {
                    System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                    count += nameBytesLen;
                }
                writeLatinStringNoCapCheck(valBytes, valLen);
                return;
            }

            // Fallback for UTF-16 strings
            if (nameByteLongs != null) {
                ensureCapacity(nameBytesLen);
                writeNameLongsNoCheck(nameByteLongs, nameBytesLen);
            } else {
                ensureCapacity(nameBytesLen);
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            writeString(value);
        }

        @Override
        public void writeNameInt32(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, int value) {
            // name(N) + int(max 11) + comma(1) — all within single ensureCapacity
            ensureCapacity(nameBytesLen + 12);
            if (nameByteLongs != null) {
                writeNameLongsNoCheck(nameByteLongs, nameBytesLen);
            } else {
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            int pos = count;
            pos += writeIntToBytes(value, buf, pos);
            buf[pos++] = ',';
            count = pos;
        }

        @Override
        public void writeNameDouble(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, double value) {
            // Direct double-to-bytes: avoid Double.toString() String allocation
            ensureCapacity(nameBytesLen + 25);
            if (nameByteLongs != null) {
                writeNameLongsNoCheck(nameByteLongs, nameBytesLen);
            } else {
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            count = com.alibaba.fastjson3.util.NumberUtils.writeDouble(buf, count, value, true, false);
            buf[count++] = ',';
        }

        @Override
        public void writeNameBool(long[] nameByteLongs, int nameBytesLen, byte[] nameBytes, char[] nameChars, boolean value) {
            // name(N) + true/false(5) + comma(1) — single ensureCapacity
            ensureCapacity(nameBytesLen + 6);
            if (nameByteLongs != null) {
                writeNameLongsNoCheck(nameByteLongs, nameBytesLen);
            } else {
                System.arraycopy(nameBytes, 0, buf, count, nameBytesLen);
                count += nameBytesLen;
            }
            if (value) {
                writeTrue();
            } else {
                writeFalse();
            }
        }

        @Override
        public void writeRaw(String raw) {
            byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
            ensureCapacity(bytes.length);
            System.arraycopy(bytes, 0, buf, count, bytes.length);
            count += bytes.length;
        }

        private int outputCount() {
            int c = count;
            if (c > 0 && buf[c - 1] == ',') {
                c--;
            }
            return c;
        }

        @Override
        public byte[] toByteArray() {
            int c = outputCount();
            byte[] result = new byte[c];
            System.arraycopy(buf, 0, result, 0, c);
            return result;
        }

        @Override
        public String toString() {
            return new String(buf, 0, outputCount(), StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            if (pooled) {
                BufferPool.returnByteBuffer(buf);
                buf = null;
                pooled = false;
            }
        }

        private static int writeIntToBytes(int val, byte[] buf, int pos) {
            if (val == Integer.MIN_VALUE) {
                // "-2147483648" = 11 bytes
                buf[pos] = '-';
                buf[pos + 1] = '2';
                buf[pos + 2] = '1';
                buf[pos + 3] = '4';
                buf[pos + 4] = '7';
                buf[pos + 5] = '4';
                buf[pos + 6] = '8';
                buf[pos + 7] = '3';
                buf[pos + 8] = '6';
                buf[pos + 9] = '4';
                buf[pos + 10] = '8';
                return 11;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            // Count digits
            int digits = stringSize(val);
            int end = pos + digits;
            // Write 2 digits at a time from the end using PACKED_DIGITS
            int p = end;
            while (val >= 100) {
                int q = val / 100;
                int r = val - q * 100;
                val = q;
                p -= 2;
                JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[r]);
            }
            if (val >= 10) {
                p -= 2;
                JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[val]);
            } else {
                buf[--p] = (byte) ('0' + val);
            }
            return end - start;
        }

        private static int stringSize(int x) {
            int d = 1;
            if (x >= 100000) {
                d += 5;
                x /= 100000;
            }
            if (x >= 100) {
                d += 2;
                x /= 100;
            }
            if (x >= 10) {
                d++;
            }
            return d;
        }

        private static int writeLongToBytes(long val, byte[] buf, int pos) {
            if (val == Long.MIN_VALUE) {
                // "-9223372036854775808" = 20 bytes
                byte[] s = {'-', '9', '2', '2', '3', '3', '7', '2', '0', '3', '6', '8', '5', '4', '7', '7', '5', '8', '0', '8'};
                System.arraycopy(s, 0, buf, pos, 20);
                return 20;
            }
            int start = pos;
            if (val < 0) {
                buf[pos++] = '-';
                val = -val;
            }
            // For values that fit in int, use the faster int path
            if (val <= Integer.MAX_VALUE) {
                return (pos - start) + writeIntToBytes((int) val, buf, pos);
            }
            // Split into two int parts for faster digit extraction (int div is cheaper than long div)
            int hi = (int) (val / 1000000000L);
            int lo = (int) (val - (long) hi * 1000000000L);
            int hiDigits = intStringSize(hi);
            int end = pos + hiDigits + 9;
            int p = end;
            // Write low 9 digits (zero-padded) using PACKED_DIGITS (1 putShort = 2 digits)
            for (int j = 0; j < 4; j++) {
                int q = lo / 100;
                int r = lo - q * 100;
                lo = q;
                p -= 2;
                JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[r]);
            }
            buf[--p] = (byte) ('0' + lo);
            // Write high part using int arithmetic + PACKED_DIGITS
            while (hi >= 100) {
                int q = hi / 100;
                int r = hi - q * 100;
                hi = q;
                p -= 2;
                JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[r]);
            }
            if (hi >= 10) {
                p -= 2;
                JDKUtils.putShortDirect(buf, p, PACKED_DIGITS[hi]);
            } else {
                buf[--p] = (byte) ('0' + hi);
            }
            return end - start;
        }

        private static int intStringSize(int x) {
            int d = 1;
            if (x >= 100000) {
                d += 5;
                x /= 100000;
            }
            if (x >= 100) {
                d += 2;
                x /= 100;
            }
            if (x >= 10) {
                d++;
            }
            return d;
        }

        private static int longStringSize(long x) {
            int d = 1;
            if (x >= 10000000000L) {
                d += 10;
                x /= 10000000000L;
            }
            if (x >= 100000) {
                d += 5;
                x /= 100000;
            }
            if (x >= 100) {
                d += 2;
                x /= 100;
            }
            if (x >= 10) {
                d++;
            }
            return d;
        }
    }
}

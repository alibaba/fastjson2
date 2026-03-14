package com.alibaba.fastjson3;

import com.alibaba.fastjson3.reader.FieldNameMatcher;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * High-performance JSON parser.
 * Uses pre-computed lookup tables and template method pattern for maximum throughput
 * across String, byte[], and char[] input sources.
 *
 * <p>This is a sealed class with exactly three implementations:
 * {@code Str} (String input), {@code UTF8} (byte[] input),
 * and {@code CharArray} (char[] input).</p>
 *
 * <pre>
 * try (JSONParser parser = JSONParser.of(jsonString)) {
 *     JSONObject obj = parser.readObject();
 * }
 * </pre>
 */
public abstract sealed class JSONParser implements Closeable
        permits JSONParser.Str, JSONParser.UTF8, JSONParser.CharArray {
    static final boolean[] WHITESPACE = new boolean[256];
    static {
        WHITESPACE[' '] = true;
        WHITESPACE['\t'] = true;
        WHITESPACE['\n'] = true;
        WHITESPACE['\r'] = true;
    }

    static final int HEX_INVALID = -1;
    static final int[] HEX_VALUES = new int[128];
    static {
        java.util.Arrays.fill(HEX_VALUES, HEX_INVALID);
        for (int i = '0'; i <= '9'; i++) {
            HEX_VALUES[i] = i - '0';
        }
        for (int i = 'a'; i <= 'f'; i++) {
            HEX_VALUES[i] = 10 + i - 'a';
        }
        for (int i = 'A'; i <= 'F'; i++) {
            HEX_VALUES[i] = 10 + i - 'A';
        }
    }

    static final int MAX_NESTING_DEPTH = 512;

    protected final long features;
    protected int offset;
    protected int depth;

    protected JSONParser(long features) {
        this.features = features;
    }

    // ==================== Factory methods ====================

    public static JSONParser of(String json) {
        if (json == null || json.isEmpty()) {
            throw new JSONException("input is null or empty");
        }
        return new Str(json, 0);
    }

    public static JSONParser of(String json, ReadFeature... features) {
        if (json == null || json.isEmpty()) {
            throw new JSONException("input is null or empty");
        }
        return new Str(json, ReadFeature.of(features));
    }

    public static JSONParser of(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            throw new JSONException("input is null or empty");
        }
        return new UTF8(jsonBytes, 0, jsonBytes.length, 0);
    }

    public static JSONParser of(byte[] jsonBytes, ReadFeature... features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            throw new JSONException("input is null or empty");
        }
        return new UTF8(jsonBytes, 0, jsonBytes.length, ReadFeature.of(features));
    }

    public static JSONParser of(char[] chars, int offset, int length) {
        return new CharArray(chars, offset, length, 0);
    }

    public boolean isEnabled(ReadFeature feature) {
        return (features & feature.mask) != 0;
    }

    // ==================== Abstract char-access primitives ====================

    /** Return char/byte value at position i. For UTF-8, returns byte & 0xFF (ASCII-safe). */
    abstract int ch(int i);

    /** Return logical end position. */
    abstract int end();

    /** Extract string between start (inclusive) and end (exclusive). For numbers/keywords (ASCII). */
    abstract String extractString(int start, int end);

    /** Read string content after opening quote, handling escapes and encoding. Advance past closing quote. */
    abstract String readStringContent();

    // ==================== Core parsing (shared logic) ====================

    protected void skipWhitespace() {
        int e = end();
        while (offset < e) {
            int c = ch(offset);
            if (c > ' ' || !WHITESPACE[c]) {
                break;
            }
            offset++;
        }
    }

    /**
     * Get internal byte[] for direct access by ObjectReaders (UTF-8 path).
     * Returns null for non-UTF8 parsers.
     */
    public byte[] getBytes() {
        return null;
    }

    public Object readAny() {
        skipWhitespace();
        if (offset >= end()) {
            throw new JSONException("unexpected end of input");
        }
        int c = ch(offset);
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't', 'f' -> readBoolean() ? Boolean.TRUE : Boolean.FALSE;
            case 'n' -> {
                readNullLiteral();
                yield null;
            }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield readNumber();
                }
                throw new JSONException("unexpected character '" + (char) c + "' at offset " + offset);
            }
        };
    }

    public JSONObject readObject() {
        skipWhitespace();
        if (offset >= end() || ch(offset) != '{') {
            throw new JSONException("expected '{' at offset " + offset);
        }
        if (++depth > MAX_NESTING_DEPTH) {
            throw new JSONException("nesting depth " + depth + " exceeds maximum " + MAX_NESTING_DEPTH);
        }
        offset++;

        JSONObject obj = new JSONObject();
        skipWhitespace();

        if (offset < end() && ch(offset) == '}') {
            offset++;
            depth--;
            return obj;
        }

        for (;;) {
            String name = readFieldName();
            Object value = readAny();
            obj.put(name, value);

            skipWhitespace();
            if (offset >= end()) {
                throw new JSONException("unterminated object");
            }
            int c = ch(offset);
            if (c == ',') {
                offset++;
                continue;
            }
            if (c == '}') {
                offset++;
                depth--;
                return obj;
            }
            throw new JSONException("expected ',' or '}' at offset " + offset);
        }
    }

    public JSONArray readArray() {
        skipWhitespace();
        if (offset >= end() || ch(offset) != '[') {
            throw new JSONException("expected '[' at offset " + offset);
        }
        if (++depth > MAX_NESTING_DEPTH) {
            throw new JSONException("nesting depth " + depth + " exceeds maximum " + MAX_NESTING_DEPTH);
        }
        offset++;

        JSONArray arr = new JSONArray();
        skipWhitespace();

        if (offset < end() && ch(offset) == ']') {
            offset++;
            depth--;
            return arr;
        }

        for (;;) {
            Object value = readAny();
            arr.add(value);

            skipWhitespace();
            if (offset >= end()) {
                throw new JSONException("unterminated array");
            }
            int c = ch(offset);
            if (c == ',') {
                offset++;
                continue;
            }
            if (c == ']') {
                offset++;
                depth--;
                return arr;
            }
            throw new JSONException("expected ',' or ']' at offset " + offset);
        }
    }

    public String readString() {
        skipWhitespace();
        if (offset >= end() || ch(offset) != '"') {
            throw new JSONException("expected '\"' at offset " + offset);
        }
        offset++;
        return readStringContent();
    }

    /**
     * Read a JSON number as int. Parses digits directly without String allocation (wast-style).
     */
    public int readInt() {
        skipWhitespace();
        int e = end();
        if (offset >= e) {
            throw new JSONException("unexpected end of input");
        }

        boolean neg = false;
        int c = ch(offset);
        if (c == '-') {
            neg = true;
            offset++;
            if (offset >= e) {
                throw new JSONException("unexpected end in number");
            }
            c = ch(offset);
        }

        if (c < '0' || c > '9') {
            throw new JSONException("expected digit at offset " + offset);
        }

        int value = c - '0';
        offset++;
        while (offset < e) {
            c = ch(offset);
            if (c < '0' || c > '9') {
                break;
            }
            // Overflow check: value * 10 + digit
            if (value > 214748364 || (value == 214748364 && c > '7' + (neg ? 1 : 0))) {
                // Fallback to readNumber for overflow
                offset -= countDigitsBackward(value, neg);
                return readNumber().intValue();
            }
            value = value * 10 + (c - '0');
            offset++;
        }
        // Handle decimal point or exponent (not a pure integer)
        if (offset < e && (ch(offset) == '.' || ch(offset) == 'e' || ch(offset) == 'E')) {
            offset -= countDigitsBackward(value, neg);
            return readNumber().intValue();
        }

        return neg ? -value : value;
    }

    /**
     * Read a JSON number as long. Parses digits directly without String allocation (wast-style).
     */
    public long readLong() {
        skipWhitespace();
        int e = end();
        if (offset >= e) {
            throw new JSONException("unexpected end of input");
        }

        boolean neg = false;
        int c = ch(offset);
        if (c == '-') {
            neg = true;
            offset++;
            if (offset >= e) {
                throw new JSONException("unexpected end in number");
            }
            c = ch(offset);
        }

        if (c < '0' || c > '9') {
            throw new JSONException("expected digit at offset " + offset);
        }

        long value = c - '0';
        offset++;
        while (offset < e) {
            c = ch(offset);
            if (c < '0' || c > '9') {
                break;
            }
            // Overflow check
            if (value > 922337203685477580L
                    || (value == 922337203685477580L && c > '7' + (neg ? 1 : 0))) {
                offset -= countLongDigitsBackward(value, neg);
                return readNumber().longValue();
            }
            value = value * 10 + (c - '0');
            offset++;
        }
        // Handle decimal point or exponent
        if (offset < e && (ch(offset) == '.' || ch(offset) == 'e' || ch(offset) == 'E')) {
            offset -= countLongDigitsBackward(value, neg);
            return readNumber().longValue();
        }

        return neg ? -value : value;
    }

    public double readDouble() {
        return readNumber().doubleValue();
    }

    private static int countDigitsBackward(int value, boolean neg) {
        int count = neg ? 1 : 0;
        if (value == 0) {
            return count + 1;
        }
        while (value > 0) {
            value /= 10;
            count++;
        }
        return count;
    }

    private static int countLongDigitsBackward(long value, boolean neg) {
        int count = neg ? 1 : 0;
        if (value == 0) {
            return count + 1;
        }
        while (value > 0) {
            value /= 10;
            count++;
        }
        return count;
    }

    public boolean readBoolean() {
        skipWhitespace();
        int e = end();
        if (offset + 4 <= e
                && ch(offset) == 't' && ch(offset + 1) == 'r'
                && ch(offset + 2) == 'u' && ch(offset + 3) == 'e') {
            offset += 4;
            return true;
        }
        if (offset + 5 <= e
                && ch(offset) == 'f' && ch(offset + 1) == 'a'
                && ch(offset + 2) == 'l' && ch(offset + 3) == 's'
                && ch(offset + 4) == 'e') {
            offset += 5;
            return false;
        }
        throw new JSONException("expected 'true' or 'false' at offset " + offset);
    }

    public boolean readNull() {
        skipWhitespace();
        if (offset + 4 <= end()
                && ch(offset) == 'n' && ch(offset + 1) == 'u'
                && ch(offset + 2) == 'l' && ch(offset + 3) == 'l') {
            offset += 4;
            return true;
        }
        return false;
    }

    public String readFieldName() {
        skipWhitespace();
        if (offset >= end() || ch(offset) != '"') {
            throw new JSONException("expected '\"' for field name at offset " + offset);
        }
        offset++;
        String name = readStringContent();
        skipWhitespace();
        if (offset >= end() || ch(offset) != ':') {
            throw new JSONException("expected ':' at offset " + offset);
        }
        offset++;
        return name;
    }

    /**
     * Read a field name and compute its hash using the given matcher's hash strategy.
     * On the fast path (no escape sequences), avoids String allocation entirely.
     * Returns the hash value; use {@code matcher.match(hash)} to find the FieldReader.
     *
     * <p>Also reads past the trailing ':'. If the field name contains escape sequences,
     * falls back to String-based matching via {@link FieldNameMatcher#hash(String)}.</p>
     *
     * @param matcher the field name matcher with pre-computed hashes
     * @return the computed hash of the field name
     */
    public long readFieldNameHash(FieldNameMatcher matcher) {
        skipWhitespace();
        if (offset >= end() || ch(offset) != '"') {
            throw new JSONException("expected '\"' for field name at offset " + offset);
        }
        offset++;

        // Fast path: compute hash incrementally without String allocation
        long hash = 0;
        int e = end();
        int start = offset;
        while (offset < e) {
            int c = ch(offset);
            if (c == '"') {
                offset++;
                skipWhitespace();
                if (offset >= e || ch(offset) != ':') {
                    throw new JSONException("expected ':' at offset " + offset);
                }
                offset++;
                return hash;
            }
            if (c == '\\') {
                // Slow path: has escapes, fall back to reading full string
                offset = start; // rewind
                String name = readStringContent();
                skipWhitespace();
                if (offset >= e || ch(offset) != ':') {
                    throw new JSONException("expected ':' at offset " + offset);
                }
                offset++;
                return matcher.hash(name);
            }
            hash = matcher.hashStep(hash, c);
            offset++;
        }
        throw new JSONException("unterminated field name");
    }

    // ==================== Direct parsing accessors ====================

    /**
     * Get current offset position. Used by ObjectReaders for direct parsing.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get char/byte at position. Used by ObjectReaders for direct parsing.
     */
    public int charAt(int i) {
        return ch(i);
    }

    /**
     * Get end position. Used by ObjectReaders for direct parsing.
     */
    public int getEnd() {
        return end();
    }

    /**
     * Advance offset by n. Used by ObjectReaders for direct parsing.
     */
    public void advance(int n) {
        offset += n;
    }

    /**
     * Set offset directly. Used by ObjectReaders for adaptive parsing.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Skip whitespace (public access for direct parsing).
     */
    public void skipWS() {
        skipWhitespace();
    }

    public boolean isEnd() {
        skipWhitespace();
        return offset >= end();
    }

    public void skipValue() {
        skipWhitespace();
        if (offset >= end()) {
            throw new JSONException("unexpected end of input");
        }
        int c = ch(offset);
        switch (c) {
            case '{' -> skipObject();
            case '[' -> skipArray();
            case '"' -> {
                offset++;
                skipStringContent();
            }
            case 't' -> {
                if (offset + 4 > end()) {
                    throw new JSONException("unexpected end in 'true'");
                }
                offset += 4;
            }
            case 'f' -> {
                if (offset + 5 > end()) {
                    throw new JSONException("unexpected end in 'false'");
                }
                offset += 5;
            }
            case 'n' -> {
                if (offset + 4 > end()) {
                    throw new JSONException("unexpected end in 'null'");
                }
                offset += 4;
            }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    skipNumber();
                } else {
                    throw new JSONException("unexpected character '" + (char) c + "' at offset " + offset);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Class<T> type) {
        if (type == Object.class) {
            return (T) readAny();
        }
        if (type == String.class) {
            return (T) readString();
        }
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(readInt());
        }
        if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(readLong());
        }
        if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(readDouble());
        }
        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf((float) readDouble());
        }
        if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(readBoolean());
        }
        if (type == BigDecimal.class) {
            return (T) readNumber();
        }
        if (type == BigInteger.class) {
            Number num = readNumber();
            if (num instanceof BigInteger) {
                return (T) num;
            }
            return (T) BigInteger.valueOf(num.longValue());
        }
        if (type == JSONObject.class) {
            return (T) readObject();
        }
        if (type == JSONArray.class) {
            return (T) readArray();
        }
        if (type == AtomicInteger.class) {
            if (readNull()) {
                return null;
            }
            return (T) new AtomicInteger(readInt());
        }
        if (type == AtomicLong.class) {
            if (readNull()) {
                return null;
            }
            return (T) new AtomicLong(readLong());
        }
        if (type == AtomicBoolean.class) {
            if (readNull()) {
                return null;
            }
            return (T) new AtomicBoolean(readBoolean());
        }
        if (type == AtomicIntegerArray.class) {
            if (readNull()) {
                return null;
            }
            JSONArray array = readArray();
            int[] values = new int[array.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = array.getIntValue(i);
            }
            return (T) new AtomicIntegerArray(values);
        }
        if (type == AtomicLongArray.class) {
            if (readNull()) {
                return null;
            }
            JSONArray array = readArray();
            long[] values = new long[array.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = array.getLongValue(i);
            }
            return (T) new AtomicLongArray(values);
        }
        // For POJO types, read as JSONObject first and let ObjectMapper handle conversion
        throw new UnsupportedOperationException("reading " + type.getName() + " not yet supported without ObjectReader");
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Type type) {
        if (type instanceof Class) {
            return read((Class<T>) type);
        }
        throw new UnsupportedOperationException("reading generic type not yet supported without ObjectReader");
    }

    @Override
    public void close() {
        // default no-op
    }

    // ==================== Number parsing ====================

    protected Number readNumber() {
        skipWhitespace();
        int start = offset;
        boolean isFloat = scanNumber();
        String numStr = extractString(start, offset);

        if (isFloat) {
            if (isEnabled(ReadFeature.UseBigDecimalForDoubles)) {
                return new BigDecimal(numStr);
            }
            return Double.parseDouble(numStr);
        }

        // Determine integer size
        int digitLen = numStr.charAt(0) == '-' ? numStr.length() - 1 : numStr.length();
        if (digitLen <= 9) {
            return Integer.parseInt(numStr);
        }
        if (digitLen <= 18) {
            long v = Long.parseLong(numStr);
            if ((int) v == v) {
                return (int) v;
            }
            return v;
        }
        // 19+ digits
        BigInteger bi = new BigInteger(numStr);
        if (bi.bitLength() <= 63) {
            long v = bi.longValue();
            if ((int) v == v) {
                return (int) v;
            }
            return v;
        }
        return bi;
    }

    /** Scan a JSON number literal. Advance offset. Return true if floating-point. */
    private boolean scanNumber() {
        int e = end();
        boolean isFloat = false;

        if (offset < e && ch(offset) == '-') {
            offset++;
        }
        if (offset >= e) {
            throw new JSONException("unexpected end in number");
        }

        // Integer part
        if (ch(offset) == '0') {
            offset++;
        } else if (ch(offset) >= '1' && ch(offset) <= '9') {
            offset++;
            while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
                offset++;
            }
        } else {
            throw new JSONException("invalid number at offset " + offset);
        }

        // Fraction
        if (offset < e && ch(offset) == '.') {
            isFloat = true;
            offset++;
            if (offset >= e || ch(offset) < '0' || ch(offset) > '9') {
                throw new JSONException("invalid number: expected digit after '.'");
            }
            while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
                offset++;
            }
        }

        // Exponent
        if (offset < e && (ch(offset) == 'e' || ch(offset) == 'E')) {
            isFloat = true;
            offset++;
            if (offset < e && (ch(offset) == '+' || ch(offset) == '-')) {
                offset++;
            }
            if (offset >= e || ch(offset) < '0' || ch(offset) > '9') {
                throw new JSONException("invalid number: expected digit in exponent");
            }
            while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
                offset++;
            }
        }

        return isFloat;
    }

    // ==================== Skip helpers ====================

    private void skipObject() {
        offset++; // skip '{'
        skipWhitespace();
        if (offset < end() && ch(offset) == '}') {
            offset++;
            return;
        }
        for (;;) {
            skipWhitespace();
            if (offset >= end() || ch(offset) != '"') {
                throw new JSONException("expected '\"' at offset " + offset);
            }
            offset++;
            skipStringContent();
            skipWhitespace();
            if (offset >= end() || ch(offset) != ':') {
                throw new JSONException("expected ':' at offset " + offset);
            }
            offset++;
            skipValue();
            skipWhitespace();
            if (offset >= end()) {
                throw new JSONException("unterminated object");
            }
            if (ch(offset) == ',') {
                offset++;
                continue;
            }
            if (ch(offset) == '}') {
                offset++;
                return;
            }
            throw new JSONException("expected ',' or '}' at offset " + offset);
        }
    }

    private void skipArray() {
        offset++; // skip '['
        skipWhitespace();
        if (offset < end() && ch(offset) == ']') {
            offset++;
            return;
        }
        for (;;) {
            skipValue();
            skipWhitespace();
            if (offset >= end()) {
                throw new JSONException("unterminated array");
            }
            if (ch(offset) == ',') {
                offset++;
                continue;
            }
            if (ch(offset) == ']') {
                offset++;
                return;
            }
            throw new JSONException("expected ',' or ']' at offset " + offset);
        }
    }

    private void skipStringContent() {
        int e = end();
        while (offset < e) {
            int c = ch(offset++);
            if (c == '"') {
                return;
            }
            if (c == '\\') {
                if (offset >= e) {
                    throw new JSONException("unterminated string escape");
                }
                if (ch(offset) == 'u') {
                    offset += 5; // skip unicode escape
                } else {
                    offset++;
                }
            }
        }
        throw new JSONException("unterminated string");
    }

    private void skipNumber() {
        int e = end();
        if (offset < e && ch(offset) == '-') {
            offset++;
        }
        while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
            offset++;
        }
        if (offset < e && ch(offset) == '.') {
            offset++;
            while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
                offset++;
            }
        }
        if (offset < e && (ch(offset) == 'e' || ch(offset) == 'E')) {
            offset++;
            if (offset < e && (ch(offset) == '+' || ch(offset) == '-')) {
                offset++;
            }
            while (offset < e && ch(offset) >= '0' && ch(offset) <= '9') {
                offset++;
            }
        }
    }

    /** Read null literal and advance. Throws if not 'null'. */
    private void readNullLiteral() {
        if (offset + 4 > end()
                || ch(offset) != 'n' || ch(offset + 1) != 'u'
                || ch(offset + 2) != 'l' || ch(offset + 3) != 'l') {
            throw new JSONException("expected 'null' at offset " + offset);
        }
        offset += 4;
    }

    // ==================== Hex decode helper ====================

    static int hexToInt(int c) {
        if (c < 128) {
            int v = HEX_VALUES[c];
            if (v != HEX_INVALID) {
                return v;
            }
        }
        throw new JSONException("invalid hex character: '" + (char) c + "'");
    }

    // ==================== String implementations ====================

    static final class Str extends JSONParser {
        private final String json;
        private final int length;

        Str(String json, long features) {
            super(features);
            this.json = json;
            this.length = json.length();
        }

        @Override
        int ch(int i) {
            return json.charAt(i);
        }

        @Override
        int end() {
            return length;
        }

        @Override
        String extractString(int start, int end) {
            return json.substring(start, end);
        }

        @Override
        String readStringContent() {
            int start = offset;
            while (offset < length) {
                char c = json.charAt(offset);
                if (c == '"') {
                    String result = json.substring(start, offset);
                    offset++;
                    return result;
                }
                if (c == '\\') {
                    return readStringEscaped(start);
                }
                offset++;
            }
            throw new JSONException("unterminated string");
        }

        private String readStringEscaped(int start) {
            StringBuilder sb = new StringBuilder(offset - start + 16);
            sb.append(json, start, offset);
            while (offset < length) {
                char c = json.charAt(offset++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (offset >= length) {
                        throw new JSONException("unterminated string escape");
                    }
                    c = json.charAt(offset++);
                    switch (c) {
                        case '"', '\\', '/' -> sb.append(c);
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (offset + 4 > length) {
                                throw new JSONException("unterminated unicode escape");
                            }
                            int code = (hexToInt(json.charAt(offset)) << 12)
                                    | (hexToInt(json.charAt(offset + 1)) << 8)
                                    | (hexToInt(json.charAt(offset + 2)) << 4)
                                    | hexToInt(json.charAt(offset + 3));
                            sb.append((char) code);
                            offset += 4;
                        }
                        default -> throw new JSONException("invalid escape: \\" + c);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new JSONException("unterminated string");
        }
    }

    public static final class UTF8 extends JSONParser {
        private final byte[] bytes;
        private final int end;

        // SWAR (SIMD Within A Register) constants for readStringDirect 8-byte-at-a-time scanning.
        // Zero-byte detection formula: hasZero(v) = (v - 0x0101...) & ~v & 0x8080...
        // WARNING: 不可省略 & ~v 项，否则会产生误检。
        private static final long SWAR_QUOTE = 0x2222222222222222L;  // '"' broadcast
        private static final long SWAR_ESCAPE = 0x5C5C5C5C5C5C5C5CL; // '\\' broadcast
        private static final long SWAR_LO = 0x0101010101010101L;
        private static final long SWAR_HI = 0x8080808080808080L;

        UTF8(byte[] bytes, int offset, int length, long features) {
            super(features);
            this.bytes = bytes;
            this.offset = offset;
            this.end = offset + length;
        }

        @Override
        int ch(int i) {
            return bytes[i] & 0xFF;
        }

        @Override
        int end() {
            return end;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        protected void skipWhitespace() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e) {
                byte c = b[off];
                if (c > ' ' || !WHITESPACE[c & 0xFF]) {
                    break;
                }
                off++;
            }
            this.offset = off;
        }

        /**
         * Single-pass field name matching: reads the first byte, uses candidate dispatch,
         * and checks if the input at the expected position is a closing quote.
         * Avoids scanning for the closing quote separately.
         *
         * @return the matched FieldReader, or null if not found
         */
        public com.alibaba.fastjson3.reader.FieldReader readFieldNameMatch(
                com.alibaba.fastjson3.reader.FieldNameMatcher matcher) {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;

            // Inline skipWhitespace
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e || b[off] != '"') {
                throw new JSONException("expected '\"' for field name at offset " + off);
            }
            off++; // skip opening quote

            // Single-pass: check candidates by first byte + position-based quote check
            if (off < e) {
                int firstByte = b[off] & 0x7F;
                com.alibaba.fastjson3.reader.FieldNameMatcher.ByteCandidate[] candidates =
                        matcher.getCandidates(firstByte);
                if (candidates != null) {
                    for (com.alibaba.fastjson3.reader.FieldNameMatcher.ByteCandidate c : candidates) {
                        int endPos = off + c.nameLen;
                        if (endPos < e && b[endPos] == '"') {
                            // Potential match: compare remaining bytes (first byte already matched)
                            if (c.nameLen <= 1
                                    || com.alibaba.fastjson3.util.JDKUtils.arrayEquals(
                                    b, off + 1, c.nameBytes, 1, c.nameLen - 1)) {
                                // Match! Skip past closing quote, whitespace, and colon
                                off = endPos + 1;
                                while (off < e && b[off] <= ' ') {
                                    off++;
                                }
                                if (off >= e || b[off] != ':') {
                                    throw new JSONException("expected ':' at offset " + off);
                                }
                                this.offset = off + 1;
                                return c.reader;
                            }
                        }
                    }
                }
            }

            // No candidate matched: scan to closing quote + colon (unknown field)
            int nameStart = off;
            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    off++;
                    while (off < e && b[off] <= ' ') {
                        off++;
                    }
                    if (off >= e || b[off] != ':') {
                        throw new JSONException("expected ':' at offset " + off);
                    }
                    this.offset = off + 1;
                    return null;
                }
                if (c == '\\') {
                    // Has escapes: use hash-based fallback
                    this.offset = nameStart;
                    String name = readStringContent();
                    skipWhitespace();
                    if (this.offset >= e || b[this.offset] != ':') {
                        throw new JSONException("expected ':' at offset " + this.offset);
                    }
                    this.offset++;
                    long hash = matcher.hash(name);
                    return matcher.match(hash);
                }
                off++;
            }
            throw new JSONException("unterminated field name");
        }

        /**
         * Fast PLHV-only field name hash reader. Kept small for JIT inlining.
         * Assumes PLHV strategy. For BIHV/PRHV, falls through to readFieldNameHashSlow.
         *
         * <p>WARNING: 性能敏感方法，修改前务必做 benchmark 验证。关键设计约束：
         * <ul>
         *   <li>此方法省略了边界检查，前提是输入为合法 JSON（保证有 '"' 和 ':'）</li>
         *   <li>2-byte 展开的 for(;;) 循环是刻意设计，不可改回 while(off&lt;e) 单字节循环</li>
         *   <li>方法体必须保持小于 ~325 字节码以确保 JIT 内联</li>
         * </ul>
         */
        public long readFieldNameHashPLHV() {
            final byte[] b = this.bytes;
            int off = this.offset;
            // Skip whitespace
            while (off < end && b[off] <= ' ') {
                off++;
            }
            if (off >= end || b[off] != '"') {
                throw new JSONException("expected '\"' for field name at offset " + off);
            }
            off++;

            // 2-byte unrolled hash loop, no bounds check (closing '"' guaranteed)
            long hash = 0;
            for (;;) {
                int c1 = b[off] & 0xFF;
                if (c1 == '"') {
                    off++;
                    break;
                }
                if (c1 == '\\' || c1 >= 0x80) {
                    // Non-ASCII or escape: fall through to slow path for correct char-based hashing
                    this.offset = off;
                    return readFieldNameHashEscape(off);
                }
                int c2 = b[off + 1] & 0xFF;
                if (c2 == '"') {
                    hash += c1;
                    off += 2;
                    break;
                }
                if (c2 == '\\' || c2 >= 0x80) {
                    hash += c1;
                    this.offset = off + 1;
                    return readFieldNameHashEscape(off + 1);
                }
                hash += c1 + c2;
                off += 2;
            }

            // Skip whitespace, ':', and whitespace after ':'
            while (off < end && b[off] <= ' ') {
                off++;
            }
            off++; // skip ':'
            while (off < end && b[off] <= ' ') {
                off++;
            }
            this.offset = off;
            return hash;
        }

        private long readFieldNameHashEscape(int nameStart) {
            // offset is already set before the escape
            this.offset = nameStart;
            // Find the start (after opening quote which is at nameStart - (nameStart - start of field))
            // Actually we need to re-read from the opening quote. Let's just use the slow path.
            String name = readStringContent();
            final byte[] b = this.bytes;
            final int e = this.end;
            skipWhitespace();
            if (this.offset >= e || b[this.offset] != ':') {
                throw new JSONException("expected ':' at offset " + this.offset);
            }
            this.offset++;
            skipWhitespace(); // skip WS after ':'
            // PLHV hash
            long hash = 0;
            for (int i = 0; i < name.length(); i++) {
                hash += name.charAt(i);
            }
            return hash;
        }

        @Override
        public long readFieldNameHash(com.alibaba.fastjson3.reader.FieldNameMatcher matcher) {
            if (matcher.strategy == com.alibaba.fastjson3.reader.FieldNameMatcher.STRATEGY_PLHV) {
                return readFieldNameHashPLHV();
            }
            return readFieldNameHashSlow(matcher);
        }

        private long readFieldNameHashSlow(com.alibaba.fastjson3.reader.FieldNameMatcher matcher) {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e || b[off] != '"') {
                throw new JSONException("expected '\"' for field name at offset " + off);
            }
            off++;

            final int strategy = matcher.strategy;
            long hash = 0;
            int start = off;

            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    off++;
                    while (off < e && b[off] <= ' ') {
                        off++;
                    }
                    if (off >= e || b[off] != ':') {
                        throw new JSONException("expected ':' at offset " + off);
                    }
                    off++; // skip ':'
                    while (off < e && b[off] <= ' ') {
                        off++;
                    }
                    this.offset = off;
                    return hash;
                }
                if (c == '\\') {
                    this.offset = start;
                    String name = readStringContent();
                    skipWhitespace();
                    if (this.offset >= e || b[this.offset] != ':') {
                        throw new JSONException("expected ':' at offset " + this.offset);
                    }
                    this.offset++;
                    skipWhitespace();
                    return matcher.hash(name);
                }

                // Decode UTF-8 to char for hash (must match FieldNameMatcher.hash which uses chars)
                int ch;
                if (c >= 0) {
                    // ASCII
                    ch = c;
                    off++;
                } else if ((c & 0xE0) == 0xC0 && off + 1 < e) {
                    ch = ((c & 0x1F) << 6) | (b[off + 1] & 0x3F);
                    off += 2;
                } else if ((c & 0xF0) == 0xE0 && off + 2 < e) {
                    ch = ((c & 0x0F) << 12) | ((b[off + 1] & 0x3F) << 6) | (b[off + 2] & 0x3F);
                    off += 3;
                } else if ((c & 0xF8) == 0xF0 && off + 3 < e) {
                    // 4-byte UTF-8 → surrogate pair (two chars)
                    int cp = ((c & 0x07) << 18) | ((b[off + 1] & 0x3F) << 12)
                            | ((b[off + 2] & 0x3F) << 6) | (b[off + 3] & 0x3F);
                    off += 4;
                    hash = matcher.hashStep(hash, Character.highSurrogate(cp));
                    ch = Character.lowSurrogate(cp);
                } else {
                    ch = c & 0xFF;
                    off++;
                }
                hash = matcher.hashStep(hash, ch);
            }
            throw new JSONException("unterminated field name");
        }

        @Override
        public int readInt() {
            // Inline skipWhitespace + direct byte access
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e) {
                throw new JSONException("unexpected end of input");
            }

            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                off++;
                if (off >= e) {
                    throw new JSONException("unexpected end in number");
                }
                c = b[off] & 0xFF;
            }

            if (c < '0' || c > '9') {
                this.offset = off;
                throw new JSONException("expected digit at offset " + off);
            }

            int value = c - '0';
            off++;
            while (off < e) {
                c = b[off] & 0xFF;
                if (c < '0' || c > '9') {
                    break;
                }
                if (value > 214748364 || (value == 214748364 && c > '7' + (neg ? 1 : 0))) {
                    this.offset = off - countDigitsBackward(value, neg);
                    return readNumber().intValue();
                }
                value = value * 10 + (c - '0');
                off++;
            }
            if (off < e && (b[off] == '.' || b[off] == 'e' || b[off] == 'E')) {
                this.offset = off - countDigitsBackward(value, neg);
                return readNumber().intValue();
            }

            this.offset = off;
            return neg ? -value : value;
        }

        @Override
        public double readDouble() {
            // Inline skipWhitespace + direct byte access for number scanning
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            this.offset = off;
            return readNumber().doubleValue();
        }

        @Override
        public boolean readBoolean() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off + 4 <= e && b[off] == 't' && b[off + 1] == 'r' && b[off + 2] == 'u' && b[off + 3] == 'e') {
                this.offset = off + 4;
                return true;
            }
            if (off + 5 <= e && b[off] == 'f' && b[off + 1] == 'a' && b[off + 2] == 'l' && b[off + 3] == 's' && b[off + 4] == 'e') {
                this.offset = off + 5;
                return false;
            }
            throw new JSONException("expected 'true' or 'false' at offset " + off);
        }

        @Override
        public String readString() {
            // Inline skipWhitespace
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e || b[off] != '"') {
                throw new JSONException("expected '\"' at offset " + off);
            }
            off++; // skip opening quote
            // Inline readStringContent fast path for ASCII
            int start = off;

            // Vector API: scan VECTOR_SIZE bytes at a time
            if (com.alibaba.fastjson3.util.JDKUtils.VECTOR_SUPPORT) {
                off = com.alibaba.fastjson3.util.VectorizedScanner.scanStringSimple(b, off, e);
            }

            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    int len = off - start;
                    this.offset = off + 1;
                    if (com.alibaba.fastjson3.util.JDKUtils.FAST_STRING_CREATION) {
                        return com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len);
                    }
                    return new String(b, start, len, StandardCharsets.ISO_8859_1);
                }
                if (c == '\\') {
                    this.offset = off;
                    return readStringEscaped(start);
                }
                if (c < 0) {
                    this.offset = off;
                    return readStringUTF8(start);
                }
                off++;
            }
            throw new JSONException("unterminated string");
        }

        /**
         * Read a string value directly from byte[], using SWAR/Vector for fast scanning.
         * Assumes whitespace already skipped by caller.
         */
        public String readStringDirect() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            // Expect '"' (skipWS already done by caller)
            if (off >= e || b[off] != '"') {
                throw new JSONException("expected '\"' at offset " + off);
            }
            off++;
            int start = off;

            // Vector API: scan VECTOR_SIZE bytes at a time (32/64 bytes)
            if (com.alibaba.fastjson3.util.JDKUtils.VECTOR_SUPPORT) {
                off = com.alibaba.fastjson3.util.VectorizedScanner.scanStringSimple(b, off, e);
            } else {
                // SWAR fallback: scan 8 bytes at a time for '"', '\\', or non-ASCII (>= 0x80)
                while (off + 8 <= e) {
                    long word = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                    long v1 = word ^ SWAR_QUOTE;
                    long v2 = word ^ SWAR_ESCAPE;
                    long detect = ((v1 - SWAR_LO) & ~v1)
                            | ((v2 - SWAR_LO) & ~v2)
                            | word; // non-ASCII bytes have high bit set
                    if ((detect & SWAR_HI) != 0) {
                        off += Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3;
                        break;
                    }
                    off += 8;
                }
            }

            // Per-byte scan for the found/remaining bytes
            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    int len = off - start;
                    this.offset = off + 1;
                    if (com.alibaba.fastjson3.util.JDKUtils.FAST_STRING_CREATION) {
                        return com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len);
                    }
                    return new String(b, start, len, StandardCharsets.ISO_8859_1);
                }
                if (c == '\\') {
                    this.offset = off;
                    return readStringEscaped(start);
                }
                if (c < 0) {
                    this.offset = off;
                    return readStringUTF8(start);
                }
                off++;
            }
            throw new JSONException("unterminated string");
        }

        /**
         * Read an integer value directly from byte[], no virtual calls.
         * Assumes whitespace already skipped.
         */
        public int readIntDirect() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            if (off >= e) {
                throw new JSONException("unexpected end of input");
            }
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                // Non-numeric value: fall back to readAny + conversion
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                return val instanceof Number n ? n.intValue() : Integer.parseInt(val.toString());
            }
            int value = c - '0';
            int start = off;
            off++;
            while (off < e) {
                c = b[off] & 0xFF;
                if (c < '0' || c > '9') {
                    break;
                }
                value = value * 10 + (c - '0');
                off++;
            }
            // Overflow guard: int has at most 10 digits
            if (off - start >= 10) {
                this.offset = start - (neg ? 1 : 0);
                return readNumber().intValue();
            }
            this.offset = off;
            return neg ? -value : value;
        }

        private static final double[] NEG_POW10 = {
            1e0, 1e-1, 1e-2, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7, 1e-8, 1e-9,
            1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15, 1e-16, 1e-17, 1e-18
        };

        /**
         * Read a double value directly from byte[], no virtual calls.
         * Fast path for simple decimals (sign + digits + '.' + digits, no exponent).
         * Falls back to Double.parseDouble for exponent notation or very long numbers.
         * Assumes whitespace already skipped.
         */
        public double readDoubleDirect() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            int start = off;

            boolean neg = false;
            if (off < e && b[off] == '-') {
                neg = true;
                off++;
            }

            if (off >= e || b[off] < '0' || b[off] > '9') {
                this.offset = start;
                Object val = readAny();
                return val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString());
            }

            // Parse integer part
            long mantissa = 0;
            while (off < e && b[off] >= '0' && b[off] <= '9') {
                mantissa = mantissa * 10 + (b[off] - '0');
                off++;
            }

            int fracDigits = 0;
            if (off < e && b[off] == '.') {
                off++;
                // Parse fraction
                while (off < e && b[off] >= '0' && b[off] <= '9') {
                    if (fracDigits < 18) { // avoid long overflow
                        mantissa = mantissa * 10 + (b[off] - '0');
                    }
                    fracDigits++;
                    off++;
                }
            }

            // Check for exponent - fall back to parseDouble
            if (off < e && (b[off] == 'e' || b[off] == 'E')) {
                this.offset = start;
                return readNumber().doubleValue();
            }

            this.offset = off;

            if (fracDigits == 0) {
                return neg ? -mantissa : mantissa;
            }

            // Fast path: compute directly
            if (fracDigits <= 18) {
                double result = mantissa * NEG_POW10[fracDigits];
                return neg ? -result : result;
            }

            // Too many fraction digits, fall back
            return Double.parseDouble(new String(b, start, off - start, StandardCharsets.ISO_8859_1));
        }

        /**
         * Read a boolean directly, no skipWS.
         * Assumes whitespace already skipped.
         */
        public boolean readBooleanDirect() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            if (off + 4 <= e && b[off] == 't' && b[off + 1] == 'r' && b[off + 2] == 'u' && b[off + 3] == 'e') {
                this.offset = off + 4;
                return true;
            }
            if (off + 5 <= e && b[off] == 'f' && b[off + 1] == 'a' && b[off + 2] == 'l' && b[off + 3] == 's' && b[off + 4] == 'e') {
                this.offset = off + 5;
                return false;
            }
            throw new JSONException("expected 'true' or 'false' at offset " + off);
        }

        /**
         * Read a long value directly from byte[], no virtual calls.
         * Assumes whitespace already skipped.
         */
        public long readLongDirect() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                return val instanceof Number n ? n.longValue() : Long.parseLong(val.toString());
            }
            int start = off;
            long value = c - '0';
            off++;
            while (off < e) {
                c = b[off] & 0xFF;
                if (c < '0' || c > '9') {
                    break;
                }
                value = value * 10 + (c - '0');
                off++;
            }
            // Overflow guard: Long.MAX_VALUE has 19 digits
            if (off - start >= 19) {
                if (off - start > 19
                        || (!neg && value < 0)
                        || (neg && value < 0 && value != Long.MIN_VALUE)) {
                    this.offset = start - (neg ? 1 : 0);
                    return readNumber().longValue();
                }
            }
            this.offset = off;
            return neg ? -value : value;
        }

        // ==================== Offset-passing methods for hot loop ====================
        // These methods take the current offset as a parameter and return the updated
        // offset, allowing the caller to keep offset in a CPU register across calls.
        // They set the parsed value directly on the bean to avoid multi-return.

        /**
         * Read a string starting at {@code off} (pointing at the opening '"'),
         * set the value on the bean via the reader, return the offset past the closing '"'.
         */
        public int readStringOff(int off, Object bean, com.alibaba.fastjson3.reader.FieldReader reader) {
            final byte[] b = this.bytes;
            off++; // skip opening '"'
            int start = off;

            // Vector API: scan VECTOR_SIZE bytes at a time (32/64 bytes)
            if (com.alibaba.fastjson3.util.JDKUtils.VECTOR_SUPPORT) {
                off = com.alibaba.fastjson3.util.VectorizedScanner.scanStringSimple(b, off, end);
            } else {
                // SWAR fallback: scan 8 bytes at a time for '"', '\\', or non-ASCII (>= 0x80)
                while (off + 8 <= end) {
                    long word = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                    long v1 = word ^ SWAR_QUOTE;
                    long v2 = word ^ SWAR_ESCAPE;
                    long detect = ((v1 - SWAR_LO) & ~v1)
                            | ((v2 - SWAR_LO) & ~v2)
                            | word;
                    if ((detect & SWAR_HI) != 0) {
                        off += Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3;
                        break;
                    }
                    off += 8;
                }
            }

            // Per-byte tail scan
            while (off < end && b[off] != '"') {
                if (b[off] == '\\') {
                    this.offset = off;
                    reader.setObjectValue(bean, readStringEscaped(start));
                    return this.offset;
                }
                if (b[off] < 0) {
                    this.offset = off;
                    reader.setObjectValue(bean, readStringUTF8(start));
                    return this.offset;
                }
                off++;
            }
            if (off >= end) {
                throw new JSONException("unterminated string");
            }

            int len = off - start;
            off++; // skip closing '"'
            reader.setObjectValue(bean, com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len));
            return off;
        }

        /**
         * Read a long starting at {@code off}, set the value on the bean via the reader,
         * return the offset past the last digit.
         */
        public int readLongOff(int off, Object bean, com.alibaba.fastjson3.reader.FieldReader reader) {
            final byte[] b = this.bytes;
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                reader.setFieldValue(bean, reader.convertValue(val));
                return this.offset;
            }
            int start = off;
            long value = c - '0';
            off++;
            while (off < end && (c = b[off] & 0xFF) >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
                off++;
            }
            if (off - start >= 19) {
                // For exactly 19 digits, the fast-path accumulation might overflow.
                // Overflow manifests as value wrapping negative for positive numbers,
                // or wrapping to something other than Long.MIN_VALUE for negative numbers.
                if (off - start > 19
                        || (!neg && value < 0)
                        || (neg && value < 0 && value != Long.MIN_VALUE)) {
                    this.offset = start - (neg ? 1 : 0);
                    reader.setLongValue(bean, readNumber().longValue());
                    return this.offset;
                }
            }
            reader.setLongValue(bean, neg ? -value : value);
            return off;
        }

        /**
         * Read an int starting at {@code off}, set the value on the bean via the reader,
         * return the offset past the last digit.
         */
        public int readIntOff(int off, Object bean, com.alibaba.fastjson3.reader.FieldReader reader) {
            final byte[] b = this.bytes;
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                reader.setFieldValue(bean, reader.convertValue(val));
                return this.offset;
            }
            int value = c - '0';
            int digitStart = off;
            off++;
            while (off < end && (c = b[off] & 0xFF) >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
                off++;
            }
            if (off - digitStart >= 10) {
                this.offset = digitStart - (neg ? 1 : 0);
                reader.setIntValue(bean, readNumber().intValue());
                return this.offset;
            }
            reader.setIntValue(bean, neg ? -value : value);
            return off;
        }

        /**
         * Read a double starting at {@code off}, set the value on the bean via the reader,
         * return the offset past the last digit.
         */
        public int readDoubleOff(int off, Object bean, com.alibaba.fastjson3.reader.FieldReader reader) {
            final byte[] b = this.bytes;
            int start = off;
            boolean neg = false;
            if (off < end && b[off] == '-') {
                neg = true;
                off++;
            }
            if (off >= end || b[off] < '0' || b[off] > '9') {
                // Non-numeric (null, string, boolean, etc.) — fallback
                this.offset = start;
                Object val = readAny();
                reader.setFieldValue(bean, reader.convertValue(val));
                return this.offset;
            }
            long mantissa = 0;
            while (off < end && b[off] >= '0' && b[off] <= '9') {
                mantissa = mantissa * 10 + (b[off] - '0');
                off++;
            }
            int fracDigits = 0;
            if (off < end && b[off] == '.') {
                off++;
                while (off < end && b[off] >= '0' && b[off] <= '9') {
                    if (fracDigits < 18) {
                        mantissa = mantissa * 10 + (b[off] - '0');
                    }
                    fracDigits++;
                    off++;
                }
            }
            if (off < end && (b[off] == 'e' || b[off] == 'E')) {
                this.offset = start;
                reader.setDoubleValue(bean, readNumber().doubleValue());
                return this.offset;
            }
            double result;
            if (fracDigits == 0) {
                result = neg ? (double) -mantissa : (double) mantissa;
            } else if (fracDigits <= 18) {
                result = mantissa * NEG_POW10[fracDigits];
                if (neg) {
                    result = -result;
                }
            } else {
                result = Double.parseDouble(new String(b, start, off - start, StandardCharsets.ISO_8859_1));
            }
            reader.setDoubleValue(bean, result);
            return off;
        }

        /**
         * Read a boolean starting at {@code off}, set the value on the bean via the reader,
         * return the offset past the last character.
         */
        public int readBooleanOff(int off, Object bean, com.alibaba.fastjson3.reader.FieldReader reader) {
            final byte[] b = this.bytes;
            if (off + 4 <= end && b[off] == 't' && b[off + 1] == 'r' && b[off + 2] == 'u' && b[off + 3] == 'e') {
                reader.setBooleanValue(bean, true);
                return off + 4;
            }
            if (off + 5 <= end && b[off] == 'f' && b[off + 1] == 'a' && b[off + 2] == 'l' && b[off + 3] == 's' && b[off + 4] == 'e') {
                reader.setBooleanValue(bean, false);
                return off + 5;
            }
            throw new JSONException("expected 'true' or 'false' at offset " + off);
        }

        // ==================== Direct offset-passing methods (for ASM code gen) ====================
        // These methods take a field offset (long) directly instead of a FieldReader,
        // eliminating the FieldReader indirection and virtual dispatch on setXxxValue.
        // Used by ASM-generated readers where the field offset is a compile-time constant.

        public int readIntOffDirect(int off, Object bean, long fieldOffset) {
            final byte[] b = this.bytes;
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                com.alibaba.fastjson3.util.JDKUtils.putInt(bean, fieldOffset,
                        val instanceof Number n ? n.intValue() : Integer.parseInt(val.toString()));
                return this.offset;
            }
            int value = c - '0';
            int digitStart = off;
            off++;
            while (off < end && (c = b[off] & 0xFF) >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
                off++;
            }
            if (off - digitStart >= 10) {
                this.offset = digitStart - (neg ? 1 : 0);
                com.alibaba.fastjson3.util.JDKUtils.putInt(bean, fieldOffset, readNumber().intValue());
                return this.offset;
            }
            com.alibaba.fastjson3.util.JDKUtils.putInt(bean, fieldOffset, neg ? -value : value);
            return off;
        }

        public int readLongOffDirect(int off, Object bean, long fieldOffset) {
            final byte[] b = this.bytes;
            boolean neg = false;
            int c = b[off] & 0xFF;
            if (c == '-') {
                neg = true;
                c = b[++off] & 0xFF;
            }
            if (c < '0' || c > '9') {
                this.offset = off - (neg ? 1 : 0);
                Object val = readAny();
                com.alibaba.fastjson3.util.JDKUtils.putLongField(bean, fieldOffset,
                        val instanceof Number n ? n.longValue() : Long.parseLong(val.toString()));
                return this.offset;
            }
            int start = off;
            long value = c - '0';
            off++;
            while (off < end && (c = b[off] & 0xFF) >= '0' && c <= '9') {
                value = value * 10 + (c - '0');
                off++;
            }
            if (off - start >= 19) {
                if (off - start > 19
                        || (!neg && value < 0)
                        || (neg && value < 0 && value != Long.MIN_VALUE)) {
                    this.offset = start - (neg ? 1 : 0);
                    com.alibaba.fastjson3.util.JDKUtils.putLongField(bean, fieldOffset, readNumber().longValue());
                    return this.offset;
                }
            }
            com.alibaba.fastjson3.util.JDKUtils.putLongField(bean, fieldOffset, neg ? -value : value);
            return off;
        }

        public int readDoubleOffDirect(int off, Object bean, long fieldOffset) {
            final byte[] b = this.bytes;
            int start = off;
            boolean neg = false;
            if (off < end && b[off] == '-') {
                neg = true;
                off++;
            }
            if (off >= end || b[off] < '0' || b[off] > '9') {
                this.offset = start;
                Object val = readAny();
                com.alibaba.fastjson3.util.JDKUtils.putDouble(bean, fieldOffset,
                        val instanceof Number n ? n.doubleValue() : Double.parseDouble(val.toString()));
                return this.offset;
            }
            long mantissa = 0;
            while (off < end && b[off] >= '0' && b[off] <= '9') {
                mantissa = mantissa * 10 + (b[off] - '0');
                off++;
            }
            int fracDigits = 0;
            if (off < end && b[off] == '.') {
                off++;
                while (off < end && b[off] >= '0' && b[off] <= '9') {
                    if (fracDigits < 18) {
                        mantissa = mantissa * 10 + (b[off] - '0');
                    }
                    fracDigits++;
                    off++;
                }
            }
            if (off < end && (b[off] == 'e' || b[off] == 'E')) {
                this.offset = start;
                com.alibaba.fastjson3.util.JDKUtils.putDouble(bean, fieldOffset, readNumber().doubleValue());
                return this.offset;
            }
            double result;
            if (fracDigits == 0) {
                result = neg ? (double) -mantissa : (double) mantissa;
            } else if (fracDigits <= 18) {
                result = mantissa * NEG_POW10[fracDigits];
                if (neg) {
                    result = -result;
                }
            } else {
                result = Double.parseDouble(new String(b, start, off - start, StandardCharsets.ISO_8859_1));
            }
            com.alibaba.fastjson3.util.JDKUtils.putDouble(bean, fieldOffset, result);
            return off;
        }

        public int readBooleanOffDirect(int off, Object bean, long fieldOffset) {
            final byte[] b = this.bytes;
            if (off + 4 <= end && b[off] == 't' && b[off + 1] == 'r' && b[off + 2] == 'u' && b[off + 3] == 'e') {
                com.alibaba.fastjson3.util.JDKUtils.putBoolean(bean, fieldOffset, true);
                return off + 4;
            }
            if (off + 5 <= end && b[off] == 'f' && b[off + 1] == 'a' && b[off + 2] == 'l' && b[off + 3] == 's' && b[off + 4] == 'e') {
                com.alibaba.fastjson3.util.JDKUtils.putBoolean(bean, fieldOffset, false);
                return off + 5;
            }
            throw new JSONException("expected 'true' or 'false' at offset " + off);
        }

        public int readStringOffDirect(int off, Object bean, long fieldOffset) {
            final byte[] b = this.bytes;
            off++; // skip opening '"'
            int start = off;

            if (com.alibaba.fastjson3.util.JDKUtils.VECTOR_SUPPORT) {
                off = com.alibaba.fastjson3.util.VectorizedScanner.scanStringSimple(b, off, end);
            } else {
                while (off + 8 <= end) {
                    long word = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                    long v1 = word ^ SWAR_QUOTE;
                    long v2 = word ^ SWAR_ESCAPE;
                    long detect = ((v1 - SWAR_LO) & ~v1)
                            | ((v2 - SWAR_LO) & ~v2)
                            | word;
                    if ((detect & SWAR_HI) != 0) {
                        off += Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3;
                        break;
                    }
                    off += 8;
                }
            }

            while (off < end && b[off] != '"') {
                if (b[off] == '\\') {
                    this.offset = off;
                    com.alibaba.fastjson3.util.JDKUtils.putObject(bean, fieldOffset, readStringEscaped(start));
                    return this.offset;
                }
                if (b[off] < 0) {
                    this.offset = off;
                    com.alibaba.fastjson3.util.JDKUtils.putObject(bean, fieldOffset, readStringUTF8(start));
                    return this.offset;
                }
                off++;
            }
            if (off >= end) {
                throw new JSONException("unterminated string");
            }

            int len = off - start;
            off++; // skip closing '"'
            com.alibaba.fastjson3.util.JDKUtils.putObject(bean, fieldOffset,
                    com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len));
            return off;
        }

        // ==================== Inline list/array readers ====================
        // These methods manage offset as a local variable to avoid per-element
        // this.offset reads/writes, eliminating ~40 cycles per element.

        /**
         * Read a JSON array of strings into an ArrayList, with inline offset management.
         * Assumes current position is at '[' (caller already checked).
         * Advances past the closing ']'.
         */
        public java.util.ArrayList<Object> readStringListInline() {
            final byte[] b = this.bytes;
            int off = this.offset;
            off++; // skip '['
            while (off < end && b[off] <= ' ') {
                off++;
            }
            if (off < end && b[off] == ']') {
                this.offset = off + 1;
                return new java.util.ArrayList<>(0);
            }

            java.util.ArrayList<Object> list = new java.util.ArrayList<>(16);
            for (;;) {
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off + 4 <= end && b[off] == 'n' && b[off + 1] == 'u' && b[off + 2] == 'l' && b[off + 3] == 'l') {
                    list.add(null);
                    off += 4;
                } else {
                    off++; // skip opening '"'
                    int start = off;
                    // SWAR scan
                    while (off + 8 <= end) {
                        long word = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                        long v1 = word ^ SWAR_QUOTE;
                        long v2 = word ^ SWAR_ESCAPE;
                        long detect = ((v1 - SWAR_LO) & ~v1)
                                | ((v2 - SWAR_LO) & ~v2)
                                | word;
                        if ((detect & SWAR_HI) != 0) {
                            off += Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3;
                            break;
                        }
                        off += 8;
                    }
                    // Per-byte tail: check for '"', or rare escape/non-ASCII
                    boolean special = false;
                    while (off < end && b[off] != '"') {
                        if (b[off] == '\\') {
                            this.offset = off;
                            list.add(readStringEscaped(start));
                            off = this.offset;
                            special = true;
                            break;
                        }
                        if (b[off] < 0) {
                            this.offset = off;
                            list.add(readStringUTF8(start));
                            off = this.offset;
                            special = true;
                            break;
                        }
                        off++;
                    }
                    if (off >= end && !special) {
                        throw new JSONException("unterminated string");
                    }
                    if (!special) {
                        int len = off - start;
                        off++; // skip closing '"'
                        list.add(com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len));
                    }
                }
                // Inline separator
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off >= end) {
                    throw new JSONException("unexpected end of input");
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == ']') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or ']'");
            }
            this.offset = off;
            return list;
        }

        /**
         * Read a JSON array of strings into a String[], with inline offset management.
         * Assumes current position is at '[' (caller already checked).
         * Advances past the closing ']'.
         */
        public String[] readStringArrayInline() {
            final byte[] b = this.bytes;
            int off = this.offset;
            off++; // skip '['
            while (off < end && b[off] <= ' ') {
                off++;
            }
            if (off < end && b[off] == ']') {
                this.offset = off + 1;
                return new String[0];
            }

            String[] arr = new String[8];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off + 4 <= end && b[off] == 'n' && b[off + 1] == 'u' && b[off + 2] == 'l' && b[off + 3] == 'l') {
                    arr[size++] = null;
                    off += 4;
                } else {
                    off++; // skip '"'
                    int start = off;
                    while (off + 8 <= end) {
                        long word = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(b, off);
                        long v1 = word ^ SWAR_QUOTE;
                        long v2 = word ^ SWAR_ESCAPE;
                        long detect = ((v1 - SWAR_LO) & ~v1)
                                | ((v2 - SWAR_LO) & ~v2)
                                | word;
                        if ((detect & SWAR_HI) != 0) {
                            off += Long.numberOfTrailingZeros(detect & SWAR_HI) >> 3;
                            break;
                        }
                        off += 8;
                    }
                    boolean special = false;
                    while (off < end && b[off] != '"') {
                        if (b[off] == '\\') {
                            this.offset = off;
                            arr[size++] = readStringEscaped(start);
                            off = this.offset;
                            special = true;
                            break;
                        }
                        if (b[off] < 0) {
                            this.offset = off;
                            arr[size++] = readStringUTF8(start);
                            off = this.offset;
                            special = true;
                            break;
                        }
                        off++;
                    }
                    if (off >= end && !special) {
                        throw new JSONException("unterminated string");
                    }
                    if (!special) {
                        int len = off - start;
                        off++;
                        arr[size++] = com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len);
                    }
                }
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off >= end) {
                    throw new JSONException("unexpected end of input");
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == ']') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or ']'");
            }
            this.offset = off;
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        /**
         * Read a JSON array of longs into a long[], with inline offset management.
         * Assumes current position is at '[' (caller already checked).
         * Advances past the closing ']'.
         */
        public long[] readLongArrayInline() {
            final byte[] b = this.bytes;
            int off = this.offset;
            off++; // skip '['
            while (off < end && b[off] <= ' ') {
                off++;
            }
            if (off < end && b[off] == ']') {
                this.offset = off + 1;
                return new long[0];
            }

            long[] arr = new long[2];
            int size = 0;
            for (;;) {
                if (size == arr.length) {
                    arr = java.util.Arrays.copyOf(arr, size + (size >> 1));
                }
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                // Inline long parsing
                boolean neg = false;
                int c = b[off] & 0xFF;
                if (c == '-') {
                    neg = true;
                    c = b[++off] & 0xFF;
                }
                long value = c - '0';
                off++;
                while (off < end && (c = b[off] & 0xFF) >= '0' && c <= '9') {
                    value = value * 10 + (c - '0');
                    off++;
                }
                arr[size++] = neg ? -value : value;
                // Separator
                while (off < end && b[off] <= ' ') {
                    off++;
                }
                if (off >= end) {
                    throw new JSONException("unexpected end of input");
                }
                if (b[off] == ',') {
                    off++;
                    continue;
                }
                if (b[off] == ']') {
                    off++;
                    break;
                }
                throw new JSONException("expected ',' or ']'");
            }
            this.offset = off;
            return size == arr.length ? arr : java.util.Arrays.copyOf(arr, size);
        }

        /**
         * Return the byte at the current offset without advancing or skipping whitespace.
         * Use when whitespace has already been skipped (e.g., after readFieldNameHash).
         */
        public int peekByte() {
            return bytes[offset] & 0xFF;
        }

        /**
         * Try to match a pre-encoded field header ('"fieldName":') at the current position.
         * Skips leading whitespace. If match succeeds, advances offset past the header
         * and any trailing whitespace, then returns true. If match fails, advances offset
         * past the leading whitespace only (so readFieldNameHashPLHV can start without
         * re-skipping WS).
         *
         * <p>Used for ordered-field speculation to avoid hash computation entirely.
         */
        public boolean tryMatchFieldHeader(byte[] header) {
            final byte[] b = this.bytes;
            int off = this.offset;
            // Skip leading whitespace
            while (off < end && b[off] <= ' ') {
                off++;
            }
            int len = header.length;
            // Compare header bytes
            if (off >= end || b[off] != '"') {
                this.offset = off;
                return false;
            }
            if (off + len > end) {
                this.offset = off;
                return false;
            }
            for (int i = 1; i < len; i++) {
                if (b[off + i] != header[i]) {
                    this.offset = off;
                    return false;
                }
            }
            // Match! Advance past header and skip trailing whitespace
            off += len;
            while (off < end && b[off] <= ' ') {
                off++;
            }
            this.offset = off;
            return true;
        }

        /**
         * Offset-based field header matching. Takes offset as parameter, returns new offset
         * on match (>= 0), or -1 on mismatch. On mismatch, sets this.offset to the
         * post-whitespace position for fallback hash-based matching.
         * On match, does NOT write this.offset (caller manages offset as local).
         */
        public int tryMatchFieldHeaderOff(int off, byte[] header) {
            final byte[] b = this.bytes;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            int len = header.length;
            if (off >= e || b[off] != '"' || off + len > e) {
                this.offset = off;
                return -1;
            }
            for (int i = 1; i < len; i++) {
                if (b[off + i] != header[i]) {
                    this.offset = off;
                    return -1;
                }
            }
            off += len;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            return off;
        }

        /**
         * Offset-based separator reading. Takes offset as parameter.
         * Returns positive value (new offset after ',') for comma.
         * Returns negative value for '}' — caller recovers offset as -result.
         * Throws on unexpected character.
         */
        public int readFieldSepOff(int off) {
            final byte[] b = this.bytes;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e) {
                throw new JSONException("unexpected end of input at offset " + off);
            }
            if (b[off] == ',') {
                return off + 1;
            }
            if (b[off] == '}') {
                return -(off + 1);
            }
            throw new JSONException("expected ',' or '}' at offset " + off);
        }

        /**
         * Skip whitespace and return the next byte (or -1 if at end).
         * Does not advance past the returned byte.
         *
         * <p>WARNING: while(off &lt; e) 边界检查不可移除。
         * JIT 依赖有界循环进行范围检查消除和循环展开，实测移除后性能回退 7%。
         */
        public int skipWSAndPeek() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            this.offset = off;
            return off < e ? (b[off] & 0xFF) : -1;
        }

        /**
         * Skip whitespace and check for field separator.
         * Returns 0 for comma (continue), 1 for close brace (done), -1 for error/end.
         * Advances past the separator character.
         *
         * <p>WARNING: while(off &lt; e) 边界检查不可移除，原因同 skipWSAndPeek。
         * 此方法与 readArraySeparator 结构几乎相同（仅 '}' vs ']'），
         * 但不可合并——JIT 对分离的小方法有更好的内联决策。
         */
        public int readFieldSeparator() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e) {
                this.offset = off;
                return -1;
            }
            byte c = b[off];
            this.offset = off + 1;
            if (c == ',') {
                return 0;
            }
            if (c == '}') {
                return 1;
            }
            return -1;
        }

        /**
         * Skip whitespace and check for array element separator.
         * Returns 0 for comma (continue), 1 for close bracket (done), -1 for error/end.
         * Advances past the separator character.
         *
         * <p>WARNING: 不可与 readFieldSeparator 合并，原因见其注释。
         */
        public int readArraySeparator() {
            final byte[] b = this.bytes;
            int off = this.offset;
            final int e = this.end;
            while (off < e && b[off] <= ' ') {
                off++;
            }
            if (off >= e) {
                this.offset = off;
                return -1;
            }
            byte c = b[off];
            this.offset = off + 1;
            if (c == ',') {
                return 0;
            }
            if (c == ']') {
                return 1;
            }
            return -1;
        }

        @Override
        String extractString(int start, int end) {
            return new String(bytes, start, end - start, StandardCharsets.UTF_8);
        }

        @Override
        String readStringContent() {
            int start = offset;
            final byte[] b = this.bytes;
            final int e = this.end;
            int off = start;
            // Fast scan for closing quote (no escape, ASCII-only)
            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    int len = off - start;
                    offset = off + 1;
                    if (com.alibaba.fastjson3.util.JDKUtils.FAST_STRING_CREATION) {
                        return com.alibaba.fastjson3.util.JDKUtils.createLatin1String(b, start, len);
                    }
                    return new String(b, start, len, StandardCharsets.ISO_8859_1);
                }
                if (c == '\\') {
                    offset = off;
                    return readStringEscaped(start);
                }
                if (c < 0) {
                    // Non-ASCII byte: need full UTF-8 decode
                    offset = off;
                    return readStringUTF8(start);
                }
                off++;
            }
            throw new JSONException("unterminated string");
        }

        /**
         * Read string that contains non-ASCII (multi-byte UTF-8) characters.
         * Falls back to new String(bytes, start, len, UTF_8).
         */
        private String readStringUTF8(int start) {
            final byte[] b = this.bytes;
            final int e = this.end;
            int off = offset;
            while (off < e) {
                byte c = b[off];
                if (c == '"') {
                    String result = new String(b, start, off - start, StandardCharsets.UTF_8);
                    offset = off + 1;
                    return result;
                }
                if (c == '\\') {
                    offset = off;
                    return readStringEscaped(start);
                }
                off++;
            }
            throw new JSONException("unterminated string");
        }

        private String readStringEscaped(int start) {
            StringBuilder sb = new StringBuilder(offset - start + 16);
            if (offset > start) {
                sb.append(new String(bytes, start, offset - start, StandardCharsets.UTF_8));
            }
            while (offset < end) {
                int b = bytes[offset++] & 0xFF;
                if (b == '"') {
                    return sb.toString();
                }
                if (b == '\\') {
                    if (offset >= end) {
                        throw new JSONException("unterminated string escape");
                    }
                    b = bytes[offset++] & 0xFF;
                    switch (b) {
                        case '"', '\\', '/' -> sb.append((char) b);
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (offset + 4 > end) {
                                throw new JSONException("unterminated unicode escape");
                            }
                            int code = (hexToInt(bytes[offset] & 0xFF) << 12)
                                    | (hexToInt(bytes[offset + 1] & 0xFF) << 8)
                                    | (hexToInt(bytes[offset + 2] & 0xFF) << 4)
                                    | hexToInt(bytes[offset + 3] & 0xFF);
                            sb.append((char) code);
                            offset += 4;
                        }
                        default -> throw new JSONException("invalid escape: \\" + (char) b);
                    }
                } else if (b < 0x80) {
                    sb.append((char) b);
                } else {
                    // Multi-byte UTF-8 sequence
                    int seqStart = offset - 1;
                    int seqLen;
                    if ((b & 0xE0) == 0xC0) {
                        seqLen = 2;
                    } else if ((b & 0xF0) == 0xE0) {
                        seqLen = 3;
                    } else if ((b & 0xF8) == 0xF0) {
                        seqLen = 4;
                    } else {
                        throw new JSONException("invalid UTF-8 byte: 0x" + Integer.toHexString(b));
                    }
                    int seqEnd = seqStart + seqLen;
                    if (seqEnd > end) {
                        throw new JSONException("truncated UTF-8 sequence");
                    }
                    sb.append(new String(bytes, seqStart, seqLen, StandardCharsets.UTF_8));
                    offset = seqEnd;
                }
            }
            throw new JSONException("unterminated string");
        }
    }

    static final class CharArray extends JSONParser {
        private final char[] chars;
        private final int end;

        CharArray(char[] chars, int offset, int length, long features) {
            super(features);
            this.chars = chars;
            this.offset = offset;
            this.end = offset + length;
        }

        @Override
        int ch(int i) {
            return chars[i];
        }

        @Override
        int end() {
            return end;
        }

        @Override
        String extractString(int start, int end) {
            return new String(chars, start, end - start);
        }

        @Override
        String readStringContent() {
            int start = offset;
            while (offset < end) {
                char c = chars[offset];
                if (c == '"') {
                    String result = new String(chars, start, offset - start);
                    offset++;
                    return result;
                }
                if (c == '\\') {
                    return readStringEscaped(start);
                }
                offset++;
            }
            throw new JSONException("unterminated string");
        }

        private String readStringEscaped(int start) {
            StringBuilder sb = new StringBuilder(offset - start + 16);
            sb.append(chars, start, offset - start);
            while (offset < end) {
                char c = chars[offset++];
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (offset >= end) {
                        throw new JSONException("unterminated string escape");
                    }
                    c = chars[offset++];
                    switch (c) {
                        case '"', '\\', '/' -> sb.append(c);
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (offset + 4 > end) {
                                throw new JSONException("unterminated unicode escape");
                            }
                            int code = (hexToInt(chars[offset]) << 12)
                                    | (hexToInt(chars[offset + 1]) << 8)
                                    | (hexToInt(chars[offset + 2]) << 4)
                                    | hexToInt(chars[offset + 3]);
                            sb.append((char) code);
                            offset += 4;
                        }
                        default -> throw new JSONException("invalid escape: \\" + c);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new JSONException("unterminated string");
        }
    }
}

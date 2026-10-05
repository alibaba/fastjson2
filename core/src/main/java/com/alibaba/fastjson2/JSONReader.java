package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.*;

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReader.BigIntegerCreator.BIG_INTEGER_CREATOR;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

/**
 * JSONReader is the core class for reading and parsing JSON data in FASTJSON2.
 * It provides methods to read various data types from JSON including primitives,
 * objects, arrays, dates, and custom types.
 *
 * <p>JSONReader supports multiple input sources including strings, byte arrays,
 * streams, and readers. It also supports different character encodings such as
 * UTF-8, UTF-16, and ASCII.</p>
 *
 * <p>Example usage:
 * <pre>
 * String json = "{\"name\":\"John\", \"age\":30}";
 * try (JSONReader reader = JSONReader.of(json)) {
 *     JSONObject obj = reader.readJSONObject();
 *     String name = (String) obj.get("name");
 *     Integer age = (Integer) obj.get("age");
 * }
 * </pre>
 *
 *
 * @since 2.0.0
 */
public abstract class JSONReader
        implements Closeable {
    static final int MAX_EXP = 2047;
    static final int MAX_NUMBER_DIGITS = 10_000;

    static final byte
            JSON_TYPE_INT = 1,
            JSON_TYPE_DEC = 2,
            JSON_TYPE_STRING = 3,
            JSON_TYPE_BOOL = 4,
            JSON_TYPE_NULL = 5,
            JSON_TYPE_OBJECT = 6,
            JSON_TYPE_ARRAY = 7,
            JSON_TYPE_BIG_DEC = 8,
            JSON_TYPE_INT8 = 9,
            JSON_TYPE_INT16 = 10,
            JSON_TYPE_INT64 = 11,
            JSON_TYPE_FLOAT = 12,
            JSON_TYPE_DOUBLE = 13,
            JSON_TYPE_NaN = 14;

    static final char EOI = 0x1A;
    static final long SPACE = 1L | (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    static final boolean[] INT_VALUE_END = new boolean[256];
    static {
        Arrays.fill(INT_VALUE_END, true);
        char[] chars = {'.', 'e', 'E', 't', 'f', 'n', '{', '[', '0', '1', '2', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char ch : chars) {
            INT_VALUE_END[ch] = false;
        }
    }

    static final byte[] CHAR1_ESCAPED;
    static {
        byte[] char1_escaped = new byte[128];
        Arrays.fill(char1_escaped, (byte) -1);
        byte[] mapping = new byte[] {
                '0', '\0',
                '1', '\1',
                '2', '\2',
                '3', '\3',
                '4', '\4',
                '5', '\5',
                '6', '\6',
                '7', '\7',
                'b', '\b',
                't', '\t',
                'n', '\n',
                'v', '\u000b',
                'f', '\f',
                'F', '\f',
                'r', '\r',
                '"', '"',
                '\'', '\'',
                '/', '/',
                '.', '.',
                '\\', '\\',
                '#', '#',
                '&', '&',
                '[', '[',
                ']', ']',
                '@', '@',
                '(', '(',
                ')', ')',
                '_', '_',
                ',', ',',
                '~', '~',
                ' ', ' ',
        };
        for (int i = 0; i < mapping.length; i += 2) {
            char1_escaped[mapping[i]] = mapping[i + 1];
        }
        CHAR1_ESCAPED = char1_escaped;
    }

    protected static int newCapacity(int minCapacity, int oldCapacity) {
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity < 0) {
            throw new OutOfMemoryError();
        }
        return newCapacity;
    }

    protected final Context context;
    public final boolean jsonb;
    public final boolean utf8;

    List<ResolveTask> resolveTasks;

    protected int offset;
    protected char ch;
    protected boolean comma;
    protected int nameBegin;

    protected boolean nameEscape;
    protected boolean valueEscape;
    protected boolean wasNull;
    protected boolean boolValue;
    protected boolean negative;

    protected byte valueType;
    protected short exponent;
    protected short scale;

    protected int mag0;
    protected int mag1;
    protected int mag2;
    protected int mag3;

    protected int level;

    protected String stringValue;
    protected Object complex; // Map | List

    protected boolean typeRedirect; // redirect for {"@type":"xxx"",...

    protected byte[] doubleChars;

    /**
     * Gets the current character being processed by the reader.
     *
     * @return The current character
     */
    public final char current() {
        return ch;
    }

    /**
     * Checks if the reader has reached the end of the input.
     *
     * @return true if at the end of input, false otherwise
     */
    public boolean isEnd() {
        return ch == EOI;
    }

    /**
     * Gets the binary type marker at the current JSONB position.
     * Text readers return {@link Byte#MIN_VALUE}; this is not a format-independent value classification.
     * <details><summary>中文</summary>返回当前位置的 JSONB 类型标记；文本读取器返回 Byte.MIN_VALUE，不能将其当作跨格式的值类型分类。</details>
     *
     * @return The type of the current JSON value, or -128 if not applicable
     * @since 2.0.51
     */
    public byte getType() {
        return -128;
    }

    /**
     * Checks if the current character represents the start of an integer value.
     *
     * @return true if the current character is '-', '+', or a digit, false otherwise
     */
    public boolean isInt() {
        return ch == '-' || ch == '+' || (ch >= '0' && ch <= '9');
    }

    /**
     * Checks if the current JSON value is null.
     *
     * @return true if the current value is null, false otherwise
     */
    public abstract boolean isNull();

    /**
     * Checks if the reader has encountered a comma.
     *
     * @return true if a comma was encountered, false otherwise
     */
    public final boolean hasComma() {
        return comma;
    }

    /**
     * Reads a Date value from JSON data, returning null if the value is null.
     *
     * @return The Date value, or null if the value is null in JSON
     */
    public abstract Date readNullOrNewDate();

    /**
     * Checks if the current JSON value is null and advances the reader if it is.
     *
     * @return true if the current value is null, false otherwise
     */
    public abstract boolean nextIfNull();

    /**
     * Constructs a new JSONReader with the specified context and configuration.
     *
     * @param context the reading context to use
     * @param jsonb whether to use JSONB binary format
     * @param utf8 whether to use UTF-8 encoding
     * @since 2.0.51
     */
    public JSONReader(Context context, boolean jsonb, boolean utf8) {
        this.context = context;
        this.jsonb = jsonb;
        this.utf8 = utf8;
    }

    /**
     * Gets the reading context for this JSONReader.
     *
     * @return The Context object
     */
    public final Context getContext() {
        return context;
    }

    /**
     * Throws a JSONException if the specified class is not serializable and the
     * ErrorOnNoneSerializable feature is enabled.
     *
     * @param objectClass The class to check for serializability
     * @throws JSONException if the class is not serializable and the feature is enabled
     */
    public final void errorOnNoneSerializable(Class objectClass) {
        if ((context.features & MASK_ERROR_ON_NONE_SERIALIZABLE) != 0
                && !Serializable.class.isAssignableFrom(objectClass)) {
            throw new JSONException("not support none-Serializable, class ".concat(objectClass.getName()));
        }
    }

    /**
     * Checks if a specific feature is enabled in the reading context.
     *
     * @param feature The feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public final boolean isEnabled(Feature feature) {
        return (context.features & feature.mask) != 0;
    }

    /**
     * Gets the locale used for parsing in this JSONReader.
     *
     * @return The Locale object
     */
    public final Locale getLocale() {
        return context.getLocale();
    }

    /**
     * Gets the zone ID used for date/time parsing in this JSONReader.
     *
     * @return The ZoneId object
     */
    public final ZoneId getZoneId() {
        return context.getZoneId();
    }

    /**
     * Combines the context features with the specified additional features.
     *
     * @param features Additional features to combine with context features
     * @return The combined features bitmask
     */
    public final long features(long features) {
        return context.features | features;
    }

    /**
     * Gets a packed four-byte input prefix for optimized matching without consuming it.
     * This does not parse a JSON integer; use {@link #readInt32Value()} for that purpose.
     * <details><summary>中文</summary>返回用于快速匹配的四字节输入前缀，不推进位置；读取 JSON 整数应使用 readInt32Value。</details>
     *
     * @return The raw integer value
     */
    public abstract int getRawInt();

    /**
     * Gets a packed eight-byte input prefix for optimized matching without consuming it.
     * This does not parse a JSON integer; use {@link #readInt64Value()} for that purpose.
     * <details><summary>中文</summary>返回用于快速匹配的八字节输入前缀，不推进位置；读取 JSON 长整数应使用 readInt64Value。</details>
     *
     * @return The raw long value
     */
    public abstract long getRawLong();

    /**
     * Checks if the next field name matches a 2-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @return true if the field name matches the 2-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match2();

    /**
     * Checks if the next value matches a 2-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @return true if the value matches the 2-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match2() {
        return false;
    }

    /**
     * Checks if the next field name matches a 3-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @return true if the field name matches the 3-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match3();

    /**
     * Checks if the next value matches a 3-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @return true if the value matches the 3-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match3() {
        return false;
    }

    /**
     * Checks if the next field name matches a 4-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param c4 the fourth character to match
     * @return true if the field name matches the 4-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match4(byte c4);

    /**
     * Checks if the next value matches a 4-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param c4 the fourth character to match
     * @return true if the value matches the 4-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match4(byte c4) {
        return false;
    }

    /**
     * Checks if the next field name matches a 5-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 5-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match5(int name1);

    /**
     * Checks if the next value matches a 5-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param c4 the fourth character to match
     * @param c5 the fifth character to match
     * @return true if the value matches the 5-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match5(byte c4, byte c5) {
        return false;
    }

    /**
     * Checks if the next field name matches a 6-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 6-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match6(int name1);

    /**
     * Checks if the next value matches a 6-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the value matches the 6-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match6(int name1) {
        return false;
    }

    /**
     * Checks if the next field name matches a 7-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 7-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match7(int name1);

    /**
     * Checks if the next value matches a 7-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the value matches the 7-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match7(int name1) {
        return false;
    }

    /**
     * Checks if the next field name matches an 8-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param c8 the eighth character to match
     * @return true if the field name matches the 8-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match8(int name1, byte c8);

    /**
     * Checks if the next value matches an 8-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param c8 the eighth character to match
     * @return true if the value matches the 8-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match8(int name1, byte c8) {
        return false;
    }

    /**
     * Checks if the next field name matches a 9-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 9-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match9(long name1);

    /**
     * Checks if the next value matches a 9-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param c8 the eighth character to match
     * @param c9 the ninth character to match
     * @return true if the value matches the 9-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match9(int name1, byte c8, byte c9) {
        return false;
    }

    /**
     * Checks if the next field name matches a 10-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 10-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match10(long name1);

    /**
     * Checks if the next value matches a 10-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the value matches the 10-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match10(long name1) {
        return false;
    }

    /**
     * Checks if the next field name matches an 11-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 11-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match11(long name1);

    /**
     * Checks if the next value matches an 11-character pattern.
     * Low-level generated-reader helper used after the caller verifies the packed input prefix.
     * A successful match consumes the remaining value; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用，调用前须验证打包的输入前缀；匹配成功后消费剩余值，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the value matches the 11-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfValue4Match11(long name1) {
        return false;
    }

    /**
     * Checks if the next field name matches a 12-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 12-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match12(long name1, byte name2);

    /**
     * Checks if the next field name matches a 13-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 13-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match13(long name1, int name2);

    /**
     * Checks if the next field name matches a 14-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 14-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName4Match14(long name1, int name2) {
        return false;
    }

    /**
     * Checks if the next field name matches a 15-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 15-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName4Match15(long name1, int name2) {
        return false;
    }

    /**
     * Checks if the next field name matches a 16-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 4 bytes of the name to match
     * @param name3 the last byte of the name to match
     * @return true if the field name matches the 16-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match16(long name1, int name2, byte name3);

    /**
     * Checks if the next field name matches a 17-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 17-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match17(long name1, long name2);

    /**
     * Checks if the next field name matches an 18-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 18-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match18(long name1, long name2);

    /**
     * Checks if the next field name matches a 19-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 19-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName4Match19(long name1, long name2) {
        return false;
    }

    /**
     * Checks if the next field name matches a 20-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the last byte of the name to match
     * @return true if the field name matches the 20-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match20(long name1, long name2, byte name3);

    /**
     * Checks if the next field name matches a 21-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 21-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName4Match21(long name1, long name2, int name3) {
        return false;
    }

    /**
     * Checks if the next field name matches a 22-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 22-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match22(long name1, long name2, int name3);

    /**
     * Checks if the next field name matches a 23-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 23-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match23(long name1, long name2, int name3);

    /**
     * Checks if the next field name matches a 24-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 4 bytes of the name to match
     * @param name4 the last byte of the name to match
     * @return true if the field name matches the 24-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match24(long name1, long name2, int name3, byte name4);

    /**
     * Checks if the next field name matches a 25-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 25-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match25(long name1, long name2, long name3);

    /**
     * Checks if the next field name matches a 26-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 26-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match26(long name1, long name2, long name3);

    /**
     * Checks if the next field name matches a 27-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 27-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match27(long name1, long name2, long name3);

    /**
     * Checks if the next field name matches a 28-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 8 bytes of the name to match
     * @param c28 the last byte of the name to match
     * @return true if the field name matches the 28-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match28(long name1, long name2, long name3, byte c28);

    /**
     * Checks if the next field name matches a 29-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 29-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match29(long name1, long name2, long name3, int name4);

    /**
     * Checks if the next field name matches a 30-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 30-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match30(long name1, long name2, long name3, int name4);

    /**
     * Checks if the next field name matches a 31-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 31-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match31(long name1, long name2, long name3, int name4);

    /**
     * Checks if the next field name matches a 32-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second to last 8 bytes of the name to match
     * @param name4 the second to last 4 bytes of the name to match
     * @param c32 the last byte of the name to match
     * @return true if the field name matches the 32-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match32(long name1, long name2, long name3, int name4, byte c32);

    /**
     * Checks if the next field name matches a 33-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 33-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match33(long name1, long name2, long name3, long name4);

    /**
     * Checks if the next field name matches a 34-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 34-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match34(long name1, long name2, long name3, long name4);

    /**
     * Checks if the next field name matches a 35-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 35-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match35(long name1, long name2, long name3, long name4);

    /**
     * Checks if the next field name matches a 36-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the second to last 8 bytes of the name to match
     * @param c35 the last byte of the name to match
     * @return true if the field name matches the 36-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match36(long name1, long name2, long name3, long name4, byte c35);

    /**
     * Checks if the next field name matches a 37-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third to last 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 37-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match37(long name1, long name2, long name3, long name4, int name5);

    /**
     * Checks if the next field name matches a 38-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third to last 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 38-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match38(long name1, long name2, long name3, long name4, int name5);

    /**
     * Checks if the next field name matches a 39-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third to last 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 39-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match39(long name1, long name2, long name3, long name4, int name5);

    /**
     * Checks if the next field name matches a 40-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third to last 8 bytes of the name to match
     * @param name5 the second to last 4 bytes of the name to match
     * @param c40 the last byte of the name to match
     * @return true if the field name matches the 40-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match40(long name1, long name2, long name3, long name4, int name5, byte c40);

    /**
     * Checks if the next field name matches a 41-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third middle 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 41-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match41(long name1, long name2, long name3, long name4, long name5);

    /**
     * Checks if the next field name matches a 42-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third middle 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 42-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match42(long name1, long name2, long name3, long name4, long name5);

    /**
     * Checks if the next field name matches a 43-character pattern.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @param name1 packed continuation bytes to match, including delimiters where required by this helper
     * @param name2 the middle 8 bytes of the name to match
     * @param name3 the second middle 8 bytes of the name to match
     * @param name4 the third middle 8 bytes of the name to match
     * @param name5 packed continuation bytes to match, including delimiters where required by this helper
     * @return true if the field name matches the 43-character pattern, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfName4Match43(long name1, long name2, long name3, long name4, long name5);

    /**
     * Checks if the next field name matches an 8-character pattern with no additional characters.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @return true if the field name matches the 8-character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName8Match0() {
        return false;
    }

    /**
     * Checks if the next field name matches an 8-character pattern with 1 additional character.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @return true if the field name matches the 8+1 character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName8Match1() {
        return false;
    }

    /**
     * Checks if the next field name matches an 8-character pattern with 2 additional characters.
     * Low-level generated-reader helper: the caller must first verify the packed input prefix.
     * A successful match consumes the remaining name and separator; unsupported implementations return false.
     * <details><summary>中文</summary>供生成读取器使用：调用前须验证打包的输入前缀；匹配成功后消费剩余字段名及分隔符，不支持的实现返回 false。</details>
     *
     * @return true if the field name matches the 8+2 character pattern, false otherwise
     * @since 2.0.51
     */
    public boolean nextIfName8Match2() {
        return false;
    }

    /**
     * Handles resolve tasks for circular references in the JSON data.
     * This method processes all pending reference resolution tasks after the main
     * parsing is complete, resolving circular references and updating the object graph.
     * This also resolves forward and shared references. Call it after low-level read methods
     * when the parsed value can contain deferred references; the supplied root anchors JSONPath evaluation.
     * <details><summary>中文</summary>同时处理前向引用与共享引用；低层读取可能登记延迟引用时，应在读取完成后调用，传入根对象作为 JSONPath 求值起点。</details>
     *
     * @param root The root object of the parsed JSON structure
     */
    public final void handleResolveTasks(Object root) {
        if (resolveTasks == null) {
            return;
        }

        Object previous = null;
        for (ResolveTask resolveTask : resolveTasks) {
            JSONPath path = resolveTask.reference;
            FieldReader fieldReader = resolveTask.fieldReader;

            Object fieldValue;
            if (path.isPrevious()) {
                fieldValue = previous;
            } else {
                if (!path.isRef()) {
                    throw new JSONException("reference path invalid : " + path);
                }
                path.setReaderContext(context);
                if ((context.features & Feature.FieldBased.mask) != 0) {
                    JSONWriter.Context writeContext = JSONFactory.createWriteContext();
                    writeContext.features |= JSONWriter.Feature.FieldBased.mask;
                    path.setWriterContext(writeContext);
                }

                fieldValue = path.eval(root);
                previous = fieldValue;
            }

            Object resolvedName = resolveTask.name;
            Object resolvedObject = resolveTask.object;

            if (resolvedName != null) {
                if (resolvedObject instanceof Map) {
                    Map map = (Map) resolvedObject;
                    if (resolvedName instanceof ReferenceKey) {
                        if (map instanceof LinkedHashMap) {
                            int size = map.size();
                            if (size == 0) {
                                continue;
                            }

                            Object[] keys = new Object[size];
                            Object[] values = new Object[size];

                            int index = 0;
                            for (Object o : map.entrySet()) {
                                Map.Entry entry = (Map.Entry) o;
                                Object entryKey = entry.getKey();
                                if (resolvedName == entryKey) {
                                    keys[index] = fieldValue;
                                } else {
                                    keys[index] = entryKey;
                                }
                                values[index++] = entry.getValue();
                            }
                            map.clear();

                            for (int j = 0; j < keys.length; j++) {
                                map.put(keys[j], values[j]);
                            }
                        } else {
                            map.put(fieldValue, map.remove(resolvedName));
                        }
                    } else {
                        map.put(resolvedName, fieldValue);
                    }
                    continue;
                }

                if (resolvedName instanceof Integer) {
                    if (resolvedObject instanceof List) {
                        int index = (Integer) resolvedName;
                        List list = (List) resolvedObject;
                        if (index == list.size()) {
                            list.add(fieldValue);
                        } else {
                            if (index < list.size() && list.get(index) == null) {
                                list.set(index, fieldValue);
                            } else {
                                list.add(index, fieldValue);
                            }
                        }
                        continue;
                    }

                    if (resolvedObject instanceof Object[]) {
                        int index = (Integer) resolvedName;
                        Object[] array = (Object[]) resolvedObject;
                        array[index] = fieldValue;
                        continue;
                    }

                    if (resolvedObject instanceof Collection) {
                        Collection collection = (Collection) resolvedObject;
                        collection.add(fieldValue);
                        continue;
                    }
                }
            }

            fieldReader.accept(resolvedObject, fieldValue);
        }
    }

    /**
     * Gets an ObjectReader for the specified type from the context's provider.
     *
     * @param type The type for which to get an ObjectReader
     * @return An ObjectReader for the specified type
     */
    public final ObjectReader getObjectReader(Type type) {
        return context.provider.getObjectReader(type, (context.features & MASK_FIELD_BASED) != 0);
    }

    /**
     * Checks if the SmartMatch feature is enabled in the context.
     *
     * @return true if SmartMatch is enabled, false otherwise
     */
    public final boolean isSupportSmartMatch() {
        return (context.features & MASK_SUPPORT_SMART_MATCH) != 0;
    }

    /**
     * Checks if the InitStringFieldAsEmpty feature is enabled in the context.
     *
     * @return true if InitStringFieldAsEmpty is enabled, false otherwise
     */
    public final boolean isInitStringFieldAsEmpty() {
        return (context.features & MASK_INIT_STRING_FIELD_AS_EMPTY) != 0;
    }

    /**
     * Checks if the SmartMatch feature is enabled, considering additional features.
     *
     * @param features Additional features to consider
     * @return true if SmartMatch is enabled with the given features, false otherwise
     */
    public final boolean isSupportSmartMatch(long features) {
        return ((context.features | features) & MASK_SUPPORT_SMART_MATCH) != 0;
    }

    /**
     * Checks if the SupportArrayToBean feature is enabled in the context.
     *
     * @return true if SupportArrayToBean is enabled, false otherwise
     */
    public final boolean isSupportBeanArray() {
        return (context.features & MASK_SUPPORT_ARRAY_TO_BEAN) != 0;
    }

    /**
     * Checks if the SupportArrayToBean feature is enabled, considering additional features.
     *
     * @param features Additional features to consider
     * @return true if SupportArrayToBean is enabled with the given features, false otherwise
     */
    public final boolean isSupportBeanArray(long features) {
        return ((context.features | features) & MASK_SUPPORT_ARRAY_TO_BEAN) != 0;
    }

    /**
     * Checks if the SupportAutoType feature is enabled, considering additional features.
     *
     * @param features Additional features to consider
     * @return true if SupportAutoType is enabled with the given features, false otherwise
     */
    public final boolean isSupportAutoType(long features) {
        return ((context.features | features) & MASK_SUPPORT_AUTO_TYPE) != 0;
    }

    /**
     * Checks if the SupportAutoType feature or handler is enabled, considering additional features.
     *
     * @param features Additional features to consider
     * @return true if SupportAutoType or a handler is enabled with the given features, false otherwise
     */
    public final boolean isSupportAutoTypeOrHandler(long features) {
        return ((context.features | features) & MASK_SUPPORT_AUTO_TYPE) != 0 || context.autoTypeBeforeHandler != null;
    }

    /**
     * Checks if this reader is using JSONB binary format.
     *
     * @return true if using JSONB format, false otherwise
     */
    public final boolean isJSONB() {
        return jsonb;
    }

    /**
     * Checks if the IgnoreNoneSerializable feature is enabled in the context.
     *
     * @return true if IgnoreNoneSerializable is enabled, false otherwise
     */
    public final boolean isIgnoreNoneSerializable() {
        return (context.features & MASK_IGNORE_NONE_SERIALIZABLE) != 0;
    }

    /**
     * Checks if there is an auto-type before handler configured in the context.
     *
     * @return true if there is an auto-type before handler, false otherwise
     */
    public boolean hasAutoTypeBeforeHandler() {
        return context.autoTypeBeforeHandler != null;
    }

    /**
     * Checks the auto type for the specified class and hash, considering additional features.
     *
     * @param expectClass The expected class
     * @param expectClassHash The expected class hash
     * @param features Additional features to consider
     * @return An ObjectReader for the auto-detected type, or null if not found
     */
    public ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        return null;
    }

    final char char1(int c) {
        byte b = CHAR1_ESCAPED[c & 0x7f];
        if (b == -1) {
            throw char1Error(c);
        }
        return (char) b;
    }

    protected JSONException char1Error(int c) {
        return new JSONException(info("unclosed.str '\\" + (char) c));
    }

    static char char2(int c1, int c2) {
        return (char) (DIGITS2[c1] * 0x10
                + DIGITS2[c2]);
    }

    /**
     * Checks if the current character is the start of a JSON object ('{') and advances the reader if it is.
     *
     * @return true if the current character is '{', false otherwise
     */
    public abstract boolean nextIfObjectStart();

    /**
     * Checks if the current value is a null or empty string and advances the reader if it is.
     *
     * @return true if the current value is null or an empty string, false otherwise
     */
    public abstract boolean nextIfNullOrEmptyString();

    /**
     * Checks if the current character is the end of a JSON object ('}') and advances the reader if it is.
     *
     * @return true if the current character is '}', false otherwise
     */
    public abstract boolean nextIfObjectEnd();

    /**
     * Consumes an array header. Text readers return {@link Integer#MAX_VALUE} because
     * the element count is not encoded; JSONB readers return the encoded count, or -1 for null.
     * <details><summary>中文</summary>消费数组头；文本格式不包含元素数量，因此返回 Integer.MAX_VALUE；JSONB 返回元素数量，null 返回 -1。</details>
     *
     * @return the encoded JSONB array length, -1 for JSONB null, or Integer.MAX_VALUE for text arrays
     * @throws JSONException if the current character is not the start of an array ('[')
     */
    public int startArray() {
        if (!nextIfArrayStart()) {
            throw new JSONException(info("illegal input, expect '[', but " + ch));
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Checks if the current value represents a reference.
     *
     * @return true if the current value represents a reference, false otherwise
     */
    public abstract boolean isReference();

    /**
     * Reads a reference value from JSON data.
     *
     * @return The reference value as a string
     */
    public abstract String readReference();

    /**
     * Consumes a reference token if present. A parent reference appends the list itself;
     * other references register deferred work for {@link #handleResolveTasks(Object)}.
     * <details><summary>中文</summary>存在引用时消费该标记；父引用追加列表自身，其余引用登记为延迟解析任务，稍后调用 handleResolveTasks。</details>
     *
     * @param list The list to which the reference should be added
     * @param i The index at which to add the reference in the list
     * @return true if a reference was read and added, false otherwise
     */
    public final boolean readReference(List list, int i) {
        if (!isReference()) {
            return false;
        }
        return readReference0(list, i);
    }

    /**
     * Consumes a reference token if present, appending the collection itself for a parent
     * reference or registering a deferred resolution task for other paths.
     * <details><summary>中文</summary>消费引用标记；父引用追加集合自身，其他路径登记为延迟解析任务。</details>
     *
     * @param list The collection to which the reference should be added
     * @param i The index at which to add the reference in the collection
     * @return true if a reference was read and added, false otherwise
     */
    public boolean readReference(Collection list, int i) {
        if (!isReference()) {
            return false;
        }
        return readReference0(list, i);
    }

    private boolean readReference0(Collection list, int i) {
        String path = readReference();
        if ("..".equals(path)) {
            list.add(list);
        } else {
            addResolveTask(list, i, JSONPath.of(path));
        }
        return true;
    }

    public final void addResolveTask(FieldReader fieldReader, Object object, JSONPath path) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(fieldReader, object, fieldReader.fieldName, path));
    }

    public final void addResolveTask(Map object, Object key, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        if (object instanceof LinkedHashMap) {
            object.put(key, null);
        }
        resolveTasks.add(new ResolveTask(null, object, key, reference));
    }

    public final void addResolveTask(Collection object, int i, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(null, object, i, reference));
    }

    public final void addResolveTask(Object[] object, int i, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(null, object, i, reference));
    }

    /**
     * Checks if the current character represents the start of a JSON array.
     *
     * @return true if the current character is '[', false otherwise
     */
    public boolean isArray() {
        return this.ch == '[';
    }

    /**
     * Checks if the current character represents the start of a JSON object.
     *
     * @return true if the current character is '{', false otherwise
     */
    public boolean isObject() {
        return this.ch == '{';
    }

    /**
     * Checks if the current character represents the start of a JSON number.
     *
     * @return true if the current character is a digit or sign, false otherwise
     */
    public boolean isNumber() {
        switch (this.ch) {
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the current character represents the start of a JSON string.
     *
     * @return true if the current character is a quote, false otherwise
     */
    public boolean isString() {
        return this.ch == '"' || this.ch == '\'';
    }

    /**
     * Checks if the current character represents the start of a JSON boolean.
     *
     * @return true if the current character is 't' (true) or 'f' (false), false otherwise
     */
    public boolean isBool() {
        return this.ch == 't' || this.ch == 'f';
    }

    /**
     * Advances the reader to the end of the current JSON array.
     */
    public void endArray() {
        next();
    }

    /**
     * Checks if the current character matches the specified character and advances the reader if it does.
     *
     * @param ch The character to match
     * @return true if the current character matches the specified character, false otherwise
     */
    public abstract boolean nextIfMatch(char ch);

    /**
     * Checks if the current character is a comma (',') and advances the reader if it is.
     *
     * @return true if the current character is a comma, false otherwise
     */
    public abstract boolean nextIfComma();

    /**
     * Checks if the current character is the start of a JSON array ('[') and advances the reader if it is.
     *
     * @return true if the current character is '[', false otherwise
     */
    public abstract boolean nextIfArrayStart();

    /**
     * Checks if the current character is the end of a JSON array (']') and advances the reader if it is.
     *
     * @return true if the current character is ']', false otherwise
     */
    public abstract boolean nextIfArrayEnd();

    /**
     * Checks if the current value represents a Set and advances the reader if it is.
     * This method is used to detect Set-type collections during JSON parsing.
     *
     * @return true if the current value represents a Set, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfSet();

    /**
     * Checks if the current value represents infinity and advances the reader if it is.
     * This method is used to detect infinity values during JSON parsing.
     *
     * @return true if the current value represents infinity, false otherwise
     * @since 2.0.51
     */
    public abstract boolean nextIfInfinity();

    /**
     * Reads a pattern string from the JSON data.
     * This method is used to read regular expression patterns or other pattern strings.
     *
     * @return The pattern string read from the JSON data
     * @since 2.0.51
     */
    public abstract String readPattern();

    /**
     * Returns the current cursor offset in bytes for byte-backed readers or UTF-16 code units
     * for character-backed readers. It is a parser cursor, not an element or field index.
     * <details><summary>中文</summary>返回当前游标偏移量：字节读取器使用字节，字符读取器使用 UTF-16 代码单元；它不是元素或字段索引。</details>
     *
     * @return the current input cursor offset
     */
    public final int getOffset() {
        return offset;
    }

    /**
     * Advances the reader to the next character in the JSON data.
     */
    public abstract void next();

    /**
     * Advances the reader to the next character in the JSON data, skipping comment processing.
     */
    public void nextWithoutComment() {
        next();
    }

    /**
     * Reads the hash code of the current JSON value.
     *
     * @return The hash code of the current value
     */
    public abstract long readValueHashCode();

    /**
     * Reads the hash code of the current type in the JSON data.
     *
     * @return The hash code of the current type
     */
    public long readTypeHashCode() {
        return readValueHashCode();
    }

    /**
     * Reads the hash code of the current field name in a JSON object.
     *
     * @return The hash code of the current field name
     */
    public abstract long readFieldNameHashCode();

    public long readFieldNameHashCode(int keySie, int min, int max) {
        return readFieldNameHashCode();
    }

    public long readFieldNameHashCodeE(int size0, int size1, int size3) {
        return readFieldNameHashCode();
    }

    /**
     * Reads the hash code of the current field name in lowercase.
     *
     * @return The hash code of the current field name in lowercase
     */
    public abstract long getNameHashCodeLCase();

    /**
     * Reads the current field name in a JSON object.
     *
     * @return The current field name
     */
    public abstract String readFieldName();

    /**
     * Gets the current field name in a JSON object.
     *
     * @return The current field name
     */
    public abstract String getFieldName();

    /**
     * Sets the type redirect flag for this reader.
     *
     * @param typeRedirect true to enable type redirection, false to disable
     */
    public final void setTypeRedirect(boolean typeRedirect) {
        this.typeRedirect = typeRedirect;
    }

    /**
     * Checks if type redirection is enabled for this reader.
     *
     * @return true if type redirection is enabled, false otherwise
     */
    public final boolean isTypeRedirect() {
        return typeRedirect;
    }

    /**
     * Reads the hash code of the current field name in a JSON object without requiring quotes.
     *
     * @return The hash code of the current field name
     */
    public abstract long readFieldNameHashCodeUnquote();

    /**
     * Reads the current field name in a JSON object without requiring quotes.
     *
     * @return The current field name
     * @throws JSONException if the field name is null or empty
     */
    public final String readFieldNameUnquote() {
        if (ch == '/') {
            skipComment();
        }
        readFieldNameHashCodeUnquote();
        String name = getFieldName();
        if (name == null || name.isEmpty()) {
            throw new JSONException(info("illegal input"));
        }
        return name;
    }

    /**
     * Skips the current field name in a JSON object.
     *
     * @return true if the field name was successfully skipped, false otherwise
     */
    public abstract boolean skipName();

    /**
     * Skips the current JSON value, advancing the reader to the next value or end of the current structure.
     */
    public abstract void skipValue();

    /**
     * Checks if the current value represents binary data.
     *
     * @return true if the current value is binary data, false otherwise
     */
    public boolean isBinary() {
        return false;
    }

    /**
     * Reads a hexadecimal string from JSON data and converts it to a byte array.
     *
     * @return The byte array representation of the hexadecimal string
     */
    public abstract byte[] readHex();

    /**
     * Reads binary data from JSON data.
     *
     * @return The byte array representation of the binary data
     * @throws JSONException if there is an error parsing the binary data
     */
    public byte[] readBinary() {
        if (ch == 'x') {
            return readHex();
        }

        if (isString()) {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }

            if ((context.features & Feature.Base64StringAsByteArray.mask) != 0) {
                return Base64.getDecoder().decode(str);
            }

            throw new JSONException(info("not support input " + str));
        }

        if (nextIfArrayStart()) {
            int index = 0;
            byte[] bytes = new byte[64];
            while (true) {
                if (ch == ']') {
                    next();
                    break;
                }
                if (index == bytes.length) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }
                bytes[index++] = (byte) readInt32Value();
            }
            nextIfComma();
            return Arrays.copyOf(bytes, index);
        }

        throw new JSONException(info("not support read binary"));
    }

    /**
     * Reads a 32-bit integer value from JSON data.
     *
     * @return The 32-bit integer value
     */
    public abstract int readInt32Value();

    /**
     * Reads an array of 32-bit integer values from JSON data.
     *
     * @return The array of 32-bit integer values
     * @throws JSONException if there is an error parsing the JSON
     */
    public int[] readInt32ValueArray() {
        if (nextIfNull()) {
            return null;
        }

        if (nextIfArrayStart()) {
            int[] values = new int[8];
            int size = 0;
            while (!nextIfArrayEnd()) {
                if (isEnd()) {
                    throw new JSONException(info("input end"));
                }

                if (size == values.length) {
                    values = Arrays.copyOf(values, values.length << 1);
                }

                values[size++] = readInt32Value();
            }
            nextIfComma();

            int[] array;
            if (size == values.length) {
                array = values;
            } else {
                array = Arrays.copyOf(values, size);
            }
            return array;
        }

        if (isString()) {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }

            throw new JSONException(info("not support input " + str));
        }

        throw new JSONException(info("TODO"));
    }

    /**
     * Checks if the next value matches the specified type and advances the reader if it does.
     *
     * @param type The type to match
     * @return true if the next value matches the specified type, false otherwise
     * @throws JSONException as this operation is not supported
     */
    public boolean nextIfMatch(byte type) {
        throw new JSONException("UnsupportedOperation");
    }

    /**
     * Checks if the next value matches any type and advances the reader if it does.
     *
     * @return true if the next value matches any type, false otherwise
     * @throws JSONException as this operation is not supported
     */
    public boolean nextIfMatchTypedAny() {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract boolean nextIfMatchIdent(char c0, char c1);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5);

    /**
     * Reads a Byte object from JSON data.
     *
     * @return The Byte object, or null if the value is null in JSON
     */
    public final Byte readInt8() {
        Integer i = readInt32();
        if (i == null) {
            return null;
        }
        return i.byteValue();
    }

    /**
     * Reads a byte value from JSON data.
     *
     * @return The byte value
     */
    public byte readInt8Value() {
        int i = readInt32Value();
        return (byte) i;
    }

    /**
     * Reads a Short object from JSON data.
     *
     * @return The Short object, or null if the value is null in JSON
     */
    public final Short readInt16() {
        Integer i = readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }

    /**
     * Reads a short value from JSON data.
     *
     * @return The short value
     */
    public short readInt16Value() {
        int i = readInt32Value();
        return (short) i;
    }

    /**
     * Reads an Integer object from JSON data.
     *
     * @return The Integer object, or null if the value is null in JSON
     */
    public abstract Integer readInt32();

    /**
     * Reads a 32-bit integer value from JSON data, handling overflow conditions.
     *
     * @return The 32-bit integer value
     */
    protected final int readInt32ValueOverflow() {
        readNumber0();
        return getInt32Value();
    }

    /**
     * Reads a 64-bit long value from JSON data, handling overflow conditions.
     *
     * @return The 64-bit long value
     */
    protected final long readInt64ValueOverflow() {
        readNumber0();
        return getInt64Value();
    }

    /**
     * Converts the cached numeric token to an int without consuming another input value.
     * Use {@link #readInt32Value()} to read from the input.
     * <details><summary>中文</summary>将已缓存数字转换为 int，不读取下一个输入值；读取输入请使用 readInt32Value。</details>
     *
     * @return the cached numeric value as an int
     */
    public final int getInt32Value() {
        switch (valueType) {
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if (mag0 == 0 && mag1 == 0 && mag2 == 0) {
                    if (negative) {
                        if (mag3 == Integer.MIN_VALUE) {
                            return mag3;
                        }
                        if (mag3 >= 0) {
                            return -mag3;
                        }
                    } else {
                        if (mag3 >= 0) {
                            return mag3;
                        }
                    }
                }
                Number number = getNumber();
                if (number instanceof Long) {
                    long longValue = number.longValue();
                    if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                        throw new JSONException(info("integer overflow " + longValue));
                    }
                    return (int) longValue;
                }
                if (number instanceof BigInteger) {
                    BigInteger bigInt = (BigInteger) number;
                    if ((context.features & Feature.NonErrorOnNumberOverflow.mask) != 0) {
                        return bigInt.intValue();
                    }
                    try {
                        return bigInt.intValueExact();
                    } catch (ArithmeticException e) {
                        throw numberError();
                    }
                }
                return number.intValue();
            case JSON_TYPE_DEC:
                return getNumber().intValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("int value not support input null"));
                }
                return 0;
            case JSON_TYPE_STRING: {
                return toInt32(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                Number num = toNumber((Map) complex);
                if (num != null) {
                    return num.intValue();
                }
                return 0;
            }
            case JSON_TYPE_INT64:
            case JSON_TYPE_FLOAT:
            case JSON_TYPE_DOUBLE: {
                Number num = getNumber();
                long int64 = num.longValue();
                if ((int64 < Integer.MIN_VALUE || int64 > Integer.MAX_VALUE)
                        && (context.features & Feature.NonErrorOnNumberOverflow.mask) == 0
                ) {
                    throw new JSONException(info("integer overflow " + int64));
                }
                return (int) int64;
                //if ((context.features & Feature.NonErrorOnNumberOverflow.mask) != 0) {
            }
            case JSON_TYPE_BIG_DEC:
                try {
                    return getBigDecimal()
                            .intValueExact();
                } catch (ArithmeticException e) {
                    throw numberError();
                }
            case JSON_TYPE_ARRAY: {
                return toInt((List) complex);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    /**
     * Converts the cached numeric token to a long without consuming another input value.
     * Use {@link #readInt64Value()} to read from the input.
     * <details><summary>中文</summary>将已缓存的数字标记转换为 long，不读取下一个输入值；读取输入请使用 readInt64Value。</details>
     *
     * @return the cached numeric value as a long
     */
    public final long getInt64Value() {
        switch (valueType) {
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if (mag0 == 0 && mag1 == 0 && mag2 == 0) {
                    if (negative) {
                        if (mag3 == Integer.MIN_VALUE) {
                            return mag3;
                        }
                        if (mag3 >= 0) {
                            return -mag3;
                        }
                    } else {
                        if (mag3 >= 0) {
                            return mag3;
                        }
                    }
                }
                Number number = getNumber();
                if (number instanceof BigInteger) {
                    BigInteger bigInt = (BigInteger) number;
                    if ((context.features & Feature.NonErrorOnNumberOverflow.mask) != 0) {
                        return bigInt.longValue();
                    }
                    try {
                        return bigInt.longValueExact();
                    } catch (ArithmeticException e) {
                        throw numberError();
                    }
                }
                return number.longValue();
            case JSON_TYPE_DEC:
            case JSON_TYPE_INT64:
            case JSON_TYPE_FLOAT:
            case JSON_TYPE_DOUBLE:
                return getNumber().longValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                return 0;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                return toLong((Map) complex);
            }
            case JSON_TYPE_ARRAY: {
                Number value = toNumber((List) complex);
                if (value != null) {
                    return value.longValue();
                }
                throw error("parseLong error, value : " + complex);
            }
            case JSON_TYPE_BIG_DEC:
                try {
                    return getBigDecimal()
                            .longValueExact();
                } catch (ArithmeticException e) {
                    throw numberError();
                }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    /**
     * Converts the cached numeric token to a double without advancing the input.
     * <details><summary>中文</summary>将已缓存的数字标记转换为 double，不推进输入位置。</details>
     *
     * @return the cached numeric value as a double
     */
    public final double getDoubleValue() {
        switch (valueType) {
            case JSON_TYPE_NaN:
                return Double.NaN;
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if ((mag0 | mag1 | mag2) == 0 && mag3 >= 0) {
                    return negative ? -mag3 : mag3;
                }
                // Fall through to the full-magnitude conversion for integers outside the fast range.
                // <details><summary>中文</summary>超出快速范围的整数继续使用完整精度转换。</details>
            case JSON_TYPE_DEC:
            case JSON_TYPE_INT64:
            case JSON_TYPE_FLOAT:
            case JSON_TYPE_DOUBLE:
                return getNumber().doubleValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                return 0;
            case JSON_TYPE_STRING: {
                try {
                    return toDoubleValue(stringValue);
                } catch (NumberFormatException e) {
                    throw new JSONException(info(e.getMessage()));
                }
            }
            case JSON_TYPE_OBJECT: {
                Map map = (Map) complex;
                if (map == null || map.isEmpty()) {
                    wasNull = true;
                    return 0;
                }
                return toDoubleValue(map);
            }
            case JSON_TYPE_ARRAY: {
                Collection list = (Collection) complex;
                if (list == null || list.isEmpty()) {
                    wasNull = true;
                    return 0;
                }
                return toDoubleValue(complex);
            }
            case JSON_TYPE_BIG_DEC:
                try {
                    return getBigDecimal()
                            .doubleValue();
                } catch (ArithmeticException e) {
                    throw numberError();
                }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    /**
     * Converts the cached numeric token to a float without advancing the input.
     * <details><summary>中文</summary>将已缓存的数字标记转换为 float，不推进输入位置。</details>
     *
     * @return the cached numeric value as a float
     */
    public final float getFloatValue() {
        return (float) getDoubleValue();
    }

    /**
     * Reads an array of 64-bit long values from JSON data.
     *
     * @return The array of 64-bit long values
     * @throws JSONException if there is an error parsing the JSON
     */
    public long[] readInt64ValueArray() {
        if (nextIfNull()) {
            return null;
        }

        if (nextIfArrayStart()) {
            long[] values = new long[8];
            int size = 0;
            while (!nextIfArrayEnd()) {
                if (isEnd()) {
                    throw new JSONException(info("input end"));
                }

                if (size == values.length) {
                    values = Arrays.copyOf(values, values.length << 1);
                }

                values[size++] = readInt64Value();
            }
            return size == values.length ? values : Arrays.copyOf(values, size);
        }

        if (isString()) {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }

            throw error("not support input ".concat(str));
        }

        throw new JSONException(info("TODO"));
    }

    /**
     * Reads a 64-bit long value from JSON data.
     *
     * @return The 64-bit long value
     */
    public abstract long readInt64Value();

    /**
     * Reads a Long object from JSON data.
     *
     * @return The Long object, or null if the value is null in JSON
     */
    public abstract Long readInt64();

    /**
     * Reads a float value from JSON data.
     *
     * @return The float value
     */
    public abstract float readFloatValue();

    /**
     * Reads a Float object from JSON data.
     *
     * @return The Float object, or null if the value is null in JSON
     */
    public Float readFloat() {
        if (nextIfNull()) {
            return null;
        }

        wasNull = false;
        float value = readFloatValue();
        if (wasNull) {
            return null;
        }
        return value;
    }

    /**
     * Reads a double value from JSON data.
     *
     * @return The double value
     */
    public abstract double readDoubleValue();

    /**
     * Reads a Double object from JSON data.
     *
     * @return The Double object, or null if the value is null in JSON
     */
    public final Double readDouble() {
        if (nextIfNull()) {
            return null;
        }

        wasNull = false;
        double value = readDoubleValue();
        if (wasNull) {
            return null;
        }
        return value;
    }

    /**
     * Reads a Number value from JSON data.
     *
     * @return The Number value
     */
    public Number readNumber() {
        readNumber0();
        return getNumber();
    }

    /**
     * Reads a BigInteger value from JSON data.
     *
     * @return The BigInteger value
     */
    public BigInteger readBigInteger() {
        readNumber0();
        return getBigInteger();
    }

    /**
     * Reads a BigDecimal value from JSON data.
     *
     * @return The BigDecimal value
     */
    public abstract BigDecimal readBigDecimal();

    /**
     * Reads a UUID value from JSON data.
     *
     * @return The UUID value
     */
    public abstract UUID readUUID();

    /**
     * Reads a LocalDate value from JSON data.
     *
     * @return The LocalDate value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the date
     */
    public LocalDate readLocalDate() {
        if (nextIfNull()) {
            return null;
        }

        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDate();
        }

        if (context.dateFormat == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            int len = getStringLength();
            LocalDateTime ldt = null;
            LocalDate localDate;
            switch (len) {
                case 8:
                    localDate = readLocalDate8();
                    ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                    break;
                case 9:
                    localDate = readLocalDate9();
                    ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                    break;
                case 10:
                    localDate = readLocalDate10();
                    ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                    break;
                case 11:
                    localDate = readLocalDate11();
                    ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                    break;
                case 19:
                    ldt = readLocalDateTime19();
                    break;
                case 20:
                    ldt = readLocalDateTime20();
                    break;
                default:
                    if (len > 20) {
                        ldt = readLocalDateTimeX(len);
                    }
                    break;
            }
            if (ldt != null) {
                return ldt.toLocalDate();
            }
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            if (context.formatHasHour) {
                return LocalDateTime
                        .parse(str, formatter)
                        .toLocalDate();
            }
            return LocalDate.parse(str, formatter);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDate();
        }

        throw new JSONException("not support input : " + str);
    }

    /**
     * Reads a LocalDateTime value from JSON data.
     *
     * @return The LocalDateTime value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the date/time
     */
    public LocalDateTime readLocalDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDateTime();
        }

        if (isTypeRedirect() && nextIfMatchIdent('"', 'v', 'a', 'l', '"')) {
            nextIfMatch(':');
            LocalDateTime dateTime = readLocalDateTime();
            nextIfObjectEnd();
            setTypeRedirect(false);
            return dateTime;
        }

        if (context.dateFormat == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            // The raw prefix includes the quote; recognize both native byte orders before fixed-width date parsing.
            // <details><summary>中文</summary>原始前缀包含引号；在定长日期解析前识别两种字节序的 .NET 日期前缀。</details>
            int prefix = getRawInt();
            boolean dotnetDate = (prefix & 0xFFFFFF00) == 0x61442F00 || (prefix & 0x00FFFFFF) == 0x002F4461;
            int len = dotnetDate ? 0 : getStringLength();
            LocalDate localDate;
            switch (len) {
                case 8:
                    localDate = readLocalDate8();
                    return localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                case 9:
                    localDate = readLocalDate9();
                    return localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                case 10:
                    localDate = readLocalDate10();
                    return localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                case 11:
                    localDate = readLocalDate11();
                    return localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                case 16:
                    return readLocalDateTime16();
                case 17: {
                    LocalDateTime ldt = readLocalDateTime17();
                    if (ldt != null) {
                        return ldt;
                    }
                    break;
                }
                case 18: {
                    LocalDateTime ldt = readLocalDateTime18();
                    if (ldt != null) {
                        return ldt;
                    }
                    break;
                }
                case 19: {
                    LocalDateTime ldt = readLocalDateTime19();
                    if (ldt != null) {
                        return ldt;
                    }
                    break;
                }
                case 20: {
                    LocalDateTime ldt = readLocalDateTime20();
                    if (ldt != null) {
                        return ldt;
                    }
                    ZonedDateTime zdt = readZonedDateTimeX(len);
                    if (zdt != null) {
                        return zdt.toLocalDateTime();
                    }
                    break;
                }
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                    LocalDateTime ldt = readLocalDateTimeX(len);
                    if (ldt != null) {
                        return ldt;
                    }
                    ZonedDateTime zdt = readZonedDateTimeX(len);
                    if (zdt != null) {
                        ZoneId contextZoneId = context.getZoneId();
                        if (!zdt.getZone().equals(contextZoneId)) {
                            ldt = zdt.toInstant().atZone(contextZoneId).toLocalDateTime();
                        } else {
                            ldt = zdt.toLocalDateTime();
                        }
                        return ldt;
                    }
                    break;
                default:
                    break;
            }
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            wasNull = true;
            return null;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            if (!context.formatHasHour) {
                return LocalDateTime.of(
                        LocalDate.parse(str, formatter),
                        LocalTime.MIN
                );
            }
            return LocalDateTime.parse(str, formatter);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);

            if (context.formatUnixTime) {
                millis *= 1000L;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        if (str.startsWith("/Date(") && str.endsWith(")/")) {
            String dotnetDateStr = str.substring(6, str.length() - 2);
            int i = dotnetDateStr.indexOf('+');
            if (i == -1) {
                i = dotnetDateStr.indexOf('-', 1);
            }
            if (i != -1) {
                dotnetDateStr = dotnetDateStr.substring(0, i);
            }
            long millis = Long.parseLong(dotnetDateStr);
            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        if ("0000-00-00 00:00:00".equals(str)) {
            wasNull = true;
            return null;
        }
        throw new JSONException(info("read LocalDateTime error " + str));
    }

    public abstract OffsetDateTime readOffsetDateTime();

    /**
     * Reads a ZonedDateTime value from JSON data.
     *
     * @return The ZonedDateTime value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the date/time
     */
    public ZonedDateTime readZonedDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return instant.atZone(context.getZoneId());
        }

        if (isString()) {
            if (context.dateFormat == null
                    || context.formatyyyyMMddhhmmss19
                    || context.formatyyyyMMddhhmmssT19
                    || context.formatyyyyMMdd8
                    || context.formatISO8601) {
                int len = getStringLength();
                LocalDateTime ldt = null;
                LocalDate localDate;
                switch (len) {
                    case 8:
                        localDate = readLocalDate8();
                        ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                        break;
                    case 9:
                        localDate = readLocalDate9();
                        ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                        break;
                    case 10:
                        localDate = readLocalDate10();
                        ldt = localDate == null ? null : LocalDateTime.of(localDate, LocalTime.MIN);
                        break;
                    case 11:
                        localDate = readLocalDate11();
                        ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                        break;
                    case 16:
                        ldt = readLocalDateTime16();
                        break;
                    case 17:
                        ldt = readLocalDateTime17();
                        break;
                    case 18:
                        ldt = readLocalDateTime18();
                        break;
                    case 19:
                        ldt = readLocalDateTime19();
                        break;
                    case 20: {
                        ldt = readLocalDateTime20();
                        break;
                    }
                    default:
                        ZonedDateTime zdt = readZonedDateTimeX(len);
                        if (zdt != null) {
                            return zdt;
                        }
                        break;
                }
                if (ldt != null) {
                    return ZonedDateTime.ofLocal(
                            ldt,
                            context.getZoneId(),
                            null
                    );
                }
            }

            String str = readString();
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            DateTimeFormatter formatter = context.getDateFormatter();
            if (formatter != null) {
                if (!context.formatHasHour) {
                    LocalDate localDate = LocalDate.parse(str, formatter);
                    return ZonedDateTime.of(localDate, LocalTime.MIN, context.getZoneId());
                }
                LocalDateTime localDateTime = LocalDateTime.parse(str, formatter);
                return ZonedDateTime.of(localDateTime, context.getZoneId());
            }

            if (IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (context.formatUnixTime) {
                    millis *= 1000L;
                }
                Instant instant = Instant.ofEpochMilli(millis);
                return instant.atZone(context.getZoneId());
            }

            return ZonedDateTime.parse(str);
        }

        if (nextIfNull()) {
            return null;
        }
        throw new JSONException("TODO : " + ch);
    }

    public abstract OffsetTime readOffsetTime();

    /**
     * Reads a Calendar value from JSON data.
     *
     * @return The Calendar value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the date
     */
    public Calendar readCalendar() {
        if (isString()) {
            long millis = readMillisFromString();
            if (millis == 0 && wasNull) {
                return null;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            return calendar;
        }

        if (readIfNull()) {
            return null;
        }

        long millis = readInt64Value();
        if (context.formatUnixTime) {
            millis *= 1000;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    /**
     * Reads a Date value from JSON data.
     *
     * @return The Date value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the date
     */
    public Date readDate() {
        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            return new Date(millis);
        }

        if (readIfNull()) {
            return null;
        }

        if (nextIfNullOrEmptyString()) {
            return null;
        }

        if (current() == 'n') {
            return readNullOrNewDate();
        }

        long millis;
        if (isTypeRedirect() && nextIfMatchIdent('"', 'v', 'a', 'l', '"')) {
            nextIfMatch(':');
            millis = readInt64Value();
            nextIfObjectEnd();
            setTypeRedirect(false);
        } else if (isObject()) {
            JSONObject object = readJSONObject();
            Object date = object.get("$date");
            if (date instanceof String) {
                return DateUtils.parseDate((String) date, context.getZoneId());
            }
            return toDate(object);
        } else {
            millis = readMillisFromString();
        }

        if (millis == 0 && wasNull) {
            return null;
        }

        return new Date(millis);
    }

    /**
     * Reads a LocalTime value from JSON data.
     *
     * @return The LocalTime value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the time
     */
    public LocalTime readLocalTime() {
        if (nextIfNull()) {
            return null;
        }

        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        int len = getStringLength();
        switch (len) {
            case 5:
                return readLocalTime5();
            case 6:
                return readLocalTime6();
            case 7:
                return readLocalTime7();
            case 8:
                return readLocalTime8();
            case 9:
                return readLocalTime9();
            case 10:
                return readLocalTime10();
            case 11:
                return readLocalTime11();
            case 12:
                return readLocalTime12();
            case 15:
                return readLocalTime15();
            case 18:
                return readLocalTime18();
            case 19:
                return readLocalDateTime19()
                        .toLocalTime();
            case 20:
                return readLocalDateTime20()
                        .toLocalTime();
            default:
                break;
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        throw new JSONException("not support len : " + str);
    }

    /**
     * Gets the length of the current string value in the JSON data.
     *
     * @return The length of the current string value
     */
    protected abstract int getStringLength();

    /**
     * Checks if the current value represents a date.
     *
     * @return true if the current value represents a date, false otherwise
     */
    public boolean isDate() {
        return false;
    }

    /**
     * Reads an Instant value from JSON data.
     *
     * @return The Instant value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the instant
     */
    public Instant readInstant() {
        if (nextIfNull()) {
            return null;
        }

        if (isNumber()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            return Instant.ofEpochMilli(millis);
        }

        if (isObject()) {
            return (Instant) getObjectReader(Instant.class)
                    .createInstance(
                            readObject(),
                            0L
                    );
        }

        ZonedDateTime zdt = readZonedDateTime();
        if (zdt == null) {
            return null;
        }

        return Instant.ofEpochSecond(
                zdt.toEpochSecond(),
                zdt.toLocalTime().getNano());
    }

    /**
     * Reads milliseconds from a string value in JSON data.
     *
     * @return The milliseconds value
     * @throws JSONException if there is an error parsing the string
     */
    public final long readMillisFromString() {
        wasNull = false;
        String format = context.dateFormat;
        if (format == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            // The raw prefix includes the quote; recognize both native byte orders before fixed-width date parsing.
            // <details><summary>中文</summary>原始前缀包含引号；在定长日期解析前识别两种字节序的 .NET 日期前缀。</details>
            int prefix = getRawInt();
            boolean dotnetDate = (prefix & 0xFFFFFF00) == 0x61442F00 || (prefix & 0x00FFFFFF) == 0x002F4461;
            int len = dotnetDate ? 0 : getStringLength();
            LocalDateTime ldt = null;
            LocalDate localDate;
            switch (len) {
                case 8: {
                    localDate = readLocalDate8();
                    if (localDate == null) {
                        throw new JSONException("TODO : " + readString());
                    }
                    ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                    break;
                }
                case 9: {
                    localDate = readLocalDate9();
                    if (localDate != null) {
                        ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                    }
                    break;
                }
                case 10: {
                    localDate = readLocalDate10();
                    if (localDate == null) {
                        String str = readString();
                        if ("0000-00-00".equals(str)) {
                            wasNull = true;
                            return 0;
                        }
                        if (IOUtils.isNumber(str)) {
                            return Long.parseLong(str);
                        }
                        throw new JSONException("TODO : " + str);
                    } else {
                        ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                    }
                    break;
                }
                case 11: {
                    localDate = readLocalDate11();
                    if (localDate != null) {
                        ldt = LocalDateTime.of(localDate, LocalTime.MIN);
                    }
                    break;
                }
                case 12: {
                    ldt = readLocalDateTime12();
                    break;
                }
                case 14: {
                    ldt = readLocalDateTime14();
                    break;
                }
                case 16: {
                    ldt = readLocalDateTime16();
                    break;
                }
                case 17: {
                    ldt = readLocalDateTime17();
                    break;
                }
                case 18: {
                    ldt = readLocalDateTime18();
                    break;
                }
                case 19: {
                    long millis = readMillis19();
                    if (millis != 0 || !wasNull) {
                        return millis;
                    }

                    ldt = readLocalDateTime19();
                    break;
                }
                case 20: {
                    ldt = readLocalDateTime20();
                    break;
                }
                default:
                    break;
            }

            ZonedDateTime zdt = null;
            if (ldt != null) {
                zdt = ZonedDateTime.ofLocal(ldt, context.getZoneId(), null);
            } else if (len >= 20) {
                zdt = readZonedDateTimeX(len);
                if (zdt == null && (len >= 32 && len <= 35)) {
                    String str = readString();
                    zdt = DateUtils.parseZonedDateTime(str, null);
                }
            }

            if (zdt != null) {
                long seconds = zdt.toEpochSecond();
                int nanos = zdt.toLocalTime().getNano();
                if (seconds < 0 && nanos > 0) {
                    long millis = (seconds + 1) * 1000;
                    long adjustment = nanos / 1000_000 - 1000;
                    return millis + adjustment;
                } else {
                    long millis = seconds * 1000L;
                    return millis + nanos / 1000_000;
                }
            }
        }

        String str = readString();

        if (str.isEmpty() || "null".equals(str)) {
            wasNull = true;
            return 0;
        }

        if (context.formatMillis || context.formatUnixTime) {
            long millis = Long.parseLong(str);
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            return millis;
        }

        if (format != null && !format.isEmpty()) {
            if ("yyyy-MM-dd HH:mm:ss".equals(format)) {
                if ((str.length() < 4 || str.charAt(4) != '-') && IOUtils.isNumber(str)) {
                    return Long.parseLong(str);
                }

                return DateUtils.parseMillis19(str, context.getZoneId());
            }

            if ("yyyy-MM-dd HH:mm:ss.SSS".equals(format)
                    && str.length() == 19
                    && str.charAt(4) == '-'
                    && str.charAt(7) == '-'
                    && str.charAt(10) == ' '
                    && str.charAt(13) == ':'
                    && str.charAt(16) == ':'
            ) {
                return DateUtils.parseMillis19(str, context.getZoneId());
            }

            SimpleDateFormat utilFormat = new SimpleDateFormat(format);
            try {
                return utilFormat
                        .parse(str)
                        .getTime();
            } catch (ParseException e) {
                throw new JSONException("parse date error, " + str + ", expect format " + utilFormat.toPattern());
            }
        }
        if ("0000-00-00T00:00:00".equals(str)
                || "0001-01-01T00:00:00+08:00".equals(str)) {
            return 0;
        }

        if (str.startsWith("/Date(") && str.endsWith(")/")) {
            String dotnetDateStr = str.substring(6, str.length() - 2);
            int i = dotnetDateStr.indexOf('+');
            if (i == -1) {
                i = dotnetDateStr.indexOf('-', 1);
            }
            if (i != -1) {
                dotnetDateStr = dotnetDateStr.substring(0, i);
            }
            return Long.parseLong(dotnetDateStr);
        } else if (IOUtils.isNumber(str)) {
            return Long.parseLong(str);
        }

        throw new JSONException(info("format " + format + " not support, input " + str));
    }

    protected abstract LocalDateTime readLocalDateTime12();

    protected abstract LocalDateTime readLocalDateTime14();

    protected abstract LocalDateTime readLocalDateTime16();

    protected abstract LocalDateTime readLocalDateTime17();

    protected abstract LocalDateTime readLocalDateTime18();

    protected abstract LocalDateTime readLocalDateTime19();

    protected abstract LocalDateTime readLocalDateTime20();

    /**
     * Reads milliseconds from a 19-character date string in JSON data.
     *
     * @return The milliseconds value
     */
    public abstract long readMillis19();

    protected abstract LocalDateTime readLocalDateTimeX(int len);

    protected abstract LocalTime readLocalTime5();

    protected abstract LocalTime readLocalTime6();

    protected abstract LocalTime readLocalTime7();

    protected abstract LocalTime readLocalTime8();

    protected abstract LocalTime readLocalTime9();

    protected abstract LocalTime readLocalTime10();

    protected abstract LocalTime readLocalTime11();

    protected abstract LocalTime readLocalTime12();

    protected abstract LocalTime readLocalTime15();

    protected abstract LocalTime readLocalTime18();

    protected abstract LocalDate readLocalDate8();

    protected abstract LocalDate readLocalDate9();

    protected abstract LocalDate readLocalDate10();

    protected abstract LocalDate readLocalDate11();

    protected abstract ZonedDateTime readZonedDateTimeX(int len);

    /**
     * Reads a number value from JSON data and passes it to a consumer.
     *
     * @param consumer The consumer to accept the number value
     * @param quoted Whether the number should be quoted in the output
     */
    public void readNumber(ValueConsumer consumer, boolean quoted) {
        readNumber0();
        Number number = getNumber();
        consumer.accept(number);
    }

    /**
     * Reads a string value from JSON data and passes it to a consumer.
     *
     * @param consumer The consumer to accept the string value
     * @param quoted Whether the string should be quoted in the output
     */
    public void readString(ValueConsumer consumer, boolean quoted) {
        String str = readString(); //
        if (quoted) {
            consumer.accept(JSON.toJSONString(str));
        } else {
            consumer.accept(str);
        }
    }

    /**
     * Reads a string value from JSON data.
     *
     * @return The string value
     */
    public abstract String readString();

    /**
     * Reads a number value from JSON data and stores it in internal fields.
     * This is a low-level method used by other number reading methods.
     */
    protected abstract void readNumber0();

    /**
     * Decodes a Base64 string, including a supported {@code data:image/...;base64,} prefix.
     * JSON null produces null, and an empty string produces an empty byte array.
     * <details><summary>中文</summary>解码 Base64 字符串，也支持带 data:image/...;base64, 前缀的数据；null 返回 null，空字符串返回空数组。</details>
     *
     * @return The decoded byte array
     * @throws IllegalArgumentException if the encoded content is not valid Base64
     */
    public byte[] readBase64() {
        String str = readString();
        if (str == null) {
            return null;
        }
        if (str != null) {
            String prefix = "data:image/";
            int p0, p1;
            String base64 = "base64";
            if (str.startsWith(prefix)
                    && (p0 = str.indexOf(';', prefix.length() + 1)) != -1
                    && (p1 = str.indexOf(',', p0 + 1)) != -1 && str.regionMatches(p0 + 1, base64, 0, base64.length())) {
                str = str.substring(p1 + 1);
            }
        }
        if (str.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(str);
    }

    /**
     * Reads a JSON array of strings and returns it as a String array.
     *
     * @return The String array
     */
    /**
     * Reads a JSON array of strings and returns it as a String array.
     *
     * @return The String array, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the JSON
     */
    public String[] readStringArray() {
        if ((ch == 'n') && nextIfNull()) {
            return null;
        }

        if (nextIfArrayStart()) {
            String[] values = null;
            int size = 0;
            for (; ; ) {
                if (nextIfArrayEnd()) {
                    if (values == null) {
                        values = new String[0];
                    }
                    break;
                }

                if (isEnd()) {
                    throw new JSONException(info("input end"));
                }

                if (values == null) {
                    values = new String[16];
                } else {
                    if (size == values.length) {
                        values = Arrays.copyOf(values, values.length << 1);
                    }
                }

                values[size++] = readString();
            }

            if (values.length == size) {
                return values;
            }
            return Arrays.copyOf(values, size);
        }

        if (this.ch == '"' || this.ch == '\'') {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }

            throw new JSONException(info("not support input " + str));
        }

        throw new JSONException(info("not support input"));
    }

    /**
     * Reads a string and returns its first UTF-16 code unit. Longer strings are not rejected.
     * A null or empty string returns {@code '\0'} and sets {@link #wasNull()}.
     * <details><summary>中文</summary>读取字符串的第一个 UTF-16 代码单元，不拒绝较长字符串；null 或空字符串返回零字符并设置 wasNull。</details>
     *
     * @return The character value
     */
    public char readCharValue() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return str.charAt(0);
    }

    /**
     * Reads a string and returns its first UTF-16 code unit, or null for a null or empty string.
     * Longer strings are not rejected.
     * <details><summary>中文</summary>返回字符串的第一个 UTF-16 代码单元；null 或空字符串返回 null，不拒绝较长字符串。</details>
     *
     * @return The Character object, or null if the value is null in JSON
     */
    public Character readCharacter() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return null;
        }
        return str.charAt(0);
    }

    /**
     * Reads a null value from JSON data and advances the reader.
     */
    public abstract void readNull();

    protected double readNaN() {
        throw new JSONException("not support");
    }

    /**
     * Checks if the current JSON value is null and advances the reader if it is.
     *
     * @return true if the current value is null, false otherwise
     */
    public abstract boolean readIfNull();

    /**
     * Gets the current string value from the JSON data.
     *
     * @return The current string value
     */
    public abstract String getString();

    /**
     * Returns the null-state flag maintained by value-reading methods. Consult it immediately
     * after a primitive read when distinguishing JSON null from the primitive default value.
     * <details><summary>中文</summary>返回值读取方法维护的 null 标记；需要区分 JSON null 与基本类型默认值时，应在基本类型读取后立即检查。</details>
     *
     * @return whether the preceding value read recorded a null value
     */
    public boolean wasNull() {
        return wasNull;
    }

    /**
     * Reads the next value using the context's reader for the specified type, including generic types.
     * It does not require end of input or execute deferred reference-resolution tasks.
     * <details><summary>中文</summary>使用上下文中的读取器读取指定类型的下一个值，支持泛型；不检查输入是否结束，也不执行延迟引用解析。</details>
     *
     * @param <T> the expected result type
     * @param type the target type
     * @return the converted value
     * @see #handleResolveTasks(Object)
     */
    public <T> T read(Type type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

    /**
     * Reads a JSON array and appends its values to an existing list without clearing it.
     * <details><summary>中文</summary>读取 JSON 数组并追加到现有列表，不清空原有元素。</details>
     *
     * @param list the destination list
     * @throws JSONException if the input is not an array or exceeds the nesting limit
     */
    public final void read(List list) {
        if (!nextIfArrayStart()) {
            throw new JSONException("illegal input, offset " + offset + ", char " + ch);
        }

        level++;
        if (level >= context.maxLevel) {
            throw new JSONException("level too large : " + level);
        }

        for (; ; ) {
            if (nextIfArrayEnd()) {
                level--;
                break;
            }

            Object item = ObjectReaderImplObject.INSTANCE.readObject(this, null, null, 0);
            list.add(item);

            nextIfComma();
        }

        nextIfComma();
    }

    /**
     * Reads a JSON array and appends its values to an existing collection without clearing it.
     * <details><summary>中文</summary>读取 JSON 数组并追加到现有集合，不清空原有元素。</details>
     *
     * @param list the destination collection
     * @throws JSONException if the input is not an array or exceeds the nesting limit
     */
    public final void read(Collection list) {
        if (!nextIfArrayStart()) {
            throw new JSONException("illegal input, offset " + offset + ", char " + ch);
        }

        level++;
        if (level >= context.maxLevel) {
            throw new JSONException("level too large : " + level);
        }

        for (; ; ) {
            if (nextIfArrayEnd()) {
                level--;
                break;
            }
            Object item = readAny();
            list.add(item);

            nextIfComma();
        }

        nextIfComma();
    }

    /**
     * Reads properties into an existing bean or map using the additional features.
     * Properties absent from the input are not reset by this operation.
     * <details><summary>中文</summary>使用附加特性将属性读入现有 bean 或 map；不会主动重置输入中未出现的属性。</details>
     *
     * @param object the non-null destination
     * @param features additional reader features
     * @throws JSONException if the destination is null or its reader does not support in-place population
     */
    public final void readObject(Object object, Feature... features) {
        long featuresLong = 0;
        for (Feature feature : features) {
            featuresLong |= feature.mask;
        }
        readObject(object, featuresLong);
    }

    /**
     * Populates an existing bean or map using a feature bitmask in addition to context features.
     * <details><summary>中文</summary>在上下文特性基础上使用附加位掩码，将数据填充到现有 bean 或 map。</details>
     *
     * @param object the non-null destination
     * @param features additional reader feature bits
     * @throws JSONException if the destination is null or does not support in-place population
     */
    public final void readObject(Object object, long features) {
        if (object == null) {
            throw new JSONException("object is null");
        }
        Class objectClass = object.getClass();
        boolean fieldBased = ((context.features | features) & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(objectClass, fieldBased);
        if (objectReader instanceof ObjectReaderBean) {
            ObjectReaderBean objectReaderBean = (ObjectReaderBean) objectReader;
            objectReaderBean.readObject(this, object, features);
        } else if (object instanceof Map) {
            read((Map) object, features);
        } else {
            throw new JSONException("read object not support");
        }
    }

    /**
     * Reads JSON data into a Map using a specified ObjectReader for the values.
     *
     * @param object The Map to populate with data
     * @param itemReader The ObjectReader to use for reading values
     * @param features Reader features to apply during reading
     * @since 2.0.35
     */
    public void read(Map object, ObjectReader itemReader, long features) {
        nextIfObjectStart();
        Map map;
        if (object instanceof Wrapper) {
            map = ((Wrapper) object).unwrap(Map.class);
        } else {
            map = object;
        }

        long contextFeatures = features | context.getFeatures();

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipComment();
            }

            if (nextIfObjectEnd()) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            String name = readFieldName();
            Object value = itemReader.readObject(this, itemReader.getObjectClass(), name, features);

            if (value == null && (contextFeatures & Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            if ((contextFeatures & Feature.SupportAutoType.mask) != 0
                    && name.equals("@type")
                    && object.getClass().getName().equals(value)
            ) {
                continue;
            }

            Object origin = map.put(name, value);
            if (origin != null) {
                if ((contextFeatures & Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        map.put(name, origin);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        map.put(name, array);
                    }
                }
            }
        }

        nextIfComma();
    }

    /**
     * Reads JSON data into a Map with specified features.
     *
     * @param object The Map to populate with data
     * @param features Reader features to apply during reading
     */
    public void read(Map object, long features) {
        if (ch == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }
        if ((ch == '"' || ch == '\'') && !typeRedirect) {
            String str = readString();
            if (str.isEmpty()) {
                return;
            }
            if (str.charAt(0) == '{') {
                try (JSONReader jsonReader = JSONReader.of(str, context)) {
                    jsonReader.readObject(object, features);
                    return;
                }
            }
        }

        boolean match = nextIfObjectStart();
        boolean typeRedirect = false;
        if (!match) {
            if (typeRedirect = isTypeRedirect()) {
                setTypeRedirect(false);
            } else {
                if (isString()) {
                    String str = readString();
                    if (str.isEmpty()) {
                        return;
                    }
                }
                throw new JSONException(info());
            }
        }

        Map map;
        if (object instanceof Wrapper) {
            map = ((Wrapper) object).unwrap(Map.class);
        } else {
            map = object;
        }

        long contextFeatures = features | context.getFeatures();

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipComment();
            }

            if (nextIfObjectEnd()) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            Object name;
            if (match || typeRedirect) {
                if ((ch >= '0' && ch <= '9') || ch == '-') {
                    name = null;
                } else {
                    name = readFieldName();
                }
            } else {
                name = getFieldName();
                match = true;
            }

            if (name == null) {
                if (isNumber()) {
                    name = readNumber();
                    if ((context.features & Feature.NonStringKeyAsString.mask) != 0) {
                        name = name.toString();
                    }
                    if (comma) {
                        throw new JSONException(info("syntax error, illegal key-value"));
                    }
                } else {
                    if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0) {
                        name = readFieldNameUnquote();
                    } else {
                        throw new JSONException(info("not allow unquoted fieldName"));
                    }
                }
                if (ch == ':') {
                    next();
                }
            }

            if (isReference()) {
                String reference = readReference();
                Object value = null;
                if ("..".equals(reference)) {
                    value = map;
                } else {
                    JSONPath jsonPath;
                    try {
                        jsonPath = JSONPath.of(reference);
                    } catch (Exception ignored) {
                        map.put(name, JSONObject.of("$ref", reference));
                        continue;
                    }
                    addResolveTask(map, name, jsonPath);
                }
                map.put(name, value);
                continue;
            }

            comma = false;
            Object value;
            switch (ch) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    value = readNumber();
                    break;
                case '[':
                    value = readArray();
                    break;
                case '{':
                    if (typeRedirect) {
                        value = ObjectReaderImplObject.INSTANCE.readObject(this, null, name, features);
                    } else {
                        value = readObject();
                    }
                    break;
                case '"':
                case '\'':
                    value = readString();
                    break;
                case 't':
                case 'f':
                    value = readBoolValue();
                    break;
                case 'n':
                    value = readNullOrNewDate();
                    break;
                case '/':
                    next();
                    if (ch == '/') {
                        skipComment();
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "input not support " + ch + ", offset " + offset);
                    }
                    continue;
                case 'S':
                    if (nextIfSet()) {
                        value = read(HashSet.class);
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "error, offset " + offset + ", char " + ch);
                    }
                    break;
                case 'I':
                    if (nextIfInfinity()) {
                        value = Double.POSITIVE_INFINITY;
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "error, offset " + offset + ", char " + ch);
                    }
                    break;
                case 'x':
                    value = readBinary();
                    break;
                default:
                    throw new JSONException("FASTJSON" + JSON.VERSION + "error, offset " + offset + ", char " + ch);
            }

            if (value == null && (contextFeatures & Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            if ((contextFeatures & Feature.SupportAutoType.mask) != 0
                    && name.equals("@type")
                    && object.getClass().getName().equals(value)
            ) {
                continue;
            }

            Object origin = map.put(name, value);
            if (origin != null) {
                if ((contextFeatures & Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        map.put(name, origin);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        map.put(name, array);
                    }
                }
            }
        }

        nextIfComma();
    }

    /**
     * Reads JSON data into a Map with specified key and value types.
     *
     * @param object The Map to populate with data
     * @param keyType The type of keys in the map
     * @param valueType The type of values in the map
     * @param features Reader features to apply during reading
     */
    public final void read(Map object, Type keyType, Type valueType, long features) {
        boolean match = nextIfObjectStart();
        if (!match) {
            throw new JSONException("illegal input， offset " + offset + ", char " + ch);
        }

        ObjectReader keyReader = context.getObjectReader(keyType);
        ObjectReader valueReader = context.getObjectReader(valueType);

        long contextFeatures = features | context.getFeatures();

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipComment();
            }

            if (nextIfObjectEnd()) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            Object name;

            if (keyType == String.class) {
                name = readFieldName();
            } else {
                name = keyReader.readObject(this, null, null, 0L);
                nextIfMatch(':');
            }

            Object value = valueReader.readObject(this, null, null, 0L);

            if (value == null && (contextFeatures & Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            if ((contextFeatures & Feature.SupportAutoType.mask) != 0
                    && name.equals("@type")
                    && object.getClass().getName().equals(value)
            ) {
                continue;
            }

            Object origin = object.put(name, value);
            if (origin != null) {
                if ((contextFeatures & Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        object.put(name, origin);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        object.put(name, array);
                    }
                }
            }
        }

        nextIfComma();
    }

    /**
     * Reads the next value as the requested class using this reader's context.
     * It does not check for trailing input or resolve deferred references; use
     * {@link #isEnd()} and {@link #handleResolveTasks(Object)} when those operations are required.
     * <details><summary>中文</summary>按指定类读取下一个值；不会检查尾随输入或处理延迟引用，需要时分别调用 isEnd 和 handleResolveTasks。</details>
     *
     * @param <T> the result type
     * @param type the target class
     * @return the converted value
     */
    public <T> T read(Class<T> type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

    /**
     * Reads JSON data and returns it as a Map.
     *
     * @return A Map representation of the JSON data
     * @throws JSONException if there is an error parsing the JSON
     */
    public Map<String, Object> readObject() {
        nextIfObjectStart();

        level++;
        if (level >= context.maxLevel) {
            throw new JSONException("level too large : " + level);
        }

        Map innerMap = null;
        Map object;
        if (context.objectSupplier == null) {
            if ((context.features & Feature.UseNativeObject.mask) != 0) {
                object = new HashMap();
            } else {
                object = new JSONObject();
            }
        } else {
            object = context.objectSupplier.get();
            innerMap = TypeUtils.getInnerMap(object);
        }

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipComment();
            }

            if (ch == '}') {
                next();
                break;
            }

            Object name = readFieldName();
            if (name == null) {
                if (ch == EOI) {
                    throw new JSONException("input end");
                }

                if (ch == '-' || (ch >= '0' && ch <= '9')) {
                    readNumber0();
                    name = getNumber();
                } else if (ch == '{') {
                    name = readObject();
                } else if (ch == '[') {
                    name = readArray();
                } else {
                    name = readFieldNameUnquote();
                }
                nextIfMatch(':');
            }

            if (i == 0 && (context.features & Feature.ErrorOnNotSupportAutoType.mask) != 0 && "@type".equals(name)) {
                String typeName = readString();
                throw new JSONException("autoType not support : " + typeName);
            }
            Object val;
            switch (ch) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    readNumber0();
                    val = getNumber();
                    break;
                case '[':
                    val = readArray();
                    break;
                case '{':
                    if (isReference()) {
                        addResolveTask(object, name, JSONPath.of(readReference()));
                        val = null;
                    } else {
                        val = readObject();
                    }
                    break;
                case '"':
                case '\'':
                    val = readString();
                    break;
                case 't':
                case 'f':
                    val = readBoolValue();
                    break;
                case 'n':
                    val = readNullOrNewDate();
                    break;
                case '/':
                    skipComment();
                    continue;
                case 'I':
                    if (nextIfInfinity()) {
                        val = Double.POSITIVE_INFINITY;
                        break;
                    } else {
                        throw new JSONException(info("illegal input " + ch));
                    }
                case 'S':
                    if (nextIfSet()) {
                        val = read(Set.class);
                        break;
                    } else {
                        throw new JSONException(info("illegal input " + ch));
                    }
                default:
                    throw new JSONException(info("illegal input " + ch));
            }

            if (val == null && (context.features & Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            if ((context.features & Feature.SupportAutoType.mask) != 0
                    && name.equals("@type")
                    && object.getClass().getName().equals(val)
            ) {
                continue;
            }

            Object origin;
            if (innerMap != null) {
                origin = innerMap.put(name, val);
            } else {
                origin = object.put(name, val);
            }
            if (origin != null) {
                if ((context.features & Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(val);
                        object.put(name, origin);
                    } else {
                        JSONArray array = JSONArray.of(origin, val);
                        object.put(name, array);
                    }
                }
            }
        }

        if (comma = (ch == ',')) {
            next();
        }

        level--;

        return object;
    }

    /**
     * Skips a comment in the JSON data, advancing the reader to the end of the comment.
     */
    public abstract void skipComment();

    /**
     * Reads a boolean value from JSON data.
     *
     * @return The boolean value, or null if the value is null in JSON
     * @throws JSONException if there is an error parsing the JSON
     */
    public Boolean readBool() {
        if (nextIfNull()) {
            return null;
        }

        wasNull = false;
        boolean boolValue = readBoolValue();
        if (!boolValue && wasNull) {
            return null;
        }
        return boolValue;
    }

    /**
     * Reads a boolean value from JSON data as a primitive boolean.
     *
     * @return The boolean value
     * @throws JSONException if there is an error parsing the JSON
     */
    /**
     * Reads a boolean value from JSON data.
     *
     * @return The boolean value
     */
    public abstract boolean readBoolValue();

    /**
     * Reads any JSON value and returns it as an Object.
     *
     * @return The JSON value as an Object
     */
    public Object readAny() {
        return read(Object.class);
    }

    /**
     * Reads a JSON array with elements of a specified type.
     *
     * @param itemType The type of elements in the array
     * @return A List containing the array elements
     */
    public List readArray(Type itemType) {
        if (nextIfNull()) {
            return null;
        }

        List list = new ArrayList();
        if (ch == '[') {
            next();

            boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
            ObjectReader objectReader = context.provider.getObjectReader(itemType, fieldBased);
            for (int i = 0; !nextIfArrayEnd(); i++) {
                int mark = offset;
                Object item;
                if (isReference()) {
                    String reference = readReference();
                    if ("..".equals(reference)) {
                        item = list;
                    } else {
                        item = null;
                        addResolveTask(list, i, JSONPath.of(reference));
                    }
                } else {
                    item = objectReader.readObject(this, null, null, 0);
                }
                list.add(item);
                if (mark == offset || ch == '}' || ch == EOI) {
                    throw new JSONException("illegal input : " + ch + ", offset " + getOffset());
                }
            }
        } else if (ch == '"' || ch == '\'' || ch == '{') {
            String str = readString();
            if (str != null && !str.isEmpty()) {
                list.add(str);
            }
        } else {
            throw new JSONException(info("syntax error"));
        }

        if (comma = (ch == ',')) {
            next();
        }

        return list;
    }

    /**
     * Reads a JSON array with elements of specified types.
     *
     * @param types The types of elements in the array
     * @return A List containing the array elements
     */
    public List readList(Type[] types) {
        if (nextIfNull()) {
            return null;
        }

        if (!nextIfArrayStart()) {
            throw new JSONException("syntax error : " + ch);
        }

        int i = 0, max = types.length;
        List list = new ArrayList(max);

        for (Object item; !nextIfArrayEnd() && i < max; list.add(item)) {
            int mark = offset;
            item = read(types[i++]);

            if (mark == offset || ch == '}' || ch == EOI) {
                throw new JSONException("illegal input : " + ch + ", offset " + getOffset());
            }
        }

        if (i != max) {
            throw new JSONException(info("element length mismatch"));
        }

        if (comma = (ch == ',')) {
            next();
        }

        return list;
    }

    /**
     * Reads a JSON array with elements of specified types into an Object array.
     *
     * @param types The types of elements in the array
     * @return An Object array containing the array elements
     */
    public final Object[] readArray(Type[] types) {
        if (nextIfNull()) {
            return null;
        }

        if (!nextIfArrayStart()) {
            throw new JSONException(info("syntax error"));
        }

        int i = 0, max = types.length;
        Object[] list = new Object[max];

        for (Object item; !nextIfArrayEnd() && i < max; list[i++] = item) {
            int mark = offset;
            item = read(types[i]);

            if (mark == offset || ch == '}' || ch == EOI) {
                throw new JSONException("illegal input : " + ch + ", offset " + getOffset());
            }
        }

        if (i != max) {
            throw new JSONException(info("element length mismatch"));
        }

        if (comma = (ch == ',')) {
            next();
        }

        return list;
    }

    /**
     * Appends typed values to a list using the array-or-single-value rules of
     * {@link #readArray(Collection, Type)}.
     * <details><summary>中文</summary>按 readArray(Collection, Type) 的数组或单值规则，将指定类型的值追加到列表。</details>
     *
     * @param list the destination list, which is not cleared
     * @param itemType the target element type
     */
    public final void readArray(List list, Type itemType) {
        readArray((Collection) list, itemType);
    }

    /**
     * Appends values converted to {@code itemType}. Arrays contribute all their elements;
     * other values contribute one element. For non-String element types, a string containing
     * commas is split and converted using the provider's String conversion function.
     * <details><summary>中文</summary>追加转换为 itemType 的值：数组逐项追加，其他值追加一项；元素类型不是 String 时，含逗号的字符串会拆分并使用提供者的字符串转换函数转换。</details>
     *
     * @param list the destination collection, which is not cleared
     * @param itemType the target element type
     * @throws JSONException if the input cannot be converted to the element type
     */
    public void readArray(Collection list, Type itemType) {
        if (nextIfArrayStart()) {
            while (!nextIfArrayEnd()) {
                Object item = read(itemType);
                list.add(item);
            }
            return;
        }

        if (isString()) {
            String str = readString();
            if (itemType == String.class) {
                list.add(str);
            } else {
                Function typeConvert = context.getProvider().getTypeConvert(String.class, itemType);
                if (typeConvert == null) {
                    throw new JSONException(info("not support input " + str));
                }
                if (str.indexOf(',') != -1) {
                    String[] items = str.split(",");
                    for (String strItem : items) {
                        Object item = typeConvert.apply(strItem);
                        list.add(item);
                    }
                } else {
                    Object item = typeConvert.apply(str);
                    list.add(item);
                }
            }
        } else {
            Object item = read(itemType);
            list.add(item);
        }

        if (comma = (ch == ',')) {
            next();
        }
    }

    /**
     * Reads a JSON array and returns it as a JSONArray.
     *
     * @return A JSONArray representation of the JSON array
     */
    public final JSONArray readJSONArray() {
        JSONArray array = new JSONArray();
        read(array);
        return array;
    }

    /**
     * Reads a JSON object and returns it as a JSONObject.
     *
     * @return A JSONObject representation of the JSON object
     */
    public final JSONObject readJSONObject() {
        JSONObject object = new JSONObject();
        read(object, 0L);
        return object;
    }

    /**
     * Reads a JSON array and returns it as a List.
     *
     * @return A List representation of the JSON array
     */
    public List readArray() {
        next();

        level++;
        if (level >= context.maxLevel) {
            throw new JSONException("level too large : " + level);
        }

        int i = 0;
        List<Object> list = null;
        Object first = null, second = null;

        _for:
        for (; ; ++i) {
            Object val;
            switch (ch) {
                case ']':
                    next();
                    break _for;
                case '[':
                    val = readArray();
                    break;
                case '{':
                    if (context.autoTypeBeforeHandler != null || (context.features & Feature.SupportAutoType.mask) != 0) {
                        val = ObjectReaderImplObject.INSTANCE.readObject(this, null, null, 0);
                    } else if (isReference()) {
                        val = JSONPath.of(readReference());
                    } else {
                        val = readObject();
                    }
                    break;
                case '\'':
                case '"':
                    val = readString();
                    break;
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    readNumber0();
                    val = getNumber();
                    break;
                case 't':
                case 'f':
                    val = readBoolValue();
                    break;
                case 'n': {
                    readNull();
                    val = null;
                    break;
                }
                case 'N':
                    val = readNaN();
                    break;
                case 'S':
                    if (nextIfSet()) {
                        val = read(java.util.Set.class);
                    } else {
                        throw new JSONException(info());
                    }
                    break;
                case '/':
                    skipComment();
                    --i;
                    continue;
                default:
                    throw new JSONException(info());
            }

            if (i == 0) {
                first = val;
            } else if (i == 1) {
                second = val;
            } else if (i == 2) {
                if (context.arraySupplier != null) {
                    list = context.arraySupplier.get();
                } else {
                    list = new JSONArray();
                }

                add(list, 0, first);
                add(list, 1, second);
                add(list, i, val);
            } else {
                add(list, i, val);
            }
        }

        if (list == null) {
            if (context.arraySupplier != null) {
                list = context.arraySupplier.get();
            } else {
                if (context.isEnabled(Feature.UseNativeObject)) {
                    list = i == 2 ? new ArrayList(2) : new ArrayList(1);
                } else {
                    list = i == 2 ? new JSONArray(2) : new JSONArray(1);
                }
            }

            if (i == 1) {
                add(list, 0, first);
            } else if (i == 2) {
                add(list, 0, first);
                add(list, 1, second);
            }
        }

        if (comma = (ch == ',')) {
            next();
        }

        level--;

        return list;
    }

    private void add(List<Object> list, int i, Object val) {
        if (val instanceof JSONPath) {
            addResolveTask(list, i, (JSONPath) val);
            list.add(null);
        } else {
            list.add(val);
        }
    }

    /**
     * Converts the cached numeric token to BigInteger without advancing the input.
     * A cached BigDecimal loses its fractional part through {@link BigDecimal#toBigInteger()}.
     * <details><summary>中文</summary>将已缓存数字转换为 BigInteger，不推进输入；BigDecimal 的小数部分通过 toBigInteger 截去。</details>
     *
     * @return the converted integer, or null for a cached null value
     */
    public final BigInteger getBigInteger() {
        Number number = getNumber();

        if (number == null) {
            return null;
        }

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).toBigInteger();
        }
        return BigInteger.valueOf(number.longValue());
    }

    /**
     * Converts the cached numeric token to BigDecimal without reading another value.
     * <details><summary>中文</summary>将已缓存数字转换为 BigDecimal，不读取下一个值。</details>
     *
     * @return the converted decimal, or null for a cached null value
     */
    public final BigDecimal getBigDecimal() {
        if (wasNull) {
            return null;
        }

        switch (valueType) {
            case JSON_TYPE_INT: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    return BigDecimal.valueOf(negative ? -mag3 : mag3);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return BigDecimal.valueOf(negative ? -v23 : v23);
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                int signum = negative ? -1 : 1;
                BigInteger bigInt = BIG_INTEGER_CREATOR.apply(signum, mag);
                return new BigDecimal(bigInt);
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;

                if (exponent == 0 && mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                    } else {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;
                            decimal = BigDecimal.valueOf(unscaledVal, scale);
                        }
                    }
                }

                if (decimal == null) {
                    int[] mag = mag0 == 0
                            ? mag1 == 0
                            ? mag2 == 0
                            ? new int[]{mag3}
                            : new int[]{mag2, mag3}
                            : new int[]{mag1, mag2, mag3}
                            : new int[]{mag0, mag1, mag2, mag3};

                    int signum = negative ? -1 : 1;
                    BigInteger bigInt = BIG_INTEGER_CREATOR.apply(signum, mag);
                    decimal = new BigDecimal(bigInt, scale);
                }

                if (exponent != 0) {
                    // Apply the exponent in decimal arithmetic to retain all significant digits.
                    // <details><summary>中文</summary>使用十进制运算应用指数，保留全部有效数字。</details>
                    return decimal.scaleByPowerOfTen(exponent);
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                return toBigDecimal(stringValue);
            }
            case JSON_TYPE_BOOL:
                return boolValue ? BigDecimal.ONE : BigDecimal.ZERO;
            case JSON_TYPE_STRING: {
                try {
                    return toBigDecimal(stringValue);
                } catch (NumberFormatException ex) {
                    throw new JSONException(info("read decimal error, value " + stringValue), ex);
                }
            }
            case JSON_TYPE_OBJECT: {
                JSONObject object = (JSONObject) complex;
                BigDecimal decimal = object.getBigDecimal("value");
                if (decimal == null) {
                    decimal = object.getBigDecimal("$numberDecimal");
                }
                if (decimal != null) {
                    return decimal;
                }
                throw new JSONException("TODO : " + valueType);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    /**
     * Returns the cached numeric token as a Number, selecting its representation according
     * to the token and context features. This method does not read the next input value.
     * <details><summary>中文</summary>根据已缓存标记及上下文特性返回相应 Number 表示，不读取下一个输入值。</details>
     *
     * @return the cached numeric value, or null
     */
    public final Number getNumber() {
        if (wasNull) {
            return null;
        }

        switch (valueType) {
            case JSON_TYPE_INT:
            case JSON_TYPE_INT64: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    int intValue;
                    if (negative) {
                        if (mag3 < 0) {
                            long longValue = -(mag3 & 0xFFFFFFFFL);
                            if ((context.features & Feature.UseBigIntegerForInts.mask) != 0) {
                                return BigInteger.valueOf(longValue);
                            }
                            return longValue;
                        }
                        intValue = -mag3;
                    } else {
                        if (mag3 < 0) {
                            long longValue = mag3 & 0xFFFFFFFFL;
                            if ((context.features & Feature.UseBigIntegerForInts.mask) != 0) {
                                return BigInteger.valueOf(longValue);
                            }
                            return longValue;
                        }
                        intValue = mag3;
                    }

                    if ((context.features & Feature.UseBigIntegerForInts.mask) != 0) {
                        return BigInteger.valueOf(intValue);
                    }

                    if ((context.features & Feature.UseLongForInts.mask) != 0) {
                        return (long) intValue;
                    }

                    if (valueType == JSON_TYPE_INT64) {
                        return (long) intValue;
                    }
                    return intValue;
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long longValue = negative ? -v23 : v23;
                            if ((context.features & Feature.UseBigIntegerForInts.mask) != 0) {
                                return BigInteger.valueOf(longValue);
                            }
                            return longValue;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                int signum = negative ? -1 : 1;
                BigInteger integer = BIG_INTEGER_CREATOR.apply(signum, mag);
                if ((context.features & Feature.UseLongForInts.mask) != 0) {
                    return integer.longValue();
                }
                return integer;
            }
            case JSON_TYPE_INT16: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return (short) intValue;
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_INT8: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return (byte) intValue;
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;
                boolean needsExponent = false;

                if (mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                        needsExponent = true;
                    } else {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + v3;
                            long unscaledVal = negative ? -v23 : v23;
                            decimal = BigDecimal.valueOf(unscaledVal, scale);
                            needsExponent = true;
                        }
                    }
                }

                if (decimal == null) {
                    int[] mag = mag0 == 0
                            ? mag1 == 0
                            ? new int[]{mag2, mag3}
                            : new int[]{mag1, mag2, mag3}
                            : new int[]{mag0, mag1, mag2, mag3};
                    int signum = negative ? -1 : 1;
                    BigInteger bigInt = BIG_INTEGER_CREATOR.apply(signum, mag);

                    int adjustedScale = scale - exponent;
                    decimal = new BigDecimal(bigInt, adjustedScale);
                    if (exponent != 0 && (context.features & (Feature.UseBigDecimalForDoubles.mask | Feature.UseBigDecimalForFloats.mask)) == 0) {
                        return decimal.doubleValue();
                    }
                }

                if (exponent != 0) {
                    String decimalStr = decimal.toPlainString();
                    if ((context.features & (Feature.UseBigDecimalForDoubles.mask | Feature.UseBigDecimalForFloats.mask)) == 0) {
                        return Double.parseDouble(
                                decimalStr + "E" + exponent);
                    }
                    if (needsExponent) {
                        return decimal.signum() == 0 ? BigDecimal.ZERO : new BigDecimal(decimalStr + "E" + exponent);
                    }
                    return decimal.signum() == 0 ? BigDecimal.ZERO : decimal;
                }

                if ((context.features & Feature.UseDoubleForDecimals.mask) != 0) {
                    return decimal.doubleValue();
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                boolean hasExponent = stringValue.indexOf('e') != -1 || stringValue.indexOf('E') != -1;
                if (hasExponent) {
                    return toBigDecimal(stringValue);
                } else if (scale > 0) {
                    if (scale > defaultDecimalMaxScale) {
                        throw new JSONException("scale overflow : " + scale);
                    }
                    return toBigDecimal(stringValue);
                } else {
                    return new BigInteger(stringValue);
                }
            }
            case JSON_TYPE_FLOAT:
            case JSON_TYPE_DOUBLE: {
                int[] mag = mag0 == 0
                        ? mag1 == 0
                        ? mag2 == 0
                        ? new int[]{mag3}
                        : new int[]{mag2, mag3}
                        : new int[]{mag1, mag2, mag3}
                        : new int[]{mag0, mag1, mag2, mag3};
                int signum = negative ? -1 : 1;
                BigInteger bigInt = BIG_INTEGER_CREATOR.apply(signum, mag);
                BigDecimal decimal = new BigDecimal(bigInt, scale);

                if (valueType == JSON_TYPE_FLOAT) {
                    if (exponent != 0) {
                        return Float.parseFloat(
                                decimal + "E" + exponent);
                    }

                    return decimal.floatValue();
                }

                if (exponent != 0) {
                    return Double.parseDouble(
                            decimal + "E" + exponent);
                }
                return decimal.doubleValue();
            }
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
                return null;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                return toNumber((Map) complex);
            }
            case JSON_TYPE_ARRAY: {
                return toNumber((List) complex);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    /**
     * Releases reusable buffers. Text readers created from a stream or Reader also close
     * that input, suppressing close-time I/O exceptions. JSONB stream factories retain no
     * stream handle, so their callers remain responsible for closing the input.
     * <details><summary>中文</summary>释放可复用缓冲区；文本读取器还会关闭输入流或 Reader，并忽略关闭时的 I/O 异常。JSONB 流工厂不保留流句柄，调用方须自行关闭输入。</details>
     */
    @Override
    public abstract void close();

    protected final int toInt32(String val) {
        if (IOUtils.isNumber(val) || val.lastIndexOf(',') == val.length() - 4) {
            return TypeUtils.toIntValue(val);
        }

        throw error("parseInt error, value : " + val);
    }

    protected final long toInt64(String val) {
        if (IOUtils.isNumber(val)
                || val.lastIndexOf(',') == val.length() - 4) {
            return TypeUtils.toLongValue(val);
        }

        if (val.length() > 10 && val.length() < 40) {
            try {
                return DateUtils.parseMillis(val, context.zoneId);
            } catch (DateTimeException | JSONException | NullPointerException ignored) {
                // ignored
            }
        }

        throw error("parseLong error, value : " + val);
    }

    protected final long toLong(Map map) {
        Object val = map.get("val");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        throw error("parseLong error, value : " + map);
    }

    protected final int toInt(List list) {
        if (list.size() == 1) {
            Object val = list.get(0);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            if (val instanceof String) {
                return Integer.parseInt((String) val);
            }
        }

        throw error("parseLong error, field : value " + list);
    }

    protected final Number toNumber(Map map) {
        Object val = map.get("val");
        if (val instanceof Number) {
            return (Number) val;
        }
        return null;
    }

    protected final BigDecimal decimal(JSONObject object) {
        BigDecimal decimal = object.getBigDecimal("value");
        if (decimal == null) {
            decimal = object.getBigDecimal("$numberDecimal");
        }
        if (decimal != null) {
            return decimal;
        }
        throw error("can not cast to decimal " + object);
    }

    protected final Number toNumber(List list) {
        if (list.size() == 1) {
            Object val = list.get(0);
            if (val instanceof Number) {
                return (Number) val;
            }

            if (val instanceof String) {
                return toBigDecimal((String) val);
            }
        }
        return null;
    }

    protected final String toString(List array) {
        JSONWriter writer = JSONWriter.of();
        writer.setRootObject(array);
        writer.write(array);
        return writer.toString();
    }

    protected final String toString(Map object) {
        JSONWriter writer = JSONWriter.of();
        writer.setRootObject(object);
        writer.write(object);
        return writer.toString();
    }

    /**
     * Creates a JSONReader from a byte array containing UTF-8 encoded JSON.
     *
     * @param utf8Bytes The byte array containing UTF-8 encoded JSON
     * @return A JSONReader instance
     */
    public static JSONReader of(byte[] utf8Bytes) {
        return of(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.UTF_8, createReadContext());
    }

    @Deprecated
    public static JSONReader of(Context context, byte[] utf8Bytes) {
        return JSONReaderUTF8.of(utf8Bytes, 0, utf8Bytes.length, context);
    }

    public static JSONReader of(byte[] utf8Bytes, Context context) {
        return JSONReaderUTF8.of(utf8Bytes, 0, utf8Bytes.length, context);
    }

    /**
     * Creates a JSONReader from a character array containing JSON data.
     *
     * @param chars The character array containing JSON data
     * @return A JSONReader instance
     */
    public static JSONReader of(char[] chars) {
        return ofUTF16(
                null,
                chars,
                0,
                chars.length, createReadContext());
    }

    @Deprecated
    public static JSONReader of(Context context, char[] chars) {
        return ofUTF16(null, chars, 0, chars.length, context);
    }

    public static JSONReader of(char[] chars, Context context) {
        return ofUTF16(null, chars, 0, chars.length, context);
    }

    /**
     * Creates a JSONReader from a byte array containing JSONB (binary JSON) data.
     *
     * @param jsonbBytes The byte array containing JSONB data
     * @return A JSONReader instance for JSONB data
     */
    public static JSONReader ofJSONB(byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(),
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    @Deprecated
    public static JSONReader ofJSONB(Context context, byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, Context context) {
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    /**
     * Reads the stream to end of input and creates a reader over the buffered JSONB bytes.
     * Neither this factory nor the returned reader closes the stream.
     * <details><summary>中文</summary>读取流至结束并缓冲 JSONB 数据；工厂及返回的读取器均不关闭输入流。</details>
     *
     * @param in the binary JSON input stream
     * @param context the reading context
     * @return a JSONB reader
     * @throws JSONException if reading the stream fails
     */
    public static JSONReader ofJSONB(InputStream in, Context context) {
        return new JSONReaderJSONB(context, in);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, Feature... features) {
        Context context = JSONFactory.createReadContext();
        context.config(features);
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(),
                bytes,
                offset,
                length);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length, Context context) {
        return new JSONReaderJSONB(context, bytes, offset, length);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length, SymbolTable symbolTable) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(symbolTable),
                bytes,
                offset,
                length);
    }

    /**
     * Creates a text reader for a byte-array slice using a supported standard charset.
     * The charset must be UTF-8, UTF-16, US-ASCII, or ISO-8859-1; null is not accepted by this overload.
     * <details><summary>中文</summary>按指定标准字符集读取字节数组切片；仅支持 UTF-8、UTF-16、US-ASCII、ISO-8859-1，此重载不接受 null 字符集。</details>
     *
     * @param bytes the input bytes
     * @param offset the first byte to read
     * @param length the number of bytes to read, not the end index
     * @param charset the input charset
     * @return a text JSON reader using a default context
     * @throws JSONException if the charset is unsupported
     */
    public static JSONReader of(byte[] bytes, int offset, int length, Charset charset) {
        Context context = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8) {
            return JSONReaderUTF8.of(bytes, offset, length, context);
        }

        if (charset == StandardCharsets.UTF_16) {
            return ofUTF16(bytes, offset, length, context);
        }

        if (charset == StandardCharsets.US_ASCII || charset == StandardCharsets.ISO_8859_1) {
            return JSONReaderASCII.of(context, null, bytes, offset, length);
        }

        throw new JSONException("not support charset " + charset);
    }

    private static JSONReader ofUTF16(byte[] bytes, int offset, int length, Context ctx) {
        return new JSONReaderUTF16(ctx, bytes, offset, length);
    }

    private static JSONReader ofUTF16(String str, char[] chars, int offset, int length, Context ctx) {
        return new JSONReaderUTF16(ctx, str, chars, offset, length);
    }

    /**
     * Creates a text reader for a byte-array slice with the supplied context.
     * Supported charsets are UTF-8, UTF-16, US-ASCII, and ISO-8859-1.
     * <details><summary>中文</summary>使用指定上下文读取字节数组切片，支持 UTF-8、UTF-16、US-ASCII、ISO-8859-1。</details>
     *
     * @param bytes the input bytes
     * @param offset the first byte to read
     * @param length the number of bytes to read
     * @param charset the supported input charset, not null
     * @param context the reading context
     * @return a text JSON reader
     * @throws JSONException if the charset is unsupported or null
     */
    public static JSONReader of(byte[] bytes, int offset, int length, Charset charset, Context context) {
        if (charset == StandardCharsets.UTF_8) {
            return JSONReaderUTF8.of(bytes, offset, length, context);
        }

        if (charset == StandardCharsets.UTF_16) {
            return ofUTF16(bytes, offset, length, context);
        }

        if (charset == StandardCharsets.US_ASCII || charset == StandardCharsets.ISO_8859_1) {
            return JSONReaderASCII.of(context, null, bytes, offset, length);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(byte[] bytes, int offset, int length) {
        return of(bytes, offset, length, StandardCharsets.UTF_8, createReadContext());
    }

    public static JSONReader of(byte[] bytes, int offset, int length, Context context) {
        return new JSONReaderUTF8(context, bytes, offset, length);
    }

    public static JSONReader of(char[] chars, int offset, int length) {
        return ofUTF16(null, chars, offset, length, createReadContext());
    }

    public static JSONReader of(char[] chars, int offset, int length, Context context) {
        return ofUTF16(null, chars, offset, length, context);
    }

    /**
     * Opens the URL, buffers its UTF-8 JSON content, and closes the opened stream before returning.
     * <details><summary>中文</summary>打开 URL，缓冲其 UTF-8 JSON 内容，并在返回前关闭所打开的流。</details>
     *
     * @param url the JSON resource
     * @param context the reading context
     * @return a reader over the buffered content
     * @throws IOException if opening or closing the URL stream fails
     * @throws JSONException if reading the content fails
     */
    public static JSONReader of(URL url, Context context) throws IOException {
        try (InputStream is = url.openStream()) {
            return of(is, StandardCharsets.UTF_8, context);
        }
    }

    /**
     * Buffers text JSON from an InputStream using the specified charset, or UTF-8 when null.
     * Closing the returned reader closes the input stream.
     * <details><summary>中文</summary>按指定字符集缓冲流中的 JSON 文本，null 表示 UTF-8；关闭返回的读取器会关闭输入流。</details>
     *
     * @param is The InputStream containing JSON data
     * @param charset The character encoding of the JSON data
     * @return A JSONReader instance
     */
    public static JSONReader of(InputStream is, Charset charset) {
        Context context = JSONFactory.createReadContext();
        return of(is, charset, context);
    }

    /**
     * Buffers text JSON from a stream using the supplied context. A null charset means UTF-8;
     * other charsets can be decoded through an InputStreamReader. Closing this reader closes the input.
     * <details><summary>中文</summary>使用指定上下文缓冲流中的 JSON 文本；null 字符集表示 UTF-8，其他字符集可通过 InputStreamReader 解码；关闭读取器会关闭输入流。</details>
     *
     * @param is the input stream
     * @param charset the input charset, or null for UTF-8
     * @param context the reading context
     * @return a text JSON reader
     * @throws JSONException if the stream is null or reading fails
     */
    public static JSONReader of(InputStream is, Charset charset, Context context) {
        if (is == null) {
            throw new JSONException("inputStream is null");
        }

        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, is);
        }

        if (charset == StandardCharsets.UTF_16) {
            return new JSONReaderUTF16(context, is);
        }

        if (charset == StandardCharsets.US_ASCII) {
            return JSONReaderASCII.of(context, is);
        }

        return JSONReader.of(new InputStreamReader(is, charset), context);
    }

    /**
     * Buffers JSON characters from the supplied Reader. Closing the returned JSONReader closes the input.
     * <details><summary>中文</summary>缓冲输入 Reader 中的 JSON 字符；关闭返回的 JSONReader 会关闭输入 Reader。</details>
     *
     * @param is The Reader containing JSON data
     * @return A JSONReader instance
     */
    public static JSONReader of(Reader is) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext(),
                is
        );
    }

    /**
     * Buffers JSON characters using the supplied context. Closing the returned reader closes the input.
     * <details><summary>中文</summary>使用指定上下文缓冲 JSON 字符；关闭返回的读取器会关闭输入 Reader。</details>
     *
     * @param is the character input
     * @param context the reading context
     * @return a text JSON reader
     * @throws JSONException if reading fails
     */
    public static JSONReader of(Reader is, Context context) {
        return new JSONReaderUTF16(
                context,
                is
        );
    }

    /**
     * Copies the buffer's remaining bytes as UTF-8 JSON, advancing its position to its limit.
     * Only UTF-8 or a null charset is supported.
     * <details><summary>中文</summary>复制缓冲区剩余字节作为 UTF-8 JSON，并将 position 推进到 limit；字符集仅支持 UTF-8 或 null。</details>
     *
     * @param buffer the source buffer
     * @param charset UTF-8, or null to select UTF-8
     * @return a reader over the copied bytes
     * @throws JSONException if the charset is unsupported
     */
    public static JSONReader of(ByteBuffer buffer, Charset charset) {
        Context context = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, buffer);
        }

        throw new JSONException("not support charset " + charset);
    }

    /**
     * Copies the buffer's remaining UTF-8 bytes using the supplied context, advancing its
     * position to its limit. The returned reader does not depend on later buffer changes.
     * <details><summary>中文</summary>使用指定上下文复制缓冲区剩余 UTF-8 字节，并将 position 推进到 limit；之后修改缓冲区不影响返回的读取器。</details>
     *
     * @param buffer the source buffer
     * @param charset UTF-8, or null to select UTF-8
     * @param context the reading context
     * @return a reader over the copied bytes
     * @throws JSONException if the charset is unsupported
     */
    public static JSONReader of(ByteBuffer buffer, Charset charset, Context context) {
        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, buffer);
        }

        throw new JSONException("not support charset " + charset);
    }

    @Deprecated
    public static JSONReader of(Context context, String str) {
        return of(str, context);
    }

    /**
     * Creates a JSONReader from a String containing JSON data.
     *
     * @param str The String containing JSON data
     * @return A JSONReader instance
     */
    public static JSONReader of(String str) {
        return of(str, JSONFactory.createReadContext());
    }

    public static JSONReader of(String str, Context context) {
        if (str == null || context == null) {
            throw new NullPointerException();
        }

        if (STRING_VALUE != null && STRING_CODER != null) {
            try {
                final int LATIN1 = 0;
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == LATIN1) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return JSONReaderASCII.of(context, str, bytes, 0, bytes.length);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        final int length = str.length();
        char[] chars;
        if (JVM_VERSION == 8) {
            chars = JDKUtils.getCharArray(str);
        } else {
            chars = str.toCharArray();
        }

        return ofUTF16(str, chars, 0, length, context);
    }

    /**
     * Creates a JSONReader from a substring of a String containing JSON data.
     *
     * @param str The String containing JSON data
     * @param offset The starting position of the substring
     * @param length The length of the substring
     * @return A JSONReader instance
     */
    public static JSONReader of(String str, int offset, int length) {
        return of(str, offset, length, JSONFactory.createReadContext());
    }

    public static JSONReader of(String str, int offset, int length, Context context) {
        if (str == null || context == null) {
            throw new NullPointerException();
        }

        if (STRING_VALUE != null && STRING_CODER != null) {
            try {
                final int LATIN1 = 0;
                int coder = STRING_CODER.applyAsInt(str);
                if (coder == LATIN1) {
                    byte[] bytes = STRING_VALUE.apply(str);
                    return JSONReaderASCII.of(context, str, bytes, offset, length);
                }
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error");
            }
        }

        char[] chars;
        if (JVM_VERSION == 8) {
            chars = JDKUtils.getCharArray(str);
        } else {
            chars = str.toCharArray();
        }

        return ofUTF16(str, chars, offset, length, context);
    }

    final void bigInt(char[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 38) {
            throw new JSONException("number too large : " + new String(chars, off, numDigits));
        }

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = chars[start++];
            if (c == '.') {
                c = chars[start++];
                cursor++;
//                    end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = chars[index];
                if (c == '.') {
                    c = chars[++index];
                    cursor++;
                    if (end < len) {
                        end++;
                    }
                }

                int nextVal = c - '0';
                result = 10 * result + nextVal;
            }
            mag3 = result;
        }

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal;
            {
                int start = cursor;
                int end = cursor += 9;

                char c = chars[start++];
                if (c == '.') {
                    c = chars[start++];
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = chars[index];
                    if (c == '.') {
                        c = chars[++index];
                        cursor++;
                        end++;
                    }

                    int nextVal = c - '0';
                    result = 10 * result + nextVal;
                }
                groupVal = result;
            }

            // destructiveMulAdd
            long ylong = 1000000000 & 0XFFFFFFFFL;

            long product;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & 0XFFFFFFFFL) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & 0XFFFFFFFFL) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & 0XFFFFFFFFL) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & 0XFFFFFFFFL) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long zlong = groupVal & 0XFFFFFFFFL;
            long sum = (mag3 & 0XFFFFFFFFL) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & 0XFFFFFFFFL) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & 0XFFFFFFFFL) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & 0XFFFFFFFFL) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & 0XFFFFFFFFL) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    final void bigInt(byte[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 38) {
            throw new JSONException("number too large : " + new String(chars, off, numDigits));
        }

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = (char) chars[start++];
            if (c == '.') {
                c = (char) chars[start++];
                cursor++;
//                    end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = (char) chars[index];
                if (c == '.') {
                    c = (char) chars[++index];
                    cursor++;
                    if (end < len) {
                        end++;
                    }
                }

                int nextVal = c - '0';
                result = 10 * result + nextVal;
            }
            mag3 = result;
        }

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal;
            {
                int start = cursor;
                int end = cursor += 9;

                char c = (char) chars[start++];
                if (c == '.') {
                    c = (char) chars[start++];
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = (char) chars[index];
                    if (c == '.') {
                        c = (char) chars[++index];
                        cursor++;
                        end++;
                    }

                    int nextVal = c - '0';
                    result = 10 * result + nextVal;
                }
                groupVal = result;
            }

            // destructiveMulAdd
            long ylong = 1000000000 & 0XFFFFFFFFL;
            long zlong = groupVal & 0XFFFFFFFFL;

            long product;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & 0XFFFFFFFFL) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & 0XFFFFFFFFL) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & 0XFFFFFFFFL) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & 0XFFFFFFFFL) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long sum = (mag3 & 0XFFFFFFFFL) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & 0XFFFFFFFFL) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & 0XFFFFFFFFL) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & 0XFFFFFFFFL) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & 0XFFFFFFFFL) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    public interface AutoTypeBeforeHandler
            extends Filter {
        default Class<?> apply(long typeNameHash, Class<?> expectClass, long features) {
            return null;
        }

        Class<?> apply(String typeName, Class<?> expectClass, long features);
    }

    /**
     * Creates an auto-type handler accepting the supplied class-name or package-name prefixes.
     * Register it with {@link Context#config(Filter)}; this method does not change global configuration.
     * <details><summary>中文</summary>创建接受指定类名或包名前缀的自动类型处理器；通过 Context.config 注册，不修改全局配置。</details>
     *
     * @param names accepted name prefixes
     * @return the auto-type handler
     */
    public static AutoTypeBeforeHandler autoTypeFilter(String... names) {
        return new ContextAutoTypeBeforeHandler(names);
    }

    /**
     * Creates a prefix-based auto-type handler, optionally including the built-in set of
     * common scalar, temporal, and collection types.
     * <details><summary>中文</summary>创建按名称前缀匹配的自动类型处理器，并可包含内置的常用标量、时间和集合类型。</details>
     *
     * @param includeBasic whether to include the built-in basic type set
     * @param names accepted class-name or package-name prefixes
     * @return the auto-type handler
     */
    public static AutoTypeBeforeHandler autoTypeFilter(boolean includeBasic, String... names) {
        return new ContextAutoTypeBeforeHandler(includeBasic, names);
    }

    /**
     * Creates an auto-type handler from the serialization names of the supplied classes.
     * Names are matched using the same prefix rules as {@link #autoTypeFilter(String...)};
     * this is not an assignability-based subtype filter.
     * <details><summary>中文</summary>使用指定类的序列化名称创建自动类型处理器；采用与字符串重载相同的前缀规则，并非基于继承关系筛选子类型。</details>
     *
     * @param types classes whose serialization names should be accepted
     * @return the auto-type handler
     */
    public static AutoTypeBeforeHandler autoTypeFilter(Class... types) {
        return new ContextAutoTypeBeforeHandler(types);
    }

    /**
     * Creates a name-based auto-type handler from classes, optionally adding the built-in basic types.
     * Class names use the prefix rules described by {@link #autoTypeFilter(Class...)}.
     * <details><summary>中文</summary>按类名创建自动类型处理器，并可加入内置基础类型；类名使用 Class 重载描述的前缀匹配规则。</details>
     *
     * @param includeBasic whether to include the built-in basic type set
     * @param types classes whose serialization names should be accepted
     * @return the auto-type handler
     */
    public static AutoTypeBeforeHandler autoTypeFilter(boolean includeBasic, Class... types) {
        return new ContextAutoTypeBeforeHandler(includeBasic, types);
    }

    /**
     * Context holds the configuration and state information for JSON reading operations.
     * It controls various aspects of the deserialization process including formatting,
     * features, providers, and other settings that affect how JSON data is parsed and
     * converted to Java objects.
     *
     * <p>The Context class is responsible for:</p>
     * <ul>
     *   <li>Managing reader features that control deserialization behavior</li>
     *   <li>Handling date/time formatting and timezone settings</li>
     *   <li>Providing object and array suppliers for custom collection creation</li>
     *   <li>Managing auto-type handlers for security and customization</li>
     *   <li>Storing parser configuration such as max nesting level and buffer size</li>
     * </ul>
     *
     * <p>Context instances can be created in several ways:</p>
     * <pre>
     * // Using default configuration
     * JSONReader.Context context = new JSONReader.Context();
     *
     * // With specific features enabled
     * JSONReader.Context context = new JSONReader.Context(
     *     JSONReader.Feature.FieldBased,
     *     JSONReader.Feature.TrimString
     * );
     *
     * // With custom date format
     * JSONReader.Context context = new JSONReader.Context("yyyy-MM-dd HH:mm:ss");
     *
     * // With custom provider and features
     * ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
     * JSONReader.Context context = new JSONReader.Context(provider,
     *     JSONReader.Feature.FieldBased
     * );
     * </pre>
     *
     * <p>Once created, a Context can be configured further:</p>
     * <pre>
     * context.setZoneId(ZoneId.of("UTC"));
     * context.setLocale(Locale.US);
     * context.setMaxLevel(1000);
     * context.setBufferSize(64 * 1024);
     * context.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
     * </pre>
     *
     * <p>Context instances are typically used when creating JSONReader instances:</p>
     * <pre>
     * JSONReader.Context context = new JSONReader.Context();
     * context.config(JSONReader.Feature.FieldBased);
     *
     * try (JSONReader reader = JSONReader.of(json, context)) {
     *     MyObject obj = reader.read(MyObject.class);
     * }
     * </pre>
     *
     * <p>Note that Context instances are not thread-safe and should not be shared
     * between multiple concurrent reading operations. Each JSONReader should have
     * its own Context instance or use the default context provided by factory methods.</p>
     *
     * @see JSONReader
     * @see JSONReader.Feature
     * @see ObjectReaderProvider
     * @since 2.0.0
     */
    public static final class Context {
        String dateFormat;
        boolean formatComplex;
        boolean formatyyyyMMddhhmmss19;
        boolean formatyyyyMMddhhmmssT19;
        boolean yyyyMMddhhmm16;
        boolean formatyyyyMMdd8;
        boolean formatMillis;
        boolean formatUnixTime;
        boolean formatISO8601;
        boolean formatHasDay;
        boolean formatHasHour;
        boolean useSimpleFormatter;
        int maxLevel = 2048;
        int bufferSize = 1024 * 512;
        DateTimeFormatter dateFormatter;
        ZoneId zoneId;
        long features;
        Locale locale;
        TimeZone timeZone;
        Supplier<Map> objectSupplier;
        Supplier<List> arraySupplier;
        AutoTypeBeforeHandler autoTypeBeforeHandler;
        ExtraProcessor extraProcessor;

        final ObjectReaderProvider provider;
        final SymbolTable symbolTable;

        /**
         * Creates a new Context with the specified object reader provider.
         *
         * @param provider the object reader provider to use
         */
        public Context(ObjectReaderProvider provider) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Creates a new Context with the specified object reader provider and features.
         *
         * @param provider the object reader provider to use
         * @param features the initial features bitmask
         */
        public Context(ObjectReaderProvider provider, long features) {
            this.features = features;
            this.provider = provider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Creates a new Context with the specified features.
         *
         * @param features the features to enable
         */
        public Context(Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = JSONFactory.getDefaultObjectReaderProvider();
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Creates a new Context with the specified date format and features.
         *
         * @param dateFormat the date format pattern to use
         * @param features the features to enable
         */
        public Context(String dateFormat, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = JSONFactory.getDefaultObjectReaderProvider();
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
            setDateFormat(dateFormat);
        }

        /**
         * Creates a new Context with the specified object reader provider and features.
         *
         * @param provider the object reader provider to use
         * @param features the features to enable
         */
        public Context(ObjectReaderProvider provider, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Creates a new Context with the specified object reader provider, filter, and features.
         *
         * @param provider the object reader provider to use
         * @param filter the filter to configure
         * @param features the features to enable
         */
        public Context(ObjectReaderProvider provider, Filter filter, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            config(filter);

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Creates a new Context with the specified object reader provider and symbol table.
         *
         * @param provider the object reader provider to use
         * @param symbolTable the symbol table to use
         */
        public Context(ObjectReaderProvider provider, SymbolTable symbolTable) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }
        }

        /**
         * Creates a new Context with the specified object reader provider, symbol table, and features.
         *
         * @param provider the object reader provider to use
         * @param symbolTable the symbol table to use
         * @param features the features to enable
         */
        public Context(ObjectReaderProvider provider, SymbolTable symbolTable, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Creates a new Context with the specified object reader provider, symbol table, filters, and features.
         *
         * @param provider the object reader provider to use
         * @param symbolTable the symbol table to use
         * @param filters the filters to configure
         * @param features the features to enable
         */
        public Context(ObjectReaderProvider provider, SymbolTable symbolTable, Filter[] filters, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
            this.zoneId = defaultReaderZoneId;

            config(filters);

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Checks if the context is configured to format Unix time.
         *
         * @return true if Unix time formatting is enabled, false otherwise
         */
        public boolean isFormatUnixTime() {
            return formatUnixTime;
        }

        /**
         * Checks if the context is configured to format dates in yyyyMMddHHmmss format (19 characters).
         *
         * @return true if this format is enabled, false otherwise
         */
        public boolean isFormatyyyyMMddhhmmss19() {
            return formatyyyyMMddhhmmss19;
        }

        /**
         * Checks if the context is configured to format dates in yyyy-MM-dd'T'HH:mm:ss format (19 characters).
         *
         * @return true if this format is enabled, false otherwise
         */
        public boolean isFormatyyyyMMddhhmmssT19() {
            return formatyyyyMMddhhmmssT19;
        }

        /**
         * Checks if the context is configured to format dates in yyyyMMdd format (8 characters).
         *
         * @return true if this format is enabled, false otherwise
         */
        public boolean isFormatyyyyMMdd8() {
            return formatyyyyMMdd8;
        }

        /**
         * Checks if the context is configured to format milliseconds.
         *
         * @return true if millisecond formatting is enabled, false otherwise
         */
        public boolean isFormatMillis() {
            return formatMillis;
        }

        /**
         * Checks if the context is configured to format dates in ISO8601 format.
         *
         * @return true if ISO8601 formatting is enabled, false otherwise
         */
        public boolean isFormatISO8601() {
            return formatISO8601;
        }

        /**
         * Checks if the context is configured to format dates with hour information.
         *
         * @return true if hour formatting is enabled, false otherwise
         */
        public boolean isFormatHasHour() {
            return formatHasHour;
        }

        /**
         * Gets an ObjectReader for the specified type.
         *
         * @param type The type for which to get an ObjectReader
         * @return An ObjectReader for the specified type
         */
        public ObjectReader getObjectReader(Type type) {
            boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
            return provider.getObjectReader(type, fieldBased);
        }

        /**
         * Gets the ObjectReaderProvider used by this context.
         *
         * @return The ObjectReaderProvider
         */
        public ObjectReaderProvider getProvider() {
            return provider;
        }

        /**
         * Gets an ObjectReader for the specified type hash code.
         *
         * @param hashCode The hash code of the type
         * @return An ObjectReader for the specified type hash code, or null if not found
         * @deprecated a hash hit is not an AutoType authorization; resolve by type name instead
         */
        @Deprecated
        public ObjectReader getObjectReaderAutoType(long hashCode) {
            return provider.getObjectReader(hashCode);
        }

        /**
         * Gets an ObjectReader for the specified type name and expected class.
         *
         * @param typeName The type name
         * @param expectClass The expected class
         * @return An ObjectReader for the specified type, or null if not found
         */
        public ObjectReader getObjectReaderAutoType(String typeName, Class expectClass) {
            if (autoTypeBeforeHandler != null) {
                Class<?> autoTypeClass = autoTypeBeforeHandler.apply(typeName, expectClass, features);
                if (autoTypeClass != null) {
                    boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
                    return provider.getObjectReader(autoTypeClass, fieldBased);
                }
            }

            return provider.getObjectReader(typeName, expectClass, features);
        }

        /**
         * Gets the AutoTypeBeforeHandler configured for this context.
         *
         * @return The AutoTypeBeforeHandler, or null if not configured
         */
        public AutoTypeBeforeHandler getContextAutoTypeBeforeHandler() {
            return autoTypeBeforeHandler;
        }

        /**
         * Gets an ObjectReader for the specified type name, expected class, and additional features.
         *
         * @param typeName The type name
         * @param expectClass The expected class
         * @param features Additional features to consider
         * @return An ObjectReader for the specified type, or null if not found
         */
        public ObjectReader getObjectReaderAutoType(String typeName, Class expectClass, long features) {
            if (autoTypeBeforeHandler != null) {
                Class<?> autoTypeClass = autoTypeBeforeHandler.apply(typeName, expectClass, features);
                if (autoTypeClass != null) {
                    boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
                    return provider.getObjectReader(autoTypeClass, fieldBased);
                }
            }

            return provider.getObjectReader(typeName, expectClass, this.features | features);
        }

        /**
         * Gets the ExtraProcessor configured for this context.
         *
         * @return The ExtraProcessor, or null if not configured
         */
        public ExtraProcessor getExtraProcessor() {
            return extraProcessor;
        }

        /**
         * Sets the ExtraProcessor for this context.
         *
         * @param extraProcessor The ExtraProcessor to set
         */
        public void setExtraProcessor(ExtraProcessor extraProcessor) {
            this.extraProcessor = extraProcessor;
        }

        /**
         * Gets the object supplier configured for this context.
         *
         * @return The object supplier
         */
        public Supplier<Map> getObjectSupplier() {
            return objectSupplier;
        }

        /**
         * Sets the object supplier for this context.
         *
         * @param objectSupplier The object supplier to set
         */
        public void setObjectSupplier(Supplier<Map> objectSupplier) {
            this.objectSupplier = objectSupplier;
        }

        /**
         * Gets the array supplier configured for this context.
         *
         * @return The array supplier
         */
        public Supplier<List> getArraySupplier() {
            return arraySupplier;
        }

        /**
         * Sets the array supplier for this context.
         *
         * @param arraySupplier The array supplier to set
         */
        public void setArraySupplier(Supplier<List> arraySupplier) {
            this.arraySupplier = arraySupplier;
        }

        /**
         * Gets the date formatter configured for this context.
         *
         * @return The DateTimeFormatter, or null if not configured
         */
        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !formatMillis && !formatISO8601 && !formatUnixTime) {
                dateFormatter = locale == null
                        ? DateTimeFormatter.ofPattern(dateFormat)
                        : DateTimeFormatter.ofPattern(dateFormat, locale);
            }
            return dateFormatter;
        }

        /**
         * Sets the date formatter for this context.
         *
         * @param dateFormatter The DateTimeFormatter to set
         */
        public void setDateFormatter(DateTimeFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        /**
         * Gets the date format pattern configured for this context.
         *
         * @return The date format pattern, or null if not set
         */
        public String getDateFormat() {
            return dateFormat;
        }

        /**
         * Sets a date/time pattern or the special format {@code millis}, {@code unixtime}, or
         * {@code iso8601}. Null or an empty string clears the format and its cached formatter.
         * <details><summary>中文</summary>设置日期时间模式或 millis、unixtime、iso8601 特殊格式；null 或空字符串清除格式及缓存格式化器。</details>
         *
         * @param format The date format pattern to set
         */
        public void setDateFormat(String format) {
            formatyyyyMMddhhmmss19 = false;
            formatyyyyMMddhhmmssT19 = false;
            formatyyyyMMdd8 = false;
            yyyyMMddhhmm16 = false;
            formatComplex = false;
            if (format != null) {
                if (format.isEmpty()) {
                    format = null;
                }
            }

            boolean formatUnixTime = false, formatISO8601 = false, formatMillis = false, hasDay = false, hasHour = false, useSimpleFormatter = false;
            if (format != null) {
                switch (format) {
                    case "unixtime":
                        formatUnixTime = true;
                        break;
                    case "iso8601":
                        formatISO8601 = true;
                        break;
                    case "millis":
                        formatMillis = true;
                        break;
                    case "yyyyMMddHHmmssSSSZ":
                        useSimpleFormatter = true;
                        break;
                    case "yyyy-MM-dd HH:mm:ss":
                    case "yyyy-MM-ddTHH:mm:ss":
                        formatyyyyMMddhhmmss19 = true;
                        hasDay = true;
                        hasHour = true;
                        break;
                    case "yyyy-MM-dd'T'HH:mm:ss":
                        formatyyyyMMddhhmmssT19 = true;
                        hasDay = true;
                        hasHour = true;
                        break;
                    case "yyyyMMdd":
                    case "yyyy-MM-dd":
                        formatyyyyMMdd8 = true;
                        hasDay = true;
                        hasHour = false;
                        break;
                    case "yyyy-MM-dd HH:mm":
                        yyyyMMddhhmm16 = true;
                        hasDay = true;
                        hasHour = true;
                        break;
                    default:
                        hasDay = format.indexOf('d') != -1;
                        hasHour = format.indexOf('H') != -1
                                || format.indexOf('h') != -1
                                || format.indexOf('K') != -1
                                || format.indexOf('k') != -1;
                        break;
                }

                this.formatComplex = !(formatyyyyMMddhhmmss19 | formatyyyyMMddhhmmssT19 | formatyyyyMMdd8 | formatISO8601);
                // this.yyyyMMddhhmm16 = "yyyy-MM-dd HH:mm".equals(format);
            }

            if (!Objects.equals(this.dateFormat, format)) {
                this.dateFormatter = null;
            }
            this.dateFormat = format;
            this.formatUnixTime = formatUnixTime;
            this.formatMillis = formatMillis;
            this.formatISO8601 = formatISO8601;

            this.formatHasDay = hasDay;
            this.formatHasHour = hasHour;
            this.useSimpleFormatter = useSimpleFormatter;
        }

        /**
         * Gets the ZoneId configured for this context.
         *
         * @return The ZoneId
         */
        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DateUtils.DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        /**
         * Gets the features bitmask for this context.
         *
         * @return The features bitmask
         */
        public long getFeatures() {
            return features;
        }

        /**
         * Sets the features bitmask for this context.
         *
         * @param features The features bitmask to set
         * @since 2.0.51
         */
        public void setFeatures(long features) {
            this.features = features;
        }

        /**
         * Sets the ZoneId for this context.
         *
         * @param zoneId The ZoneId to set
         */
        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        /**
         * Gets the maximum nesting level allowed for this context.
         *
         * @return The maximum nesting level
         */
        public int getMaxLevel() {
            return maxLevel;
        }

        /**
         * Sets the maximum nesting level allowed for this context.
         *
         * @param maxLevel The maximum nesting level to set
         */
        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        /**
         * Gets the buffer size configured for this context.
         *
         * @return The buffer size in bytes
         */
        public int getBufferSize() {
            return bufferSize;
        }

        /**
         * Sets the buffer size for this context.
         *
         * @param bufferSize The buffer size to set in bytes
         * @return This Context instance for method chaining
         * @throws IllegalArgumentException if bufferSize is negative
         */
        public Context setBufferSize(int bufferSize) {
            if (bufferSize < 0) {
                throw new IllegalArgumentException("buffer size can not be less than zero");
            }
            this.bufferSize = bufferSize;
            return this;
        }

        /**
         * Gets the Locale configured for this context.
         *
         * @return The Locale
         */
        public Locale getLocale() {
            return locale;
        }

        /**
         * Sets the Locale for this context.
         *
         * @param locale The Locale to set
         */
        public void setLocale(Locale locale) {
            if (!Objects.equals(this.locale, locale)) {
                dateFormatter = null;
            }
            this.locale = locale;
        }

        /**
         * Gets the TimeZone configured for this context.
         *
         * @return The TimeZone
         */
        public TimeZone getTimeZone() {
            return timeZone;
        }

        /**
         * Sets the TimeZone for this context.
         *
         * @param timeZone The TimeZone to set
         */
        public void setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        /**
         * Configures features for this context.
         *
         * @param features The features to enable
         */
        public void config(Feature... features) {
            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        /**
         * Configures a filter and features for this context.
         *
         * @param filter The filter to configure
         * @param features The features to enable
         */
        public void config(Filter filter, Feature... features) {
            if (filter instanceof AutoTypeBeforeHandler) {
                autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
            }

            if (filter instanceof ExtraProcessor) {
                extraProcessor = (ExtraProcessor) filter;
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Configures a filter for this context.
         *
         * @param filter The filter to configure
         */
        public void config(Filter filter) {
            if (filter instanceof AutoTypeBeforeHandler) {
                autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
            }

            if (filter instanceof ExtraProcessor) {
                extraProcessor = (ExtraProcessor) filter;
            }
        }

        /**
         * Configures filters and features for this context.
         *
         * @param filters The filters to configure
         * @param features The features to enable
         */
        public void config(Filter[] filters, Feature... features) {
            for (Filter filter : filters) {
                if (filter instanceof AutoTypeBeforeHandler) {
                    autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
                }

                if (filter instanceof ExtraProcessor) {
                    extraProcessor = (ExtraProcessor) filter;
                }
            }

            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        /**
         * Configures filters for this context.
         *
         * @param filters The filters to configure
         */
        public void config(Filter[] filters) {
            for (Filter filter : filters) {
                if (filter instanceof AutoTypeBeforeHandler) {
                    autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
                }

                if (filter instanceof ExtraProcessor) {
                    extraProcessor = (ExtraProcessor) filter;
                }
            }
        }

        /**
         * Checks if the specified feature is enabled in this context.
         *
         * @param feature The feature to check
         * @return true if the feature is enabled, false otherwise
         */
        public boolean isEnabled(Feature feature) {
            return (this.features & feature.mask) != 0;
        }

        /**
         * Configures a specific feature for this context.
         *
         * @param feature The feature to configure
         * @param state true to enable the feature, false to disable it
         */
        public void config(Feature feature, boolean state) {
            if (state) {
                features |= feature.mask;
            } else {
                features &= ~feature.mask;
            }
        }
    }

    protected static final long MASK_FIELD_BASED = 1L;
    protected static final long MASK_IGNORE_NONE_SERIALIZABLE = 1L << 1;
    protected static final long MASK_ERROR_ON_NONE_SERIALIZABLE = 1L << 2;
    protected static final long MASK_SUPPORT_ARRAY_TO_BEAN = 1L << 3;
    protected static final long MASK_INIT_STRING_FIELD_AS_EMPTY = 1L << 4;
    protected static final long MASK_SUPPORT_AUTO_TYPE = 1L << 5;
    protected static final long MASK_SUPPORT_SMART_MATCH = 1L << 6;
    protected static final long MASK_TRIM_STRING = 1L << 14;
    protected static final long MASK_ALLOW_UN_QUOTED_FIELD_NAMES = 1L << 17;
    protected static final long MASK_EMPTY_STRING_AS_NULL = 1L << 27;
    protected static final long MASK_DISABLE_SINGLE_QUOTE = 1L << 31L;
    protected static final long MASK_DISABLE_REFERENCE_DETECT = 1L << 33;

    /**
     * Feature is used to control the behavior of JSON reading and parsing in FASTJSON2.
     * Each feature represents a specific configuration option that can be enabled or disabled
     * to customize how JSON data is processed during deserialization.
     *
     * <p>Features can be enabled in several ways:
     * <ul>
     *   <li>Using factory methods like {@link JSONReader#of(String, Context)} with {@link JSONFactory#createReadContext(JSONReader.Feature...)}</li>
     *   <li>Using {@link Context#config(Feature...)} method</li>
     *   <li>Using {@link JSONFactory#getDefaultReaderFeatures()} for global configuration</li>
     * </ul>
     *
     *
     * <p>Example usage:
     * <pre>
     * // Enable FieldBased feature for this reader only
     * try (JSONReader reader = JSONReader.of(json, JSONFactory.createReadContext(JSONReader.Feature.FieldBased))) {
     *     MyObject obj = reader.read(MyObject.class);
     * }
     *
     * // Enable multiple features
     * try (JSONReader reader = JSONReader.of(json,
     *         JSONFactory.createReadContext(
     *         JSONReader.Feature.FieldBased,
     *         JSONReader.Feature.TrimString))) {
     *     MyObject obj = reader.read(MyObject.class);
     * }
     *
     * // Using context configuration
     * JSONReader.Context context = new JSONReader.Context();
     * context.config(JSONReader.Feature.FieldBased);
     * try (JSONReader reader = JSONReader.of(json, context)) {
     *     MyObject obj = reader.read(MyObject.class);
     * }
     * </pre>
     *
     *
     * <p>Features are implemented as bitmask flags for efficient storage and checking.
     * Each feature has a unique mask value that is used internally to determine
     * whether the feature is enabled in a given configuration.</p>
     *
     * @see JSONReader.Context
     * @see JSONFactory
     * @since 2.0.0
     */
    public enum Feature {
        /**
         * Feature that determines whether to use field-based deserialization instead of getter/setter-based deserialization.
         * When enabled, fields are directly accessed rather than using getter and setter methods.
         * This can improve performance but may bypass validation logic in setters.
         *
         * <p>By default, this feature is disabled, meaning that getter/setter-based deserialization is used.</p>
         *
         * @since 2.0.0
         */
        FieldBased(MASK_FIELD_BASED),

        /**
         * Feature that determines whether to ignore non-serializable classes during deserialization.
         * When enabled, classes that do not implement {@link java.io.Serializable} will be ignored
         * rather than causing an exception to be thrown.
         *
         * <p>By default, this feature is disabled, meaning that non-serializable classes are not ignored.</p>
         *
         * @since 2.0.0
         */
        IgnoreNoneSerializable(MASK_IGNORE_NONE_SERIALIZABLE),

        /**
         * Feature that determines whether to throw an exception when encountering non-serializable classes
         * during deserialization.
         * When enabled, an exception will be thrown if a class does not implement {@link java.io.Serializable}.
         *
         * <p>By default, this feature is disabled, meaning that no exception is thrown for non-serializable classes.</p>
         *
         * @since 2.0.14
         */
        ErrorOnNoneSerializable(MASK_ERROR_ON_NONE_SERIALIZABLE),

        /**
         * Feature that determines whether to support deserializing JSON arrays into Java beans.
         * When enabled, JSON arrays can be mapped to Java bean properties, with each array element
         * corresponding to a property in the bean.
         *
         * <p>By default, this feature is disabled, meaning that array-to-bean conversion is not supported.</p>
         *
         * @since 2.0.0
         */
        SupportArrayToBean(MASK_SUPPORT_ARRAY_TO_BEAN),

        /**
         * Feature that determines whether to initialize string fields as empty strings instead of null values.
         * When enabled, string fields will be initialized with empty strings ("") rather than null.
         *
         * <p>By default, this feature is disabled, meaning that string fields are initialized with null values.</p>
         *
         * @since 2.0.0
         */
        InitStringFieldAsEmpty(MASK_INIT_STRING_FIELD_AS_EMPTY),

        /**
         * Feature that enables automatic type detection during deserialization.
         * It is not safe to explicitly turn on autoType, it is recommended to use AutoTypeBeforeHandler.
         *
         * <p>This feature is deprecated and should not be used in production code.</p>
         *
         * @deprecated It is not safe to explicitly turn on autoType, it is recommended to use AutoTypeBeforeHandler
         * @since 2.0.0
         */
        @Deprecated
        SupportAutoType(MASK_SUPPORT_AUTO_TYPE),

        /**
         * Feature that enables smart matching of field names during deserialization.
         * When enabled, field names in JSON can be matched to Java bean properties in a case-insensitive
         * manner or with other smart matching rules.
         *
         * <p>By default, this feature is disabled, meaning that exact field name matching is required.</p>
         *
         * @since 2.0.0
         */
        SupportSmartMatch(MASK_SUPPORT_SMART_MATCH),

        /**
         * Feature that determines whether to use native Java objects (HashMap, ArrayList) instead of
         * FASTJSON's JSONObject and JSONArray during deserialization.
         * When enabled, standard Java collections are used rather than FASTJSON-specific ones.
         *
         * <p>By default, this feature is disabled, meaning that FASTJSON's JSONObject and JSONArray are used.</p>
         *
         * @since 2.0.0
         */
        UseNativeObject(1 << 7),

        /**
         * Feature that enables support for Class.forName() during deserialization.
         * When enabled, the deserializer can use Class.forName() to load classes by name.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        SupportClassForName(1 << 8),

        /**
         * Feature that determines whether to ignore null values when setting properties.
         * When enabled, null values in JSON will not be set on Java bean properties,
         * preserving their default values.
         *
         * <p>By default, this feature is disabled, meaning that null values are set on properties.</p>
         *
         * @since 2.0.0
         */
        IgnoreSetNullValue(1 << 9),

        /**
         * Feature that determines whether to use default constructors as much as possible during deserialization.
         * When enabled, the deserializer will prefer to use default (no-argument) constructors when creating objects.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        UseDefaultConstructorAsPossible(1 << 10),

        /**
         * Feature that determines whether to deserialize floating-point numbers as BigDecimal
         * when the target type is float.
         * When enabled, float values will be represented with higher precision using BigDecimal.
         *
         * <p>By default, this feature is disabled, meaning that standard float precision is used.</p>
         *
         * @since 2.0.0
         */
        UseBigDecimalForFloats(1 << 11),

        /**
         * Feature that determines whether to deserialize floating-point numbers as BigDecimal
         * when the target type is double.
         * When enabled, double values will be represented with higher precision using BigDecimal.
         *
         * <p>By default, this feature is disabled, meaning that standard double precision is used.</p>
         *
         * @since 2.0.0
         */
        UseBigDecimalForDoubles(1 << 12),

        /**
         * Feature that determines whether to throw an exception when an enum value in JSON does not
         * match any of the defined enum constants.
         * When enabled, an exception will be thrown if an unknown enum value is encountered.
         *
         * <p>By default, this feature is disabled, meaning that unknown enum values are ignored.</p>
         *
         * @since 2.0.0
         */
        ErrorOnEnumNotMatch(1 << 13),

        /**
         * Feature that determines whether to trim whitespace from string values during deserialization.
         * When enabled, leading and trailing whitespace will be removed from string values.
         *
         * <p>By default, this feature is disabled, meaning that string values are not trimmed.</p>
         *
         * @since 2.0.0
         */
        TrimString(MASK_TRIM_STRING),

        /**
         * Feature that determines whether to throw an exception when autoType is not supported.
         * When enabled, an exception will be thrown if autoType functionality is not available.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        ErrorOnNotSupportAutoType(1 << 15),

        /**
         * Feature that determines how to handle duplicate keys in JSON objects.
         * When enabled, duplicate keys will be stored as arrays rather than overwriting previous values.
         *
         * <p>By default, this feature is disabled, meaning that duplicate keys overwrite previous values.</p>
         *
         * @since 2.0.0
         */
        DuplicateKeyValueAsArray(1 << 16),

        /**
         * Feature that determines whether to allow unquoted field names in JSON.
         * When enabled, field names in JSON objects do not need to be enclosed in quotes.
         *
         * <p>By default, this feature is disabled, meaning that field names must be quoted.</p>
         *
         * @since 2.0.0
         */
        AllowUnQuotedFieldNames(MASK_ALLOW_UN_QUOTED_FIELD_NAMES),

        /**
         * Feature that determines whether to treat non-string keys as strings during deserialization.
         * When enabled, keys in JSON objects that are not strings will be converted to string representation.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.0
         */
        NonStringKeyAsString(1 << 18),

        /**
         * Feature that determines whether to treat Base64-encoded strings as byte arrays during deserialization.
         * When enabled, strings that contain Base64-encoded data will be automatically decoded to byte arrays.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.13
         */
        Base64StringAsByteArray(1 << 19),

        /**
         * Skips the check that the complete text input has been consumed after parsing a value.
         * This permits trailing content; it does not change resource cleanup.
         * <details><summary>中文</summary>跳过解析后的输入结束检查，允许尾随内容，不影响资源释放。</details>
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.16
         */
        IgnoreCheckClose(1 << 20),

        /**
         * Feature that determines whether to throw an exception when null values are encountered
         * for primitive types during deserialization.
         * When enabled, an exception will be thrown if a null value is found for a primitive type field.
         *
         * <p>By default, this feature is disabled, meaning that primitive types are initialized with default values.</p>
         *
         * @since 2.0.20
         */
        ErrorOnNullForPrimitives(1 << 21),

        /**
         * Feature that determines whether to return null on error during deserialization.
         * When enabled, errors during deserialization will result in null values rather than exceptions.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.20
         */
        NullOnError(1 << 22),

        /**
         * Feature that determines whether to ignore autoType mismatches during deserialization.
         * When enabled, mismatches between expected and actual types in autoType scenarios will be ignored.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.21
         */
        IgnoreAutoTypeNotMatch(1 << 23),

        /**
         * Feature that determines whether to cast non-zero numbers to boolean true during deserialization.
         * When enabled, any non-zero numeric value will be treated as true when converting to boolean.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.24
         */
        NonZeroNumberCastToBooleanAsTrue(1 << 24),

        /**
         * Feature that determines whether to ignore null property values during deserialization.
         * When enabled, properties with null values in JSON will be ignored rather than set to null.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.40
         */
        IgnoreNullPropertyValue(1 << 25),

        /**
         * Feature that determines whether to throw an exception when unknown properties are encountered
         * during deserialization.
         * When enabled, an exception will be thrown if JSON contains properties that do not exist in the target class.
         *
         * <p>By default, this feature is disabled, meaning that unknown properties are ignored.</p>
         *
         * @since 2.0.42
         */
        ErrorOnUnknownProperties(1 << 26),

        /**
         * Feature that determines whether to convert empty strings to null values during deserialization.
         * When enabled, empty string values ("") in JSON will be converted to null values.
         *
         * <p>By default, this feature is disabled, meaning that empty strings are preserved as empty strings.</p>
         *
         * @since 2.0.48
         */
        EmptyStringAsNull(MASK_EMPTY_STRING_AS_NULL),

        /**
         * Feature that determines whether to avoid throwing exceptions on number overflow during deserialization.
         * When enabled, numeric overflow conditions will not cause exceptions to be thrown.
         *
         * <p>By default, this feature is disabled, meaning that number overflow will cause exceptions.</p>
         *
         * @since 2.0.48
         */
        NonErrorOnNumberOverflow(1 << 28),

        /**
         * Feature that determines whether JSON integral (non-floating-point)
         * numbers are to be deserialized into {@link java.math.BigInteger}s
         * if only generic type description (either {@link Object} or
         * {@link Number}, or within untyped {@link java.util.Map}
         * or {@link java.util.Collection} context) is available.
         * If enabled such values will be deserialized as
         * {@link java.math.BigInteger}s;
         * if disabled, will be deserialized as "smallest" available type,
         * which is either {@link Integer}, {@link Long} or
         * {@link java.math.BigInteger}, depending on number of digits.
         * <p>
         * Feature is disabled by default, meaning that "untyped" integral
         * numbers will by default be deserialized using whatever
         * is the most compact integral type, to optimize efficiency.
         * @since 2.0.51
         */
        UseBigIntegerForInts(1 << 29),

        /**
         * Feature that determines how "small" JSON integral (non-floating-point)
         * numbers -- ones that fit in 32-bit signed integer (`int`) -- are bound
         * when target type is loosely typed as {@link Object} or {@link Number}
         * (or within untyped {@link java.util.Map} or {@link java.util.Collection} context).
         * If enabled, such values will be deserialized as {@link java.lang.Long};
         * if disabled, they will be deserialized as "smallest" available type,
         * {@link Integer}.
         *<p>
         * Note: if {@link #UseBigIntegerForInts} is enabled, it has precedence
         * over this setting, forcing use of {@link java.math.BigInteger} for all
         * integral values.
         *<p>
         * Feature is disabled by default, meaning that "untyped" integral
         * numbers will by default be deserialized using {@link java.lang.Integer}
         * if value fits.
         *
         * @since 2.0.51
         */
        UseLongForInts(1 << 30),

        /**
         * Feature that disables the support for single quote.
         * When enabled, single quotes are not allowed as string delimiters in JSON.
         *
         * <p>By default, this feature is disabled, meaning that single quotes are supported.</p>
         *
         * @since 2.0.53
         */
        DisableSingleQuote(MASK_DISABLE_SINGLE_QUOTE),

        /**
         * Feature that determines whether to deserialize decimal numbers as double values.
         * When enabled, decimal values will be represented as double precision floating-point numbers.
         *
         * <p>By default, this feature is disabled.</p>
         *
         * @since 2.0.53
         */
        UseDoubleForDecimals(1L << 32L),

        /**
         * Feature that disables reference detection during deserialization.
         * When enabled, JSON references (such as those using $ref) will not be processed.
         *
         * <p>By default, this feature is disabled, meaning that reference detection is enabled.</p>
         *
         * @since 2.0.56
         */
        DisableReferenceDetect(MASK_DISABLE_REFERENCE_DETECT),

        /**
         * Feature that disables unwrapping of single-element string arrays to scalar values.
         * When enabled, JSON arrays containing a single string element will NOT be
         * unwrapped, and will instead be returned as arrays.
         * For example, ["value"] would be returned as ["value"] rather than "value".
         * @since 2.0.60
         */
        DisableStringArrayUnwrapping(1L << 34L);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }

        /**
         * Combines the masks of the specified features into a single bitmask.
         *
         * @param features The features to combine
         * @return A bitmask representing the combined features, or 0 if features is null
         */
        public static long of(Feature[] features) {
            if (features == null) {
                return 0;
            }

            long value = 0;

            for (Feature feature : features) {
                value |= feature.mask;
            }

            return value;
        }

        /**
         * Checks if this feature is enabled in the specified features bitmask.
         *
         * @param features The features bitmask to check
         * @return true if this feature is enabled, false otherwise
         */
        public boolean isEnabled(long features) {
            return (features & mask) != 0;
        }

        /**
         * Checks if the specified feature is enabled in the given features bitmask.
         *
         * @param features The features bitmask to check
         * @param feature The feature to check for
         * @return true if the specified feature is enabled, false otherwise
         */
        public static boolean isEnabled(long features, Feature feature) {
            return (features & feature.mask) != 0;
        }
    }

    static final class ResolveTask {
        final FieldReader fieldReader;
        final Object object;
        final Object name;
        final JSONPath reference;

        ResolveTask(FieldReader fieldReader, Object object, Object name, JSONPath reference) {
            this.fieldReader = fieldReader;
            this.object = object;
            this.name = name;
            this.reference = reference;
        }

        @Override
        public String toString() {
            return reference.toString();
        }
    }

    /**
     * Captures a lightweight cursor bookmark for this reader. It does not snapshot the context,
     * cached values, nesting level, or deferred reference-resolution tasks.
     * <details><summary>中文</summary>记录当前读取器的轻量游标书签，不保存上下文、缓存值、嵌套层级或延迟引用解析任务。</details>
     *
     * @return a cursor bookmark for use with reset
     */
    public SavePoint mark() {
        return new SavePoint(this.offset, this.ch);
    }

    /**
     * Restores a cursor bookmark created by this reader. This is not a rollback of all parser state.
     * <details><summary>中文</summary>恢复由当前读取器创建的游标书签；这并不回滚全部解析状态。</details>
     *
     * @param savePoint a bookmark previously obtained from this reader
     */
    public void reset(SavePoint savePoint) {
        this.offset = savePoint.offset;
        this.ch = (char) savePoint.current;
    }

    final boolean checkNameBegin(int quote) {
        long features = context.features;
        if (quote == '\'' && ((features & MASK_DISABLE_SINGLE_QUOTE) != 0)) {
            throw notSupportName();
        }
        if (quote != '"' && quote != '\'') {
            if ((features & MASK_ALLOW_UN_QUOTED_FIELD_NAMES) != 0) {
                readFieldNameHashCodeUnquote();
                return true;
            }
            throw notSupportName();
        }
        return false;
    }

    final JSONException notSupportName() {
        return new JSONException(info("not support unquoted name"));
    }

    final JSONException valueError() {
        return new JSONException(info("illegal value"));
    }

    final JSONException error(String message) {
        return new JSONException(info(message));
    }

    final JSONException error(String message, int ch) {
        return new JSONException(info(message).concat(Integer.toString(ch)));
    }

    final JSONException error(String message, Exception cause) {
        return new JSONException(info(message), cause);
    }

    final JSONException error() {
        throw new JSONValidException("error, offset " + offset + ", char " + (char) ch);
    }

    final JSONException error(int offset, int ch) {
        throw new JSONValidException("error, offset " + offset + ", char " + (char) ch);
    }

    static JSONException syntaxError(int ch) {
        return new JSONException("syntax error, expect ',', but '" + (char) ch + "'");
    }

    static JSONException syntaxError(int offset, int ch) {
        return new JSONException("syntax error, offset " + offset + ", char " + (char) ch);
    }

    static JSONException numberError(int offset, int ch) {
        return new JSONException("illegal number, offset " + offset + ", char " + (char) ch);
    }

    JSONException numberError() {
        return new JSONException("illegal number, offset " + offset + ", char " + ch);
    }

    public final String info() {
        return info(null);
    }

    public String info(String message) {
        if (message == null || message.isEmpty()) {
            return "offset " + offset;
        }
        return message + ", offset " + offset;
    }

    static boolean isFirstIdentifier(int ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_' || ch == '$' || (ch >= '0' && ch <= '9') || ch > 0x7F;
    }

    public static class SavePoint {
        protected final int offset;
        protected final int current;

        protected SavePoint(int offset, int current) {
            this.offset = offset;
            this.current = current;
        }
    }

    static final class BigIntegerCreator
            implements BiFunction<Integer, int[], BigInteger> {
        static final BiFunction<Integer, int[], BigInteger> BIG_INTEGER_CREATOR;

        static {
            BiFunction<Integer, int[], BigInteger> bigIntegerCreator = null;
            if (!ANDROID && !GRAAL) {
                try {
                    MethodHandles.Lookup caller = JDKUtils.trustedLookup(BigInteger.class);
                    MethodHandle handle = caller.findConstructor(
                            BigInteger.class, MethodType.methodType(void.class, int.class, int[].class)
                    );
                    CallSite callSite = LambdaMetafactory.metafactory(
                            caller,
                            "apply",
                            MethodType.methodType(BiFunction.class),
                            handle.type().generic(),
                            handle,
                            MethodType.methodType(BigInteger.class, Integer.class, int[].class)
                    );
                    bigIntegerCreator = (BiFunction<Integer, int[], BigInteger>) callSite.getTarget().invokeExact();
                } catch (Throwable ignored) {
                    // ignored
                }
            }
            if (bigIntegerCreator == null) {
                bigIntegerCreator = new BigIntegerCreator();
            }
            BIG_INTEGER_CREATOR = bigIntegerCreator;
        }

        @Override
        public BigInteger apply(Integer integer, int[] mag) {
            int signum = integer;
            final int bitLength;
            if (mag.length == 0) {
                bitLength = 0; // offset by one to initialize
            } else {
                // Calculate the bit length of the magnitude
                int bitLengthForInt = 32 - Integer.numberOfLeadingZeros(mag[0]);
                int magBitLength = ((mag.length - 1) << 5) + bitLengthForInt;
                if (signum < 0) {
                    // Check if magnitude is a power of two
                    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                    for (int i = 1; i < mag.length && pow2; i++) {
                        pow2 = (mag[i] == 0);
                    }
                    bitLength = (pow2 ? magBitLength - 1 : magBitLength);
                } else {
                    bitLength = magBitLength;
                }
            }
            int byteLen = bitLength / 8 + 1;

            byte[] bytes = new byte[byteLen];
            for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
                if (bytesCopied == 4) {
                    // nextInt = getInt(intIndex++
                    int n = intIndex++;
                    if (n < 0) {
                        nextInt = 0;
                    } else if (n >= mag.length) {
                        nextInt = signum < 0 ? -1 : 0;
                    } else {
                        int magInt = mag[mag.length - n - 1];
                        if (signum >= 0) {
                            nextInt = magInt;
                        } else {
                            int firstNonzeroIntNum;
                            {
                                int j;
                                int mlen = mag.length;
                                for (j = mlen - 1; j >= 0 && mag[j] == 0; j--) {
                                    // empty
                                }
                                firstNonzeroIntNum = mlen - j - 1;
                            }

                            if (n <= firstNonzeroIntNum) {
                                nextInt = -magInt;
                            } else {
                                nextInt = ~magInt;
                            }
                        }
                    }

                    bytesCopied = 1;
                } else {
                    nextInt >>>= 8;
                    bytesCopied++;
                }
                bytes[i] = (byte) nextInt;
            }

            return new BigInteger(bytes);
        }
    }

    /**
     * Gets an ObjectReader for the specified type hash, expected class, and features.
     * This method handles auto-type detection and applies any configured auto-type before handlers.
     *
     * @param typeHash The hash code of the type
     * @param expectClass The expected class
     * @param features Additional features to consider
     * @return An ObjectReader for the specified type, or null if not found
     */
    public ObjectReader getObjectReaderAutoType(long typeHash, Class expectClass, long features) {
        return context.getObjectReaderAutoType(getString(), expectClass, context.features | features);
    }

    protected final String readStringNotMatch() {
        switch (ch) {
            case '[':
                List array = readArray();
                if (array.size() == 1) {
                    Object item = array.get(0);
                    if (item == null) {
                        return null;
                    }
                    if ((context.features & Feature.DisableStringArrayUnwrapping.mask) == 0 && item instanceof String) {
                        return item.toString();
                    }
                }
                return toString(array);
            case '{':
                return toString(
                        readObject());
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                readNumber0();
                Number number = getNumber();
                return number.toString();
            case 't':
            case 'f':
                boolValue = readBoolValue();
                return boolValue ? "true" : "false";
            case 'n': {
                readNull();
                return null;
            }
            default:
                throw new JSONException(info("illegal input : " + ch));
        }
    }

    protected static String stringValue(String str, long features) {
        if ((features & MASK_TRIM_STRING) != 0) {
            str = str.trim();
        }
        if ((features & MASK_EMPTY_STRING_AS_NULL) != 0 && str.isEmpty()) {
            return null;
        }
        return str;
    }

    protected final long readFieldNameHashCodeError(int nameBegin, int ch) {
        if ((context.features & MASK_ALLOW_UN_QUOTED_FIELD_NAMES) != 0 && isFirstIdentifier(ch)) {
            return readFieldNameHashCodeUnquote();
        }
        if (ch == '}' || nextIfNull()) {
            return -1;
        }

        String errorMsg, preFieldName;
        if (ch == '[' && nameBegin > 0 && (preFieldName = getFieldName()) != null) {
            errorMsg = "illegal fieldName input " + ch + ", previous fieldName " + preFieldName;
        } else {
            errorMsg = "illegal fieldName input" + ch;
        }

        throw new JSONException(info(errorMsg));
    }
}

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

public abstract class JSONReader
        implements Closeable {
    static final int MAX_EXP = 2047;

    static final byte JSON_TYPE_INT = 1;
    static final byte JSON_TYPE_DEC = 2;
    static final byte JSON_TYPE_STRING = 3;
    static final byte JSON_TYPE_BOOL = 4;
    static final byte JSON_TYPE_NULL = 5;
    static final byte JSON_TYPE_OBJECT = 6;
    static final byte JSON_TYPE_ARRAY = 7;
    static final byte JSON_TYPE_BIG_DEC = 8;

    static final byte JSON_TYPE_INT8 = 9;
    static final byte JSON_TYPE_INT16 = 10;
    static final byte JSON_TYPE_INT64 = 11;
    static final byte JSON_TYPE_FLOAT = 12;
    static final byte JSON_TYPE_DOUBLE = 13;
    static final byte JSON_TYPE_NaN = 14;

    static final char EOI = 0x1A;
    static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    static final boolean[] INT_VALUE_END = new boolean[256];
    static {
        Arrays.fill(INT_VALUE_END, true);
        char[] chars = {'.', 'e', 'E', 't', 'f', 'n', '{', '[', '0', '1', '2', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char ch : chars) {
            INT_VALUE_END[ch] = false;
        }
    }

    protected final Context context;
    public final boolean jsonb;
    public final boolean utf8;

    List<ResolveTask> resolveTasks;

    protected int offset;
    protected char ch;
    protected boolean comma;

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

    public final char current() {
        return ch;
    }

    public boolean isEnd() {
        return ch == EOI;
    }

    public byte getType() {
        return -128;
    }

    public boolean isInt() {
        return ch == '-' || ch == '+' || (ch >= '0' && ch <= '9');
    }

    public abstract boolean isNull();

    public final boolean hasComma() {
        return comma;
    }

    public abstract Date readNullOrNewDate();

    public abstract boolean nextIfNull();

    public JSONReader(Context context, boolean jsonb, boolean utf8) {
        this.context = context;
        this.jsonb = jsonb;
        this.utf8 = utf8;
    }

    public final Context getContext() {
        return context;
    }

    public final void errorOnNoneSerializable(Class objectClass) {
        if ((context.features & MASK_ERROR_ON_NONE_SERIALIZABLE) != 0
                && !Serializable.class.isAssignableFrom(objectClass)) {
            throw new JSONException("not support none-Serializable, class ".concat(objectClass.getName()));
        }
    }

    public final boolean isEnabled(Feature feature) {
        return (context.features & feature.mask) != 0;
    }

    public final Locale getLocale() {
        return context.getLocale();
    }

    public final ZoneId getZoneId() {
        return context.getZoneId();
    }

    public final long features(long features) {
        return context.features | features;
    }

    public abstract int getRawInt();

    public abstract long getRawLong();

    public abstract boolean nextIfName4Match2();

    public boolean nextIfValue4Match2() {
        return false;
    }

    public abstract boolean nextIfName4Match3();

    public boolean nextIfValue4Match3() {
        return false;
    }

    public abstract boolean nextIfName4Match4(byte c4);

    public boolean nextIfValue4Match4(byte c4) {
        return false;
    }

    public abstract boolean nextIfName4Match5(int name1);

    public boolean nextIfValue4Match5(byte c4, byte c5) {
        return false;
    }

    public abstract boolean nextIfName4Match6(int name1);

    public boolean nextIfValue4Match6(int name1) {
        return false;
    }

    public abstract boolean nextIfName4Match7(int name1);

    public boolean nextIfValue4Match7(int name1) {
        return false;
    }

    public abstract boolean nextIfName4Match8(int name1, byte c8);

    public boolean nextIfValue4Match8(int name1, byte c8) {
        return false;
    }

    public abstract boolean nextIfName4Match9(long name1);

    public boolean nextIfValue4Match9(int name1, byte c8, byte c9) {
        return false;
    }

    public abstract boolean nextIfName4Match10(long name1);

    public boolean nextIfValue4Match10(long name1) {
        return false;
    }

    public abstract boolean nextIfName4Match11(long name1);

    public boolean nextIfValue4Match11(long name1) {
        return false;
    }

    public abstract boolean nextIfName4Match12(long name1, byte name2);

    public abstract boolean nextIfName4Match13(long name1, int name2);

    public boolean nextIfName4Match14(long name1, int name2) {
        return false;
    }

    public boolean nextIfName4Match15(long name1, int name2) {
        return false;
    }

    public abstract boolean nextIfName4Match16(long name1, int name2, byte name3);

    public abstract boolean nextIfName4Match17(long name1, long name2);

    public abstract boolean nextIfName4Match18(long name1, long name2);

    public boolean nextIfName4Match19(long name1, long name2) {
        return false;
    }

    public abstract boolean nextIfName4Match20(long name1, long name2, byte name3);

    public boolean nextIfName4Match21(long name1, long name2, int name3) {
        return false;
    }

    public abstract boolean nextIfName4Match22(long name1, long name2, int name3);

    public abstract boolean nextIfName4Match23(long name1, long name2, int name3);

    public abstract boolean nextIfName4Match24(long name1, long name2, int name3, byte name4);

    public abstract boolean nextIfName4Match25(long name1, long name2, long name3);

    public abstract boolean nextIfName4Match26(long name1, long name2, long name3);

    public abstract boolean nextIfName4Match27(long name1, long name2, long name3);

    public abstract boolean nextIfName4Match28(long name1, long name2, long name3, byte c28);

    public abstract boolean nextIfName4Match29(long name1, long name2, long name3, int name4);

    public abstract boolean nextIfName4Match30(long name1, long name2, long name3, int name4);

    public abstract boolean nextIfName4Match31(long name1, long name2, long name3, int name4);

    public abstract boolean nextIfName4Match32(long name1, long name2, long name3, int name4, byte c32);

    public abstract boolean nextIfName4Match33(long name1, long name2, long name3, long name4);

    public abstract boolean nextIfName4Match34(long name1, long name2, long name3, long name4);

    public abstract boolean nextIfName4Match35(long name1, long name2, long name3, long name4);

    public abstract boolean nextIfName4Match36(long name1, long name2, long name3, long name4, byte c35);

    public abstract boolean nextIfName4Match37(long name1, long name2, long name3, long name4, int name5);

    public abstract boolean nextIfName4Match38(long name1, long name2, long name3, long name4, int name5);

    public abstract boolean nextIfName4Match39(long name1, long name2, long name3, long name4, int name5);

    public abstract boolean nextIfName4Match40(long name1, long name2, long name3, long name4, int name5, byte c40);

    public abstract boolean nextIfName4Match41(long name1, long name2, long name3, long name4, long name5);

    public abstract boolean nextIfName4Match42(long name1, long name2, long name3, long name4, long name5);

    public abstract boolean nextIfName4Match43(long name1, long name2, long name3, long name4, long name5);

    public boolean nextIfName8Match0() {
        return false;
    }

    public boolean nextIfName8Match1() {
        return false;
    }

    public boolean nextIfName8Match2() {
        return false;
    }

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

    public final ObjectReader getObjectReader(Type type) {
        return context.provider.getObjectReader(type, (context.features & MASK_FIELD_BASED) != 0);
    }

    public final boolean isSupportSmartMatch() {
        return (context.features & MASK_SUPPORT_SMART_MATCH) != 0;
    }

    public final boolean isInitStringFieldAsEmpty() {
        return (context.features & MASK_INIT_STRING_FIELD_AS_EMPTY) != 0;
    }

    public final boolean isSupportSmartMatch(long features) {
        return ((context.features | features) & MASK_SUPPORT_SMART_MATCH) != 0;
    }

    public final boolean isSupportBeanArray() {
        return (context.features & MASK_SUPPORT_ARRAY_TO_BEAN) != 0;
    }

    public final boolean isSupportBeanArray(long features) {
        return ((context.features | features) & MASK_SUPPORT_ARRAY_TO_BEAN) != 0;
    }

    public final boolean isSupportAutoType(long features) {
        return ((context.features | features) & MASK_SUPPORT_AUTO_TYPE) != 0;
    }

    public final boolean isSupportAutoTypeOrHandler(long features) {
        return ((context.features | features) & MASK_SUPPORT_AUTO_TYPE) != 0 || context.autoTypeBeforeHandler != null;
    }

    public final boolean isJSONB() {
        return jsonb;
    }

    public final boolean isIgnoreNoneSerializable() {
        return (context.features & MASK_IGNORE_NONE_SERIALIZABLE) != 0;
    }

    public boolean hasAutoTypeBeforeHandler() {
        return context.autoTypeBeforeHandler != null;
    }

    public ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        return null;
    }

    final char char1(int c) {
        switch (c) {
            case '0':
                return '\0';
            case '1':
                return '\1';
            case '2':
                return '\2';
            case '3':
                return '\3';
            case '4':
                return '\4';
            case '5':
                return '\5';
            case '6':
                return '\6';
            case '7':
                return '\7';
            case 'b': // 8
                return '\b';
            case 't': // 9
                return '\t';
            case 'n': // 10
                return '\n';
            case 'v': // 11
                return '\u000B';
            case 'f': // 12
            case 'F':
                return '\f';
            case 'r': // 13
                return '\r';
            case '"': // 34
            case '\'': // 39
            case '/': // 47
            case '.': // 47
            case '\\': // 92
            case '#':
            case '&':
            case '[':
            case ']':
            case '@':
            case '(':
            case ')':
            case '_':
            case ',':
            case '~':
            case ' ':
                return (char) c;
            default:
                throw new JSONException(info("unclosed.str '\\" + (char) c));
        }
    }

    static char char2(int c1, int c2) {
        return (char) (DIGITS2[c1] * 0x10
                + DIGITS2[c2]);
    }

    public abstract boolean nextIfObjectStart();

    public abstract boolean nextIfNullOrEmptyString();

    public abstract boolean nextIfObjectEnd();

    public int startArray() {
        if (!nextIfArrayStart()) {
            throw new JSONException(info("illegal input, expect '[', but " + ch));
        }
        return Integer.MAX_VALUE;
    }

    public abstract boolean isReference();

    public abstract String readReference();

    public final boolean readReference(List list, int i) {
        if (!isReference()) {
            return false;
        }
        return readReference0(list, i);
    }

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

    public boolean isArray() {
        return this.ch == '[';
    }

    public boolean isObject() {
        return this.ch == '{';
    }

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

    public boolean isString() {
        return this.ch == '"' || this.ch == '\'';
    }

    public void endArray() {
        next();
    }

    public abstract boolean nextIfMatch(char ch);

    public abstract boolean nextIfComma();

    public abstract boolean nextIfArrayStart();

    public abstract boolean nextIfArrayEnd();

    public abstract boolean nextIfSet();

    public abstract boolean nextIfInfinity();

    public abstract String readPattern();

    public final int getOffset() {
        return offset;
    }

    public abstract void next();

    public void nextWithoutComment() {
        next();
    }

    public abstract long readValueHashCode();

    public long readTypeHashCode() {
        return readValueHashCode();
    }

    public abstract long readFieldNameHashCode();

    public abstract long getNameHashCodeLCase();

    public abstract String readFieldName();

    public abstract String getFieldName();

    public final void setTypeRedirect(boolean typeRedirect) {
        this.typeRedirect = typeRedirect;
    }

    public final boolean isTypeRedirect() {
        return typeRedirect;
    }

    public abstract long readFieldNameHashCodeUnquote();

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

    public abstract boolean skipName();

    public abstract void skipValue();

    public boolean isBinary() {
        return false;
    }

    public abstract byte[] readHex();

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

    public abstract int readInt32Value();

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

    public boolean nextIfMatch(byte type) {
        throw new JSONException("UnsupportedOperation");
    }

    public boolean nextIfMatchTypedAny() {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract boolean nextIfMatchIdent(char c0, char c1);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5);

    public final Byte readInt8() {
        Integer i = readInt32();
        if (i == null) {
            return null;
        }
        return i.byteValue();
    }

    public byte readInt8Value() {
        int i = readInt32Value();
        return (byte) i;
    }

    public final Short readInt16() {
        Integer i = readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }

    public short readInt16Value() {
        int i = readInt32Value();
        return (short) i;
    }

    public abstract Integer readInt32();

    protected final int readInt32ValueOverflow() {
        readNumber0();
        return getInt32Value();
    }

    protected final long readInt64ValueOverflow() {
        readNumber0();
        return getInt64Value();
    }

    public final int getInt32Value() {
        switch (valueType) {
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0) {
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

    public final long getInt64Value() {
        switch (valueType) {
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0) {
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
                return toInt((List) complex);
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

    public final double getDoubleValue() {
        switch (valueType) {
            case JSON_TYPE_NaN:
                return Double.NaN;
            case JSON_TYPE_INT8:
            case JSON_TYPE_INT16:
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return negative ? -mag3 : mag3;
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
                return number.doubleValue();
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

    public final float getFloatValue() {
        return (float) getDoubleValue();
    }

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

    public abstract long readInt64Value();

    public abstract Long readInt64();

    public abstract float readFloatValue();

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

    public abstract double readDoubleValue();

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

    public Number readNumber() {
        readNumber0();
        return getNumber();
    }

    public BigInteger readBigInteger() {
        readNumber0();
        return getBigInteger();
    }

    public abstract BigDecimal readBigDecimal();

    public abstract UUID readUUID();

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

    public LocalDateTime readLocalDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
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
            int len = getStringLength();
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
                i = dotnetDateStr.indexOf('-');
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

    public Date readDate() {
        if (isInt()) {
            long millis = readInt64Value();
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

    protected abstract int getStringLength();

    public boolean isDate() {
        return false;
    }

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

    public final long readMillisFromString() {
        wasNull = false;
        String format = context.dateFormat;
        if (format == null
                || context.formatyyyyMMddhhmmss19
                || context.formatyyyyMMddhhmmssT19
                || context.formatyyyyMMdd8
                || context.formatISO8601) {
            int len = getStringLength();
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
                i = dotnetDateStr.indexOf('-');
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

    public void readNumber(ValueConsumer consumer, boolean quoted) {
        readNumber0();
        Number number = getNumber();
        consumer.accept(number);
    }

    public void readString(ValueConsumer consumer, boolean quoted) {
        String str = readString(); //
        if (quoted) {
            consumer.accept(JSON.toJSONString(str));
        } else {
            consumer.accept(str);
        }
    }

    protected abstract void readNumber0();

    public abstract String readString();

    public byte[] readBase64() {
        String str = readString();
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

    public char readCharValue() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return str.charAt(0);
    }

    public Character readCharacter() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return str.charAt(0);
    }

    public abstract void readNull();

    protected double readNaN() {
        throw new JSONException("not support");
    }

    public abstract boolean readIfNull();

    public abstract String getString();

    public boolean wasNull() {
        return wasNull;
    }

    public <T> T read(Type type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

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

    public final void readObject(Object object, Feature... features) {
        long featuresLong = 0;
        for (Feature feature : features) {
            featuresLong |= feature.mask;
        }
        readObject(object, featuresLong);
    }

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

    public <T> T read(Class<T> type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, null, null, 0);
    }

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

    public abstract void skipComment();

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

    public abstract boolean readBoolValue();

    public Object readAny() {
        return read(Object.class);
    }

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

    public final void readArray(List list, Type itemType) {
        readArray((Collection) list, itemType);
    }

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

    public final JSONArray readJSONArray() {
        JSONArray array = new JSONArray();
        read(array);
        return array;
    }

    public final JSONObject readJSONObject() {
        JSONObject object = new JSONObject();
        read(object, 0L);
        return object;
    }

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

    public final BigInteger getBigInteger() {
        Number number = getNumber();

        if (number == null) {
            return null;
        }

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        return BigInteger.valueOf(number.longValue());
    }

    public final BigDecimal getBigDecimal() {
        if (wasNull) {
            return null;
        }

        switch (valueType) {
            case JSON_TYPE_INT: {
                if (mag1 == 0 && mag2 == 0 && mag3 >= 0) {
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
                    String doubleStr = decimal.toPlainString() + "E" + exponent;
                    double doubleValue = Double.parseDouble(doubleStr);
                    return toBigDecimal(doubleValue);
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

                if (mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                    } else {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + v3;
                            long unscaledVal = negative ? -v23 : v23;
                            decimal = BigDecimal.valueOf(unscaledVal, scale);
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
                    return decimal.signum() == 0 ? BigDecimal.ZERO : new BigDecimal(decimalStr + "E" + exponent);
                }

                if ((context.features & Feature.UseDoubleForDecimals.mask) != 0) {
                    return decimal.doubleValue();
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                if (scale > 0) {
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
            return ((Number) val).intValue();
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

    public static JSONReader of(URL url, Context context) throws IOException {
        try (InputStream is = url.openStream()) {
            return of(is, StandardCharsets.UTF_8, context);
        }
    }

    public static JSONReader of(InputStream is, Charset charset) {
        Context context = JSONFactory.createReadContext();
        return of(is, charset, context);
    }

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

    public static JSONReader of(Reader is) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext(),
                is
        );
    }

    public static JSONReader of(Reader is, Context context) {
        return new JSONReaderUTF16(
                context,
                is
        );
    }

    public static JSONReader of(ByteBuffer buffer, Charset charset) {
        Context context = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, buffer);
        }

        throw new JSONException("not support charset " + charset);
    }

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

    public static AutoTypeBeforeHandler autoTypeFilter(String... names) {
        return new ContextAutoTypeBeforeHandler(names);
    }

    public static AutoTypeBeforeHandler autoTypeFilter(boolean includeBasic, String... names) {
        return new ContextAutoTypeBeforeHandler(includeBasic, names);
    }

    public static AutoTypeBeforeHandler autoTypeFilter(Class... types) {
        return new ContextAutoTypeBeforeHandler(types);
    }

    public static AutoTypeBeforeHandler autoTypeFilter(boolean includeBasic, Class... types) {
        return new ContextAutoTypeBeforeHandler(includeBasic, types);
    }

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

        public boolean isFormatUnixTime() {
            return formatUnixTime;
        }

        public boolean isFormatyyyyMMddhhmmss19() {
            return formatyyyyMMddhhmmss19;
        }

        public boolean isFormatyyyyMMddhhmmssT19() {
            return formatyyyyMMddhhmmssT19;
        }

        public boolean isFormatyyyyMMdd8() {
            return formatyyyyMMdd8;
        }

        public boolean isFormatMillis() {
            return formatMillis;
        }

        public boolean isFormatISO8601() {
            return formatISO8601;
        }

        public boolean isFormatHasHour() {
            return formatHasHour;
        }

        public ObjectReader getObjectReader(Type type) {
            boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
            return provider.getObjectReader(type, fieldBased);
        }

        public ObjectReaderProvider getProvider() {
            return provider;
        }

        public ObjectReader getObjectReaderAutoType(long hashCode) {
            return provider.getObjectReader(hashCode);
        }

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

        public AutoTypeBeforeHandler getContextAutoTypeBeforeHandler() {
            return autoTypeBeforeHandler;
        }

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

        public ExtraProcessor getExtraProcessor() {
            return extraProcessor;
        }

        public void setExtraProcessor(ExtraProcessor extraProcessor) {
            this.extraProcessor = extraProcessor;
        }

        public Supplier<Map> getObjectSupplier() {
            return objectSupplier;
        }

        public void setObjectSupplier(Supplier<Map> objectSupplier) {
            this.objectSupplier = objectSupplier;
        }

        public Supplier<List> getArraySupplier() {
            return arraySupplier;
        }

        public void setArraySupplier(Supplier<List> arraySupplier) {
            this.arraySupplier = arraySupplier;
        }

        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !formatMillis && !formatISO8601 && !formatUnixTime) {
                dateFormatter = locale == null
                        ? DateTimeFormatter.ofPattern(dateFormat)
                        : DateTimeFormatter.ofPattern(dateFormat, locale);
            }
            return dateFormatter;
        }

        public void setDateFormatter(DateTimeFormatter dateFormatter) {
            this.dateFormatter = dateFormatter;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(String format) {
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

        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DateUtils.DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        public long getFeatures() {
            return features;
        }

        /**
         * @since 2.0.51
         */
        public void setFeatures(long features) {
            this.features = features;
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public Context setBufferSize(int bufferSize) {
            if (bufferSize < 0) {
                throw new IllegalArgumentException("buffer size can not be less than zero");
            }
            this.bufferSize = bufferSize;
            return this;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        public void config(Feature... features) {
            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

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

        public void config(Filter filter) {
            if (filter instanceof AutoTypeBeforeHandler) {
                autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
            }

            if (filter instanceof ExtraProcessor) {
                extraProcessor = (ExtraProcessor) filter;
            }
        }

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

        public boolean isEnabled(Feature feature) {
            return (this.features & feature.mask) != 0;
        }

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

    public enum Feature {
        FieldBased(MASK_FIELD_BASED),
        IgnoreNoneSerializable(MASK_IGNORE_NONE_SERIALIZABLE),
        /**
         * @since 2.0.14
         */
        ErrorOnNoneSerializable(MASK_ERROR_ON_NONE_SERIALIZABLE),
        SupportArrayToBean(MASK_SUPPORT_ARRAY_TO_BEAN),
        InitStringFieldAsEmpty(MASK_INIT_STRING_FIELD_AS_EMPTY),
        /**
         * It is not safe to explicitly turn on autoType, it is recommended to use AutoTypeBeforeHandler
         */
        @Deprecated
        SupportAutoType(MASK_SUPPORT_AUTO_TYPE),
        SupportSmartMatch(MASK_SUPPORT_SMART_MATCH),
        UseNativeObject(1 << 7),
        SupportClassForName(1 << 8),
        IgnoreSetNullValue(1 << 9),
        UseDefaultConstructorAsPossible(1 << 10),
        UseBigDecimalForFloats(1 << 11),
        UseBigDecimalForDoubles(1 << 12),
        ErrorOnEnumNotMatch(1 << 13),
        TrimString(MASK_TRIM_STRING),
        ErrorOnNotSupportAutoType(1 << 15),
        DuplicateKeyValueAsArray(1 << 16),
        AllowUnQuotedFieldNames(MASK_ALLOW_UN_QUOTED_FIELD_NAMES),
        NonStringKeyAsString(1 << 18),
        /**
         * @since 2.0.13
         */
        Base64StringAsByteArray(1 << 19),

        /**
         * @since 2.0.16
         */
        IgnoreCheckClose(1 << 20),
        /**
         * @since 2.0.20
         */
        ErrorOnNullForPrimitives(1 << 21),

        /**
         * @since 2.0.20
         */
        NullOnError(1 << 22),

        /**
         * @since 2.0.21
         */
        IgnoreAutoTypeNotMatch(1 << 23),

        /**
         * @since 2.0.24
         */
        NonZeroNumberCastToBooleanAsTrue(1 << 24),

        /**
         * @since 2.0.40
         */
        IgnoreNullPropertyValue(1 << 25),

        /**
         * @since 2.0.42
         */
        ErrorOnUnknownProperties(1 << 26),

        /**
         * empty string "" convert to null
         *
         * @since 2.0.48
         */
        EmptyStringAsNull(MASK_EMPTY_STRING_AS_NULL),

        /**
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
         * @since 2.0.53
         */
        DisableSingleQuote(MASK_DISABLE_SINGLE_QUOTE),

        /**
         * @since 2.0.53
         */
        UseDoubleForDecimals(1L << 32L),

        /**
         * @since 2.0.56
         */
        DisableReferenceDetect(MASK_DISABLE_REFERENCE_DETECT);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }

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

        public boolean isEnabled(long features) {
            return (features & mask) != 0;
        }

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

    public SavePoint mark() {
        return new SavePoint(this.offset, this.ch);
    }

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

    public ObjectReader getObjectReaderAutoType(long typeHash, Class expectClass, long features) {
        ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
        if (autoTypeObjectReader != null) {
            return autoTypeObjectReader;
        }

        String typeName = getString();
        if (context.autoTypeBeforeHandler != null) {
            Class<?> autoTypeClass = context.autoTypeBeforeHandler.apply(typeName, expectClass, features);
            if (autoTypeClass != null) {
                boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
                return context.provider.getObjectReader(autoTypeClass, fieldBased);
            }
        }

        return context.provider.getObjectReader(typeName, expectClass, context.features | features);
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
                    if (item instanceof String) {
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
}

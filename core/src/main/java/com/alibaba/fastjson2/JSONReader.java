package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.function.BiFunction;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.function.Supplier;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.time.*;
import com.alibaba.fastjson2.util.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReader.BigIntegerCreator.BIG_INTEGER_CREATOR;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

public abstract class JSONReader
        implements Closeable {
    static final int MAX_EXP = 1023;

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

    static final char EOI = 0x1A;
    static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    public final Context context;
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
    protected byte scale;

    protected int mag0;
    protected int mag1;
    protected int mag2;
    protected int mag3;

    protected int level;

    protected String stringValue;
    protected Object complex; // Map | List

    protected boolean typeRedirect; // redirect for {"@type":"xxx"",...

    protected char[] doubleChars;
    public final boolean jsonb;

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

    public JSONReader(Context context, boolean jsonb) {
        this.context = context;
        this.jsonb = jsonb;
    }

    public final Context getContext() {
        return context;
    }

    public final void errorOnNoneSerializable(Class objectClass) {
        if ((context.features & Feature.ErrorOnNoneSerializable.mask) != 0
                && !Serializable.class.isAssignableFrom(objectClass)) {
            throw new JSONException("not support none-Serializable, class " + objectClass.getName());
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

    public final void handleResolveTasks(Object root) {
        if (resolveTasks == null) {
            return;
        }

        Object previous = null;
        for (int i = 0; i < resolveTasks.size(); i++) {
            ResolveTask resolveTask = resolveTasks.get(i);
            JSONPath path = resolveTask.reference;
            FieldReader fieldReader = resolveTask.fieldReader;

            Object fieldValue;
            if (path.isPrevious()) {
                fieldValue = previous;
            } else {
                if (!path.isRef()) {
                    throw new JSONException("reference path invalid : " + path);
                }
                path.readerContext = context;
                if ((context.features & Feature.FieldBased.mask) != 0) {
                    JSONWriter.Context writeContext = JSONFactory.createWriteContext();
                    writeContext.features |= JSONWriter.Feature.FieldBased.mask;
                    path.writerContext = writeContext;
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
                            list.set(index, fieldValue);
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
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        return context.provider.getObjectReader(type, fieldBased);
    }

    public final boolean isSupportSmartMatch() {
        return (context.features & Feature.SupportSmartMatch.mask) != 0;
    }

    public final boolean isInitStringFieldAsEmpty() {
        return (context.features & Feature.InitStringFieldAsEmpty.mask) != 0;
    }

    public final boolean isSupportSmartMatch(long features) {
        return ((context.features | features) & Feature.SupportSmartMatch.mask) != 0;
    }

    public final boolean isSupportBeanArray() {
        return (context.features & Feature.SupportArrayToBean.mask) != 0;
    }

    public final boolean isSupportBeanArray(long features) {
        return ((context.features | features) & Feature.SupportArrayToBean.mask) != 0;
    }

    public final boolean isSupportAutoType(long features) {
        return ((context.features | features) & Feature.SupportAutoType.mask) != 0;
    }

    public final boolean isSupportAutoTypeOrHandler(long features) {
        return ((context.features | features) & Feature.SupportAutoType.mask) != 0 || context.autoTypeBeforeHandler != null;
    }

    public final boolean isJSONB() {
        return jsonb;
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
                return (char) c;
            default:
                throw new JSONException(info("unclosed.str '\\" + (char) c));
        }
    }

    static char char2(int c1, int c2) {
        return (char) (DIGITS2[c1] * 0x10
                + DIGITS2[c2]);
    }

    static char char4(int c1, int c2, int c3, int c4) {
        return (char) (DIGITS2[c1] * 0x1000
                + DIGITS2[c2] * 0x100
                + DIGITS2[c3] * 0x10
                + DIGITS2[c4]);
    }

    public abstract boolean nextIfObjectStart();
    public abstract boolean nextIfNullOrEmptyString();
    public abstract boolean nextIfObjectEnd();

    public abstract boolean nextIfArrayStart();
    public abstract boolean nextIfArrayEnd();

    public abstract boolean nextIfComma();

    public int startArray() {
        if (!nextIfArrayStart()) {
            throw new JSONException(info("illegal input, expect '[', but " + ch));
        }
        return Integer.MAX_VALUE;
    }

    public abstract boolean isReference();

    public abstract String readReference();

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

    public abstract boolean isArray();

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

    public abstract boolean nextIfSet();

    public abstract boolean nextIfInfinity();

    public abstract String readPattern();

    public final int getOffset() {
        return offset;
    }

    public abstract void next();

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
        readFieldNameHashCodeUnquote();
        return getFieldName();
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
                return IOUtils.decodeBase64(str);
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

    public boolean nextIfMatch(byte type) {
        throw new JSONException("UnsupportedOperation");
    }

    public boolean nextIfMatchTypedAny() {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4);

    public abstract boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5);

    public abstract Integer readInt32();

    public final int getInt32Value() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return negative ? -mag3 : mag3;
                }
                Number number = getNumber();
                if (number instanceof Long) {
                    long longValue = number.longValue();
                    if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                        throw new JSONException(info("integer overflow " + longValue));
                    }
                    return (int) longValue;
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
            case JSON_TYPE_ARRAY: {
                return toInt((List) complex);
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    public final long getInt64Value() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return negative ? -mag3 : mag3;
                }
                return getNumber().longValue();
            case JSON_TYPE_DEC:
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
            default:
                throw new JSONException("TODO");
        }
    }

    protected final Long getInt64() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return Long.valueOf(negative ? -mag3 : mag3);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        if (mag2 == Integer.MIN_VALUE && mag3 == 0 && !negative) {
                            return Long.MIN_VALUE;
                        }

                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
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
                return bigInt.longValue();
            case JSON_TYPE_DEC:
                return getNumber().longValue();
            case JSON_TYPE_BOOL:
                return Long.valueOf(boolValue ? 1 : 0);
            case JSON_TYPE_NULL:
                return null;
            case JSON_TYPE_STRING: {
                return toInt64(stringValue);
            }
            case JSON_TYPE_OBJECT: {
                Number num = toNumber((Map) complex);
                if (num != null) {
                    return num.longValue();
                }
                return null;
            }
            default:
                throw new JSONException("TODO");
        }
    }

    public long[] readInt64ValueArray() {
        if (nextIfNull()) {
            return null;
        }

        if (nextIfArrayStart()) {
            long[] values = new long[8];
            int size = 0;
            for (; ; ) {
                if (nextIfArrayEnd()) {
                    break;
                }

                if (isEnd()) {
                    throw new JSONException(info("input end"));
                }

                if (size == values.length) {
                    values = Arrays.copyOf(values, values.length << 1);
                }

                values[size++] = readInt64Value();
            }
            nextIfComma();

            long[] array;
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

    public abstract long readInt64Value();

    public abstract Long readInt64();

    public abstract float readFloatValue();

    public final Float readFloat() {
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

    public final boolean isLocalDate() {
        if (!isString()) {
            return false;
        }

        LocalDate localDate;
        int len = getStringLength();
        switch (len) {
            case 8:
                localDate = readLocalDate8();
                break;
            case 9:
                localDate = readLocalDate9();
                break;
            case 10:
                localDate = readLocalDate10();
                break;
            case 11:
                localDate = readLocalDate11();
                break;
            default:
                return false;
        }

        return localDate != null;
    }

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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
            return zdt.dateTime.date;
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
                return ldt.date;
            }
        }

        String str = readString();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }

        DateTimeFormatter formatter = context.getDateFormatter();
        if (formatter != null) {
            if (context.formatHasHour) {
                return formatter.parseLocalDateTime(str)
                        .date;
            }
            return formatter.parseLocalDate(str);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
            return zdt.dateTime.date;
        }

        throw new JSONException("not support input : " + str);
    }

    public final boolean isLocalDateTime() {
        if (!isString()) {
            return false;
        }

        int len = getStringLength();
        switch (len) {
            case 16:
                return readLocalDateTime16() != null;
            case 17:
                return readLocalDateTime17() != null;
            case 18:
                return readLocalDateTime18() != null;
            case 19:
                return readLocalDateTime19() != null;
            case 20:
                return readLocalDateTime20() != null;
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return readLocalDateTimeX(len) != null;
            default:
                break;
        }
        return false;
    }

    public LocalDateTime readLocalDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
            return zdt.dateTime;
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
                        return zdt.dateTime;
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
                        if (!zdt.zone.equals(contextZoneId)) {
                            ldt = ZonedDateTime.ofInstant(zdt.toInstant(), contextZoneId).dateTime;
                        } else {
                            ldt = zdt.dateTime;
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
                        formatter.parseLocalDate(str),
                        LocalTime.MIN
                );
            }
            return formatter.parseLocalDateTime(str);
        }

        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);

            if (context.formatUnixTime) {
                millis *= 1000L;
            }

            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        if (str.startsWith("/Date(", 0) && str.endsWith(")/")) {
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

        if (str.equals("0000-00-00 00:00:00")) {
            wasNull = true;
            return null;
        }
        throw new JSONException(info("read LocalDateTime error " + str));
    }

    public ZonedDateTime readZonedDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000L;
            }
            Instant instant = Instant.ofEpochMilli(millis);
            return ZonedDateTime.ofInstant(instant, context.getZoneId());
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
                            context.getZoneId()
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
                    LocalDate localDate = formatter.parseLocalDate(str);
                    return ZonedDateTime.of(localDate, LocalTime.MIN, context.getZoneId());
                }
                LocalDateTime localDateTime = formatter.parseLocalDateTime(str);
                return ZonedDateTime.of(localDateTime, context.getZoneId());
            }

            if (IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (context.formatUnixTime) {
                    millis *= 1000L;
                }
                Instant instant = Instant.ofEpochMilli(millis);
                return ZonedDateTime.ofInstant(instant, context.getZoneId());
            }

            return DateUtils.parseZonedDateTime(str);
        }
        throw new JSONException("TODO : " + ch);
    }

    public LocalTime readLocalTime() {
        if (nextIfNull()) {
            return null;
        }

        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
            return zdt.dateTime.time;
        }

        int len = getStringLength();
        switch (len) {
            case 5:
                return readLocalTime5();
            case 8:
                return readLocalTime8();
            case 10:
                return readLocalTime10();
            case 11:
                return readLocalTime11();
            case 12:
                return readLocalTime12();
            case 18:
                return readLocalTime18();
            case 19:
                return readLocalDateTime19()
                        .time;
            case 20:
                return readLocalDateTime20()
                        .time;
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());
            return zdt.dateTime.time;
        }

        throw new JSONException("not support len : " + len);
    }

    protected abstract int getStringLength();

    public Instant readInstant() {
        if (nextIfNullOrEmptyString()) {
            wasNull = true;
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
                zdt.dateTime.time.nano);
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
                zdt = ZonedDateTime.ofLocal(ldt, context.getZoneId());
            } else if (len >= 20) {
                zdt = readZonedDateTimeX(len);
                if (zdt == null && (len >= 32 && len <= 35)) {
                    String str = readString();
                    zdt = DateUtils.parseZonedDateTime(str, null);
                }
            }

            if (zdt != null) {
                long seconds = zdt.toEpochSecond();
                int nanos = zdt.dateTime.time.nano;
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

                return DateUtils.parseMillis19(str, null);
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

        if (str.startsWith("/Date(", 0) && str.endsWith(")/")) {
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

    protected abstract LocalTime readLocalTime8();

    protected abstract LocalTime readLocalTime10();

    protected abstract LocalTime readLocalTime11();

    protected abstract LocalTime readLocalTime12();

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

    public Date readDate() {
        if ((context.formatMillis || context.formatUnixTime || context.dateFormat == null) && isInt()) {
            long millis = readInt64Value();
            if (context.formatUnixTime) {
                millis *= 1000;
            }
            return new Date(millis);
        }

        String str = readString();
        return DateUtils.parseDate(str, context.dateFormat, context.getZoneId());
    }

    public Calendar readCalendar() {
        Date date = readDate();
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance(context.getTimeZone());
        calendar.setTime(date);
        return calendar;
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
            nextIfComma();

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

    public abstract void readNull();

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

            if (nextIfComma()) {
                continue;
            }
        }

        nextIfComma();
    }

    public final void readObject(Object object, Feature... features) {
        long featuresLong = 0;
        for (int i = 0; i < features.length; i++) {
            featuresLong |= features[i].mask;
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

    public void read(Map object, long features) {
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
                throw new JSONException("illegal input， offset " + offset + ", char " + ch);
            }
        }

        long contextFeatures = features | context.features;

        for_:
        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipLineComment();
            }

            if (nextIfObjectEnd()) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            Object name;
            if (match || typeRedirect) {
                if (ch >= '1' && ch <= '9') {
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
                        skipLineComment();
                    } else {
                        throw new JSONException("FASTJSON" + JSON.VERSION + "input not support " + ch + ", offset " + offset);
                    }
                    continue for_;
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

            Object origin = object.put(name, value);
            if (origin != null) {
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
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

    public final void read(Map object, Type keyType, Type valueType, long features) {
        boolean match = nextIfObjectStart();
        if (!match) {
            throw new JSONException("illegal input， offset " + offset + ", char " + ch);
        }

        ObjectReader keyReader = context.getObjectReader(keyType);
        ObjectReader valueReader = context.getObjectReader(valueType);

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipLineComment();
            }

            if (nextIfMatch('}')) {
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
            Object origin = object.put(name, value);
            if (origin != null) {
                long contextFeatures = features | context.getFeatures();
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
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

        Map object;
        if (context.objectSupplier == null) {
            if ((context.features & Feature.UseNativeObject.mask) != 0) {
                object = new HashMap();
            } else {
                object = new JSONObject();
            }
        } else {
            object = context.objectSupplier.get();
        }

        for_:
        for (int i = 0; ; ++i) {
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
                    val = readObject();
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
                    readNull();
                    val = null;
                    break;
                case '/':
                    next();
                    if (ch == '/') {
                        skipLineComment();
                    }
                    continue for_;
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
            object.put(name, val);
        }

        if (comma = (ch == ',')) {
            next();
        }

        level--;

        return object;
    }

    public abstract void skipLineComment();

    public final Boolean readBool() {
        if (isNull()) {
            readNull();
            return null;
        }

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

        if (!nextIfArrayStart()) {
            throw new JSONException(info("syntax error : " + ch));
        }

        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(itemType, fieldBased);

        List list = new ArrayList();
        for (Object item; !nextIfArrayEnd(); list.add(item)) {
            int mark = offset;
            item = objectReader.readObject(this, null, null, 0);

            if (mark == offset || ch == '}' || ch == EOI) {
                throw new JSONException("illegal input : " + ch + ", offset " + getOffset());
            }
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

        boolean arrayEnd = false;
        Object[] list = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            if (i != 0) {
                if (nextIfArrayEnd()) {
                    arrayEnd = true;
                    break;
                } else if (isEnd()) {
                    break;
                }
            }

            Type itemType = types[i];
            Object item = read(itemType);
            list[i] = item;

            if (i == types.length - 1) {
                arrayEnd = true;
            }
        }

        if (!arrayEnd) {
            throw new JSONException(info("syntax error"));
        }
        return list;
    }

    public final void readArray(List list, Type itemType) {
        readArray((Collection) list, itemType);
    }

    public final void readArray(Collection list, Type itemType) {
        if (nextIfArrayStart()) {
            for (; ; ) {
                if (nextIfArrayEnd()) {
                    break;
                }
                Object item = read(itemType);
                list.add(item);

                if (ch == '}' || ch == EOI) {
                    throw new JSONException(info());
                }
            }

            if (comma = (ch == ',')) {
                next();
            }
            return;
        }

        if (isString()) {
            String str = readString();
            if (itemType == String.class) {
                list.add(str);
            } else {
                Function typeConvert = context.provider.getTypeConvert(String.class, itemType);
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
                case '/':
                    skipLineComment();
                    continue _for;
                default:
                    throw new JSONException("TODO : " + ch);
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

                list.add(first);
                list.add(second);
                list.add(val);
            } else {
                list.add(val);
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
                list.add(first);
            } else if (i == 2) {
                list.add(first);
                list.add(second);
            }
        }

        if (comma = (ch == ',')) {
            next();
        }

        level--;

        return list;
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
                    int intVlaue;
                    if (negative) {
                        if (mag3 < 0) {
                            return -(mag3 & 0xFFFFFFFFL);
                        }
                        intVlaue = -mag3;
                    } else {
                        if (mag3 < 0) {
                            return mag3 & 0xFFFFFFFFL;
                        }
                        intVlaue = mag3;
                    }
                    if (valueType == JSON_TYPE_INT64) {
                        return Long.valueOf(intVlaue);
                    }
                    return Integer.valueOf(intVlaue);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            return negative ? -v23 : v23;
                        }
                        mag = new int[]{mag2, mag3};
                    } else {
                        mag = new int[]{mag1, mag2, mag3};
                    }
                } else {
                    mag = new int[]{mag0, mag1, mag2, mag3};
                }

                int signum = negative ? -1 : 1;
                return BIG_INTEGER_CREATOR.apply(signum, mag);
            }
            case JSON_TYPE_INT16: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return Short.valueOf((short) intValue);
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_INT8: {
                if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 >= 0) {
                    int intValue = negative ? -mag3 : mag3;
                    return Byte.valueOf((byte) intValue);
                }
                throw new JSONException(info("shortValue overflow"));
            }
            case JSON_TYPE_DEC: {
                BigDecimal decimal = null;

                if (mag0 == 0 && mag1 == 0) {
                    if (mag2 == 0 && mag3 >= 0) {
                        int unscaledVal = negative ? -mag3 : mag3;

                        if (exponent == 0) {
                            if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                                switch (scale) {
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    case 10:
                                        return (float) (unscaledVal / DOUBLE_10_POW[scale]);
                                    default:
                                        break;
                                }
                            } else if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                                if (unscaledVal == 0) {
                                    return DOUBLE_ZERO;
                                }

                                switch (scale) {
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                    case 8:
                                    case 9:
                                    case 10:
                                    case 11:
                                    case 12:
                                    case 13:
                                    case 14:
                                    case 15:
                                        return unscaledVal / DOUBLE_10_POW[scale];
                                    default:
                                        break;
                                }
                            }
                        }
                        decimal = BigDecimal.valueOf(unscaledVal, scale);
                    } else {
                        long v3 = mag3 & 0XFFFFFFFFL;
                        long v2 = mag2 & 0XFFFFFFFFL;

                        if (v2 <= Integer.MAX_VALUE) {
                            long v23 = (v2 << 32) + (v3);
                            long unscaledVal = negative ? -v23 : v23;

                            if (exponent == 0) {
                                if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                                    boolean isNegative;
                                    long unsignedUnscaledVal;
                                    if (unscaledVal < 0) {
                                        isNegative = true;
                                        unsignedUnscaledVal = -unscaledVal;
                                    } else {
                                        isNegative = false;
                                        unsignedUnscaledVal = unscaledVal;
                                    }

                                    /*
                                     * If both unscaledVal and the scale can be exactly
                                     * represented as float values, perform a single float
                                     * multiply or divide to compute the (properly
                                     * rounded) result.
                                     */

                                    if (doubleChars == null) {
                                        doubleChars = new char[20];
                                    }
                                    int len = IOUtils.writeInt64(doubleChars, 0, unsignedUnscaledVal);
                                    return TypeUtils.floatValue(isNegative, len - scale, doubleChars, len);
                                } else if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                                    boolean isNegative;
                                    long unsignedUnscaledVal;
                                    if (unscaledVal < 0) {
                                        isNegative = true;
                                        unsignedUnscaledVal = -unscaledVal;
                                    } else {
                                        isNegative = false;
                                        unsignedUnscaledVal = unscaledVal;
                                    }

                                    /*
                                     * If both unscaledVal and the scale can be exactly
                                     * represented as double values, perform a single
                                     * double multiply or divide to compute the (properly
                                     * rounded) result.
                                     */
                                    if (unsignedUnscaledVal < 1L << 52) {
                                        // Don't have too guard against
                                        // Math.abs(MIN_VALUE) because of outer check
                                        // against INFLATED.
                                        if (scale > 0 && scale < DOUBLE_10_POW.length) {
                                            return (double) unscaledVal / DOUBLE_10_POW[scale];
                                        } else if (scale < 0 && scale > -DOUBLE_10_POW.length) {
                                            return (double) unscaledVal * DOUBLE_10_POW[-scale];
                                        }
                                    }

                                    if (doubleChars == null) {
                                        doubleChars = new char[20];
                                    }
                                    int len = IOUtils.writeInt64(doubleChars, 0, unsignedUnscaledVal);
                                    return TypeUtils.doubleValue(isNegative, len - scale, doubleChars, len);
                                }
                            }
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

                    if (exponent != 0) {
                        return decimal.doubleValue();
                    }
                }

                if (exponent != 0) {
                    String decimalStr = decimal.toPlainString();
                    double doubleValue = Double.parseDouble(
                            decimalStr + "E" + exponent);
                    return Double.valueOf(doubleValue);
                }

                if ((context.features & Feature.UseBigDecimalForFloats.mask) != 0) {
                    return decimal.floatValue();
                }

                if ((context.features & Feature.UseBigDecimalForDoubles.mask) != 0) {
                    return decimal.doubleValue();
                }

                return decimal;
            }
            case JSON_TYPE_BIG_DEC: {
                if (scale > 0) {
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
                        float floatValueValue = Float.parseFloat(
                                decimal + "E" + exponent);
                        return Float.valueOf(floatValueValue);
                    }

                    return decimal.floatValue();
                }

                if (exponent != 0) {
                    double doubleValue = Double.parseDouble(
                            decimal + "E" + exponent);
                    return Double.valueOf(doubleValue);
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
        if (IOUtils.isNumber(val)) {
            return Integer.parseInt(val);
        }
        throw new JSONException("parseInt error, value : " + val);
    }

    protected final long toInt64(String val) {
        if (IOUtils.isNumber(val)) {
            return Long.parseLong(val);
        }

        if (val.length() > 10 && val.length() < 40) {
            try {
                return DateUtils.parseMillis(val, context.zoneId);
            } catch (DateTimeException | JSONException ignored) {
                // ignored
            }
        }

        throw new JSONException("parseLong error, value : " + val);
    }

    protected final long toLong(Map map) {
        Object val = map.get("val");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new JSONException("parseLong error, value : " + map);
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

        throw new JSONException("parseLong error, field : value " + list);
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
        throw new JSONException("can not cast to decimal " + object);
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
        return new JSONReaderUTF8(createReadContext(), null, utf8Bytes, 0, utf8Bytes.length);
    }

    @Deprecated
    public static JSONReader of(JSONReader.Context context, byte[] utf8Bytes) {
        return new JSONReaderUTF8(context, null, utf8Bytes, 0, utf8Bytes.length);
    }

    public static JSONReader of(byte[] utf8Bytes, JSONReader.Context context) {
        return new JSONReaderUTF8(context, null, utf8Bytes, 0, utf8Bytes.length);
    }

    public static JSONReader of(char[] chars) {
        Context context = createReadContext();
        return new JSONReaderUTF16(
                context,
                null,
                chars,
                0,
                chars.length);
    }

    @Deprecated
    public static JSONReader of(Context context, char[] chars) {
        return new JSONReaderUTF16(
                context,
                null,
                chars,
                0,
                chars.length
        );
    }

    public static JSONReader of(char[] chars, Context context) {
        return new JSONReaderUTF16(
                context,
                null,
                chars,
                0,
                chars.length
        );
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext(),
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    @Deprecated
    public static JSONReader ofJSONB(JSONReader.Context context, byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, JSONReader.Context context) {
        return new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, JSONReader.Feature... features) {
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
        return new JSONReaderJSONB(
                context,
                bytes,
                offset,
                length);
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

        if (charset == IOUtils.UTF_8) {
            return new JSONReaderUTF8(context, null, bytes, offset, length);
        }

        if (charset == IOUtils.UTF_16) {
            return new JSONReaderUTF16(context, bytes, offset, length);
        }

        if (charset == IOUtils.US_ASCII || charset == IOUtils.ISO_8859_1) {
            return new JSONReaderASCII(context, null, bytes, offset, length);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(byte[] bytes, int offset, int length, Charset charset, Context context) {
        if (charset == IOUtils.UTF_8) {
            if (offset == 0 && bytes.length == length) {
                return of(bytes, context);
            }

            return new JSONReaderUTF8(context, null, bytes, offset, length);
        }

        if (charset == IOUtils.UTF_16) {
            return new JSONReaderUTF16(context, bytes, offset, length);
        }

        if (charset == IOUtils.US_ASCII || charset == IOUtils.ISO_8859_1) {
            return new JSONReaderASCII(context, null, bytes, offset, length);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(byte[] bytes, int offset, int length) {
        return new JSONReaderUTF8(createReadContext(), null, bytes, offset, length);
    }

    public static JSONReader of(byte[] bytes, int offset, int length, JSONReader.Context context) {
        return new JSONReaderUTF8(context, null, bytes, offset, length);
    }

    public static JSONReader of(char[] chars, int offset, int length) {
        Context context = createReadContext();
        return new JSONReaderUTF16(context, null, chars, offset, length);
    }

    public static JSONReader of(char[] chars, int offset, int length, Context context) {
        return new JSONReaderUTF16(context, null, chars, offset, length);
    }

    public static JSONReader of(URL url, Context context) throws IOException {
        try (InputStream is = url.openStream()) {
            return of(is, IOUtils.UTF_8, context);
        }
    }

    public static JSONReader of(InputStream is, Charset charset) {
        return of(is, charset, new JSONReader.Context(defaultObjectReaderProvider));
    }

    public static JSONReader of(InputStream is, Charset charset, Context context) {
        if (charset == IOUtils.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, is);
        }

        if (charset == IOUtils.UTF_16) {
            return new JSONReaderUTF16(context, is);
        }

        throw new JSONException("not support charset " + charset);
    }

    public static JSONReader of(java.io.Reader is) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext(),
                is
        );
    }

    public static JSONReader of(java.io.Reader is, Context context) {
        return new JSONReaderUTF16(
                context,
                is
        );
    }

    public static JSONReader of(ByteBuffer buffer, Charset charset) {
        return of(buffer, createReadContext(), charset);
    }

    public static JSONReader of(ByteBuffer buffer, Context context, Charset charset) {
        if (charset == IOUtils.UTF_8 || charset == null) {
            return new JSONReaderUTF8(context, buffer);
        }

        throw new JSONException("not support charset " + charset);
    }

    @Deprecated
    public static JSONReader of(Context context, String str) {
        return of(str, context);
    }

    public static JSONReader of(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        return new JSONReaderUTF16(
                new JSONReader.Context(JSONFactory.defaultObjectReaderProvider),
                str,
                0,
                str.length()
        );
    }

    public static JSONReader of(String str, Context context) {
        if (str == null || context == null) {
            throw new NullPointerException();
        }

        return new JSONReaderUTF16(context, str, 0, str.length());
    }

    public static JSONReader of(String str, int offset, int length) {
        if (str == null) {
            throw new NullPointerException();
        }

        Context context = JSONFactory.createReadContext();
        char[] chars = str.toCharArray();
        return new JSONReaderUTF16(context, str, chars, offset, length);
    }

    public static JSONReader of(String str, int offset, int length, Context context) {
        if (str == null || context == null) {
            throw new NullPointerException();
        }
        char[] chars = str.toCharArray();
        return new JSONReaderUTF16(context, str, chars, offset, length);
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
        public ExtraProcessor extraProcessor;

        public final ObjectReaderProvider provider;
        protected final SymbolTable symbolTable;

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
            this.provider = JSONFactory.defaultObjectReaderProvider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public Context(String dateFormat, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = JSONFactory.defaultObjectReaderProvider;
            this.objectSupplier = JSONFactory.defaultObjectSupplier;
            this.arraySupplier = JSONFactory.defaultArraySupplier;
            this.symbolTable = null;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
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

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
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

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public Context(ObjectReaderProvider provider, SymbolTable symbolTable, Filter filter, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            config(filter);

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public Context(ObjectReaderProvider provider, SymbolTable symbolTable, Filter[] filters, Feature... features) {
            this.features = defaultReaderFeatures;
            this.provider = provider;
            this.symbolTable = symbolTable;
            this.zoneId = defaultReaderZoneId;

            String format = defaultReaderFormat;
            if (format != null) {
                setDateFormat(format);
            }

            config(filters);

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
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
            }

            if (this.dateFormat != null && !this.dateFormat.equals(format)) {
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
                zoneId = ZoneId.DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        public long getFeatures() {
            return features;
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

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public TimeZone getTimeZone() {
            if (timeZone == null) {
                timeZone = ZoneId.DEFAULT_TIME_ZONE;
            }
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

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
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
            for (int i = 0; i < filters.length; i++) {
                Filter filter = filters[i];
                if (filter instanceof AutoTypeBeforeHandler) {
                    autoTypeBeforeHandler = (AutoTypeBeforeHandler) filter;
                }

                if (filter instanceof ExtraProcessor) {
                    extraProcessor = (ExtraProcessor) filter;
                }
            }

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
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

    public enum Feature {
        FieldBased(1),
        IgnoreNoneSerializable(1 << 1),
        /**
         * @since 2.0.14
         */
        ErrorOnNoneSerializable(1 << 2),
        SupportArrayToBean(1 << 3),
        InitStringFieldAsEmpty(1 << 4),
        SupportAutoType(1 << 5),
        SupportSmartMatch(1 << 6),
        UseNativeObject(1 << 7),
        SupportClassForName(1 << 8),
        IgnoreSetNullValue(1 << 9),
        UseDefaultConstructorAsPossible(1 << 10),
        UseBigDecimalForFloats(1 << 11),
        UseBigDecimalForDoubles(1 << 12),
        ErrorOnEnumNotMatch(1 << 13),
        TrimString(1 << 14),
        ErrorOnNotSupportAutoType(1 << 15),
        DuplicateKeyValueAsArray(1 << 16),
        AllowUnQuotedFieldNames(1 << 17),
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
        IgnoreNullPropertyValue(1 << 25);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }

        public static long of(Feature[] features) {
            if (features == null) {
                return 0;
            }

            long value = 0;

            for (int i = 0; i < features.length; i++) {
                value |= features[i].mask;
            }

            return value;
        }
    }

    static class ResolveTask {
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

    public final String info() {
        return info(null);
    }

    public String info(String message) {
        if (message == null || message.isEmpty()) {
            return "offset " + offset;
        }
        return message + ", offset " + offset;
    }

    static boolean isFirstIdentifier(char ch) {
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
        static final BiFunction<Integer, int[], BigInteger> BIG_INTEGER_CREATOR = new BigIntegerCreator();

        @Override
        public BigInteger apply(Integer integer, int[] mag) {
            int signum = integer.intValue();
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

    public Byte readInt8() {
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

    public Short readInt16() {
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

    public Character readCharacter() {
        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return Character.valueOf(str.charAt(0));
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

    /**
     * @since 2.0.35
     */
    public void read(Map map, ObjectReader itemReader, long features) {
        nextIfObjectStart();

        for (int i = 0; ; ++i) {
            if (ch == '/') {
                skipLineComment();
            }

            if (nextIfObjectEnd()) {
                break;
            }

            if (i != 0 && !comma) {
                throw new JSONException(info());
            }

            String name = readFieldName();
            Object value = itemReader.readObject(this, itemReader.getObjectClass(), name, features);

            Object origin = map.put(name, value);
            if (origin != null) {
                long contextFeatures = features | context.getFeatures();
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
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
}

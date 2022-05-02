package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.ReferenceKey;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONFactory.Utils.*;

public abstract class JSONReader implements Closeable {
    static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    static final ZoneId UTC = ZoneId.of("UTC");
    static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    static final long LONG_MASK = 0XFFFFFFFFL;

    static final byte JSON_TYPE_INT = 1;
    static final byte JSON_TYPE_DEC = 2;
    static final byte JSON_TYPE_STRING = 3;
    static final byte JSON_TYPE_BOOL = 4;
    static final byte JSON_TYPE_NULL = 5;
    static final byte JSON_TYPE_OBJECT = 6;
    static final byte JSON_TYPE_ARRAY = 7;

    static final char EOI = 0x1A;
    static final long SPACE = (1L << ' ') | (1L << '\n') | (1L << '\r') | (1L << '\f') | (1L << '\t') | (1L << '\b');

    final Context context;
    List<ResolveTask> resolveTasks;

    protected int offset;
    protected char ch;
    protected boolean comma;

    protected boolean nameEscape = false;
    protected boolean valueEscape = false;
    protected boolean wasNull = false;
    protected boolean boolValue = false;
    protected boolean negative = false;

    protected byte valueType = 0;
    protected byte exponent;
    protected byte scale;

    protected int mag0;
    protected int mag1;
    protected int mag2;
    protected int mag3;

    protected String stringValue;
    protected Object complex; // Map | List

    protected boolean typeRedirect; // redirect for {"@type":"xxx"",...

    public final char current() {
        return ch;
    }

    public byte getType() {
        return -128;
    }

    public boolean isInt() {
        return ch == '-' || ch == '+' || (ch >= '0' && ch <= '9');
    }

    public boolean isNull() {
        return ch == 'n';
    }

    public boolean nextIfNull() {
        if (ch == 'n') {
            this.readNull();
            return true;
        }
        return false;
    }

    public JSONReader(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public long features(long features) {
        return context.features | features;
    }

    public void handleResolveTasks(Object root) {
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
                        list.set(index, fieldValue);
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

    public ObjectReader getObjectReader(Type type) {
        boolean fieldBased = (context.features & JSONReader.Feature.FieldBased.mask) != 0;
        return context.provider.getObjectReader(type, fieldBased);
    }

    public boolean isSupportSmartMatch() {
        return (context.features & Feature.SupportSmartMatch.mask) != 0;
    }

    public boolean isSupportSmartMatch(long features) {
        return ((context.features | features) & Feature.SupportSmartMatch.mask) != 0;
    }

    public boolean isSupportBeanArray() {
        return (context.features & Feature.SupportArrayToBean.mask) != 0;
    }

    public boolean isSupportBeanArray(long features) {
        return ((context.features | features) & Feature.SupportArrayToBean.mask) != 0;
    }

    public boolean isSupportAutoType(long features) {
        return ((context.features | features) & Feature.SupportAutoType.mask) != 0;
    }

    public boolean isJSONB() {
        return false;
    }

    public boolean isIgnoreNoneSerializable() {
        return (context.features & Feature.IgnoreNoneSerializable.mask) != 0;
    }

    public ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        return null;
    }

    static char char1(int c) {
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
            case '#': //
            case '&': //
            case '[': //
            case ']': //
                return (char) c;
            default:
                throw new JSONException("unclosed.str.lit " + (char) c);
        }
    }

    static char char2(int c1, int c2) {
        return (char) (DIGITS2[c1] * 0x10
                + DIGITS2[c2]
        );
    }

    static char char4(int c1, int c2, int c3, int c4) {
        return (char) (DIGITS2[c1] * 0x1000
                + DIGITS2[c2] * 0x100
                + DIGITS2[c3] * 0x10
                + DIGITS2[c4]
        );
    }

    public boolean nextIfObjectStart() {
        if (this.ch != '{') {
            return false;
        }
        next();
        return true;
    }

    public boolean nextIfObjectEnd() {
        if (this.ch != '}') {
            return false;
        }
        next();
        return true;
    }

    public int startArray() {
        next();
        return 0;
    }

    public boolean isReference() {
        return false;
    }

    public String readReference() {
        throw new JSONException("TODO");
    }

    public void addResolveTask(FieldReader fieldReader, Object object, JSONPath path) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(fieldReader, object, null, path));
    }

    public void addResolveTask(Map object, Object key, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        if (object instanceof LinkedHashMap) {
            object.put(key, null);
        }
        resolveTasks.add(new ResolveTask(null, object, key, reference));
    }

    public void addResolveTask(Collection object, int i, JSONPath reference) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(new ResolveTask(null, object, i, reference));
    }

    public void addResolveTask(Object[] object, int i, JSONPath reference) {
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

    public boolean nextIfMatch(char ch) {
        if (this.ch != ch) {
            return false;
        }
        next();
        return true;
    }

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

    public void setTypeRedirect(boolean typeRedirect) {
        this.typeRedirect = typeRedirect;
    }

    public boolean isTypeRedirect() {
        return typeRedirect;
    }

    public long readFieldNameHashCodeUnquote() {
        throw new UnsupportedOperationException();
    }

    public String readFieldNameUnquote() {
        readFieldNameHashCodeUnquote();
        return getFieldName();
    }

    public boolean skipName() {
        readFieldNameHashCode();
        return true;
    }

    public abstract void skipValue();

    public boolean isBinary() {
        throw new UnsupportedOperationException();
    }

    public byte[] readBinary() {
        throw new UnsupportedOperationException();
    }

    public abstract int readInt32Value();

    public boolean nextIfMatch(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public boolean nextIfMatch(byte type) {
        throw new UnsupportedOperationException();
    }

    public Integer readInt32() {
        readNumber0();

        if (valueType == JSON_TYPE_INT) {
            return negative ? -mag3 : mag3;
        }

        if (valueType == JSON_TYPE_NULL) {
            return null;
        }

        Number number = getNumber();
        return number == null ? 0 : number.intValue();
    }

    public int getInt32Value() {
        switch (valueType) {
            case JSON_TYPE_INT:
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return negative ? -mag3 : mag3;
                }
                return getNumber().intValue();
            case JSON_TYPE_DEC:
                return getNumber().intValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
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

    protected long getInt64Value() {
        switch (valueType) {
            case JSON_TYPE_DEC:
                return getNumber().longValue();
            case JSON_TYPE_BOOL:
                return boolValue ? 1 : 0;
            case JSON_TYPE_NULL:
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

    protected Long getInt64() {
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

                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
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

                return getBigInt(negative, mag).longValue();
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

    public abstract long readInt64Value();

    public abstract Long readInt64();

    public float readFloatValue() {
        readNumber();
        Number number = getNumber();
        return number == null ? 0 : number.floatValue();
    }

    public Float readFloat() {
        readNumber();
        Number number = getNumber();
        if (number instanceof Float) {
            return (Float) number;
        }
        return number == null ? null : number.floatValue();
    }

    public double readDoubleValue() {
        readNumber();
        Number number = getNumber();
        return number == null ? 0 : number.doubleValue();
    }

    public Double readDouble() {
        readNumber();
        Number number = getNumber();
        if (number instanceof Double) {
            return (Double) number;
        }
        return number == null ? null : number.doubleValue();
    }

    public Number readNumber() {
        readNumber0();
        return getNumber();
    }

    public BigInteger readBigInteger() {
        readNumber0();
        return getBigInteger();
    }

    public BigDecimal readBigDecimal() {
        readNumber0();
        return getBigDecimal();
    }

    public abstract UUID readUUID();

    public LocalDate readLocalDate() {
        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalDate();
        }

        int len = getStringLength();
        switch (len) {
            case 8:
                return readLocalDate8()
                        .toLocalDate();
            case 9:
                return readLocalDate9()
                        .toLocalDate();
            case 10: {
                return readLocalDate10()
                        .toLocalDate();
            }
            case 11:
                return readLocalDate11()
                        .toLocalDate();
            default:
                break;
        }

        String str = readString();
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

        int len = getStringLength();
        switch (len) {
            case 8:
                return readLocalDate8();
            case 9:
                return readLocalDate9();
            case 10:
                return readLocalDate10();
            case 11:
                return readLocalDate11();
            case 16:
                return readLocalDateTime16();
            case 17:
                return readLocalDateTime17();
            case 18:
                return readLocalDateTime18();
            case 19:
                return readLocalDateTime19();
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return readLocalDateTimeX(len);
            default:
                break;
        }

        String strVal = readString();
        if (IOUtils.isNumber(strVal)) {
            long millis = Long.parseLong(strVal);
            Instant instant = Instant.ofEpochMilli(millis);
            return LocalDateTime.ofInstant(instant, context.getZoneId());
        }

        throw new JSONException("TODO : " + len + ", " + strVal);
    }

    public ZonedDateTime readZonedDateTime() {
        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            return instant.atZone(context.getZoneId());
        }

        if (ch == '"') {
            int len = getStringLength();
            if (len == 10) {
                LocalDateTime ldt = readLocalDate10();
                if (ldt != null) {
                    return ZonedDateTime.of(ldt, context.getZoneId());
                }
            }

            ZonedDateTime zdt = readZonedDateTimeX(len);
            if (zdt != null) {
                return zdt;
            }

            String str = readString();
            if (IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                Instant instant = Instant.ofEpochMilli(millis);
                return instant.atZone(context.getZoneId());
            }

            throw new JSONException("TODO : " + str + ", len : " + len);
        }
        throw new JSONException("TODO : " + ch);
    }

    public LocalTime readLocalTime() {
        if (isInt()) {
            long millis = readInt64Value();
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        int len = getStringLength();
        switch (len) {
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
            default:
                break;
        }

        String str = readString();
        if (IOUtils.isNumber(str)) {
            long millis = Long.parseLong(str);
            Instant instant = Instant.ofEpochMilli(millis);
            ZonedDateTime zdt = instant.atZone(context.getZoneId());
            return zdt.toLocalTime();
        }

        throw new JSONException("not support len : " + len);
    }

    protected abstract int getStringLength();

    public Instant readInstant() {
        if (isString()) {
            int len = getStringLength();
            ZonedDateTime zdt = readZonedDateTimeX(len);

            if (zdt == null) {
                String str = readString();

                if (IOUtils.isNumber(str)) {
                    long millis = Long.parseLong(str);
                    return Instant.ofEpochMilli(millis);
                }

                throw new JSONException("TODO : " + str + ", len : " + len);
            }

            return zdt.toInstant();
        }

        if (isNumber()) {
            long millis = readInt64Value();
            return Instant.ofEpochMilli(millis);
        }

        if (isObject()) {
            return (Instant) getObjectReader(Instant.class)
                    .createInstance(
                            readObject()
                    );
        }

        throw new UnsupportedOperationException();
    }

    public long readMillisFromString() {
        int len = getStringLength();
        switch (len) {
            case 8: {
                LocalDateTime date = readLocalDate8();
                if (date != null) {
                    return ZonedDateTime.of(date
                            , context.getZoneId())
                            .toInstant()
                            .toEpochMilli();
                }
                throw new JSONException("TODO : " + readString());
            }
            case 9: {
                LocalDateTime date = readLocalDate9();
                if (date == null) {
                    break;
                }

                return ZonedDateTime.of(date
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            case 10: {
                LocalDateTime date = readLocalDate10();
                if (date != null) {
                    return ZonedDateTime.of(date
                            , context.getZoneId())
                            .toInstant()
                            .toEpochMilli();
                }
                String str = readString();
                if ("0000-00-00".equals(str)) {
                    return 0;
                }
                throw new JSONException("TODO : " + str);
            }
            case 11: {
                LocalDateTime date = readLocalDate11();
                return ZonedDateTime.of(date
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            case 16: {
                LocalDateTime date = readLocalDateTime16();
                return ZonedDateTime.of(date
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            case 17: {
                LocalDateTime ldt = readLocalDateTime17();
                return ZonedDateTime.of(ldt
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            case 18: {
                LocalDateTime date = readLocalDateTime18();
                return ZonedDateTime.of(date
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            case 19: {
                LocalDateTime date = readLocalDateTime19();
                return ZonedDateTime.of(date
                        , context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
            }
            default:
                break;
        }

        if (len >= 20) {
            ZonedDateTime zdt = readZonedDateTimeX(len);
            if (zdt != null) {
                return zdt.toInstant().toEpochMilli();
            }
        }

        String str = readString();
        String utilDateFormat = context.getUtilDateFormat();

        if (utilDateFormat != null && !utilDateFormat.isEmpty()) {
            SimpleDateFormat utilFormat = new SimpleDateFormat(utilDateFormat);
            try {
                return utilFormat
                        .parse(str)
                        .getTime();
            } catch (ParseException e) {
                throw new JSONException("parse date error, " + str + ", expect format " + utilFormat);
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

        throw new JSONException("TODO : " + str + ", len : " + len);
    }

    protected LocalDateTime readLocalDateTime16() {
        throw new UnsupportedOperationException();
    }

    protected LocalDateTime readLocalDateTime17() {
        throw new UnsupportedOperationException();
    }

    protected LocalDateTime readLocalDateTime18() {
        throw new UnsupportedOperationException();
    }

    protected abstract LocalDateTime readLocalDateTime19();

    protected abstract LocalDateTime readLocalDateTimeX(int len);

    protected abstract LocalTime readLocalTime8();

    protected LocalTime readLocalTime10() {
        throw new UnsupportedOperationException();
    }

    protected LocalTime readLocalTime11() {
        throw new UnsupportedOperationException();
    }

    protected abstract LocalTime readLocalTime12();

    protected abstract LocalTime readLocalTime18();

    protected abstract LocalDateTime readLocalDate8();

    protected abstract LocalDateTime readLocalDate9();

    protected abstract LocalDateTime readLocalDate10();

    protected LocalDateTime readLocalDate11() {
        throw new UnsupportedOperationException();
    }

    protected ZonedDateTime readZonedDateTimeX(int len) {
        throw new UnsupportedOperationException();
    }

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

    public abstract void readNull();

    public abstract boolean readIfNull();

    public abstract String getString();

    public boolean wasNull() {
        return wasNull;
    }

    public <T> T read(Type type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(this, 0);
    }

    public Map<String, Object> readObject() {
        nextIfObjectStart();
        Map object;
        if (context.objectClass == null || context.objectClass == Object.class) {
            if ((context.features & Feature.UseNativeObject.mask) != 0) {
                object = new HashMap();
            } else {
                object = new JSONObject();
            }
        } else {
            try {
                object = (Map) context.objectClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create object instance error, objectClass " + context.objectClass.getName(), e);
            }
        }

        for (; ; ) {
            if (ch == '}') {
                next();
                break;
            }

            String name = readFieldName();
            if (name == null) {
                name = readFieldNameUnquote();
                nextIfMatch(':');
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
                default:
                    throw new JSONException("illegal input offset " + offset + ", char " + ch);
            }
            object.put(name, val);
        }


        if (ch == ',') {
            next();
        }

        return object;
    }

    public Boolean readBool() {
        if (isNull()) {
            readNull();
            return null;
        }
        return readBoolValue();
    }

    public boolean readBoolValue() {
        wasNull = false;
        boolean val;
        if (ch == 't') {
            next();
            char c1 = ch;
            next();
            char c2 = ch;
            next();
            char c3 = ch;
            if (c1 == 'r' && c2 == 'u' || c3 == 'e') {
                val = true;
            } else {
                throw new JSONException("syntax error : " + ch);
            }

        } else if (ch == 'f') {
            next();
            char c1 = ch;
            next();
            char c2 = ch;
            next();
            char c3 = ch;
            next();
            char c4 = ch;
            if (c1 == 'a' && c2 == 'l' || c3 == 's' || c4 == 'e') {
                val = false;
            } else {
                throw new JSONException("syntax error : " + ch);
            }
        } else if (ch == '-' || (ch >= '0' && ch <= '9')) {
            readNumber();
            return valueType == JSON_TYPE_INT
                    && mag1 == 0
                    && mag2 == 0
                    && mag3 == 1;
        } else if (ch == 'n') {
            wasNull = true;
            readNull();
            return false;
        } else if (ch == '"') {
            int len = getStringLength();
            if (len == 1) {
                next();
                if (ch == '0' || ch == 'N') {
                    next();
                    next();
                    nextIfMatch(',');
                    return false;
                } else if (ch == '1' || ch == 'Y') {
                    next();
                    next();
                    nextIfMatch(',');
                    return true;
                }
                throw new JSONException("can not convert to boolean : " + ch);
            }
            String str = readString();
            if (str.equalsIgnoreCase("true")) {
                return true;
            }

            if (str.equalsIgnoreCase("false")) {
                return false;
            }
            throw new JSONException("can not convert to boolean : " + str);
        } else {
            throw new JSONException("syntax error : " + ch);
        }

        next();

        nextIfMatch(',');

        return val;
    }

    public Object readAny() {
        return read(Object.class);
    }

    public List readArray() {
        next();

        List list = new JSONArray();

        _for:
        for (; ; ) {
            Object val;
            switch (ch) {
                case ']':
                    next();
                    break _for;
                case '[':
                    val = readArray();
                    break;
                case '{':
                    val = readObject();
                    break;
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
                default:
                    throw new JSONException("TODO : " + ch);
            }
            list.add(val);
        }

        if (ch == ',') {
            next();
        }

        return list;
    }

    public BigInteger getBigInteger() {
        Number number = getNumber();

        if (number == null) {
            return null;
        }

        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        return BigInteger.valueOf(number.longValue());
    }

    public BigDecimal getBigDecimal() {
        switch (valueType) {
            case JSON_TYPE_INT: {
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    return BigDecimal.valueOf(negative ? -mag3 : mag3);
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
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

                return new BigDecimal(getBigInt(negative, mag));
            }
            case JSON_TYPE_DEC: {
                int[] mag = mag0 == 0
                        ? mag1 == 0
                        ? mag2 == 0
                        ? new int[]{mag3}
                        : new int[]{mag2, mag3}
                        : new int[]{mag1, mag2, mag3}
                        : new int[]{mag0, mag1, mag2, mag3};
                BigInteger bigInt = getBigInt(negative, mag);
                return new BigDecimal(bigInt, scale);
            }
            case JSON_TYPE_BOOL:
                return boolValue ? BigDecimal.ONE : BigDecimal.ZERO;
            case JSON_TYPE_NULL:
                return null;
            case JSON_TYPE_STRING: {
                try {
                    return new BigDecimal(stringValue);
                } catch (NumberFormatException ex) {
                    throw new JSONException("read decimal error, value " + stringValue, ex);
                }
            }
            default:
                throw new JSONException("TODO : " + valueType);
        }
    }

    public Number getNumber() {
        switch (valueType) {
            case JSON_TYPE_INT: {
                if (mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                    if (negative) {
                        if (mag3 < 0) {
                            return -(mag3 & 0xFFFFFFFFL);
                        }
                        return -mag3;
                    } else {
                        if (mag3 < 0) {
                            return mag3 & 0xFFFFFFFFL;
                        }
                        return mag3;
                    }
                }
                int[] mag;
                if (mag0 == 0) {
                    if (mag1 == 0) {
                        long v3 = mag3 & LONG_MASK;
                        long v2 = mag2 & LONG_MASK;

                        if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
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

                return getBigInt(negative, mag);
            }
            case JSON_TYPE_DEC: {
                int[] mag = mag0 == 0
                        ? mag1 == 0
                        ? mag2 == 0
                        ? new int[]{mag3}
                        : new int[]{mag2, mag3}
                        : new int[]{mag1, mag2, mag3}
                        : new int[]{mag0, mag1, mag2, mag3};
                BigInteger bigInt = getBigInt(negative, mag);
                BigDecimal decimal = new BigDecimal(bigInt, scale);
                if (exponent != 0) { // TODO
                    double doubleValue = Double.parseDouble(
                            decimal + "E" + exponent);
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
    public void close() {

    }

    static BigInteger getBigInt(boolean negative, int[] mag) {
        int signum = mag.length == 0 ? 0 : negative ? -1 : 1;

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
                            for (j = mlen - 1; j >= 0 && mag[j] == 0; j--) ;
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

    protected final Number toNumber(List list) {
        if (list.size() == 1) {
            Object val = list.get(0);
            if (val instanceof Number) {
                return (Number) val;
            }

            if (val instanceof String) {
                return new BigDecimal((String) val);
            }
        }
        return null;
    }

    protected final String toString(List array) {
        JSONWriter writer = JSONWriter.of();
        writer.write(array);
        return writer.toString();
    }

    protected final String toString(Map object) {
        JSONWriter writer = JSONWriter.of();
        writer.write(object);
        return writer.toString();
    }

    public static JSONReader of(byte[] utf8Bytes) {
        return new JSONReaderUTF8(JSONFactory.createReadContext(), utf8Bytes, 0, utf8Bytes.length);
    }

    public static JSONReader of(char[] chars) {
        return new JSONReaderUTF16(
                JSONFactory.createReadContext()
                , null
                , chars
                , 0
                , chars.length);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext()
                , jsonbBytes
                , 0
                , jsonbBytes.length, null);
    }

    public static JSONReader ofJSONB(byte[] jsonbBytes, JSONReader.Feature... features) {
        Context context = JSONFactory.createReadContext();
        context.config(features);
        return new JSONReaderJSONB(
                context
                , jsonbBytes
                , 0
                , jsonbBytes.length, null);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext()
                , bytes
                , offset
                , length, null);
    }

    public static JSONReader ofJSONB(byte[] bytes, int offset, int length, JSONB.SymbolTable symbolTable) {
        return new JSONReaderJSONB(
                JSONFactory.createReadContext()
                , bytes
                , offset
                , length, symbolTable);
    }

    public static JSONReader of(byte[] bytes, int offset, int length, Charset charset) {
        Context ctx = JSONFactory.createReadContext();

        if (charset == StandardCharsets.UTF_8) {
            return new JSONReaderUTF8(ctx, bytes, offset, length);
        }

        if (charset == StandardCharsets.UTF_16) {
            return new JSONReaderUTF16(ctx, bytes, offset, length);
        }

        if (charset == StandardCharsets.US_ASCII) {
            return new JSONReaderASCII(ctx, null, bytes, offset, length);
        }

        throw new JSONException("TODO");
    }

    public static JSONReader of(byte[] bytes, int offset, int length) {
        return new JSONReaderUTF8(JSONFactory.createReadContext(), bytes, offset, length);
    }

    public static JSONReader of(char[] chars, int offset, int length) {
        return new JSONReaderUTF16(JSONFactory.createReadContext(), null, chars, offset, length);
    }

    public static JSONReader of(InputStream is, Charset charset) {
        Context ctx = JSONFactory.createReadContext();

        byte[] bytes = BYTES0_UPDATER.getAndSet(CACHE, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
        int off = 0;
        try {
            for (; ; ) {
                int n = is.read(bytes, off, bytes.length - off);
                if (n == -1) {
                    break;
                }
                off += n;

                if (off == bytes.length) {
                    bytes = Arrays.copyOf(bytes, bytes.length + 8192);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }

        if (charset == StandardCharsets.UTF_8 || charset == null) {
            return new JSONReaderUTF8(ctx, bytes, 0, off);
        }

        if (charset == StandardCharsets.UTF_16) {
            if (off % 2 == 1) {
                throw new JSONException("illegal input utf16 bytes, length " + off);
            }

            char[] chars = new char[off / 2];
            for (int i = 0, j = 0; i < off; i += 2, ++j) {
                byte c0 = bytes[i];
                byte c1 = bytes[i + 1];
                chars[j] = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
            }

            return new JSONReaderUTF16(ctx, null, chars, 0, chars.length);
        }

        throw new JSONException("not support input charset : " + charset);
    }

    public static JSONReader of(java.io.Reader is) {
        char[] chars = CHARS_UPDATER.getAndSet(CACHE, null);

        if (chars == null) {
            chars = new char[8192];
        }
        int off = 0;
        try {
            for (; ; ) {
                int n = is.read(chars, off, chars.length - off);
                if (n == -1) {
                    break;
                }
                off += n;

                if (off == chars.length) {
                    chars = Arrays.copyOf(chars, chars.length + 8192);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }

        return new JSONReaderUTF16(
                JSONFactory.createReadContext(), null, chars, 0, off);
    }

    public static JSONReader of(String str) {
        Context ctx = JSONFactory.createReadContext();

        if (JDKUtils.STRING_BYTES_INTERNAL_API) {
            if (CODER_FUNCTION == null && !CODER_FUNCTION_ERROR) {
                try {
                    CODER_FUNCTION = JDKUtils.getStringCode11();
                    VALUE_FUNCTION = JDKUtils.getStringValue11();
                } catch (Throwable ignored) {
                    CODER_FUNCTION_ERROR = true;
                }
            }
        }

        if (CODER_FUNCTION != null && VALUE_FUNCTION != null) {
            int coder = CODER_FUNCTION.applyAsInt(str);
            if (coder == 0) {
                byte[] value = VALUE_FUNCTION.apply(str);
                return new JSONReaderASCII(
                        ctx
                        , str
                        , value
                        , 0
                        , value.length
                );
            }
        }

        char[] chars = JDKUtils.getCharArray(str);
        return new JSONReaderUTF16(
                ctx
                , str
                , chars
                , 0
                , chars.length
        );
    }

    void bigInt(char[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 128) {
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
            long ylong = 1000000000 & LONG_MASK;

            long product = 0;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & LONG_MASK) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & LONG_MASK) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & LONG_MASK) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & LONG_MASK) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long zlong = groupVal & LONG_MASK;
            long sum = (mag3 & LONG_MASK) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & LONG_MASK) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & LONG_MASK) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & LONG_MASK) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & LONG_MASK) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    void bigInt(byte[] chars, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 128) {
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
            long ylong = 1000000000 & LONG_MASK;
            long zlong = groupVal & LONG_MASK;

            long product = 0;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & LONG_MASK) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & LONG_MASK) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & LONG_MASK) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & LONG_MASK) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long sum = (mag3 & LONG_MASK) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & LONG_MASK) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & LONG_MASK) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & LONG_MASK) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & LONG_MASK) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    public static class Context {
        String utilDateFormat;
        DateTimeFormatter dateFormat;
        ZoneId zoneId;
        long features;
        Locale locale;
        TimeZone timeZone;
        Class objectClass;

        protected final ObjectReaderProvider provider;

        public Context(ObjectReaderProvider provider) {
            this.provider = provider;
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
            return provider.getObjectReader(typeName, expectClass, features);
        }

        public ObjectReader getObjectReaderAutoType(String typeName, Class expectClass, long features) {
            return provider.getObjectReader(typeName, expectClass, this.features | features);
        }

        public Class getObjectClass() {
            return objectClass;
        }

        public void setObjectClass(Class objectClass) {
            this.objectClass = objectClass;
        }

        public DateTimeFormatter getDateFormat() {
            return dateFormat;
        }

        public void setDateFormat(DateTimeFormatter dateFormat) {
            this.dateFormat = dateFormat;
        }

        public String getUtilDateFormat() {
            return utilDateFormat;
        }

        public void setUtilDateFormat(String utilDateFormat) {
            this.utilDateFormat = utilDateFormat;
        }

        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        public long getFeatures() {
            return features;
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
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
            for (Feature feature : features) {
                this.features |= feature.mask;
            }
        }

        public boolean isEnable(Feature feature) {
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
        FieldBased                      (1),
        IgnoreNoneSerializable          (1 << 1),
        SupportArrayToBean              (1 << 2),
        InitStringFieldAsEmpty          (1 << 3),
        SupportAutoType                 (1 << 4),
        SupportSmartMatch               (1 << 5),
        UseNativeObject                 (1 << 6),
        SupportClassForName             (1 << 7),
        IgnoreSetNullValue              (1 << 8),
        UseDefaultConstructorAsPossible (1 << 9),
        UseBigDecimalForFloats          (1 << 10),
        UseBigDecimalForDoubles         (1 << 11),
        ErrorOnEnumNotMatch             (1 << 12),
//        TrimStringFieldValue            (1 << 13),
        ;

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
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

    static LocalDateTime getLocalDateTime(
            char y0,
            char y1,
            char y2,
            char y3,
            char m0,
            char m1,
            char d0,
            char d1,
            char h0,
            char h1,
            char i0,
            char i1,
            char s0,
            char s1,
            char S0,
            char S1,
            char S2,
            char S3,
            char S4,
            char S5,
            char S6,
            char S7,
            char S8) {

        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            return null;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            return null;
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            return null;
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int nanos;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
                && S3 >= '0' && S3 <= '9'
                && S4 >= '0' && S4 <= '9'
                && S5 >= '0' && S5 <= '9'
                && S6 >= '0' && S6 <= '9'
                && S7 >= '0' && S7 <= '9'
                && S8 >= '0' && S8 <= '9'
        ) {
            nanos = (S0 - '0') * 1000_000_00
                    + (S1 - '0') * 1000_000_0
                    + (S2 - '0') * 1000_000
                    + (S3 - '0') * 1000_00
                    + (S4 - '0') * 1000_0
                    + (S5 - '0') * 1000
                    + (S6 - '0') * 100
                    + (S7 - '0') * 10
                    + (S8 - '0');
        } else {
            return null;
        }

        return LocalDateTime.of(year, month, dom, hour, minute, second, nanos);
    }

    static ZonedDateTime getZonedDateTime(
            char y0,
            char y1,
            char y2,
            char y3,
            char m0,
            char m1,
            char d0,
            char d1,
            char h0,
            char h1,
            char i0,
            char i1,
            char s0,
            char s1,
            char S0,
            char S1,
            char S2,
            char S3,
            char S4,
            char S5,
            char S6,
            char S7,
            char S8,
            ZoneId zoneId) {
        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            return null;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            return null;
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            return null;
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int nanos;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
                && S3 >= '0' && S3 <= '9'
                && S4 >= '0' && S4 <= '9'
                && S5 >= '0' && S5 <= '9'
                && S6 >= '0' && S6 <= '9'
                && S7 >= '0' && S7 <= '9'
                && S8 >= '0' && S8 <= '9'
        ) {
            nanos = (S0 - '0') * 1000_000_00
                    + (S1 - '0') * 1000_000_0
                    + (S2 - '0') * 1000_000
                    + (S3 - '0') * 1000_00
                    + (S4 - '0') * 1000_0
                    + (S5 - '0') * 1000
                    + (S6 - '0') * 100
                    + (S7 - '0') * 10
                    + (S8 - '0');
        } else {
            return null;
        }

        return ZonedDateTime.of(year, month, dom, hour, minute, second, nanos, zoneId);
    }

    protected ZoneId getZoneId(LocalDateTime ldt, String zoneIdStr) {
        ZoneId zoneId;
        if (zoneIdStr != null) {
            if ("000".equals(zoneIdStr)) {
                zoneId = UTC;
            } else if ("+08:00[Asia/Shanghai]".equals(zoneIdStr) || "Asia/Shanghai".equals(zoneIdStr)) {
                zoneId = SHANGHAI;
            } else {
                zoneId = ZoneId.of(zoneIdStr);
            }
        } else {
            zoneId = context.getZoneId();
        }
        return zoneId;
    }
}

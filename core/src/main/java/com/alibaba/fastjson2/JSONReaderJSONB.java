package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONB.typeName;
import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.DateUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

final class JSONReaderJSONB
        extends JSONReader {
    static final long BASE = UNSAFE.arrayBaseOffset(byte[].class);

    static final byte[] SHANGHAI_ZONE_ID_NAME_BYTES = JSONB.toBytes(SHANGHAI_ZONE_ID_NAME);
    static Charset GB18030;

    protected final byte[] bytes;
    protected final int length;
    protected final int end;

    protected byte type;
    protected int strlen;
    protected byte strtype;
    protected int strBegin;

    protected byte[] valueBytes;
    protected final CacheItem cacheItem;

    protected final SymbolTable symbolTable;

    protected long symbol0Hash;
    protected int symbol0Begin;
    protected int symbol0Length;
    protected byte symbol0StrType;

    protected long[] symbols;

    JSONReaderJSONB(Context ctx, InputStream is) {
        super(ctx, true, false);

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        int bufferSize = ctx.bufferSize;
        if (bytes == null) {
            bytes = new byte[bufferSize];
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
                    bytes = Arrays.copyOf(bytes, bytes.length + bufferSize);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }

        this.bytes = bytes;
        this.offset = 0;
        this.length = off;
        this.end = length;
        this.symbolTable = ctx.symbolTable;
    }

    JSONReaderJSONB(Context ctx, byte[] bytes, int off, int length) {
        super(ctx, true, false);
        this.bytes = bytes;
        this.offset = off;
        this.length = length;
        this.end = off + length;
        this.symbolTable = ctx.symbolTable;
        this.cacheItem = CACHE_ITEMS[System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1)];
    }

    @Override
    public String getString() {
        if (strtype == BC_NULL) {
            return null;
        }

        if (strlen < 0) {
            return symbolTable.getName(-strlen);
        }

        Charset charset;
        if (strtype == BC_STR_ASCII) {
            charset = StandardCharsets.ISO_8859_1;
        } else if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            if (STRING_CREATOR_JDK8 != null) {
                char[] chars = new char[strlen];
                for (int i = 0; i < strlen; ++i) {
                    chars[i] = (char) (bytes[strBegin + i] & 0xff);
                }
                return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
            } else if (STRING_CREATOR_JDK11 != null) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, strBegin, chars, 0, strlen);
                return STRING_CREATOR_JDK11.apply(chars, LATIN1);
            }
            charset = StandardCharsets.ISO_8859_1;
        } else if (strtype == BC_STR_UTF8) {
            charset = StandardCharsets.UTF_8;
        } else if (strtype == BC_STR_UTF16) {
            charset = StandardCharsets.UTF_16;
        } else if (strtype == BC_STR_UTF16LE) {
            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            charset = StandardCharsets.UTF_16BE;
//      } else if (strtype == BC_SYMBOL) {
//          int symbol = strlen;
//          int index = symbol * 2;
//          return symbols[index];
        } else {
            throw new JSONException("TODO : " + JSONB.typeName(strtype));
        }

        return new String(bytes, strBegin, strlen, charset);
    }

    public int readLength() {
        byte type = bytes[offset++];
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return type;
        }

        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            int len = getInt3(bytes, offset, type);
            offset += 2;
            return len;
        }

        if (type == BC_INT32) {
            int len = getInt(bytes, offset);
            offset += 4;
            if (len > 1024 * 1024 * 256) {
                throw new JSONException("input length overflow");
            }
            return len;
        }

        throw new JSONException("not support length type : " + typeName(type));
    }

    static int getInt3(byte[] bytes, int offset, int type) {
        return ((type - BC_INT32_SHORT_ZERO) << 16)
                + ((bytes[offset] & 0xFF) << 8)
                + (bytes[offset + 1] & 0xFF);
    }

    @Override
    public boolean isArray() {
        if (offset >= bytes.length) {
            return false;
        }

        byte type = bytes[offset];
        return type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY;
    }

    @Override
    public boolean isObject() {
        return offset < end && bytes[offset] == BC_OBJECT;
    }

    @Override
    public boolean isNumber() {
        byte type = bytes[offset];
        return type >= BC_DOUBLE_NUM_0 && type <= BC_INT32;
    }

    @Override
    public boolean isString() {
        return offset < bytes.length
                && (type = bytes[offset]) >= BC_STR_ASCII_FIX_MIN;
    }

    @Override
    public boolean nextIfMatch(char ch) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfArrayStart() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfComma() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfArrayEnd() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfObjectStart() {
        if (bytes[offset] != BC_OBJECT) {
            return false;
        }
        offset++;
        return true;
    }

    @Override
    public boolean nextIfObjectEnd() {
        if (bytes[offset] != BC_OBJECT_END) {
            return false;
        }
        offset++;
        return true;
    }

    @Override
    public boolean nextIfNullOrEmptyString() {
        if (bytes[offset] == BC_NULL) {
            offset++;
            return true;
        }

        if (bytes[offset] != BC_STR_ASCII_FIX_MIN) {
            return false;
        }
        offset += 1;
        return true;
    }

    @Override
    public <T> T read(Type type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readJSONBObject(this, null, null, 0);
    }

    @Override
    public <T> T read(Class<T> type) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = context.provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readJSONBObject(this, null, null, 0);
    }

    @Override
    public Map<String, Object> readObject() {
        type = bytes[offset++];
        if (type == BC_NULL) {
            return null;
        }

        if (type >= BC_OBJECT) {
            Map map;
            if ((context.features & Feature.UseNativeObject.mask) != 0) {
                if (JVM_VERSION == 8 && bytes[offset] != BC_OBJECT_END) {
                    map = new HashMap(10);
                } else {
                    map = new HashMap();
                }
            } else {
                if (JVM_VERSION == 8 && bytes[offset] != BC_OBJECT_END) {
                    map = new JSONObject(10);
                } else {
                    map = new JSONObject();
                }
            }

            for (int i = 0; ; ++i) {
                type = bytes[offset];
                if (type == BC_OBJECT_END) {
                    offset++;
                    break;
                }

                Object name;
                if (isString()) {
                    name = readFieldName();
                } else {
                    name = readAny();
                }

                if (offset < bytes.length && bytes[offset] == BC_REFERENCE) {
                    String reference = readReference();
                    if ("..".equals(reference)) {
                        map.put(name, map);
                    } else {
                        addResolveTask(map, name, JSONPath.of(reference));
                    }
                    continue;
                }

                byte valueType = bytes[offset];
                Object value;
                if (valueType >= BC_STR_ASCII_FIX_MIN && valueType <= BC_STR_GB18030) {
                    value = readString();
                } else if (valueType >= BC_INT32_NUM_MIN && valueType <= BC_INT32_NUM_MAX) {
                    offset++;
                    value = (int) valueType;
                } else if (valueType == BC_TRUE) {
                    offset++;
                    value = Boolean.TRUE;
                } else if (valueType == BC_FALSE) {
                    offset++;
                    value = Boolean.FALSE;
                } else if (valueType == BC_OBJECT) {
                    value = readObject();
                } else if (valueType == BC_INT64) {
                    long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 1);
                    offset += 9;
                    value = BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value);
                } else if (valueType >= BC_ARRAY_FIX_MIN && valueType <= BC_ARRAY) {
                    offset++;
                    int len;
                    if (valueType == BC_ARRAY) {
                        byte itemType = bytes[offset];
                        if (itemType >= BC_INT32_NUM_MIN && itemType <= BC_INT32_NUM_MAX) {
                            offset++;
                            len = itemType;
                        } else {
                            len = readLength();
                        }
                    } else {
                        len = valueType - BC_ARRAY_FIX_MIN;
                    }

                    if (len == 0) {
                        if ((context.features & Feature.UseNativeObject.mask) != 0) {
                            value = new ArrayList<>();
                        } else {
                            if (context.arraySupplier != null) {
                                value = context.arraySupplier.get();
                            } else {
                                value = new JSONArray();
                            }
                        }
                    } else {
                        List list;
                        if ((context.features & Feature.UseNativeObject.mask) != 0) {
                            list = new ArrayList(len);
                        } else {
                            list = new JSONArray(len);
                        }

                        for (int j = 0; j < len; ++j) {
                            if (isReference()) {
                                String reference = readReference();
                                if ("..".equals(reference)) {
                                    list.add(list);
                                } else {
                                    list.add(null);
                                    addResolveTask(list, j, JSONPath.of(reference));
                                }
                                continue;
                            }

                            byte itemType = bytes[offset];
                            Object item;
                            if (itemType >= BC_STR_ASCII_FIX_MIN && itemType <= BC_STR_GB18030) {
                                item = readString();
                            } else if (itemType == BC_OBJECT) {
                                item = readObject();
                            } else {
                                item = readAny();
                            }
                            list.add(item);
                        }
                        value = list;
                    }
                } else if (valueType >= BC_INT32_BYTE_MIN && valueType <= BC_INT32_BYTE_MAX) {
                    value = ((valueType - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset + 1] & 0xFF);
                    offset += 2;
                } else if (valueType >= BC_INT32_SHORT_MIN && valueType <= BC_INT32_SHORT_MAX) {
                    int int32Value = getInt3(bytes, offset + 1, valueType);
                    offset += 3;
                    value = int32Value;
                } else if (valueType == BC_INT32) {
                    int int32Value = getInt(bytes, offset + 1);
                    offset += 5;
                    value = int32Value;
                } else {
                    value = readAny();
                }

                if (value == null && (context.features & Feature.IgnoreNullPropertyValue.mask) != 0) {
                    continue;
                }

                map.put(name, value);
            }

            return map;
        }

        if (type == BC_TYPED_ANY) {
            ObjectReader objectReader = checkAutoType(Map.class, 0, 0);
            return (Map) objectReader.readObject(this, null, null, 0);
        }

        throw new JSONException("object not support input " + error(type));
    }

    @Override
    public void read(final Map map, long features) {
        if (bytes[offset] != BC_OBJECT) {
            throw new JSONException("object not support input " + error(type));
        }
        offset++;

        long contextFeatures = features | context.getFeatures();

        while (true) {
            byte type = bytes[offset];
            if (type == BC_OBJECT_END) {
                offset++;
                break;
            }

            Object name;
            if (type >= BC_STR_ASCII_FIX_MIN) {
                name = readFieldName();
            } else {
                name = readAny();
            }

            if (isReference()) {
                String reference = readReference();
                if ("..".equals(reference)) {
                    map.put(name, map);
                } else {
                    addResolveTask(map, name, JSONPath.of(reference));
                    map.put(name, null);
                }
                continue;
            }

            byte valueType = bytes[offset];
            Object value;
            if (valueType >= BC_STR_ASCII_FIX_MIN && valueType <= BC_STR_GB18030) {
                value = readString();
            } else if (valueType >= BC_INT32_NUM_MIN && valueType <= BC_INT32_NUM_MAX) {
                offset++;
                value = (int) valueType;
            } else if (valueType == BC_TRUE) {
                offset++;
                value = Boolean.TRUE;
            } else if (valueType == BC_FALSE) {
                offset++;
                value = Boolean.FALSE;
            } else if (valueType == BC_OBJECT) {
                value = readObject();
            } else {
                value = readAny();
            }

            if (value == null && (contextFeatures & Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            map.put(name, value);
        }
    }

    @Override
    public Object readAny() {
        if (offset >= bytes.length) {
            throw new JSONException("readAny overflow : " + offset + "/" + bytes.length);
        }

        type = bytes[offset++];
        switch (type) {
            case BC_NULL:
                return null;
            case BC_TRUE:
                return true;
            case BC_FALSE:
                return false;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                return (short) ((bytes[offset++] << 8)
                        + (bytes[offset++] & 0xFF));
            case BC_INT32: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return int32Value;
            }
            case BC_INT64_INT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return (long) int32Value;
            }
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value);
            }
            case BC_BIGINT: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                return new BigInteger(bytes);
            }
            case BC_FLOAT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_DOUBLE_LONG: {
                return (double) readInt64Value();
            }
            case BC_STR_UTF8: {
                int strlen = readLength();

                if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                    if (valueBytes == null) {
                        valueBytes = BYTES_UPDATER.getAndSet(cacheItem, null);
                        if (valueBytes == null) {
                            valueBytes = new byte[8192];
                        }
                    }

                    int minCapacity = strlen << 1;
                    if (minCapacity > valueBytes.length) {
                        valueBytes = new byte[minCapacity];
                    }

                    int utf16_len = IOUtils.decodeUTF8(bytes, offset, strlen, valueBytes);
                    if (utf16_len != -1) {
                        byte[] value = new byte[utf16_len];
                        System.arraycopy(valueBytes, 0, value, 0, utf16_len);
                        String str = STRING_CREATOR_JDK11.apply(value, UTF16);
                        offset += strlen;
                        return str;
                    }
                }

                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                return str;
            }
            case BC_STR_UTF16: {
                int strlen = readLength();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16);
                offset += strlen;
                return str;
            }
            case BC_STR_UTF16LE: {
                int strlen = readLength();

                String str;
                if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, strlen == 0 ? LATIN1 : UTF16);
                } else {
                    str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                }

                offset += strlen;
                return str;
            }
            case BC_STR_UTF16BE: {
                int strlen = readLength();

                String str;
                if (STRING_CREATOR_JDK11 != null && JDKUtils.BIG_ENDIAN) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, strlen == 0 ? LATIN1 : UTF16);
                } else {
                    str = new String(bytes, offset, strlen, StandardCharsets.UTF_16BE);
                }

                offset += strlen;
                return str;
            }
            case BC_STR_GB18030: {
                if (GB18030 == null) {
                    GB18030 = Charset.forName("GB18030");
                }
                int strlen = readLength();
                String str = new String(bytes, offset, strlen, GB18030);
                offset += strlen;
                return str;
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal;
            }
            case BC_DECIMAL_LONG: {
                return BigDecimal.valueOf(
                        readInt64Value()
                );
            }
            case BC_BINARY: {
                int len = readLength();
                byte[] binary = Arrays.copyOfRange(this.bytes, offset, offset + len);
                offset += len;
                return binary;
            }
            case BC_TIMESTAMP_MINUTES: {
                long minutes = getInt(bytes, offset);
                offset += 4;
                return new Date(minutes * 60L * 1000L);
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds = getInt(bytes, offset);
                offset += 4;
                return new Date(seconds * 1000);
            }
            case BC_LOCAL_DATE: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                byte month = bytes[offset++];
                byte dayOfMonth = bytes[offset++];
                return LocalDate.of(year, month, dayOfMonth);
            }
            case BC_LOCAL_TIME: {
                byte hour = bytes[offset++];
                byte minute = bytes[offset++];
                byte second = bytes[offset++];
                int nano = readInt32Value();
                return LocalTime.of(hour, minute, second, nano);
            }
            case BC_LOCAL_DATETIME: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                byte month = bytes[offset++];
                byte dayOfMonth = bytes[offset++];
                byte hour = bytes[offset++];
                byte minute = bytes[offset++];
                byte second = bytes[offset++];
                int nano = readInt32Value();
                return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
            }
            case BC_TIMESTAMP_WITH_TIMEZONE: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                byte month = bytes[offset++];
                byte dayOfMonth = bytes[offset++];
                byte hour = bytes[offset++];
                byte minute = bytes[offset++];
                byte second = bytes[offset++];
                int nano = readInt32Value();
                // SHANGHAI_ZONE_ID_NAME_BYTES
                ZoneId zoneId;
                {
                    boolean shanghai;
                    byte[] shanghaiZoneIdNameBytes = SHANGHAI_ZONE_ID_NAME_BYTES;
                    if (offset + shanghaiZoneIdNameBytes.length < bytes.length) {
                        shanghai = true;
                        for (int i = 0; i < shanghaiZoneIdNameBytes.length; ++i) {
                            if (bytes[offset + i] != shanghaiZoneIdNameBytes[i]) {
                                shanghai = false;
                                break;
                            }
                        }
                    } else {
                        shanghai = false;
                    }
                    if (shanghai) {
                        offset += shanghaiZoneIdNameBytes.length;
                        zoneId = SHANGHAI_ZONE_ID;
                    } else {
                        String zoneIdStr = readString();
                        zoneId = DateUtils.getZoneId(zoneIdStr, SHANGHAI_ZONE_ID);
                    }
                }
                LocalDateTime ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
                return ZonedDateTime.of(ldt, zoneId);
            }
            case BC_TIMESTAMP: {
                long epochSeconds = readInt64Value();
                int nano = readInt32Value();
                return Instant.ofEpochSecond(epochSeconds, nano);
            }
            case BC_TIMESTAMP_MILLIS: {
                long millis = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return new Date(BIG_ENDIAN ? millis : Long.reverseBytes(millis));
            }
            case BC_BIGINT_LONG: {
                return BigInteger.valueOf(
                        readInt64Value()
                );
            }
            case BC_TYPED_ANY: {
                long typeHash = readTypeHashCode();

                if (context.autoTypeBeforeHandler != null) {
                    Class<?> filterClass = context.autoTypeBeforeHandler.apply(typeHash, null, context.features);

                    if (filterClass == null) {
                        String typeName = getString();
                        filterClass = context.autoTypeBeforeHandler.apply(typeName, null, context.features);
                    }

                    if (filterClass != null) {
                        ObjectReader autoTypeObjectReader = context.getObjectReader(filterClass);
                        return autoTypeObjectReader.readJSONBObject(this, null, null, 0);
                    }
                }

                boolean supportAutoType = (context.features & Feature.SupportAutoType.mask) != 0;
                if (!supportAutoType) {
                    if (isObject()) {
                        return readObject();
                    }

                    if (isArray()) {
                        return readArray();
                    }

                    throw new JSONException("auoType not support , offset " + offset + "/" + bytes.length);
                }

                ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException("auoType not support : " + typeName + ", offset " + offset + "/" + bytes.length);
                    }
                }
                return autoTypeObjectReader.readJSONBObject(this, null, null, 0);
            }
            case BC_DOUBLE_NUM_0:
                return 0D;
            case BC_DOUBLE_NUM_1:
                return 1D;
            case BC_CHAR:
                int intValue = readInt32Value();
                return (char) intValue;
            case BC_OBJECT: {
                Map map = null;
                boolean supportAutoType = (context.features & Feature.SupportAutoType.mask) != 0;
                for (int i = 0; ; ++i) {
                    byte type = bytes[offset];
                    if (type == BC_OBJECT_END) {
                        offset++;
                        break;
                    }

                    Object name;
                    if (supportAutoType && i == 0 && type >= BC_STR_ASCII_FIX_MIN) {
                        long hash = readFieldNameHashCode();
                        if (hash == ObjectReader.HASH_TYPE) {
                            long typeHash = readValueHashCode();
                            ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                            if (autoTypeObjectReader == null) {
                                String typeName = getString();
                                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                                if (autoTypeObjectReader == null) {
                                    throw new JSONException("auotype not support : " + typeName + ", offset " + offset + "/" + bytes.length);
                                }
                            }

                            typeRedirect = true;
                            return autoTypeObjectReader.readJSONBObject(this, null, null, 0);
                        }
                        name = getFieldName();
                    } else {
                        if (type >= BC_STR_ASCII_FIX_MIN) {
                            name = readFieldName();
                        } else {
                            name = readAny();
                        }
                    }

                    if (map == null) {
                        if ((context.features & Feature.UseNativeObject.mask) != 0) {
                            map = new HashMap();
                        } else {
                            map = new JSONObject();
                        }
                    }

                    if (isReference()) {
                        String reference = readReference();
                        if ("..".equals(reference)) {
                            map.put(name, map);
                        } else {
                            addResolveTask(map, name, JSONPath.of(reference));
                            map.put(name, null);
                        }
                        continue;
                    }

                    byte valueType = bytes[offset];
                    Object value;
                    if (valueType >= BC_STR_ASCII_FIX_MIN && valueType <= BC_STR_GB18030) {
                        value = readString();
                    } else if (valueType >= BC_INT32_NUM_MIN && valueType <= BC_INT32_NUM_MAX) {
                        offset++;
                        value = (int) valueType;
                    } else if (valueType == BC_TRUE) {
                        offset++;
                        value = Boolean.TRUE;
                    } else if (valueType == BC_FALSE) {
                        offset++;
                        value = Boolean.FALSE;
                    } else if (valueType == BC_OBJECT) {
                        value = readObject();
                    } else {
                        value = readAny();
                    }

                    if (value == null && (context.features & Feature.IgnoreNullPropertyValue.mask) != 0) {
                        continue;
                    }

                    map.put(name, value);
                }

                if (map == null) {
                    if ((context.features & Feature.UseNativeObject.mask) != 0) {
                        map = new HashMap();
                    } else {
                        map = new JSONObject();
                    }
                }
                return map;
            }
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return (int) type;
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    return ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int int3 = getInt3(bytes, offset, type);
                    offset += 2;
                    return int3;
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    return (long) INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    return (long) ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    return (long) (((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF));
                }

                if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY) {
                    int len = type == BC_ARRAY
                            ? readLength()
                            : type - BC_ARRAY_FIX_MIN;

                    if (len == 0) {
                        if ((context.features & Feature.UseNativeObject.mask) != 0) {
                            return new ArrayList<>();
                        } else {
                            if (context.arraySupplier != null) {
                                return context.arraySupplier.get();
                            }
                            return new JSONArray();
                        }
                    }

                    List list;
                    if ((context.features & Feature.UseNativeObject.mask) != 0) {
                        list = new ArrayList(len);
                    } else {
                        list = new JSONArray(len);
                    }

                    for (int i = 0; i < len; ++i) {
                        if (isReference()) {
                            String reference = readReference();
                            if ("..".equals(reference)) {
                                list.add(list);
                            } else {
                                list.add(null);
                                addResolveTask(list, i, JSONPath.of(reference));
                            }
                            continue;
                        }

                        Object item = readAny();
                        list.add(item);
                    }
                    return list;
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII) {
                    strlen = type == BC_STR_ASCII
                            ? readLength()
                            : type - BC_STR_ASCII_FIX_MIN;

                    if (strlen < 0) {
                        return symbolTable.getName(-strlen);
                    }

                    if (STRING_CREATOR_JDK8 != null) {
                        char[] chars = new char[strlen];
                        for (int i = 0; i < strlen; ++i) {
                            chars[i] = (char) (bytes[offset + i] & 0xff);
                        }
                        offset += strlen;

                        String str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        if ((context.features & Feature.TrimString.mask) != 0) {
                            str = str.trim();
                        }
                        return str;
                    } else if (STRING_CREATOR_JDK11 != null) {
                        byte[] chars = new byte[strlen];
                        System.arraycopy(bytes, offset, chars, 0, strlen);
                        offset += strlen;
                        String str = STRING_CREATOR_JDK11.apply(chars, LATIN1);

                        if ((context.features & Feature.TrimString.mask) != 0) {
                            str = str.trim();
                        }
                        return str;
                    }
                    String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                    offset += strlen;

                    if ((context.features & Feature.TrimString.mask) != 0) {
                        str = str.trim();
                    }
                    return str;
                }

                if (type == BC_SYMBOL) {
                    strlen = readLength();

                    if (strlen >= 0) {
                        throw new JSONException("not support symbol : " + strlen);
                    }

                    return symbolTable.getName(-strlen);
                }

                throw new JSONException("not support type : " + error(type));
        }
    }

    @Override
    public byte getType() {
        return bytes[offset];
    }

    @Override
    public List readArray() {
        int entryCnt = startArray();
        JSONArray array = new JSONArray(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            byte valueType = bytes[offset];
            Object value;
            if (valueType >= BC_STR_ASCII_FIX_MIN && valueType <= BC_STR_GB18030) {
                value = readString();
            } else if (valueType >= BC_INT32_NUM_MIN && valueType <= BC_INT32_NUM_MAX) {
                offset++;
                value = (int) valueType;
            } else if (valueType == BC_TRUE) {
                offset++;
                value = Boolean.TRUE;
            } else if (valueType == BC_FALSE) {
                offset++;
                value = Boolean.FALSE;
            } else if (valueType == BC_OBJECT) {
                value = readObject();
            } else if (valueType == BC_INT64) {
                offset++;
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                value = BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value);
            } else if (valueType >= BC_ARRAY_FIX_MIN && valueType <= BC_ARRAY) {
                offset++;
                int len = valueType == BC_ARRAY
                        ? readLength()
                        : valueType - BC_ARRAY_FIX_MIN;

                if (len == 0) {
                    if ((context.features & Feature.UseNativeObject.mask) != 0) {
                        value = new ArrayList<>();
                    } else {
                        if (context.arraySupplier != null) {
                            value = context.arraySupplier.get();
                        } else {
                            value = new JSONArray();
                        }
                    }
                } else {
                    List list;
                    if ((context.features & Feature.UseNativeObject.mask) != 0) {
                        list = new ArrayList(len);
                    } else {
                        list = new JSONArray(len);
                    }

                    for (int j = 0; j < len; ++j) {
                        if (isReference()) {
                            String reference = readReference();
                            if ("..".equals(reference)) {
                                list.add(list);
                            } else {
                                list.add(null);
                                addResolveTask(list, j, JSONPath.of(reference));
                            }
                            continue;
                        }

                        byte itemType = bytes[offset];
                        Object item;
                        if (itemType >= BC_STR_ASCII_FIX_MIN && itemType <= BC_STR_GB18030) {
                            item = readString();
                        } else if (itemType == BC_OBJECT) {
                            item = readObject();
                        } else {
                            item = readAny();
                        }
                        list.add(item);
                    }
                    value = list;
                }
            } else if (valueType >= BC_INT32_BYTE_MIN && valueType <= BC_INT32_BYTE_MAX) {
                value = ((valueType - BC_INT32_BYTE_ZERO) << 8)
                        + (bytes[offset + 1] & 0xFF);
                offset += 2;
            } else if (valueType >= BC_INT32_SHORT_MIN && valueType <= BC_INT32_SHORT_MAX) {
                int int3 = getInt3(bytes, offset + 1, valueType);
                offset += 3;
                value = int3;
            } else if (valueType == BC_INT32) {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 1);
                offset += 5;
                value = BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
            } else if (valueType == BC_REFERENCE) {
                String reference = readReference();
                if ("..".equals(reference)) {
                    value = array;
                } else {
                    addResolveTask(array, i, JSONPath.of(reference));
                    continue;
                }
            } else {
                value = readAny();
            }
            array.add(value);
        }
        return array;
    }

    @Override
    public List readArray(Type itemType) {
        if (nextIfNull()) {
            return null;
        }

        if (bytes[offset] == BC_TYPED_ANY) {
            Object obj = readAny();
            if (obj instanceof List) {
                return (List) obj;
            }
            if (obj instanceof Collection) {
                return new JSONArray((Collection) obj);
            }
            throw new JSONException("not support class " + obj.getClass());
        }

        int entryCnt = startArray();
        JSONArray array = new JSONArray(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            array.add(read(itemType));
        }
        return array;
    }

    @Override
    public List readList(Type[] types) {
        if (nextIfNull()) {
            return null;
        }

        int entryCnt = startArray();
        JSONArray array = new JSONArray(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            Type itemType = types[i];
            array.add(read(itemType));
        }
        return array;
    }

    @Override
    public byte[] readHex() {
        String str = readString();
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; ++i) {
            char c0 = str.charAt(i * 2);
            char c1 = str.charAt(i * 2 + 1);

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            bytes[i] = (byte) ((b0 << 4) | b1);
        }
        return bytes;
    }

    @Override
    public boolean isReference() {
        return offset < bytes.length && bytes[offset] == BC_REFERENCE;
    }

    @Override
    public String readReference() {
        if (bytes[offset] != BC_REFERENCE) {
            return null;
        }
        offset++;
        if (isString()) {
            return readString();
        }

        throw new JSONException("reference not support input " + error(type));
    }

    Object readAnyObject() {
        if (bytes[offset] != BC_TYPED_ANY) {
            return readAny();
        }

        Context context = this.context;
        offset++;
        final long typeHash = readTypeHashCode();

        ObjectReader autoTypeObjectReader = null;
        AutoTypeBeforeHandler autoTypeBeforeHandler = context.autoTypeBeforeHandler;
        if (autoTypeBeforeHandler != null) {
            Class<?> objectClass = autoTypeBeforeHandler.apply(typeHash, Object.class, 0L);
            if (objectClass == null) {
                objectClass = autoTypeBeforeHandler.apply(getString(), Object.class, 0L);
            }
            if (objectClass != null) {
                autoTypeObjectReader = context.getObjectReader(objectClass);
            }
        }

        final long features = context.features;

        if (autoTypeObjectReader == null) {
            if ((features & Feature.SupportAutoType.mask) == 0) {
                if ((features & Feature.ErrorOnNotSupportAutoType.mask) == 0) {
                    return null;
                }
                autoTypeError();
            }
            autoTypeObjectReader = context.provider.getObjectReader(typeHash);
        }

        if (autoTypeObjectReader != null) {
            Class objectClass = autoTypeObjectReader.getObjectClass();
            if (objectClass != null) {
                ClassLoader classLoader = objectClass.getClassLoader();
                if (classLoader != null) {
                    ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                    if (classLoader != tcl) {
                        autoTypeObjectReader = getObjectReaderContext(autoTypeObjectReader, objectClass, tcl);
                    }
                }
            }
        }

        if (autoTypeObjectReader == null) {
            autoTypeObjectReader = context.provider.getObjectReader(getString(), Object.class, features);
            if (autoTypeObjectReader == null) {
                if ((features & Feature.ErrorOnNotSupportAutoType.mask) == 0) {
                    return null;
                }
                autoTypeError();
            }
        }

        this.type = bytes[offset];

        return autoTypeObjectReader.readJSONBObject(this, Object.class, null, context.features);
    }

    @Override
    public ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        ObjectReader autoTypeObjectReader = null;
        if (bytes[offset] == BC_TYPED_ANY) {
            offset++;

            Context context = this.context;
            final long typeHash = readTypeHashCode();
            if (expectClassHash == typeHash) {
                ObjectReader objectReader = context.getObjectReader(expectClass);
                Class objectClass = objectReader.getObjectClass();
                if (objectClass != null && objectClass == expectClass) {
                    context.provider.registerIfAbsent(typeHash, objectReader);
                    return objectReader;
                }
            }

            AutoTypeBeforeHandler autoTypeBeforeHandler = context.autoTypeBeforeHandler;
            if (autoTypeBeforeHandler != null) {
                ObjectReader objectReader = checkAutoTypeWithHandler(expectClass, features, autoTypeBeforeHandler, typeHash);
                if (objectReader != null) {
                    return objectReader;
                }
            }

            final long features2 = context.features | features;
            boolean isSupportAutoType = (features2 & Feature.SupportAutoType.mask) != 0;
            if (!isSupportAutoType) {
                if ((features2 & Feature.ErrorOnNotSupportAutoType.mask) == 0) {
                    return null;
                }
                autoTypeError();
            }

            autoTypeObjectReader = context.provider.getObjectReader(typeHash);

            if (autoTypeObjectReader != null) {
                Class objectClass = autoTypeObjectReader.getObjectClass();
                if (objectClass != null) {
                    ClassLoader classLoader = objectClass.getClassLoader();
                    if (classLoader != null) {
                        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
                        if (classLoader != tcl) {
                            autoTypeObjectReader = getObjectReaderContext(autoTypeObjectReader, objectClass, tcl);
                        }
                    }
                }
            }

            if (autoTypeObjectReader == null) {
                autoTypeObjectReader = context.provider.getObjectReader(getString(), expectClass, features2);
                if (autoTypeObjectReader == null) {
                    if ((features2 & Feature.ErrorOnNotSupportAutoType.mask) == 0) {
                        return null;
                    }
                    autoTypeError();
                }
            }

            this.type = bytes[offset];
        }
        return autoTypeObjectReader;
    }

    ObjectReader checkAutoTypeWithHandler(
            Class expectClass,
            long features,
            AutoTypeBeforeHandler autoTypeBeforeHandler,
            long typeHash
    ) {
        Class<?> objectClass = autoTypeBeforeHandler.apply(typeHash, expectClass, features);
        if (objectClass == null) {
            objectClass = autoTypeBeforeHandler.apply(getString(), expectClass, features);
        }
        if (objectClass != null) {
            return context.getObjectReader(objectClass);
        }
        return null;
    }

    void autoTypeError() {
        throw new JSONException("auotype not support : " + getString());
    }

    private ObjectReader getObjectReaderContext(
            ObjectReader autoTypeObjectReader,
            Class objectClass,
            ClassLoader contextClassLoader
    ) {
        String typeName = getString();
        Class contextClass = TypeUtils.getMapping(typeName);
        if (contextClass == null) {
            try {
                if (contextClassLoader == null) {
                    contextClassLoader = JSON.class.getClassLoader();
                }
                contextClass = contextClassLoader.loadClass(typeName);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (contextClass != null && !objectClass.equals(contextClass)) {
            autoTypeObjectReader = getObjectReader(contextClass);
        }
        return autoTypeObjectReader;
    }

    @Override
    public int startArray() {
        final byte type = this.type = bytes[offset++];

        if (type == BC_NULL) {
            return -1;
        }

        if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY_FIX_MAX) {
            ch = (char) -type;
            return type - BC_ARRAY_FIX_MIN;
        }

        if (type == BC_BINARY) {
            return readInt32Value();
        }

        if (type != BC_ARRAY) {
            throw new JSONException("array not support input " + error(type));
        }

        return readInt32Value();
    }

    public String error(byte type) {
        StringBuilder buf = new StringBuilder();

        buf.append(typeName(type));
        if (isString()) {
            int mark = offset;
            offset--;

            String str = null;
            try {
                str = readString();
            } catch (Throwable ignored) {
            }
            if (str != null) {
                buf.append(' ');
                buf.append(str);
            }

            offset = mark;
        }

        buf.append(", offset ");
        buf.append(offset);
        buf.append('/');
        buf.append(bytes.length);

        return buf.toString();
    }

    @Override
    public void next() {
        offset++;
    }

    @Override
    public long readFieldNameHashCode() {
        strtype = bytes[offset++];
        boolean typeSymbol = strtype == BC_SYMBOL;

        if (typeSymbol) {
            strtype = bytes[offset];
            if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32) {
                int symbol;
                if (strtype <= BC_INT32_NUM_MAX) {
                    offset++;
                    symbol = strtype;
                } else {
                    symbol = readInt32Value();
                }

                if (symbol < 0) {
                    return symbolTable.getHashCode(-symbol);
                }

                if (symbol == 0) {
                    strtype = symbol0StrType;
                    strlen = symbol0Length;
                    strBegin = symbol0Begin;
                    if (symbol0Hash == 0) {
                        symbol0Hash = getNameHashCode();
                    }
                    return symbol0Hash;
                }

                int index = symbol * 2;
                long strInfo = symbols[index + 1];
                this.strtype = (byte) strInfo;
                strlen = ((int) strInfo) >> 8;
                strBegin = (int) (strInfo >> 32);
                long nameHashCode = symbols[index];
                if (nameHashCode == 0) {
                    nameHashCode = getNameHashCode();
                    symbols[index] = nameHashCode;
                }
                return nameHashCode;
            }
            offset++;
        }

        strBegin = offset;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            strlen = strtype - BC_STR_ASCII_FIX_MIN;
        } else if (strtype == BC_STR_ASCII || strtype == BC_STR_UTF8) {
            strlen = readLength();
            strBegin = offset;
        } else {
            throw readFieldNameHashCodeEror();
        }

        long hashCode;
        if (strlen < 0) {
            hashCode = symbolTable.getHashCode(-strlen);
        } else {
            long nameValue = 0;
            if (strlen <= 8 && offset + strlen <= bytes.length) {
                switch (strlen) {
                    case 1:
                        nameValue = bytes[offset];
                        break;
                    case 2:
                        nameValue = UNSAFE.getShort(bytes, BASE + offset) & 0xFFFFL;
                        break;
                    case 3:
                        nameValue = (bytes[offset + 2] << 16)
                                + (UNSAFE.getShort(bytes, BASE + offset) & 0xFFFFL);
                        break;
                    case 4:
                        nameValue = UNSAFE.getInt(bytes, BASE + offset);
                        break;
                    case 5:
                        nameValue = (((long) bytes[offset + 4]) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 6:
                        nameValue = ((long) UNSAFE.getShort(bytes, BASE + offset + 4) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 7:
                        nameValue = (((long) bytes[offset + 6]) << 48)
                                + (((long) bytes[offset + 5] & 0xFFL) << 40)
                                + (((long) bytes[offset + 4] & 0xFFL) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 8:
                        nameValue = UNSAFE.getLong(bytes, BASE + offset);
                        break;
                    default:
                        break;
                }
            }

            if (nameValue != 0) {
                offset += strlen;
                hashCode = nameValue;
            } else {
                hashCode = Fnv.MAGIC_HASH_CODE;
                for (int i = 0; i < strlen; ++i) {
                    byte c = bytes[offset++];
                    hashCode ^= c;
                    hashCode *= Fnv.MAGIC_PRIME;
                }
            }
        }

        if (typeSymbol) {
            int symbol;
            if ((type = bytes[offset]) >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                symbol = type;
                offset++;
            } else {
                symbol = readInt32Value();
            }

            if (symbol == 0) {
                symbol0Begin = strBegin;
                symbol0Length = strlen;
                symbol0StrType = strtype;
                symbol0Hash = hashCode;
                return hashCode;
            }

            long strInfo = ((long) strBegin << 32) + ((long) strlen << 8) + strtype;

            int symbolIndex = symbol << 1;
            int minCapacity = symbolIndex + 2;
            if (symbols == null) {
                symbols = new long[Math.max(minCapacity, 32)];
            } else if (symbols.length < minCapacity) {
                symbols = Arrays.copyOf(symbols, minCapacity + 16);
            }

            symbols[symbolIndex] = hashCode;
            symbols[symbolIndex + 1] = strInfo;
        }

        return hashCode;
    }

    protected JSONException readFieldNameHashCodeEror() {
        StringBuilder message = new StringBuilder()
                .append("fieldName not support input type ")
                .append(typeName(strtype));
        if (strtype == BC_REFERENCE) {
            message.append(" ")
                    .append(readString());
        }

        message.append(", offset ")
                .append(offset);
        return new JSONException(message.toString());
    }

    @Override
    public boolean isInt() {
        int type = bytes[offset];
        return (type >= BC_BIGINT_LONG && type <= BC_INT32)
                || type == BC_TIMESTAMP_SECONDS
                || type == BC_TIMESTAMP_MINUTES
                || type == BC_TIMESTAMP_MILLIS;
    }

    @Override
    public boolean isNull() {
        return bytes[offset] == BC_NULL;
    }

    @Override
    public Date readNullOrNewDate() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfNull() {
        if (bytes[offset] == BC_NULL) {
            offset++;
            return true;
        }
        return false;
    }

    @Override
    public void readNull() {
        type = bytes[offset++];
        if (type != BC_NULL) {
            throw new JSONException("null not match, " + type);
        }
    }

    @Override
    public boolean readIfNull() {
        if (bytes[offset] == BC_NULL) {
            offset++;
            return true;
        }
        return false;
    }

    @Override
    public long readTypeHashCode() {
        strtype = bytes[offset];

        final byte[] bytes = this.bytes;
        boolean typeSymbol = strtype == BC_SYMBOL;
        if (typeSymbol) {
            offset++;
            strtype = bytes[offset];
            if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32) {
                int symbol;
                if (strtype <= BC_INT32_NUM_MAX) {
                    offset++;
                    symbol = strtype;
                } else {
                    symbol = readInt32Value();
                }

                if (symbol < 0) {
                    return symbolTable.getHashCode(-symbol);
                }

                if (symbol == 0) {
                    strtype = symbol0StrType;
                    strlen = symbol0Length;
                    strBegin = symbol0Begin;
                    if (symbol0Hash == 0) {
                        symbol0Hash = getNameHashCode();
                    }
                    return symbol0Hash;
                }

                int index = symbol * 2;
                long strInfo = symbols[index + 1];
                this.strtype = (byte) strInfo;
                strlen = ((int) strInfo) >> 8;
                strBegin = (int) (strInfo >> 32);
                long nameHashCode = symbols[index];
                if (nameHashCode == 0) {
                    nameHashCode = getNameHashCode();
                    symbols[index] = nameHashCode;
                }
                return nameHashCode;
            }
        }

        if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32) {
            int typeIndex;
            if (strtype <= BC_INT32_NUM_MAX) {
                offset++;
                typeIndex = strtype;
            } else if (strtype <= BC_INT32_BYTE_MAX) {
                offset++;
                typeIndex = ((strtype - BC_INT32_BYTE_ZERO) << 8)
                        + (bytes[offset++] & 0xFF);
            } else {
                typeIndex = readInt32Value();
            }

            long refTypeHash;
            if (typeIndex == 0) {
                strtype = symbol0StrType;
                strlen = symbol0Length;
                strBegin = symbol0Begin;
                if (symbol0Hash == 0) {
                    symbol0Hash = Fnv.hashCode64(getString());
                }
                refTypeHash = symbol0Hash;
            } else if (typeIndex < 0) {
                strlen = strtype;
                refTypeHash = symbolTable.getHashCode(-typeIndex);
            } else {
                refTypeHash = symbols[typeIndex * 2];
                if (refTypeHash == 0) {
                    long strInfo = symbols[typeIndex * 2 + 1];
                    strtype = (byte) strInfo;
                    strlen = ((int) strInfo) >> 8;
                    strBegin = (int) (strInfo >> 32);
                    refTypeHash = Fnv.hashCode64(getString());
                }
            }

            if (refTypeHash == -1) {
                throw new JSONException("type ref not found : " + typeIndex);
            }

            return refTypeHash;
        }

        offset++;
        strBegin = offset;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            strlen = strtype - BC_STR_ASCII_FIX_MIN;
        } else if (strtype == BC_STR_ASCII
                || strtype == BC_STR_UTF8
                || strtype == BC_STR_UTF16
                || strtype == BC_STR_UTF16LE
                || strtype == BC_STR_UTF16BE
        ) {
            byte strType = bytes[offset];
            if (strType >= BC_INT32_NUM_MIN && strType <= BC_INT32_NUM_MAX) {
                offset++;
                strlen = strType;
            } else if (strType >= BC_INT32_BYTE_MIN && strType <= BC_INT32_BYTE_MAX) {
                offset++;
                strlen = ((strType - BC_INT32_BYTE_ZERO) << 8)
                        + (bytes[offset++] & 0xFF);
            } else {
                strlen = readLength();
            }
            strBegin = offset;
        } else {
            throw new JSONException("string value not support input " + typeName(type)
                    + " offset " + offset + "/" + bytes.length);
        }

        long hashCode;
        if (strlen < 0) {
            hashCode = symbolTable.getHashCode(-strlen);
        } else if (strtype == BC_STR_UTF8) {
            hashCode = Fnv.MAGIC_HASH_CODE;
            int end = offset + strlen;
            while (offset < end) {
                int c = bytes[offset];

                if (c >= 0) {
                    offset++;
                } else {
                    c &= 0xFF;
                    switch (c >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            int c2 = bytes[offset + 1];
                            if ((c2 & 0xC0) != 0x80) {
                                throw new JSONException("malformed input around byte " + offset);
                            }
                            c = (char) (((c & 0x1F) << 6)
                                    | (c2 & 0x3F));
                            offset += 2;
                            break;
                        }
                        case 14: {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                                throw new JSONException("malformed input around byte " + offset);
                            }
                            c = (char) (((c & 0x0F) << 12) |
                                    ((c2 & 0x3F) << 6) |
                                    ((c3 & 0x3F)));
                            offset += 3;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }

                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else if (strtype == BC_STR_UTF16 || strtype == BC_STR_UTF16BE) {
            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; i < strlen; i += 2) {
                byte c0 = bytes[offset + i];
                byte c1 = bytes[offset + i + 1];
                char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else if (strtype == BC_STR_UTF16LE) {
            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; i < strlen; i += 2) {
                byte c0 = bytes[offset + i];
                byte c1 = bytes[offset + i + 1];
                char ch = (char) ((c0 & 0xff) | ((c1 & 0xff) << 8));
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else {
            long nameValue = 0;
            if (strlen <= 8) {
                for (int i = 0, start = offset; i < strlen; offset++, i++) {
                    byte c = bytes[offset];
                    if (c < 0 || (c == 0 && bytes[start] == 0)) {
                        nameValue = 0;
                        offset = start;
                        break;
                    }

                    switch (i) {
                        case 0:
                            nameValue = c;
                            break;
                        case 1:
                            nameValue = ((c) << 8) + (nameValue & 0xFFL);
                            break;
                        case 2:
                            nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                            break;
                        case 3:
                            nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                            break;
                        case 4:
                            nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                            break;
                        case 5:
                            nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                            break;
                        case 6:
                            nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                            break;
                        case 7:
                            nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                            break;
                        default:
                            break;
                    }
                }
            }

            if (nameValue != 0) {
                hashCode = nameValue;
            } else {
                hashCode = Fnv.MAGIC_HASH_CODE;
                for (int i = 0; i < strlen; ++i) {
                    byte c = bytes[offset++];
                    hashCode ^= c;
                    hashCode *= Fnv.MAGIC_PRIME;
                }
            }
        }

        int symbol;
        if ((type = bytes[offset]) >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            symbol = type;
            offset++;
        } else {
            symbol = readInt32Value();
        }

        if (symbol == 0) {
            symbol0Begin = strBegin;
            symbol0Length = strlen;
            symbol0StrType = strtype;
            symbol0Hash = hashCode;
        } else {
            int minCapacity = symbol * 2 + 2;

            if (symbols == null) {
                symbols = new long[Math.max(minCapacity, 32)];
            } else if (symbols.length < minCapacity) {
                symbols = Arrays.copyOf(symbols, minCapacity + 16);
            }

            long strInfo = ((long) strBegin << 32) + ((long) strlen << 8) + strtype;
            symbols[symbol * 2 + 1] = strInfo;
        }

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        strtype = bytes[offset];

        offset++;
        strBegin = offset;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            strlen = strtype - BC_STR_ASCII_FIX_MIN;
        } else if (strtype == BC_STR_ASCII
                || strtype == BC_STR_UTF8
                || strtype == BC_STR_UTF16
                || strtype == BC_STR_UTF16LE
                || strtype == BC_STR_UTF16BE
        ) {
            strlen = readLength();
            strBegin = offset;
        } else if (strtype == BC_SYMBOL) {
            strlen = readLength();
            strBegin = offset;
        } else {
            throw new JSONException("string value not support input " + typeName(type)
                    + " offset " + offset + "/" + bytes.length);
        }

        long hashCode;
        if (strlen < 0) {
            hashCode = symbolTable.getHashCode(-strlen);
        } else if (strtype == BC_STR_UTF8) {
            hashCode = Fnv.MAGIC_HASH_CODE;
            int end = offset + strlen;
            while (offset < end) {
                int c = bytes[offset];

                if (c >= 0) {
                    offset++;
                } else {
                    c &= 0xFF;
                    switch (c >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            int c2 = bytes[offset + 1];
                            if ((c2 & 0xC0) != 0x80) {
                                throw new JSONException("malformed input around byte " + offset);
                            }
                            c = (char) (((c & 0x1F) << 6)
                                    | (c2 & 0x3F));
                            offset += 2;
                            break;
                        }
                        case 14: {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                                throw new JSONException("malformed input around byte " + offset);
                            }
                            c = (char) (((c & 0x0F) << 12) |
                                    ((c2 & 0x3F) << 6) |
                                    ((c3 & 0x3F)));
                            offset += 3;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }

                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else if (strtype == BC_STR_UTF16) {
            hashCode = Fnv.MAGIC_HASH_CODE;
            if (bytes[offset] == (byte) 0xFE
                    && bytes[offset + 1] == (byte) 0xFF
            ) {
                if (strlen <= 16) {
                    long nameValue = 0;
                    for (int i = 2; i < strlen; i += 2) {
                        byte c0 = bytes[offset + i];
                        byte c1 = bytes[offset + i + 1];
                        char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));

                        if (ch > 0x7F || (i == 0 && ch == 0)) {
                            nameValue = 0;
                            break;
                        }

                        byte c = (byte) ch;
                        switch ((i - 2) >> 1) {
                            case 0:
                                nameValue = c;
                                break;
                            case 1:
                                nameValue = ((c) << 8) + (nameValue & 0xFFL);
                                break;
                            case 2:
                                nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                                break;
                            case 3:
                                nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                                break;
                            case 4:
                                nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                                break;
                            case 5:
                                nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                                break;
                            case 6:
                                nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                                break;
                            case 7:
                                nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                                break;
                            default:
                                break;
                        }
                    }

                    if (nameValue != 0) {
                        return nameValue;
                    }
                }

                for (int i = 2; i < strlen; i += 2) {
                    byte c0 = bytes[offset + i];
                    byte c1 = bytes[offset + i + 1];
                    char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                }
            } else if (bytes[offset] == (byte) 0xFF
                    && bytes[offset + 1] == (byte) 0xFE
            ) {
                for (int i = 2; i < strlen; i += 2) {
                    byte c1 = bytes[offset + i];
                    byte c0 = bytes[offset + i + 1];
                    char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                }
            } else {
                for (int i = 0; i < strlen; i += 2) {
                    byte c0 = bytes[offset + i];
                    byte c1 = bytes[offset + i + 1];
                    char ch = (char) ((c0 & 0xff) | ((c1 & 0xff) << 8));
                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                }
            }
        } else if (strtype == BC_STR_UTF16BE) {
            if (strlen <= 16) {
                long nameValue = 0;
                for (int i = 0; i < strlen; i += 2) {
                    byte c0 = bytes[offset + i];
                    byte c1 = bytes[offset + i + 1];
                    char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));

                    if (ch > 0x7F || (i == 0 && ch == 0)) {
                        nameValue = 0;
                        break;
                    }

                    byte c = (byte) ch;
                    switch (i >> 1) {
                        case 0:
                            nameValue = c;
                            break;
                        case 1:
                            nameValue = ((c) << 8) + (nameValue & 0xFFL);
                            break;
                        case 2:
                            nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                            break;
                        case 3:
                            nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                            break;
                        case 4:
                            nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                            break;
                        case 5:
                            nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                            break;
                        case 6:
                            nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                            break;
                        case 7:
                            nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                            break;
                        default:
                            break;
                    }
                }

                if (nameValue != 0) {
                    return nameValue;
                }
            }

            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; i < strlen; i += 2) {
                byte c0 = bytes[offset + i];
                byte c1 = bytes[offset + i + 1];
                char ch = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else if (strtype == BC_STR_UTF16LE) {
            if (strlen <= 16) {
                long nameValue = 0;
                for (int i = 0; i < strlen; i += 2) {
                    byte c0 = bytes[offset + i];
                    byte c1 = bytes[offset + i + 1];
                    char ch = (char) ((c0 & 0xff) | ((c1 & 0xff) << 8));

                    if (ch > 0x7F || (i == 0 && ch == 0)) {
                        nameValue = 0;
                        break;
                    }

                    byte c = (byte) ch;
                    switch (i >> 1) {
                        case 0:
                            nameValue = c;
                            break;
                        case 1:
                            nameValue = ((c) << 8) + (nameValue & 0xFFL);
                            break;
                        case 2:
                            nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                            break;
                        case 3:
                            nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                            break;
                        case 4:
                            nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                            break;
                        case 5:
                            nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                            break;
                        case 6:
                            nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                            break;
                        case 7:
                            nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                            break;
                        default:
                            break;
                    }
                }

                if (nameValue != 0) {
                    return nameValue;
                }
            }

            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; i < strlen; i += 2) {
                byte c0 = bytes[offset + i];
                byte c1 = bytes[offset + i + 1];
                char ch = (char) ((c0 & 0xff) | ((c1 & 0xff) << 8));
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        } else {
            if (strlen <= 8) {
                long nameValue = 0;
                for (int i = 0, start = offset; i < strlen; offset++, i++) {
                    byte c = bytes[offset];
                    if (c < 0 || (c == 0 && bytes[start] == 0)) {
                        nameValue = 0;
                        offset = start;
                        break;
                    }

                    switch (i) {
                        case 0:
                            nameValue = c;
                            break;
                        case 1:
                            nameValue = ((c) << 8) + (nameValue & 0xFFL);
                            break;
                        case 2:
                            nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                            break;
                        case 3:
                            nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                            break;
                        case 4:
                            nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                            break;
                        case 5:
                            nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                            break;
                        case 6:
                            nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                            break;
                        case 7:
                            nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                            break;
                        default:
                            break;
                    }
                }

                if (nameValue != 0) {
                    return nameValue;
                }
            }

            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; i < strlen; ++i) {
                byte c = bytes[offset++];
                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        }

        return hashCode;
    }

    protected long getNameHashCode() {
        int offset = strBegin;
        long nameValue = 0;
        for (int i = 0; i < strlen; offset++) {
            byte c = bytes[offset];
            if (c < 0 || i >= 8 || (i == 0 && bytes[strBegin] == 0)) {
                offset = strBegin;
                nameValue = 0;
                break;
            }

            switch (i) {
                case 0:
                    nameValue = c;
                    break;
                case 1:
                    nameValue = ((c) << 8) + (nameValue & 0xFFL);
                    break;
                case 2:
                    nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                    break;
                case 3:
                    nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                    break;
                case 4:
                    nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                    break;
                case 5:
                    nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                    break;
                case 6:
                    nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                    break;
                case 7:
                    nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                    break;
                default:
                    break;
            }
            i++;
        }

        if (nameValue != 0) {
            return nameValue;
        }

        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; i < strlen; ++i) {
            byte c = bytes[offset++];
            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }
        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        int offset = strBegin;
        long nameValue = 0;
        for (int i = 0; i < strlen; offset++) {
            byte c = bytes[offset];
            if (c < 0 || i >= 8 || (i == 0 && bytes[strBegin] == 0)) {
                offset = strBegin;
                nameValue = 0;
                break;
            }

            if (c == '_' || c == '-' || c == ' ') {
                byte c1 = bytes[offset + 1];
                if (c1 != c) {
                    continue;
                }
            }

            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }

            switch (i) {
                case 0:
                    nameValue = c;
                    break;
                case 1:
                    nameValue = ((c) << 8) + (nameValue & 0xFFL);
                    break;
                case 2:
                    nameValue = ((c) << 16) + (nameValue & 0xFFFFL);
                    break;
                case 3:
                    nameValue = ((c) << 24) + (nameValue & 0xFFFFFFL);
                    break;
                case 4:
                    nameValue = (((long) c) << 32) + (nameValue & 0xFFFFFFFFL);
                    break;
                case 5:
                    nameValue = (((long) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                    break;
                case 6:
                    nameValue = (((long) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                    break;
                case 7:
                    nameValue = (((long) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                    break;
                default:
                    break;
            }
            i++;
        }

        if (nameValue != 0) {
            return nameValue;
        }

        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; i < strlen; ++i) {
            byte c = bytes[offset++];
            if (c >= 'A' && c <= 'Z') {
                c = (byte) (c + 32);
            }

            if (c == '_' || c == '-' || c == ' ') {
                continue;
            }

            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }
        return hashCode;
    }

    @Override
    public void skipValue() {
        byte type = bytes[offset++];
        switch (type) {
            case BC_NULL:
            case BC_ARRAY_FIX_0:
            case BC_STR_ASCII_FIX_0:
            case BC_FALSE:
            case BC_TRUE:
                return;
            case BC_INT8:
                offset++;
                return;
            case BC_INT16:
                offset += 2;
                return;
            case BC_INT32:
            case BC_TIMESTAMP_SECONDS:
            case BC_TIMESTAMP_MINUTES:
            case BC_FLOAT:
                offset += 4;
                return;
            case BC_FLOAT_INT:
                readInt32Value(); // skip
                return;
            case BC_INT64:
            case BC_TIMESTAMP_MILLIS:
            case BC_DOUBLE:
                offset += 8;
                return;
            case BC_DOUBLE_LONG:
            case BC_DECIMAL_LONG:
                readInt64Value();
                return;
            case BC_DECIMAL:
                // TODO skip big decimal
                readInt32Value();
                readBigInteger();
                return;
            case BC_LOCAL_TIME:
                offset += 3;
                readInt32Value(); // skip
                return;
            case BC_LOCAL_DATETIME:
                offset += 7;
                readInt32Value(); // skip
                return;
            case BC_TIMESTAMP_WITH_TIMEZONE:
                offset += 7;
                readInt32Value(); // nano
                readString(); // skip
                return;
            case BC_BINARY:
                int byteslen = readInt32Value();
                offset += byteslen;
                return;
            case BC_STR_ASCII:
            case BC_STR_UTF8:
            case BC_STR_UTF16:
            case BC_STR_UTF16LE:
            case BC_STR_UTF16BE:
                int strlen = readInt32Value();
                offset += strlen;
                return;
            case BC_TYPED_ANY: {
                readTypeHashCode();
                skipValue();
                return;
            }
            case BC_OBJECT: {
                while (true) {
                    if (bytes[offset] == BC_OBJECT_END) {
                        offset++;
                        break;
                    }
                    skipName();
                    skipValue();
                }
                return;
            }
            case BC_REFERENCE: {
                if (isString()) {
                    skipName();
                    return;
                }
                throw new JSONException("skip not support type " + typeName(type));
            }
            default:
                // [-16, 47]
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return;
                }

                // [-40, -17]
                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    return;
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    offset++;
                    return;
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    offset += (type - BC_STR_ASCII_FIX_MIN);
                    return;
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    offset++;
                    return;
                }

                if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY) {
                    int itemCnt;
                    if (type == BC_ARRAY) {
                        itemCnt = readInt32Value();
                    } else {
                        itemCnt = type - BC_ARRAY_FIX_MIN;
                    }
                    for (int i = 0; i < itemCnt; ++i) {
                        skipValue();
                    }
                    return;
                }

                throw new JSONException("skip not support type " + typeName(type));
        }
    }

    @Override
    public boolean skipName() {
        strtype = bytes[offset++];
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            offset += (strtype - BC_STR_ASCII_FIX_MIN);
            return true;
        }

        if (strtype == BC_STR_ASCII
                || strtype == BC_STR_UTF8
                || strtype == BC_STR_UTF16
                || strtype == BC_STR_UTF16LE
                || strtype == BC_STR_UTF16BE
        ) {
            strlen = readLength();
            offset += strlen;
            return true;
        }

        if (strtype == BC_SYMBOL) {
            int type = bytes[offset];
            if (type >= BC_INT32_NUM_MIN && type <= BC_INT32) {
                readInt32Value();
                return true;
            }

            String str = readString();
            readInt32Value();
            return true;
        }

        throw new JSONException("name not support input : " + typeName(strtype));
    }

    @Override
    public String readFieldName() {
        strtype = bytes[offset];
        if (strtype == BC_NULL) {
            offset++;
            return null;
        }
        offset++;

        boolean typeSymbol = strtype == BC_SYMBOL;
        if (typeSymbol) {
            strtype = bytes[offset];
            if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32) {
                int symbol = readInt32Value();
                if (symbol < 0) {
                    return symbolTable.getName(-symbol);
                }

                if (symbol == 0) {
                    strtype = symbol0StrType;
                    strlen = symbol0Length;
                    strBegin = symbol0Begin;
                    return getFieldName();
                }

                int index = symbol * 2 + 1;
                long strInfo = symbols[index];
                strtype = (byte) strInfo;
                strlen = ((int) strInfo) >> 8;
                strBegin = (int) (strInfo >> 32);
                return getString();
            }
            offset++;
        }

        strBegin = offset;
        Charset charset = null;
        String str = null;
        if (strtype == BC_STR_ASCII_FIX_MIN + 1) {
            str = TypeUtils.toString((char) (bytes[offset] & 0xff));
            strlen = 1;
            offset++;
        } else if (strtype == BC_STR_ASCII_FIX_MIN + 2) {
            str = TypeUtils.toString(
                    (char) (bytes[offset] & 0xff),
                    (char) (bytes[offset + 1] & 0xff)
            );
            strlen = 2;
            offset += 2;
        } else if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            long nameValue0 = -1, nameValue1 = -1;

            if (strtype == BC_STR_ASCII) {
                strlen = readLength();
                strBegin = offset;
            } else {
                strlen = strtype - BC_STR_ASCII_FIX_MIN;

                if (offset + strlen > bytes.length) {
                    throw new JSONException("illegal jsonb data");
                }

                switch (strlen) {
                    case 3:
                        nameValue0
                                = (bytes[offset + 2] << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 4:
                        nameValue0 = UNSAFE.getInt(bytes, BASE + offset);
                        break;
                    case 5:
                        nameValue0
                                = (((long) bytes[offset + 4]) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 6:
                        nameValue0
                                = (((long) bytes[offset + 5]) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 7:
                        nameValue0
                                = (((long) bytes[offset + 6]) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        break;
                    case 8:
                        nameValue0 = UNSAFE.getLong(bytes, BASE + offset);
                        break;
                    case 9:
                        nameValue0 = bytes[offset];
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 1);
                        break;
                    case 10:
                        nameValue0 = UNSAFE.getShort(bytes, BASE + offset);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 2);
                        break;
                    case 11:
                        nameValue0
                                = (bytes[offset] << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset + 2] & 0xFFL);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 3);
                        break;
                    case 12:
                        nameValue0 = UNSAFE.getInt(bytes, BASE + offset);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 4);
                        break;
                    case 13:
                        nameValue0
                                = (((long) bytes[offset + 4]) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 5);
                        break;
                    case 14:
                        nameValue0
                                = (((long) bytes[offset + 5]) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 6);
                        break;
                    case 15:
                        nameValue0
                                = (((long) bytes[offset + 6]) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + (UNSAFE.getInt(bytes, BASE + offset) & 0xFFFFFFFFL);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 7);
                        break;
                    case 16:
                        nameValue0 = UNSAFE.getLong(bytes, BASE + offset);
                        nameValue1 = UNSAFE.getLong(bytes, BASE + offset + 8);
                        break;
                    default:
                        break;
                }
            }

            if (bytes[offset + strlen - 1] > 0 && nameValue0 != -1) {
                if (nameValue1 != -1) {
                    long nameValue01 = nameValue0 ^ nameValue1;
                    int indexMask = ((int) (nameValue01 ^ (nameValue01 >>> 32))) & (NAME_CACHE2.length - 1);
                    JSONFactory.NameCacheEntry2 entry = NAME_CACHE2[indexMask];
                    if (entry == null) {
                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) (bytes[offset + i] & 0xff);
                            }
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                        }

                        NAME_CACHE2[indexMask] = new JSONFactory.NameCacheEntry2(name, nameValue0, nameValue1);
                        offset += strlen;
                        str = name;
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                        offset += strlen;
                        str = entry.name;
                    }
                } else {
                    int indexMask = ((int) (nameValue0 ^ (nameValue0 >>> 32))) & (NAME_CACHE.length - 1);
                    JSONFactory.NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) (bytes[offset + i] & 0xff);
                            }
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                        }

                        NAME_CACHE[indexMask] = new JSONFactory.NameCacheEntry(name, nameValue0);
                        offset += strlen;
                        str = name;
                    } else if (entry.value == nameValue0) {
                        offset += strlen;
                        str = entry.name;
                    }
                }
            }

            if (str == null) {
                if (STRING_CREATOR_JDK8 != null && strlen >= 0) {
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) (bytes[offset + i] & 0xff);
                    }
                    offset += strlen;
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null && strlen >= 0) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, LATIN1);
                    offset += strlen;
                }
                charset = StandardCharsets.ISO_8859_1;
            }
        } else if (strtype == BC_STR_UTF8) {
            strlen = readLength();
            strBegin = offset;

            if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                if (valueBytes == null) {
                    valueBytes = BYTES_UPDATER.getAndSet(cacheItem, null);
                    if (valueBytes == null) {
                        valueBytes = new byte[8192];
                    }
                }

                int minCapacity = strlen << 1;
                if (minCapacity > valueBytes.length) {
                    valueBytes = new byte[minCapacity];
                }

                int utf16_len = IOUtils.decodeUTF8(bytes, offset, strlen, valueBytes);
                if (utf16_len != -1) {
                    byte[] value = new byte[utf16_len];
                    System.arraycopy(valueBytes, 0, value, 0, utf16_len);
                    str = STRING_CREATOR_JDK11.apply(value, UTF16);
                    offset += strlen;
                }
            }

            charset = StandardCharsets.UTF_8;
        } else if (strtype == BC_STR_UTF16) {
            strlen = readLength();
            strBegin = offset;
            charset = StandardCharsets.UTF_16;
        } else if (strtype == BC_STR_UTF16LE) {
            strlen = readLength();
            strBegin = offset;

            if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);
                str = STRING_CREATOR_JDK11.apply(chars, UTF16);
                offset += strlen;
            }

            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            strlen = readLength();
            strBegin = offset;

            if (STRING_CREATOR_JDK11 != null && JDKUtils.BIG_ENDIAN) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);
                str = STRING_CREATOR_JDK11.apply(chars, UTF16);
                offset += strlen;
            }

            charset = StandardCharsets.UTF_16BE;
        } else if (strtype == BC_STR_GB18030) {
            strlen = readLength();

            if (GB18030 == null) {
                GB18030 = Charset.forName("GB18030");
            }
            charset = GB18030;
        }

        if (strlen < 0) {
            str = symbolTable.getName(-strlen);
        }

        if (str == null) {
            str = new String(bytes, offset, strlen, charset);
            offset += strlen;
        }

        if (typeSymbol) {
            int symbol = readInt32Value();

            if (symbol == 0) {
                symbol0Begin = strBegin;
                symbol0Length = strlen;
                symbol0StrType = strtype;
            } else {
                int minCapacity = symbol * 2 + 2;
                if (symbols == null) {
                    symbols = new long[Math.max(minCapacity, 32)];
                } else if (symbols.length < minCapacity) {
                    symbols = Arrays.copyOf(symbols, symbols.length + 16);
                }

                long strInfo = ((long) strBegin << 32) + ((long) strlen << 8) + strtype;
                symbols[symbol * 2 + 1] = strInfo;
            }
        }

        return str;
    }

    @Override
    public String getFieldName() {
        return getString();
    }

    @Override
    public String readString() {
        final byte strtype = bytes[offset++];
        this.strtype = strtype;
        if (strtype == BC_NULL) {
            return null;
        }

        strBegin = offset;
        String str = null;
        boolean ascii = false;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            ascii = true;
            final int strlen;
            if (strtype == BC_STR_ASCII) {
                byte strType = bytes[offset];
                if (strType >= BC_INT32_NUM_MIN && strType <= BC_INT32_NUM_MAX) {
                    offset++;
                    strlen = strType;
                } else {
                    strlen = readLength();
                }
                strBegin = offset;
            } else {
                strlen = strtype - BC_STR_ASCII_FIX_MIN;
            }
            this.strlen = strlen;

            if (strlen >= 0) {
                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) (bytes[offset + i] & 0xff);
                    }
                    offset += strlen;
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, LATIN1);
                    offset += strlen;
                }
            }

            if (str != null) {
                if ((context.features & Feature.TrimString.mask) != 0) {
                    str = str.trim();
                }
                return str;
            }
        }

        return readStringNonAscii(null, ascii);
    }

    private String readStringNonAscii(String str, boolean ascii) {
        Charset charset;
        if (ascii) {
            charset = StandardCharsets.ISO_8859_1;
        } else if (strtype == BC_STR_UTF8) {
            str = readStringUTF8();
            charset = StandardCharsets.UTF_8;
        } else if (strtype == BC_STR_UTF16) {
            strlen = readLength();
            strBegin = offset;
            charset = StandardCharsets.UTF_16;
        } else if (strtype == BC_STR_UTF16LE) {
            str = readUTF16LE();
            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            str = readUTF16BE();
            if (str != null) {
                return str;
            }
            charset = StandardCharsets.UTF_16BE;
        } else if (strtype == BC_STR_GB18030) {
            readGB18030();
            charset = GB18030;
        } else {
            return readStringTypeNotMatch();
        }

        if (str != null) {
            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }
            return str;
        }

        return readString(charset);
    }

    private String readString(Charset charset) {
        String str;
        if (strlen < 0) {
            return symbolTable.getName(-strlen);
        }

        char[] chars = null;
        if (JVM_VERSION == 8 && strtype == BC_STR_UTF8 && strlen < 8192) {
            final int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
            final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
            chars = CHARS_UPDATER.getAndSet(cacheItem, null);
            if (chars == null) {
                chars = new char[8192];
            }
        }
        if (chars != null) {
            int len = IOUtils.decodeUTF8(bytes, offset, strlen, chars);
            str = new String(chars, 0, len);
            if (chars.length < CACHE_THRESHOLD) {
                CHARS_UPDATER.lazySet(cacheItem, chars);
            }
        } else {
            str = new String(bytes, offset, strlen, charset);
        }
        offset += strlen;

        if ((context.features & Feature.TrimString.mask) != 0) {
            str = str.trim();
        }

        return str;
    }

    private void readGB18030() {
        strlen = readLength();
        strBegin = offset;

        if (GB18030 == null) {
            GB18030 = Charset.forName("GB18030");
        }
    }

    private String readUTF16BE() {
        strlen = readLength();
        strBegin = offset;

        if (STRING_CREATOR_JDK11 != null && JDKUtils.BIG_ENDIAN) {
            byte[] chars = new byte[strlen];
            System.arraycopy(bytes, offset, chars, 0, strlen);
            String str = STRING_CREATOR_JDK11.apply(chars, UTF16);
            offset += strlen;

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            return str;
        }

        return null;
    }

    private String readUTF16LE() {
        byte strType = bytes[offset];
        if (strType >= BC_INT32_NUM_MIN && strType <= BC_INT32_NUM_MAX) {
            offset++;
            strlen = strType;
        } else if (strType >= BC_INT32_BYTE_MIN && strType <= BC_INT32_BYTE_MAX) {
            offset++;
            strlen = ((strType - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        } else {
            strlen = readLength();
        }
        strBegin = offset;

        if (strlen == 0) {
            return "";
        }

        if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
            byte[] chars = new byte[strlen];
            System.arraycopy(bytes, offset, chars, 0, strlen);
            String str = STRING_CREATOR_JDK11.apply(chars, UTF16);
            offset += strlen;

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }
            return str;
        }
        return null;
    }

    private String readStringUTF8() {
        byte strType = bytes[offset];
        if (strType >= BC_INT32_NUM_MIN && strType <= BC_INT32_NUM_MAX) {
            offset++;
            strlen = strType;
        } else if (strType >= BC_INT32_BYTE_MIN && strType <= BC_INT32_BYTE_MAX) {
            offset++;
            strlen = ((strType - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        } else {
            strlen = readLength();
        }
        strBegin = offset;

        if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
            if (valueBytes == null) {
                valueBytes = BYTES_UPDATER.getAndSet(cacheItem, null);
                if (valueBytes == null) {
                    valueBytes = new byte[8192];
                }
            }

            int minCapacity = strlen << 1;
            if (minCapacity > valueBytes.length) {
                valueBytes = new byte[minCapacity];
            }

            int utf16_len = IOUtils.decodeUTF8(bytes, offset, strlen, valueBytes);
            if (utf16_len != -1) {
                byte[] value = new byte[utf16_len];
                System.arraycopy(valueBytes, 0, value, 0, utf16_len);
                String str = STRING_CREATOR_JDK11.apply(value, UTF16);
                offset += strlen;

                if ((context.features & Feature.TrimString.mask) != 0) {
                    str = str.trim();
                }

                return str;
            }
        }
        return null;
    }

    private String readStringTypeNotMatch() {
        if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32_NUM_MAX) {
            return Byte.toString(strtype);
        } else if (strtype >= BC_INT32_BYTE_MIN && strtype <= BC_INT32_BYTE_MAX) {
            int intValue = ((strtype - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
            return Integer.toString(intValue);
        } else if (strtype >= BC_INT32_SHORT_MIN && strtype <= BC_INT32_SHORT_MAX) {
            int int3 = getInt3(bytes, offset, strtype);
            offset += 2;
            return Integer.toString(int3);
        } else if (strtype >= BC_INT64_NUM_MIN && strtype <= BC_INT64_NUM_MAX) {
            int intValue = INT64_NUM_LOW_VALUE + (strtype - BC_INT64_NUM_MIN);
            return Integer.toString(intValue);
        } else if (strtype >= BC_INT64_BYTE_MIN && strtype <= BC_INT64_BYTE_MAX) {
            int intValue = ((strtype - BC_INT64_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
            return Integer.toString(intValue);
        } else if (strtype >= BC_INT64_SHORT_MIN && strtype <= BC_INT64_SHORT_MAX) {
            int intValue = ((strtype - BC_INT64_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
            return Integer.toString(intValue);
        }

        switch (strtype) {
            case BC_NULL:
                return null;
            case BC_DOUBLE_NUM_0:
                return "0.0";
            case BC_DOUBLE_NUM_1:
                return "1.0";
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                return Long.toString(BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value));
            }
            case BC_FLOAT_INT:
                return Float.toString(
                        readInt32Value());
            case BC_FLOAT: {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                float floatValue = Float.intBitsToFloat(BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value));
                return Float.toString(floatValue);
            }
            case BC_DOUBLE: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                double doubleValue = Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
                return Double.toString(doubleValue);
            }
            case BC_TIMESTAMP_SECONDS: {
                int seconds = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                long millis = (BIG_ENDIAN ? seconds : Integer.reverseBytes(seconds)) * 1000L;
                Date date = new Date(millis);
                return DateUtils.toString(date);
            }
            case BC_TIMESTAMP_MINUTES: {
                int minutes = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                long millis = (BIG_ENDIAN ? minutes : Integer.reverseBytes(minutes)) * 60000L;
                Date date = new Date(millis);
                return DateUtils.toString(date);
            }
            case BC_TIMESTAMP_MILLIS: {
                long millis = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                Date date = new Date(BIG_ENDIAN ? millis : Long.reverseBytes(millis));
                return DateUtils.toString(date);
            }
            case BC_INT64:
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return Long.toString(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            case BC_BIGINT: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                return new BigInteger(bytes).toString();
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.toString();
            }
            case BC_TYPED_ANY: {
                offset--;
                Object typedAny = readAny();
                return typedAny == null ? null : JSON.toJSONString(typedAny, JSONWriter.Feature.WriteThrowableClassName);
            }
            case BC_DECIMAL_LONG:
            case BC_BIGINT_LONG: {
                return Long.toString(
                        readInt64Value()
                );
            }
            case BC_DOUBLE_LONG: {
                double doubleValue = readInt64Value();
                return Double.toString(doubleValue);
            }
            default:
                break;
        }
        throw new JSONException("readString not support type " + typeName(strtype) + ", offset " + offset + "/" + bytes.length);
    }

    public String[] readStringArray() {
        if (nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = readTypeHashCode();
            if (typeHash != ObjectReaderImplStringArray.HASH_TYPE) {
                throw new JSONException(info("not support type " + getString()));
            }
        }

        int entryCnt = startArray();
        if (entryCnt == -1) {
            return null;
        }
        String[] array = new String[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = readString();
        }
        return array;
    }

    @Override
    public char readCharValue() {
        byte type = bytes[offset];
        if (type == BC_CHAR) {
            offset++;
            return (char) readInt32Value();
        } else if (type == BC_STR_ASCII_FIX_0) {
            offset++;
            return '\0';
        } else if (type > BC_STR_ASCII_FIX_0 && type < BC_STR_ASCII_FIX_MAX) {
            offset++;
            return (char) (bytes[offset++] & 0xff);
        }

        String str = readString();
        if (str == null || str.isEmpty()) {
            return '\0';
        }
        return str.charAt(0);
    }

    @Override
    public int[] readInt32ValueArray() {
        if (nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = readTypeHashCode();
            if (typeHash != ObjectReaderImplInt64ValueArray.HASH_TYPE
                    && typeHash != ObjectReaderImplInt64Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32ValueArray.HASH_TYPE
            ) {
                throw new JSONException(info("not support " + getString()));
            }
        }

        int entryCnt = startArray();
        if (entryCnt == -1) {
            return null;
        }

        int[] array = new int[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = readInt32Value();
        }
        return array;
    }

    @Override
    public long[] readInt64ValueArray() {
        if (nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = readTypeHashCode();
            if (typeHash != ObjectReaderImplInt64ValueArray.HASH_TYPE
                    && typeHash != ObjectReaderImplInt64Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32ValueArray.HASH_TYPE
            ) {
                throw new JSONException(info("not support " + getString()));
            }
        }

        int entryCnt = startArray();
        if (entryCnt == -1) {
            return null;
        }

        long[] array = new long[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = readInt64Value();
        }
        return array;
    }

    @Override
    public long readInt64Value() {
        wasNull = false;

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        byte type = bytes[offset++];

        long int64Value;
        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            int64Value = INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        } else if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
            int64Value = ((type - BC_INT64_BYTE_ZERO) << 8)
                    + (bytes[offset] & 0xFF);
            offset += 1;
        } else if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
            int64Value = ((type - BC_INT64_SHORT_ZERO) << 16)
                    + ((bytes[offset] & 0xFF) << 8)
                    + (bytes[offset + 1] & 0xFF);
            offset += 2;
        } else if (type == BC_INT64_INT) {
            int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            int64Value = BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
            offset += 4;
        } else if (type == BC_INT64) {
            int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            if (!BIG_ENDIAN) {
                int64Value = Long.reverseBytes(int64Value);
            }
            offset += 8;
        } else {
            this.offset = offset;
            return readInt64Value0(bytes, type);
        }
        this.offset = offset;
        return int64Value;
    }

    private long readInt64Value0(byte[] bytes, byte type) {
        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return type;
        }

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            int int3 = getInt3(bytes, offset, type);
            offset += 2;
            return int3;
        }

        switch (type) {
            case BC_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                wasNull = true;
                return 0;
            case BC_FALSE:
            case BC_DOUBLE_NUM_0:
                return 0;
            case BC_TRUE:
            case BC_DOUBLE_NUM_1:
                return 1;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return int16Value;
            case BC_INT32: {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                return BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
            }
            case BC_FLOAT: {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                float floatValue = Float.intBitsToFloat(BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value));
                return (long) floatValue;
            }
            case BC_DOUBLE: {
                offset--;
                return (long) readDoubleValue();
            }
            case BC_FLOAT_INT:
                return (long) ((float) readInt32Value());
            case BC_DOUBLE_LONG:
                return (long) ((double) readInt64Value());
            case BC_TIMESTAMP_MINUTES: {
                long minutes = getInt(bytes, offset);
                offset += 4;
                return minutes * 60 * 1000;
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds = getInt(bytes, offset);
                offset += 4;
                return seconds * 1000;
            }
            case BC_TIMESTAMP_MILLIS:
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value);
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.longValue();
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            default:
                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    if (str.indexOf('.') == -1) {
                        return new BigInteger(str).longValue();
                    } else {
                        return toBigDecimal(str).longValue();
                    }
                }
                break;
        }
        throw new JSONException("readInt64Value not support " + typeName(type) + ", offset " + offset + "/" + bytes.length);
    }

    @Override
    public int readInt32Value() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        final byte type = bytes[offset++];

        int int32Value;
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            int32Value = type;
        } else if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            int32Value = ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset] & 0xFF);
            offset += 1;
        } else if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            int32Value = ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset] & 0xFF) << 8)
                    + (bytes[offset + 1] & 0xFF);
            offset += 2;
        } else if (type == BC_INT32) {
            int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            if (!BIG_ENDIAN) {
                int32Value = Integer.reverseBytes(int32Value);
            }
            offset += 4;
        } else {
            this.offset = offset;
            return readInt32Value0(bytes, type);
        }
        this.offset = offset;
        return int32Value;
    }

    private int readInt32Value0(byte[] bytes, byte type) {
        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            return INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        }

        if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
            return ((type - BC_INT64_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
            return ((type - BC_INT64_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        switch (type) {
            case BC_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("int value not support input null"));
                }

                wasNull = true;
                return 0;
            case BC_FALSE:
            case BC_DOUBLE_NUM_0:
                return 0;
            case BC_TRUE:
            case BC_DOUBLE_NUM_1:
                return 1;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return int16Value;
            case BC_DOUBLE_LONG: {
                return (int) readInt64Value();
            }
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return (int) (BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_FLOAT_INT:
                return (int) (float) readInt32Value();
            case BC_FLOAT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
                return (int) floatValue;
            }
            case BC_DOUBLE: {
                offset--;
                return (int) readDoubleValue();
            }
            case BC_TIMESTAMP_MINUTES:
            case BC_TIMESTAMP_SECONDS:
            case BC_INT64_INT:
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return int32Value;
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.intValue();
            }
            default:
                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    if (str.indexOf('.') == -1) {
                        return new BigInteger(str).intValue();
                    } else {
                        return toBigDecimal(str).intValue();
                    }
                }
                break;
        }
        throw new JSONException("readInt32Value not support " + typeName(type) + ", offset " + offset + "/" + bytes.length);
    }

    @Override
    public boolean isBinary() {
        return bytes[offset] == BC_BINARY;
    }

    @Override
    public byte[] readBinary() {
        byte type = bytes[offset++];
        if (type != BC_BINARY) {
            throw new JSONException("not support input : " + typeName(type));
        }

        int len = readLength();
        byte[] bytes = new byte[len];
        System.arraycopy(this.bytes, offset, bytes, 0, len);
        offset += len;
        return bytes;
    }

    @Override
    public Integer readInt32() {
        if (bytes[offset] == BC_NULL) {
            offset++;
            wasNull = true;
            return null;
        }

        wasNull = false;
        int value = readInt32Value();
        if (wasNull) {
            return null;
        }
        return value;
    }

    @Override
    public Long readInt64() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        byte type = bytes[offset++];

        if (type == BC_NULL) {
            this.offset = offset;
            return null;
        }

        long int64Value;
        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            int64Value = INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        } else if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
            int64Value = ((type - BC_INT64_BYTE_ZERO) << 8)
                    + (bytes[offset] & 0xFF);
            offset += 1;
        } else if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
            int64Value = ((type - BC_INT64_SHORT_ZERO) << 16)
                    + ((bytes[offset] & 0xFF) << 8)
                    + (bytes[offset + 1] & 0xFF);
            offset += 2;
        } else if (type == BC_INT64_INT) {
            int int32Val = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            int64Value = BIG_ENDIAN ? int32Val : Integer.reverseBytes(int32Val);
            offset += 4;
        } else if (type == BC_INT64) {
            int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            if (!BIG_ENDIAN) {
                int64Value = Long.reverseBytes(int64Value);
            }
            offset += 8;
        } else {
            this.offset = offset;
            return readInt64Value0(bytes, type);
        }
        this.offset = offset;
        return int64Value;
    }

    public List<Long> readInt64List() {
        if (nextIfNull()) {
            return null;
        }

        int entryCnt = startArray();
        if (entryCnt == -1) {
            return null;
        }

        ArrayList<Long> list = new ArrayList<>(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            list.add(readInt64());
        }
        return list;
    }

    protected String readFixedAsciiString(int strlen) {
        String str;
        if (strlen == 1) {
            str = TypeUtils.toString((char) (bytes[offset] & 0xff));
        } else if (strlen == 2) {
            str = TypeUtils.toString(
                    (char) (bytes[offset] & 0xff),
                    (char) (bytes[offset + 1] & 0xff)
            );
        } else if (STRING_CREATOR_JDK8 != null) {
            char[] chars = new char[strlen];
            for (int i = 0; i < strlen; ++i) {
                chars[i] = (char) (bytes[offset + i] & 0xff);
            }

            str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
        } else {
            str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
        }
        return str;
    }

    public Float readFloat() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        byte type = bytes[offset];
        if (type == BC_FLOAT) {
            int int32Value = ((bytes[offset + 4] & 0xFF)) +
                    ((bytes[offset + 3] & 0xFF) << 8) +
                    ((bytes[offset + 2] & 0xFF) << 16) +
                    ((bytes[offset + 1]) << 24);
            this.offset = offset + 5;
            return Float.intBitsToFloat(int32Value);
        } else if (type == BC_NULL) {
            this.offset = offset + 1;
            return null;
        }

        return readFloat0();
    }

    @Override
    public float readFloatValue() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        if (bytes[offset] == BC_FLOAT) {
            int int32Val = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 1);
            this.offset = offset + 5;
            return Float.intBitsToFloat(BIG_ENDIAN ? int32Val : Integer.reverseBytes(int32Val));
        }

        return readFloat0();
    }

    private float readFloat0() {
        byte type = bytes[offset++];
        switch (type) {
            case BC_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                wasNull = true;
                return 0;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return int16Value;
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return (float) (BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return int32Value;
            }
            case BC_DOUBLE: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return (float) Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE_LONG: {
                return (float) (double) readInt64Value();
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.intValue();
            }
            case BC_FALSE:
//            case FLOAT_NUM_0:
            case BC_DOUBLE_NUM_0:
                return 0;
            case BC_TRUE:
//            case FLOAT_NUM_1:
            case BC_DOUBLE_NUM_1:
                return 1;
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return type;
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    return ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int int3 = getInt3(bytes, offset, type);
                    this.offset = offset + 2;
                    return int3;
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    return (float) (INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN));
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    return ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    return ((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    if (str.indexOf('.') == -1) {
                        return new BigInteger(str).intValue();
                    } else {
                        return toBigDecimal(str).intValue();
                    }
                }
                break;
        }
        throw new JSONException("TODO : " + typeName(type));
    }

    @Override
    public double readDoubleValue() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        if (bytes[offset] == BC_DOUBLE) {
            long int64Value = ((bytes[offset + 8] & 0xFFL)) +
                    ((bytes[offset + 7] & 0xFFL) << 8) +
                    ((bytes[offset + 6] & 0xFFL) << 16) +
                    ((bytes[offset + 5] & 0xFFL) << 24) +
                    ((bytes[offset + 4] & 0xFFL) << 32) +
                    ((bytes[offset + 3] & 0xFFL) << 40) +
                    ((bytes[offset + 2] & 0xFFL) << 48) +
                    ((long) (bytes[offset + 1]) << 56);
            this.offset = offset + 9;
            return Double.longBitsToDouble(int64Value);
        }

        return readDoubleValue0();
    }

    private double readDoubleValue0() {
        byte type = bytes[offset++];
        switch (type) {
            case BC_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                wasNull = true;
                return 0;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return int16Value;
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return (double) (BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                return BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
            }
            case BC_FLOAT:
                int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 4;
                return Float.intBitsToFloat(BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value));
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE_LONG:
                return readInt64Value();
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return toBigDecimal(str).intValue();
                }
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.intValue();
            }
            case BC_FALSE:
//            case FLOAT_NUM_0:
            case BC_DOUBLE_NUM_0:
                return 0;
//            case FLOAT_NUM_1:
            case BC_DOUBLE_NUM_1:
            case BC_TRUE:
                return 1;
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return type;
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    return ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int int3 = getInt3(bytes, offset, type);
                    this.offset = offset + 2;
                    return int3;
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    return (long) INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    return ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    return ((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    if (str.indexOf('.') == -1) {
                        return new BigInteger(str).intValue();
                    } else {
                        return toBigDecimal(str).intValue();
                    }
                }
                break;
        }
        throw new JSONException("TODO : " + typeName(type));
    }

    @Override
    protected void readNumber0() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public Number readNumber() {
        byte type = bytes[offset++];
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return (int) type;
        }

        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            int int3 = getInt3(bytes, offset, type);
            this.offset = offset + 2;
            return int3;
        }

        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            return (long) INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        }

        if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
            return ((type - BC_INT64_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
            return ((type - BC_INT64_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        switch (type) {
            case BC_NULL:
                return null;
            case BC_FALSE:
            case BC_DOUBLE_NUM_0:
                return 0D;
            case BC_TRUE:
            case BC_DOUBLE_NUM_1:
                return 1D;
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16: {
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return (short) int16Value;
            }
            case BC_INT32: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return int32Value;
            }
            case BC_INT64_INT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return (long) int32Value;
            }
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value);
            }
            case BC_BIGINT: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                return new BigInteger(bytes);
            }
            case BC_BIGINT_LONG: {
                return BigInteger.valueOf(
                        readInt64Value()
                );
            }
            case BC_FLOAT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE:
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            case BC_DOUBLE_LONG:
                return (double) readInt64Value();
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                if (scale == 0) {
                    return new BigDecimal(unscaledValue);
                } else {
                    return new BigDecimal(unscaledValue, scale);
                }
            }
            case BC_DECIMAL_LONG: {
                return BigDecimal.valueOf(
                        readInt64Value()
                );
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                return toBigDecimal(str);
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                return toBigDecimal(str);
            }
            case BC_TYPED_ANY: {
                String typeName = readString();
                throw new JSONException("not support input type : " + typeName);
            }
            default:
                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    return toBigDecimal(str);
                }
                break;
        }
        throw new JSONException("not support type :" + typeName(type));
    }

    @Override
    public BigDecimal readBigDecimal() {
        byte type = bytes[offset++];
        BigDecimal decimal;
        if (type == BC_DECIMAL) {
            int scale = readInt32Value();
            if (bytes[offset] == BC_BIGINT_LONG) {
                offset++;
                long unscaledLongValue = readInt64Value();
                decimal = BigDecimal.valueOf(unscaledLongValue, scale);
            } else if (bytes[offset] == BC_INT32) {
                decimal = BigDecimal.valueOf(getInt(bytes, offset + 1), scale);
                offset += 5;
            } else if (bytes[offset] == BC_INT64) {
                long unscaledValue = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 1);
                decimal = BigDecimal.valueOf(
                        BIG_ENDIAN
                                ? unscaledValue
                                : Long.reverseBytes(unscaledValue), scale
                );
                offset += 9;
            } else {
                BigInteger unscaledValue = readBigInteger();
                decimal = scale == 0
                        ? new BigDecimal(unscaledValue)
                        : new BigDecimal(unscaledValue, scale);
            }
        } else if (type == BC_DECIMAL_LONG) {
            decimal = BigDecimal.valueOf(
                    readInt64Value()
            );
        } else {
            decimal = readDecimal0(type);
        }
        return decimal;
    }

    private BigDecimal readDecimal0(byte type) {
        switch (type) {
            case BC_NULL:
                return null;
            case BC_DOUBLE_NUM_0:
            case BC_FALSE:
                return BigDecimal.ZERO;
            case BC_DOUBLE_NUM_1:
            case BC_TRUE:
                return BigDecimal.ONE;
            case BC_INT8:
                return BigDecimal.valueOf(bytes[offset++]);
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return BigDecimal.valueOf(int16Value);
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return BigDecimal.valueOf(int32Value);
            }
            case BC_FLOAT_INT: {
                float floatValue = (float) readInt32Value();
                return BigDecimal.valueOf((long) floatValue);
            }
            case BC_FLOAT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
                return BigDecimal.valueOf((long) floatValue);
            }
            case BC_DOUBLE: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                double doubleValue = Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
                return BigDecimal.valueOf(
                        (long) doubleValue);
            }
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return BigDecimal.valueOf(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_BIGINT: {
                BigInteger bigInt = readBigInteger();
                return new BigDecimal(bigInt);
            }
            case BC_DOUBLE_LONG: {
                double doubleValue = readInt64Value();
                return BigDecimal.valueOf((long) doubleValue);
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                return toBigDecimal(str);
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                return toBigDecimal(str);
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                return toBigDecimal(str);
            }
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return BigDecimal.valueOf(type);
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    int intValue = ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigDecimal.valueOf(intValue);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int int3 = getInt3(bytes, offset, type);
                    this.offset = offset + 2;
                    return BigDecimal.valueOf(int3);
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    int intValue = INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
                    return BigDecimal.valueOf(intValue);
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    int intValue = ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigDecimal.valueOf(intValue);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    int intValue = ((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigDecimal.valueOf(intValue);
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    return toBigDecimal(str);
                }
                break;
        }
        throw new JSONException("not support type :" + typeName(type));
    }

    @Override
    public BigInteger readBigInteger() {
        byte type = bytes[offset++];
        BigInteger bigInt;
        if (type == BC_BIGINT_LONG) {
            bigInt = BigInteger.valueOf(
                    readInt64Value()
            );
        } else if (type == BC_BIGINT) {
            int len = readInt32Value();
            byte[] bytes = new byte[len];
            System.arraycopy(this.bytes, offset, bytes, 0, len);
            offset += len;
            bigInt = new BigInteger(bytes);
        } else {
            bigInt = readBigInteger0(type);
        }
        return bigInt;
    }

    private BigInteger readBigInteger0(byte type) {
        switch (type) {
            case BC_NULL:
                return null;
            case BC_DOUBLE_NUM_0:
            case BC_FALSE:
                return BigInteger.ZERO;
            case BC_DOUBLE_NUM_1:
            case BC_TRUE:
                return BigInteger.ONE;
            case BC_INT8:
                return BigInteger.valueOf(bytes[offset++]);
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return BigInteger.valueOf(int16Value);
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                return BigInteger.valueOf(int32Value);
            }
            case BC_FLOAT_INT: {
                float floatValue = (float) readInt32Value();
                return BigInteger.valueOf(
                        (long) floatValue);
            }
            case BC_FLOAT: {
                int int32Value = getInt(bytes, offset);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
                return BigInteger.valueOf(
                        (long) floatValue);
            }
            case BC_DOUBLE: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                double doubleValue = Double.longBitsToDouble(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
                return BigInteger.valueOf(
                        (long) doubleValue);
            }
            case BC_INT64: {
                long int64Value = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return BigInteger.valueOf(BIG_ENDIAN ? int64Value : Long.reverseBytes(int64Value));
            }
            case BC_BINARY: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                return new BigInteger(bytes);
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();
                BigInteger unscaledValue = readBigInteger();
                BigDecimal decimal;
                if (scale == 0) {
                    decimal = new BigDecimal(unscaledValue);
                } else {
                    decimal = new BigDecimal(unscaledValue, scale);
                }
                return decimal.toBigInteger();
            }
            case BC_DOUBLE_LONG: {
                double doubleValue = readInt64Value();
                return BigInteger.valueOf((long) doubleValue);
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return toBigDecimal(str).toBigInteger();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return toBigDecimal(str).toBigInteger();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return toBigDecimal(str).toBigInteger();
                }
            }
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return BigInteger.valueOf(type);
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    int intValue = ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigInteger.valueOf(intValue);
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int int3 = getInt3(bytes, offset, type);
                    this.offset = offset + 2;
                    return BigInteger.valueOf(int3);
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    int intValue = INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
                    return BigInteger.valueOf(intValue);
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    int intValue = ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigInteger.valueOf(intValue);
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    int intValue = ((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigInteger.valueOf(intValue);
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
                    int strlen = type - BC_STR_ASCII_FIX_MIN;
                    String str = readFixedAsciiString(strlen);
                    offset += strlen;
                    return new BigInteger(str);
                }
                break;
        }
        throw new JSONException("not support type :" + typeName(type));
    }

    @Override
    public LocalDate readLocalDate() {
        int type = bytes[offset];
        if (type == BC_LOCAL_DATE) {
            offset++;
            int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
            int month = bytes[offset++];
            int dayOfMonth = bytes[offset++];
            return LocalDate.of(year, month, dayOfMonth);
        }

        if (type == BC_NULL) {
            offset++;
            return null;
        }

        return readLocalDate0(type);
    }

    private LocalDate readLocalDate0(int type) {
        if (type == BC_LOCAL_DATETIME) {
            return readLocalDateTime().toLocalDate();
        }

        if (type == BC_TIMESTAMP_WITH_TIMEZONE) {
            return readZonedDateTime().toLocalDate();
        }

        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
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
                default:
                    if (bytes[offset + len] == 'Z') {
                        ZonedDateTime zdt = readZonedDateTime();
                        return zdt.toLocalDate();
                    }
                    break;
            }
            throw new JSONException("TODO : " + len + ", " + readString());
        }

        if (type == BC_STR_UTF8 || type == BC_STR_ASCII) {
            strtype = (byte) type;
            offset++;
            strlen = readLength();
            switch (strlen) {
                case 8:
                    return readLocalDate8();
                case 9:
                    return readLocalDate9();
                case 10: {
                    return readLocalDate10();
                }
                case 11:
                    return readLocalDate11();
                default:
                    break;
            }
        }

        throw new JSONException("not support type : " + typeName((byte) type));
    }

    @Override
    public LocalDateTime readLocalDateTime() {
        int type = bytes[offset];
        if (type == BC_LOCAL_DATETIME) {
            offset++;
            int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
            int month = bytes[offset++];
            int dayOfMonth = bytes[offset++];
            int hour = bytes[offset++];
            int minute = bytes[offset++];
            int second = bytes[offset++];

            int nano = readInt32Value();

            return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
        }

        if (type == BC_NULL) {
            offset++;
            return null;
        }

        return readLocalDateTime0(type);
    }

    private LocalDateTime readLocalDateTime0(int type) {
        if (type == BC_TIMESTAMP_WITH_TIMEZONE) {
            return readZonedDateTime().toLocalDateTime();
        }

        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
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
                case 17:
                    return readLocalDateTime17();
                case 18:
                    return readLocalDateTime18();
                case 19:
                    return readLocalDateTime19();
                case 20:
                    return readLocalDateTime20();
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
                        return zdt.toLocalDateTime();
                    }
                    break;
                default:
                    break;
            }

            throw new JSONException("TODO : " + len + ", " + readString());
        }

        throw new JSONException("not support type : " + typeName((byte) type));
    }

    @Override
    protected LocalDateTime readLocalDateTime12() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 12
                || (ldt = DateUtils.parseLocalDateTime12(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 13;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime14() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 14
                || (ldt = DateUtils.parseLocalDateTime14(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 15;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime16() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 16
                || (ldt = DateUtils.parseLocalDateTime16(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 17;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime17() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 17
                || (ldt = DateUtils.parseLocalDateTime17(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 18;
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime10() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 10
                || (time = DateUtils.parseLocalTime10(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 11;
        return time;
    }

    @Override
    protected LocalTime readLocalTime11() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 11
                || (time = DateUtils.parseLocalTime11(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 12;
        return time;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        type = bytes[offset];
        if (type < BC_STR_ASCII_FIX_MIN || type > BC_STR_ASCII_FIX_MAX) {
            throw new JSONException("date only support string input");
        }

        ZonedDateTime ldt;
        if (len < 19
                || (ldt = DateUtils.parseZonedDateTime(bytes, offset + 1, len, context.zoneId)) == null
        ) {
            throw new JSONException("illegal LocalDateTime string : " + readString());
        }

        offset += (len + 1);
        return ldt;
    }

    @Override
    public void skipLineComment() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public LocalTime readLocalTime() {
        int type = bytes[offset];
        if (type == BC_LOCAL_TIME) {
            offset++;
            int hour = bytes[offset++];
            int minute = bytes[offset++];
            int second = bytes[offset++];
            int nano = readInt32Value();
            return LocalTime.of(hour, minute, second, nano);
        }

        if (type == BC_NULL) {
            offset++;
            return null;
        }

        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
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
                default:
                    break;
            }
            throw new JSONException("not support len : " + len);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public Instant readInstant() {
        int type = bytes[offset++];
        switch (type) {
            case BC_TIMESTAMP: {
                return Instant.ofEpochSecond(
                        readInt64Value(),
                        readInt32Value()
                );
            }
            case BC_TIMESTAMP_MINUTES: {
                long minutes = getInt(bytes, offset);
                offset += 4;
                return Instant.ofEpochSecond(minutes * 60, 0);
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds = getInt(bytes, offset);
                offset += 4;
                return Instant.ofEpochSecond(seconds, 0);
            }
            case BC_INT64:
            case BC_TIMESTAMP_MILLIS: {
                long millis = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                return Instant.ofEpochMilli(BIG_ENDIAN ? millis : Long.reverseBytes(millis));
            }
            default:
                break;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public OffsetTime readOffsetTime() {
        ZonedDateTime zdt = readZonedDateTime();
        return zdt == null ? null : zdt.toOffsetDateTime().toOffsetTime();
    }

    @Override
    public OffsetDateTime readOffsetDateTime() {
        ZonedDateTime zdt = readZonedDateTime();
        return zdt == null ? null : zdt.toOffsetDateTime();
    }

    @Override
    public ZonedDateTime readZonedDateTime() {
        int type = bytes[offset++];
        if (type == BC_TIMESTAMP_WITH_TIMEZONE) {
            int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
            int month = bytes[offset++];
            int dayOfMonth = bytes[offset++];
            int hour = bytes[offset++];
            int minute = bytes[offset++];
            int second = bytes[offset++];
            int nano = readInt32Value();
            LocalDateTime ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);

            ZoneId zoneId;
            long zoneIdHash = readValueHashCode();
            final long SHANGHAI_ZONE_ID_HASH = -4800907791268808639L; // Fnv.hashCode64("Asia/Shanghai");
            if (zoneIdHash == SHANGHAI_ZONE_ID_HASH) {
                zoneId = SHANGHAI_ZONE_ID;
            } else {
                String zoneIdStr = getString();
                ZoneId contextZoneId = context.getZoneId();
                if (contextZoneId.getId().equals(zoneIdStr)) {
                    zoneId = contextZoneId;
                } else {
                    zoneId = DateUtils.getZoneId(zoneIdStr, SHANGHAI_ZONE_ID);
                }
            }
            return ZonedDateTime.ofLocal(ldt, zoneId, null);
        }

        return readZonedDateTime0(type);
    }

    private ZonedDateTime readZonedDateTime0(int type) {
        switch (type) {
            case BC_NULL:
                return null;
            case BC_TIMESTAMP: {
                long second = readInt64Value();
                int nano = readInt32Value();
                Instant instant = Instant.ofEpochSecond(second, nano);
                return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
            }
            case BC_TIMESTAMP_MINUTES: {
                long minutes = getInt(bytes, offset);
                offset += 4;
                Instant instant = Instant.ofEpochSecond(minutes * 60);
                return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds = getInt(bytes, offset);
                offset += 4;
                Instant instant = Instant.ofEpochSecond(seconds);
                return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
            }
            case BC_LOCAL_DATE: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                byte month = bytes[offset++];
                byte dayOfMonth = bytes[offset++];
                LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
                return ZonedDateTime.of(localDate, LocalTime.MIN, DEFAULT_ZONE_ID);
            }
            case BC_LOCAL_DATETIME: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                byte month = bytes[offset++];
                byte dayOfMonth = bytes[offset++];
                byte hour = bytes[offset++];
                byte minute = bytes[offset++];
                byte second = bytes[offset++];
                int nano = readInt32Value();
                LocalDateTime ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
                return ZonedDateTime.of(ldt, DEFAULT_ZONE_ID);
            }
            case BC_INT64:
            case BC_TIMESTAMP_MILLIS: {
                long millis = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                offset += 8;
                Instant instant = Instant.ofEpochMilli(BIG_ENDIAN ? millis : Long.reverseBytes(millis));
                return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
            }
            default:
                if (type >= BC_STR_ASCII_FIX_0 && type <= BC_STR_ASCII_FIX_MAX) {
                    offset--;
                    return readZonedDateTimeX(type - BC_STR_ASCII_FIX_MIN);
                }
                break;
        }
        throw new JSONException("type not support : " + JSONB.typeName((byte) type));
    }

    @Override
    public UUID readUUID() {
        byte type = bytes[offset++];
        switch (type) {
            case BC_NULL:
                return null;
            case BC_BINARY:
                int len = readLength();
                if (len != 16) {
                    throw new JSONException("uuid not support " + len);
                }
                long msb = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
                long lsb = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 8);
                offset += 16;
                return new UUID(
                        BIG_ENDIAN ? msb : Long.reverseBytes(msb),
                        BIG_ENDIAN ? lsb : Long.reverseBytes(lsb)
                );
            case BC_STR_ASCII_FIX_32: {
                long hi = 0;
                for (int i = 0; i < 16; i++) {
                    hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }
                long lo = 0;
                for (int i = 16; i < 32; i++) {
                    lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }

                offset += 32;
                return new UUID(hi, lo);
            }
            case BC_STR_ASCII_FIX_36: {
                byte ch1 = bytes[offset + 8];
                byte ch2 = bytes[offset + 13];
                byte ch3 = bytes[offset + 18];
                byte ch4 = bytes[offset + 23];
                if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                    long hi = 0;
                    for (int i = 0; i < 8; i++) {
                        hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }
                    for (int i = 9; i < 13; i++) {
                        hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }
                    for (int i = 14; i < 18; i++) {
                        hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }

                    long lo = 0;
                    for (int i = 19; i < 23; i++) {
                        lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }
                    for (int i = 24; i < 36; i++) {
                        lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }

                    offset += 36;
                    return new UUID(hi, lo);
                }
                throw new JSONException("Invalid UUID string:  " + new String(bytes, offset, 36, StandardCharsets.ISO_8859_1));
            }
            case BC_STR_ASCII:
            case BC_STR_UTF8: {
                int strlen = readLength();
                if (strlen == 32) {
                    long hi = 0;
                    for (int i = 0; i < 16; i++) {
                        hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }
                    long lo = 0;
                    for (int i = 16; i < 32; i++) {
                        lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                    }

                    offset += 32;
                    return new UUID(hi, lo);
                } else if (strlen == 36) {
                    byte ch1 = bytes[offset + 8];
                    byte ch2 = bytes[offset + 13];
                    byte ch3 = bytes[offset + 18];
                    byte ch4 = bytes[offset + 23];
                    if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                        long hi = 0;
                        for (int i = 0; i < 8; i++) {
                            hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                        }
                        for (int i = 9; i < 13; i++) {
                            hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                        }
                        for (int i = 14; i < 18; i++) {
                            hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                        }

                        long lo = 0;
                        for (int i = 19; i < 23; i++) {
                            lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                        }
                        for (int i = 24; i < 36; i++) {
                            lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                        }

                        offset += 36;
                        return new UUID(hi, lo);
                    }
                }
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                throw new JSONException("Invalid UUID string:  " + str);
            }
            default:
                throw new JSONException("type not support : " + JSONB.typeName(type));
        }
    }

    @Override
    public Boolean readBool() {
        byte type = this.bytes[offset++];
        if (type == BC_NULL) {
            return null;
        } else if (type == BC_TRUE) {
            return true;
        } else if (type == BC_FALSE) {
            return false;
        }

        return readBoolValue0(type);
    }

    @Override
    public boolean readBoolValue() {
        wasNull = false;
        byte type = this.bytes[offset++];
        if (type == BC_TRUE) {
            return true;
        } else if (type == BC_FALSE) {
            return false;
        }

        return readBoolValue0(type);
    }

    private boolean readBoolValue0(byte type) {
        final byte[] bytes = this.bytes;
        switch (type) {
            case BC_INT32_NUM_1:
                return true;
            case BC_INT32_NUM_0:
                return false;
            case BC_NULL:
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }
                wasNull = true;
                return false;
            case BC_STR_ASCII_FIX_1:
                if (bytes[offset] == '1' || bytes[offset] == 'Y') {
                    offset++;
                    return true;
                }
                if (bytes[offset] == '0' || bytes[offset] == 'N') {
                    offset++;
                    return false;
                }
            case BC_STR_ASCII_FIX_4:
                if (bytes[offset] == 't'
                        && bytes[offset + 1] == 'r'
                        && bytes[offset + 2] == 'u'
                        && bytes[offset + 3] == 'e'
                ) {
                    offset += 4;
                    return true;
                }
                if (bytes[offset] == 'T'
                        && bytes[offset + 1] == 'R'
                        && bytes[offset + 2] == 'U'
                        && bytes[offset + 3] == 'E'
                ) {
                    offset += 4;
                    return true;
                }
            case BC_STR_ASCII_FIX_5:
                if (bytes[offset] == 'f'
                        && bytes[offset + 1] == 'a'
                        && bytes[offset + 2] == 'l'
                        && bytes[offset + 3] == 's'
                        && bytes[offset + 4] == 'e'
                ) {
                    offset += 5;
                    return false;
                }
                if (bytes[offset] == 'F'
                        && bytes[offset + 1] == 'A'
                        && bytes[offset + 2] == 'L'
                        && bytes[offset + 3] == 'S'
                        && bytes[offset + 4] == 'E'
                ) {
                    offset += 5;
                    return false;
                }
            case BC_STR_UTF8:
            case BC_STR_ASCII: {
                strlen = readLength();
                if (strlen == 1) {
                    if (bytes[offset] == 'Y') {
                        offset++;
                        return true;
                    }
                    if (bytes[offset] == 'N') {
                        offset++;
                        return true;
                    }
                } else if (strlen == 4
                        && bytes[offset] == 't'
                        && bytes[offset + 1] == 'r'
                        && bytes[offset + 2] == 'u'
                        && bytes[offset + 3] == 'e'
                ) {
                    offset += 4;
                    return true;
                } else if (strlen == 5) {
                    if (bytes[offset] == 'f'
                            && bytes[offset + 1] == 'a'
                            && bytes[offset + 2] == 'l'
                            && bytes[offset + 3] == 's'
                            && bytes[offset + 4] == 'e') {
                        offset += 5;
                        return false;
                    } else if (bytes[offset] == 'F'
                            && bytes[offset + 1] == 'A'
                            && bytes[offset + 2] == 'L'
                            && bytes[offset + 3] == 'S'
                            && bytes[offset + 4] == 'E') {
                        offset += 5;
                        return false;
                    }
                }
                String str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
                offset += strlen;
                throw new JSONException("not support input " + str);
            }
            case BC_STR_UTF16:
            case BC_STR_UTF16BE:
            case BC_STR_UTF16LE: {
                strlen = readLength();
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);

                Charset charset = type == BC_STR_UTF16BE
                        ? StandardCharsets.UTF_16BE
                        : type == BC_STR_UTF16LE ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16;

                String str = new String(chars, charset);
                offset += strlen;

                switch (str) {
                    case "0":
                    case "N":
                    case "false":
                    case "FALSE":
                        return false;
                    case "1":
                    case "Y":
                    case "true":
                    case "TRUE":
                        return true;
                    default:
                        throw new JSONException("not support input " + str);
                }
            }
            default:
                throw new JSONException("not support type : " + typeName(type));
        }
    }

    @Override
    public boolean nextIfMatch(byte type) {
        if (bytes[offset] == type) {
            offset++;
            return true;
        }
        return false;
    }

    public boolean nextIfMatchTypedAny() {
        if (bytes[offset] == BC_TYPED_ANY) {
            offset++;
            return true;
        }
        return false;
    }

    @Override
    protected int getStringLength() {
        type = bytes[offset];
        if (type >= BC_STR_ASCII_FIX_MIN && type < BC_STR_ASCII_FIX_MAX) {
            return type - BC_STR_ASCII_FIX_MIN;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate readLocalDate8() {
        LocalDate ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 8
                || (ldt = DateUtils.parseLocalDate8(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }

        offset += 9;
        return ldt;
    }

    @Override
    public LocalDate readLocalDate9() {
        LocalDate ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 9
                || (ldt = DateUtils.parseLocalDate9(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }

        offset += 10;
        return ldt;
    }

    @Override
    protected LocalDate readLocalDate10() {
        LocalDate ldt;
        if ((strtype == BC_STR_ASCII || strtype == BC_STR_UTF8) && strlen == 10) {
            ldt = DateUtils.parseLocalDate10(bytes, offset);
        } else if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 10
                || (ldt = DateUtils.parseLocalDate10(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }

        offset += 11;
        return ldt;
    }

    @Override
    protected LocalDate readLocalDate11() {
        LocalDate ldt;
        if ((strtype == BC_STR_ASCII || strtype == BC_STR_UTF8) && strlen == 11) {
            ldt = DateUtils.parseLocalDate11(bytes, offset);
        } else if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 11
                || (ldt = DateUtils.parseLocalDate11(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }

        offset += 12;
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime5() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 5
                || (time = DateUtils.parseLocalTime5(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 6;
        return time;
    }

    @Override
    protected LocalTime readLocalTime8() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 8
                || (time = DateUtils.parseLocalTime8(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 9;
        return time;
    }

    @Override
    protected LocalTime readLocalTime9() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 8
                || (time = DateUtils.parseLocalTime8(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 10;
        return time;
    }

    @Override
    protected LocalTime readLocalTime12() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 12
                || (time = DateUtils.parseLocalTime12(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 13;
        return time;
    }

    @Override
    protected LocalTime readLocalTime18() {
        LocalTime time;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 18
                || (time = DateUtils.parseLocalTime18(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 19;
        return time;
    }

    @Override
    protected LocalDateTime readLocalDateTime18() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 18
                || (ldt = DateUtils.parseLocalDateTime18(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 19;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime20() {
        LocalDateTime ldt;
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 20
                || (ldt = DateUtils.parseLocalDateTime20(bytes, offset + 1)) == null
        ) {
            throw new JSONException("date only support string input");
        }
        offset += 21;
        return ldt;
    }

    @Override
    public long readMillis19() {
        if (bytes[offset] != BC_STR_ASCII_FIX_MIN + 19) {
            throw new JSONException("date only support string input");
        }

        long millis = DateUtils.parseMillis19(bytes, offset + 1, context.zoneId);
        offset += 20;
        return millis;
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 19) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime19(bytes, offset + 1);
        if (ldt == null) {
            throw new JSONException("date only support string input");
        }

        offset += 20;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTimeX(int len) {
        type = bytes[offset];
        if (type < BC_STR_ASCII_FIX_MIN || type > BC_STR_ASCII_FIX_MAX) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt;
        if (len < 21
                || len > 29
                || (ldt = DateUtils.parseLocalDateTimeX(bytes, offset + 1, len)) == null
        ) {
            throw new JSONException("illegal LocalDateTime string : " + readString());
        }

        offset += (len + 1);
        return ldt;
    }

    @Override
    public String readPattern() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public long readFieldNameHashCodeUnquote() {
        return readFieldNameHashCode();
    }

    @Override
    public boolean nextIfSet() {
        return false;
    }

    @Override
    public boolean nextIfInfinity() {
        return false;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public SavePoint mark() {
        return new SavePoint(this.offset, this.type);
    }

    @Override
    public void reset(SavePoint savePoint) {
        this.offset = savePoint.offset;
        this.type = (byte) savePoint.current;
    }

    @Override
    public void close() {
        byte[] valueBytes = this.valueBytes;
        if (valueBytes != null && valueBytes.length < CACHE_THRESHOLD) {
            BYTES_UPDATER.lazySet(cacheItem, valueBytes);
        }
    }

    public boolean isEnd() {
        return offset >= end;
    }

    static int getInt(byte[] bytes, int offset) {
        int int32Value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        return BIG_ENDIAN ? int32Value : Integer.reverseBytes(int32Value);
    }
}

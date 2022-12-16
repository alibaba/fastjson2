package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.*;

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
import static com.alibaba.fastjson2.util.DateUtils.localDateTime;
import static com.alibaba.fastjson2.util.IOUtils.SHANGHAI_ZONE_ID;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.UUIDUtils.parse4Nibbles;

class JSONReaderJSONB
        extends JSONReader {
    static Charset GB18030;

    protected final byte[] bytes;
    protected final int length;
    protected final int end;

    protected byte type;
    protected int strlen;
    protected byte strtype;
    protected int strBegin;

    protected byte[] valueBytes;
    protected final int cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);

    protected final SymbolTable symbolTable;

    protected long symbol0Hash;
    protected int symbol0Begin;
    protected int symbol0Length;
    protected byte symbol0StrType;

    protected long[] symbols;

    JSONReaderJSONB(Context ctx, byte[] bytes, int off, int length) {
        super(ctx);
        this.bytes = bytes;
        this.offset = off;
        this.length = length;
        this.end = off + length;
        this.symbolTable = ctx.symbolTable;
    }

    @Override
    public boolean isJSONB() {
        return true;
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
            charset = StandardCharsets.US_ASCII;
        } else if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII_FIX_MAX) {
            if (STRING_CREATOR_JDK8 != null) {
                char[] chars = new char[strlen];
                for (int i = 0; i < strlen; ++i) {
                    chars[i] = (char) bytes[strBegin + i];
                }
                return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
            } else if (STRING_CREATOR_JDK11 != null) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, strBegin, chars, 0, strlen);
                return STRING_CREATOR_JDK11.apply(chars, LATIN1);
            }
            charset = StandardCharsets.US_ASCII;
        } else if (strtype == BC_STR_UTF8) {
            charset = StandardCharsets.UTF_8;
        } else if (strtype == BC_STR_UTF16) {
            charset = StandardCharsets.UTF_16;
        } else if (strtype == BC_STR_UTF16LE) {
            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            charset = StandardCharsets.UTF_16BE;
        } else if (strtype == BC_SYMBOL) {
            int symbol = strlen;
            if (symbol < 0) {
                return symbolTable.getName(-symbol);
            }
            int index = symbol * 2;
//            return symbols[index];
            throw new JSONException("TODO : " + JSONB.typeName(strtype));
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
            return ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type == BC_INT32) {
            int len = (bytes[offset++] << 24)
                    + ((bytes[offset++] & 0xFF) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
            if (len > 1024 * 1024 * 256) {
                throw new JSONException("input length overflow");
            }
            return len;
        }

        throw new JSONException("not support length type : " + typeName(type));
    }

    @Override
    public final boolean isArray() {
        if (offset >= bytes.length) {
            return false;
        }

        byte type = bytes[offset];
        return type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY;
    }

    @Override
    public final boolean isObject() {
        return offset < end && bytes[offset] == BC_OBJECT;
    }

    @Override
    public boolean isNumber() {
        byte type = bytes[offset];
        return type >= BC_DOUBLE_LONG && type <= BC_INT32;
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
    public boolean nextIfObjectStart() {
        if (bytes[offset] != BC_OBJECT) {
            return false;
        }
        offset++;
        return true;
    }

    @Override
    public final boolean nextIfObjectEnd() {
        if (bytes[offset] != BC_OBJECT_END) {
            return false;
        }
        offset++;
        return true;
    }

    @Override
    public final boolean nextIfNullOrEmptyString() {
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

                String name = readFieldName();

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
                    offset++;
                    long int64Value =
                            ((bytes[offset + 7] & 0xFFL)) +
                                    ((bytes[offset + 6] & 0xFFL) << 8) +
                                    ((bytes[offset + 5] & 0xFFL) << 16) +
                                    ((bytes[offset + 4] & 0xFFL) << 24) +
                                    ((bytes[offset + 3] & 0xFFL) << 32) +
                                    ((bytes[offset + 2] & 0xFFL) << 40) +
                                    ((bytes[offset + 1] & 0xFFL) << 48) +
                                    ((long) (bytes[offset]) << 56);
                    offset += 8;
                    value = int64Value;
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
                    offset++;
                    value = ((valueType - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                } else if (valueType >= BC_INT32_SHORT_MIN && valueType <= BC_INT32_SHORT_MAX) {
                    offset++;
                    int int32Value = ((valueType - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    value = new Integer(int32Value);
                } else if (valueType == BC_INT32) {
                    offset++;
                    int int32Value =
                            ((bytes[offset + 3] & 0xFF)) +
                                    ((bytes[offset + 2] & 0xFF) << 8) +
                                    ((bytes[offset + 1] & 0xFF) << 16) +
                                    ((bytes[offset]) << 24);
                    offset += 4;
                    value = new Integer(int32Value);
                } else {
                    value = readAny();
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
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return new Integer(int32Value);
            }
            case BC_INT64_INT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return new Long(int32Value);
            }
            case BC_INT64: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return Long.valueOf(int64Value);
            }
            case BC_BIGINT: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                return new BigInteger(bytes);
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return Double.longBitsToDouble(int64Value);
            }
            case BC_DOUBLE_LONG: {
                return (double) readInt64Value();
            }
            case BC_STR_UTF8: {
                int strlen = readLength();

                if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                    if (valueBytes == null) {
                        valueBytes = JSONFactory.allocateByteArray(cachedIndex);
                    }

                    int minCapacity = strlen << 1;
                    if (valueBytes == null) {
                        valueBytes = new byte[minCapacity];
                    } else {
                        if (minCapacity > valueBytes.length) {
                            valueBytes = new byte[minCapacity];
                        }
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
                    str = STRING_CREATOR_JDK11.apply(chars, UTF16);
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
                    str = STRING_CREATOR_JDK11.apply(chars, UTF16);
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
                long minutes =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return new Date(minutes * 60L * 1000L);
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return new Date(seconds * 1000);
            }
            case BC_TIMESTAMP_MILLIS: {
                long millis =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return new Date(millis);
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
                    if (supportAutoType && i == 0 && type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_GB18030) {
                        long hash = readFieldNameHashCode();

                        if (hash == ObjectReader.HASH_TYPE && supportAutoType) {
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
                        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_SYMBOL) {
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
                    return ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
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
                            chars[i] = (char) bytes[offset + i];
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
                    String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                    offset += strlen;

                    if ((context.features & Feature.TrimString.mask) != 0) {
                        str = str.trim();
                    }
                    return str;
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
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                value = int64Value;
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
                offset++;
                value = ((valueType - BC_INT32_BYTE_ZERO) << 8)
                        + (bytes[offset++] & 0xFF);
            } else if (valueType >= BC_INT32_SHORT_MIN && valueType <= BC_INT32_SHORT_MAX) {
                offset++;
                int int32Value = ((valueType - BC_INT32_SHORT_ZERO) << 16)
                        + ((bytes[offset++] & 0xFF) << 8)
                        + (bytes[offset++] & 0xFF);
                value = new Integer(int32Value);
            } else if (valueType == BC_INT32) {
                offset++;
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                value = new Integer(int32Value);
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
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public final boolean isReference() {
        return offset < bytes.length && bytes[offset] == BC_REFERENCE;
    }

    @Override
    public final String readReference() {
        if (bytes[offset] != BC_REFERENCE) {
            return null;
        }
        offset++;
        if (isString()) {
            return readString();
        }

        throw new JSONException("reference not support input " + error(type));
    }

    @Override
    public final ObjectReader checkAutoType(Class expectClass, long expectClassHash, long features) {
        ObjectReader autoTypeObjectReader = null;
        type = bytes[offset];
        if (type == BC_TYPED_ANY) {
            offset++;

            long typeHash = readTypeHashCode();

            if (expectClassHash == typeHash) {
                ObjectReader objectReader = context.getObjectReader(expectClass);
                Class objectClass = objectReader.getObjectClass();
                if (objectClass != null && objectClass == expectClass) {
                    context.getProvider().registerIfAbsent(typeHash, objectReader);
                    return objectReader;
                }
            }

            if (context.autoTypeBeforeHandler != null) {
                Class<?> objectClass = context.autoTypeBeforeHandler.apply(typeHash, expectClass, features);
                if (objectClass == null) {
                    String typeName = getString();
                    objectClass = context.autoTypeBeforeHandler.apply(typeName, expectClass, features);
                }
                if (objectClass != null) {
                    ObjectReader objectReader = context.getObjectReader(objectClass);
                    if (objectReader != null) {
                        return objectReader;
                    }
                }
            }

            boolean isSupportAutoType = ((context.features | features) & Feature.SupportAutoType.mask) != 0;
            if (!isSupportAutoType) {
                String typeName = getString();
                throw new JSONException("autoType not support input " + typeName);
            }

            ObjectReaderProvider provider = context.provider;
            autoTypeObjectReader = provider.getObjectReader(typeHash);

            if (autoTypeObjectReader != null) {
                Class objectClass = autoTypeObjectReader.getObjectClass();
                if (objectClass != null) {
                    ClassLoader objectClassLoader = objectClass.getClassLoader();
                    if (objectClassLoader != null) {
                        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                        if (objectClassLoader != contextClassLoader) {
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
                        }
                    }
                }
            }

            if (autoTypeObjectReader == null) {
                String typeName = getString();
                autoTypeObjectReader = provider.getObjectReader(typeName, expectClass, context.features | features);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + getString());
            }

            type = bytes[offset];
        }
        return autoTypeObjectReader;
    }

    @Override
    public int startArray() {
        type = bytes[offset++];

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
    public final void next() {
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
                if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32_NUM_MAX) {
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
            StringBuffer message = new StringBuffer()
                    .append("fieldName not support input type ")
                    .append(typeName(strtype));
            if (strtype == BC_REFERENCE) {
                message.append(" ")
                        .append(readString());
            }

            message.append(", offset ")
                    .append(offset);
            throw new JSONException(message.toString());
        }

        long hashCode;
        if (strlen < 0) {
            hashCode = symbolTable.getHashCode(-strlen);
        } else {
            long nameValue = 0;
            if (MIXED_HASH_ALGORITHM && strlen <= 8) {
                switch (strlen) {
                    case 1:
                        nameValue = bytes[offset];
                        break;
                    case 2:
                        nameValue = (bytes[offset + 1] << 8)
                                + (bytes[offset] & 0xFF);
                        break;
                    case 3:
                        nameValue = (bytes[offset + 2] << 16)
                                + ((bytes[offset + 1] & 0xFF) << 8)
                                + (bytes[offset] & 0xFF);
                        break;
                    case 4:
                        nameValue = (bytes[offset + 3] << 24)
                                + ((bytes[offset + 2] & 0xFF) << 16)
                                + ((bytes[offset + 1] & 0xFF) << 8)
                                + (bytes[offset] & 0xFF);
                        break;
                    case 5:
                        nameValue = (((long) bytes[offset + 4]) << 32)
                                + (((long) bytes[offset + 3] & 0xFFL) << 24)
                                + (((long) bytes[offset + 2] & 0xFFL) << 16)
                                + (((long) bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 6:
                        nameValue = (((long) bytes[offset + 5]) << 40)
                                + (((long) bytes[offset + 4] & 0xFFL) << 32)
                                + (((long) bytes[offset + 3] & 0xFFL) << 24)
                                + (((long) bytes[offset + 2] & 0xFFL) << 16)
                                + (((long) bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 7:
                        nameValue = (((long) bytes[offset + 6]) << 48)
                                + (((long) bytes[offset + 5] & 0xFFL) << 40)
                                + (((long) bytes[offset + 4] & 0xFFL) << 32)
                                + (((long) bytes[offset + 3] & 0xFFL) << 24)
                                + (((long) bytes[offset + 2] & 0xFFL) << 16)
                                + (((long) bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 8:
                        nameValue = (((long) bytes[offset + 7]) << 56)
                                + (((long) bytes[offset + 6] & 0xFFL) << 48)
                                + (((long) bytes[offset + 5] & 0xFFL) << 40)
                                + (((long) bytes[offset + 4] & 0xFFL) << 32)
                                + (((long) bytes[offset + 3] & 0xFFL) << 24)
                                + (((long) bytes[offset + 2] & 0xFFL) << 16)
                                + (((long) bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
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

            int minCapacity = symbol * 2 + 2;
            if (symbols == null) {
                symbols = new long[minCapacity < 32 ? 32 : minCapacity];
            } else if (symbols.length < minCapacity) {
                symbols = Arrays.copyOf(symbols, minCapacity + 16);
            }

            symbols[symbol * 2] = hashCode;
            symbols[symbol * 2 + 1] = strInfo;
        }

        return hashCode;
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

        if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32) {
            int typeIndex;
            if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32_NUM_MAX) {
                offset++;
                typeIndex = strtype;
            } else if (strtype >= BC_INT32_BYTE_MIN && strtype <= BC_INT32_BYTE_MAX) {
                offset++;
                return ((strtype - BC_INT32_BYTE_ZERO) << 8)
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
            for (; offset < end; ) {
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
                                    ((c3 & 0x3F) << 0));
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
            if (MIXED_HASH_ALGORITHM && strlen <= 8) {
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
                symbols = new long[minCapacity < 32 ? 32 : minCapacity];
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
            for (; offset < end; ) {
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
                                    ((c3 & 0x3F) << 0));
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
                if (MIXED_HASH_ALGORITHM && strlen <= 16) {
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
            if (MIXED_HASH_ALGORITHM && strlen <= 16) {
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
            if (MIXED_HASH_ALGORITHM && strlen <= 16) {
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
            if (MIXED_HASH_ALGORITHM && strlen <= 8) {
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

        if (MIXED_HASH_ALGORITHM) {
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

        if (MIXED_HASH_ALGORITHM) {
            long nameValue = 0;
            for (int i = 0; i < strlen; offset++) {
                byte c = bytes[offset];
                if (c < 0 || i >= 8 || (i == 0 && bytes[strBegin] == 0)) {
                    offset = strBegin;
                    nameValue = 0;
                    break;
                }

                if (c == '_' || c == '-') {
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
        }

        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; i < strlen; ++i) {
            byte c = bytes[offset++];
            if (c >= 'A' && c <= 'Z') {
                c = (byte) (c + 32);
            }

            if (c == '_' || c == '-') {
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
                readInt64Value();
                return;
            case BC_DECIMAL:
                // TODO skip big decimal
                readInt32Value();
                readBigInteger();
                return;
            case BC_DECIMAL_LONG:
                readInt64Value();
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
                for (int i = 0; ; ++i) {
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
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    return;
                }

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

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
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

        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_UTF16BE) {
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
                    return getString();
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
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            long nameValue0 = -1, nameValue1 = -1;

            if (strtype == BC_STR_ASCII) {
                strlen = readLength();
                strBegin = offset;
            } else {
                strlen = strtype - BC_STR_ASCII_FIX_MIN;

                switch (strlen) {
                    case 1:
                        nameValue0 = bytes[offset];
                        break;
                    case 2:
                        nameValue0
                                = (bytes[offset + 1] << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 3:
                        nameValue0
                                = (bytes[offset + 2] << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 4:
                        nameValue0
                                = (bytes[offset + 3] << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 5:
                        nameValue0
                                = (((long) bytes[offset + 4]) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 6:
                        nameValue0
                                = (((long) bytes[offset + 5]) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset + 0] & 0xFFL);
                        break;
                    case 7:
                        nameValue0
                                = (((long) bytes[offset + 6]) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 4] & 0xFFL) << 24)
                                + ((bytes[offset + 3] & 0xFFL) << 16)
                                + ((bytes[offset + 2] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 8:
                        nameValue0
                                = (((long) bytes[offset + 7]) << 56)
                                + ((bytes[offset + 6] & 0xFFL) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        break;
                    case 9:
                        nameValue0 = bytes[offset + 0];
                        nameValue1
                                = (((long) bytes[offset + 8]) << 56)
                                + ((bytes[offset + 7]) << 48)
                                + ((bytes[offset + 6] & 0xFFL) << 40)
                                + ((bytes[offset + 5] & 0xFFL) << 32)
                                + ((bytes[offset + 4] & 0xFFL) << 24)
                                + ((bytes[offset + 3] & 0xFFL) << 16)
                                + ((bytes[offset + 2] & 0xFFL) << 8)
                                + (bytes[offset + 1] & 0xFFL);
                        break;
                    case 10:
                        nameValue0
                                = (bytes[offset + 1] << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 9]) << 56)
                                + ((bytes[offset + 8] & 0xFFL) << 48)
                                + ((bytes[offset + 7] & 0xFFL) << 40)
                                + ((bytes[offset + 6] & 0xFFL) << 32)
                                + ((bytes[offset + 5] & 0xFFL) << 24)
                                + ((bytes[offset + 4] & 0xFFL) << 16)
                                + ((bytes[offset + 3] & 0xFFL) << 8)
                                + (bytes[offset + 2] & 0xFFL);
                        break;
                    case 11:
                        nameValue0
                                = (bytes[offset + 2] << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 10]) << 56)
                                + ((bytes[offset + 9] & 0xFFL) << 48)
                                + ((bytes[offset + 8] & 0xFFL) << 40)
                                + ((bytes[offset + 7] & 0xFFL) << 32)
                                + ((bytes[offset + 6] & 0xFFL) << 24)
                                + ((bytes[offset + 5] & 0xFFL) << 16)
                                + ((bytes[offset + 4] & 0xFFL) << 8)
                                + (bytes[offset + 3] & 0xFFL);
                        break;
                    case 12:
                        nameValue0
                                = (bytes[offset + 3] << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 11]) << 56)
                                + ((bytes[offset + 10] & 0xFFL) << 48)
                                + ((bytes[offset + 9] & 0xFFL) << 40)
                                + ((bytes[offset + 8] & 0xFFL) << 32)
                                + ((bytes[offset + 7] & 0xFFL) << 24)
                                + ((bytes[offset + 6] & 0xFFL) << 16)
                                + ((bytes[offset + 5] & 0xFFL) << 8)
                                + (bytes[offset + 4] & 0xFFL);
                        break;
                    case 13:
                        nameValue0
                                = (((long) bytes[offset + 4]) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 12]) << 56)
                                + ((bytes[offset + 11] & 0xFFL) << 48)
                                + ((bytes[offset + 10] & 0xFFL) << 40)
                                + ((bytes[offset + 9] & 0xFFL) << 32)
                                + ((bytes[offset + 8] & 0xFFL) << 24)
                                + ((bytes[offset + 7] & 0xFFL) << 16)
                                + ((bytes[offset + 6] & 0xFFL) << 8)
                                + (bytes[offset + 5] & 0xFFL);
                        break;
                    case 14:
                        nameValue0
                                = (((long) bytes[offset + 5]) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 13]) << 56)
                                + ((bytes[offset + 12] & 0xFFL) << 48)
                                + ((bytes[offset + 11] & 0xFFL) << 40)
                                + ((bytes[offset + 10] & 0xFFL) << 32)
                                + ((bytes[offset + 9] & 0xFFL) << 24)
                                + ((bytes[offset + 8] & 0xFFL) << 16)
                                + ((bytes[offset + 7] & 0xFFL) << 8)
                                + (bytes[offset + 6] & 0xFFL);
                        break;
                    case 15:
                        nameValue0
                                = (((long) bytes[offset + 6]) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 14]) << 56)
                                + ((bytes[offset + 13] & 0xFFL) << 48)
                                + ((bytes[offset + 12] & 0xFFL) << 40)
                                + ((bytes[offset + 11] & 0xFFL) << 32)
                                + ((bytes[offset + 10] & 0xFFL) << 24)
                                + ((bytes[offset + 9] & 0xFFL) << 16)
                                + ((bytes[offset + 8] & 0xFFL) << 8)
                                + (bytes[offset + 7] & 0xFFL);
                        break;
                    case 16:
                        nameValue0
                                = (((long) bytes[offset + 7]) << 56)
                                + ((bytes[offset + 6]) << 48)
                                + ((bytes[offset + 5] & 0xFFL) << 40)
                                + ((bytes[offset + 4] & 0xFFL) << 32)
                                + ((bytes[offset + 3] & 0xFFL) << 24)
                                + ((bytes[offset + 2] & 0xFFL) << 16)
                                + ((bytes[offset + 1] & 0xFFL) << 8)
                                + (bytes[offset] & 0xFFL);
                        nameValue1
                                = (((long) bytes[offset + 15]) << 56)
                                + ((bytes[offset + 14] & 0xFFL) << 48)
                                + ((bytes[offset + 13] & 0xFFL) << 40)
                                + ((bytes[offset + 12] & 0xFFL) << 32)
                                + ((bytes[offset + 11] & 0xFFL) << 24)
                                + ((bytes[offset + 10] & 0xFFL) << 16)
                                + ((bytes[offset + 9] & 0xFFL) << 8)
                                + (bytes[offset + 8] & 0xFFL);
                        break;
                    default:
                        break;
                }
            }

            if (nameValue0 != -1) {
                if (nameValue1 != -1) {
                    int indexMask = ((int) nameValue1) & (NAME_CACHE2.length - 1);
                    JSONFactory.NameCacheEntry2 entry = NAME_CACHE2[indexMask];
                    if (entry == null) {
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) bytes[offset + i];
                            }
                            str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                        }

                        NAME_CACHE2[indexMask] = new JSONFactory.NameCacheEntry2(str, nameValue0, nameValue1);
                        offset += strlen;
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                        offset += strlen;
                        str = entry.name;
                    }
                } else {
                    int indexMask = ((int) nameValue0) & (NAME_CACHE.length - 1);
                    JSONFactory.NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) bytes[offset + i];
                            }
                            str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                        }

                        NAME_CACHE[indexMask] = new JSONFactory.NameCacheEntry(str, nameValue0);
                        offset += strlen;
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
                        chars[i] = (char) bytes[offset + i];
                    }
                    offset += strlen;

                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null && strlen >= 0) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, LATIN1);
                    offset += strlen;
                }
            }
            charset = StandardCharsets.US_ASCII;
        } else if (strtype == BC_STR_UTF8) {
            strlen = readLength();
            strBegin = offset;

            if (STRING_CREATOR_JDK11 != null && !JDKUtils.BIG_ENDIAN) {
                if (valueBytes == null) {
                    valueBytes = JSONFactory.allocateByteArray(cachedIndex);
                }

                int minCapacity = strlen << 1;
                if (valueBytes == null) {
                    valueBytes = new byte[minCapacity];
                } else {
                    if (minCapacity > valueBytes.length) {
                        valueBytes = new byte[minCapacity];
                    }
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
                    symbols = new long[minCapacity < 32 ? 32 : minCapacity];
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
        strtype = bytes[offset++];
        if (strtype == BC_NULL) {
            return null;
        }

        strBegin = offset;
        Charset charset;
        String str;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            if (strtype == BC_STR_ASCII) {
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
                strlen = strtype - BC_STR_ASCII_FIX_MIN;
            }

            if (strlen >= 0) {
                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) bytes[offset + i];
                    }
                    offset += strlen;
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                    if ((context.features & Feature.TrimString.mask) != 0) {
                        str = str.trim();
                    }

                    return str;
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars, LATIN1);
                    offset += strlen;

                    if ((context.features & Feature.TrimString.mask) != 0) {
                        str = str.trim();
                    }

                    return str;
                }
            }

            charset = StandardCharsets.US_ASCII;
        } else if (strtype == BC_STR_UTF8) {
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
                    valueBytes = JSONFactory.allocateByteArray(cachedIndex);
                }

                int minCapacity = strlen << 1;
                if (valueBytes == null) {
                    valueBytes = new byte[minCapacity];
                } else {
                    if (minCapacity > valueBytes.length) {
                        valueBytes = new byte[minCapacity];
                    }
                }

                int utf16_len = IOUtils.decodeUTF8(bytes, offset, strlen, valueBytes);
                if (utf16_len != -1) {
                    byte[] value = new byte[utf16_len];
                    System.arraycopy(valueBytes, 0, value, 0, utf16_len);
                    str = STRING_CREATOR_JDK11.apply(value, UTF16);
                    offset += strlen;

                    if ((context.features & Feature.TrimString.mask) != 0) {
                        str = str.trim();
                    }

                    return str;
                }
            }

            charset = StandardCharsets.UTF_8;
        } else if (strtype == BC_STR_UTF16) {
            strlen = readLength();
            strBegin = offset;
            charset = StandardCharsets.UTF_16;
        } else if (strtype == BC_STR_UTF16LE) {
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
                str = STRING_CREATOR_JDK11.apply(chars, UTF16);
                offset += strlen;

                if ((context.features & Feature.TrimString.mask) != 0) {
                    str = str.trim();
                }
                return str;
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

                if ((context.features & Feature.TrimString.mask) != 0) {
                    str = str.trim();
                }

                return str;
            }

            charset = StandardCharsets.UTF_16BE;
        } else if (strtype == BC_STR_GB18030) {
            strlen = readLength();
            strBegin = offset;

            if (GB18030 == null) {
                GB18030 = Charset.forName("GB18030");
            }
            charset = GB18030;
        } else if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32_NUM_MAX) {
            return Byte.toString(strtype);
        } else if (strtype >= BC_INT32_BYTE_MIN && strtype <= BC_INT32_BYTE_MAX) {
            int intValue = ((strtype - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
            return Integer.toString(intValue);
        } else if (strtype >= BC_INT32_SHORT_MIN && strtype <= BC_INT32_SHORT_MAX) {
            int intValue = ((strtype - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
            return Integer.toString(intValue);
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
        } else {
            switch (strtype) {
                case BC_NULL:
                    return null;
                case BC_DOUBLE_NUM_0:
                    return "0.0";
                case BC_DOUBLE_NUM_1:
                    return "1.0";
                case BC_INT64_INT:
                case BC_INT32: {
                    long int32Value =
                            ((bytes[offset + 3] & 0xFF)) +
                                    ((bytes[offset + 2] & 0xFF) << 8) +
                                    ((bytes[offset + 1] & 0xFF) << 16) +
                                    ((bytes[offset]) << 24);
                    offset += 4;
                    return Long.toString(int32Value);
                }
                case BC_FLOAT_INT:
                    return Float.toString(
                            readInt32Value());
                case BC_FLOAT: {
                    int int32Value =
                            ((bytes[offset + 3] & 0xFF)) +
                                    ((bytes[offset + 2] & 0xFF) << 8) +
                                    ((bytes[offset + 1] & 0xFF) << 16) +
                                    ((bytes[offset]) << 24);
                    offset += 4;
                    float floatValue = Float.intBitsToFloat(int32Value);
                    return Float.toString(floatValue);
                }
                case BC_DOUBLE: {
                    long int64Value =
                            ((bytes[offset + 7] & 0xFFL)) +
                                    ((bytes[offset + 6] & 0xFFL) << 8) +
                                    ((bytes[offset + 5] & 0xFFL) << 16) +
                                    ((bytes[offset + 4] & 0xFFL) << 24) +
                                    ((bytes[offset + 3] & 0xFFL) << 32) +
                                    ((bytes[offset + 2] & 0xFFL) << 40) +
                                    ((bytes[offset + 1] & 0xFFL) << 48) +
                                    ((long) (bytes[offset]) << 56);
                    offset += 8;
                    double doubleValue = Double.longBitsToDouble(int64Value);
                    return Double.toString(doubleValue);
                }
                case BC_TIMESTAMP_SECONDS: {
                    long int32Value =
                            ((bytes[offset + 3] & 0xFF)) +
                                    ((bytes[offset + 2] & 0xFF) << 8) +
                                    ((bytes[offset + 1] & 0xFF) << 16) +
                                    ((bytes[offset]) << 24);
                    offset += 4;
                    return Long.toString(int32Value * 1000);
                }
                case BC_TIMESTAMP_MINUTES: {
                    long minutes =
                            ((bytes[offset + 3] & 0xFF)) +
                                    ((bytes[offset + 2] & 0xFF) << 8) +
                                    ((bytes[offset + 1] & 0xFF) << 16) +
                                    ((bytes[offset]) << 24);
                    offset += 4;
                    return Long.toString(minutes * 60 * 1000);
                }
                case BC_TIMESTAMP_MILLIS:
                case BC_INT64:
                    long int64Value =
                            ((bytes[offset + 7] & 0xFFL)) +
                                    ((bytes[offset + 6] & 0xFFL) << 8) +
                                    ((bytes[offset + 5] & 0xFFL) << 16) +
                                    ((bytes[offset + 4] & 0xFFL) << 24) +
                                    ((bytes[offset + 3] & 0xFFL) << 32) +
                                    ((bytes[offset + 2] & 0xFFL) << 40) +
                                    ((bytes[offset + 1] & 0xFFL) << 48) +
                                    ((long) (bytes[offset]) << 56);
                    offset += 8;
                    return Long.toString(int64Value);
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
                    Object typedAny = readAny();
                    return typedAny == null ? null : typedAny.toString();
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

        if (strlen < 0) {
            return symbolTable.getName(-strlen);
        }

        char[] chars = null;
        if (JVM_VERSION == 8 && strtype == BC_STR_UTF8 && strlen < 8192) {
            final int cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
            chars = allocateCharArray(cachedIndex);
        }
        if (chars != null) {
            int len = IOUtils.decodeUTF8(bytes, offset, strlen, chars);
            str = new String(chars, 0, len);
            releaseCharArray(cachedIndex, chars);
        } else {
            str = new String(bytes, offset, strlen, charset);
        }
        offset += strlen;

        if ((context.features & Feature.TrimString.mask) != 0) {
            str = str.trim();
        }

        return str;
    }

    @Override
    public char readCharValue() {
        byte type = bytes[offset];
        if (type == BC_CHAR) {
            offset++;
            return (char) readInt32Value();
        } else if (type >= BC_STR_ASCII_FIX_0 && type < BC_STR_ASCII_FIX_MAX) {
            offset++;
            return (char) bytes[offset++];
        }

        String str = readString();
        if (str == null || str.isEmpty()) {
            wasNull = true;
            return '\0';
        }
        return str.charAt(0);
    }

    @Override
    public long readInt64Value() {
        wasNull = false;

        byte type = bytes[offset++];
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return type;
        }

        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            return (long) INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        }

        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
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

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            return ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
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
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
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
                long minutes =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return minutes * 60 * 1000;
            }
            case BC_TIMESTAMP_SECONDS: {
                long seconds =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return seconds * 1000;
            }
            case BC_TIMESTAMP_MILLIS:
            case BC_INT64:
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return int64Value;
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
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
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
                        return new BigDecimal(str).longValue();
                    }
                }
                break;
        }
        throw new JSONException("readInt64Value not support " + typeName(type) + ", offset " + offset + "/" + bytes.length);
    }

    @Override
    public int readInt32Value() {
        byte type = bytes[offset++];
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return type;
        }

        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            return ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            return (int) INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
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
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return (int) int64Value;
            }
            case BC_FLOAT_INT:
                return (int) (float) readInt32Value();
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
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
            case BC_INT32:
            case BC_INT64_INT:
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
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
                        return new BigDecimal(str).intValue();
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
        if (bytes[offset] == BC_NULL) {
            offset++;
            wasNull = true;
            return null;
        }

        long value = readInt64Value();
        if (wasNull) {
            return null;
        }
        return value;
    }

    protected final String readFixedAsciiString(int strlen) {
        String str;
        if (STRING_CREATOR_JDK8 != null) {
            char[] chars = new char[strlen];
            for (int i = 0; i < strlen; ++i) {
                chars[i] = (char) bytes[offset + i];
            }

            str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
        } else {
            str = new String(bytes, offset, strlen, StandardCharsets.ISO_8859_1);
        }
        return str;
    }

    @Override
    public float readFloatValue() {
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
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return (float) int64Value;
            }
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            }
            case BC_DOUBLE: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return (float) Double.longBitsToDouble(int64Value);
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE_LONG: {
                return (float) (double) readInt64Value();
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
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
                    return ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
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
                        return new BigDecimal(str).intValue();
                    }
                }
                break;
        }
        throw new JSONException("TODO : " + typeName(type));
    }

    @Override
    public double readDoubleValue() {
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
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return (double) int64Value;
            }
            case BC_INT64_INT:
            case BC_INT32: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            }
            case BC_FLOAT:
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE:
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return Double.longBitsToDouble(int64Value);
            case BC_DOUBLE_LONG:
                return readInt64Value();
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str).intValue();
                } else {
                    return new BigDecimal(str).intValue();
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
                    return ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
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
                        return new BigDecimal(str).intValue();
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
            return ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
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
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            }
            case BC_INT64_INT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return (long) int32Value;
            }
            case BC_INT64: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return int64Value;
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
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return Float.intBitsToFloat(int32Value);
            }
            case BC_FLOAT_INT: {
                return (float) readInt32Value();
            }
            case BC_DOUBLE:
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return Double.longBitsToDouble(int64Value);
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
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                return new BigDecimal(str);
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                return new BigDecimal(str);
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
                    return new BigDecimal(str);
                }
                break;
        }
        throw new JSONException("not support type :" + typeName(type));
    }

    @Override
    public BigDecimal readBigDecimal() {
        byte type = bytes[offset++];
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
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return BigDecimal.valueOf(int32Value);
            }
            case BC_FLOAT_INT: {
                float floatValue = (float) readInt32Value();
                return BigDecimal.valueOf((long) floatValue);
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
                return BigDecimal.valueOf((long) floatValue);
            }
            case BC_DOUBLE: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                double doubleValue = Double.longBitsToDouble(int64Value);
                return BigDecimal.valueOf(
                        (long) doubleValue);
            }
            case BC_INT64: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return BigDecimal.valueOf(int64Value);
            }
            case BC_BIGINT: {
                return new BigDecimal(
                        readBigInteger()
                );
            }
            case BC_DECIMAL: {
                int scale = readInt32Value();

                if (bytes[offset] == BC_BIGINT_LONG) {
                    offset++;
                    long unscaledLongValue = readInt64Value();
                    return BigDecimal.valueOf(unscaledLongValue, scale);
                }

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
            case BC_DOUBLE_LONG: {
                double doubleValue = readInt64Value();
                return BigDecimal.valueOf((long) doubleValue);
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                return new BigDecimal(str);
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                return new BigDecimal(str);
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                return new BigDecimal(str);
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
                    int intValue = ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigDecimal.valueOf(intValue);
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
                    return new BigDecimal(str);
                }
                break;
        }
        throw new JSONException("not support type :" + typeName(type));
    }

    @Override
    public BigInteger readBigInteger() {
        byte type = bytes[offset++];
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
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return BigInteger.valueOf(int32Value);
            }
            case BC_FLOAT_INT: {
                float floatValue = (float) readInt32Value();
                return BigInteger.valueOf(
                        (long) floatValue);
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                float floatValue = Float.intBitsToFloat(int32Value);
                return BigInteger.valueOf(
                        (long) floatValue);
            }
            case BC_DOUBLE: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                double doubleValue = Double.longBitsToDouble(int64Value);
                return BigInteger.valueOf(
                        (long) doubleValue);
            }
            case BC_INT64: {
                long int64Value =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;
                return BigInteger.valueOf(int64Value);
            }
            case BC_BIGINT:
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
            case BC_BIGINT_LONG: {
                return BigInteger.valueOf(
                        readInt64Value()
                );
            }
            case BC_DOUBLE_LONG: {
                double doubleValue = readInt64Value();
                return BigInteger.valueOf((long) doubleValue);
            }
            case BC_STR_ASCII: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return new BigDecimal(str).toBigInteger();
                }
            }
            case BC_STR_UTF8: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return new BigDecimal(str).toBigInteger();
                }
            }
            case BC_STR_UTF16LE: {
                int strlen = readInt32Value();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                if (str.indexOf('.') == -1) {
                    return new BigInteger(str);
                } else {
                    return new BigDecimal(str).toBigInteger();
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
                    int intValue = ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    return BigInteger.valueOf(intValue);
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

        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
            int len = getStringLength();
            switch (len) {
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

        throw new UnsupportedOperationException();
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

        throw new UnsupportedOperationException();
    }

    @Override
    protected LocalDateTime readLocalDateTime16() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    protected LocalDateTime readLocalDateTime17() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    protected LocalTime readLocalTime10() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    protected LocalTime readLocalTime11() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    protected LocalDate readLocalDate11() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        throw new JSONException("UnsupportedOperation");
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

        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX) {
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
            throw new JSONException("not support len : " + len);
        }

        if (type == BC_STR_UTF8 || type == BC_STR_ASCII) {
            strtype = (byte) type;
            offset++;
            strlen = readLength();
            switch (strlen) {
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
            throw new JSONException("not support len : " + strlen);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public Instant readInstant() {
        int type = bytes[offset++];
        switch (type) {
            case BC_TIMESTAMP: {
                long second = readInt64Value();
                int nano = readInt32Value();
                return Instant.ofEpochSecond(second, nano);
            }
            case BC_TIMESTAMP_MINUTES: {
                long second = readInt32Value() * 60;
                return Instant.ofEpochSecond(second, 0);
            }
            case BC_TIMESTAMP_SECONDS: {
                long second = readInt32Value();
                return Instant.ofEpochSecond(second, 0);
            }
            case BC_INT64:
            case BC_TIMESTAMP_MILLIS: {
                long millis = readInt64Value();
                return Instant.ofEpochMilli(millis);
            }
            default:
                break;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ZonedDateTime readZonedDateTime() {
        int type = bytes[offset++];
        switch (type) {
            case BC_TIMESTAMP: {
                long second = readInt64Value();
                int nano = readInt32Value();
                Instant instant = Instant.ofEpochSecond(second, nano);
                return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            case BC_TIMESTAMP_MINUTES: {
                long second = readInt32Value() * 60;
                Instant instant = Instant.ofEpochSecond(second);
                return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            case BC_TIMESTAMP_SECONDS: {
                long second = readInt32Value();
                Instant instant = Instant.ofEpochSecond(second);
                return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            case BC_INT64:
            case BC_TIMESTAMP_MILLIS: {
                long millis = readInt64Value();
                Instant instant = Instant.ofEpochMilli(millis);
                return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            case BC_TIMESTAMP_WITH_TIMEZONE:
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
                if (zoneIdHash == SHANGHAI_ZONE_ID_HASH) {
                    zoneId = SHANGHAI_ZONE_ID;
                } else {
                    String zoneIdStr = getString();
                    ZoneId contextZondId = context.getZoneId();
                    if (contextZondId.getId().equals(zoneIdStr)) {
                        zoneId = contextZondId;
                    } else {
                        zoneId = ZoneId.of(zoneIdStr);
                    }
                }
                return ZonedDateTime.of(ldt, zoneId);
            default:
                break;
        }
        throw new UnsupportedOperationException();
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
                long msb =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;

                long lsb =
                        ((bytes[offset + 7] & 0xFFL)) +
                                ((bytes[offset + 6] & 0xFFL) << 8) +
                                ((bytes[offset + 5] & 0xFFL) << 16) +
                                ((bytes[offset + 4] & 0xFFL) << 24) +
                                ((bytes[offset + 3] & 0xFFL) << 32) +
                                ((bytes[offset + 2] & 0xFFL) << 40) +
                                ((bytes[offset + 1] & 0xFFL) << 48) +
                                ((long) (bytes[offset]) << 56);
                offset += 8;

                return new UUID(msb, lsb);
            case BC_STR_ASCII_FIX_32: {
                long msb1 = parse4Nibbles(bytes, offset + 0);
                long msb2 = parse4Nibbles(bytes, offset + 4);
                long msb3 = parse4Nibbles(bytes, offset + 8);
                long msb4 = parse4Nibbles(bytes, offset + 12);
                long lsb1 = parse4Nibbles(bytes, offset + 16);
                long lsb2 = parse4Nibbles(bytes, offset + 20);
                long lsb3 = parse4Nibbles(bytes, offset + 24);
                long lsb4 = parse4Nibbles(bytes, offset + 28);
                if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                    offset += 32;
                    return new UUID(
                            msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                            lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
                }
                throw new JSONException("Invalid UUID string:  " + new String(bytes, offset, 32, StandardCharsets.US_ASCII));
            }
            case BC_STR_ASCII_FIX_36: {
                byte ch1 = bytes[offset + 8];
                byte ch2 = bytes[offset + 13];
                byte ch3 = bytes[offset + 18];
                byte ch4 = bytes[offset + 23];
                if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                    long msb1 = parse4Nibbles(bytes, offset + 0);
                    long msb2 = parse4Nibbles(bytes, offset + 4);
                    long msb3 = parse4Nibbles(bytes, offset + 9);
                    long msb4 = parse4Nibbles(bytes, offset + 14);
                    long lsb1 = parse4Nibbles(bytes, offset + 19);
                    long lsb2 = parse4Nibbles(bytes, offset + 24);
                    long lsb3 = parse4Nibbles(bytes, offset + 28);
                    long lsb4 = parse4Nibbles(bytes, offset + 32);
                    if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                        offset += 36;
                        return new UUID(
                                msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                                lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
                    }
                }
                throw new JSONException("Invalid UUID string:  " + new String(bytes, offset, 36, StandardCharsets.US_ASCII));
            }
            case BC_STR_ASCII:
            case BC_STR_UTF8: {
                int strlen = readLength();
                if (strlen == 32) {
                    long msb1 = parse4Nibbles(bytes, offset + 0);
                    long msb2 = parse4Nibbles(bytes, offset + 4);
                    long msb3 = parse4Nibbles(bytes, offset + 8);
                    long msb4 = parse4Nibbles(bytes, offset + 12);
                    long lsb1 = parse4Nibbles(bytes, offset + 16);
                    long lsb2 = parse4Nibbles(bytes, offset + 20);
                    long lsb3 = parse4Nibbles(bytes, offset + 24);
                    long lsb4 = parse4Nibbles(bytes, offset + 28);
                    if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                        offset += 32;
                        return new UUID(
                                msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                                lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
                    }
                } else if (strlen == 36) {
                    byte ch1 = bytes[offset + 8];
                    byte ch2 = bytes[offset + 13];
                    byte ch3 = bytes[offset + 18];
                    byte ch4 = bytes[offset + 23];
                    if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                        long msb1 = parse4Nibbles(bytes, offset + 0);
                        long msb2 = parse4Nibbles(bytes, offset + 4);
                        long msb3 = parse4Nibbles(bytes, offset + 9);
                        long msb4 = parse4Nibbles(bytes, offset + 14);
                        long lsb1 = parse4Nibbles(bytes, offset + 19);
                        long lsb2 = parse4Nibbles(bytes, offset + 24);
                        long lsb3 = parse4Nibbles(bytes, offset + 28);
                        long lsb4 = parse4Nibbles(bytes, offset + 32);
                        if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                            offset += 36;
                            return new UUID(
                                    msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                                    lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
                        }
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
    public boolean readBoolValue() {
        wasNull = false;
        byte type = bytes[offset++];
        switch (type) {
            case BC_INT32_NUM_1:
            case BC_TRUE:
                return true;
            case BC_INT32_NUM_0:
            case BC_FALSE:
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
                String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                offset += strlen;
                throw new JSONException("not support input " + str);
            }
            case BC_STR_UTF16LE: {
                strlen = readLength();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16LE);
                offset += strlen;
                switch (str) {
                    case "0":
                    case "N":
                        return false;
                    case "1":
                    case "Y":
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
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 8) {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 1];
        byte c1 = bytes[offset + 2];
        byte c2 = bytes[offset + 3];
        byte c3 = bytes[offset + 4];
        byte c4 = bytes[offset + 5];
        byte c5 = bytes[offset + 6];
        byte c6 = bytes[offset + 7];
        byte c7 = bytes[offset + 8];

        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '-' && c6 == '-') {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = '0';
            m1 = (char) c5;

            d0 = '0';
            d1 = (char) c7;
        } else {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = (char) c4;
            m1 = (char) c5;

            d0 = (char) c6;
            d1 = (char) c7;
        }

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

        LocalDate ldt = LocalDate.of(year, month, dom);

        offset += 9;
        return ldt;
    }

    @Override
    public LocalDate readLocalDate9() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 9) {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 1];
        byte c1 = bytes[offset + 2];
        byte c2 = bytes[offset + 3];
        byte c3 = bytes[offset + 4];
        byte c4 = bytes[offset + 5];
        byte c5 = bytes[offset + 6];
        byte c6 = bytes[offset + 7];
        byte c7 = bytes[offset + 8];
        byte c8 = bytes[offset + 9];

        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '-' && c6 == '-') {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = '0';
            m1 = (char) c5;

            d0 = (char) c7;
            d1 = (char) c8;
        } else if (c4 == '-' && c7 == '-') {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = (char) c5;
            m1 = (char) c6;

            d0 = '0';
            d1 = (char) c8;
        } else {
            return null;
        }

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

        LocalDate ldt = LocalDate.of(year, month, dom);

        offset += 10;
        return ldt;
    }

    @Override
    protected LocalDate readLocalDate10() {
        byte c0, c1, c2, c3, c4, c5, c6, c7, c8, c9;
        if (bytes[offset] == BC_STR_ASCII_FIX_MIN + 10) {
            c0 = bytes[offset + 1];
            c1 = bytes[offset + 2];
            c2 = bytes[offset + 3];
            c3 = bytes[offset + 4];
            c4 = bytes[offset + 5];
            c5 = bytes[offset + 6];
            c6 = bytes[offset + 7];
            c7 = bytes[offset + 8];
            c8 = bytes[offset + 9];
            c9 = bytes[offset + 10];
        } else if ((strtype == BC_STR_UTF8 || strtype == BC_STR_ASCII) && strlen == 10) {
            c0 = bytes[offset + 0];
            c1 = bytes[offset + 1];
            c2 = bytes[offset + 2];
            c3 = bytes[offset + 3];
            c4 = bytes[offset + 4];
            c5 = bytes[offset + 5];
            c6 = bytes[offset + 6];
            c7 = bytes[offset + 7];
            c8 = bytes[offset + 8];
            c9 = bytes[offset + 9];
        } else {
            throw new JSONException("date only support string input");
        }

        byte y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '-' && c7 == '-') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
        } else if (c4 == '/' && c7 == '/') { // tw : yyyy/mm/dd
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
        } else if (c2 == '.' && c5 == '.') {
            d0 = c0;
            d1 = c1;

            m0 = c3;
            m1 = c4;

            y0 = c6;
            y1 = c7;
            y2 = c8;
            y3 = c9;
        } else if (c2 == '-' && c5 == '-') {
            d0 = c0;
            d1 = c1;

            m0 = c3;
            m1 = c4;

            y0 = c6;
            y1 = c7;
            y2 = c8;
            y3 = c9;
        } else {
            return null;
        }

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

        if (year == 0 && month == 0 && dom == 0) {
            return null;
        }

        LocalDate ldt = LocalDate.of(year, month, dom);

        offset += 11;
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime5() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 5) {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 1];
        byte c1 = bytes[offset + 2];
        byte c2 = bytes[offset + 3];
        byte c3 = bytes[offset + 4];
        byte c4 = bytes[offset + 5];

        byte h0, h1, i0, i1, s0, s1;
        if (c2 == ':') {
            h0 = c0;
            h1 = c1;
            i0 = c3;
            i1 = c4;
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

        offset += 6;

        return LocalTime.of(hour, minute);
    }

    @Override
    protected LocalTime readLocalTime8() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 8) {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 1];
        byte c1 = bytes[offset + 2];
        byte c2 = bytes[offset + 3];
        byte c3 = bytes[offset + 4];
        byte c4 = bytes[offset + 5];
        byte c5 = bytes[offset + 6];
        byte c6 = bytes[offset + 7];
        byte c7 = bytes[offset + 8];

        byte h0, h1, i0, i1, s0, s1;
        if (c2 == ':' && c5 == ':') {
            h0 = c0;
            h1 = c1;
            i0 = c3;
            i1 = c4;
            s0 = c6;
            s1 = c7;
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

        int seccond;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            seccond = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        offset += 9;

        return LocalTime.of(hour, minute, seccond);
    }

    @Override
    protected LocalTime readLocalTime12() {
        byte c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11;
        if (bytes[offset] == BC_STR_ASCII_FIX_MIN + 12) {
            c0 = bytes[offset + 1];
            c1 = bytes[offset + 2];
            c2 = bytes[offset + 3];
            c3 = bytes[offset + 4];
            c4 = bytes[offset + 5];
            c5 = bytes[offset + 6];
            c6 = bytes[offset + 7];
            c7 = bytes[offset + 8];
            c8 = bytes[offset + 9];
            c9 = bytes[offset + 10];
            c10 = bytes[offset + 11];
            c11 = bytes[offset + 12];
        } else if ((strtype == BC_STR_UTF8 || strtype == BC_STR_ASCII) && strlen == 12) {
            c0 = bytes[offset];
            c1 = bytes[offset + 1];
            c2 = bytes[offset + 2];
            c3 = bytes[offset + 3];
            c4 = bytes[offset + 4];
            c5 = bytes[offset + 5];
            c6 = bytes[offset + 6];
            c7 = bytes[offset + 7];
            c8 = bytes[offset + 8];
            c9 = bytes[offset + 9];
            c10 = bytes[offset + 10];
            c11 = bytes[offset + 11];
        } else {
            throw new JSONException("date only support string input");
        }

        byte h0, h1, i0, i1, s0, s1, m0, m1, m2;
        if (c2 == ':' && c5 == ':' && c8 == '.') {
            h0 = c0;
            h1 = c1;
            i0 = c3;
            i1 = c4;
            s0 = c6;
            s1 = c7;
            m0 = c9;
            m1 = c10;
            m2 = c11;
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

        int seccond;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            seccond = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int millis;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
                && m2 >= '0' && m2 <= '9'
        ) {
            millis = (m0 - '0') * 100 + (m1 - '0') * 10 + (m2 - '0');
            millis *= 1000_000;
        } else {
            return null;
        }

        offset += 13;

        return LocalTime.of(hour, minute, seccond, millis);
    }

    @Override
    protected LocalTime readLocalTime18() {
        byte c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15, c16, c17;
        if (bytes[offset] == BC_STR_ASCII_FIX_MIN + 18) {
            c0 = bytes[offset + 1];
            c1 = bytes[offset + 2];
            c2 = bytes[offset + 3];
            c3 = bytes[offset + 4];
            c4 = bytes[offset + 5];
            c5 = bytes[offset + 6];
            c6 = bytes[offset + 7];
            c7 = bytes[offset + 8];
            c8 = bytes[offset + 9];
            c9 = bytes[offset + 10];
            c10 = bytes[offset + 11];
            c11 = bytes[offset + 12];
            c12 = bytes[offset + 13];
            c13 = bytes[offset + 14];
            c14 = bytes[offset + 15];
            c15 = bytes[offset + 16];
            c16 = bytes[offset + 17];
            c17 = bytes[offset + 18];
        } else if ((strtype == BC_STR_UTF8 || strtype == BC_STR_ASCII) && strlen == 18) {
            c0 = bytes[offset];
            c1 = bytes[offset + 1];
            c2 = bytes[offset + 2];
            c3 = bytes[offset + 3];
            c4 = bytes[offset + 4];
            c5 = bytes[offset + 5];
            c6 = bytes[offset + 6];
            c7 = bytes[offset + 7];
            c8 = bytes[offset + 8];
            c9 = bytes[offset + 9];
            c10 = bytes[offset + 10];
            c11 = bytes[offset + 11];
            c12 = bytes[offset + 12];
            c13 = bytes[offset + 13];
            c14 = bytes[offset + 14];
            c15 = bytes[offset + 15];
            c16 = bytes[offset + 16];
            c17 = bytes[offset + 17];
        } else {
            throw new JSONException("date only support string input");
        }

        byte h0, h1, i0, i1, s0, s1, m0, m1, m2, m3, m4, m5, m6, m7, m8;
        if (c2 == ':' && c5 == ':' && c8 == '.') {
            h0 = c0;
            h1 = c1;
            i0 = c3;
            i1 = c4;
            s0 = c6;
            s1 = c7;
            m0 = c9;
            m1 = c10;
            m2 = c11;
            m3 = c12;
            m4 = c13;
            m5 = c14;
            m6 = c15;
            m7 = c16;
            m8 = c17;
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

        int seccond;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            seccond = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        int millis;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
                && m2 >= '0' && m2 <= '9'
                && m3 >= '0' && m3 <= '9'
                && m4 >= '0' && m4 <= '9'
                && m5 >= '0' && m5 <= '9'
                && m6 >= '0' && m6 <= '9'
                && m7 >= '0' && m7 <= '9'
                && m8 >= '0' && m8 <= '9'
        ) {
            millis = (m0 - '0') * 1000_000_00
                    + (m1 - '0') * 1000_000_0
                    + (m2 - '0') * 1000_000
                    + (m3 - '0') * 1000_00
                    + (m4 - '0') * 1000_0
                    + (m5 - '0') * 1000
                    + (m6 - '0') * 100
                    + (m7 - '0') * 10
                    + (m8 - '0');
        } else {
            return null;
        }

        offset += 19;

        return LocalTime.of(hour, minute, seccond, millis);
    }

    @Override
    protected LocalDateTime readLocalDateTime18() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public long readMillis19() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 19) {
            throw new JSONException("date only support string input");
        }

        char c0 = (char) bytes[offset + 1];
        char c1 = (char) bytes[offset + 2];
        char c2 = (char) bytes[offset + 3];
        char c3 = (char) bytes[offset + 4];
        char c4 = (char) bytes[offset + 5];
        char c5 = (char) bytes[offset + 6];
        char c6 = (char) bytes[offset + 7];
        char c7 = (char) bytes[offset + 8];
        char c8 = (char) bytes[offset + 9];
        char c9 = (char) bytes[offset + 10];
        char c10 = (char) bytes[offset + 11];
        char c11 = (char) bytes[offset + 12];
        char c12 = (char) bytes[offset + 13];
        char c13 = (char) bytes[offset + 14];
        char c14 = (char) bytes[offset + 15];
        char c15 = (char) bytes[offset + 16];
        char c16 = (char) bytes[offset + 17];
        char c17 = (char) bytes[offset + 18];
        char c18 = (char) bytes[offset + 19];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else if (c4 == '/' && c7 == '/' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else {
            wasNull = true;
            return 0;
        }

        int year;
        if (y0 >= '0' && y0 <= '9'
                && y1 >= '0' && y1 <= '9'
                && y2 >= '0' && y2 <= '9'
                && y3 >= '0' && y3 <= '9'
        ) {
            year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int month;
        if (m0 >= '0' && m0 <= '9'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int dom;
        if (d0 >= '0' && d0 <= '9'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int hour;
        if (h0 >= '0' && h0 <= '9'
                && h1 >= '0' && h1 <= '9'
        ) {
            hour = (h0 - '0') * 10 + (h1 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int minute;
        if (i0 >= '0' && i0 <= '9'
                && i1 >= '0' && i1 <= '9'
        ) {
            minute = (i0 - '0') * 10 + (i1 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            wasNull = true;
            return 0;
        }

        int nanoOfSecond;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
        ) {
            nanoOfSecond = (S0 - '0') * 100
                    + (S1 - '0') * 10
                    + (S2 - '0');
            nanoOfSecond *= 1000_000;
        } else {
            wasNull = true;
            return 0;
        }

        offset += 20;
        return DateUtils.millis(context.getZoneId(), year, month, dom, hour, minute, second, nanoOfSecond);
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        type = bytes[offset];
        if (type != BC_STR_ASCII_FIX_MIN + 19) {
            throw new JSONException("date only support string input");
        }

        char c0 = (char) bytes[offset + 1];
        char c1 = (char) bytes[offset + 2];
        char c2 = (char) bytes[offset + 3];
        char c3 = (char) bytes[offset + 4];
        char c4 = (char) bytes[offset + 5];
        char c5 = (char) bytes[offset + 6];
        char c6 = (char) bytes[offset + 7];
        char c7 = (char) bytes[offset + 8];
        char c8 = (char) bytes[offset + 9];
        char c9 = (char) bytes[offset + 10];
        char c10 = (char) bytes[offset + 11];
        char c11 = (char) bytes[offset + 12];
        char c12 = (char) bytes[offset + 13];
        char c13 = (char) bytes[offset + 14];
        char c14 = (char) bytes[offset + 15];
        char c15 = (char) bytes[offset + 16];
        char c16 = (char) bytes[offset + 17];
        char c17 = (char) bytes[offset + 18];
        char c18 = (char) bytes[offset + 19];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else if (c4 == '/' && c7 == '/' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = c11;
            h1 = c12;

            i0 = c14;
            i1 = c15;

            s0 = c17;
            s1 = c18;

            S0 = '0';
            S1 = '0';
            S2 = '0';
        } else {
            return null;
        }

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

        int millis;
        if (S0 >= '0' && S0 <= '9'
                && S1 >= '0' && S1 <= '9'
                && S2 >= '0' && S2 <= '9'
        ) {
            millis = (S0 - '0') * 100
                    + (S1 - '0') * 10
                    + (S2 - '0');
            millis *= 1000_000;
        } else {
            return null;
        }

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second, millis);

        offset += 20;
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTimeX(int len) {
        type = bytes[offset];
        if (type < BC_STR_ASCII_FIX_MIN || type > BC_STR_ASCII_FIX_MAX) {
            throw new JSONException("date only support string input");
        }

        if (len < 21 || len > 29) {
            throw new JSONException("illeal localdatetime string : " + readString());
        }

        byte c0 = bytes[offset + 1];
        byte c1 = bytes[offset + 2];
        byte c2 = bytes[offset + 3];
        byte c3 = bytes[offset + 4];
        byte c4 = bytes[offset + 5];
        byte c5 = bytes[offset + 6];
        byte c6 = bytes[offset + 7];
        byte c7 = bytes[offset + 8];
        byte c8 = bytes[offset + 9];
        byte c9 = bytes[offset + 10];
        byte c10 = bytes[offset + 11];
        byte c11 = bytes[offset + 12];
        byte c12 = bytes[offset + 13];
        byte c13 = bytes[offset + 14];
        byte c14 = bytes[offset + 15];
        byte c15 = bytes[offset + 16];
        byte c16 = bytes[offset + 17];
        byte c17 = bytes[offset + 18];
        byte c18 = bytes[offset + 19];
        byte c19 = bytes[offset + 20];
        byte c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0';
        switch (len) {
            case 21:
                c20 = bytes[offset + 21];
                break;
            case 22:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                break;
            case 23:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                break;
            case 24:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                break;
            case 25:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                c24 = bytes[offset + 25];
                break;
            case 26:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                c24 = bytes[offset + 25];
                c25 = bytes[offset + 26];
                break;
            case 27:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                c24 = bytes[offset + 25];
                c25 = bytes[offset + 26];
                c26 = bytes[offset + 27];
                break;
            case 28:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                c24 = bytes[offset + 25];
                c25 = bytes[offset + 26];
                c26 = bytes[offset + 27];
                c27 = bytes[offset + 28];
                break;
            default:
                c20 = bytes[offset + 21];
                c21 = bytes[offset + 22];
                c22 = bytes[offset + 23];
                c23 = bytes[offset + 24];
                c24 = bytes[offset + 25];
                c25 = bytes[offset + 26];
                c26 = bytes[offset + 27];
                c27 = bytes[offset + 28];
                c28 = bytes[offset + 29];
                break;
        }

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.') {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = (char) c5;
            m1 = (char) c6;

            d0 = (char) c8;
            d1 = (char) c9;

            h0 = (char) c11;
            h1 = (char) c12;

            i0 = (char) c14;
            i1 = (char) c15;

            s0 = (char) c17;
            s1 = (char) c18;

            S0 = (char) c20;
            S1 = (char) c21;
            S2 = (char) c22;
            S3 = (char) c23;
            S4 = (char) c24;
            S5 = (char) c25;
            S6 = (char) c26;
            S7 = (char) c27;
            S8 = (char) c28;
        } else {
            return null;
        }

        LocalDateTime ldt = localDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);
        if (ldt == null) {
            return null;
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
    public final void close() {
        if (valueBytes != null) {
            JSONFactory.releaseByteArray(cachedIndex, valueBytes);
        }
    }
}

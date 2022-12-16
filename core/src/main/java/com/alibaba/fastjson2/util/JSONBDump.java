package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONB.typeName;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;
import static com.alibaba.fastjson2.util.JDKUtils.UTF16;

public class JSONBDump {
    static Charset GB18030;

    final byte[] bytes;
    final boolean raw;
    int offset;
    byte type;

    int strlen;
    byte strtype;
    int strBegin;

    String lastReference;

    JSONWriter jsonWriter;
    SymbolTable symbolTable;

    Map<Integer, String> symbols = new HashMap<>();

    public static void dump(byte[] jsonbBytes) {
        JSONBDump dump = new JSONBDump(jsonbBytes, true);
        String str = dump.toString();
        System.out.println(str);
    }

    public static void dump(byte[] jsonbBytes, SymbolTable symbolTable) {
        JSONBDump dump = new JSONBDump(jsonbBytes, symbolTable, true);
        String str = dump.toString();
        System.out.println(str);
    }

    public JSONBDump(byte[] bytes, boolean raw) {
        this.bytes = bytes;
        this.raw = raw;
        jsonWriter = JSONWriter.ofPretty();

        dumpAny();
    }

    public JSONBDump(byte[] bytes, SymbolTable symbolTable, boolean raw) {
        this.bytes = bytes;
        this.raw = raw;
        this.symbolTable = symbolTable;
        jsonWriter = JSONWriter.ofPretty();

        dumpAny();
    }

    private void dumpAny() {
        if (offset >= bytes.length) {
            return;
        }

        type = bytes[offset++];

        switch (type) {
            case BC_NULL:
                jsonWriter.writeNull();
                return;
            case BC_TRUE:
                jsonWriter.writeBool(true);
                return;
            case BC_FALSE:
                jsonWriter.writeBool(false);
                return;
            case BC_DOUBLE_NUM_0:
                jsonWriter.writeDouble(0);
                return;
            case BC_DOUBLE_NUM_1:
                jsonWriter.writeDouble(1);
                return;
            case BC_INT8:
                jsonWriter.writeInt8(bytes[offset++]);
                return;
            case BC_INT16:
                jsonWriter.writeInt16(
                        (short) (
                                (bytes[offset++] << 8)
                                        + (bytes[offset++] & 0xFF)
                        )
                );
                return;
            case BC_TIMESTAMP_MINUTES:
            case BC_TIMESTAMP_SECONDS:
            case BC_INT32: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                jsonWriter.writeInt32(int32Value);
                return;
            }
            case BC_INT64_INT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                jsonWriter.writeInt64(int32Value);
                return;
            }
            case BC_TIMESTAMP_MILLIS:
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
                jsonWriter.writeInt64(int64Value);
                jsonWriter.writeInt64(int64Value);
                return;
            }
            case BC_BIGINT: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                jsonWriter.writeBigInt(
                        new BigInteger(bytes));
                return;
            }
            case BC_BIGINT_LONG: {
                jsonWriter.writeInt64(
                        readInt64Value()
                );
                return;
            }
            case BC_FLOAT: {
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                jsonWriter.writeFloat(
                        Float.intBitsToFloat(int32Value));
                return;
            }
            case BC_FLOAT_INT: {
                jsonWriter.writeFloat(
                        readInt32Value()
                );
                return;
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
                jsonWriter.writeDouble(
                        Double.longBitsToDouble(int64Value));
                return;
            }
            case BC_DOUBLE_LONG: {
                jsonWriter.writeDouble(
                        readInt64Value()
                );
                return;
            }
            case BC_STR_UTF8: {
                int strlen = readLength();

                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_8);
                offset += strlen;
                jsonWriter.writeString(str);
                return;
            }
            case BC_CHAR: {
                int intValue = readInt32Value();
                jsonWriter.writeChar((char) intValue);
                return;
            }
            case BC_STR_UTF16: {
                int strlen = readLength();
                String str = new String(bytes, offset, strlen, StandardCharsets.UTF_16);
                offset += strlen;
                jsonWriter.writeString(str);
                return;
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
                jsonWriter.writeString(str);
                return;
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
                jsonWriter.writeString(str);
                return;
            }
            case BC_STR_GB18030: {
                if (GB18030 == null) {
                    GB18030 = Charset.forName("GB18030");
                }

                int strlen = readLength();
                String str = new String(bytes, offset, strlen, GB18030);
                offset += strlen;
                jsonWriter.writeString(str);
                return;
            }
            case BC_SYMBOL: {
                int symbol;
                if (isInt()) {
                    symbol = readInt32Value();
                    if (raw) {
                        jsonWriter.writeString("#" + symbol);
                    } else {
                        String name = getString(symbol);
                        jsonWriter.writeString(name);
                    }
                } else {
                    String name = readString();
                    symbol = readInt32Value();
                    symbols.put(symbol, name);
                    if (raw) {
                        jsonWriter.writeString(name + "#" + symbol);
                    } else {
                        jsonWriter.writeString(name);
                    }
                }
                return;
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
                jsonWriter.writeDecimal(decimal);
                return;
            }
            case BC_DECIMAL_LONG: {
                jsonWriter.writeDecimal(
                        BigDecimal.valueOf(
                                readInt64Value()
                        )
                );
                return;
            }
            case BC_TYPED_ANY: {
                boolean isInt = isInt();
                int symbol;
                String typeName = null;
                if (isInt) {
                    symbol = readInt32Value();
                } else {
                    typeName = readString();
                    symbol = readInt32Value();
                    symbols.put(symbol, typeName);
                }

                if (!raw && bytes[offset] == BC_OBJECT) {
                    if (typeName == null) {
                        typeName = getString(symbol);
                    }
                    offset++;
                    dumpObject(typeName);
                    return;
                }

                jsonWriter.startObject();

                jsonWriter.writeName("@type");
                jsonWriter.writeColon();
                if (typeName == null) {
                    if (symbol < 0) {
                        if (raw) {
                            jsonWriter.writeString("#" + symbol);
                        } else {
                            String name = symbolTable.getName(-symbol);
                            jsonWriter.writeString(name);
                        }
                    } else {
                        jsonWriter.writeString("#" + symbol);
                    }
                } else {
                    if (raw) {
                        jsonWriter.writeString(typeName + "#" + symbol);
                    } else {
                        jsonWriter.writeString(typeName);
                    }
                }

                jsonWriter.writeName("@value");
                jsonWriter.writeColon();
                dumpAny();

                jsonWriter.endObject();
                return;
            }
            case BC_BINARY: {
                int len = readInt32Value();
                byte[] bytes = new byte[len];
                System.arraycopy(this.bytes, offset, bytes, 0, len);
                offset += len;
                jsonWriter.writeBinary(bytes);
                return;
            }
            case BC_REFERENCE: {
                dumpReference();
                break;
            }
            case BC_LOCAL_DATETIME: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                int month = bytes[offset++];
                int dayOfMonth = bytes[offset++];
                int hour = bytes[offset++];
                int minute = bytes[offset++];
                int second = bytes[offset++];

                int nano = readInt32Value();

                LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nano);
                jsonWriter.writeLocalDateTime(localDateTime);
                break;
            }
            case BC_LOCAL_DATE: {
                int year = (bytes[offset++] << 8) + (bytes[offset++] & 0xFF);
                int month = bytes[offset++];
                int dayOfMonth = bytes[offset++];

                LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
                jsonWriter.writeLocalDate(localDate);
                break;
            }
            case BC_OBJECT: {
                dumpObject(null);
                break;
            }
            default:
                if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
                    jsonWriter.writeInt32(type);
                    return;
                }

                if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
                    long value = INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
                    jsonWriter.writeInt64(value);
                    return;
                }

                if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
                    int value = ((type - BC_INT32_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    jsonWriter.writeInt32(value);
                    return;
                }

                if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
                    int value = ((type - BC_INT32_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    jsonWriter.writeInt32(value);
                    return;
                }

                if (type >= BC_INT64_BYTE_MIN && type <= BC_INT64_BYTE_MAX) {
                    int value = ((type - BC_INT64_BYTE_ZERO) << 8)
                            + (bytes[offset++] & 0xFF);
                    jsonWriter.writeInt32(value);
                    return;
                }

                if (type >= BC_INT64_SHORT_MIN && type <= BC_INT64_SHORT_MAX) {
                    int value = ((type - BC_INT64_SHORT_ZERO) << 16)
                            + ((bytes[offset++] & 0xFF) << 8)
                            + (bytes[offset++] & 0xFF);
                    jsonWriter.writeInt64(value);
                    return;
                }

                if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY) {
                    dumpArray();
                    return;
                }

                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII) {
                    strlen = type == BC_STR_ASCII
                            ? readLength()
                            : type - BC_STR_ASCII_FIX_MIN;

                    if (strlen < 0) {
                        jsonWriter.writeRaw("{\"$symbol\":");
                        jsonWriter.writeInt32(strlen);
                        jsonWriter.writeRaw("}");
                        return;
                    }

                    String str = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                    offset += strlen;
                    jsonWriter.writeString(str);
                    return;
                }

                throw new JSONException("not support type : " + typeName(type) + ", offset " + offset);
        }
    }

    private void dumpArray() {
        int len = type == BC_ARRAY
                ? readLength()
                : type - BC_ARRAY_FIX_MIN;

        if (len == 0) {
            jsonWriter.writeRaw("[]");
            return;
        }

        if (len == 1) {
            type = bytes[offset];
            if (isInt() || type == BC_NULL || (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_ASCII_FIX_MAX)) {
                jsonWriter.writeRaw("[");
                dumpAny();
                jsonWriter.writeRaw("]");
                return;
            }
        }

        jsonWriter.startArray();
        for (int i = 0; i < len; ++i) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            if (isReference()) {
                dumpReference();
                continue;
            }

            dumpAny();
        }
        jsonWriter.endArray();
    }

    private void dumpObject(String typeName) {
        if (typeName != null) {
            jsonWriter.startObject();
            jsonWriter.writeName("@type");
            jsonWriter.writeColon();
            jsonWriter.writeString(typeName);
        } else {
            if (bytes[offset] == BC_OBJECT_END) {
                jsonWriter.writeRaw("{}");
                offset++;
                return;
            }
            jsonWriter.startObject();
        }

        _for:
        for (int valueCount = 0; ; ) {
            byte type = bytes[offset];
            switch (type) {
                case BC_OBJECT_END:
                    offset++;
                    break _for;
                case BC_TRUE:
                    offset++;
                    jsonWriter.writeName("true");
                    break;
                case BC_FALSE:
                    offset++;
                    jsonWriter.writeName("false");
                    break;
                case BC_REFERENCE: {
                    dumpReference();
                    break;
                }
                case BC_SYMBOL: {
                    offset++;
                    if (isInt()) {
                        int symbol = readInt32Value();
                        if (raw) {
                            jsonWriter.writeName("#" + symbol);
                        } else {
                            String name = symbols.get(symbol);
                            if (name == null) {
                                throw new JSONException("symbol not found " + symbol);
                            }
                            jsonWriter.writeName(name);
                        }
                    } else {
                        String name = readString();
                        int symbol = readInt32Value();

                        symbols.put(symbol, name);
                        if (raw) {
                            jsonWriter.writeName(name + "#" + symbol);
                        } else {
                            jsonWriter.writeName(name);
                        }
                    }
                    break;
                }
                case BC_NULL:
                    jsonWriter.writeNameRaw("null".toCharArray());
                    offset++;
                    break;
                default:
                    if (isString()) {
                        jsonWriter.writeName(readString());
                    } else {
                        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32) {
                            int intValue = readInt32Value();
                            int size = (intValue < 0) ? IOUtils.stringSize(-intValue) + 1 : IOUtils.stringSize(intValue);
                            if (jsonWriter.utf16) {
                                char[] chars = new char[size];
                                IOUtils.getChars(intValue, chars.length, chars);
                                jsonWriter.writeNameRaw(chars);
                            } else {
                                byte[] chars = new byte[size];
                                IOUtils.getChars(intValue, chars.length, chars);
                                jsonWriter.writeNameRaw(chars);
                            }
                        } else if ((type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX)
                                || type == BC_INT64
                        ) {
                            long value = readInt64Value();
                            int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                            char[] chars = new char[size];
                            IOUtils.getChars(value, chars.length, chars);
                            jsonWriter.writeNameRaw(chars);
                        } else {
                            if (valueCount != 0) {
                                jsonWriter.writeComma();
                            }

                            dumpAny();
                        }
                    }
                    break;
            }

            valueCount++;
            jsonWriter.writeColon();

            if (isReference()) {
                dumpReference();
                continue;
            }

            dumpAny();
        }

        jsonWriter.endObject();
    }

    private void dumpReference() {
        jsonWriter.writeRaw("{\"$ref\":");
        String reference = readReference();
        jsonWriter.writeString(reference);
        if (!"#-1".equals(reference)) {
            lastReference = reference;
        }
        jsonWriter.writeRaw("}");
    }

    int readInt32Value() {
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

        switch (type) {
            case BC_INT32:
            case BC_TIMESTAMP_MINUTES:
            case BC_TIMESTAMP_SECONDS:
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
            default:
                break;
        }
        throw new JSONException("readInt32Value not support " + typeName(type) + ", offset " + offset + "/" + bytes.length);
    }

    long readInt64Value() {
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

        switch (type) {
            case BC_INT8:
                return bytes[offset++];
            case BC_INT16:
                int int16Value =
                        ((bytes[offset + 1] & 0xFF) +
                                (bytes[offset] << 8));
                offset += 2;
                return int16Value;
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
            case BC_INT32:
            case BC_INT64_INT:
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return int32Value;
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
            default:
                break;
        }
        throw new JSONException("readInt64Value not support " + typeName(type) + ", offset " + offset + "/" + bytes.length);
    }

    int readLength() {
        byte type = bytes[offset++];
        if (type >= BC_INT32_NUM_MIN && type <= BC_INT32_NUM_MAX) {
            return type;
        }

        if (type >= BC_INT32_SHORT_MIN && type <= BC_INT32_SHORT_MAX) {
            return ((type - BC_INT32_SHORT_ZERO) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type >= BC_INT64_NUM_MIN && type <= BC_INT64_NUM_MAX) {
            return INT64_NUM_LOW_VALUE + (type - BC_INT64_NUM_MIN);
        }

        if (type >= BC_INT32_BYTE_MIN && type <= BC_INT32_BYTE_MAX) {
            return ((type - BC_INT32_BYTE_ZERO) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        if (type == BC_INT32) {
            return (bytes[offset++] << 24)
                    + ((bytes[offset++] & 0xFF) << 16)
                    + ((bytes[offset++] & 0xFF) << 8)
                    + (bytes[offset++] & 0xFF);
        }

        throw new JSONException("not support length type : " + type);
    }

    BigInteger readBigInteger() {
        int type = bytes[offset++];
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

        switch (type) {
            case BC_NULL:
                return null;
            case BC_FALSE:
                return BigInteger.ZERO;
            case BC_TRUE:
                return BigInteger.ONE;
            case BC_INT32:
                int int32Value =
                        ((bytes[offset + 3] & 0xFF)) +
                                ((bytes[offset + 2] & 0xFF) << 8) +
                                ((bytes[offset + 1] & 0xFF) << 16) +
                                ((bytes[offset]) << 24);
                offset += 4;
                return BigInteger.valueOf(int32Value);
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
            case BC_BIGINT_LONG: {
                return BigInteger.valueOf(
                        readInt64Value()
                );
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
            default:
                break;
        }
        throw new JSONException("not support type :" + type);
    }

    boolean isReference() {
        return offset < bytes.length && bytes[offset] == BC_REFERENCE;
    }

    boolean isString() {
        byte type = bytes[offset];
        return type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_UTF16BE;
    }

    String readString() {
        strtype = bytes[offset++];

        if (strtype == BC_NULL) {
            return null;
        }

        strBegin = offset;
        Charset charset;
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            if (strtype == BC_STR_ASCII) {
                strlen = readLength();
                strBegin = offset;
            } else {
                strlen = strtype - BC_STR_ASCII_FIX_MIN;
            }

            charset = StandardCharsets.US_ASCII;
        } else if (strtype == BC_STR_UTF8) {
            strlen = readLength();
            strBegin = offset;

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
                String str = STRING_CREATOR_JDK11.apply(chars, UTF16);
                offset += strlen;
                return str;
            }

            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            strlen = readLength();
            strBegin = offset;

            if (STRING_CREATOR_JDK11 != null && JDKUtils.BIG_ENDIAN) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);
                String str = STRING_CREATOR_JDK11.apply(chars, UTF16);
                offset += strlen;
                return str;
            }

            charset = StandardCharsets.UTF_16BE;
        } else if (strtype >= BC_INT32_NUM_MIN && strtype <= BC_INT32_NUM_MAX) {
            return Byte.toString(strtype);
        } else {
            throw new JSONException("readString not support type " + typeName(strtype) + ", offset " + offset + "/" + bytes.length);
        }

        if (strlen < 0) {
            return symbolTable.getName(-strlen);
        }

        String str = new String(bytes, offset, strlen, charset);
        offset += strlen;
        return str;
    }

    String readReference() {
        if (bytes[offset] != BC_REFERENCE) {
            return null;
        }
        offset++;
        if (isString()) {
            return readString();
        }

        throw new JSONException("reference not support input " + typeName(type));
    }

    @Override
    public String toString() {
        return jsonWriter.toString();
    }

    boolean isInt() {
        int type = bytes[offset];
        return (type >= BC_BIGINT_LONG && type <= BC_INT32)
                || type == BC_TIMESTAMP_MINUTES
                || type == BC_TIMESTAMP_SECONDS
                || type == BC_TIMESTAMP_MILLIS;
    }

    public String getString(int symbol) {
        String name;
        if (symbol < 0) {
            name = symbolTable.getName(-symbol);
        } else {
            name = symbols.get(symbol);
        }

        if (name == null) {
            throw new JSONException("symbol not found : " + symbol);
        }
        return name;
    }
}

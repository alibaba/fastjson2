package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONB.typeName;
import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class JSONReaderJSONBUF
        extends JSONReaderJSONB{
    static final long BASE = UNSAFE.arrayBaseOffset(byte[].class);

    JSONReaderJSONBUF(Context ctx, byte[] bytes, int off, int length) {
        super(ctx, bytes, off, length);
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
        if (strtype >= BC_STR_ASCII_FIX_MIN && strtype <= BC_STR_ASCII) {
            long nameValue0 = -1, nameValue1 = -1;

            if (strtype == BC_STR_ASCII) {
                strlen = readLength();
                strBegin = offset;
            } else {
                strlen = strtype - BC_STR_ASCII_FIX_MIN;

                if (offset + strlen >= bytes.length) {
                    throw new JSONException("illegal jsonb data");
                }

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

            if (nameValue0 != -1) {
                if (nameValue1 != -1) {
                    int indexMask = ((int) nameValue1) & (NAME_CACHE2.length - 1);
                    JSONFactory.NameCacheEntry2 entry = NAME_CACHE2[indexMask];
                    if (entry == null) {
                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) bytes[offset + i];
                            }
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
                        }

                        NAME_CACHE2[indexMask] = new JSONFactory.NameCacheEntry2(name, nameValue0, nameValue1);
                        offset += strlen;
                        str = name;
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                        offset += strlen;
                        str = entry.name;
                    }
                } else {
                    int indexMask = ((int) nameValue0) & (NAME_CACHE.length - 1);
                    JSONFactory.NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            char[] chars = new char[strlen];
                            for (int i = 0; i < strlen; ++i) {
                                chars[i] = (char) bytes[offset + i];
                            }
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(bytes, offset, strlen, StandardCharsets.US_ASCII);
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
                charset = StandardCharsets.US_ASCII;
            }
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
            } else {
                offset++;
            }
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
            if (MIXED_HASH_ALGORITHM && strlen <= 8 && offset + strlen < bytes.length) {
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
}

package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE;
import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE2;
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
                                = (((long) bytes[offset] + 5) << 40)
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
                        if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                            try {
                                STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                            } catch (Throwable e) {
                                STRING_CREATOR_ERROR = true;
                            }
                        }
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
                        return name;
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                        offset += strlen;
                        return entry.name;
                    }
                } else {
                    int indexMask = ((int) nameValue0) & (NAME_CACHE.length - 1);
                    JSONFactory.NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
                        if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                            try {
                                STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                            } catch (Throwable e) {
                                STRING_CREATOR_ERROR = true;
                            }
                        }
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
                        return name;
                    } else if (entry.value == nameValue0) {
                        offset += strlen;
                        return entry.name;
                    }
                }
            }

            if (JDKUtils.JVM_VERSION == 8 && strlen >= 0) {
                char[] chars = new char[strlen];
                for (int i = 0; i < strlen; ++i) {
                    chars[i] = (char) bytes[offset + i];
                }
                offset += strlen;

                if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                    try {
                        STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                    } catch (Throwable e) {
                        STRING_CREATOR_ERROR = true;
                    }
                }

                if (STRING_CREATOR_JDK8 == null) {
                    str = new String(chars);
                } else {
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                }
            } else if (JDKUtils.JVM_VERSION == 11 && strlen >= 0) {
                if (STRING_CREATOR_JDK11 == null && !STRING_CREATOR_ERROR) {
                    try {
                        STRING_CREATOR_JDK11 = JDKUtils.getStringCreatorJDK11();
                    } catch (Throwable e) {
                        STRING_CREATOR_ERROR = true;
                    }
                }

                if (STRING_CREATOR_JDK11 != null) {
                    byte[] chars = new byte[strlen];
                    System.arraycopy(bytes, offset, chars, 0, strlen);
                    str = STRING_CREATOR_JDK11.apply(chars);
                    offset += strlen;
                }
            }
            charset = StandardCharsets.US_ASCII;
        } else if (strtype == BC_STR_UTF8) {
            strlen = readLength();
            strBegin = offset;

            if (JDKUtils.UNSAFE_UTF16_CREATOR != null && !JDKUtils.BIG_ENDIAN) {
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
                    str = (String) JDKUtils.UNSAFE_UTF16_CREATOR.apply(value);
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

            if (JDKUtils.UNSAFE_UTF16_CREATOR != null && !JDKUtils.BIG_ENDIAN) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);
                str = (String) JDKUtils.UNSAFE_UTF16_CREATOR.apply(chars);
                offset += strlen;
            }

            charset = StandardCharsets.UTF_16LE;
        } else if (strtype == BC_STR_UTF16BE) {
            strlen = readLength();
            strBegin = offset;

            if (JDKUtils.UNSAFE_UTF16_CREATOR != null && JDKUtils.BIG_ENDIAN) {
                byte[] chars = new byte[strlen];
                System.arraycopy(bytes, offset, chars, 0, strlen);
                str = (String) JDKUtils.UNSAFE_UTF16_CREATOR.apply(chars);
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

            int minCapacity = symbol * 2 + 2;
            if (symbols.length < minCapacity) {
                symbols = Arrays.copyOf(symbols, symbols.length + 16);
            }

            long strInfo = ((long) strBegin << 32) + ((long) strlen << 8) + strtype;
            symbols[symbol * 2 + 1] = strInfo;
        }

        return str;
    }
}

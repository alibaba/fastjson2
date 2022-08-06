package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE;
import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE2;
import static com.alibaba.fastjson2.JSONFactory.Utils.*;

final class JSONReaderASCII
        extends JSONReaderUTF8 {
    final String str;

    JSONReaderASCII(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, bytes, offset, length);
        this.str = str;
        nameAscii = true;
    }

    @Override
    public void next() {
        if (offset >= end) {
            ch = EOI;
            return;
        }

        ch = (char) (bytes[offset] & 0xFF);
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return;
            }
            ch = (char) (bytes[offset] & 0xFF);
        }
        offset++;
    }

    @Override
    public boolean nextIfMatch(char ch) {
        if (this.ch != ch) {
            return false;
        }
        if (ch == ',') {
            this.comma = true;
        }

        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        this.ch = (char) (bytes[offset] & 0xFF);
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = (char) (bytes[offset] & 0xFF);
        }
        offset++;
        return true;
    }

    @Override
    public boolean nextIfEmptyString() {
        final char first = this.ch;
        if ((first != '"' && first != '\'') || offset >= end || bytes[offset] != first) {
            return false;
        }

        offset++;
        this.ch = offset == end ? EOI : (char) bytes[offset];

        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = (char) bytes[offset];
        }

        if (ch == ',') {
            this.comma = true;
            ch = (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }

        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        this.ch = (char) bytes[offset];
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = (char) bytes[offset];
        }
        offset++;
        return true;
    }

    @Override
    public long readFieldNameHashCode() {
        if (ch != '"' && ch != '\'') {
            if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0) {
                return readFieldNameHashCodeUnquote();
            }
            if (ch == '}' || isNull()) {
                return -1;
            }
            throw new JSONException("illegal character " + ch);
        }

        final char quote = ch;

        if (this.nameEscape) {
            this.nameEscape = false;
        }

        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; ; ++i) {
            char c = (char) bytes[offset];
            if (c == '\\') {
                if (!this.nameEscape) {
                    nameEscape = true;
                }
                c = (char) bytes[++offset];
                switch (c) {
                    case 'u': {
                        byte c1 = bytes[++offset];
                        byte c2 = bytes[++offset];
                        byte c3 = bytes[++offset];
                        byte c4 = bytes[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        byte c1 = bytes[++offset];
                        byte c2 = bytes[++offset];
                        c = char2(c1, c2);
                        break;
                    }
                    case '\\':
                    case '"':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
                offset++;
                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
                continue;
            }

            if (c == quote) {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                c = (char) bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = (char) bytes[offset];
                }
                if (c != ':') {
                    return -1;
                }

                offset++;
                c = (char) bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = (char) bytes[offset];
                }

                this.offset = offset + 1;
                this.ch = c;
                break;
            }

            offset++;
            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        if (ch != '"') {
            return -1;
        }

        if (this.nameEscape) {
            this.nameEscape = false;
        }
        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; ; ++i) {
            char c = (char) (bytes[offset] & 0xff);
            if (c == '\\') {
                if (!this.nameEscape) {
                    nameEscape = true;
                }
                c = (char) bytes[++offset];
                switch (c) {
                    case 'u': {
                        byte c1 = bytes[++offset];
                        byte c2 = bytes[++offset];
                        byte c3 = bytes[++offset];
                        byte c4 = bytes[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        byte c1 = bytes[++offset];
                        byte c2 = bytes[++offset];
                        c = char2(c1, c2);
                        break;
                    }
                    case '\\':
                    case '"':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
                offset++;
                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
                continue;
            }

            if (c == '"') {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                if (offset == end) {
                    c = EOI;
                } else {
                    c = (char) bytes[offset];
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = (char) bytes[offset];
                }

                if (c == ',') {
                    this.comma = true;
                    offset++;
                    if (offset == end) {
                        c = EOI;
                    } else {
                        c = (char) bytes[offset];
                    }

                    while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                        offset++;
                        c = (char) bytes[offset];
                    }
                }

                this.offset = offset + 1;
                this.ch = c;
                break;
            }

            offset++;
            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        long hashCode = Fnv.MAGIC_HASH_CODE;
        int offset = nameBegin;
        if (!nameEscape) {
            for (int i = nameBegin; i < nameEnd; ++i) {
                char c = (char) bytes[i];
                if (c >= 'A' && c <= 'Z') {
                    c = (char) (c + 32);
                }

                if (c == '_' || c == '-') {
                    byte c1 = bytes[i + 1];
                    if (c1 != '"' && c1 != '\'' && c1 != c) {
                        continue;
                    }
                }

                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
            return hashCode;
        }

        for (; ; ) {
            char c = (char) (bytes[offset] & 0xff);

            if (c == '\\') {
                c = (char) bytes[++offset];
                switch (c) {
                    case 'u': {
                        int c1 = bytes[++offset];
                        int c2 = bytes[++offset];
                        int c3 = bytes[++offset];
                        int c4 = bytes[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = bytes[++offset];
                        int c2 = bytes[++offset];
                        c = char2(c1, c2);
                        break;
                    }
                    case '\\':
                    case '"':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            } else {
                if (c >= 'A' && c <= 'Z') {
                    c = (char) (c + 32);
                }
            }

            offset++;
            if (c == '_') {
                continue;
            }
            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public String getFieldName() {
        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            if (this.str != null) {
                return this.str.substring(nameBegin, nameEnd);
            } else {
                return new String(bytes, nameBegin, length, StandardCharsets.US_ASCII);
            }
        }

        if (JDKUtils.JVM_VERSION > 8) {
            byte[] chars = new byte[nameLength];

            int offset = nameBegin;
            forStmt:
            for (int i = 0; offset < nameEnd; ++i) {
                byte b = bytes[offset];

                if (b == '\\') {
                    b = bytes[++offset];
                    switch (b) {
                        case 'u': {
                            int c1 = bytes[++offset];
                            int c2 = bytes[++offset];
                            int c3 = bytes[++offset];
                            int c4 = bytes[++offset];
                            char c = char4(c1, c2, c3, c4);
                            if (c > 0xFF) {
                                chars = null;
                                break forStmt;
                            }
                            b = (byte) c;
                            break;
                        }
                        case 'x': {
                            int c1 = bytes[++offset];
                            int c2 = bytes[++offset];
                            char c = char2(c1, c2);
                            if (c > 0xFF) {
                                chars = null;
                                break forStmt;
                            }
                            b = (byte) c;
                            break;
                        }
                        case '\\':
                        case '"':
                        case '.':
                        case '-':
                        case '+':
                        case '*':
                        case '/':
                        case '>':
                        case '<':
                        case '=':
                        case '@':
                        case ':':
                            break;
                        default:
                            b = (byte) char1(b);
                            break;
                    }
                } else if (b == '"') {
                    break;
                }
                chars[i] = b;
                offset++;
            }

            if (chars != null) {
                if (JDKUtils.UNSAFE_ASCII_CREATOR != null) {
                    return JDKUtils.UNSAFE_ASCII_CREATOR.apply(chars);
                }

                return new String(chars, 0, chars.length, StandardCharsets.US_ASCII);
            }
        }

        char[] chars = new char[nameLength];

        int offset = nameBegin;
        for (int i = 0; offset < nameEnd; ++i) {
            char c = (char) bytes[offset];

            if (c == '\\') {
                c = (char) bytes[++offset];
                switch (c) {
                    case 'u': {
                        int c1 = bytes[++offset];
                        int c2 = bytes[++offset];
                        int c3 = bytes[++offset];
                        int c4 = bytes[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = bytes[++offset];
                        int c2 = bytes[++offset];
                        c = char2(c1, c2);
                        break;
                    }
                    case '.':
                    case '-':
                    case '+':
                    case '*':
                    case '/':
                    case '>':
                    case '<':
                    case '=':
                    case '@':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }
            chars[i] = c;
            offset++;
        }

        return new String(chars);
    }

    @Override
    public String readFieldName() {
        if (ch != '"' && ch != '\'') {
            return null;
        }

        final char quote = ch;

        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                nameEscape = true;
                c = bytes[++offset];
                switch (c) {
                    case 'u': {
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        offset += 2;
                        break;
                    }
                    default:
                        // skip
                        break;
                }
                offset++;
                continue;
            }

            if (c == quote) {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                c = bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset];
                }
                if (c != ':') {
                    throw new JSONException("syntax error : " + offset);
                }

                offset++;
                c = bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset];
                }

                this.offset = offset + 1;
                this.ch = (char) c;
                break;
            }

            offset++;
        }

        if (!nameEscape) {
            long nameValue0 = -1, nameValue1 = -1;
            int length = nameEnd - nameBegin;
            switch (length) {
                case 1:
                    nameValue0 = bytes[nameBegin];
                    break;
                case 2:
                    nameValue0
                            = (bytes[nameBegin] << 8)
                            + (bytes[nameBegin + 1]);
                    break;
                case 3:
                    nameValue0
                            = (bytes[nameBegin] << 16)
                            + (bytes[nameBegin + 1] << 8)
                            + (bytes[nameBegin + 2]);
                    break;
                case 4:
                    nameValue0
                            = (bytes[nameBegin] << 24)
                            + (bytes[nameBegin + 1] << 16)
                            + (bytes[nameBegin + 2] << 8)
                            + (bytes[nameBegin + 3]);
                    break;
                case 5:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 32)
                            + (((long) bytes[nameBegin + 1]) << 24)
                            + (((long) bytes[nameBegin + 2]) << 16)
                            + (((long) bytes[nameBegin + 3]) << 8)
                            + ((long) bytes[nameBegin + 4]);
                    break;
                case 6:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 40)
                            + (((long) bytes[nameBegin + 1]) << 32)
                            + (((long) bytes[nameBegin + 2]) << 24)
                            + (((long) bytes[nameBegin + 3]) << 16)
                            + (((long) bytes[nameBegin + 4]) << 8)
                            + ((long) bytes[nameBegin + 5]);
                    break;
                case 7:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 48)
                            + (((long) bytes[nameBegin + 1]) << 40)
                            + (((long) bytes[nameBegin + 2]) << 32)
                            + (((long) bytes[nameBegin + 3]) << 24)
                            + (((long) bytes[nameBegin + 4]) << 16)
                            + (((long) bytes[nameBegin + 5]) << 8)
                            + ((long) bytes[nameBegin + 6]);
                    break;
                case 8:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 56)
                            + (((long) bytes[nameBegin + 1]) << 48)
                            + (((long) bytes[nameBegin + 2]) << 40)
                            + (((long) bytes[nameBegin + 3]) << 32)
                            + (((long) bytes[nameBegin + 4]) << 24)
                            + (((long) bytes[nameBegin + 5]) << 16)
                            + (((long) bytes[nameBegin + 6]) << 8)
                            + ((long) bytes[nameBegin + 7]);
                    break;
                case 9:
                    nameValue0 = bytes[nameBegin + 0];
                    nameValue1
                            = (((long) bytes[nameBegin] + 1) << 56)
                            + (((long) bytes[nameBegin + 2]) << 48)
                            + (((long) bytes[nameBegin + 3]) << 40)
                            + (((long) bytes[nameBegin + 4]) << 32)
                            + (((long) bytes[nameBegin + 5]) << 24)
                            + (((long) bytes[nameBegin + 6]) << 16)
                            + (((long) bytes[nameBegin + 7]) << 8)
                            + ((long) bytes[nameBegin + 8]);
                    break;
                case 10:
                    nameValue0
                            = (bytes[nameBegin] << 8)
                            + (bytes[nameBegin + 1]);
                    nameValue1
                            = (((long) bytes[nameBegin + 2]) << 56)
                            + (((long) bytes[nameBegin + 3]) << 48)
                            + (((long) bytes[nameBegin + 4]) << 40)
                            + (((long) bytes[nameBegin + 5]) << 32)
                            + (((long) bytes[nameBegin + 6]) << 24)
                            + (((long) bytes[nameBegin + 7]) << 16)
                            + (((long) bytes[nameBegin + 8]) << 8)
                            + ((long) bytes[nameBegin + 9]);
                    break;
                case 11:
                    nameValue0
                            = (bytes[nameBegin] << 16)
                            + (bytes[nameBegin + 1] << 8)
                            + (bytes[nameBegin + 2]);
                    nameValue1
                            = (((long) bytes[nameBegin + 3]) << 56)
                            + (((long) bytes[nameBegin + 4]) << 48)
                            + (((long) bytes[nameBegin + 5]) << 40)
                            + (((long) bytes[nameBegin + 6]) << 32)
                            + (((long) bytes[nameBegin + 7]) << 24)
                            + (((long) bytes[nameBegin + 8]) << 16)
                            + (((long) bytes[nameBegin + 9]) << 8)
                            + ((long) bytes[nameBegin + 10]);
                    break;
                case 12:
                    nameValue0
                            = (bytes[nameBegin] << 24)
                            + (bytes[nameBegin + 1] << 16)
                            + (bytes[nameBegin + 2] << 8)
                            + (bytes[nameBegin + 3]);
                    nameValue1
                            = (((long) bytes[nameBegin + 4]) << 56)
                            + (((long) bytes[nameBegin + 5]) << 48)
                            + (((long) bytes[nameBegin + 6]) << 40)
                            + (((long) bytes[nameBegin + 7]) << 32)
                            + (((long) bytes[nameBegin + 8]) << 24)
                            + (((long) bytes[nameBegin + 9]) << 16)
                            + (((long) bytes[nameBegin + 10]) << 8)
                            + ((long) bytes[nameBegin + 11]);
                    break;
                case 13:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 32)
                            + (((long) bytes[nameBegin + 1]) << 24)
                            + (((long) bytes[nameBegin + 2]) << 16)
                            + (((long) bytes[nameBegin + 3]) << 8)
                            + ((long) bytes[nameBegin + 4]);
                    nameValue1
                            = (((long) bytes[nameBegin + 5]) << 56)
                            + (((long) bytes[nameBegin + 6]) << 48)
                            + (((long) bytes[nameBegin + 7]) << 40)
                            + (((long) bytes[nameBegin + 8]) << 32)
                            + (((long) bytes[nameBegin + 9]) << 24)
                            + (((long) bytes[nameBegin + 10]) << 16)
                            + (((long) bytes[nameBegin + 11]) << 8)
                            + ((long) bytes[nameBegin + 12]);
                    break;
                case 14:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 40)
                            + (((long) bytes[nameBegin + 1]) << 32)
                            + (((long) bytes[nameBegin + 2]) << 24)
                            + (((long) bytes[nameBegin + 3]) << 16)
                            + (((long) bytes[nameBegin + 4]) << 8)
                            + ((long) bytes[nameBegin + 5]);
                    nameValue1
                            = (((long) bytes[nameBegin + 6]) << 56)
                            + (((long) bytes[nameBegin + 7]) << 48)
                            + (((long) bytes[nameBegin + 8]) << 40)
                            + (((long) bytes[nameBegin + 9]) << 32)
                            + (((long) bytes[nameBegin + 10]) << 24)
                            + (((long) bytes[nameBegin + 11]) << 16)
                            + (((long) bytes[nameBegin + 12]) << 8)
                            + ((long) bytes[nameBegin + 13]);
                    break;
                case 15:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 48)
                            + (((long) bytes[nameBegin + 1]) << 40)
                            + (((long) bytes[nameBegin + 2]) << 32)
                            + (((long) bytes[nameBegin + 3]) << 24)
                            + (((long) bytes[nameBegin + 4]) << 16)
                            + (((long) bytes[nameBegin + 5]) << 8)
                            + ((long) bytes[nameBegin + 6]);
                    nameValue1
                            = (((long) bytes[nameBegin + 7]) << 56)
                            + (((long) bytes[nameBegin + 8]) << 48)
                            + (((long) bytes[nameBegin + 9]) << 40)
                            + (((long) bytes[nameBegin + 10]) << 32)
                            + (((long) bytes[nameBegin + 11]) << 24)
                            + (((long) bytes[nameBegin + 12]) << 16)
                            + (((long) bytes[nameBegin + 13]) << 8)
                            + ((long) bytes[nameBegin + 14]);
                    break;
                case 16:
                    nameValue0
                            = (((long) bytes[nameBegin]) << 56)
                            + (((long) bytes[nameBegin + 1]) << 48)
                            + (((long) bytes[nameBegin + 2]) << 40)
                            + (((long) bytes[nameBegin + 3]) << 32)
                            + (((long) bytes[nameBegin + 4]) << 24)
                            + (((long) bytes[nameBegin + 5]) << 16)
                            + (((long) bytes[nameBegin + 6]) << 8)
                            + ((long) bytes[nameBegin + 7]);
                    nameValue1
                            = (((long) bytes[nameBegin + 8]) << 56)
                            + (((long) bytes[nameBegin + 9]) << 48)
                            + (((long) bytes[nameBegin + 10]) << 40)
                            + (((long) bytes[nameBegin + 11]) << 32)
                            + (((long) bytes[nameBegin + 12]) << 24)
                            + (((long) bytes[nameBegin + 13]) << 16)
                            + (((long) bytes[nameBegin + 14]) << 8)
                            + ((long) bytes[nameBegin + 15]);
                    break;
                default:
                    break;
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

                        char[] chars = new char[length];
                        for (int i = 0; i < length; ++i) {
                            chars[i] = (char) (bytes[nameBegin + i] & 0xFF);
                        }

                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(chars);
                        }

                        NAME_CACHE2[indexMask] = new JSONFactory.NameCacheEntry2(name, nameValue0, nameValue1);
                        return name;
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
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

                        char[] chars = new char[length];
                        for (int i = 0; i < length; ++i) {
                            chars[i] = (char) (bytes[nameBegin + i] & 0xFF);
                        }

                        String name;
                        if (STRING_CREATOR_JDK8 != null) {
                            name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                        } else {
                            name = new String(chars);
                        }

                        NAME_CACHE[indexMask] = new JSONFactory.NameCacheEntry(name, nameValue0);
                        return name;
                    } else if (entry.value == nameValue0) {
                        return entry.name;
                    }
                }
            }
        }

        return getFieldName();
    }

    @Override
    protected void readString0() {
        char quote = this.ch;
        int start = offset;
        int valueLength;
        valueEscape = false;

        _for:
        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                valueEscape = true;
                c = bytes[++offset];
                switch (c) {
                    case 'u': {
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        offset += 2;
                        break;
                    }
                    default:
                        break;
                }
                offset++;
                continue;
            }

            if (c == quote) {
                valueLength = i;
                break _for;
            }
            offset++;
        }

        String str;
        if (valueEscape) {
            char[] chars = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                char c = (char) bytes[offset];
                if (c == '\\') {
                    c = (char) (bytes[++offset]);
                    switch (c) {
                        case 'u': {
                            byte c1 = bytes[++offset];
                            byte c2 = bytes[++offset];
                            byte c3 = bytes[++offset];
                            byte c4 = bytes[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            byte c1 = bytes[++offset];
                            byte c2 = bytes[++offset];
                            c = char2(c1, c2);
                            break;
                        }
                        case '\\':
                        case '"':
                            break;
                        default:
                            c = char1(c);
                            break;
                    }
                } else if (c == '"') {
                    break;
                }
                chars[i] = c;
                offset++;
            }

            str = new String(chars);
        } else {
            if (JDKUtils.JVM_VERSION >= 9) {
                if (STRING_CREATOR_JDK11 == null && !STRING_CREATOR_ERROR) {
                    try {
                        STRING_CREATOR_JDK11 = JDKUtils.getStringCreatorJDK11();
                    } catch (Throwable e) {
                        STRING_CREATOR_ERROR = true;
                    }
                }

                if (STRING_CREATOR_JDK11 == null) {
                    str = new String(bytes, start, this.offset - start, StandardCharsets.US_ASCII);
                } else {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, start, offset);
                    str = STRING_CREATOR_JDK11.apply(bytes);
                }
            } else {
                str = new String(bytes, start, this.offset - start, StandardCharsets.US_ASCII);
            }
        }

        int b = bytes[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = bytes[++offset];
        }

        if (b == ',') {
            this.comma = true;
            this.offset = offset + 1;
            next();
        } else {
            this.offset = offset + 1;
            this.ch = (char) b;
        }

        stringValue = str;
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            final char quote = ch;

            int offset = this.offset;
            int start = offset;
            int valueLength;
            boolean valueEscape = false;

            _for:
            {
                int i = 0;

                // vector optimize
                while (offset + 8 <= end) {
                    byte c0 = bytes[offset];
                    byte c1 = bytes[offset + 1];
                    byte c2 = bytes[offset + 2];
                    byte c3 = bytes[offset + 3];
                    byte c4 = bytes[offset + 4];
                    byte c5 = bytes[offset + 5];
                    byte c6 = bytes[offset + 6];
                    byte c7 = bytes[offset + 7];
                    if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\' || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\') {
                        break;
                    }
                    if (c0 == quote || c1 == quote || c2 == quote || c3 == quote || c4 == quote || c5 == quote || c6 == quote || c7 == quote) {
                        break;
                    }
                    offset += 8;
                    i += 8;
                }

                // vector optimize
                while (offset + 4 <= end) {
                    byte c0 = bytes[offset];
                    byte c1 = bytes[offset + 1];
                    byte c2 = bytes[offset + 2];
                    byte c3 = bytes[offset + 3];
                    if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\') {
                        break;
                    }
                    if (c0 == quote || c1 == quote || c2 == quote || c3 == quote) {
                        break;
                    }
                    offset += 4;
                    i += 4;
                }

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    byte c = bytes[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[++offset];
                        switch (c) {
                            case 'u': {
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                offset += 2;
                                break;
                            }
                            default:
                                // skip
                                break;
                        }
                        offset++;
                        continue;
                    }

                    if (c == quote) {
                        valueLength = i;
                        break _for;
                    }
                    offset++;
                }
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    char c = (char) bytes[offset];
                    if (c == '\\') {
                        c = (char) bytes[++offset];
                        switch (c) {
                            case 'u': {
                                char c1 = (char) this.bytes[++offset];
                                char c2 = (char) this.bytes[++offset];
                                char c3 = (char) this.bytes[++offset];
                                char c4 = (char) this.bytes[++offset];
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                char c1 = (char) this.bytes[++offset];
                                char c2 = (char) this.bytes[++offset];
                                c = char2(c1, c2);
                                break;
                            }
                            case '\\':
                            case '"':
                                break;
                            default:
                                c = char1(c);
                                break;
                        }
                    } else if (c == quote) {
                        break;
                    }
                    chars[i] = c;
                    offset++;
                }

                str = new String(chars);
            } else {
                if (this.str != null) {
                    str = this.str.substring(this.offset, offset);
                } else if (JDKUtils.JVM_VERSION == 11 && !STRING_CREATOR_ERROR) {
                    if (STRING_CREATOR_JDK11 == null) {
                        try {
                            STRING_CREATOR_JDK11 = JDKUtils.getStringCreatorJDK11();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }

                    if (STRING_CREATOR_JDK11 == null) {
                        str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.US_ASCII);
                    } else {
                        byte[] bytes = Arrays.copyOfRange(this.bytes, this.offset, offset);
                        str = STRING_CREATOR_JDK11.apply(bytes);
                    }
                } else if (JDKUtils.JVM_VERSION > 8 && JDKUtils.UNSAFE_ASCII_CREATOR != null) {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, this.offset, offset);
                    str = JDKUtils.UNSAFE_ASCII_CREATOR.apply(bytes);
                } else {
                    str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.US_ASCII);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            if (offset + 1 == end) {
                this.offset = end;
                this.ch = EOI;
                return str;
            }

            byte b = bytes[++offset];
            while (b <= ' ' && ((1L << b) & SPACE) != 0) {
                b = bytes[++offset];
            }

            if (b == ',') {
                this.comma = true;
                this.offset = offset + 1;

                // inline next
                ch = (char) bytes[this.offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (this.offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[this.offset++];
                    }
                }
            } else {
                this.offset = offset + 1;
                this.ch = (char) b;
            }

            return str;
        }

        switch (ch) {
            case '[':
                return toString(
                        readArray());
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
                throw new JSONException("TODO : " + ch);
        }
    }
}

package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONFactory.UUIDUtils.*;
import static com.alibaba.fastjson2.JSONFactory.Utils.*;

class JSONReaderUTF8 extends JSONReader {
    protected final byte[] bytes;
    protected final int length;
    protected final int end;

    protected int nameBegin;
    protected int nameEnd;
    protected int nameLength;

    protected boolean nameAscii = false;

    JSONReaderUTF8(Context ctx, byte[] bytes, int offset, int length) {
        super(ctx);

        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.end = offset + length;
        next();
    }

    @Override
    public boolean nextIfMatch(char e) {
        if (this.ch != e) {
            return false;
        }

        if (offset >= end) {
            ch = EOI;
            return true;
        }

        int c = bytes[offset];
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return true;
            }
            c = bytes[offset];
        }

        if (c >= 0) {
            offset++;
            ch = (char) c;
            return true;
        }

        c &= 0xFF;
        switch (c >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                offset += 2;
                int char2 = bytes[offset - 1];
                if ((char2 & 0xC0) != 0x80) {
                    throw new JSONException(
                            "malformed input around byte " + offset);
                }
                ch = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            }
            case 14: {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                offset += 3;
                int char2 = bytes[offset - 2];
                int char3 = bytes[offset - 1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                    throw new JSONException("malformed input around byte " + (offset - 1));
                }
                ch = (char)
                        (((c & 0x0F) << 12) |
                                ((char2 & 0x3F) << 6) |
                                ((char3 & 0x3F) << 0));
                break;
            }
            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new JSONException("malformed input around byte " + offset);
        }
        return true;
    }

    @Override
    public void next() {
        if (offset >= end) {
            ch = EOI;
            return;
        }

        int c = bytes[offset];
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return;
            }
            c = bytes[offset];
        }

        if (c >= 0) {
            offset++;
            ch = (char) c;
            return;
        }

        c &= 0xFF;
        switch (c >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                offset += 2;
                int char2 = bytes[offset - 1];
                if ((char2 & 0xC0) != 0x80) {
                    throw new JSONException(
                            "malformed input around byte " + offset);
                }
                ch = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            }
            case 14: {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                offset += 3;
                int char2 = bytes[offset - 2];
                int char3 = bytes[offset - 1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                    throw new JSONException("malformed input around byte " + (offset - 1));
                }
                ch = (char)
                        (((c & 0x0F) << 12) |
                                ((char2 & 0x3F) << 6) |
                                ((char3 & 0x3F) << 0));
                break;
            }
            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new JSONException("malformed input around byte " + offset);
        }
    }

    @Override
    public long readFieldNameHashCodeUnquote() {
        this.nameEscape = false;
        this.nameBegin = this.offset - 1;
        long hashCode = Fnv.MAGIC_HASH_CODE;

        _for:
        for (int i = 0; ; ++i) {
            if (ch == '\\') {
                nameEscape = true;
                ch = (char) bytes[offset++];
                switch (ch) {
                    case 'u': {
                        char c1 = (char) bytes[offset++];
                        char c2 = (char) bytes[offset++];
                        char c3 = (char) bytes[offset++];
                        char c4 = (char) bytes[offset++];
                        ch = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        char c1 = (char) bytes[offset++];
                        char c2 = (char) bytes[offset++];
                        ch = char2(c1, c2);
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
                    case ':':
                        break;
                    default:
                        ch = char1(ch);
                        break;
                }

                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
                next();
                continue;
            }

            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case '\f':
                case '\b':
                case '.':
                case '-':
                case '+':
                case '*':
                case '/':
                case '>':
                case '<':
                case '=':
                case '!':
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case ',':
                case ':':
                case EOI:
                    nameLength = i;
                    if (ch == EOI) {
                        this.nameEnd = offset;
                    } else {
                        this.nameEnd = offset - 1;
                    }
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        next();
                    }
                    break _for;
                default:
                    break;
            }

            hashCode ^= ch;
            hashCode *= Fnv.MAGIC_PRIME;

            ch = offset >= end
                    ? EOI
                    : (char) bytes[offset++];
        }

        return hashCode;
    }

    public long readFieldNameHashCodeUnquote1() {
        this.nameEscape = false;
        this.nameBegin = this.offset - 1;
        long hashCode = Fnv.MAGIC_HASH_CODE;

        _for:
        for (int i = 0; ; ++i) {
            if (ch == '\\') {
                nameEscape = true;
                ch = (char) bytes[offset++];
                switch (ch) {
                    case 'u': {
                        char c1 = (char) bytes[offset++];
                        char c2 = (char) bytes[offset++];
                        char c3 = (char) bytes[offset++];
                        char c4 = (char) bytes[offset++];
                        ch = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        char c1 = (char) bytes[offset++];
                        char c2 = (char) bytes[offset++];
                        ch = char2(c1, c2);
                        break;
                    }
                    case '.':
                        break;
                    default:
                        ch = char1(ch);
                        break;
                }

                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
                next();
                continue;
            }

            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case '\f':
                case '\b':
                case '.':
                case '-':
                case '+':
                case '*':
                case '/':
                case '>':
                case '<':
                case '=':
                case '!':
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case ',':
                case ':':
                case EOI:
                    nameLength = i;
                    if (ch == EOI) {
                        this.nameEnd = offset;
                    } else {
                        this.nameEnd = offset - 1;
                    }
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        next();
                    }
                    break _for;
                default:
                    break;
            }

            hashCode ^= ch;
            hashCode *= Fnv.MAGIC_PRIME;
            next();
        }

        return hashCode;
    }

    @Override
    public long readFieldNameHashCode() {
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        final char quote = ch;

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                nameEscape = true;
                c = bytes[++offset];
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
                c = bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset];
                }
                if (c != ':') {
                    return -1;
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
                                | (c2 & 0x3F)
                        );

                        offset += 2;
                        nameAscii = false;
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
                        nameAscii = false;
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

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        if (ch != '"') {
            return -1;
        }

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;

        for_:
        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                nameEscape = true;
                c = bytes[++offset];
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
                    c = bytes[offset];
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset];
                }

                if (c == ',') {
                    offset++;
                    c = bytes[offset];

                    while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                        offset++;
                        c = bytes[offset];
                    }
                }

                this.offset = offset + 1;
                this.ch = (char) c;
                break;
            }

            if (c >= 0) {
                offset++;
            } else {
                switch ((c & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        int c2 = bytes[offset + 1];
                        if ((c2 & 0xC0) != 0x80) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        c = (char) (((c & 0x1F) << 6)
                                | (c2 & 0x3F)
                        );

                        offset += 2;
                        nameAscii = false;
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
                        nameAscii = false;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            offset++;
                            int c2 = bytes[offset++];
                            int c3 = bytes[offset++];
                            int c4 = bytes[offset++];
                            int uc = ((c << 18) ^
                                    (c2 << 12) ^
                                    (c3 << 6) ^
                                    (c4 ^ (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));

                            if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                    ||
                                    // shortest form check
                                    !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                            ) {
                                throw new JSONException("malformed input around byte " + offset);
                            } else {
                                char x1 = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                char x2 = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);

                                hashCode ^= x1;
                                hashCode *= Fnv.MAGIC_PRIME;

                                hashCode ^= x2;
                                hashCode *= Fnv.MAGIC_PRIME;
                                i++;
                            }
                            continue for_;
                        }
                        throw new JSONException("malformed input around byte " + offset);
                }
            }

            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        long hashCode = Fnv.MAGIC_HASH_CODE;
        int offset = nameBegin;
        if (nameAscii && !nameEscape) {
            for (int i = 0; i < nameLength; ++i) {
                char c = (char) bytes[offset + i];
                if (c >= 'A' && c <= 'Z') {
                    c = (char) (c + 32);
                }

                if (c == '_') {
                    continue;
                }

                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
            return hashCode;
        }

        for (; ; ) {
            int c = bytes[offset];

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
                    default:
                        c = char1(c);
                        break;
                }
                offset++;
            } else if (c == '"') {
                break;
            } else {
                if (c >= 0) {
                    if (c >= 'A' && c <= 'Z') {
                        c = (char) (c + 32);
                    }
                    offset++;
                } else {
                    c &= 0xFF;
                    switch (c >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            int c2 = bytes[offset + 1];
                            c = (char) (((c & 0x1F) << 6)
                                    | (c2 & 0x3F)
                            );
                            offset += 2;
                            break;
                        }
                        case 14: {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            c = (char) (((c & 0x0F) << 12)
                                    | ((c2 & 0x3F) << 6)
                                    | ((c3 & 0x3F) << 0)
                            );

                            offset += 3;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }
            }

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
            if (nameAscii) {
                if (JDKUtils.JVM_VERSION == 8) {
                    if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                        try {
                            STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }
                    if (STRING_CREATOR_JDK8 != null) {
                        char[] chars = new char[length];
                        for (int i = 0; i < length; ++i) {
                            chars[i] = (char) bytes[nameBegin + i];
                        }
                        return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                    }
                } else if (JDKUtils.JVM_VERSION == 11) {
                    if (STRING_CREATOR_JDK11 == null && !STRING_CREATOR_ERROR) {
                        try {
                            STRING_CREATOR_JDK11 = JDKUtils.getStringCreatorJDK11();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }

                    if (STRING_CREATOR_JDK11 != null) {
                        byte[] bytes = Arrays.copyOfRange(this.bytes, nameBegin, nameEnd);
                        return STRING_CREATOR_JDK11.apply(bytes);
                    }
                }
            }

            return new String(bytes, nameBegin, length
                    , nameAscii ? StandardCharsets.US_ASCII : StandardCharsets.UTF_8
            );
        }

        char[] chars = new char[nameLength];

        int offset = nameBegin;
        for (int i = 0; offset < nameEnd; ++i) {
            int c = bytes[offset];
            if (c < 0) {
                c &= 0xFF;
                switch (c >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        int char2 = bytes[offset + 1];
                        if ((char2 & 0xC0) != 0x80) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        c = (((c & 0x1F) << 6) | (char2 & 0x3F));
                        offset += 2;
                        break;
                    }
                    case 14: {
                        int char2 = bytes[offset + 1];
                        int char3 = bytes[offset + 2];
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                            throw new JSONException("malformed input around byte " + (offset + 2));
                        }
                        c = (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new JSONException("malformed input around byte " + offset);
                }
                chars[i] = (char) c;
                continue;
            }

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
                    case ':':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }
            chars[i] = (char) c;
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

        this.nameAscii = true;
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
                        break;
                }
                offset++;
                continue;
            }

            if (c == quote) {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                c = bytes[offset] & 0xff;

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset] & 0xff;
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

            if (c >= 0) {
                offset++;
            } else {
                if (nameAscii) {
                    nameAscii = false;
                }
                c &= 0xFF;
                switch (c >> 4) {
                    case 12:
                    case 13:
                        /* 110x xxxx   10xx xxxx*/
                        offset += 2;
                        break;
                    case 14: {
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new JSONException("malformed input around byte " + offset);
                }
            }
        }

        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            if (nameAscii) {
                if (JDKUtils.JVM_VERSION == 8) {
                    if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                        try {
                            STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }
                    if (STRING_CREATOR_JDK8 != null) {
                        char[] chars = new char[length];
                        for (int i = 0; i < length; ++i) {
                            chars[i] = (char) bytes[nameBegin + i];
                        }
                        return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                    }
                } else if (JDKUtils.JVM_VERSION == 11) {
                    if (STRING_CREATOR_JDK11 == null && !STRING_CREATOR_ERROR) {
                        try {
                            STRING_CREATOR_JDK11 = JDKUtils.getStringCreatorJDK11();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }

                    if (STRING_CREATOR_JDK11 != null) {
                        byte[] bytes = Arrays.copyOfRange(this.bytes, nameBegin, nameEnd);
                        return STRING_CREATOR_JDK11.apply(bytes);
                    }
                }
            }

            return new String(bytes, nameBegin, length
                    , nameAscii ? StandardCharsets.US_ASCII : StandardCharsets.UTF_8
            );
        }

        return getFieldName();
    }

    @Override
    public final int readInt32Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        int intValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            if (!overflow) {
                int intValue10 = intValue * 10 + (ch - '0');
                if (intValue10 < intValue) {
                    overflow = true;
                    break;
                } else {
                    intValue = intValue10;
                }
            }
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset++];
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || ch == '{'
                || ch == '['
                || overflow) {
            notMatch = true;
        } else if (quote != 0 && ch != quote) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            if (valueType == JSON_TYPE_INT) {
                BigInteger bigInteger = getBigInteger();
                try {
                    return bigInteger.intValueExact();
                } catch (ArithmeticException ex) {
                    throw new JSONException("int overflow, value " + bigInteger.toString());
                }
            } else {
                return getInt32Value();
            }
        }

        if (quote != 0) {
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return negative ? -intValue : intValue;
    }

    @Override
    public Integer readInt32() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        int intValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            if (!overflow) {
                int intValue10 = intValue * 10 + (ch - '0');
                if (intValue10 < intValue) {
                    overflow = true;
                    break;
                } else {
                    intValue = intValue10;
                }
            }
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) bytes[offset++];
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || ch == '{'
                || ch == '['
                || overflow) {
            notMatch = true;
        } else if (quote != 0 && ch != quote) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            if (wasNull) {
                return null;
            }
            return getInt32Value();
        }

        if (quote != 0) {
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return negative ? -intValue : intValue;
    }

    @Override
    public final long readInt64Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        long longValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            if (!overflow) {
                long intValue10 = longValue * 10 + (ch - '0');
                if (intValue10 < longValue) {
                    overflow = true;
                    break;
                } else {
                    longValue = intValue10;
                }
            }
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset++];
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || ch == '{'
                || ch == '['
                || overflow) {
            notMatch = true;
        } else if (quote != 0 && ch != quote) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            if (valueType == JSON_TYPE_INT) {
                BigInteger bigInteger = getBigInteger();
                try {
                    return bigInteger.longValueExact();
                } catch (ArithmeticException ex) {
                    throw new JSONException("long overflow, value " + bigInteger.toString());
                }
            } else {
                return getInt64Value();
            }
        }

        if (quote != 0) {
            this.ch = (char) bytes[offset++];
        }

        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[offset++];
            }
        }

        if (this.ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        this.ch = EOI;
                    } else {
                        this.ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return negative ? -longValue : longValue;
    }

    @Override
    public Long readInt64() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        long longValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            if (!overflow) {
                long intValue10 = longValue * 10 + (ch - '0');
                if (intValue10 < longValue) {
                    overflow = true;
                    break;
                } else {
                    longValue = intValue10;
                }
            }

            if (offset == end) {
                ch = EOI;
                break;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        boolean notMatch = false;
        if (ch == '.'
                || ch == 'e'
                || ch == 'E'
                || ch == 't'
                || ch == 'f'
                || ch == 'n'
                || ch == '{'
                || ch == '['
                || overflow) {
            notMatch = true;
        } else if (quote != 0 && ch != quote) {
            notMatch = true;
        }

        if (notMatch) {
            this.offset = firstOffset;
            this.ch = firstChar;
            readNumber0();
            return getInt64();
        }

        if (quote != 0) {
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return negative ? -longValue : longValue;
    }

    @Override
    public void readString(ValueConsumer consumer, boolean quoted) {
        char quote = this.ch;
        int valueLength;
        int offset = this.offset;
        int start = offset;
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

            if (c >= 0) {
                if (c == quote) {
                    valueLength = i;
                    break _for;
                }
                offset++;
            } else {
                switch ((c & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        offset += 2;
                        break;
                    }
                    case 14: {
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            offset += 4;
                            i++;
                            break;
                        }
                        throw new JSONException("malformed input around byte " + offset);
                }
            }
        }

        if (valueEscape) {
            int bytesMaxiumLength = offset - this.offset;

            char[] chars = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                int c = bytes[offset];
                if (c == '\\') {
                    c = bytes[++offset];
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
                        default:
                            c = char1(c);
                            break;
                    }
                } else if (c == '"') {
                    break;
                }

                if (c >= 0) {
                    chars[i] = (char) c;
                    offset++;
                    continue;
                }

                switch ((c & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        offset++;
                        int c2 = bytes[offset++];
                        chars[i] = (char) (
                                ((c & 0x1F) << 6) | (c2 & 0x3F)
                        );
                        break;
                    }
                    case 14: {
                        offset++;
                        int c2 = bytes[offset++];
                        int c3 = bytes[offset++];
                        chars[i] = (char)
                                (((c & 0x0F) << 12) |
                                        ((c2 & 0x3F) << 6) |
                                        ((c3 & 0x3F) << 0));
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            offset++;
                            int c2 = bytes[offset++];
                            int c3 = bytes[offset++];
                            int c4 = bytes[offset++];
                            int uc = ((c << 18) ^
                                    (c2 << 12) ^
                                    (c3 << 6) ^
                                    (c4 ^ (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));

                            if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                    ||
                                    // shortest form check
                                    !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                            ) {
                                throw new JSONException("malformed input around byte " + offset);
                            } else {
                                chars[i++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                chars[i] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                            }

                            break;
                        }
                        throw new JSONException("malformed input around byte " + offset);
                }

            }

            byte[] bytes = new byte[bytesMaxiumLength];
            int bytesLength = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, 0);
            consumer.accept(bytes, 0, bytesLength);
        } else {
            int consumStart = quoted ? this.offset - 1 : this.offset;
            int consumLen = quoted ? offset - this.offset + 2 : offset - this.offset;
            if (quoted && quote == '\'') {
                byte[] quotedBytes = new byte[consumLen];
                System.arraycopy(bytes, this.offset - 1, quotedBytes, 0, consumLen);
                quotedBytes[0] = '"';
                quotedBytes[quotedBytes.length - 1] = '"';
                consumer.accept(quotedBytes, 0, quotedBytes.length);
            } else {
                consumer.accept(bytes, consumStart, consumLen);
            }
        }

        int b = bytes[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = bytes[++offset];
        }

        if (b == ',') {
            this.offset = offset + 1;
            next();
        } else {
            this.offset = offset + 1;
            this.ch = (char) b;
        }
    }

    protected void readString0() {
        char quote = this.ch;
        int valueLength;
        int offset = this.offset;
        int start = offset;
        boolean ascii = true;
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
                        c = char1(c);
                        break;
                }
                offset++;
                continue;
            }

            if (c >= 0) {
                if (c == quote) {
                    valueLength = i;
                    break _for;
                }
                offset++;
            } else {
                switch ((c & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        offset += 2;
                        ascii = false;
                        break;
                    }
                    case 14: {
                        offset += 3;
                        ascii = false;
                        break;
                    }
                    default: {
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            offset += 4;
                            i++;
                            ascii = false;
                            break;
                        }

                        throw new JSONException("malformed input around byte " + offset);
                    }
                }
            }
        }

        String str;
        if (valueEscape) {
            char[] chars = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                int c = bytes[offset];
                if (c == '\\') {
                    c = bytes[++offset];
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
                        default:
                            c = char1(c);
                            break;
                    }
                    chars[i] = (char) c;
                    offset++;
                } else if (c == '"') {
                    break;
                } else {
                    if (c >= 0) {
                        chars[i] = (char) c;
                        offset++;
                    } else {
                        switch ((c & 0xFF) >> 4) {
                            case 12:
                            case 13: {
                                /* 110x xxxx   10xx xxxx*/
                                offset++;
                                int c2 = bytes[offset++];
                                chars[i] = (char) (
                                        ((c & 0x1F) << 6) | (c2 & 0x3F)
                                );
                                break;
                            }
                            case 14: {
                                offset++;
                                int c2 = bytes[offset++];
                                int c3 = bytes[offset++];
                                chars[i] = (char)
                                        (((c & 0x0F) << 12) |
                                                ((c2 & 0x3F) << 6) |
                                                ((c3 & 0x3F) << 0));
                                break;
                            }
                            default: {
                                /* 10xx xxxx,  1111 xxxx */
                                if ((c >> 3) == -2) {
                                    offset++;
                                    int c2 = bytes[offset++];
                                    int c3 = bytes[offset++];
                                    int c4 = bytes[offset++];
                                    int uc = ((c << 18) ^
                                            (c2 << 12) ^
                                            (c3 << 6) ^
                                            (c4 ^ (((byte) 0xF0 << 18) ^
                                                    ((byte) 0x80 << 12) ^
                                                    ((byte) 0x80 << 6) ^
                                                    ((byte) 0x80 << 0))));

                                    if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                            ||
                                            // shortest form check
                                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                                    ) {
                                        throw new JSONException("malformed input around byte " + offset);
                                    } else {
                                        chars[i++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                        chars[i] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                                    }
                                    break;
                                }

                                throw new JSONException("malformed input around byte " + offset);
                            }
                        }
                    }
                }
            }

            str = new String(chars);
        } else if (ascii) {
            str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.US_ASCII);
        } else {
            str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);
        }

        int b = bytes[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = bytes[++offset];
        }

        if (b == ',') {
            this.offset = offset + 1;
            next();
        } else {
            this.offset = offset + 1;
            this.ch = (char) b;
        }

        stringValue = str;
    }

    @Override
    public boolean skipName() {
        if (ch != '"') {
            throw new JSONException("not support unquoted name");
        }

        int offset = this.offset;
        for (; ; ) {
            byte c = bytes[offset];
            if (c == '\\') {
                offset += 2;
                continue;
            }

            if (c == '"') {
                offset++;
                c = bytes[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = bytes[offset];
                }
                if (c != ':') {
                    throw new JSONException("syntax error, expect ',', but '" + c + "'");
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

        return true;
    }

    @Override
    public void skipValue() {
        switch (ch) {
            case '[': {
                next();
                for (int i = 0; ; ++i) {
                    if (ch == ']') {
                        next();
                        break;
                    }

                    if (i != 0 && !comma) {
                        throw new JSONValidException("offset " + this.offset);
                    }
                    comma = false;
                    skipValue();
                }
                break;
            }
            case '{': {
                next();
                for (; ; ) {
                    if (ch == '}') {
                        next();
                        break;
                    }
                    skipName();
                    skipValue();
                }
                break;
            }
            case '"': {
                skipString();
                break;
            }
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
            case 't':
            case 'f':
            case 'n':
                for (; offset < end; ) {
                    ch = (char) bytes[offset++];
                    if (ch == '}' || ch == ']') {
                        break;
                    }
                    if (ch == ',') {
                        if (offset >= end) {
                            ch = EOI;
                            return;
                        }

                        ch = (char) bytes[offset];
                        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                            offset++;
                            if (offset >= end) {
                                ch = EOI;
                                return;
                            }
                            ch = (char) bytes[offset];
                        }
                        offset++;
                        break;
                    }
                }
                break;
            default:
                throw new JSONException("TODO : " + ch);
        }

        if (ch == ',') {
            comma = true;

            if (offset >= end) {
                ch = EOI;
                return;
            }

            ch = (char) bytes[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    ch = EOI;
                    return;
                }
                ch = (char) bytes[offset];
            }
            offset++;
        }
    }

    @Override
    public String getString() {
        if (stringValue != null) {
            return stringValue;
        }

        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            return new String(bytes, nameBegin, length
                    , nameAscii ? StandardCharsets.US_ASCII : StandardCharsets.UTF_8
            );
        }

        char[] chars = new char[nameLength];

        int offset = nameBegin;
        for_:
        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c < 0) {
                switch ((c & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        int char2 = bytes[offset + 1];
                        if ((char2 & 0xC0) != 0x80) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        c = (((c & 0x1F) << 6) | (char2 & 0x3F));
                        offset += 2;
                        break;
                    }
                    case 14: {
                        int char2 = bytes[offset + 1];
                        int char3 = bytes[offset + 2];
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                            throw new JSONException("malformed input around byte " + (offset + 2));
                        }
                        c = (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            offset++;
                            int c2 = bytes[offset++];
                            int c3 = bytes[offset++];
                            int c4 = bytes[offset++];
                            int uc = ((c << 18) ^
                                    (c2 << 12) ^
                                    (c3 << 6) ^
                                    (c4 ^ (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));

                            if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                    ||
                                    // shortest form check
                                    !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                            ) {
                                throw new JSONException("malformed input around byte " + offset);
                            } else {
                                chars[i++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                chars[i] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                            }
                            continue for_;
                        } else {
                            c &= 0xFF;
                            offset++;
                            break;
                        }
                }
                chars[i] = (char) c;
                continue;
            }

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
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }
            chars[i] = (char) c;
            offset++;
        }

        return stringValue = new String(chars);
    }

    protected void skipString() {
        char quote = this.ch;

        while (offset + 4 < end && offset + 4 < bytes.length) {
            byte b0 = bytes[offset];
            byte b1 = bytes[offset + 1];
            byte b2 = bytes[offset + 2];
            byte b3 = bytes[offset + 3];

            if (b0 != '\\'
                    && b1 != '\\'
                    && b2 != '\\'
                    && b3 != '\\'
                    && b0 != '"'
                    && b1 != '"'
                    && b2 != '"'
                    && b3 != '"'
            ) {
                offset += 4;
                continue;
            }
            break;
        }

        byte ch = bytes[offset++];
        for (; ; ) {
            if (ch == '\\') {
                ch = bytes[offset++];
                char1(ch);
                ch = bytes[offset++];
                continue;
            }
            if (ch == quote) {
                ch = bytes[offset++];
                break;
            }

            ch = bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = bytes[offset++];
        }

        if (ch == ',') {
            comma = true;

            if (offset >= end) {
                this.ch = EOI;
                return;
            }

            ch = bytes[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    this.ch = EOI;
                    return;
                }
                ch = bytes[offset];
            }
            offset++;
        }
        this.ch = (char) ch;
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            char quote = this.ch;
            int valueLength;
            int offset = this.offset;
            int start = offset;
            boolean ascii = true;
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
                            c = char1(c);
                            break;
                    }
                    offset++;
                    continue;
                }

                if (c >= 0) {
                    if (c == quote) {
                        valueLength = i;
                        break _for;
                    }
                    offset++;
                } else {
                    switch ((c & 0xFF) >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            offset += 2;
                            ascii = false;
                            break;
                        }
                        case 14: {
                            offset += 3;
                            ascii = false;
                            break;
                        }
                        default: {
                            /* 10xx xxxx,  1111 xxxx */
                            if ((c >> 3) == -2) {
                                offset += 4;
                                i++;
                                ascii = false;
                                break;
                            }

                            throw new JSONException("malformed input around byte " + offset);
                        }
                    }
                }
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    int c = bytes[offset];
                    if (c == '\\') {
                        c = bytes[++offset];
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
                            default:
                                c = char1(c);
                                break;
                        }
                        chars[i] = (char) c;
                        offset++;
                    } else if (c == '"') {
                        break;
                    } else {
                        if (c >= 0) {
                            chars[i] = (char) c;
                            offset++;
                        } else {
                            switch ((c & 0xFF) >> 4) {
                                case 12:
                                case 13: {
                                    /* 110x xxxx   10xx xxxx*/
                                    offset++;
                                    int c2 = bytes[offset++];
                                    chars[i] = (char) (
                                            ((c & 0x1F) << 6) | (c2 & 0x3F)
                                    );
                                    break;
                                }
                                case 14: {
                                    offset++;
                                    int c2 = bytes[offset++];
                                    int c3 = bytes[offset++];
                                    chars[i] = (char)
                                            (((c & 0x0F) << 12) |
                                                    ((c2 & 0x3F) << 6) |
                                                    ((c3 & 0x3F) << 0));
                                    break;
                                }
                                default: {
                                    /* 10xx xxxx,  1111 xxxx */
                                    if ((c >> 3) == -2) {
                                        offset++;
                                        int c2 = bytes[offset++];
                                        int c3 = bytes[offset++];
                                        int c4 = bytes[offset++];
                                        int uc = ((c << 18) ^
                                                (c2 << 12) ^
                                                (c3 << 6) ^
                                                (c4 ^ (((byte) 0xF0 << 18) ^
                                                        ((byte) 0x80 << 12) ^
                                                        ((byte) 0x80 << 6) ^
                                                        ((byte) 0x80 << 0))));

                                        if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                                                ||
                                                // shortest form check
                                                !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                                        ) {
                                            throw new JSONException("malformed input around byte " + offset);
                                        } else {
                                            chars[i++] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                                            chars[i] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                                        }
                                        break;
                                    }

                                    throw new JSONException("malformed input around byte " + offset);
                                }
                            }
                        }
                    }
                }

                str = new String(chars);
            } else if (ascii) {
                if (JDKUtils.JVM_VERSION == 8) {
                    int strlen = offset - this.offset;
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) bytes[this.offset + i];
                    }

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
                } else if (JDKUtils.JVM_VERSION == 11) {
                    if (STRING_CREATOR_JDK11 == null && !STRING_CREATOR_ERROR) {
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
                } else {
                    str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.US_ASCII);
                }
            } else {
                str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);
            }

            if (offset + 1 == end) {
                this.offset = end;
                this.ch = EOI;
                return str;
            }

            int b = bytes[++offset];
            while (b <= ' ' && ((1L << b) & SPACE) != 0) {
                b = bytes[++offset];
            }

            if (b == ',') {
                this.offset = offset + 1;
                next();
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

    @Override
    public void readNumber0() {
        this.wasNull = false;
        this.mag0 = 0;
        this.mag1 = 0;
        this.mag2 = 0;
        this.mag3 = 0;
        this.negative = false;
        this.exponent = 0;
        this.scale = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }
        final int start = offset;

        final int limit, multmin;
        if (ch == '-') {
            limit = Integer.MIN_VALUE;
            multmin = -214748364; // limit / 10;
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            limit = -2147483647; // -Integer.MAX_VALUE;
            multmin = -214748364; // limit / 10;
        }

        // if (result < limit + digit) {
        boolean intOverflow = false;
        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            if (!intOverflow) {
                int digit = ch - '0';
                mag3 *= 10;
                if (mag3 < multmin
                        || mag3 < limit + digit) {
                    intOverflow = true;
                } else {
                    mag3 -= digit;
                    if (mag3 < multmin) {
                        intOverflow = true;
                    }
                }
            }
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) bytes[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                if (!intOverflow) {
                    int digit = ch - '0';
                    mag3 *= 10;
                    if (mag3 < multmin
                            || mag3 < limit + digit) {
                        intOverflow = true;
                    } else {
                        mag3 -= digit;
                        if (mag3 < multmin) {
                            intOverflow = true;
                        }
                    }
                }

                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;
            bigInt(bytes, numStart, offset - 1);
        } else {
            mag3 = -mag3;
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = (char) bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) bytes[offset++];
            } else if (ch == '+') {
                ch = (char) bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > 100) {
                    throw new JSONException("to large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    break;
                }
                ch = (char) bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (byte) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (bytes[offset++] == 'u'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 'l'
                ) {
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (bytes[offset++] == 'a'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 's'
                        && bytes[offset++] == 'e'
                ) {
                    boolValue = false;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == '{' && quote == 0) {
                this.complex = readObject();
                valueType = JSON_TYPE_OBJECT;
                return;
            } else if (ch == '[' && quote == 0) {
                this.complex = readArray();
                valueType = JSON_TYPE_ARRAY;
                return;
            }
        }

        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                readString0();
                valueType = JSON_TYPE_STRING;
                return;
            }
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }
    }

    @Override
    public void readNumber(ValueConsumer consumer, boolean quoted) {
        this.wasNull = false;
        this.boolValue = false;
        this.mag0 = 0;
        this.mag1 = 0;
        this.mag2 = 0;
        this.mag3 = 0;
        this.negative = false;
        this.exponent = 0;
        this.scale = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];
        }
        final int start = offset;

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        }

        boolean intOverflow = false;
        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            if (!intOverflow) {
                int mag3_10 = mag3 * 10 + (ch - '0');
                if (mag3_10 < mag3) {
                    intOverflow = true;
                } else {
                    mag3 = mag3_10;
                }
            }
            ch = (char) bytes[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                if (!intOverflow) {
                    int mag3_10 = mag3 * 10 + (ch - '0');
                    if (mag3_10 < mag3) {
                        intOverflow = true;
                    } else {
                        mag3 = mag3_10;
                    }
                }

                this.scale++;
                ch = (char) bytes[offset++];
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;
            bigInt(bytes, numStart, offset - 1);
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = (char) bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) bytes[offset++];
            } else if (ch == '+') {
                ch = (char) bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > 100) {
                    throw new JSONException("to large exp value : " + expValue);
                }
                ch = (char) bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (byte) expValue;
            valueType = JSON_TYPE_DEC;
        }

        int len = offset - start;

        if (offset == start) {
            if (ch == 'n') {
                if (bytes[offset++] == 'u'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 'l'
                ) {
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    ch = (char) bytes[offset++];
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    ch = (char) bytes[offset++];
                }
            } else if (ch == 'f') {
                if (bytes[offset++] == 'a'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 's'
                        && bytes[offset++] == 'e'
                ) {
                    boolValue = false;
                    valueType = JSON_TYPE_BOOL;
                    ch = (char) bytes[offset++];
                }
            } else if (ch == '{' && quote == 0) {
                this.complex = readObject();
                valueType = JSON_TYPE_OBJECT;
                return;
            } else if (ch == '[' && quote == 0) {
                this.complex = readArray();
                valueType = JSON_TYPE_ARRAY;
                return;
            }
        }

        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                readString0();
                valueType = JSON_TYPE_STRING;
                return;
            }
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (ch == ',') {
            this.ch = (char) bytes[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        if (!quoted && (valueType == JSON_TYPE_INT || valueType == JSON_TYPE_DEC)) {
            consumer.accept(bytes, start - 1, len);
            return;
        }

        if (valueType == JSON_TYPE_INT) {
            if (mag1 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                int intValue = negative ? -mag3 : mag3;
                consumer.accept(intValue);
                return;
            }

            if (mag1 == 0 && mag1 == 0) {
                long v3 = mag3 & LONG_MASK;
                long v2 = mag2 & LONG_MASK;

                if (v2 >= Integer.MIN_VALUE && v2 <= Integer.MAX_VALUE) {
                    long v23 = (v2 << 32) + (v3);
                    long longValue = negative ? -v23 : v23;
                    consumer.accept(longValue);
                    return;
                }
            }
        }

        Number number = getNumber();
        consumer.accept(number);
    }

    @Override
    public boolean readIfNull() {
        if (ch == 'n'
                && bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset + 3];
            }
            offset += 4;
        } else {
            return false;
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }
        if (ch == ',') {
            ch = (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }

        return true;
    }

    @Override
    public void readNull() {
        if (bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset + 3];
            }
            offset += 4;
        } else {
            throw new JSONException("json syntax error, not match null" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }
        if (ch == ',') {
            ch = (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }
    }

    @Override
    public int getStringLength() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }
        char quote = ch;

        int len = 0;
        for (int i = offset; i < end; ++i, ++len) {
            if (bytes[i] == quote) {
                break;
            }
        }
        return len;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }
        final char quote = ch;

        if (len < 19) {
            return null;
        }

        char c0 = (char) bytes[offset + 0];
        char c1 = (char) bytes[offset + 1];
        char c2 = (char) bytes[offset + 2];
        char c3 = (char) bytes[offset + 3];
        char c4 = (char) bytes[offset + 4];
        char c5 = (char) bytes[offset + 5];
        char c6 = (char) bytes[offset + 6];
        char c7 = (char) bytes[offset + 7];
        char c8 = (char) bytes[offset + 8];
        char c9 = (char) bytes[offset + 9];
        char c10 = (char) bytes[offset + 10];
        char c11 = (char) bytes[offset + 11];
        char c12 = (char) bytes[offset + 12];
        char c13 = (char) bytes[offset + 13];
        char c14 = (char) bytes[offset + 14];
        char c15 = (char) bytes[offset + 15];
        char c16 = (char) bytes[offset + 16];
        char c17 = (char) bytes[offset + 17];
        char c18 = (char) bytes[offset + 18];
        char c19 = len == 19 ? ' ' : (char) bytes[offset + 19];

        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0', c29 = '\0';
        switch (len) {
            case 19:
            case 20:
                c20 = '\0';
                break;
            case 21:
                c20 = (char) bytes[offset + 20];
                break;
            case 22:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                break;
            case 23:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                break;
            case 24:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                break;
            case 25:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                break;
            case 26:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                break;
            case 27:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                break;
            case 28:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                c27 = (char) bytes[offset + 27];
                break;
            case 29:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                c27 = (char) bytes[offset + 27];
                c28 = (char) bytes[offset + 28];
                break;
            default:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                c27 = (char) bytes[offset + 27];
                c28 = (char) bytes[offset + 28];
                c29 = (char) bytes[offset + 29];
                break;
        }

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8;
        int zoneIdBegin;
        boolean isTimeZone = false;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':'
                && (c19 == '[' || c19 == 'Z' || c19 == '+' || c19 == '-' || c19 == ' ')
        ) {
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
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 19;

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 21 || c21 == '[' || c21 == '+' || c21 == '-' || c21 == 'Z')) {
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

            S0 = c20;
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 21;
            isTimeZone = c21 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 22 || c22 == '[' || c22 == '+' || c22 == '-' || c22 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 22;
            isTimeZone = c22 == '|';
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 23 || c23 == '[' || c23 == '|' || c23 == '+' || c23 == '-' || c23 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 23;
            isTimeZone = c23 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 24 || c24 == '[' || c24 == '|' || c24 == '+' || c24 == '-' || c24 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 24;
            isTimeZone = c24 == '|';


        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 25 || c25 == '[' || c25 == '|' || c25 == '+' || c25 == '-' || c25 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 25;
            isTimeZone = c25 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 26 || c26 == '[' || c26 == '|' || c26 == '+' || c26 == '-' || c26 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 26;
            isTimeZone = c26 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 27 || c27 == '[' || c27 == '|' || c27 == '+' || c27 == '-' || c27 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 27;
            isTimeZone = c27 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 28 || c28 == '[' || c28 == '|' || c28 == '+' || c28 == '-' || c28 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = c27;
            S8 = '0';
            zoneIdBegin = 28;
            isTimeZone = c28 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && (len == 29 || c29 == '[' || c29 == '|' || c29 == '+' || c29 == '-' || c29 == 'Z')) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = c27;
            S8 = c28;
            zoneIdBegin = 29;
            isTimeZone = c29 == '|';

        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.'
                && len == 23) {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 23;
            isTimeZone = false;
        } else {
            return null;
        }

        char first = (char) bytes[this.offset + zoneIdBegin];

        LocalDateTime ldt = getLocalDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);

        ZoneId zoneId;
        if (isTimeZone) {
            String tzStr = new String(bytes, this.offset + zoneIdBegin, len - zoneIdBegin);
            TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            zoneId = timeZone.toZoneId();
        } else {
            if (first == 'Z') {
                zoneId = UTC;
            } else {
                String zoneIdStr;
                if (first == '+' || first == '-') {
                    zoneIdStr = new String(bytes, this.offset + zoneIdBegin, len - zoneIdBegin, StandardCharsets.US_ASCII);
                } else if (first == ' ') {
                    zoneIdStr = new String(bytes, this.offset + zoneIdBegin + 1, len - zoneIdBegin - 1, StandardCharsets.US_ASCII);
                } else { // '[
                    if (zoneIdBegin < len) {
                        zoneIdStr = new String(bytes, this.offset + zoneIdBegin + 1, len - zoneIdBegin - 2, StandardCharsets.US_ASCII);
                    } else {
                        zoneIdStr = null;
                    }
                }
                zoneId = getZoneId(ldt, zoneIdStr);
            }
        }

        ZonedDateTime zdt = ldt.atZone(zoneId);
        if (zdt == null) {
            return null;
        }

        offset += (len + 1);
        next();
        if (ch == ',') {
            next();
        }
        return zdt;
    }

    @Override
    public LocalDateTime readLocalDate8() {
        if (ch != '"') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = (char) bytes[offset + 0];
        char c1 = (char) bytes[offset + 1];
        char c2 = (char) bytes[offset + 2];
        char c3 = (char) bytes[offset + 3];
        char c4 = (char) bytes[offset + 4];
        char c5 = (char) bytes[offset + 5];
        char c6 = (char) bytes[offset + 6];
        char c7 = (char) bytes[offset + 7];


        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '-' && c6 == '-') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = '0';
            d1 = c7;
        } else {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c4;
            m1 = c5;

            d0 = c6;
            d1 = c7;
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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, 0, 0, 0);

        offset += 9;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    public LocalDateTime readLocalDate9() {
        if (ch != '"') {
            throw new JSONException("localDate only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];

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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, 0, 0, 0);

        offset += 10;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    public LocalDateTime readLocalDate10() {
        if (ch != '"') {
            throw new JSONException("localDate only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];

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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, 0, 0, 0);

        offset += 11;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime17() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];
        byte c11 = bytes[offset + 11];
        byte c12 = bytes[offset + 12];
        byte c13 = bytes[offset + 13];
        byte c14 = bytes[offset + 14];
        byte c15 = bytes[offset + 15];
        byte c16 = bytes[offset + 16];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c7 == '-' && c10 == 'T' && c13 == ':' && c16 == 'Z') {
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

            s0 = '0';
            s1 = '0';
        } else if (c4 == '-' && c6 == '-' && c8 == ' ' && c11 == ':' && c14 == ':') {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = '0';
            m1 = (char) c5;

            d0 = '0';
            d1 = (char) c7;

            h0 = (char) c9;
            h1 = (char) c10;

            i0 = (char) c12;
            i1 = (char) c13;

            s0 = (char) c15;
            s1 = (char) c16;
        } else if (c4 == -27 && c5 == -71 && c6 == -76 // 年
                && c9 == -26 && c10 == -100 && c11 == -120 // 月
                && c14 == -26 && c15 == -105 && c16 == -91 // 日
        ) {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = (char) c7;
            m1 = (char) c8;

            d0 = (char) c12;
            d1 = (char) c13;

            h0 = '0';
            h1 = '0';

            i0 = '0';
            i1 = '0';

            s0 = '0';
            s1 = '0';
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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second);

        offset += 18;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime8() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];

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
        next();
        if (ch == ',') {
            next();
        }

        return LocalTime.of(hour, minute, seccond);
    }

    @Override
    protected LocalTime readLocalTime11() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];

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
            m2 = '0';
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

        offset += 12;
        next();
        if (ch == ',') {
            next();
        }

        return LocalTime.of(hour, minute, seccond, millis);
    }


    @Override
    protected LocalTime readLocalTime10() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];

        byte h0, h1, i0, i1, s0, s1, m0, m1, m2;
        if (c2 == ':' && c5 == ':' && c8 == '.') {
            h0 = c0;
            h1 = c1;
            i0 = c3;
            i1 = c4;
            s0 = c6;
            s1 = c7;
            m0 = c9;
            m1 = '0';
            m2 = '0';
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

        offset += 11;
        next();
        if (ch == ',') {
            next();
        }

        return LocalTime.of(hour, minute, seccond, millis);
    }

    @Override
    protected LocalTime readLocalTime12() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];
        byte c11 = bytes[offset + 11];

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
        next();
        if (ch == ',') {
            next();
        }

        return LocalTime.of(hour, minute, seccond, millis);
    }

    @Override
    protected LocalTime readLocalTime18() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];
        byte c11 = bytes[offset + 11];
        byte c12 = bytes[offset + 12];
        byte c13 = bytes[offset + 13];
        byte c14 = bytes[offset + 14];
        byte c15 = bytes[offset + 15];
        byte c16 = bytes[offset + 16];
        byte c17 = bytes[offset + 17];

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
        next();
        if (ch == ',') {
            next();
        }

        return LocalTime.of(hour, minute, seccond, millis);
    }

    @Override
    protected LocalDateTime readLocalDateTime16() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];
        byte c11 = bytes[offset + 11];
        byte c12 = bytes[offset + 12];
        byte c13 = bytes[offset + 13];
        byte c14 = bytes[offset + 14];
        byte c15 = bytes[offset + 15];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1;
        if (c4 == '-' && c7 == '-' && (c10 == 'T' || c10 == ' ') && c13 == ':') {
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
        } else if (c4 == -27 && c5 == -71 && c6 == -76 // 年
                && c8 == -26 && c9 == -100 && c10 == -120 // 月
                && c13 == -26 && c14 == -105 && c15 == -91 // 日
        ) {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = '0';
            m1 = (char) c7;

            d0 = (char) c11;
            d1 = (char) c12;

            h0 = '0';
            h1 = '0';

            i0 = '0';
            i1 = '0';
        } else if (c4 == -27 && c5 == -71 && c6 == -76 // 年
                && c9 == -26 && c10 == -100 && c11 == -120 // 月
                && c13 == -26 && c14 == -105 && c15 == -91 // 日
        ) {
            y0 = (char) c0;
            y1 = (char) c1;
            y2 = (char) c2;
            y3 = (char) c3;

            m0 = (char) c7;
            m1 = (char) c8;

            d0 = '0';
            d1 = (char) c12;

            h0 = '0';
            h1 = '0';

            i0 = '0';
            i1 = '0';
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


        int second = 0;
//            if (c17 >= '0' && c17 <= '9'
//                    && c18 >= '0' && c18 <= '9'
//            ) {
//                second = (c17 - '0') * 10 + (c18 - '0');
//            } else {
//                return null;
//            }

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second);

        offset += 17;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime18() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        char c0 = (char) bytes[offset + 0];
        char c1 = (char) bytes[offset + 1];
        char c2 = (char) bytes[offset + 2];
        char c3 = (char) bytes[offset + 3];
        char c4 = (char) bytes[offset + 4];
        char c5 = (char) bytes[offset + 5];
        char c6 = (char) bytes[offset + 6];
        char c7 = (char) bytes[offset + 7];
        char c8 = (char) bytes[offset + 8];
        char c9 = (char) bytes[offset + 9];
        char c10 = (char) bytes[offset + 10];
        char c11 = (char) bytes[offset + 11];
        char c12 = (char) bytes[offset + 12];
        char c13 = (char) bytes[offset + 13];
        char c14 = (char) bytes[offset + 14];
        char c15 = (char) bytes[offset + 15];
        char c16 = (char) bytes[offset + 16];
        char c17 = (char) bytes[offset + 17];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c6 == '-' && c9 == ' ' && c12 == ':' && c15 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;

            h0 = c10;
            h1 = c11;

            i0 = c13;
            i1 = c14;

            s0 = c16;
            s1 = c17;
        } else if (c4 == '-' && c7 == '-' && c9 == ' ' && c12 == ':' && c15 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;

            h0 = c10;
            h1 = c11;

            i0 = c13;
            i1 = c14;

            s0 = c16;
            s1 = c17;
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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second);

        offset += 19;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        byte c0 = bytes[offset + 0];
        byte c1 = bytes[offset + 1];
        byte c2 = bytes[offset + 2];
        byte c3 = bytes[offset + 3];
        byte c4 = bytes[offset + 4];
        byte c5 = bytes[offset + 5];
        byte c6 = bytes[offset + 6];
        byte c7 = bytes[offset + 7];
        byte c8 = bytes[offset + 8];
        byte c9 = bytes[offset + 9];
        byte c10 = bytes[offset + 10];
        byte c11 = bytes[offset + 11];
        byte c12 = bytes[offset + 12];
        byte c13 = bytes[offset + 13];
        byte c14 = bytes[offset + 14];
        byte c15 = bytes[offset + 15];
        byte c16 = bytes[offset + 16];
        byte c17 = bytes[offset + 17];
        byte c18 = bytes[offset + 18];

        byte y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2;
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
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTimeX(int len) {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        if (len < 21 || len > 29) {
            throw new JSONException("illeal localdatetime string : " + readString());
        }

        char c0 = (char) bytes[offset + 0];
        char c1 = (char) bytes[offset + 1];
        char c2 = (char) bytes[offset + 2];
        char c3 = (char) bytes[offset + 3];
        char c4 = (char) bytes[offset + 4];
        char c5 = (char) bytes[offset + 5];
        char c6 = (char) bytes[offset + 6];
        char c7 = (char) bytes[offset + 7];
        char c8 = (char) bytes[offset + 8];
        char c9 = (char) bytes[offset + 9];
        char c10 = (char) bytes[offset + 10];
        char c11 = (char) bytes[offset + 11];
        char c12 = (char) bytes[offset + 12];
        char c13 = (char) bytes[offset + 13];
        char c14 = (char) bytes[offset + 14];
        char c15 = (char) bytes[offset + 15];
        char c16 = (char) bytes[offset + 16];
        char c17 = (char) bytes[offset + 17];
        char c18 = (char) bytes[offset + 18];
        char c19 = (char) bytes[offset + 19];
        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0';
        switch (len) {
            case 21:
                c20 = (char) bytes[offset + 20];
                break;
            case 22:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                break;
            case 23:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                break;
            case 24:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                break;
            case 25:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                break;
            case 26:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                break;
            case 27:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                break;
            case 28:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                c27 = (char) bytes[offset + 27];
                break;
            default:
                c20 = (char) bytes[offset + 20];
                c21 = (char) bytes[offset + 21];
                c22 = (char) bytes[offset + 22];
                c23 = (char) bytes[offset + 23];
                c24 = (char) bytes[offset + 24];
                c25 = (char) bytes[offset + 25];
                c26 = (char) bytes[offset + 26];
                c27 = (char) bytes[offset + 27];
                c28 = (char) bytes[offset + 28];
                break;
        }

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8;
        if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':' && c19 == '.') {
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

            S0 = c20;
            S1 = c21;
            S2 = c22;
            S3 = c23;
            S4 = c24;
            S5 = c25;
            S6 = c26;
            S7 = c27;
            S8 = c28;
        } else {
            return null;
        }

        LocalDateTime ldt = JSONReaderUTF16.getLocalDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);
        if (ldt == null) {
            return null;
        }

        offset += (len + 1);
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    public UUID readUUID() {
        if (ch == 'n') {
            readNull();
            return null;
        }

        if (ch != '\"') {
            throw new JSONException("syntax error, can not read uuid, position " + offset);
        }

        if (offset + 32 < bytes.length && bytes[offset + 32] == '"') {
            long msb1 = parse4Nibbles(bytes, offset + 0);
            long msb2 = parse4Nibbles(bytes, offset + 4);
            long msb3 = parse4Nibbles(bytes, offset + 8);
            long msb4 = parse4Nibbles(bytes, offset + 12);
            long lsb1 = parse4Nibbles(bytes, offset + 16);
            long lsb2 = parse4Nibbles(bytes, offset + 20);
            long lsb3 = parse4Nibbles(bytes, offset + 24);
            long lsb4 = parse4Nibbles(bytes, offset + 28);
            if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                offset += 33;
                ch = (char) bytes[offset++];
                return new UUID(
                        msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                        lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
            }
        } else if (offset + 36 < bytes.length && bytes[offset + 36] == '"') {
            char ch1 = (char) bytes[offset + 8];
            char ch2 = (char) bytes[offset + 13];
            char ch3 = (char) bytes[offset + 18];
            char ch4 = (char) bytes[offset + 23];
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
                    offset += 37;
                    ch = (char) bytes[offset++];
                    return new UUID(
                            msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                            lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
                }
            }
        }

        String str = readString();
        return UUID.fromString(str);
    }

    @Override
    public String readPattern() {
        if (ch != '/') {
            throw new JSONException("illegal pattern");
        }

        int offset = this.offset;
        for (int i = 0; ; ++i) {
            char c = (char) bytes[offset];
            if (c == '/') {
                break;
            }
            offset++;
            if (offset >= end) {
                break;
            }
        }
        String str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);

        if (offset + 1 == end) {
            this.offset = end;
            this.ch = EOI;
            return str;
        }

        int b = (char) bytes[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = (char) bytes[++offset];
        }

        if (b == ',') {
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
}

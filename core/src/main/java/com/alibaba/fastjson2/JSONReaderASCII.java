package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
            for (int i = 0; ; ++i) {
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

package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONFactory.UUIDUtils.parse4Nibbles;

final class JSONReaderStr extends JSONReader {
    protected final String str;
    protected final int length;
    protected final int end;

    protected int nameBegin;
    protected int nameEnd;
    protected int nameLength;


    JSONReaderStr(Context ctx, String str, int offset, int length) {
        super(ctx);

        this.str = str;
        this.offset = offset;
        this.length = length;
        this.end = offset + length;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = str.charAt(offset);
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = str.charAt(this.offset);
            }
            this.offset++;
        }

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }
    }

    @Override
    public boolean nextIfMatch(char ch) {
        if (this.ch != ch) {
            return false;
        }

        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        this.ch = str.charAt(offset);
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = str.charAt(offset);
        }
        offset++;
        return true;
    }

    @Override
    public void next() {
        if (offset >= end) {
            ch = EOI;
            return;
        }

        ch = str.charAt(offset);
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return;
            }
            ch = str.charAt(offset);
        }
        offset++;
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
                ch = str.charAt(offset++);
                switch (ch) {
                    case 'u': {
                        char c1 = str.charAt(offset++);
                        char c2 = str.charAt(offset++);
                        char c3 = str.charAt(offset++);
                        char c4 = str.charAt(offset++);
                        ch = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        char c1 = str.charAt(offset++);
                        char c2 = str.charAt(offset++);
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
                    : str.charAt(offset++);
        }

        return hashCode;
    }

    @Override
    public long readFieldNameHashCode() {
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        final char quote = ch;

        this.stringValue = null;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; ; ++i) {
            char c = str.charAt(offset);
            if (c == '\\') {
                nameEscape = true;
                c = str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        char c1 = str.charAt(++offset);
                        char c2 = str.charAt(++offset);
                        char c3 = str.charAt(++offset);
                        char c4 = str.charAt(++offset);
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        char c1 = str.charAt(++offset);
                        char c2 = str.charAt(++offset);
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
                if (offset < end) {
                    c = str.charAt(offset);

                    while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                        offset++;
                        c = str.charAt(offset);
                    }
                } else {
                    ch = EOI;
                }
                if (c != ':') {
                    return -1;
                }

                offset++;
                c = str.charAt(offset);

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
                }

                this.offset = offset + 1;
                this.ch = (char) c;
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

        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (int i = 0; ; ++i) {
            char c = str.charAt(offset);
            if (c == '\\') {
                nameEscape = true;
                c = str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        char c1 = str.charAt(++offset);
                        char c2 = str.charAt(++offset);
                        char c3 = str.charAt(++offset);
                        char c4 = str.charAt(++offset);
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        char c1 = str.charAt(++offset);
                        char c2 = str.charAt(++offset);
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
                this.stringValue = null;
                offset++;
                if (offset == end) {
                    c = EOI;
                } else {
                    c = str.charAt(offset);
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
                }

                if (c == ',') {
                    offset++;
                    c = str.charAt(offset);

                    while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                        offset++;
                        c = str.charAt(offset);
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
        for (; ; ) {
            char c = str.charAt(offset);

            if (c == '\\') {
                c = str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        int c1 = str.charAt(++offset);
                        int c2 = str.charAt(++offset);
                        int c3 = str.charAt(++offset);
                        int c4 = str.charAt(++offset);
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = str.charAt(++offset);
                        int c2 = str.charAt(++offset);
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

            offset++;
            if (c == '_') {
                continue;
            }

            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 32);
            }
            hashCode ^= c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public String getFieldName() {
        if (!nameEscape) {
            return this.str.substring(nameBegin, nameEnd);
        }

        char[] chars = new char[nameLength];
        int offset = nameBegin;
        for (int i = 0; offset < nameEnd; ++i) {
            char c = this.str.charAt(offset);

            if (c == '\\') {
                c = this.str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        int c1 = this.str.charAt(++offset);
                        int c2 = this.str.charAt(++offset);
                        int c3 = this.str.charAt(++offset);
                        int c4 = this.str.charAt(++offset);
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = this.str.charAt(++offset);
                        int c2 = this.str.charAt(++offset);
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
        for (int i = 0; offset < end ; ++i) {
            int c = str.charAt(offset);
            if (c == '\\') {
                nameEscape = true;
                c = str.charAt(++offset);
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

            if (c == quote) {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                if (offset < end) {
                    c = str.charAt(offset);
                } else {
                    c = EOI;
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
                }
                if (c != ':') {
                    throw new JSONException("syntax error : " + offset);
                }

                offset++;
                c = str.charAt(offset);

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
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
    public boolean skipName() {
        if (ch != '"') {
            throw new JSONException("not support unquoted name");
        }

        int offset = this.offset;
        for (; ; ) {
            char c = str.charAt(offset);
            if (c == '\\') {
                c = str.charAt(++offset);
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

            if (c == '"') {
                ;
                offset++;
                c = str.charAt(offset);

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
                }
                if (c != ':') {
                    throw new JSONException("syntax error, expect ',', but '" + c + "'");
                }

                offset++;
                c = str.charAt(offset);

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = str.charAt(offset);
                }

                this.offset = offset + 1;
                this.ch = c;
                break;
            }

            offset++;
        }

        return true;
    }

    @Override
    public int readInt32Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        int intValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = str.charAt(offset++);
        }

        if (ch == '-') {
            negative = true;
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
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
                    throw new JSONException("int overflow, value " + bigInteger);
                }
            } else {
                return getInt32Value();
            }
        }

        if (quote != 0) {
            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }

        if (ch == ',') {
            this.ch = str.charAt(this.offset++);
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        if (ch == '-') {
            negative = true;
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }

        if (ch == ',') {
            this.ch = str.charAt(this.offset++);
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = str.charAt(offset++);
                    }
                }
            }
        }

        return negative ? -intValue : intValue;
    }

    @Override
    public long readInt64Value() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        long longValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = str.charAt(offset++);
        }

        if (ch == '-') {
            negative = true;
            ch = str.charAt(offset++);
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
            if (offset >= end) {
                ch = EOI;
                break;
            }

            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }

        if (ch == ',') {
            this.ch = str.charAt(this.offset++);
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        if (ch == '-') {
            negative = true;
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }

        if (ch == ',') {
            this.ch = str.charAt(this.offset++);
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = str.charAt(offset++);
                    }
                }
            }
        }

        return negative ? -longValue : longValue;
    }

    protected void skipString() {
        char quote = this.ch;
        ch = str.charAt(offset++);
        _for:
        for (; ; ) {
            if (ch == '\\') {
                ch = str.charAt(offset++);
                char1(ch);
                continue;
            }
            if (ch == quote) {
                ch = str.charAt(offset++);
                break;
            }

            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = str.charAt(offset++);
        }

        if (ch == ',') {
            comma = true;

            if (offset >= end) {
                ch = EOI;
                return;
            }

            ch = str.charAt(offset);
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    ch = EOI;
                    return;
                }
                ch = str.charAt(offset);
            }
            offset++;
        }
    }

    @Override
    public String getString() {
        if (stringValue != null) {
            return stringValue;
        }

        if (!nameEscape) {
            return this.str.substring(nameBegin, nameEnd);
        }

        char[] chars = new char[nameLength];
        int offset = nameBegin;
        for (int i = 0; ; ++i) {
            char c = this.str.charAt(offset);

            if (c == '\\') {
                c = this.str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        int c1 = this.str.charAt(++offset);
                        int c2 = this.str.charAt(++offset);
                        int c3 = this.str.charAt(++offset);
                        int c4 = this.str.charAt(++offset);
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = this.str.charAt(++offset);
                        int c2 = this.str.charAt(++offset);
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
            chars[i] = c;
            offset++;
        }

        return stringValue = new String(chars);
    }

    protected void readString0() {
        char quote = this.ch;
        int offset = this.offset;
        int start = offset;
        int valueLength;
        valueEscape = false;

        _for:
        for (int i = 0; ; ++i) {
            char c = str.charAt(offset);
            if (c == '\\') {
                valueEscape = true;
                c = str.charAt(++offset);
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
                char c = this.str.charAt(offset);
                if (c == '\\') {
                    c = this.str.charAt(++offset);
                    switch (c) {
                        case 'u': {
                            int c1 = this.str.charAt(++offset);
                            int c2 = this.str.charAt(++offset);
                            int c3 = this.str.charAt(++offset);
                            int c4 = this.str.charAt(++offset);
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            int c1 = this.str.charAt(++offset);
                            int c2 = this.str.charAt(++offset);
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
                chars[i] = c;
                offset++;
            }

            str = new String(chars);
        } else {
            str = this.str.substring(this.offset, offset);
        }

        ++offset;
        int b;
        if (offset == end) {
            b = EOI;
        } else {
            b = this.str.charAt(offset);
        }
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = this.str.charAt(++offset);
        }

        if (b == ',') {
            this.offset = offset + 1;

            // inline next
            ch = this.str.charAt(this.offset++);

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (this.offset >= end) {
                    ch = EOI;
                } else {
                    ch = this.str.charAt(this.offset++);
                }
            }
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
                char c = str.charAt(offset);
                if (c == '\\') {
                    valueEscape = true;
                    c = str.charAt(++offset);
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
                    char c = this.str.charAt(offset);
                    if (c == '\\') {
                        c = this.str.charAt(++offset);
                        switch (c) {
                            case 'u': {
                                char c1 = this.str.charAt(++offset);
                                char c2 = this.str.charAt(++offset);
                                char c3 = this.str.charAt(++offset);
                                char c4 = this.str.charAt(++offset);
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                char c1 = this.str.charAt(++offset);
                                char c2 = this.str.charAt(++offset);
                                c = char2(c1, c2);
                                break;
                            }
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
                str = this.str.substring(this.offset, offset);
            }

            if (offset + 1 == end) {
                this.offset = end;
                this.ch = EOI;
                return str;
            }

            int b = this.str.charAt(++offset);
            while (b <= ' ' && ((1L << b) & SPACE) != 0) {
                b = this.str.charAt(++offset);
            }

            if (b == ',') {
                this.offset = offset + 1;

                // inline next
                ch = this.str.charAt(this.offset++);

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (this.offset >= end) {
                        ch = EOI;
                    } else {
                        ch = this.str.charAt(this.offset++);
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

    @Override
    public byte[] readBinary() {
        if (ch != '"' && ch != '\'') {
            throw new UnsupportedOperationException();
        }

        throw new UnsupportedOperationException();
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
                    ch = str.charAt(offset++);
                    if (ch == '}' || ch == ']') {
                        break;
                    }
                    if (ch == ',') {
                        if (offset >= end) {
                            ch = EOI;
                            return;
                        }

                        ch = str.charAt(offset);
                        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                            offset++;
                            if (offset >= end) {
                                ch = EOI;
                                return;
                            }
                            ch = str.charAt(offset);
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

            ch = str.charAt(offset);
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    ch = EOI;
                    return;
                }
                ch = str.charAt(offset);
            }
            offset++;
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
            ch = str.charAt(offset++);
        }
        final int start = offset;

        final int limit, multmin;
        if (ch == '-') {
            limit = Integer.MIN_VALUE;
            multmin = -214748364; // limit / 10;
            negative = true;
            ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = str.charAt(offset++);
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
                ch = str.charAt(offset++);
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;
            bigInt(numStart, offset - 1);
        } else {
            mag3 = -mag3;
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = str.charAt(offset++);

            if (ch == '-') {
                negativeExp = true;
                ch = str.charAt(offset++);
            } else if (ch == '+') {
                ch = str.charAt(offset++);
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
                ch = str.charAt(offset++);
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (byte) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (str.charAt(offset++) == 'u'
                        && str.charAt(offset++) == 'l'
                        && str.charAt(offset++) == 'l'
                ) {
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = str.charAt(offset++);
                    }
                }
            } else if (ch == 't') {
                if (str.charAt(offset++) == 'r'
                        && str.charAt(offset++) == 'u'
                        && str.charAt(offset++) == 'e'
                ) {
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = str.charAt(offset++);
                    }
                }
            } else if (ch == 'f') {
                if (str.charAt(offset++) == 'a'
                        && str.charAt(offset++) == 'l'
                        && str.charAt(offset++) == 's'
                        && str.charAt(offset++) == 'e'
                ) {
                    boolValue = false;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = str.charAt(offset++);
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
            ch = str.charAt(offset++);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }

        if (ch == ',') {
            this.ch = str.charAt(this.offset++);
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = str.charAt(offset++);
                    }
                }
            }
        }
    }

    @Override
    public boolean readIfNull() {
        if (ch == 'n'
                && str.charAt(offset) == 'u'
                && str.charAt(offset + 1) == 'l'
                && str.charAt(offset + 2) == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset + 3);
            }
            offset += 4;
        } else {
            return false;
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }
        if (ch == ',') {
            ch = str.charAt(offset++);

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = str.charAt(offset++);
                }
            }
        }

        return true;
    }

    @Override
    public void readNull() {
        if (str.charAt(offset) == 'u'
                && str.charAt(offset + 1) == 'l'
                && str.charAt(offset + 2) == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset + 3);
            }
            offset += 4;
        } else {
            throw new JSONException("json syntax error, not match null" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = str.charAt(offset++);
            }
        }
        if (ch == ',') {
            ch = str.charAt(offset++);

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = str.charAt(offset++);
                }
            }
        }
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

        if (offset + 32 < end && str.charAt(offset + 32) == '"') {
            long msb1 = parse4Nibbles(str, offset + 0);
            long msb2 = parse4Nibbles(str, offset + 4);
            long msb3 = parse4Nibbles(str, offset + 8);
            long msb4 = parse4Nibbles(str, offset + 12);
            long lsb1 = parse4Nibbles(str, offset + 16);
            long lsb2 = parse4Nibbles(str, offset + 20);
            long lsb3 = parse4Nibbles(str, offset + 24);
            long lsb4 = parse4Nibbles(str, offset + 28);
            if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                offset += 33;
                ch = str.charAt(offset++);
                return new UUID(
                        msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                        lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
            }
        } else if (offset + 36 < str.length() && str.charAt(offset + 36) == '"') {
            char ch1 = str.charAt(offset + 8);
            char ch2 = str.charAt(offset + 13);
            char ch3 = str.charAt(offset + 18);
            char ch4 = str.charAt(offset + 23);
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                long msb1 = parse4Nibbles(str, offset + 0);
                long msb2 = parse4Nibbles(str, offset + 4);
                long msb3 = parse4Nibbles(str, offset + 9);
                long msb4 = parse4Nibbles(str, offset + 14);
                long lsb1 = parse4Nibbles(str, offset + 19);
                long lsb2 = parse4Nibbles(str, offset + 24);
                long lsb3 = parse4Nibbles(str, offset + 28);
                long lsb4 = parse4Nibbles(str, offset + 32);
                if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                    offset += 37;
                    ch = str.charAt(offset++);
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
    public int getStringLength() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        final char quote = ch;

        int len = 0;
        for (int i = offset; i < end; ++i, ++len) {
            if (str.charAt(i) == quote) {
                break;
            }
        }
        return len;
    }

    @Override
    protected LocalDateTime readLocalDateTime16() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1;
        if (c4 == '-' && c7 == '-' && (c10 == 'T' || c10 == ' ') && c13 == ':') {
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
    protected LocalDateTime readLocalDateTime17() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c7 == '-' && c10 == 'T' && c13 == ':' && c16 == 'Z') {
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

            s0 = '0';
            s1 = '0';
        } else if (c4 == '-' && c6 == '-' && c8 == ' ' && c11 == ':' && c14 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = '0';
            d1 = c7;

            h0 = c9;
            h1 = c10;

            i0 = c12;
            i1 = c13;

            s0 = c15;
            s1 = c16;
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
    protected LocalDateTime readLocalDateTime18() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);
        char c17 = str.charAt(offset + 17);

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
    protected LocalTime readLocalTime8() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);

        char h0, h1, i0, i1, s0, s1;
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
    public LocalDateTime readLocalDate8() {
        if (ch != '"') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);


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

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);

        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '年' && c6 == '月' && c8 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = '0';
            d1 = c7;
        } else if (c4 == '년' && c6 == '월' && c8 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = '0';
            d1 = c7;
        } else if (c4 == '-' && c6 == '-') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;
        } else if (c4 == '-' && c7 == '-') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;
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

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);

        char y0, y1, y2, y3, m0, m1, d0, d1;
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
        } else if (c4 == '年' && c6 == '月' && c9 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;
        } else if (c4 == '년' && c6 == '월' && c9 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = '0';
            m1 = c5;

            d0 = c7;
            d1 = c8;
        } else if (c4 == '年' && c7 == '月' && c9 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;
        } else if (c4 == '년' && c7 == '월' && c9 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = '0';
            d1 = c8;
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
    public LocalDateTime readLocalDate11() {
        if (ch != '"') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);

        char y0, y1, y2, y3, m0, m1, d0, d1;
        if (c4 == '年' && c7 == '月' && c10 == '日') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
        } else if (c4 == '년' && c7 == '월' && c10 == '일') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;
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

        offset += 12;
        next();
        if (ch == ',') {
            next();
        }
        return ldt;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        if (len < 19) {
            return null;
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);
        char c17 = str.charAt(offset + 17);
        char c18 = str.charAt(offset + 18);
        char c19 = len == 19 ? ' ' : str.charAt(offset + 19);

        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0', c29 = '\0';
        switch (len) {
            case 19:
            case 20:
                c20 = '\0';
                break;
            case 21:
                c20 = str.charAt(offset + 20);
                break;
            case 22:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                break;
            case 23:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                break;
            case 24:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                break;
            case 25:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                break;
            case 26:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                break;
            case 27:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                break;
            case 28:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                c27 = str.charAt(offset + 27);
                break;
            case 29:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                c27 = str.charAt(offset + 27);
                c28 = str.charAt(offset + 28);
                break;
            default:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                c27 = str.charAt(offset + 27);
                c28 = str.charAt(offset + 28);
                c29 = str.charAt(offset + 29);
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

        char first = str.charAt(this.offset + zoneIdBegin);

        LocalDateTime ldt = getLocalDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);

        ZoneId zoneId;
        if (isTimeZone) {
            String tzStr = this.str.substring(this.offset + zoneIdBegin, this.offset + len);
            // String tzStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin);
            TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            zoneId = timeZone.toZoneId();
        } else {
            if (first == 'Z') {
                zoneId = UTC;
            } else {
                String zoneIdStr;
                if (first == '+' || first == '-') {
                    zoneIdStr = this.str.substring(this.offset + zoneIdBegin, this.offset + len);
                    ;
//                    zoneIdStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin);
                } else if (first == ' ') {
                    zoneIdStr = this.str.substring(this.offset + zoneIdBegin + 1, this.offset + len);
                } else { // '[
                    if (zoneIdBegin < len) {
                        zoneIdStr = this.str.substring(this.offset + zoneIdBegin + 1, this.offset + len - 1);
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
    protected LocalDateTime readLocalDateTime19() {
        if (ch != '"') {
            throw new JSONException("date only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);
        char c17 = str.charAt(offset + 17);
        char c18 = str.charAt(offset + 18);

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

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);
        char c17 = str.charAt(offset + 17);
        char c18 = str.charAt(offset + 18);
        char c19 = str.charAt(offset + 19);
        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0';
        switch (len) {
            case 21:
                c20 = str.charAt(offset + 20);
                break;
            case 22:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                break;
            case 23:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                break;
            case 24:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                break;
            case 25:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                break;
            case 26:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                break;
            case 27:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                break;
            case 28:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                c27 = str.charAt(offset + 27);
                break;
            default:
                c20 = str.charAt(offset + 20);
                c21 = str.charAt(offset + 21);
                c22 = str.charAt(offset + 22);
                c23 = str.charAt(offset + 23);
                c24 = str.charAt(offset + 24);
                c25 = str.charAt(offset + 25);
                c26 = str.charAt(offset + 26);
                c27 = str.charAt(offset + 27);
                c28 = str.charAt(offset + 28);
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

        LocalDateTime ldt = getLocalDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);
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

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second, nanos);
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime10() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);

        char h0, h1, i0, i1, s0, s1, m0, m1, m2;
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
    protected LocalTime readLocalTime11() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);

        char h0, h1, i0, i1, s0, s1, m0, m1, m2;
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
    protected LocalTime readLocalTime12() {
        if (ch != '"') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);

        char h0, h1, i0, i1, s0, s1, m0, m1, m2;
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

        char c0 = str.charAt(offset + 0);
        char c1 = str.charAt(offset + 1);
        char c2 = str.charAt(offset + 2);
        char c3 = str.charAt(offset + 3);
        char c4 = str.charAt(offset + 4);
        char c5 = str.charAt(offset + 5);
        char c6 = str.charAt(offset + 6);
        char c7 = str.charAt(offset + 7);
        char c8 = str.charAt(offset + 8);
        char c9 = str.charAt(offset + 9);
        char c10 = str.charAt(offset + 10);
        char c11 = str.charAt(offset + 11);
        char c12 = str.charAt(offset + 12);
        char c13 = str.charAt(offset + 13);
        char c14 = str.charAt(offset + 14);
        char c15 = str.charAt(offset + 15);
        char c16 = str.charAt(offset + 16);
        char c17 = str.charAt(offset + 17);

        char h0, h1, i0, i1, s0, s1, m0, m1, m2, m3, m4, m5, m6, m7, m8;
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
    public String readPattern() {
        if (ch != '/') {
            throw new JSONException("illegal pattern");
        }

        int offset = this.offset;
        for (int i = 0; ; ++i) {
            char c = str.charAt(offset);
            if (c == '/') {
                break;
            }
            offset++;
            if (offset >= end) {
                break;
            }
        }
        String str = this.str.substring(this.offset, offset);

        if (offset + 1 == end) {
            this.offset = end;
            this.ch = EOI;
            return str;
        }

        int b = this.str.charAt(++offset);
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = this.str.charAt(++offset);
        }

        if (b == ',') {
            this.offset = offset + 1;

            // inline next
            ch = this.str.charAt(this.offset++);

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (this.offset >= end) {
                    ch = EOI;
                } else {
                    ch = this.str.charAt(this.offset++);
                }
            }
        } else {
            this.offset = offset + 1;
            this.ch = (char) b;
        }

        return str;
    }

    void bigInt(int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        if (numDigits > 128) {
            throw new JSONException("number too large : " + str.substring(off, off + numDigits));
        }

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = str.charAt(start++);
            if (c == '.') {
                c = str.charAt(start++);
                cursor++;
//                    end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = str.charAt(index);
                if (c == '.') {
                    c = str.charAt(++index);
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

                char c = str.charAt(start++);
                if (c == '.') {
                    c = str.charAt(start++);
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = str.charAt(index);
                    if (c == '.') {
                        c = str.charAt(++index);
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
}

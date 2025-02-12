package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReaderJSONB.check3;
import static com.alibaba.fastjson2.util.IOUtils.getLongLE;
import static com.alibaba.fastjson2.util.IOUtils.hexDigit4;
import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONReaderASCII
        extends JSONReaderUTF8 {
    final String str;

    JSONReaderASCII(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, bytes, offset, length);
        this.str = str;
        nameAscii = true;
    }

    JSONReaderASCII(Context ctx, InputStream is) {
        super(ctx, is);
        nameAscii = true;
        str = null;
    }

    @Override
    public final void next() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = offset >= end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch > 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        this.offset = offset;
        this.ch = (char) (ch & 0xFF);
        if (ch == '/') {
            skipComment();
        }
    }

    public final boolean nextIfObjectStart() {
        int ch = this.ch;
        if (ch != '{') {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        this.ch = (char) (ch & 0xFF);
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfNullOrEmptyString() {
        final char first = this.ch;
        final int end = this.end;
        int offset = this.offset;
        byte[] bytes = this.bytes;
        if (first == 'n'
                && offset + 2 < end
                && bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l'
        ) {
            offset += 3;
        } else if (first == '"' || first == '\'') {
            if (offset < end && bytes[offset] == first) {
                offset++;
            } else if (offset + 4 < end
                    && IOUtils.isNULL(bytes, offset)
                    && bytes[offset + 4] == first
            ) {
                offset += 5;
            } else {
                return false;
            }
        } else {
            return false;
        }

        int ch = offset == end ? EOI : bytes[offset++];

        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        this.offset = offset;
        this.ch = (char) (ch & 0xFF);
        return true;
    }

    @Override
    public long readFieldNameHashCode() {
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        if (ch == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }
        if (ch != '"' && ch != '\'') {
            if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0 && isFirstIdentifier(ch)) {
                return readFieldNameHashCodeUnquote();
            }
            if (ch == '}' || isNull()) {
                return -1;
            }

            String errorMsg, preFieldName;
            if (ch == '[' && nameBegin > 0 && (preFieldName = getFieldName()) != null) {
                errorMsg = "illegal fieldName input " + ch + ", previous fieldName " + preFieldName;
            } else {
                errorMsg = "illegal fieldName input " + ch;
            }

            throw new JSONException(info(errorMsg));
        }

        final int quote = ch;
        final int end = this.end;
        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;

        if (offset + 9 < end) {
            byte c0, c1, c2, c3, c4, c5, c6, c7;

            if ((c0 = bytes[offset]) == quote) {
                nameValue = 0;
            } else if ((c1 = bytes[offset + 1]) == quote && c0 != '\\' && c0 > 0) {
                nameValue = c0;
                this.nameLength = 1;
                this.nameEnd = offset + 1;
                offset += 2;
            } else if ((c2 = bytes[offset + 2]) == quote
                    && c0 != '\\' && c1 != '\\'
                    && c0 >= 0 && c1 > 0
            ) {
                nameValue = (c1 << 8)
                        + c0;
                this.nameLength = 2;
                this.nameEnd = offset + 2;
                offset += 3;
            } else if ((c3 = bytes[offset + 3]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\'
                    && (c0 | c1) >= 0 && c2 > 0
            ) {
                nameValue
                        = (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 3;
                this.nameEnd = offset + 3;
                offset += 4;
            } else if ((c4 = bytes[offset + 4]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\'
                    && (c0 | c1 | c2) >= 0 && c3 > 0
            ) {
                nameValue
                        = (c3 << 24)
                        + (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 4;
                this.nameEnd = offset + 4;
                offset += 5;
            } else if ((c5 = bytes[offset + 5]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\'
                    && (c0 | c1 | c2 | c3) >= 0 && c4 > 0
            ) {
                nameValue
                        = (((long) c4) << 32)
                        + (c3 << 24)
                        + (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 5;
                this.nameEnd = offset + 5;
                offset += 6;
            } else if ((c6 = bytes[offset + 6]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\'
                    && (c0 | c1 | c2 | c3 | c4) >= 0 && c5 > 0
            ) {
                nameValue
                        = (((long) c5) << 40)
                        + (((long) c4) << 32)
                        + (c3 << 24)
                        + (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 6;
                this.nameEnd = offset + 6;
                offset += 7;
            } else if ((c7 = bytes[offset + 7]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\' && c6 != '\\'
                    && (c0 | c1 | c2 | c3 | c4 | c5) >= 0 && c6 > 0
            ) {
                nameValue
                        = (((long) c6) << 48)
                        + (((long) c5) << 40)
                        + (((long) c4) << 32)
                        + (c3 << 24)
                        + (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 7;
                this.nameEnd = offset + 7;
                offset += 8;
            } else if (bytes[offset + 8] == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\' && c6 != '\\' && c7 != '\\'
                    && (c0 | c1 | c2 | c3 | c4 | c5 | c6) >= 0 && c7 > 0
            ) {
                nameValue
                        = (((long) c7) << 56)
                        + (((long) c6) << 48)
                        + (((long) c5) << 40)
                        + (((long) c4) << 32)
                        + (c3 << 24)
                        + (c2 << 16)
                        + (c1 << 8)
                        + c0;
                this.nameLength = 8;
                this.nameEnd = offset + 8;
                offset += 9;
            }
        }

        if (nameValue == 0) {
            for (int i = 0; offset < end; offset++, i++) {
                ch = bytes[offset] & 0xFF;

                if (ch == quote) {
                    if (i == 0) {
                        offset = this.nameBegin;
                        break;
                    }

                    this.nameLength = i;
                    this.nameEnd = offset;
                    offset++;
                    break;
                }

                if (ch == '\\') {
                    nameEscape = true;
                    ch = bytes[++offset];
                    switch (ch) {
                        case 'u': {
                            ch = hexDigit4(bytes, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
                            break;
                        }
                        case '\\':
                        case '"':
                        default:
                            ch = char1(ch);
                            break;
                    }
                    if (ch > 0xFF) {
                        nameAscii = false;
                    }
                } else if (ch == -61 || ch == -62) {
                    byte c1 = bytes[++offset];
                    ch = (char) (((ch & 0x1F) << 6)
                            | (c1 & 0x3F));
                    nameAscii = false;
                }

                if (ch > 0xFF || ch < 0 || i >= 8 || (i == 0 && ch == 0)) {
                    nameValue = 0;
                    offset = this.nameBegin;
                    break;
                }

                switch (i) {
                    case 0:
                        nameValue = (byte) ch;
                        break;
                    case 1:
                        nameValue = (((byte) ch) << 8) + (nameValue & 0xFFL);
                        break;
                    case 2:
                        nameValue = (((byte) ch) << 16) + (nameValue & 0xFFFFL);
                        break;
                    case 3:
                        nameValue = (((byte) ch) << 24) + (nameValue & 0xFFFFFFL);
                        break;
                    case 4:
                        nameValue = (((long) (byte) ch) << 32) + (nameValue & 0xFFFFFFFFL);
                        break;
                    case 5:
                        nameValue = (((long) (byte) ch) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                        break;
                    case 6:
                        nameValue = (((long) (byte) ch) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                        break;
                    case 7:
                        nameValue = (((long) (byte) ch) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                        break;
                    default:
                        break;
                }
            }
        }

        long hashCode;
        if (nameValue != 0) {
            hashCode = nameValue;
        } else {
            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; ; ++i) {
                ch = bytes[offset];
                if (ch == '\\') {
                    nameEscape = true;
                    ch = bytes[++offset];
                    switch (ch) {
                        case 'u': {
                            ch = hexDigit4(bytes, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
                            break;
                        }
                        case '\\':
                        case '"':
                        default:
                            ch = char1(ch);
                            break;
                    }
                    offset++;
                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                    continue;
                }

                if (ch == quote) {
                    this.nameLength = i;
                    this.nameEnd = offset;
                    offset++;
                    break;
                }

                offset++;
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch != ':') {
            throw new JSONException(info("expect ':', but " + ch));
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        this.offset = offset;
        this.ch = (char) (ch & 0xFF);

        return hashCode;
    }

    @Override
    public final long readFieldNameHashCodeUnquote() {
        this.nameEscape = false;
        int offset = this.offset, end = this.end;
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        this.nameBegin = offset - 1;
        int first = ch;
        long nameValue = 0;
        _for:
        for (int i = 0; offset <= end; ++i) {
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
                case '|':
                case '&':
                case EOI:
                    nameLength = i;
                    this.nameEnd = ch == EOI ? offset : offset - 1;
                    if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        ch = offset == end ? EOI : (char) bytes[offset++];
                    }
                    break _for;
                default:
                    break;
            }

            if (ch == '\\') {
                nameEscape = true;
                ch = (char) bytes[offset++];
                switch (ch) {
                    case 'u': {
                        ch = hexDigit4(bytes, check3(offset, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        ch = char2(bytes[offset], bytes[offset + 1]);
                        offset += 2;
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
                        ch = char1(ch);
                        break;
                }
            }

            if (ch > 0xFF || i >= 8 || (i == 0 && ch == 0)) {
                nameValue = 0;
                ch = first;
                offset = this.nameBegin + 1;
                break;
            }

            byte c = (byte) ch;
            switch (i) {
                case 0:
                    nameValue = c;
                    break;
                case 1:
                    nameValue = (c << 8) + (nameValue & 0xFFL);
                    break;
                case 2:
                    nameValue = (c << 16) + (nameValue & 0xFFFFL);
                    break;
                case 3:
                    nameValue = (c << 24) + (nameValue & 0xFFFFFFL);
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

            ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
        }

        long hashCode;

        if (nameValue != 0) {
            hashCode = nameValue;
        } else {
            hashCode = Fnv.MAGIC_HASH_CODE;
            _for:
            for (int i = 0; ; ++i) {
                if (ch == '\\') {
                    nameEscape = true;
                    ch = bytes[offset++];
                    switch (ch) {
                        case 'u': {
                            ch = hexDigit4(bytes, check3(offset, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(bytes[offset], bytes[offset + 1]);
                            offset += 2;
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
                            ch = char1(ch);
                            break;
                    }

                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                    ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
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
                        this.nameEnd = ch == EOI ? offset : offset - 1;
                        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                            ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
                        }
                        break _for;
                    default:
                        break;
                }

                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;

                ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
            }
        }

        if (ch == ':') {
            ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : (bytes[offset++] & 0xFF);
            }
        }

        this.offset = offset;
        this.ch = (char) ch;

        return hashCode;
    }

    @Override
    public final long readValueHashCode() {
        int ch = this.ch;
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        final byte[] bytes = this.bytes;
        final int quote = ch;

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;
        for (int i = 0; offset < end; offset++, i++) {
            ch = bytes[offset];

            if (ch == quote) {
                if (i == 0) {
                    nameValue = 0;
                    offset = this.nameBegin;
                    break;
                }

                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                break;
            }

            if (ch == '\\') {
                nameEscape = true;
                ch = bytes[++offset];
                switch (ch) {
                    case 'u': {
                        ch = hexDigit4(bytes, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        ch = char2(bytes[offset + 1], bytes[offset + 2]);
                        offset += 2;
                        break;
                    }
                    case '\\':
                    case '"':
                    default:
                        ch = char1(ch);
                        break;
                }
            } else if (ch == -61 || ch == -62) {
                ch = (char) (((ch & 0x1F) << 6) | (bytes[++offset] & 0x3F));
            }

            if (ch > 0xFF || ch < 0 || i >= 8 || (i == 0 && ch == 0)) {
                nameValue = 0;
                offset = this.nameBegin;
                break;
            }

            switch (i) {
                case 0:
                    nameValue = (byte) ch;
                    break;
                case 1:
                    nameValue = (((byte) ch) << 8) + (nameValue & 0xFFL);
                    break;
                case 2:
                    nameValue = (((byte) ch) << 16) + (nameValue & 0xFFFFL);
                    break;
                case 3:
                    nameValue = (((byte) ch) << 24) + (nameValue & 0xFFFFFFL);
                    break;
                case 4:
                    nameValue = (((long) (byte) ch) << 32) + (nameValue & 0xFFFFFFFFL);
                    break;
                case 5:
                    nameValue = (((long) (byte) ch) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                    break;
                case 6:
                    nameValue = (((long) (byte) ch) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                    break;
                case 7:
                    nameValue = (((long) (byte) ch) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                    break;
                default:
                    break;
            }
        }

        long hashCode;
        if (nameValue != 0) {
            hashCode = nameValue;
        } else {
            hashCode = Fnv.MAGIC_HASH_CODE;
            for (int i = 0; ; ++i) {
                ch = bytes[offset];
                if (ch == '\\') {
                    nameEscape = true;
                    ch = bytes[++offset];
                    switch (ch) {
                        case 'u': {
                            ch = hexDigit4(bytes, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
                            break;
                        }
                        case '\\':
                        case '"':
                        default:
                            ch = char1(ch);
                            break;
                    }
                    offset++;
                    hashCode ^= ch;
                    hashCode *= Fnv.MAGIC_PRIME;
                    continue;
                }

                if (ch == '"') {
                    this.nameLength = i;
                    this.nameEnd = offset;
                    offset++;
                    break;
                }

                offset++;
                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.offset = offset;
        this.ch = (char) (ch & 0xFF);

        return hashCode;
    }

    @Override
    public final long getNameHashCodeLCase() {
        final byte[] bytes = this.bytes;
        int offset = nameBegin, end = this.end;
        long nameValue = 0;
        for (int i = 0; offset < end; offset++) {
            int c = bytes[offset];

            if (c == '\\') {
                c = bytes[++offset];
                switch (c) {
                    case 'u': {
                        c = hexDigit4(bytes, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(bytes[offset + 1], bytes[offset + 2]);
                        offset += 2;
                        break;
                    }
                    case '\\':
                    case '"':
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }

            if (c > 0xFF || c < 0 || i >= 8 || (i == 0 && c == 0)) {
                nameValue = 0;
                offset = this.nameBegin;
                break;
            }

            if (c == '_' || c == '-' || c == ' ') {
                byte c1 = bytes[offset + 1];
                if (c1 != '"' && c1 != '\'' && c1 != c) {
                    continue;
                }
            }

            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 32);
            }

            switch (i) {
                case 0:
                    nameValue = (byte) c;
                    break;
                case 1:
                    nameValue = (((byte) c) << 8) + (nameValue & 0xFFL);
                    break;
                case 2:
                    nameValue = (((byte) c) << 16) + (nameValue & 0xFFFFL);
                    break;
                case 3:
                    nameValue = (((byte) c) << 24) + (nameValue & 0xFFFFFFL);
                    break;
                case 4:
                    nameValue = (((long) (byte) c) << 32) + (nameValue & 0xFFFFFFFFL);
                    break;
                case 5:
                    nameValue = (((long) (byte) c) << 40) + (nameValue & 0xFFFFFFFFFFL);
                    break;
                case 6:
                    nameValue = (((long) (byte) c) << 48) + (nameValue & 0xFFFFFFFFFFFFL);
                    break;
                case 7:
                    nameValue = (((long) (byte) c) << 56) + (nameValue & 0xFFFFFFFFFFFFFFL);
                    break;
                default:
                    break;
            }
            ++i;
        }

        if (nameValue != 0) {
            return nameValue;
        }

        long hashCode = Fnv.MAGIC_HASH_CODE;
        while (offset < end) {
            int c = bytes[offset];

            if (c == '\\') {
                c = bytes[++offset];
                switch (c) {
                    case 'u': {
                        c = hexDigit4(bytes, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(bytes[offset + 1], bytes[offset + 2]);
                        offset += 2;
                        break;
                    }
                    case '\\':
                    case '"':
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }

            offset++;
            if (c == '_' || c == '-' || c == ' ') {
                byte c1 = bytes[offset];
                if (c1 != '"' && c1 != '\'' && c1 != c) {
                    continue;
                }
            }

            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 32);
            }
            hashCode ^= c < 0 ? (c & 0xFF) : c;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    @Override
    public String getFieldName() {
        final byte[] bytes = this.bytes;
        int offset = nameBegin;
        int length = nameEnd - offset;
        if (!nameEscape) {
            if (this.str != null) {
                return this.str.substring(offset, nameEnd);
            } else if (ANDROID) {
                return getLatin1String(offset, length);
            } else {
                return new String(bytes, offset, length, StandardCharsets.ISO_8859_1);
            }
        }

        if (JDKUtils.STRING_CREATOR_JDK11 != null) {
            byte[] chars = new byte[nameLength];
            forStmt:
            for (int i = 0, end = this.end; offset < nameEnd; ++i) {
                byte b = bytes[offset];

                if (b == '\\') {
                    b = bytes[++offset];
                    switch (b) {
                        case 'u': {
                            int ch = hexDigit4(bytes, check3(offset + 1, end));
                            offset += 4;
                            if (ch > 0xFF) {
                                chars = null;
                                break forStmt;
                            }
                            b = (byte) ch;
                            break;
                        }
                        case 'x': {
                            char c = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
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
                return STRING_CREATOR_JDK11.apply(chars, LATIN1);
            }
        }

        offset = nameBegin;
        char[] chars = new char[nameLength];
        for (int i = 0; offset < nameEnd; ++i) {
            char ch = (char) (bytes[offset] & 0xff);

            if (ch == '\\') {
                ch = (char) bytes[++offset];
                switch (ch) {
                    case 'u': {
                        ch = (char) hexDigit4(bytes, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        ch = char2(bytes[offset + 1], bytes[offset + 2]);
                        offset += 2;
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
                        ch = char1(ch);
                        break;
                }
            } else if (ch == '"') {
                break;
            }
            chars[i] = ch;
            offset++;
        }

        return new String(chars);
    }

    @Override
    public String readFieldName() {
        final char quote = ch;
        if (quote == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }
        if (quote != '"' && quote != '\'') {
            if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0 && isFirstIdentifier(quote)) {
                return readFieldNameUnquote();
            }

            return null;
        }

        final byte[] bytes = this.bytes;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        final int nameBegin = this.nameBegin;
        for (int i = 0; offset < end; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                nameEscape = true;
                c = bytes[offset + 1];
                offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
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
                    throw syntaxError(offset, ch);
                }

                offset++;
                if (offset >= end) {
                    this.ch = EOI;
                    throw syntaxError(offset, ch);
                }

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

        if (nameEnd < nameBegin) {
            throw new JSONException("syntax error : " + offset);
        }

        if (!nameEscape) {
            long nameValue0 = -1, nameValue1 = -1;
            int length = nameEnd - nameBegin;
            switch (length) {
                case 1:
                    return TypeUtils.toString(bytes[nameBegin]);
                case 2:
                    return TypeUtils.toString(
                            bytes[nameBegin],
                            bytes[nameBegin + 1]
                    );
                case 3:
                    nameValue0
                            = (bytes[nameBegin + 2] << 16)
                            + ((bytes[nameBegin + 1] & 0xFF) << 8)
                            + (bytes[nameBegin] & 0xFF);
                    break;
                case 4:
                    nameValue0
                            = (bytes[nameBegin + 3] << 24)
                            + ((bytes[nameBegin + 2] & 0xFF) << 16)
                            + ((bytes[nameBegin + 1] & 0xFF) << 8)
                            + (bytes[nameBegin] & 0xFF);
                    break;
                case 5:
                    nameValue0
                            = (((long) bytes[nameBegin + 4]) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    break;
                case 6:
                    nameValue0
                            = (((long) bytes[nameBegin + 5]) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    break;
                case 7:
                    nameValue0
                            = (((long) bytes[nameBegin + 6]) << 48)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    break;
                case 8:
                    nameValue0
                            = (((long) bytes[nameBegin + 7]) << 56)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    break;
                case 9:
                    nameValue0 = bytes[nameBegin];
                    nameValue1
                            = (((long) bytes[nameBegin + 8]) << 56)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 8)
                            + (bytes[nameBegin + 1] & 0xFFL);
                    break;
                case 10:
                    nameValue0
                            = (bytes[nameBegin + 1] << 8)
                            + (bytes[nameBegin]);
                    nameValue1
                            = (((long) bytes[nameBegin + 9]) << 56)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 8)
                            + (bytes[nameBegin + 2] & 0xFFL);
                    break;
                case 11:
                    nameValue0
                            = (bytes[nameBegin + 2] << 16)
                            + (bytes[nameBegin + 1] << 8)
                            + (bytes[nameBegin]);
                    nameValue1
                            = (((long) bytes[nameBegin + 10]) << 56)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 8)
                            + (bytes[nameBegin + 3] & 0xFFL);
                    break;
                case 12:
                    nameValue0
                            = (bytes[nameBegin + 3] << 24)
                            + (bytes[nameBegin + 2] << 16)
                            + (bytes[nameBegin + 1] << 8)
                            + (bytes[nameBegin]);
                    nameValue1
                            = (((long) bytes[nameBegin + 11]) << 56)
                            + ((bytes[nameBegin + 10] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 8)
                            + (bytes[nameBegin + 4] & 0xFFL);
                    break;
                case 13:
                    nameValue0
                            = (((long) bytes[nameBegin + 4]) << 32)
                            + (((long) bytes[nameBegin + 3]) << 24)
                            + (((long) bytes[nameBegin + 2]) << 16)
                            + (((long) bytes[nameBegin + 1]) << 8)
                            + ((long) bytes[nameBegin]);
                    nameValue1
                            = (((long) bytes[nameBegin + 12]) << 56)
                            + ((bytes[nameBegin + 11] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 10] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 8)
                            + (bytes[nameBegin + 5] & 0xFFL);
                    break;
                case 14:
                    nameValue0
                            = (((long) bytes[nameBegin + 5]) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    nameValue1
                            = (((long) bytes[nameBegin + 13]) << 56)
                            + ((bytes[nameBegin + 12] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 11] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 10] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 7] & 0xFFL) << 8)
                            + (bytes[nameBegin + 6] & 0xFFL);
                    break;
                case 15:
                    nameValue0
                            = (((long) bytes[nameBegin + 6]) << 48)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    nameValue1
                            = (((long) bytes[nameBegin + 14]) << 56)
                            + ((bytes[nameBegin + 13] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 12] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 11] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 10] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 8] & 0xFFL) << 8)
                            + (bytes[nameBegin + 7] & 0xFFL);
                    break;
                case 16:
                    nameValue0
                            = (((long) bytes[nameBegin + 7]) << 56)
                            + ((bytes[nameBegin + 6] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 5] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 4] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 3] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 2] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 1] & 0xFFL) << 8)
                            + (bytes[nameBegin] & 0xFFL);
                    nameValue1
                            = (((long) bytes[nameBegin + 15]) << 56)
                            + ((bytes[nameBegin + 14] & 0xFFL) << 48)
                            + ((bytes[nameBegin + 13] & 0xFFL) << 40)
                            + ((bytes[nameBegin + 12] & 0xFFL) << 32)
                            + ((bytes[nameBegin + 11] & 0xFFL) << 24)
                            + ((bytes[nameBegin + 10] & 0xFFL) << 16)
                            + ((bytes[nameBegin + 9] & 0xFFL) << 8)
                            + (bytes[nameBegin + 8] & 0xFFL);
                    break;
                default:
                    break;
            }

            if (nameValue0 != -1) {
                if (nameValue1 != -1) {
                    long nameValue01 = nameValue0 ^ nameValue1;
                    int indexMask = ((int) (nameValue01 ^ (nameValue01 >>> 32))) & (NAME_CACHE2.length - 1);
                    JSONFactory.NameCacheEntry2 entry = NAME_CACHE2[indexMask];
                    if (entry == null) {
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
                    int indexMask = ((int) (nameValue0 ^ (nameValue0 >>> 32))) & (NAME_CACHE.length - 1);
                    JSONFactory.NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
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
    protected final void readString0() {
        final byte[] bytes = this.bytes;
        char quote = this.ch;
        final int start = offset;
        int offset = this.offset;
        int valueLength;
        valueEscape = false;

        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                valueEscape = true;
                c = bytes[offset + 1];
                offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                continue;
            }

            if (c == quote) {
                valueLength = i;
                break;
            }
            offset++;
        }

        String str;
        if (valueEscape) {
            char[] chars = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                char c = (char) (bytes[offset] & 0xff);
                if (c == '\\') {
                    c = (char) (bytes[++offset]);
                    switch (c) {
                        case 'u': {
                            c = (char) hexDigit4(bytes, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            c = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
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
            if (STRING_CREATOR_JDK11 != null) {
                byte[] buf = Arrays.copyOfRange(bytes, start, offset);
                str = STRING_CREATOR_JDK11.apply(buf, LATIN1);
            } else {
                str = new String(bytes, start, offset - start, StandardCharsets.ISO_8859_1);
            }
        }

        int ch = bytes[++offset];
        while (ch > 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = bytes[++offset];
        }

        this.offset = offset + 1;
        if (comma = (ch == ',')) {
            next();
        } else {
            this.ch = (char) ch;
        }

        stringValue = str;
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;
            char quote = this.ch;
            int valueLength;
            int offset = this.offset;
            final int start = offset, end = this.end;
            final long byteVectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;
            valueEscape = false;

            {
                int i = 0;
                int upperBound = offset + ((end - offset) & ~7);
                while (offset < upperBound) {
                    long v = getLongLE(bytes, offset);
                    if ((v & 0xFF00FF00FF00FF00L) != 0 || containsSlashOrQuote(v, byteVectorQuote)) {
                        break;
                    }

                    offset += 8;
                    i += 8;
                }
                // ...

                for (; ; ++i) {
                    if (offset >= end) {
                        throw new JSONException("invalid escape character EOI");
                    }

                    int c = bytes[offset] & 0xFF;
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[offset + 1];
                        offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                        continue;
                    }

                    if (c == quote) {
                        valueLength = i;
                        break;
                    }
                    offset++;
                }
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    int ch = bytes[offset];
                    if (ch == '\\') {
                        ch = bytes[++offset];
                        switch (ch) {
                            case 'u': {
                                ch = hexDigit4(bytes, check3(offset + 1, end));
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                ch = char2(bytes[offset + 1], bytes[offset + 2]);
                                offset += 2;
                                break;
                            }
                            case '\\':
                            case '"':
                                break;
                            case 'b':
                                ch = '\b';
                                break;
                            case 't':
                                ch = '\t';
                                break;
                            case 'n':
                                ch = '\n';
                                break;
                            case 'f':
                                ch = '\f';
                                break;
                            case 'r':
                                ch = '\r';
                                break;
                            default:
                                ch = char1(ch);
                                break;
                        }
                        chars[i] = (char) ch;
                        offset++;
                    } else if (ch == quote) {
                        break;
                    } else {
                        chars[i] = (char) ch;
                        offset++;
                    }
                }

                str = new String(chars);
            } else {
                int strlen = offset - start;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[start] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[start] & 0xff),
                            (char) (bytes[start + 1] & 0xff)
                    );
                } else {
                    str = new String(bytes, start, offset - start, StandardCharsets.US_ASCII);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            int ch = ++offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : bytes[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;
            return str;
        }

        return readStringNotMatch();
    }

    public static JSONReaderASCII of(Context ctx, String str, byte[] bytes, int offset, int length) {
        return new JSONReaderASCII(ctx, str, bytes, offset, length);
    }
}

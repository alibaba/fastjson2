package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONFactory.*;
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
        if (ch == '/') {
            skipComment();
            ch = this.ch;
        }
        if (ch == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }
        if (ch != '"' && ch != '\'') {
            return readFieldNameHashCodeError(nameBegin, ch);
        }

        int offset = this.nameBegin = this.offset, end = this.end;
        int start = offset;

        int index;
        if (INDEX_OF_CHAR_LATIN1 == null) {
            index = IOUtils.indexOfQuoteV(bytes, ch, offset, end);
        } else {
            try {
                index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
            }
            catch (Throwable e) {
                throw new JSONException(e.getMessage());
            }
        }
        if (index == -1) {
            throw error("invalid escape character EOI");
        }
        int slashIndex = indexOfSlash(this, bytes, offset, end);
        if ((slashIndex == -1 || slashIndex > index)) {
            offset = index + 1;
        } else {
            return readFieldNameHashCode0();
        }

        int name_len = index - start;
        long nameHashCode = Fnv.hashCode64(bytes, start, name_len, true);
        this.nameEnd = index;

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
        this.ch = (char) ch;

        this.nameAscii = true;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    @Override
    public long readFieldNameHashCode(int min, int max) {
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        if (ch == '/') {
            skipComment();
            ch = this.ch;
        }
        if (ch == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }
        if (ch != '"' && ch != '\'') {
            return readFieldNameHashCodeError(nameBegin, ch);
        }

        int offset = this.nameBegin = this.offset, end = this.end;
        int start = offset;

        int index;
        if (INDEX_OF_CHAR_LATIN1 == null) {
            index = IOUtils.indexOfQuoteV(bytes, ch, offset, end);
        } else {
            try {
                index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
            }
            catch (Throwable e) {
                throw new JSONException(e.getMessage());
            }
        }
        if (index == -1) {
            throw error("invalid escape character EOI");
        }
        int slashIndex = indexOfSlash(this, bytes, offset, end);
        if ((slashIndex == -1 || slashIndex > index)) {
            offset = index + 1;
        } else {
            return readFieldNameHashCode0();
        }

        long nameHashCode;
        int name_len = index - start;
        if (name_len < min || name_len > max) {
            nameHashCode = Long.MAX_VALUE;
        } else {
            nameHashCode = Fnv.hashCode64(bytes, start, name_len, nameAscii);
        }
        this.nameEnd = index;

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
        this.ch = (char) ch;

        this.nameAscii = true;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
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
                        ch = hexDigit4(bytes, offset, end);
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

            nameValue = (((long) ch) << (i << 3)) | nameValue;

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
                            ch = hexDigit4(bytes, offset, end);
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
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        if (ch == '/') {
            skipComment();
            ch = this.ch;
        }
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        int offset = this.nameBegin = this.offset, end = this.end;
        int start = offset;

        int index;
        if (INDEX_OF_CHAR_LATIN1 == null) {
            index = IOUtils.indexOfQuoteV(bytes, ch, offset, end);
        } else {
            try {
                index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
            }
            catch (Throwable e) {
                throw new JSONException(e.getMessage());
            }
        }
        if (index == -1) {
            throw error("invalid escape character EOI");
        }
        int slashIndex = indexOfSlash(this, bytes, offset, end);
        if ((slashIndex == -1 || slashIndex > index)) {
            offset = index + 1;
        } else {
            return readValueHashCode0();
        }

        int name_len = index - start;
        long nameHashCode = Fnv.hashCode64(bytes, start, name_len, true);
        this.nameEnd = index;

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = ch == ',') {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.offset = offset;
        this.ch = (char) ch;

        this.nameAscii = true;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    private final long readValueHashCode0() {
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
            ch = bytes[offset] & 0xff;

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
                        ch = hexDigit4(bytes, offset + 1, end);
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

            nameValue = (((long) ch) << (i << 3)) | nameValue;
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
                            ch = hexDigit4(bytes, offset + 1, end);
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
        byte quote = '"';
        final byte[] bytes = this.bytes;
        int offset = nameBegin, end = this.end;
        if (offset > 0 && bytes[offset - 1] == '\'') {
            quote = '\'';
        }
        long nameValue = 0;
        for (int i = 0; offset < end; offset++) {
            int c = bytes[offset];

            if (c == '\\') {
                c = bytes[++offset];
                switch (c) {
                    case 'u': {
                        c = hexDigit4(bytes, offset + 1, end);
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
            } else if (c == quote) {
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

            nameValue = (((long) c) << (i << 3)) | nameValue;
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
                        c = hexDigit4(bytes, offset + 1, end);
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
            } else if (c == quote) {
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
    public final String getFieldName() {
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
                            int ch = hexDigit4(bytes, offset + 1, end);
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
                        ch = (char) hexDigit4(bytes, offset + 1, end);
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
    public final String readFieldName() {
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
                if (offset < end) {
                    c = bytes[offset];
                } else {
                    c = EOI;
                }

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
    public final String readString() {
        int ch = this.ch;
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;

            int offset = this.offset;
            final int start = offset, end = this.end;

            int index;
            if (INDEX_OF_CHAR_LATIN1 == null) {
                index = IOUtils.indexOfQuoteV(bytes, ch, offset, end);
            } else {
                try {
                    index = (int) INDEX_OF_CHAR_LATIN1.invokeExact(bytes, ch, offset, end);
                }
                catch (Throwable e) {
                    throw new JSONException(e.getMessage());
                }
            }
            if (index == -1) {
                throw error("invalid escape character EOI");
            }
            int slashIndex = indexOfSlash(this, bytes, offset, end);
            if (slashIndex == -1 || slashIndex > index) {
                offset = index + 1;
            } else {
                return readEscaped(bytes, slashIndex, start, end, slashIndex - offset, ch);
            }

            String str = STRING_CREATOR_JDK11 != null
                    ? STRING_CREATOR_JDK11.apply(Arrays.copyOfRange(bytes, start, index), LATIN1)
                    : new String(bytes, start, index - start, StandardCharsets.ISO_8859_1);
            long features = context.features;
            if ((features & MASK_TRIM_STRING) != 0) {
                str = str.trim();
            }
            if ((features & MASK_EMPTY_STRING_AS_NULL) != 0 && str.isEmpty()) {
                str = null;
            }

            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : bytes[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) (ch & 0xFF);
            this.offset = offset;
            return str;
        } else if (ch == 'n') {
            readNull();
            return null;
        }

        return readStringNotMatch();
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
                            c = (char) hexDigit4(bytes, offset + 1, end);
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

    final String readEscaped(byte[] bytes, int offset, int start, int end, int valueLength, int quote) {
        for (;;) {
            if (offset >= end) {
                throw error("invalid escape character EOI");
            }

            byte c = bytes[offset];
            if (c == '\\') {
                valueLength++;
                c = bytes[offset + 1];
                offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                continue;
            }

            if (c == quote) {
                break;
            }
            offset++;
            valueLength++;
        }

        char[] buf = new char[valueLength];
        offset = readEscaped(bytes, start, quote, buf);
        String str = new String(buf);

        long features = context.features;
        if ((features & (MASK_TRIM_STRING | MASK_EMPTY_STRING_AS_NULL)) != 0) {
            str = stringValue(str, features);
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

        this.ch = (char) (ch & 0xFF);
        this.offset = offset;
        return str;
    }

    protected final int readEscaped(byte[] bytes, int offset, int quote, char[] buf) {
        for (int i = 0; ; ++i) {
            char c = (char) (bytes[offset] & 0xff);
            if (c == '\\') {
                c = (char) bytes[++offset];
                switch (c) {
                    case 'u': {
                        c = (char) hexDigit4(bytes, offset + 1, end);
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
                    case 'b':
                        c = '\b';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == quote) {
                break;
            }
            buf[i] = c;
            offset++;
        }
        return offset;
    }

    static JSONReaderASCII of(Context ctx, String str, byte[] bytes, int offset, int length) {
        return new JSONReaderASCII(ctx, str, bytes, offset, length);
    }

    static JSONReaderASCII of(Context ctx, InputStream is) {
        return new JSONReaderASCII(ctx, is);
    }
}

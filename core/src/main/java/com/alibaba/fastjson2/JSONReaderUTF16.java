package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.DateUtils.localDateTime;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK8;
import static com.alibaba.fastjson2.util.UUIDUtils.parse4Nibbles;
import static java.time.ZoneOffset.UTC;

class JSONReaderUTF16
        extends JSONReader {
    protected final String str;
    protected final char[] chars;
    protected final int length;
    protected final int start;
    protected final int end;

    private int nameBegin;
    private int nameEnd;
    private int nameLength;

    private int referenceBegin;

    private Closeable input;
    private int cacheIndex = -1;
    protected boolean csv;

    JSONReaderUTF16(Context ctx, byte[] bytes, int offset, int length) {
        super(ctx);

        this.str = null;
        this.chars = new char[length / 2];
        int j = 0;
        int bytesEnd = offset + length;
        for (int i = offset; i < bytesEnd; i += 2, ++j) {
            byte c0 = bytes[i];
            byte c1 = bytes[i + 1];
            chars[j] = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
        }
        this.start = offset;
        this.end = this.length = j;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[this.offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[this.offset];
            }
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[this.offset];
            }
            this.offset++;
        }

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    @Override
    public byte[] readHex() {
        next();
        if (ch != '\'') {
            throw new JSONException("illegal state. " + ch);
        }
        int start = offset;
        offset++;

        for (; ; ) {
            ch = chars[offset++];
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                // continue;
            } else if (ch == '\'') {
                ch = chars[offset++];
                break;
            } else {
                throw new JSONException("illegal state. " + ch);
            }
        }

        int len = offset - start - 2;
        if (len == 0) {
            return new byte[0];
        }

        if (len % 2 != 0) {
            throw new JSONException("illegal state. " + len);
        }

        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < bytes.length; ++i) {
            char c0 = chars[start + i * 2];
            char c1 = chars[start + i * 2 + 1];

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            bytes[i] = (byte) ((b0 << 4) | b1);
        }

        nextIfMatch(',');

        return bytes;
    }

    @Override
    public boolean isReference() {
        if (ch != '{') {
            return false;
        }

        final int start = this.offset;
        if (offset == end) {
            return false;
        }

        ch = chars[this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = chars[offset];
        }

        char quote = ch;
        if (quote != '"' && quote != '\'' || this.offset + 5 >= end) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        if (chars[offset + 1] != '$'
                || chars[offset + 2] != 'r'
                || chars[offset + 3] != 'e'
                || chars[offset + 4] != 'f'
                || chars[offset + 5] != quote
                || offset + 6 >= end
        ) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        offset += 6;
        ch = chars[this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = chars[offset];
        }

        if (ch != ':' || offset + 1 >= end) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        ch = chars[++this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = chars[offset];
        }

        if (ch != quote) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        this.referenceBegin = offset;
        this.offset = start;
        this.ch = '{';
        return true;
    }

    @Override
    public String readReference() {
        if (referenceBegin == end) {
            return null;
        }
        this.offset = referenceBegin;
        this.ch = chars[offset++];

        String reference = readString();

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.ch = EOI;
                return reference;
            }
            ch = chars[offset];
        }

        if (ch != '}') {
            throw new JSONException("illegal reference : " + reference);
        }

        if (offset == end) {
            ch = EOI;
        } else {
            ch = chars[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (comma = (ch == ',')) {
            this.ch = chars[this.offset++];
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[offset++];
                    }
                }
            }
        }

        return reference;
    }

    JSONReaderUTF16(Context ctx, Reader input) {
        super(ctx);
        this.input = input;

        cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
        char[] chars = JSONFactory.allocateCharArray(cacheIndex);

        if (chars == null) {
            chars = new char[8192];
        }
        int off = 0;
        try {
            for (; ; ) {
                int n = input.read(chars, off, chars.length - off);
                if (n == -1) {
                    break;
                }
                off += n;

                if (off == chars.length) {
                    int oldCapacity = chars.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    chars = Arrays.copyOf(chars, newCapacity);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }

        this.str = null;
        this.chars = chars;
        this.offset = 0;
        this.length = off;
        this.start = 0;
        this.end = length;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[this.offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[this.offset];
            }
            this.offset++;
        }

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    JSONReaderUTF16(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx);

        this.str = str;
        this.chars = chars;
        this.offset = offset;
        this.length = length;
        this.start = offset;
        this.end = offset + length;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[this.offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[this.offset];
            }
            this.offset++;
        }

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    JSONReaderUTF16(Context ctx, InputStream input) {
        super(ctx);
        this.input = input;
        final int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
        byte[] bytes = JSONFactory.allocateByteArray(cacheIndex);

        char[] chars;
        try {
            int off = 0;
            for (; ; ) {
                int n = input.read(bytes, off, bytes.length - off);
                if (n == -1) {
                    break;
                }
                off += n;

                if (off == bytes.length) {
                    bytes = Arrays.copyOf(bytes, bytes.length + 8192);
                }
            }

            if (off % 2 == 1) {
                throw new JSONException("illegal input utf16 bytes, length " + off);
            }

            chars = new char[off / 2];
            for (int i = 0, j = 0; i < off; i += 2, ++j) {
                byte c0 = bytes[i];
                byte c1 = bytes[i + 1];
                chars[j] = (char) ((c1 & 0xff) | ((c0 & 0xff) << 8));
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        } finally {
            JSONFactory.releaseByteArray(cacheIndex, bytes);
        }

        this.str = null;
        this.chars = chars;
        this.offset = 0;
        this.length = chars.length;
        this.start = 0;
        this.end = length;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[this.offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[this.offset];
            }
            this.offset++;
        }

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    @Override
    public boolean nextIfMatch(char ch) {
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = chars[offset++];
            }
        }

        if (this.ch != ch) {
            return false;
        }
        comma = ch == ',';

        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        this.ch = chars[offset];
        while (this.ch == '\0' || (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = chars[offset];
        }
        offset++;
        return true;
    }

    @Override
    public boolean nextIfNullOrEmptyString() {
        final char first = this.ch;
        if (first == 'n' && offset + 2 < end && chars[offset] == 'u') {
            this.readNull();
            return true;
        }

        if ((first != '"' && first != '\'') || offset >= end || chars[offset] != first) {
            return false;
        }
        offset++;
        this.ch = offset == end ? EOI : chars[offset];

        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = chars[offset];
        }

        if (comma = (ch == ',')) {
            ch = chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        this.ch = chars[offset];
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            this.ch = chars[offset];
        }
        offset++;
        return true;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2) {
        if (ch != c0) {
            return false;
        }

        int offset2 = offset + 2;
        if (offset2 > end || chars[offset] != c1 || chars[offset + 1] != c2) {
            return false;
        }

        if (offset2 == end) {
            offset = offset2;
            this.ch = EOI;
            return true;
        }

        int offset = offset2;
        char ch = chars[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset];
        }
        if (offset == offset2) {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        if (ch != c0) {
            return false;
        }

        int offset3 = offset + 3;
        if (offset3 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3) {
            return false;
        }

        if (offset3 == end) {
            offset = offset3;
            this.ch = EOI;
            return true;
        }

        int offset = offset3;
        char ch = chars[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset];
        }

        if (offset == offset3 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        if (ch != c0) {
            return false;
        }

        int offset4 = offset + 4;
        if (offset4 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3
                || chars[offset + 3] != c4) {
            return false;
        }

        if (offset4 == end) {
            offset = offset4;
            this.ch = EOI;
            return true;
        }

        int offset = offset4;
        char ch = chars[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset];
        }

        if (offset == offset4 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        if (ch != c0) {
            return false;
        }

        int offset5 = offset + 5;
        if (offset5 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3
                || chars[offset + 3] != c4
                || chars[offset + 4] != c5) {
            return false;
        }

        if (offset5 == end) {
            offset = offset5;
            this.ch = EOI;
            return true;
        }

        int offset = offset5;
        char ch = chars[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset];
        }

        if (offset == offset5 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public boolean nextIfSet() {
        if (ch == 'S'
                && offset + 1 < end
                && chars[offset] == 'e'
                && chars[offset + 1] == 't') {
            offset += 2;
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = chars[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = chars[offset++];
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean nextIfInfinity() {
        if (ch == 'I'
                && offset + 6 < end
                && chars[offset] == 'n'
                && chars[offset + 1] == 'f'
                && chars[offset + 2] == 'i'
                && chars[offset + 3] == 'n'
                && chars[offset + 4] == 'i'
                && chars[offset + 5] == 't'
                && chars[offset + 6] == 'y'
        ) {
            offset += 7;
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = chars[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = chars[offset++];
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void next() {
        if (offset >= end) {
            ch = EOI;
            return;
        }

        ch = chars[offset];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return;
            }
            ch = chars[offset];
        }
        offset++;
    }

    @Override
    public long readFieldNameHashCodeUnquote() {
        this.nameEscape = false;
        this.nameBegin = this.offset - 1;
        char first = ch;

        long nameValue = 0;
        if (MIXED_HASH_ALGORITHM) {
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

                if (ch == '\\') {
                    nameEscape = true;
                    ch = chars[offset++];
                    switch (ch) {
                        case 'u': {
                            char c1 = chars[offset++];
                            char c2 = chars[offset++];
                            char c3 = chars[offset++];
                            char c4 = chars[offset++];
                            ch = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[offset++];
                            char c2 = chars[offset++];
                            ch = char2(c1, c2);
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

                ch = offset >= end
                        ? EOI
                        : chars[offset++];
            }
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
                    ch = chars[offset++];
                    switch (ch) {
                        case 'u': {
                            char c1 = chars[offset++];
                            char c2 = chars[offset++];
                            char c3 = chars[offset++];
                            char c4 = chars[offset++];
                            ch = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[offset++];
                            char c2 = chars[offset++];
                            ch = char2(c1, c2);
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
                        : chars[offset++];
            }
        }

        if (ch == ':') {
            if (offset == end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset == end) {
                    ch = EOI;
                    break;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        return hashCode;
    }

    @Override
    public long readFieldNameHashCode() {
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
                errorMsg = "illegal fieldName input" + ch;
            }

            throw new JSONException(info(errorMsg));
        }

        final char quote = ch;

        this.stringValue = null;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;
        if (MIXED_HASH_ALGORITHM) {
            if (offset + 9 < end) {
                char c0 = chars[offset];
                char c1 = chars[offset + 1];
                char c2 = chars[offset + 2];
                char c3 = chars[offset + 3];
                char c4 = chars[offset + 4];
                char c5 = chars[offset + 5];
                char c6 = chars[offset + 6];
                char c7 = chars[offset + 7];
                char c8 = chars[offset + 8];

                if (c0 == quote) {
                    nameValue = 0;
                } else if (c1 == quote && c0 != 0 && c0 != '\\' && c0 <= 0xFF) {
                    nameValue = (byte) c0;
                    this.nameLength = 1;
                    this.nameEnd = offset + 1;
                    offset += 2;
                } else if (c2 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF
                ) {
                    nameValue = (((byte) c1) << 8)
                            + c0;
                    this.nameLength = 2;
                    this.nameEnd = offset + 2;
                    offset += 3;
                } else if (c3 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF) {
                    nameValue
                            = (((byte) c2) << 16)
                            + (c1 << 8)
                            + c0;
                    this.nameLength = 3;
                    this.nameEnd = offset + 3;
                    offset += 4;
                } else if (c4 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF
                ) {
                    nameValue
                            = (((byte) c3) << 24)
                            + (c2 << 16)
                            + (c1 << 8)
                            + c0;
                    this.nameLength = 4;
                    this.nameEnd = offset + 4;
                    offset += 5;
                } else if (c5 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF
                ) {
                    nameValue
                            = (((long) ((byte) c4)) << 32)
                            + (((long) c3) << 24)
                            + (((long) c2) << 16)
                            + (((long) c1) << 8)
                            + (long) c0;
                    this.nameLength = 5;
                    this.nameEnd = offset + 5;
                    offset += 6;
                } else if (c6 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF
                ) {
                    nameValue
                            = (((long) ((byte) c5)) << 40)
                            + (((long) c4) << 32)
                            + (((long) c3) << 24)
                            + (((long) c2) << 16)
                            + (((long) c1) << 8)
                            + (long) c0;
                    this.nameLength = 6;
                    this.nameEnd = offset + 6;
                    offset += 7;
                } else if (c7 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\' && c6 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF && c6 <= 0xFF
                ) {
                    nameValue
                            = (((long) ((byte) c6)) << 48)
                            + (((long) c5) << 40)
                            + (((long) c4) << 32)
                            + (((long) c3) << 24)
                            + (((long) c2) << 16)
                            + (((long) c1) << 8)
                            + (long) c0;
                    this.nameLength = 7;
                    this.nameEnd = offset + 7;
                    offset += 8;
                } else if (c8 == quote && c0 != 0
                        && c0 != '\\' && c1 != '\\' && c2 != '\\' && c3 != '\\' && c4 != '\\' && c5 != '\\' && c6 != '\\' && c7 != '\\'
                        && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF && c6 <= 0xFF && c7 <= 0xFF
                ) {
                    nameValue
                            = (((long) ((byte) c7)) << 56)
                            + (((long) c6) << 48)
                            + (((long) c5) << 40)
                            + (((long) c4) << 32)
                            + (((long) c3) << 24)
                            + (((long) c2) << 16)
                            + (((long) c1) << 8)
                            + (long) c0;
                    this.nameLength = 8;
                    this.nameEnd = offset + 8;
                    offset += 9;
                }
            }
        }

        if (MIXED_HASH_ALGORITHM && nameValue == 0) {
            for (int i = 0; offset < end; offset++, i++) {
                char c = chars[offset];

                if (c == quote) {
                    if (i == 0) {
                        offset = this.nameBegin;
                        break;
                    }

                    this.nameLength = i;
                    this.nameEnd = offset;
                    offset++;
                    break;
                }

                if (c == '\\') {
                    nameEscape = true;
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            char c3 = chars[++offset];
                            char c4 = chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            c = char2(c1, c2);
                            break;
                        }
                        case '\\':
                        case '"':
                        default:
                            c = char1(c);
                            break;
                    }
                }

                if (c > 0xFF || i >= 8 || (i == 0 && c == 0)) {
                    nameValue = 0;
                    offset = this.nameBegin;
                    break;
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
                        nameValue = (((long) (byte) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                        break;
                    case 6:
                        nameValue = (((long) (byte) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                        break;
                    case 7:
                        nameValue = (((long) (byte) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
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
                char c = chars[offset];
                if (c == '\\') {
                    nameEscape = true;
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            char c3 = chars[++offset];
                            char c4 = chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            c = char2(c1, c2);
                            break;
                        }
                        case '\\':
                        case '"':
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
                    break;
                }

                offset++;
                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        }

        char c;
        if (offset < end) {
            c = chars[offset];

            while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                offset++;
                c = chars[offset];
            }
        } else {
            c = EOI;
        }

        if (c != ':') {
            throw new JSONException(info("expect ':', but " + c));
        }

        offset++;
        if (offset == end) {
            c = EOI;
        } else {
            c = chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        final char quote = ch;

        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;
        if (MIXED_HASH_ALGORITHM) {
            for (int i = 0; offset < end; offset++, i++) {
                char c = chars[offset];

                if (c == quote) {
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

                if (c == '\\') {
                    nameEscape = true;
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            char c3 = chars[++offset];
                            char c4 = chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            c = char2(c1, c2);
                            break;
                        }
                        case '\\':
                        case '"':
                        default:
                            c = char1(c);
                            break;
                    }
                }

                if (c > 0xFF || i >= 8 || (i == 0 && c == 0)) {
                    nameValue = 0;
                    offset = this.nameBegin;
                    break;
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
                        nameValue = (((long) (byte) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                        break;
                    case 6:
                        nameValue = (((long) (byte) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                        break;
                    case 7:
                        nameValue = (((long) (byte) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
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
                char c = chars[offset];
                if (c == '\\') {
                    nameEscape = true;
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            char c3 = chars[++offset];
                            char c4 = chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            char c1 = chars[++offset];
                            char c2 = chars[++offset];
                            c = char2(c1, c2);
                            break;
                        }
                        case '\\':
                        case '"':
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
                    break;
                }

                offset++;
                hashCode ^= c;
                hashCode *= Fnv.MAGIC_PRIME;
            }
        }

        char c;
        if (offset == end) {
            c = EOI;
        } else {
            c = chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        if (comma = (c == ',')) {
            offset++;
            if (offset == end) {
                c = EOI;
            } else {
                c = chars[offset];
            }

            while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                offset++;
                c = chars[offset];
            }
        }

        this.offset = offset + 1;
        this.ch = c;

        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        int offset = nameBegin;

        if (MIXED_HASH_ALGORITHM) {
            long nameValue = 0;
            for (int i = 0; offset < end; offset++) {
                char c = chars[offset];

                if (c == '\\') {
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            int c1 = chars[++offset];
                            int c2 = chars[++offset];
                            int c3 = chars[++offset];
                            int c4 = chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            int c1 = chars[++offset];
                            int c2 = chars[++offset];
                            c = char2(c1, c2);
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

                if (c > 0xFF || i >= 8 || (i == 0 && c == 0)) {
                    nameValue = 0;
                    offset = this.nameBegin;
                    break;
                }

                if (c == '_' || c == '-') {
                    char c1 = chars[offset + 1];
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
                        nameValue = (((long) (byte) c) << 40L) + (nameValue & 0xFFFFFFFFFFL);
                        break;
                    case 6:
                        nameValue = (((long) (byte) c) << 48L) + (nameValue & 0xFFFFFFFFFFFFL);
                        break;
                    case 7:
                        nameValue = (((long) (byte) c) << 56L) + (nameValue & 0xFFFFFFFFFFFFFFL);
                        break;
                    default:
                        break;
                }
                ++i;
            }

            if (nameValue != 0) {
                return nameValue;
            }
        }

        long hashCode = Fnv.MAGIC_HASH_CODE;
        for (; offset < end; ) {
            char c = chars[offset];

            if (c == '\\') {
                c = chars[++offset];
                switch (c) {
                    case 'u': {
                        int c1 = chars[++offset];
                        int c2 = chars[++offset];
                        int c3 = chars[++offset];
                        int c4 = chars[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = chars[++offset];
                        int c2 = chars[++offset];
                        c = char2(c1, c2);
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
            if (c == '_' || c == '-') {
                char c1 = chars[offset];
                if (c1 != '"' && c1 != '\'' && c1 != c) {
                    continue;
                }
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
            if (this.str != null) {
                return this.str.substring(nameBegin, nameEnd);
            } else {
                return new String(chars, nameBegin, nameEnd - nameBegin);
            }
        }

        char[] chars = new char[nameLength];
        int offset = nameBegin;
        for (int i = 0; offset < nameEnd; ++i) {
            char c = this.chars[offset];

            if (c == '\\') {
                c = this.chars[++offset];
                switch (c) {
                    case 'u': {
                        int c1 = this.chars[++offset];
                        int c2 = this.chars[++offset];
                        int c3 = this.chars[++offset];
                        int c4 = this.chars[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = this.chars[++offset];
                        int c2 = this.chars[++offset];
                        c = char2(c1, c2);
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
        if (ch == '/') {
            skipLineComment();
        }

        if (ch != '"' && ch != '\'') {
            return null;
        }

        final char quote = ch;

        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;
        for (int i = 0; offset < end; ++i) {
            int c = chars[offset];
            if (c == '\\') {
                nameEscape = true;
                c = chars[++offset];
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
                if (offset < end) {
                    c = chars[offset];
                } else {
                    c = EOI;
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = chars[offset];
                }
                if (c != ':') {
                    throw new JSONException("syntax error : " + offset);
                }

                offset++;
                if (offset == end) {
                    c = EOI;
                } else {
                    c = chars[offset];
                }

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = chars[offset];
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
            int c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12, c13, c14, c15;
            switch (nameLength) {
                case 1:
                    c0 = chars[nameBegin];
                    if ((c0 & 0xFF) == c0) {
                        nameValue0 = (byte) c0;
                    }
                    break;
                case 2:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    if ((c0 & 0xFF) == c0 && (c1 & 0xFF) == c1) {
                        nameValue0
                                = (((byte) c1) << 8)
                                + c0;
                    }
                    break;
                case 3:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2) {
                        nameValue0
                                = (c2 << 16)
                                + (c1 << 8)
                                + c0;
                    }
                    break;
                case 4:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3) {
                        nameValue0
                                = (c3 << 24)
                                + (c2 << 16)
                                + (c1 << 8)
                                + c0;
                    }
                    break;
                case 5:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4) {
                        nameValue0
                                = (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                    }
                    break;
                case 6:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5) {
                        nameValue0
                                = (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                    }
                    break;
                case 7:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6) {
                        nameValue0
                                = (((long) c6) << 48)
                                + (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                    }
                    break;
                case 8:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7) {
                        nameValue0
                                = (((long) c7) << 56)
                                + (((long) c6) << 48)
                                + (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                    }
                    break;
                case 9:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8) {
                        nameValue0 = c0;
                        nameValue1
                                = (((long) c8) << 56)
                                + (((long) c7) << 48)
                                + (((long) c6) << 40)
                                + (((long) c5) << 32)
                                + (((long) c4) << 24)
                                + (((long) c3) << 16)
                                + (((long) c2) << 8)
                                + ((long) c1);
                    }
                    break;
                case 10:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9) {
                        nameValue0 = c1 << 8
                                + c0;
                        nameValue1
                                = (((long) c9) << 56)
                                + (((long) c8) << 48)
                                + (((long) c7) << 40)
                                + (((long) c6) << 32)
                                + (((long) c5) << 24)
                                + (((long) c4) << 16)
                                + (((long) c3) << 8)
                                + ((long) c2);
                    }
                    break;
                case 11:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10) {
                        nameValue0
                                = c2 << 16
                                + (c1 << 8)
                                + c0;
                        nameValue1
                                = (((long) c10) << 56)
                                + (((long) c9) << 48)
                                + (((long) c8) << 40)
                                + (((long) c7) << 32)
                                + (((long) c6) << 24)
                                + (((long) c5) << 16)
                                + (((long) c4) << 8)
                                + ((long) c3);
                    }
                    break;
                case 12:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    c11 = chars[nameBegin + 11];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10
                            && (c11 & 0xFF) == c11) {
                        nameValue0
                                = c3 << 24
                                + (c2 << 16)
                                + (c1 << 8)
                                + c0;
                        nameValue1
                                = (((long) c11) << 56)
                                + (((long) c10) << 48)
                                + (((long) c9) << 40)
                                + (((long) c8) << 32)
                                + (((long) c7) << 24)
                                + (((long) c6) << 16)
                                + (((long) c5) << 8)
                                + ((long) c4);
                    }
                    break;
                case 13:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    c11 = chars[nameBegin + 11];
                    c12 = chars[nameBegin + 12];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10
                            && (c11 & 0xFF) == c11
                            && (c12 & 0xFF) == c12) {
                        nameValue0
                                = (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                        nameValue1
                                = (((long) c12) << 56)
                                + (((long) c11) << 48)
                                + (((long) c10) << 40)
                                + (((long) c9) << 32)
                                + (((long) c8) << 24)
                                + (((long) c7) << 16)
                                + (((long) c6) << 8)
                                + ((long) c5);
                    }
                    break;
                case 14:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    c11 = chars[nameBegin + 11];
                    c12 = chars[nameBegin + 12];
                    c13 = chars[nameBegin + 13];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10
                            && (c11 & 0xFF) == c11
                            && (c12 & 0xFF) == c12
                            && (c13 & 0xFF) == c13) {
                        nameValue0
                                = (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                        nameValue1
                                = (((long) c13) << 56)
                                + (((long) c12) << 48)
                                + (((long) c11) << 40)
                                + (((long) c10) << 32)
                                + (((long) c9) << 24)
                                + (((long) c8) << 16)
                                + (((long) c7) << 8)
                                + ((long) c6);
                    }
                    break;
                case 15:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    c11 = chars[nameBegin + 11];
                    c12 = chars[nameBegin + 12];
                    c13 = chars[nameBegin + 13];
                    c14 = chars[nameBegin + 14];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10
                            && (c11 & 0xFF) == c11
                            && (c12 & 0xFF) == c12
                            && (c13 & 0xFF) == c13
                            && (c14 & 0xFF) == c14) {
                        nameValue0
                                = (((long) c6) << 48)
                                + (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                        nameValue1
                                = (((long) c14) << 56)
                                + (((long) c13) << 48)
                                + (((long) c12) << 40)
                                + (((long) c11) << 32)
                                + (((long) c10) << 24)
                                + (((long) c9) << 16)
                                + (((long) c8) << 8)
                                + ((long) c7);
                    }
                    break;
                case 16:
                    c0 = chars[nameBegin];
                    c1 = chars[nameBegin + 1];
                    c2 = chars[nameBegin + 2];
                    c3 = chars[nameBegin + 3];
                    c4 = chars[nameBegin + 4];
                    c5 = chars[nameBegin + 5];
                    c6 = chars[nameBegin + 6];
                    c7 = chars[nameBegin + 7];
                    c8 = chars[nameBegin + 8];
                    c9 = chars[nameBegin + 9];
                    c10 = chars[nameBegin + 10];
                    c11 = chars[nameBegin + 11];
                    c12 = chars[nameBegin + 12];
                    c13 = chars[nameBegin + 13];
                    c14 = chars[nameBegin + 14];
                    c15 = chars[nameBegin + 15];
                    if ((c0 & 0xFF) == c0
                            && (c1 & 0xFF) == c1
                            && (c2 & 0xFF) == c2
                            && (c3 & 0xFF) == c3
                            && (c4 & 0xFF) == c4
                            && (c5 & 0xFF) == c5
                            && (c6 & 0xFF) == c6
                            && (c7 & 0xFF) == c7
                            && (c8 & 0xFF) == c8
                            && (c9 & 0xFF) == c9
                            && (c10 & 0xFF) == c10
                            && (c11 & 0xFF) == c11
                            && (c12 & 0xFF) == c12
                            && (c13 & 0xFF) == c13
                            && (c14 & 0xFF) == c14
                            && (c15 & 0xFF) == c15) {
                        nameValue0
                                = (((long) c7) << 56)
                                + (((long) c6) << 48)
                                + (((long) c5) << 40)
                                + (((long) c4) << 32)
                                + (((long) c3) << 24)
                                + (((long) c2) << 16)
                                + (((long) c1) << 8)
                                + ((long) c0);
                        nameValue1
                                = (((long) c15) << 56)
                                + (((long) c14) << 48)
                                + (((long) c13) << 40)
                                + (((long) c12) << 32)
                                + (((long) c11) << 24)
                                + (((long) c10) << 16)
                                + (((long) c9) << 8)
                                + ((long) c8);
                    }
                    break;
                default:
                    break;
            }

            if (nameValue0 != -1) {
                if (nameValue1 != -1) {
                    int indexMask = ((int) nameValue1) & (NAME_CACHE2.length - 1);
                    NameCacheEntry2 entry = NAME_CACHE2[indexMask];
                    if (entry == null) {
                        String name;
                        if (this.str != null) {
                            name = this.str.substring(nameBegin, nameEnd);
                        } else {
                            name = new String(chars, nameBegin, nameEnd - nameBegin);
                        }
                        NAME_CACHE2[indexMask] = new NameCacheEntry2(name, nameValue0, nameValue1);
                        return name;
                    } else if (entry.value0 == nameValue0 && entry.value0 == nameValue1) {
                        return entry.name;
                    }
                } else {
                    int indexMask = ((int) nameValue0) & (NAME_CACHE.length - 1);
                    NameCacheEntry entry = NAME_CACHE[indexMask];
                    if (entry == null) {
                        String name;
                        if (this.str != null) {
                            name = this.str.substring(nameBegin, nameEnd);
                        } else {
                            name = new String(chars, nameBegin, nameEnd - nameBegin);
                        }
                        NAME_CACHE[indexMask] = new NameCacheEntry(name, nameValue0);
                        return name;
                    } else if (entry.value == nameValue0) {
                        return entry.name;
                    }
                }
            }

            if (this.str != null) {
                return this.str.substring(nameBegin, nameEnd);
            } else {
                return new String(chars, nameBegin, nameEnd - nameBegin);
            }
        }

        return getFieldName();
    }

    @Override
    public boolean skipName() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException(info("not support unquoted name"));
        }
        char quote = ch;

        int offset = this.offset;
        for (; ; ) {
            char c = chars[offset];
            if (c == '\\') {
                c = chars[++offset];
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
                offset++;
                c = chars[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = chars[offset];
                }
                if (c != ':') {
                    throw new JSONException("syntax error, expect ',', but '" + c + "'");
                }

                offset++;
                c = chars[offset];

                while (c <= ' ' && ((1L << c) & SPACE) != 0) {
                    offset++;
                    c = chars[offset];
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
            ch = chars[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else if (ch == '+') {
            ch = chars[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            int intValue10 = intValue * 10 + (ch - '0');
            if (intValue10 < intValue) {
                overflow = true;
                break;
            } else {
                intValue = intValue10;
            }
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset++];
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
                if (valueType == JSON_TYPE_NULL && (context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("int value not support input null"));
                }

                return getInt32Value();
            }
        }

        if (quote != 0) {
            wasNull = firstOffset + 1 == offset;
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    valueType = JSON_TYPE_INT8;
                    break;
                case 'S':
                    valueType = JSON_TYPE_INT16;
                    break;
                case 'L':
                    valueType = JSON_TYPE_INT64;
                    break;
                case 'F':
                    valueType = JSON_TYPE_FLOAT;
                    break;
                case 'D':
                    valueType = JSON_TYPE_DOUBLE;
                    break;
                default:
                    break;
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                this.ch = offset == end ? EOI : chars[this.offset++];
                // next inline
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[offset++];
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
            ch = chars[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                    nextIfMatch(',');
                }
                return null;
            }
        } else if (csv && (ch == ',' || ch == '\r' || ch == '\n')) {
            return null;
        }

        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else if (ch == '+') {
            ch = chars[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            int intValue10 = intValue * 10 + (ch - '0');
            if (intValue10 < intValue) {
                overflow = true;
                break;
            } else {
                intValue = intValue10;
            }
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = chars[offset++];
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
            if (offset == end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    valueType = JSON_TYPE_INT8;
                    break;
                case 'S':
                    valueType = JSON_TYPE_INT16;
                    break;
                case 'L':
                    valueType = JSON_TYPE_INT64;
                    break;
                case 'F':
                    valueType = JSON_TYPE_FLOAT;
                    break;
                case 'D':
                    valueType = JSON_TYPE_DOUBLE;
                    break;
                default:
                    break;
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                if (this.offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = chars[this.offset++];
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
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
            ch = chars[offset++];
        }

        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else if (ch == '+') {
            ch = chars[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            long intValue10 = longValue * 10 + (ch - '0');
            if (intValue10 < longValue) {
                overflow = true;
                break;
            } else {
                longValue = intValue10;
            }
            if (offset >= end) {
                ch = EOI;
                break;
            }

            ch = chars[offset++];
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
                    throw new JSONException("long overflow, value " + bigInteger);
                }
            } else {
                return getInt64Value();
            }
        }

        if (quote != 0) {
            wasNull = firstOffset + 1 == offset;
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    valueType = JSON_TYPE_INT8;
                    break;
                case 'S':
                    valueType = JSON_TYPE_INT16;
                    break;
                case 'L':
                    valueType = JSON_TYPE_INT64;
                    break;
                case 'F':
                    valueType = JSON_TYPE_FLOAT;
                    break;
                case 'D':
                    valueType = JSON_TYPE_DOUBLE;
                    break;
                default:
                    break;
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                this.ch = offset == end ? EOI : chars[this.offset++];
                // next inline
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[offset++];
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
            ch = chars[offset++];
            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
                nextIfMatch(',');
                return null;
            }
        } else if (csv && (ch == ',' || ch == '\r' || ch == '\n')) {
            return null;
        }

        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else if (ch == '+') {
            ch = chars[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            long intValue10 = longValue * 10 + (ch - '0');
            if (intValue10 < longValue) {
                overflow = true;
                break;
            } else {
                longValue = intValue10;
            }

            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = chars[offset++];
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
            if (offset == end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    valueType = JSON_TYPE_INT8;
                    break;
                case 'S':
                    valueType = JSON_TYPE_INT16;
                    break;
                case 'L':
                    valueType = JSON_TYPE_INT64;
                    break;
                case 'F':
                    valueType = JSON_TYPE_FLOAT;
                    break;
                case 'D':
                    valueType = JSON_TYPE_DOUBLE;
                    break;
                default:
                    break;
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                // next inline
                if (this.offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = chars[this.offset++];
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
                    }
                }
            }
        }

        return negative ? -longValue : longValue;
    }

    @Override
    public double readDoubleValue() {
        this.wasNull = false;

        boolean value = false;
        double doubleValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else {
            if (ch == '+') {
                ch = chars[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = chars[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = chars[offset++];
            while (ch >= '0' && ch <= '9') {
                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = chars[offset++];
            }
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = chars[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = chars[offset++];
            } else if (ch == '+') {
                ch = chars[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = chars[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (chars[offset++] == 'u'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 'l'
                ) {
                    if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                        throw new JSONException(info("long value not support input null"));
                    }
                    wasNull = true;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 't') {
                if (chars[offset++] == 'r'
                        && chars[offset++] == 'u'
                        && chars[offset++] == 'e'
                ) {
                    value = true;
                    doubleValue = 1;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (chars[offset++] == 'a'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 's'
                        && chars[offset++] == 'e'
                ) {
                    doubleValue = 0;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == '{' && quote == 0) {
                Map<String, Object> obj = readObject();
                if (!obj.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            } else if (ch == '[' && quote == 0) {
                List array = readArray();
                if (!array.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            }
        }

        int len = offset - start;

        String str = null;
        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                str = readString();
            } else {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        if (!value) {
            if (str != null) {
                try {
                    doubleValue = Double.parseDouble(str);
                } catch (NumberFormatException ex) {
                    throw new JSONException(info(), ex);
                }
            } else {
                doubleValue = TypeUtils.parseDouble(chars, start - 1, len);
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                switch (ch) {
                    case 'B':
                        valueType = JSON_TYPE_INT8;
                        break;
                    case 'S':
                        valueType = JSON_TYPE_INT16;
                        break;
                    case 'L':
                        valueType = JSON_TYPE_INT64;
                        break;
                    case 'F':
                        valueType = JSON_TYPE_FLOAT;
                        break;
                    case 'D':
                        valueType = JSON_TYPE_DOUBLE;
                        break;
                    default:
                        break;
                }
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                // next inline
                if (this.offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = chars[this.offset++];
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
                    }
                }
            }
        }

        return doubleValue;
    }

    @Override
    public float readFloatValue() {
        this.wasNull = false;

        boolean value = false;
        float floatValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else {
            if (ch == '+') {
                ch = chars[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = chars[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = chars[offset++];
            while (ch >= '0' && ch <= '9') {
                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = chars[offset++];
            }
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = chars[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = chars[offset++];
            } else if (ch == '+') {
                ch = chars[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = chars[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (chars[offset++] == 'u'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 'l'
                ) {
                    if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                        throw new JSONException(info("long value not support input null"));
                    }

                    wasNull = true;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 't') {
                if (chars[offset++] == 'r'
                        && chars[offset++] == 'u'
                        && chars[offset++] == 'e'
                ) {
                    value = true;
                    floatValue = 1;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (chars[offset++] == 'a'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 's'
                        && chars[offset++] == 'e'
                ) {
                    floatValue = 0;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == '{' && quote == 0) {
                Map<String, Object> obj = readObject();
                if (!obj.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            } else if (ch == '[' && quote == 0) {
                List array = readArray();
                if (!array.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            }
        }

        int len = offset - start;

        String str = null;
        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                str = readString();
            } else {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        if (!value) {
            if (str != null) {
                try {
                    floatValue = Float.parseFloat(str);
                } catch (NumberFormatException ex) {
                    throw new JSONException(info(), ex);
                }
            } else {
                floatValue = TypeUtils.parseFloat(chars, start - 1, len);
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                switch (ch) {
                    case 'B':
                        valueType = JSON_TYPE_INT8;
                        break;
                    case 'S':
                        valueType = JSON_TYPE_INT16;
                        break;
                    case 'L':
                        valueType = JSON_TYPE_INT64;
                        break;
                    case 'F':
                        valueType = JSON_TYPE_FLOAT;
                        break;
                    case 'D':
                        valueType = JSON_TYPE_DOUBLE;
                        break;
                    default:
                        break;
                }
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                // next inline
                if (this.offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = chars[this.offset++];
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
                    }
                }
            }
        }

        return floatValue;
    }

    private void skipString() {
        char quote = this.ch;
        ch = chars[offset++];
        _for:
        for (; ; ) {
            if (ch == '\\') {
                if (offset >= end) {
                    throw new JSONException(info("illegal string, end"));
                }

                ch = chars[offset++];
                if (ch == '\\' || ch == '"') {
                    ch = chars[offset++];
                    continue;
                }
                if (ch == 'u') {
                    offset += 4;
                    ch = chars[offset++];
                    continue;
                }
                ch = char1(ch);
                continue;
            }

            if (ch == quote) {
                if (offset < end) {
                    ch = chars[offset++];
                } else {
                    ch = EOI;
                }
                break;
            }

            if (offset < end) {
                ch = chars[offset++];
            } else {
                ch = EOI;
                break;
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = chars[offset++];
        }

        if (comma = (ch == ',')) {
            if (offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    ch = EOI;
                    return;
                }
                ch = chars[offset];
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
            return new String(chars, nameBegin, length);
        }

        char[] chars = new char[nameLength];
        int offset = nameBegin;
        for (int i = 0; ; ++i) {
            char c = this.chars[offset];

            if (c == '\\') {
                c = this.chars[++offset];
                switch (c) {
                    case 'u': {
                        int c1 = this.chars[++offset];
                        int c2 = this.chars[++offset];
                        int c3 = this.chars[++offset];
                        int c4 = this.chars[++offset];
                        c = char4(c1, c2, c3, c4);
                        break;
                    }
                    case 'x': {
                        int c1 = this.chars[++offset];
                        int c2 = this.chars[++offset];
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
            char c = chars[offset];
            if (c == '\\') {
                valueEscape = true;
                c = chars[++offset];
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
                char c = this.chars[offset];
                if (c == '\\') {
                    c = this.chars[++offset];
                    switch (c) {
                        case 'u': {
                            int c1 = this.chars[++offset];
                            int c2 = this.chars[++offset];
                            int c3 = this.chars[++offset];
                            int c4 = this.chars[++offset];
                            c = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            int c1 = this.chars[++offset];
                            int c2 = this.chars[++offset];
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
            str = new String(chars, this.offset, offset - this.offset);
        }

        ++offset;
        int b;
        if (offset == end) {
            b = EOI;
        } else {
            b = chars[offset];
        }
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = chars[++offset];
        }

        if (comma = (b == ',')) {
            this.offset = offset + 1;

            // inline next
            ch = chars[this.offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (this.offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[this.offset++];
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
            {
                int i = 0;
                char c0 = 0, c1 = 0, c2 = 0, c3 = 0;

                // vector optimize
                boolean quoted = false;
                int upperBound = offset + ((end - offset) & ~3);
                while (offset < upperBound) {
                    c0 = chars[offset];
                    c1 = chars[offset + 1];
                    c2 = chars[offset + 2];
                    c3 = chars[offset + 3];
                    if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\') {
                        break;
                    }
                    if (c0 == quote || c1 == quote || c2 == quote || c3 == quote) {
                        quoted = true;
                        break;
                    }
                    offset += 4;
                    i += 4;
                }

                if (quoted) {
                    if (c0 == quote) {
                        // skip
                    } else if (c1 == quote) {
                        offset++;
                        i++;
                    } else if (c2 == quote) {
                        offset += 2;
                        i += 2;
                    } else if (c3 == quote) {
                        offset += 3;
                        i += 3;
                    }
                    valueLength = i;
                } else {
                    for (; ; ++i) {
                        if (offset >= end) {
                            throw new JSONException(info("invalid escape character EOI"));
                        }
                        char c = chars[offset];
                        if (c == '\\') {
                            valueEscape = true;
                            c = chars[++offset];
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
            }

            String str;
            if (valueEscape) {
                char[] chars = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    char c = this.chars[offset];
                    if (c == '\\') {
                        c = this.chars[++offset];
                        switch (c) {
                            case 'u': {
                                char c1 = this.chars[++offset];
                                char c2 = this.chars[++offset];
                                char c3 = this.chars[++offset];
                                char c4 = this.chars[++offset];
                                c = char4(c1, c2, c3, c4);
                                break;
                            }
                            case 'x': {
                                char c1 = this.chars[++offset];
                                char c2 = this.chars[++offset];
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

                if (STRING_CREATOR_JDK8 != null) {
                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else {
                    str = new String(chars);
                }
            } else {
                if (this.str != null && JVM_VERSION > 8) {
                    str = this.str.substring(this.offset, offset);
                } else {
                    str = new String(chars, this.offset, offset - this.offset);
                }
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            if (offset + 1 == end) {
                this.offset = end;
                this.ch = EOI;
                comma = false;
                return str;
            }

            int b = chars[++offset];
            while (b <= ' ' && ((1L << b) & SPACE) != 0) {
                b = chars[++offset];
            }

            if (comma = (b == ',')) {
                this.offset = offset + 1;

                // inline next
                ch = this.offset == end ? EOI : chars[this.offset++];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (this.offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[this.offset++];
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
                        throw new JSONValidException(info("illegal value"));
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
                for (; ; ) {
                    if (offset < end) {
                        ch = chars[offset++];
                    } else {
                        ch = EOI;
                        break;
                    }
                    if (ch == '}' || ch == ']' || ch == '{' || ch == '[') {
                        break;
                    }

                    if (ch == '\"' || ch == '\'') {
                        throw new JSONException("error, offset " + offset + ", char " + ch);
                    }

                    if (ch == ',') {
                        comma = true;
                        if (offset >= end) {
                            ch = EOI;
                            return;
                        }

                        ch = chars[offset];
                        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                            offset++;
                            if (offset >= end) {
                                ch = EOI;
                                return;
                            }
                            ch = chars[offset];
                        }
                        comma = true;
                        offset++;
                        break;
                    }
                }
                break;
            case 'S':
                if (nextIfSet()) {
                    skipValue();
                    break;
                }
                throw new JSONException("error, offset " + offset + ", char " + ch);
            default:
                throw new JSONException("error, offset " + offset + ", char " + ch);
        }

        if (ch == ',') {
            comma = true;
            if (offset >= length) {
                ch = EOI;
                return;
            }

            ch = chars[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= length) {
                    ch = EOI;
                    return;
                }
                ch = chars[offset];
            }
            offset++;
        } else if (!comma && ch != '}' && ch != ']' && ch != EOI) {
            throw new JSONValidException("offset " + offset);
        }
    }

    @Override
    public void skipLineComment() {
        while (true) {
            if (ch == '\n') {
                offset++;

                if (offset >= length) {
                    ch = EOI;
                    return;
                }

                ch = chars[offset];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    offset++;
                    if (offset >= length) {
                        ch = EOI;
                        return;
                    }
                    ch = chars[offset];
                }

                offset++;
                break;
            }

            offset++;
            if (offset >= length) {
                ch = EOI;
                return;
            }
            ch = chars[offset];
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
            ch = chars[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return;
            }
        } else if (csv && (ch == ',' || ch == '\r' || ch == '\n')) {
            wasNull = true;
            valueType = JSON_TYPE_NULL;
            return;
        }
        final int start = offset;

        final int limit, multmin;
        if (ch == '-') {
            limit = Integer.MIN_VALUE;
            multmin = -214748364; // limit / 10;
            negative = true;
            ch = chars[offset++];
        } else {
            if (ch == '+') {
                ch = chars[offset++];
            }
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
            ch = chars[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = chars[offset++];
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
                ch = chars[offset++];
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;

            int numDigits = scale > 0 ? offset - 2 - numStart : offset - 1 - numStart;
            if (numDigits > 38) {
                valueType = JSON_TYPE_BIG_DEC;
                stringValue = new String(chars, numStart, offset - 1 - numStart);
            } else {
                bigInt(chars, numStart, offset - 1);
            }
        } else {
            mag3 = -mag3;
        }

        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            int expValue = 0;
            ch = chars[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = chars[offset++];
            } else if (ch == '+') {
                ch = chars[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                int byteVal = (ch - '0');
                expValue = expValue * 10 + byteVal;
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    break;
                }
                ch = chars[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (chars[offset++] == 'u'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 'l'
                ) {
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 't') {
                if (chars[offset++] == 'r'
                        && chars[offset++] == 'u'
                        && chars[offset++] == 'e'
                ) {
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (chars[offset++] == 'a'
                        && chars[offset++] == 'l'
                        && chars[offset++] == 's'
                        && chars[offset++] == 'e'
                ) {
                    boolValue = false;
                    valueType = JSON_TYPE_BOOL;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = chars[offset++];
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

            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    valueType = JSON_TYPE_INT8;
                    break;
                case 'S':
                    valueType = JSON_TYPE_INT16;
                    break;
                case 'L':
                    valueType = JSON_TYPE_INT64;
                    break;
                case 'F':
                    valueType = JSON_TYPE_FLOAT;
                    break;
                case 'D':
                    valueType = JSON_TYPE_DOUBLE;
                    break;
                default:
                    break;
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }

        if (!csv) {
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (comma = (ch == ',')) {
                // next inline
                if (this.offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = chars[this.offset++];
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean readIfNull() {
        if (ch == 'n'
                && chars[offset] == 'u'
                && chars[offset + 1] == 'l'
                && chars[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = chars[offset + 3];
            }
            offset += 4;
        } else {
            return false;
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        return true;
    }

    @Override
    public Date readNullOrNewDate() {
        Date date = null;
        if (offset + 2 < end
                && chars[offset] == 'u'
                && chars[offset + 1] == 'l'
                && chars[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = chars[offset + 3];
            }
            offset += 4;
        } else if (offset + 1 < end
                && chars[offset] == 'e'
                && chars[offset + 1] == 'w') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = chars[offset + 2];
            }
            offset += 3;

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (offset + 4 < end
                    && ch == 'D'
                    && chars[offset] == 'a'
                    && chars[offset + 1] == 't'
                    && chars[offset + 2] == 'e') {
                if (offset + 3 == end) {
                    ch = EOI;
                } else {
                    ch = chars[offset + 3];
                }
                offset += 4;
            } else {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            if (ch != '(' || offset >= end) {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }

            long millis = readInt64Value();

            if (ch != ')') {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }

            date = new Date(millis);
        } else {
            throw new JSONException("json syntax error, not match null or new Date" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
                }
            }
        }

        return date;
    }

    @Override
    public boolean isNull() {
        return ch == 'n' && offset < end && chars[offset] == 'u';
    }

    @Override
    public boolean nextIfNull() {
        if (ch == 'n' && offset + 2 < end && chars[offset] == 'u') {
            this.readNull();
            return true;
        }
        return false;
    }

    @Override
    public void readNull() {
        if (chars[offset] == 'u'
                && chars[offset + 1] == 'l'
                && chars[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = chars[offset + 3];
            }
            offset += 4;
        } else {
            throw new JSONException("json syntax error, not match null, offset " + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }
        }
        if (comma = (ch == ',')) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = chars[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[offset++];
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

        if (ch != '"' && ch != '\'') {
            throw new JSONException(info("syntax error, can not read uuid"));
        }
        final char quote = ch;

        if (offset + 32 < chars.length && chars[offset + 32] == quote) {
            long msb1 = parse4Nibbles(chars, offset);
            long msb2 = parse4Nibbles(chars, offset + 4);
            long msb3 = parse4Nibbles(chars, offset + 8);
            long msb4 = parse4Nibbles(chars, offset + 12);
            long lsb1 = parse4Nibbles(chars, offset + 16);
            long lsb2 = parse4Nibbles(chars, offset + 20);
            long lsb3 = parse4Nibbles(chars, offset + 24);
            long lsb4 = parse4Nibbles(chars, offset + 28);
            if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                offset += 33;
                if (offset < end) {
                    ch = chars[offset++];
                } else {
                    ch = EOI;
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = chars[offset++];
                    }
                }

                if (comma = (ch == ',')) {
                    next();
                }

                return new UUID(
                        msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                        lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
            }
        } else if (offset + 36 < chars.length && chars[offset + 36] == quote) {
            char ch1 = chars[offset + 8];
            char ch2 = chars[offset + 13];
            char ch3 = chars[offset + 18];
            char ch4 = chars[offset + 23];
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                long msb1 = parse4Nibbles(chars, offset);
                long msb2 = parse4Nibbles(chars, offset + 4);
                long msb3 = parse4Nibbles(chars, offset + 9);
                long msb4 = parse4Nibbles(chars, offset + 14);
                long lsb1 = parse4Nibbles(chars, offset + 19);
                long lsb2 = parse4Nibbles(chars, offset + 24);
                long lsb3 = parse4Nibbles(chars, offset + 28);
                long lsb4 = parse4Nibbles(chars, offset + 32);
                if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                    offset += 37;
                    if (offset < end) {
                        ch = chars[offset++];
                    } else {
                        ch = EOI;
                    }

                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = chars[offset++];
                        }
                    }

                    if (comma = (ch == ',')) {
                        next();
                    }

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
            throw new JSONException("date only support string input : " + ch);
        }

        final char quote = ch;

        int len = 0;
        for (int i = offset; i < end; ++i, ++len) {
            if (chars[i] == quote) {
                break;
            }
        }
        return len;
    }

    @Override
    protected LocalDateTime readLocalDateTime16() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0 = '0', s1 = '0';
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
        } else if (c8 == 'T' && c15 == 'Z') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;
            m0 = c4;
            m1 = c5;
            d0 = c6;
            d1 = c7;
            h0 = c9;
            h1 = c10;
            i0 = c11;
            i1 = c12;
            s0 = c13;
            s1 = c14;
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

        offset += 17;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime17() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c7 == '-' && (c10 == 'T' || c10 == ' ') && c13 == ':' && c16 == 'Z') {
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
        } else if (c4 == '-' && c6 == '-' && (c8 == ' ' || c8 == 'T') && c11 == ':' && c14 == ':') {
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
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalDateTime readLocalDateTime18() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1;
        if (c4 == '-' && c6 == '-' && (c9 == ' ' || c9 == 'T') && c12 == ':' && c15 == ':') {
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
        } else if (c4 == '-' && c7 == '-' && (c9 == ' ' || c9 == 'T') && c12 == ':' && c15 == ':') {
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
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c12 == ':' && c15 == ':') {
            y0 = c0;
            y1 = c1;
            y2 = c2;
            y3 = c3;

            m0 = c5;
            m1 = c6;

            d0 = c8;
            d1 = c9;

            h0 = '0';
            h1 = c11;

            i0 = c13;
            i1 = c14;

            s0 = c16;
            s1 = c17;
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c15 == ':') {
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

            i0 = '0';
            i1 = c14;

            s0 = c16;
            s1 = c17;
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == ':') {
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
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime5() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];

        char h0, h1, i0, i1;
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
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute);
    }

    @Override
    protected LocalTime readLocalTime8() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];

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

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
        } else {
            return null;
        }

        offset += 9;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute, second);
    }

    @Override
    public LocalDate readLocalDate8() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];

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
        if (m0 >= '0' && m0 <= '1'
                && m1 >= '0' && m1 <= '9'
        ) {
            month = (m0 - '0') * 10 + (m1 - '0');
        } else {
            return null;
        }

        int dom;
        if (d0 >= '0' && d0 <= '3'
                && d1 >= '0' && d1 <= '9'
        ) {
            dom = (d0 - '0') * 10 + (d1 - '0');
        } else {
            return null;
        }

        LocalDate ldt;
        try {
            ldt = LocalDate.of(year, month, dom);
        } catch (DateTimeException e) {
            throw new JSONException(info(), e);
        }

        offset += 9;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public LocalDate readLocalDate9() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];

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

        LocalDate ldt;
        try {
            ldt = LocalDate.of(year, month, dom);
        } catch (DateTimeException e) {
            throw new JSONException(info(), e);
        }

        offset += 10;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public LocalDate readLocalDate10() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];

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

        LocalDate ldt;
        try {
            ldt = LocalDate.of(year, month, dom);
        } catch (DateTimeException e) {
            throw new JSONException(info(), e);
        }

        offset += 11;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public LocalDate readLocalDate11() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];

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

        LocalDate ldt = LocalDate.of(year, month, dom);

        offset += 12;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        if (len < 19) {
            return null;
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];
        char c18 = chars[offset + 18];
        char c19 = len == 19 ? ' ' : chars[offset + 19];

        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0', c29 = '\0';
        switch (len) {
            case 19:
                c20 = '\0';
                break;
            case 20:
                c20 = '\0';
                break;
            case 21:
                c20 = chars[offset + 20];
                break;
            case 22:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                break;
            case 23:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                break;
            case 24:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                break;
            case 25:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                break;
            case 26:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                break;
            case 27:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                break;
            case 28:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                c27 = chars[offset + 27];
                break;
            case 29:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                c27 = chars[offset + 27];
                c28 = chars[offset + 28];
                break;
            default:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                c27 = chars[offset + 27];
                c28 = chars[offset + 28];
                c29 = chars[offset + 29];
                break;
        }

        char y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8;
        int zoneIdBegin;
        boolean isTimeZone = false, pm = false;
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
        } else if (c4 == '-' && c7 == '-' && (c10 == ' ' || c10 == 'T') && c13 == ':' && c16 == 'Z' && c17 == '['
                && len == 22 && c21 == ']') {
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

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            isTimeZone = true;
            zoneIdBegin = 17;
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
        } else if (len == 22
                && c3 == ' ' && c5 == ',' && c6 == ' ' && c11 == ' ' && c13 == ':' && c16 == ':' && c19 == ' ' && (c20 == 'A' || c20 == 'P') && c21 == 'M'
        ) {
            y0 = c7;
            y1 = c8;
            y2 = c9;
            y3 = c10;

            int month = DateUtils.month(c0, c1, c2);
            if (month > 0) {
                m0 = (char) ('0' + month / 10);
                m1 = (char) ('0' + (month % 10));
            } else {
                m0 = '0';
                m1 = '0';
            }

            d0 = '0';
            d1 = c4;

            h0 = '0';
            h1 = c12;
            pm = c20 == 'P';

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
            zoneIdBegin = 22;
            isTimeZone = false;
        } else if (len == 23
                && c3 == ' ' && c5 == ',' && c6 == ' ' && c11 == ' ' && c14 == ':' && c17 == ':' && c20 == ' ' && (c21 == 'A' || c21 == 'P') && c22 == 'M'
        ) {
            y0 = c7;
            y1 = c8;
            y2 = c9;
            y3 = c10;

            int month = DateUtils.month(c0, c1, c2);
            if (month > 0) {
                m0 = (char) ('0' + month / 10);
                m1 = (char) ('0' + (month % 10));
            } else {
                m0 = '0';
                m1 = '0';
            }

            d0 = '0';
            d1 = c4;

            h0 = c12;
            h1 = c13;
            pm = c21 == 'P';

            i0 = c15;
            i1 = c16;

            s0 = c18;
            s1 = c19;

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 23;
            isTimeZone = false;
        } else if (len == 23
                && c3 == ' ' && c6 == ',' && c7 == ' ' && c12 == ' ' && c14 == ':' && c17 == ':' && c20 == ' ' && (c21 == 'A' || c21 == 'P') && c22 == 'M'
        ) {
            y0 = c8;
            y1 = c9;
            y2 = c10;
            y3 = c11;

            int month = DateUtils.month(c0, c1, c2);
            if (month > 0) {
                m0 = (char) ('0' + month / 10);
                m1 = (char) ('0' + (month % 10));
            } else {
                m0 = '0';
                m1 = '0';
            }

            d0 = c4;
            d1 = c5;

            h0 = '0';
            h1 = c13;
            pm = c21 == 'P';

            i0 = c15;
            i1 = c16;

            s0 = c18;
            s1 = c19;

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 23;
            isTimeZone = false;
        } else if (len == 24
                && c3 == ' ' && c6 == ',' && c7 == ' ' && c12 == ' ' && c15 == ':' && c18 == ':' && c21 == ' ' && (c22 == 'A' || c22 == 'P') && c23 == 'M'
        ) {
            y0 = c8;
            y1 = c9;
            y2 = c10;
            y3 = c11;

            int month = DateUtils.month(c0, c1, c2);
            if (month > 0) {
                m0 = (char) ('0' + month / 10);
                m1 = (char) ('0' + (month % 10));
            } else {
                m0 = '0';
                m1 = '0';
            }

            d0 = c4;
            d1 = c5;

            h0 = c13;
            h1 = c14;
            pm = c22 == 'P';

            i0 = c16;
            i1 = c17;

            s0 = c19;
            s1 = c20;

            S0 = '0';
            S1 = '0';
            S2 = '0';
            S3 = '0';
            S4 = '0';
            S5 = '0';
            S6 = '0';
            S7 = '0';
            S8 = '0';
            zoneIdBegin = 24;
            isTimeZone = false;
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
        } else {
            return null;
        }

        char first = chars[this.offset + zoneIdBegin];

        if (pm) {
            int hourValue = DateUtils.hourAfterNoon(h0, h1);
            h0 = (char) (hourValue >> 16);
            h1 = (char) ((short) hourValue);
        }

        LocalDateTime ldt = localDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);

        ZoneId zoneId;
        if (isTimeZone) {
            String tzStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin);
            TimeZone timeZone = TimeZone.getTimeZone(tzStr);
            zoneId = timeZone.toZoneId();
        } else {
            if (first == 'Z') {
                zoneId = UTC;
            } else {
                String zoneIdStr;
                if (first == '+' || first == '-') {
                    zoneIdStr = new String(chars, this.offset + zoneIdBegin, len - zoneIdBegin);
                } else if (first == ' ') {
                    zoneIdStr = new String(chars, this.offset + zoneIdBegin + 1, len - zoneIdBegin - 1);
                } else { // '[
                    if (zoneIdBegin < len) {
                        zoneIdStr = new String(chars, this.offset + zoneIdBegin + 1, len - zoneIdBegin - 2);
                    } else {
                        zoneIdStr = null;
                    }
                }
                zoneId = DateUtils.getZoneId(zoneIdStr, context.zoneId);
            }
        }

        ZonedDateTime zdt = ZonedDateTime.ofLocal(ldt, zoneId, null);

        offset += (len + 1);
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return zdt;
    }

    @Override
    protected LocalDateTime readLocalDateTime19() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];
        char c18 = chars[offset + 18];

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

        if (year == 0 && month == 0 && dom == 0) {
            year = 1970;
            month = 1;
            dom = 1;
        }

        LocalDateTime ldt = LocalDateTime.of(year, month, dom, hour, minute, second, millis);

        offset += 20;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public long readMillis19() {
        char quote = ch;
        if (quote != '"' && quote != '\'') {
            throw new JSONException("date only support string input");
        }

        if (offset + 18 >= end) {
            wasNull = true;
            return 0;
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];
        char c18 = chars[offset + 18];

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
            return 0L;
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
            wasNull = false;
            return 0L;
        }

        if (year == 0 && month == 0 && dom == 0) {
            year = 1970;
            month = 1;
            dom = 1;
        }

        if (chars[offset + 19] != quote) {
            throw new JSONException(info("illegal date input"));
        }
        offset += 20;

        next();
        if (comma = (ch == ',')) {
            next();
        }

        return DateUtils.millis(context.getZoneId(), year, month, dom, hour, minute, second, nanoOfSecond);
    }

    @Override
    protected LocalDateTime readLocalDateTimeX(int len) {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        if (len < 21 || len > 29) {
            throw new JSONException("illegal localDatetime string : " + readString());
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];
        char c18 = chars[offset + 18];
        char c19 = chars[offset + 19];
        char c20, c21 = '0', c22 = '0', c23 = '0', c24 = '0', c25 = '0', c26 = '0', c27 = '0', c28 = '0';
        switch (len) {
            case 21:
                c20 = chars[offset + 20];
                break;
            case 22:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                break;
            case 23:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                break;
            case 24:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                break;
            case 25:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                break;
            case 26:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                break;
            case 27:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                break;
            case 28:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                c27 = chars[offset + 27];
                break;
            default:
                c20 = chars[offset + 20];
                c21 = chars[offset + 21];
                c22 = chars[offset + 22];
                c23 = chars[offset + 23];
                c24 = chars[offset + 24];
                c25 = chars[offset + 25];
                c26 = chars[offset + 26];
                c27 = chars[offset + 27];
                c28 = chars[offset + 28];
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

        LocalDateTime ldt = localDateTime(y0, y1, y2, y3, m0, m1, d0, d1, h0, h1, i0, i1, s0, s1, S0, S1, S2, S3, S4, S5, S6, S7, S8);
        if (ldt == null) {
            return null;
        }

        offset += (len + 1);
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected LocalTime readLocalTime10() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];

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

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
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
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute, second, millis);
    }

    @Override
    protected LocalTime readLocalTime11() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];

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

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
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
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute, second, millis);
    }

    @Override
    protected LocalTime readLocalTime12() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];

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

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
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
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute, second, millis);
    }

    @Override
    protected LocalTime readLocalTime18() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        char c0 = chars[offset];
        char c1 = chars[offset + 1];
        char c2 = chars[offset + 2];
        char c3 = chars[offset + 3];
        char c4 = chars[offset + 4];
        char c5 = chars[offset + 5];
        char c6 = chars[offset + 6];
        char c7 = chars[offset + 7];
        char c8 = chars[offset + 8];
        char c9 = chars[offset + 9];
        char c10 = chars[offset + 10];
        char c11 = chars[offset + 11];
        char c12 = chars[offset + 12];
        char c13 = chars[offset + 13];
        char c14 = chars[offset + 14];
        char c15 = chars[offset + 15];
        char c16 = chars[offset + 16];
        char c17 = chars[offset + 17];

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

        int second;
        if (s0 >= '0' && s0 <= '9'
                && s1 >= '0' && s1 <= '9'
        ) {
            second = (s0 - '0') * 10 + (s1 - '0');
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
        if (comma = (ch == ',')) {
            next();
        }

        return LocalTime.of(hour, minute, second, millis);
    }

    @Override
    public String readPattern() {
        if (ch != '/') {
            throw new JSONException("illegal pattern");
        }

        int offset = this.offset;
        for (; ; ) {
            char c = chars[offset];
            if (c == '/') {
                break;
            }
            offset++;
            if (offset >= end) {
                break;
            }
        }
        String str = new String(chars, this.offset, offset - this.offset);

        if (offset + 1 == end) {
            this.offset = end;
            this.ch = EOI;
            return str;
        }

        int b = chars[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = chars[++offset];
        }

        if (comma = (b == ',')) {
            this.offset = offset + 1;

            // inline next
            ch = chars[this.offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (this.offset >= end) {
                    ch = EOI;
                } else {
                    ch = chars[this.offset++];
                }
            }
        } else {
            this.offset = offset + 1;
            this.ch = (char) b;
        }

        return str;
    }

    @Override
    public String info(String message) {
        int line = 1, column = 1;
        for (int i = 0; i < offset & i < end; i++, column++) {
            if (chars[i] == '\n') {
                column = 1;
                line++;
            }
        }

        StringBuilder buf = new StringBuilder();

        if (message != null && !message.isEmpty()) {
            buf.append(message).append(", ");
        }

        buf.append("offset ").append(offset)
                .append(", character ").append(ch)
                .append(", line ").append(line)
                .append(", column ").append(column)
                .append(", fastjson-version ").append(JSON.VERSION)
                .append(line > 1 ? '\n' : ' ');

        final int MAX_OUTPUT_LENGTH = 65535;
        buf.append(chars, this.start, length < MAX_OUTPUT_LENGTH ? length : MAX_OUTPUT_LENGTH);

        return buf.toString();
    }

    @Override
    public void close() {
        if (cacheIndex != -1) {
            JSONFactory.releaseCharArray(cacheIndex, chars);
        }

        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }
}

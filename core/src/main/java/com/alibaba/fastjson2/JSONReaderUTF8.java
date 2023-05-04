package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;

class JSONReaderUTF8
        extends JSONReader {
    protected final byte[] bytes;
    protected final int length;
    protected final int start;
    protected final int end;

    protected int nameBegin;
    protected int nameEnd;
    protected int nameLength;

    protected boolean nameAscii;
    protected int referenceBegin;

    protected final InputStream in;

    protected final CacheItem cacheItem;

    JSONReaderUTF8(Context ctx, InputStream is) {
        super(ctx);

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }

        int off = 0;
        try {
            for (; ; ) {
                int n = is.read(bytes, off, bytes.length - off);
                if (n == -1) {
                    break;
                }
                off += n;

                if (off == bytes.length) {
                    bytes = Arrays.copyOf(bytes, bytes.length + 8192);
                }
            }
        } catch (IOException ioe) {
            throw new JSONException("read error", ioe);
        }

        this.bytes = bytes;
        this.offset = 0;
        this.length = off;
        this.in = is;
        this.start = 0;
        this.end = length;
        next();

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    JSONReaderUTF8(Context ctx, ByteBuffer buffer) {
        super(ctx);

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        final int remaining = buffer.remaining();
        if (bytes == null || bytes.length < remaining) {
            bytes = new byte[remaining];
        }
        buffer.get(bytes, 0, remaining);

        this.bytes = bytes;
        this.offset = 0;
        this.length = remaining;
        this.in = null;
        this.start = 0;
        this.end = length;
        next();

        while (ch == '/') {
            next();
            if (ch == '/') {
                skipLineComment();
            } else {
                throw new JSONException("input not support " + ch + ", offset " + offset);
            }
        }
    }

    JSONReaderUTF8(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx);

        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.in = null;
        this.start = offset;
        this.end = offset + length;
        this.cacheItem = null;
        next();

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
    public boolean nextIfMatch(char e) {
        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[offset++];
            }
        }

        if (this.ch != e) {
            return false;
        }
        comma = (ch == ',');

        if (offset >= end) {
            ch = EOI;
            return true;
        }

        int c = bytes[offset];
        while (c == '\0' || (c <= ' ' && ((1L << c) & SPACE) != 0)) {
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
    public final boolean nextIfSet() {
        if (ch == 'S'
                && offset + 1 < end
                && bytes[offset] == 'e'
                && bytes[offset + 1] == 't') {
            offset += 2;
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = (char) bytes[offset++];
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean nextIfInfinity() {
        if (ch == 'I'
                && offset + 6 < end
                && bytes[offset] == 'n'
                && bytes[offset + 1] == 'f'
                && bytes[offset + 2] == 'i'
                && bytes[offset + 3] == 'n'
                && bytes[offset + 4] == 'i'
                && bytes[offset + 5] == 't'
                && bytes[offset + 6] == 'y'
        ) {
            offset += 7;
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = (char) bytes[offset++];
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

        int c = bytes[offset];
        while (c == '\0' || (c <= ' ' && ((1L << c) & SPACE) != 0)) {
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
    public final long readFieldNameHashCodeUnquote() {
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
                    ch = (char) bytes[offset++];
                    switch (ch) {
                        case 'u': {
                            byte c1 = bytes[offset++];
                            byte c2 = bytes[offset++];
                            byte c3 = bytes[offset++];
                            byte c4 = bytes[offset++];
                            ch = char4(c1, c2, c3, c4);
                            break;
                        }
                        case 'x': {
                            byte c1 = bytes[offset++];
                            byte c2 = bytes[offset++];
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

                if (ch > 0x7F || i >= 8 || (i == 0 && ch == 0)) {
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
                        : (char) bytes[offset++];
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
                        : (char) bytes[offset++];
            }
        }

        if (ch == ':') {
            if (offset == end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset == end) {
                    ch = EOI;
                    break;
                } else {
                    ch = (char) bytes[offset++];
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

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;

        if (MIXED_HASH_ALGORITHM && offset + 9 < end) {
            byte c0, c1, c2, c3, c4, c5, c6, c7;

            if ((c0 = bytes[offset]) == quote) {
                nameValue = 0;
            } else if ((c1 = bytes[offset + 1]) == quote && c0 != '\\' && c0 > 0 && c0 <= 0xFF) {
                nameValue = c0;
                this.nameLength = 1;
                this.nameEnd = offset + 1;
                offset += 2;
            } else if ((c2 = bytes[offset + 2]) == quote
                    && c0 != '\\' && c1 != '\\'
                    && c0 <= 0xFF && c1 <= 0xFF
                    && c0 >= 0 && c1 > 0
            ) {
                nameValue = (c1 << 8)
                        + c0;
                this.nameLength = 2;
                this.nameEnd = offset + 2;
                offset += 3;
            } else if ((c3 = bytes[offset + 3]) == quote
                    && c0 != '\\' && c1 != '\\' && c2 != '\\'
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 > 0
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
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 >= 0 && c3 > 0
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
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 >= 0 && c3 >= 0 && c4 > 0
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
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 >= 0 && c3 >= 0 && c4 >= 0 && c5 > 0
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
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF && c6 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 >= 0 && c3 >= 0 && c4 >= 0 && c5 >= 0 && c6 > 0
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
                    && c0 <= 0xFF && c1 <= 0xFF && c2 <= 0xFF && c3 <= 0xFF && c4 <= 0xFF && c5 <= 0xFF && c6 <= 0xFF && c7 <= 0xFF
                    && c0 >= 0 && c1 >= 0 && c2 >= 0 && c3 >= 0 && c4 >= 0 && c5 >= 0 && c6 >= 0 && c7 > 0
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

        if (MIXED_HASH_ALGORITHM && nameValue == 0) {
            for (int i = 0; offset < end; offset++, i++) {
                int c = bytes[offset];

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
                        case '\\':
                        case '"':
                        default:
                            c = char1(c);
                            break;
                    }
                    if (c > 0xFF) {
                        nameAscii = false;
                    }
                } else if (c == -61 || c == -62) {
                    byte c1 = bytes[++offset];
                    c = (char) (((c & 0x1F) << 6)
                            | (c1 & 0x3F));
                    nameAscii = false;
                }

                if (c > 0xFF || c < 0 || i >= 8 || (i == 0 && c == 0)) {
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
                                    | (c2 & 0x3F));

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
        }

        byte c = bytes[offset];

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset];
        }
        if (c != ':') {
            // return -1;
            throw new JSONException(info("expect ':', but " + c));
        }

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

        this.offset = offset + 1;
        this.ch = (char) c;

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException(info("illegal character " + ch));
        }

        final char quote = ch;

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset;

        long nameValue = 0;
        if (MIXED_HASH_ALGORITHM) {
            for (int i = 0; offset < end; offset++, i++) {
                int c = bytes[offset];

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
                        case '\\':
                        case '"':
                        default:
                            c = char1(c);
                            break;
                    }
                } else if (c == -61 || c == -62) {
                    byte c1 = bytes[++offset];
                    c = (char) (((c & 0x1F) << 6)
                            | (c1 & 0x3F));
                }

                if (c > 0xFF || c < 0 || i >= 8 || (i == 0 && c == 0)) {
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
                    offset++;
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
                                    | (c2 & 0x3F));

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
        }

        byte c;
        if (offset == end) {
            c = EOI;
        } else {
            c = bytes[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset];
        }

        if (comma = (c == ',')) {
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
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        long hashCode = Fnv.MAGIC_HASH_CODE;
        int offset = nameBegin;

        if (MIXED_HASH_ALGORITHM) {
            long nameValue = 0;
            for (int i = 0; offset < end; offset++) {
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
                        case '\\':
                        case '"':
                        default:
                            c = char1(c);
                            break;
                    }
                } else if (c == -61 || c == -62) {
                    byte c1 = bytes[++offset];
                    c = (char) (((c & 0x1F) << 6)
                            | (c1 & 0x3F));
                } else if (c == '"') {
                    break;
                }

                if (i >= 8 || c > 0xFF || c < 0 || (i == 0 && c == 0)) {
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

        if (nameAscii && !nameEscape) {
            for (int i = nameBegin; i < nameEnd; ++i) {
                char c = (char) bytes[i];
                if (c >= 'A' && c <= 'Z') {
                    c = (char) (c + 32);
                }

                if (c == '_' || c == '-' || c == ' ') {
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
                    case '\\':
                    case '"':
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
                                    | (c2 & 0x3F));
                            offset += 2;
                            break;
                        }
                        case 14: {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            c = (char) (((c & 0x0F) << 12)
                                    | ((c2 & 0x3F) << 6)
                                    | ((c3 & 0x3F) << 0));

                            offset += 3;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }
            }

            if (c == '_' || c == '-' || c == ' ') {
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
                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[length];
                    for (int i = 0; i < length; ++i) {
                        chars[i] = (char) bytes[nameBegin + i];
                    }
                    return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, nameBegin, nameEnd);
                    return STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                }
            }

            return new String(bytes, nameBegin, length,
                    nameAscii ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8
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
        for (int i = 0; offset < end; ++i) {
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
                if (offset == end) {
                    c = EOI;
                } else {
                    c = bytes[offset];
                }

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

        if (nameEnd < nameBegin) {
            throw new JSONException("syntax error : " + offset);
        }

        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            if (nameAscii) {
                long nameValue0 = -1, nameValue1 = -1;
                switch (length) {
                    case 1:
                        return TypeUtils.toString((char) (bytes[nameBegin] & 0xff));
                    case 2:
                        return TypeUtils.toString(
                                (char) (bytes[nameBegin] & 0xff),
                                (char) (bytes[nameBegin + 1] & 0xff)
                        );
                    case 3:
                        nameValue0
                                = (bytes[nameBegin + 2] << 16)
                                + (bytes[nameBegin + 1] << 8)
                                + (bytes[nameBegin]);
                        break;
                    case 4:
                        nameValue0
                                = (bytes[nameBegin + 3] << 24)
                                + (bytes[nameBegin + 2] << 16)
                                + (bytes[nameBegin + 1] << 8)
                                + (bytes[nameBegin]);
                        break;
                    case 5:
                        nameValue0
                                = (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        break;
                    case 6:
                        nameValue0
                                = (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        break;
                    case 7:
                        nameValue0
                                = (((long) bytes[nameBegin + 6]) << 48)
                                + (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        break;
                    case 8:
                        nameValue0
                                = (((long) bytes[nameBegin + 7]) << 56)
                                + (((long) bytes[nameBegin + 6]) << 48)
                                + (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        break;
                    case 9:
                        nameValue0 = bytes[nameBegin];
                        nameValue1
                                = (((long) bytes[nameBegin + 8]) << 56)
                                + (((long) bytes[nameBegin + 7]) << 48)
                                + (((long) bytes[nameBegin + 6]) << 40)
                                + (((long) bytes[nameBegin + 5]) << 32)
                                + (((long) bytes[nameBegin + 4]) << 24)
                                + (((long) bytes[nameBegin + 3]) << 16)
                                + (((long) bytes[nameBegin + 2]) << 8)
                                + ((long) bytes[nameBegin + 1]);
                        break;
                    case 10:
                        nameValue0
                                = (bytes[nameBegin + 1] << 8)
                                + (bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 9]) << 56)
                                + (((long) bytes[nameBegin + 8]) << 48)
                                + (((long) bytes[nameBegin + 7]) << 40)
                                + (((long) bytes[nameBegin + 6]) << 32)
                                + (((long) bytes[nameBegin + 5]) << 24)
                                + (((long) bytes[nameBegin + 4]) << 16)
                                + (((long) bytes[nameBegin + 3]) << 8)
                                + ((long) bytes[nameBegin + 2]);
                        break;
                    case 11:
                        nameValue0
                                = (bytes[nameBegin + 2] << 16)
                                + (bytes[nameBegin + 1] << 8)
                                + (bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 10]) << 56)
                                + (((long) bytes[nameBegin + 9]) << 48)
                                + (((long) bytes[nameBegin + 8]) << 40)
                                + (((long) bytes[nameBegin + 7]) << 32)
                                + (((long) bytes[nameBegin + 6]) << 24)
                                + (((long) bytes[nameBegin + 5]) << 16)
                                + (((long) bytes[nameBegin + 4]) << 8)
                                + ((long) bytes[nameBegin + 3]);
                        break;
                    case 12:
                        nameValue0
                                = (bytes[nameBegin + 3] << 24)
                                + (bytes[nameBegin + 2] << 16)
                                + (bytes[nameBegin + 1] << 8)
                                + (bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 11]) << 56)
                                + (((long) bytes[nameBegin + 10]) << 48)
                                + (((long) bytes[nameBegin + 9]) << 40)
                                + (((long) bytes[nameBegin + 8]) << 32)
                                + (((long) bytes[nameBegin + 7]) << 24)
                                + (((long) bytes[nameBegin + 6]) << 16)
                                + (((long) bytes[nameBegin + 5]) << 8)
                                + ((long) bytes[nameBegin + 4]);
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
                                + (((long) bytes[nameBegin + 11]) << 48)
                                + (((long) bytes[nameBegin + 10]) << 40)
                                + (((long) bytes[nameBegin + 9]) << 32)
                                + (((long) bytes[nameBegin + 8]) << 24)
                                + (((long) bytes[nameBegin + 7]) << 16)
                                + (((long) bytes[nameBegin + 6]) << 8)
                                + ((long) bytes[nameBegin + 5]);
                        break;
                    case 14:
                        nameValue0
                                = (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 13]) << 56)
                                + (((long) bytes[nameBegin + 12]) << 48)
                                + (((long) bytes[nameBegin + 11]) << 40)
                                + (((long) bytes[nameBegin + 10]) << 32)
                                + (((long) bytes[nameBegin + 9]) << 24)
                                + (((long) bytes[nameBegin + 8]) << 16)
                                + (((long) bytes[nameBegin + 8]) << 8)
                                + ((long) bytes[nameBegin + 6]);
                        break;
                    case 15:
                        nameValue0
                                = (((long) bytes[nameBegin + 6]) << 48)
                                + (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 14]) << 56)
                                + (((long) bytes[nameBegin + 13]) << 48)
                                + (((long) bytes[nameBegin + 12]) << 40)
                                + (((long) bytes[nameBegin + 11]) << 32)
                                + (((long) bytes[nameBegin + 10]) << 24)
                                + (((long) bytes[nameBegin + 9]) << 16)
                                + (((long) bytes[nameBegin + 8]) << 8)
                                + ((long) bytes[nameBegin + 7]);
                        break;
                    case 16:
                        nameValue0
                                = (((long) bytes[nameBegin + 7]) << 56)
                                + (((long) bytes[nameBegin + 6]) << 48)
                                + (((long) bytes[nameBegin + 5]) << 40)
                                + (((long) bytes[nameBegin + 4]) << 32)
                                + (((long) bytes[nameBegin + 3]) << 24)
                                + (((long) bytes[nameBegin + 2]) << 16)
                                + (((long) bytes[nameBegin + 1]) << 8)
                                + ((long) bytes[nameBegin]);
                        nameValue1
                                = (((long) bytes[nameBegin + 15]) << 56)
                                + (((long) bytes[nameBegin + 14]) << 48)
                                + (((long) bytes[nameBegin + 13]) << 40)
                                + (((long) bytes[nameBegin + 12]) << 32)
                                + (((long) bytes[nameBegin + 11]) << 24)
                                + (((long) bytes[nameBegin + 10]) << 16)
                                + (((long) bytes[nameBegin + 9]) << 8)
                                + ((long) bytes[nameBegin + 8]);
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
                            if (STRING_CREATOR_JDK8 != null) {
                                char[] chars = new char[length];
                                for (int i = 0; i < length; ++i) {
                                    chars[i] = (char) bytes[nameBegin + i];
                                }
                                name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                            } else {
                                name = new String(bytes, nameBegin, length, StandardCharsets.ISO_8859_1);
                            }

                            NAME_CACHE2[indexMask] = new NameCacheEntry2(name, nameValue0, nameValue1);
                            return name;
                        } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                            return entry.name;
                        }
                    } else {
                        int indexMask = ((int) nameValue0) & (NAME_CACHE.length - 1);
                        NameCacheEntry entry = NAME_CACHE[indexMask];
                        if (entry == null) {
                            String name;
                            if (STRING_CREATOR_JDK8 != null) {
                                char[] chars = new char[length];
                                for (int i = 0; i < length; ++i) {
                                    chars[i] = (char) bytes[nameBegin + i];
                                }
                                name = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                            } else {
                                name = new String(bytes, nameBegin, length, StandardCharsets.ISO_8859_1);
                            }

                            NAME_CACHE[indexMask] = new NameCacheEntry(name, nameValue0);
                            return name;
                        } else if (entry.value == nameValue0) {
                            return entry.name;
                        }
                    }
                }

                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[length];
                    for (int i = 0; i < length; ++i) {
                        chars[i] = (char) bytes[nameBegin + i];
                    }
                    return STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, nameBegin, nameEnd);
                    return STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                }
            }

            return new String(bytes, nameBegin, length,
                    nameAscii ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8
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
        } else if (ch == '+') {
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
            wasNull = firstOffset + 1 == offset;
            ch = offset == end ? EOI : (char) bytes[offset++];
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
                ch = (char) bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            this.ch = offset == end ? EOI : (char) bytes[this.offset++];
            // next inline
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }

        return negative ? -intValue : intValue;
    }

    @Override
    public final Integer readInt32() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        int intValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                    nextIfMatch(',');
                }
                return null;
            }
        } else if (ch == ',' || ch == '\r' || ch == '\n') {
            return null;
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else if (ch == '+') {
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
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
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
                ch = (char) bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[this.offset++];
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
        } else if (ch == '+') {
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
                    throw new JSONException("long overflow, value " + bigInteger);
                }
            } else {
                return getInt64Value();
            }
        }

        if (quote != 0) {
            wasNull = firstOffset + 1 == offset;
            this.ch = offset == end ? EOI : (char) bytes[offset++];
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
                ch = (char) bytes[offset++];
            }
        }

        while (this.ch <= ' ' && ((1L << this.ch) & SPACE) != 0) {
            if (offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[offset++];
            }
        }

        if (comma = (this.ch == ',')) {
            this.ch = offset == end ? EOI : (char) bytes[this.offset++];
            // next inline
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    this.ch = EOI;
                } else {
                    this.ch = (char) bytes[offset++];
                }
            }
        }

        return negative ? -longValue : longValue;
    }

    @Override
    public final Long readInt64() {
        boolean negative = false;
        int firstOffset = offset;
        char firstChar = ch;

        long longValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
                nextIfMatch(',');
                return null;
            }
        } else if (ch == ',' || ch == '\r' || ch == '\n') {
            return null;
        }

        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
                nextIfMatch(',');
                return null;
            }
        } else if (ch == '+') {
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
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
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
                ch = (char) bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[this.offset++];
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
    public final double readDoubleValue() {
        this.wasNull = false;

        boolean value = false;
        double doubleValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            if (ch == '+') {
                ch = (char) bytes[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
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
                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
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
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (bytes[offset++] == 'u'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 'l'
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
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    value = true;
                    doubleValue = 1;
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
                    doubleValue = 0;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
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
            }

            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
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
                doubleValue = TypeUtils.parseDouble(bytes, start - 1, len);
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
                    ch = (char) bytes[offset++];
                }
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[this.offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return doubleValue;
    }

    @Override
    public final float readFloatValue() {
        this.wasNull = false;

        boolean value = false;
        float floatValue = 0;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            if (ch == '+') {
                ch = (char) bytes[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
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
                this.scale++;
                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
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
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (bytes[offset++] == 'u'
                        && bytes[offset++] == 'l'
                        && bytes[offset++] == 'l'
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
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    value = true;
                    floatValue = 1;
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
                    floatValue = 0;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
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
            }

            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
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
                floatValue = TypeUtils.parseFloat(bytes, start - 1, len);
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
                    ch = (char) bytes[offset++];
                }
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (this.comma = ch == ',') {
            // next inline
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[this.offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            }
        }

        return floatValue;
    }

    @Override
    public final void readString(ValueConsumer consumer, boolean quoted) {
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
                                ((c & 0x1F) << 6) | (c2 & 0x3F));
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

            if (quoted) {
                JSONWriter jsonWriter = JSONWriterUTF8.of();
                jsonWriter.writeString(chars, 0, chars.length);
                byte[] bytes = jsonWriter.getBytes();
                consumer.accept(bytes, 0, bytes.length);
            } else {
                byte[] bytes = new byte[bytesMaxiumLength];
                int bytesLength = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, 0);
                consumer.accept(bytes, 0, bytesLength);
            }
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

        if (comma = (b == ',')) {
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
                        case '\\':
                        case '"':
                            break;
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
                                        ((c & 0x1F) << 6) | (c2 & 0x3F));
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
            str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.ISO_8859_1);
        } else {
            str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);
        }

        int b = bytes[++offset];
        while (b <= ' ' && ((1L << b) & SPACE) != 0) {
            b = bytes[++offset];
        }

        this.comma = b == ',';
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
    public final boolean skipName() {
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
    public final void skipValue() {
        comma = false;
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
                boolean sign = ch == '-' || ch == '+';
                if (sign) {
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        throw new JSONException("illegal number, offset " + offset);
                    }
                }
                boolean dot = ch == '.';
                boolean space = false;
                boolean num = false;
                if (!dot && (ch >= '0' && ch <= '9')) {
                    num = true;
                    for (; ; ) {
                        if (offset < end) {
                            ch = (char) (bytes[offset++] & 0xff);
                        } else {
                            ch = EOI;
                            return;
                        }

                        if (space || ch < '0' || ch > '9') {
                            break;
                        }
                    }
                }

                boolean small = false;
                if (ch == '.') {
                    small = true;
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        ch = EOI;
                        return;
                    }

                    if (ch >= '0' && ch <= '9') {
                        for (; ; ) {
                            if (offset < end) {
                                ch = (char) (bytes[offset++] & 0xff);
                            } else {
                                ch = EOI;
                                return;
                            }

                            if (space || ch < '0' || ch > '9') {
                                break;
                            }
                        }
                    }
                }

                if (!num && !small) {
                    throw new JSONException("illegal number, offset " + offset + ", char " + ch);
                }

                if (ch == 'e' || ch == 'E') {
                    ch = (char) (bytes[offset++] & 0xff);

                    boolean eSign = false;
                    if (ch == '+' || ch == '-') {
                        eSign = true;
                        if (offset < end) {
                            ch = (char) (bytes[offset++] & 0xff);
                        } else {
                            throw new JSONException("illegal number, offset " + offset);
                        }
                    }

                    if (ch >= '0' && ch <= '9') {
                        for (; ; ) {
                            if (offset < end) {
                                ch = (char) (bytes[offset++] & 0xff);
                            } else {
                                ch = EOI;
                                return;
                            }

                            if (ch < '0' || ch > '9') {
                                break;
                            }
                        }
                    } else if (eSign) {
                        throw new JSONException("illegal number, offset " + offset + ", char " + ch);
                    }
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        ch = EOI;
                        return;
                    }
                }

                if (ch == '}' || ch == ']') {
                    return;
                }

                if (ch == ',') {
                    comma = true;
                    if (offset >= end) {
                        throw new JSONException("illegal number, offset " + offset);
                    }

                    ch = (char) (bytes[offset] & 0xff);
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        offset++;
                        if (offset >= end) {
                            throw new JSONException("illegal number, offset " + offset);
                        }
                        ch = (char) (bytes[offset] & 0xff);
                    }
                    comma = true;
                    offset++;
                    return;
                }

                throw new JSONException("error, offset " + offset + ", char " + ch);
            case 't':
                if (offset + 3 > end) {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                if (bytes[offset] != 'r' || bytes[offset + 1] != 'u' || bytes[offset + 2] != 'e') {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                offset += 3;
                if (offset < end) {
                    ch = (char) (bytes[offset++] & 0xff);
                } else {
                    ch = EOI;
                    return;
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        ch = EOI;
                        return;
                    }
                }

                if (ch == '}' || ch == ']') {
                    return;
                }
                break;
            case 'f':
                if (offset + 4 > end) {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                if (bytes[offset] != 'a' || bytes[offset + 1] != 'l' || bytes[offset + 2] != 's' || bytes[offset + 3] != 'e') {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                offset += 4;
                if (offset < end) {
                    ch = (char) (bytes[offset++] & 0xff);
                } else {
                    ch = EOI;
                    return;
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        ch = EOI;
                        return;
                    }
                }

                if (ch == '}' || ch == ']') {
                    return;
                }
                break;
            case 'n':
                if (offset + 3 > end) {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                if (bytes[offset] != 'u' || bytes[offset + 1] != 'l' || bytes[offset + 2] != 'l') {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                offset += 3;
                if (offset < end) {
                    ch = (char) (bytes[offset++] & 0xff);
                } else {
                    ch = EOI;
                    return;
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset < end) {
                        ch = (char) (bytes[offset++] & 0xff);
                    } else {
                        ch = EOI;
                        return;
                    }
                }

                if (ch == '}' || ch == ']') {
                    return;
                }
                break;
            case 'S':
                if (nextIfSet()) {
                    skipValue();
                    break;
                }
                throw new JSONException("error, offset " + offset + ", char " + ch);
            default:
                throw new JSONException("TODO : " + ch);
        }

        if (ch == ',') {
            comma = true;
            if (offset >= end) {
                throw new JSONException("error, offset " + offset + ", char " + ch);
            }

            ch = (char) bytes[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    throw new JSONException("error, offset " + offset + ", char " + ch);
                }
                ch = (char) bytes[offset];
            }
            offset++;
        } else if (!comma && ch != '}' && ch != ']' && ch != EOI) {
            throw new JSONValidException("offset " + offset);
        }
    }

    @Override
    public final String getString() {
        if (stringValue != null) {
            return stringValue;
        }

        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            return new String(bytes, nameBegin, length,
                    nameAscii ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8
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
            chars[i] = (char) c;
            offset++;
        }

        return stringValue = new String(chars);
    }

    protected final void skipString() {
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

                if (ch == '\\' || ch == '"') {
                    ch = bytes[offset++];
                    continue;
                }

                if (ch == 'u') {
                    offset += 4;
                    ch = bytes[offset++];
                    continue;
                }

                char1(ch);
                continue;
            }
            if (ch == quote) {
                if (offset < end) {
                    ch = bytes[offset++];
                } else {
                    ch = EOI;
                }
                break;
            }

            if (offset < end) {
                ch = bytes[offset++];
            } else {
                ch = EOI;
                break;
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = bytes[offset++];
        }

        if (comma = (ch == ',')) {
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
    public final void skipLineComment() {
        while (true) {
            if (ch == '\n') {
                offset++;

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

            offset++;
            if (offset >= end) {
                ch = EOI;
                return;
            }
            ch = (char) bytes[offset];
        }
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
                if (offset >= end) {
                    throw new JSONException("invalid escape character EOI");
                }

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
                                            ((c & 0x1F) << 6) | (c2 & 0x3F));
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
                int strlen = offset - this.offset;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[this.offset] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[this.offset] & 0xff),
                            (char) (bytes[this.offset + 1] & 0xff)
                    );
                } else if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[strlen];
                    for (int i = 0; i < strlen; ++i) {
                        chars[i] = (char) bytes[this.offset + i];
                    }

                    str = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                } else if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes = Arrays.copyOfRange(this.bytes, this.offset, offset);
                    str = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                } else {
                    str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.ISO_8859_1);
                }
            } else {
                str = new String(bytes, this.offset, offset - this.offset, StandardCharsets.UTF_8);
            }

            if ((context.features & Feature.TrimString.mask) != 0) {
                str = str.trim();
            }

            clear:
            if (++offset != end) {
                byte e = bytes[offset++];
                while (e <= ' ' && (1L << e & SPACE) != 0) {
                    if (offset == end) {
                        break clear;
                    } else {
                        e = bytes[offset++];
                    }
                }

                if (comma = e == ',') {
                    if (offset == end) {
                        e = EOI;
                    } else {
                        e = bytes[offset++];
                        while (e <= ' ' && (1L << e & SPACE) != 0) {
                            if (offset == end) {
                                e = EOI;
                                break;
                            } else {
                                e = bytes[offset++];
                            }
                        }
                    }
                }

                this.ch = (char) e;
                this.offset = offset;
                return str;
            }

            this.ch = EOI;
            this.comma = false;
            this.offset = offset;
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
    public final void readNumber0() {
        this.wasNull = false;
        this.mag0 = 0;
        this.mag1 = 0;
        this.mag2 = 0;
        this.mag3 = 0;
        this.negative = false;
        this.exponent = 0;
        this.scale = 0;
        int firstOffset = offset;

        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
                nextIfMatch(',');
                wasNull = true;
                return;
            }
        } else if (ch == ',' || ch == '\r' || ch == '\n') {
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
            ch = (char) bytes[offset++];
        } else {
            if (ch == '+') {
                ch = (char) bytes[offset++];
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
            int numDigits = scale > 0 ? offset - 2 - numStart : offset - 1 - numStart;
            if (numDigits > 38) {
                valueType = JSON_TYPE_BIG_DEC;
                stringValue = new String(bytes, numStart, offset - 1 - numStart);
            } else {
                bigInt(bytes, numStart, offset - 1);
            }
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
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
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

            this.exponent = (short) expValue;
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
                this.offset = firstOffset;
                this.ch = quote;
                readString0();
                valueType = JSON_TYPE_STRING;
                return;
            }

            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
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
                ch = (char) bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            if (this.offset >= end) {
                this.ch = EOI;
            } else {
                this.ch = (char) bytes[this.offset++];
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
    public final void readNumber(ValueConsumer consumer, boolean quoted) {
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
                if (expValue > MAX_EXP) {
                    throw new JSONException("too large exp value : " + expValue);
                }
                ch = (char) bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
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

        if (comma = (ch == ',')) {
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
    public final boolean readIfNull() {
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
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : (char) bytes[offset++];

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
    public final boolean isNull() {
        return ch == 'n' && offset < end && bytes[offset] == 'u';
    }

    @Override
    public final Date readNullOrNewDate() {
        Date date = null;
        if (offset + 2 < end
                && bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset + 3];
            }
            offset += 4;
        } else if (offset + 1 < end
                && bytes[offset] == 'e'
                && bytes[offset + 1] == 'w') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset + 2];
            }
            offset += 3;

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }

            if (offset + 4 < end
                    && ch == 'D'
                    && bytes[offset] == 'a'
                    && bytes[offset + 1] == 't'
                    && bytes[offset + 2] == 'e') {
                if (offset + 3 == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset + 3];
                }
                offset += 4;
            } else {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }

            if (ch != '(' || offset >= end) {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }

            long millis = readInt64Value();

            if (ch != ')') {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }

            date = new Date(millis);
        } else {
            throw new JSONException("json syntax error, not match null or new Date" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }

        return date;
    }

    @Override
    public final boolean nextIfNull() {
        if (ch == 'n' && offset + 2 < end && bytes[offset] == 'u') {
            this.readNull();
            return true;
        }
        return false;
    }

    @Override
    public final void readNull() {
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
        if (comma = (ch == ',')) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }

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
    public final int getStringLength() {
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
    protected final ZonedDateTime readZonedDateTimeX(int len) {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        if (len < 19) {
            return null;
        }

        ZonedDateTime zdt = DateUtils.parseZonedDateTime(bytes, offset, len, context.zoneId);
        if (zdt == null) {
            return null;
        }

        offset += (len + 1);
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return zdt;
    }

    @Override
    public final LocalDate readLocalDate8() {
        if (!isString()) {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate8(bytes, offset);
        } catch (DateTimeException ex) {
            throw new JSONException(info("read date error"), ex);
        }

        offset += 9;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public final LocalDate readLocalDate9() {
        if (!isString()) {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate9(bytes, offset);
        } catch (DateTimeException ex) {
            throw new JSONException(info("read date error"), ex);
        }

        offset += 10;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public final LocalDate readLocalDate10() {
        if (!isString()) {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate10(bytes, offset);
        } catch (DateTimeException ex) {
            throw new JSONException(info("read date error"), ex);
        }
        if (ldt == null) {
            return null;
        }

        offset += 11;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDate readLocalDate11() {
        if (!isString()) {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt = DateUtils.parseLocalDate11(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 11;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime17() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime17(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 18;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalTime readLocalTime5() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime5(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 6;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime8() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime8(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 9;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime10() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime10(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 11;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime11() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime11(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 12;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime12() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime12(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 13;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime18() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime18(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 19;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalDateTime readLocalDateTime12() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime12(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 13;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime14() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime14(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 15;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime16() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime16(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 17;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime18() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime18(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 19;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime19() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime19(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 20;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    protected final LocalDateTime readLocalDateTime20() {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime20(bytes, offset);
        if (ldt == null) {
            return null;
        }

        offset += 21;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    @Override
    public final long readMillis19() {
        char quote = ch;
        if (quote != '"' && quote != '\'') {
            throw new JSONException("date only support string input");
        }

        if (offset + 18 >= end) {
            wasNull = true;
            return 0;
        }

        long millis = DateUtils.parseMillis19(bytes, offset, context.zoneId);

        if (bytes[offset + 19] != quote) {
            throw new JSONException(info("illegal date input"));
        }
        offset += 20;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return millis;
    }

    @Override
    protected final LocalDateTime readLocalDateTimeX(int len) {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTimeX(bytes, offset, len);
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
    public final UUID readUUID() {
        if (ch == 'n') {
            readNull();
            return null;
        }

        if (ch != '"' && ch != '\'') {
            throw new JSONException(info("syntax error, can not read uuid"));
        }
        final char quote = ch;

        if (offset + 32 < bytes.length && bytes[offset + 32] == quote) {
            long msb1 = TypeUtils.uuidNibbles(bytes, offset + 0);
            long msb2 = TypeUtils.uuidNibbles(bytes, offset + 4);
            long msb3 = TypeUtils.uuidNibbles(bytes, offset + 8);
            long msb4 = TypeUtils.uuidNibbles(bytes, offset + 12);
            long lsb1 = TypeUtils.uuidNibbles(bytes, offset + 16);
            long lsb2 = TypeUtils.uuidNibbles(bytes, offset + 20);
            long lsb3 = TypeUtils.uuidNibbles(bytes, offset + 24);
            long lsb4 = TypeUtils.uuidNibbles(bytes, offset + 28);
            if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                offset += 33;
                if (offset == end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }

                if (comma = (ch == ',')) {
                    next();
                }
                return new UUID(
                        msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                        lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
            }
        } else if (offset + 36 < bytes.length && bytes[offset + 36] == quote) {
            char ch1 = (char) bytes[offset + 8];
            char ch2 = (char) bytes[offset + 13];
            char ch3 = (char) bytes[offset + 18];
            char ch4 = (char) bytes[offset + 23];
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                long msb1 = TypeUtils.uuidNibbles(bytes, offset + 0);
                long msb2 = TypeUtils.uuidNibbles(bytes, offset + 4);
                long msb3 = TypeUtils.uuidNibbles(bytes, offset + 9);
                long msb4 = TypeUtils.uuidNibbles(bytes, offset + 14);
                long lsb1 = TypeUtils.uuidNibbles(bytes, offset + 19);
                long lsb2 = TypeUtils.uuidNibbles(bytes, offset + 24);
                long lsb3 = TypeUtils.uuidNibbles(bytes, offset + 28);
                long lsb4 = TypeUtils.uuidNibbles(bytes, offset + 32);
                if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0) {
                    offset += 37;
                    if (offset == end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }

                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = (char) bytes[offset++];
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
    public final String readPattern() {
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

        if (comma = (b == ',')) {
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

    @Override
    public boolean nextIfNullOrEmptyString() {
        final char first = this.ch;
        if (first == 'n' && offset + 2 < end && bytes[offset] == 'u') {
            this.readNull();
            return true;
        }

        if ((first != '"' && first != '\'') || offset >= end || this.bytes[offset] != first) {
            return false;
        }
        offset++;
        this.ch = offset == end ? EOI : (char) bytes[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                ch = EOI;
                return true;
            }
            ch = (char) bytes[offset];
        }

        if (comma = (ch == ',')) {
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
    public final boolean nextIfMatchIdent(char c0, char c1, char c2) {
        if (ch != c0) {
            return false;
        }

        int offset2 = offset + 2;
        if (offset2 > end || bytes[offset] != c1 || bytes[offset + 1] != c2) {
            return false;
        }

        if (offset2 == end) {
            offset = offset2;
            this.ch = EOI;
            return true;
        }

        int offset = offset2;
        char ch = (char) bytes[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset];
        }
        if (offset == offset2) {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        if (ch != c0) {
            return false;
        }

        int offset3 = offset + 3;
        if (offset3 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3) {
            return false;
        }

        if (offset3 == end) {
            offset = offset3;
            this.ch = EOI;
            return true;
        }

        int offset = offset3;
        char ch = (char) bytes[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset];
        }
        if (offset == offset3 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        if (ch != c0) {
            return false;
        }

        int offset4 = offset + 4;
        if (offset4 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3
                || bytes[offset + 3] != c4) {
            return false;
        }

        if (offset4 == end) {
            offset = offset4;
            this.ch = EOI;
            return true;
        }

        int offset = offset4;
        char ch = (char) bytes[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset];
        }
        if (offset == offset4 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        if (ch != c0) {
            return false;
        }

        int offset5 = offset + 5;
        if (offset5 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3
                || bytes[offset + 3] != c4
                || bytes[offset + 4] != c5) {
            return false;
        }

        if (offset5 == end) {
            offset = offset5;
            this.ch = EOI;
            return true;
        }

        int offset = offset5;
        char ch = (char) bytes[offset];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset == end) {
                ch = EOI;
                break;
            }
            ch = (char) bytes[offset];
        }
        if (offset == offset5 && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset + 1;
        this.ch = ch;
        return true;
    }

    @Override
    public final byte[] readHex() {
        if (ch == 'x') {
            next();
        }
        final char quote = ch;
        if (quote != '\'' && quote != '"') {
            throw new JSONException("illegal state. " + ch);
        }
        int start = offset;
        offset++;

        for (; ; ) {
            ch = (char) bytes[offset++];
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                // continue;
            } else if (ch == quote) {
                ch = (char) bytes[offset++];
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
            byte c0 = this.bytes[start + i * 2];
            byte c1 = this.bytes[start + i * 2 + 1];

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            bytes[i] = (byte) ((b0 << 4) | b1);
        }

        nextIfMatch(',');

        return bytes;
    }

    @Override
    public final boolean isReference() {
        if (ch != '{') {
            return false;
        }

        final int start = this.offset;

        ch = (char) bytes[this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = (char) bytes[offset];
        }

        char quote = ch;
        if (quote != '"' && quote != '\'' || this.offset + 5 >= end) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        if (bytes[offset + 1] != '$'
                || bytes[offset + 2] != 'r'
                || bytes[offset + 3] != 'e'
                || bytes[offset + 4] != 'f'
                || bytes[offset + 5] != quote
        ) {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        offset += 6;
        ch = (char) bytes[this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = (char) bytes[offset];
        }

        if (ch != ':') {
            this.offset = start;
            this.ch = '{';
            return false;
        }

        ch = (char) bytes[++this.offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.offset = start;
                this.ch = '{';
                return false;
            }
            ch = (char) bytes[offset];
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
    public final String readReference() {
        if (referenceBegin == end) {
            return null;
        }
        this.offset = referenceBegin;
        this.ch = (char) bytes[offset++];
        String reference = readString();

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.ch = EOI;
                return reference;
            }
            ch = (char) bytes[offset];
        }

        if (ch != '}') {
            throw new JSONException("illegal reference : " + reference);
        }

        if (offset == end) {
            ch = EOI;
        } else {
            ch = (char) bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
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

        return reference;
    }

    @Override
    public final String info(String message) {
        int line = 1, column = 1;
        for (int i = 0; i < offset && i < end; i++, column++) {
            if (bytes[i] == '\n') {
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

        String str = new String(bytes, this.start, length < 65535 ? length : 65535);
        buf.append(str);
        return buf.toString();
    }

    @Override
    public final void close() {
        if (cacheItem != null) {
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }
}

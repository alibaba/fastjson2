package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.IOUtils.ALSE;
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
        super(ctx, false, true);

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        int bufferSize = ctx.bufferSize;
        if (bytes == null) {
            bytes = new byte[bufferSize];
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
                    bytes = Arrays.copyOf(bytes, bytes.length + bufferSize);
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

        while (ch == '/' && this.offset < this.bytes.length && this.bytes[this.offset] == '/') {
            skipLineComment();
        }
    }

    JSONReaderUTF8(Context ctx, ByteBuffer buffer) {
        super(ctx, false, true);

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

        while (ch == '/' && this.offset < this.bytes.length && this.bytes[this.offset] == '/') {
            skipLineComment();
        }
    }

    JSONReaderUTF8(Context ctx, String str, byte[] bytes, int offset, int length) {
        super(ctx, false, true);

        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.in = null;
        this.start = offset;
        this.end = offset + length;
        this.cacheItem = null;
        next();
    }

    @Override
    public boolean nextIfMatch(char e) {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = bytes[offset++];
            }
        }

        if (ch != e) {
            return false;
        }
        comma = (ch == ',');

        if (offset >= end) {
            this.offset = offset;
            this.ch = EOI;
            return true;
        }

        ch = bytes[offset];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                this.offset = offset;
                this.ch = EOI;
                return true;
            }
            ch = bytes[offset];
        }

        if (ch >= 0) {
            offset++;
        } else {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    /* 110x xxxx   10xx xxxx*/
                    offset += 2;
                    int char2 = bytes[offset - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new JSONException(
                                "malformed input around byte " + offset);
                    }
                    ch = ((ch & 0x1F) << 6) | (char2 & 0x3F);
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
                    ch = (((ch & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    break;
                }
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new JSONException("malformed input around byte " + offset);
            }
        }

        this.offset = offset;
        this.ch = (char) ch;
        while (this.ch == '/' && offset < bytes.length && bytes[offset] == '/') {
            skipLineComment();
        }

        return true;
    }

    @Override
    public boolean nextIfComma() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        if (ch != ',') {
            this.offset = offset;
            this.ch = (char) ch;
            return false;
        }
        comma = true;

        if (offset >= end) {
            this.offset = offset;
            this.ch = EOI;
            return true;
        }

        ch = bytes[offset];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                this.offset = offset;
                this.ch = EOI;
                return true;
            }
            ch = bytes[offset];
        }

        if (ch >= 0) {
            offset++;
        } else {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    /* 110x xxxx   10xx xxxx*/
                    offset += 2;
                    int char2 = bytes[offset - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new JSONException(
                                "malformed input around byte " + offset);
                    }
                    ch = ((ch & 0x1F) << 6) | (char2 & 0x3F);
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
                    ch = (((ch & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    break;
                }
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new JSONException("malformed input around byte " + offset);
            }
        }

        this.offset = offset;
        this.ch = (char) ch;
        while (this.ch == '/' && offset < bytes.length && bytes[offset] == '/') {
            skipLineComment();
        }

        return true;
    }

    @Override
    public boolean nextIfArrayStart() {
        int ch = this.ch;
        if (ch != '[') {
            return false;
        }

        int offset = this.offset;
        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        byte[] bytes = this.bytes;

        ch = bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            if (offset >= end) {
                this.ch = EOI;
                this.offset = offset;
                return true;
            }
            ch = bytes[offset++];
        }

        validate:
        if (ch < 0) {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    int c2 = bytes[offset];
                    if ((c2 & 0xC0) != 0x80) {
                        break;
                    } else {
                        ch = ((ch & 0x1F) << 6) | (c2 & 0x3F);
                        offset += 1;
                        break validate;
                    }
                }
                case 14: {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                        break;
                    } else {
                        ch = (((ch & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
                        offset += 2;
                        break validate;
                    }
                }
            }
            throw new JSONException("malformed input around byte " + offset);
        }

        this.ch = (char) ch;
        this.offset = offset;

        while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
            skipLineComment();
        }
        return true;
    }

    @Override
    public boolean nextIfArrayEnd() {
        int ch = this.ch;
        if (ch == '}' || ch == EOI) {
            throw new JSONException(info("Illegal syntax: `" + (char) ch + '`'));
        }

        if (ch != ']') {
            return false;
        }

        int offset = this.offset;
        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        byte[] bytes = this.bytes;

        ch = bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            if (offset >= end) {
                this.ch = EOI;
                this.offset = offset;
                return true;
            }
            ch = bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = bytes[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                if (offset >= end) {
                    this.ch = EOI;
                    this.offset = offset;
                    return true;
                }
                ch = bytes[offset++];
            }
        }

        validate:
        if (ch < 0) {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    int c2 = bytes[offset];
                    if ((c2 & 0xC0) != 0x80) {
                        break;
                    } else {
                        ch = ((ch & 0x1F) << 6) | (c2 & 0x3F);
                        offset += 1;
                        break validate;
                    }
                }
                case 14: {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                        break;
                    } else {
                        ch = (((ch & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
                        offset += 2;
                        break validate;
                    }
                }
            }
            throw new JSONException("malformed input around byte " + offset);
        }

        this.ch = (char) ch;
        this.offset = offset;

        while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
            skipLineComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfSet() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        byte ch = (byte) this.ch;
        if (ch == 'S'
                && offset + 1 < end
                && bytes[offset] == 'e'
                && bytes[offset + 1] == 't') {
            offset += 2;
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = bytes[offset++];
                }
            }

            this.offset = offset;
            this.ch = (char) ch;
            return true;
        }
        return false;
    }

    @Override
    public final boolean nextIfInfinity() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        byte ch = (byte) this.ch;
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
                ch = EOI;
            } else {
                ch = bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset == end) {
                        ch = EOI;
                        break;
                    }
                    ch = bytes[offset++];
                }
            }

            this.offset = offset;
            this.ch = (char) ch;
            return true;
        }
        return false;
    }

    public boolean nextIfObjectStart() {
        int ch = this.ch;
        if (ch != '{') {
            return false;
        }

        int offset = this.offset;
        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        byte[] bytes = this.bytes;

        ch = bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            if (offset >= end) {
                this.ch = EOI;
                this.offset = offset;
                return true;
            }
            ch = bytes[offset++];
        }

        validate:
        if (ch < 0) {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    int c2 = bytes[offset];
                    if ((c2 & 0xC0) != 0x80) {
                        break;
                    } else {
                        ch = ((ch & 0x1F) << 6) | (c2 & 0x3F);
                        offset += 1;
                        break validate;
                    }
                }
                case 14: {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                        break;
                    } else {
                        ch = (((ch & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
                        offset += 2;
                        break validate;
                    }
                }
            }
            throw new JSONException("malformed input around byte " + offset);
        }

        this.ch = (char) ch;
        this.offset = offset;

        while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
            skipLineComment();
        }
        return true;
    }

    @Override
    public boolean nextIfObjectEnd() {
        int ch = this.ch;
        if (ch == ']' || ch == EOI) {
            throw new JSONException(info("Illegal syntax: `" + (char) ch + '`'));
        }

        if (ch != '}') {
            return false;
        }

        int offset = this.offset;
        if (offset >= end) {
            this.ch = EOI;
            return true;
        }

        byte[] bytes = this.bytes;

        ch = bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            if (offset >= end) {
                this.ch = EOI;
                this.offset = offset;
                return true;
            }
            ch = bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = bytes[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                if (offset >= end) {
                    this.ch = EOI;
                    this.offset = offset;
                    return true;
                }
                ch = bytes[offset++];
            }
        }

        validate:
        if (ch < 0) {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    int c2 = bytes[offset];
                    if ((c2 & 0xC0) != 0x80) {
                        break;
                    } else {
                        ch = ((ch & 0x1F) << 6) | (c2 & 0x3F);
                        offset += 1;
                        break validate;
                    }
                }
                case 14: {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                        break;
                    } else {
                        ch = (((ch & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
                        offset += 2;
                        break validate;
                    }
                }
            }
            throw new JSONException("malformed input around byte " + offset);
        }

        this.ch = (char) ch;
        this.offset = offset;

        while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
            skipLineComment();
        }
        return true;
    }

    @Override
    public void next() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset >= end) {
            ch = EOI;
            return;
        }

        int ch = bytes[offset];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return;
            }
            ch = bytes[offset];
        }

        if (ch >= 0) {
            this.offset = offset + 1;
            this.ch = (char) ch;

            while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
                skipLineComment();
            }
            return;
        }

        ch &= 0xFF;
        switch (ch >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                offset += 2;
                int char2 = bytes[offset - 1];
                if ((char2 & 0xC0) != 0x80) {
                    throw new JSONException(
                            "malformed input around byte " + offset);
                }
                ch = ((ch & 0x1F) << 6) | (char2 & 0x3F);
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
                ch = ((ch & 0x0F) << 12)
                        | ((char2 & 0x3F) << 6)
                        | (char3 & 0x3F);
                break;
            }
            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new JSONException("malformed input around byte " + offset);
        }

        this.offset = offset;
        this.ch = (char) ch;
        while (this.ch == '/' && this.offset < bytes.length && bytes[this.offset] == '/') {
            skipLineComment();
        }
    }

    @Override
    public final long readFieldNameHashCodeUnquote() {
        this.nameEscape = false;
        this.nameBegin = this.offset - 1;
        char first = ch;

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
    public final int getRawInt() {
        if (offset + 3 < bytes.length) {
            return UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 1);
        }
        return 0;
    }

    @Override
    public final long getRawLong() {
        if (offset + 8 < bytes.length) {
            return UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 1);
        }
        return 0;
    }

    @Override
    public final boolean nextIfName8Match2() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 9;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName8Match1() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 8;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName8Match0() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 7;

        if (offset == end) {
            this.ch = EOI;
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;
        return true;
    }

    @Override
    public final boolean nextIfName4Match2() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 4;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match3() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 5;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match4(byte c4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 6;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 3] != c4 || bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match5(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 7;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name1) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match6(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 8;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name1 || bytes[offset - 1] != ':') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match7(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 9;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name1
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match8(int name1, byte c8) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 10;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name1
                || bytes[offset - 3] != c8
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match9(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 11;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name1) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match10(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 12;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name1
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match11(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 13;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name1
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match12(long name1, byte c12) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 14;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name1
                || bytes[offset - 3] != c12
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match13(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 15;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name2
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match14(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 16;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name2
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match15(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 17;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name2
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match16(long name1, int name2, byte c16) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 18;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name2
                || bytes[offset - 3] != c16
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match17(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 19;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name2
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match18(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 20;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name2
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match19(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 21;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 18) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name2
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match20(long name1, long name2, byte c20) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 22;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 19) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name2
                || bytes[offset - 3] != c20
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match21(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 23;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 20) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name3
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match22(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 24;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 21) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name3
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match23(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 25;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 22) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match24(long name1, long name2, int name3, byte c24) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 26;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 23) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name3
                || bytes[offset - 3] != c24
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match25(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 27;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 24) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name3
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match26(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 28;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 25) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name3
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match27(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 29;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 26) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 18) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match28(long name1, long name2, long name3, byte c29) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 30;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 27) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 19) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name3
                || bytes[offset - 3] != c29
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match29(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 31;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 28) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 20) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name4
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match30(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 32;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 29) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 21) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name4
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match31(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 33;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 30) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 22) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name4
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match32(long name1, long name2, long name3, int name4, byte c32) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 34;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 31) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 23) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name4
                || bytes[offset - 3] != c32
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match33(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 35;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 32) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 24) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name4
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match34(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 36;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 33) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 25) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name4
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match35(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 37;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 34) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 26) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 18) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name4
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match36(long name1, long name2, long name3, long name4, byte c36) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 38;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 35) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 27) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 19) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name4
                || bytes[offset - 3] != c36
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match37(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 39;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 36) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 28) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 20) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name4
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name5
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match38(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 40;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 37) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 29) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 21) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name4
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name5
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match39(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 41;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 38) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 30) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 22) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name4
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name5
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match40(long name1, long name2, long name3, long name4, int name5, byte c40) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 42;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 39) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 31) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 23) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name4
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name5
                || bytes[offset - 3] != c40
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match41(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 43;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 40) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 32) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 24) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name4
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name5
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match42(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 44;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 41) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 33) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 25) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name4
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name5
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match43(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 45;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 42) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 34) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 26) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 18) != name4
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name5
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match2() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 3;

        if (offset >= end) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match3() {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 4;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 1] != '"') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match4(byte c4) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 5;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 2] != c4 || bytes[offset - 1] != '"') {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match5(byte c4, byte c5) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 6;

        if (offset >= end) {
            return false;
        }

        if (bytes[offset - 3] != c4
                || bytes[offset - 2] != c5
                || bytes[offset - 1] != '"'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match6(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 7;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name1) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match7(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 8;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name1
                || bytes[offset - 1] != '"'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match8(int name1, byte c8) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 9;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name1
                || bytes[offset - 2] != c8
                || bytes[offset - 1] != '"'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match9(int name1, byte c8, byte c9) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 10;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name1
                || bytes[offset - 3] != c8
                || bytes[offset - 2] != c9
                || bytes[offset - 1] != '"'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match10(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 11;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name1) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match11(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset;
        offset += 12;

        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name1
                || bytes[offset - 1] != '"'
        ) {
            return false;
        }

        int c = bytes[offset] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : (bytes[offset] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = bytes[offset] & 0xff;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public long readFieldNameHashCode() {
        final byte[] bytes = this.bytes;
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

        if (nameValue == 0) {
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
                                    (c3 & 0x3F));
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
                                    (c3 & 0x3F));
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
                                                ((byte) 0x80))));

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
                                continue;
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
                                    | (c3 & 0x3F));

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
                        c = (((c & 0x0F) << 12)
                                | ((char2 & 0x3F) << 6)
                                | ((char3 & 0x3F)));
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
            if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0 && isFirstIdentifier(ch)) {
                return readFieldNameUnquote();
            }

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
                        long nameValue01 = nameValue0 ^ nameValue1;
                        int indexMask = ((int) (nameValue01 ^ (nameValue01 >>> 32))) & (NAME_CACHE2.length - 1);
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
                        int indexMask = ((int) (nameValue0 ^ (nameValue0 >>> 32))) & (NAME_CACHE.length - 1);
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
        boolean valid = false;
        boolean negative = false;
        int firstOffset = offset;
        final char firstChar = ch;
        final byte[] bytes = this.bytes;

        int intValue = 0;

        char quote = '\0';
        if (firstChar == '"' || firstChar == '\'') {
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
            valid = true;
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
                    throw new JSONException("int overflow, value " + bigInteger);
                }
            } else {
                return getInt32Value();
            }
        }

        if (quote != 0) {
            wasNull = firstOffset + 1 == offset;
            if (wasNull) {
                valid = true;
            }
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

        if (valid) {
            return negative ? -intValue : intValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final Integer readInt32() {
        boolean valid = false;
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
                    while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        if (offset >= end) {
                            ch = EOI;
                        } else {
                            ch = (char) bytes[offset++];
                        }
                    }

                    nextIfComma();
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
            valid = true;
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

        if (valid) {
            return negative ? -intValue : intValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final long readInt64Value() {
        boolean valid = false;
        boolean negative = false;
        final int firstOffset = offset;
        final char firstChar = ch;
        final byte[] bytes = this.bytes;

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
            valid = true;
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
            if (wasNull) {
                valid = true;
            }
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

        if (valid) {
            return negative ? -longValue : longValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final Long readInt64() {
        boolean valid = false;
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

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    if (offset >= end) {
                        ch = EOI;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }

                nextIfComma();
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
                nextIfComma();
                return null;
            }
        } else if (ch == '+') {
            ch = (char) bytes[offset++];
        }

        boolean overflow = false;
        while (ch >= '0' && ch <= '9') {
            valid = true;
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

        if (valid) {
            return negative ? -longValue : longValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final double readDoubleValue() {
        boolean valid = false;
        this.wasNull = false;

        boolean value = false;
        double doubleValue = 0;

        final byte[] bytes = this.bytes;
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
                nextIfComma();
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            negative = false;
            if (ch == '+') {
                ch = (char) bytes[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        boolean overflow = false;
        long longValue = 0;
        while (ch >= '0' && ch <= '9') {
            valid = true;
            if (!overflow) {
                long intValue10 = longValue * 10 + (ch - '0');
                if (intValue10 < longValue) {
                    overflow = true;
                } else {
                    longValue = intValue10;
                }
            }

            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) bytes[offset++];
        }

        this.scale = 0;
        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                valid = true;
                this.scale++;
                if (!overflow) {
                    long intValue10 = longValue * 10 + (ch - '0');
                    if (intValue10 < longValue) {
                        overflow = true;
                    } else {
                        longValue = intValue10;
                    }
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
        }

        int expValue = 0;
        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            ch = (char) bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) bytes[offset++];
            } else if (ch == '+') {
                ch = (char) bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                valid = true;
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
                    valid = true;
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
                    valid = true;
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
                if (offset + 4 <= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE) {
                    valid = true;
                    offset += 4;
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
                valid = true;
                Map<String, Object> obj = readObject();
                if (!obj.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            } else if (ch == '[' && quote == 0) {
                valid = true;
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
            if (!overflow) {
                if (longValue == 0) {
                    if (scale == 1) {
                        doubleValue = 0;
                        value = true;
                    }
                } else {
                    int scale = this.scale - expValue;
                    if (scale == 0) {
                        doubleValue = (double) longValue;
                        if (negative) {
                            doubleValue = -doubleValue;
                        }
                        value = true;
                    } else if ((long) (double) longValue == longValue) {
                        if (0 < scale && scale < DOUBLE_10_POW.length) {
                            doubleValue = (double) longValue / DOUBLE_10_POW[scale];
                            if (negative) {
                                doubleValue = -doubleValue;
                            }
                            value = true;
                        } else if (0 > scale && scale > -DOUBLE_10_POW.length) {
                            doubleValue = (double) longValue * DOUBLE_10_POW[-scale];
                            if (negative) {
                                doubleValue = -doubleValue;
                            }
                            value = true;
                        }
                    }

                    if (!value && scale > -128 && scale < 128) {
                        doubleValue = TypeUtils.doubleValue(negative ? -1 : 1, longValue, scale);
                        value = true;
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
                    doubleValue = TypeUtils.parseDouble(bytes, start - 1, len);
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

        if (valid) {
            return doubleValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final float readFloatValue() {
        boolean valid = false;
        this.wasNull = false;

        boolean value = false;
        float floatValue = 0;

        byte[] bytes = this.bytes;
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
                nextIfComma();
                wasNull = true;
                return 0;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            negative = false;
            if (ch == '+') {
                ch = (char) bytes[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        boolean overflow = false;
        long longValue = 0;

        while (ch >= '0' && ch <= '9') {
            valid = true;
            if (!overflow) {
                long intValue10 = longValue * 10 + (ch - '0');
                if (intValue10 < longValue) {
                    overflow = true;
                } else {
                    longValue = intValue10;
                }
            }

            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) bytes[offset++];
        }

        this.scale = 0;
        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                valid = true;
                this.scale++;
                if (!overflow) {
                    long intValue10 = longValue * 10 + (ch - '0');
                    if (intValue10 < longValue) {
                        overflow = true;
                    } else {
                        longValue = intValue10;
                    }
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
        }

        int expValue = 0;
        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            ch = (char) bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) bytes[offset++];
            } else if (ch == '+') {
                ch = (char) bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                valid = true;
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
                    valid = true;
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
                    valid = true;
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
                if (offset + 4 <= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE) {
                    valid = true;
                    offset += 4;
                    floatValue = 0;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
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
                overflow = true;
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
            if (!overflow) {
                int scale = this.scale - expValue;
                if (scale == 0) {
                    floatValue = (float) longValue;
                    if (negative) {
                        floatValue = -floatValue;
                    }
                    value = true;
                } else if ((long) (float) longValue == longValue) {
                    if (0 < scale && scale < FLOAT_10_POW.length) {
                        floatValue = (float) longValue / FLOAT_10_POW[scale];
                        if (negative) {
                            floatValue = -floatValue;
                        }
                    } else if (0 > scale && scale > -FLOAT_10_POW.length) {
                        floatValue = (float) longValue * FLOAT_10_POW[-scale];
                        if (negative) {
                            floatValue = -floatValue;
                        }
                    }
                }

                if (!value && scale > -128 && scale < 128) {
                    floatValue = TypeUtils.floatValue(negative ? -1 : 1, longValue, scale);
                    value = true;
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

        if (valid) {
            return floatValue;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final void readString(ValueConsumer consumer, boolean quoted) {
        char quote = this.ch;
        int valueLength;
        int offset = this.offset;
        int start = offset;
        valueEscape = false;

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
                    break;
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
                                        ((c3 & 0x3F)));
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
                                            ((byte) 0x80))));

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
                    break;
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
                                                (c3 & 0x3F));
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
                                                    ((byte) 0x80))));

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
//                boolean space = false;
                boolean num = false;
                if (!dot && (ch >= '0' && ch <= '9')) {
                    num = true;
                    do {
                        if (offset < end) {
                            ch = (char) (bytes[offset++] & 0xff);
                        } else {
                            ch = EOI;
                            return;
                        }
                    } while (ch >= '0' && ch <= '9');
                }

                if (num && (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S')) {
                    next();
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
                        do {
                            if (offset < end) {
                                ch = (char) (bytes[offset++] & 0xff);
                            } else {
                                ch = EOI;
                                return;
                            }
                        } while (ch >= '0' && ch <= '9');
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
                        do {
                            if (offset < end) {
                                ch = (char) (bytes[offset++] & 0xff);
                            } else {
                                ch = EOI;
                                return;
                            }
                        } while (ch >= '0' && ch <= '9');
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
                if (offset + 4 >= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) != ALSE) {
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
                        c = (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F)));
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
                                            ((byte) 0x80))));

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
                            continue;
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
        byte ch = (byte) this.ch;
        byte quote = ch;

        byte[] bytes = this.bytes;
        int offset = this.offset;
        ch = bytes[offset++];
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
                ch = offset < end ? bytes[offset++] : (byte) EOI;
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
                this.offset = offset;
                this.ch = EOI;
                return;
            }

            ch = bytes[offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                offset++;
                if (offset >= end) {
                    this.ch = EOI;
                    this.offset = offset;
                    return;
                }
                ch = bytes[offset];
            }
            offset++;
        }
        this.offset = offset;
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
                        break;
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
                                                    ((c3 & 0x3F)));
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
                                                        ((byte) 0x80))));

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
                throw new JSONException(info("illegal input : " + ch));
        }
    }

    @Override
    public final void readNumber0() {
        boolean valid = false;
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
                nextIfComma();
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
            valid = true;
            if (!intOverflow) {
                int digit = ch - '0';
                mag3 *= 10;
                if (mag3 < multmin) {
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
                valid = true;
                if (!intOverflow) {
                    int digit = ch - '0';
                    mag3 *= 10;
                    if (mag3 < multmin) {
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
                valid = true;
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
                    valid = true;
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
                    valid = true;
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
                if (offset + 4 <= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE) {
                    valid = true;
                    offset += 4;
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

        if (!valid) {
            throw new JSONException(info("illegal input error"));
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
                if (offset + 4 <= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE) {
                    offset += 4;
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
            if (mag0 == 0 && mag1 == 0 && mag2 == 0 && mag3 != Integer.MIN_VALUE) {
                int intValue = negative ? -mag3 : mag3;
                consumer.accept(intValue);
                return;
            }

            if (mag0 == 0 && mag1 == 0) {
                long v3 = mag3 & 0XFFFFFFFFL;
                long v2 = mag2 & 0XFFFFFFFFL;

                if (v2 <= Integer.MAX_VALUE) {
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
        int offset = this.offset;
        final byte[] bytes = this.bytes;
        if (ch == 'n'
                && offset + 2 < end
                && bytes[offset] == 'u'
        ) {
            this.readNull();
            return true;
        }
        return false;
    }

    @Override
    public final void readNull() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        char ch = this.ch;
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
        this.ch = ch;
        this.offset = offset;
    }

    @Override
    public final int getStringLength() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("string length only support string input");
        }
        final char quote = ch;

        int len = 0;
        int i = offset;
        byte[] bytes = this.bytes;

        int i8 = i + 8;
        if (i8 < end && i8 < bytes.length) {
            if (bytes[i] != quote
                    && bytes[i + 1] != quote
                    && bytes[i + 2] != quote
                    && bytes[i + 3] != quote
                    && bytes[i + 4] != quote
                    && bytes[i + 5] != quote
                    && bytes[i + 6] != quote
                    && bytes[i + 7] != quote
            ) {
                i += 8;
                len += 8;
            }
        }

        for (; i < end; ++i, ++len) {
            if (bytes[i] == quote) {
                break;
            }
        }
        return len;
    }

    public final LocalDate readLocalDate() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (ch == '"' || ch == '\'') {
            Context context = this.context;
            if (context.dateFormat == null
                    || context.formatyyyyMMddhhmmss19
                    || context.formatyyyyMMddhhmmssT19
                    || context.formatyyyyMMdd8
                    || context.formatISO8601
            ) {
                char quote = ch;
                int c10 = offset + 10;
                if (c10 < bytes.length
                        && c10 < end
                        && bytes[offset + 4] == '-'
                        && bytes[offset + 7] == '-'
                        && bytes[offset + 10] == quote
                ) {
                    byte y0 = bytes[offset];
                    byte y1 = bytes[offset + 1];
                    byte y2 = bytes[offset + 2];
                    byte y3 = bytes[offset + 3];
                    byte m0 = bytes[offset + 5];
                    byte m1 = bytes[offset + 6];
                    byte d0 = bytes[offset + 8];
                    byte d1 = bytes[offset + 9];

                    int year;
                    int month;
                    if (y0 >= '0' && y0 <= '9'
                            && y1 >= '0' && y1 <= '9'
                            && y2 >= '0' && y2 <= '9'
                            && y3 >= '0' && y3 <= '9'
                    ) {
                        year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
                        if (m0 >= '0' && m0 <= '9' && m1 >= '0' && m1 <= '9') {
                            month = (m0 - '0') * 10 + (m1 - '0');
                            int dom;
                            if (d0 >= '0' && d0 <= '9' && d1 >= '0' && d1 <= '9') {
                                dom = (d0 - '0') * 10 + (d1 - '0');

                                LocalDate ldt;
                                try {
                                    if (year == 0 && month == 0 && dom == 0) {
                                        ldt = null;
                                    } else {
                                        ldt = LocalDate.of(year, month, dom);
                                    }
                                } catch (DateTimeException ex) {
                                    throw new JSONException(info("read date error"), ex);
                                }

                                this.offset = offset + 11;
                                next();
                                if (comma = (this.ch == ',')) {
                                    next();
                                }
                                return ldt;
                            }
                        }
                    }
                }
            }
        }
        return super.readLocalDate();
    }

    public final OffsetDateTime readOffsetDateTime() {
        final byte[] bytes = this.bytes;
        final int offset = this.offset;
        final Context context = this.context;
        if (this.ch == '"' || this.ch == '\'') {
            if (context.dateFormat == null
                    || context.formatyyyyMMddhhmmss19
                    || context.formatyyyyMMddhhmmssT19
                    || context.formatyyyyMMdd8
                    || context.formatISO8601
            ) {
                char quote = this.ch;
                byte c10;
                int off21 = offset + 19;
                if (off21 < bytes.length
                        && off21 < end
                        && bytes[offset + 4] == '-'
                        && bytes[offset + 7] == '-'
                        && ((c10 = bytes[offset + 10]) == ' ' || c10 == 'T')
                        && bytes[offset + 13] == ':'
                        && bytes[offset + 16] == ':'
                ) {
                    byte y0 = bytes[offset];
                    byte y1 = bytes[offset + 1];
                    byte y2 = bytes[offset + 2];
                    byte y3 = bytes[offset + 3];
                    byte m0 = bytes[offset + 5];
                    byte m1 = bytes[offset + 6];
                    byte d0 = bytes[offset + 8];
                    byte d1 = bytes[offset + 9];
                    byte h0 = bytes[offset + 11];
                    byte h1 = bytes[offset + 12];
                    byte i0 = bytes[offset + 14];
                    byte i1 = bytes[offset + 15];
                    byte s0 = bytes[offset + 17];
                    byte s1 = bytes[offset + 18];

                    int year;
                    int month;
                    if (y0 >= '0' && y0 <= '9'
                            && y1 >= '0' && y1 <= '9'
                            && y2 >= '0' && y2 <= '9'
                            && y3 >= '0' && y3 <= '9'
                    ) {
                        year = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    if (m0 >= '0' && m0 <= '9'
                            && m1 >= '0' && m1 <= '9'
                    ) {
                        month = (m0 - '0') * 10 + (m1 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    int dom;
                    if (d0 >= '0' && d0 <= '9'
                            && d1 >= '0' && d1 <= '9'
                    ) {
                        dom = (d0 - '0') * 10 + (d1 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    int hour;
                    if (h0 >= '0' && h0 <= '9'
                            && h1 >= '0' && h1 <= '9'
                    ) {
                        hour = (h0 - '0') * 10 + (h1 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    int minute;
                    if (i0 >= '0' && i0 <= '9'
                            && i1 >= '0' && i1 <= '9'
                    ) {
                        minute = (i0 - '0') * 10 + (i1 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    int second;
                    if (s0 >= '0' && s0 <= '9'
                            && s1 >= '0' && s1 <= '9'
                    ) {
                        second = (s0 - '0') * 10 + (s1 - '0');
                    } else {
                        return readZonedDateTime().toOffsetDateTime();
                    }

                    LocalDate localDate;
                    try {
                        if (year == 0 && month == 0 && dom == 0) {
                            localDate = null;
                        } else {
                            localDate = LocalDate.of(year, month, dom);
                        }
                    } catch (DateTimeException ex) {
                        throw new JSONException(info("read date error"), ex);
                    }

                    int nanoSize = -1;
                    int len = 0;
                    for (int start = offset + 19, i = start, end = offset + 31; i < end && i < this.end && i < bytes.length; ++i) {
                        if (bytes[i] == quote && bytes[i - 1] == 'Z') {
                            nanoSize = i - start - 2;
                            len = i - offset + 1;
                            break;
                        }
                    }
                    if (nanoSize != -1 || len == 21) {
                        int nano = nanoSize <= 0 ? 0 : DateUtils.readNanos(bytes, nanoSize, offset + 20);
                        LocalTime localTime = LocalTime.of(hour, minute, second, nano);
                        LocalDateTime ldt = LocalDateTime.of(localDate, localTime);
                        OffsetDateTime oft = OffsetDateTime.of(ldt, ZoneOffset.UTC);
                        this.offset += len;
                        next();
                        if (comma = (this.ch == ',')) {
                            next();
                        }
                        return oft;
                    }
                }
            }
        }
        return readZonedDateTime().toOffsetDateTime();
    }

    @Override
    public final OffsetTime readOffsetTime() {
        final byte[] bytes = this.bytes;
        final int offset = this.offset;
        final Context context = this.context;
        if (this.ch == '"' || this.ch == '\'') {
            if (context.dateFormat == null) {
                char quote = this.ch;
                int off10 = offset + 8;
                if (off10 < bytes.length
                        && off10 < end
                        && bytes[offset + 2] == ':'
                        && bytes[offset + 5] == ':'
                ) {
                    byte h0 = bytes[offset];
                    byte h1 = bytes[offset + 1];
                    byte i0 = bytes[offset + 3];
                    byte i1 = bytes[offset + 4];
                    byte s0 = bytes[offset + 6];
                    byte s1 = bytes[offset + 7];

                    int hour;
                    if (h0 >= '0' && h0 <= '9'
                            && h1 >= '0' && h1 <= '9'
                    ) {
                        hour = (h0 - '0') * 10 + (h1 - '0');
                    } else {
                        throw new JSONException(this.info("illegal offsetTime"));
                    }

                    int minute;
                    if (i0 >= '0' && i0 <= '9'
                            && i1 >= '0' && i1 <= '9'
                    ) {
                        minute = (i0 - '0') * 10 + (i1 - '0');
                    } else {
                        throw new JSONException(this.info("illegal offsetTime"));
                    }

                    int second;
                    if (s0 >= '0' && s0 <= '9'
                            && s1 >= '0' && s1 <= '9'
                    ) {
                        second = (s0 - '0') * 10 + (s1 - '0');
                    } else {
                        throw new JSONException(this.info("illegal offsetTime"));
                    }

                    int nanoSize = -1;
                    int len = 0;
                    for (int start = offset + 8, i = start, end = offset + 25; i < end && i < this.end && i < bytes.length; ++i) {
                        byte b = bytes[i];
                        if (nanoSize == -1 && (b == 'Z' || b == '+' || b == '-')) {
                            nanoSize = i - start - 1;
                        }
                        if (b == quote) {
                            len = i - offset;
                            break;
                        }
                    }

                    int nano = nanoSize <= 0 ? 0 : DateUtils.readNanos(bytes, nanoSize, offset + 9);

                    ZoneOffset zoneOffset;
                    int zoneOffsetSize = len - 9 - nanoSize;
                    if (zoneOffsetSize <= 1) {
                        zoneOffset = ZoneOffset.UTC;
                    } else {
                        String zonedId = new String(bytes, offset + 9 + nanoSize, zoneOffsetSize);
                        zoneOffset = ZoneOffset.of(zonedId);
                    }
                    LocalTime localTime = LocalTime.of(hour, minute, second, nano);
                    OffsetTime oft = OffsetTime.of(localTime, zoneOffset);
                    this.offset += len + 2;
                    next();
                    if (comma = (this.ch == ',')) {
                        next();
                    }
                    return oft;
                }
            }
        }
        throw new JSONException(this.info("illegal offsetTime"));
    }

    @Override
    protected final ZonedDateTime readZonedDateTimeX(int len) {
        if (!isString()) {
            throw new JSONException("date only support string input");
        }

        if (len < 19) {
            return null;
        }

        ZonedDateTime zdt;
        if (len == 30 && bytes[offset + 29] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime29(bytes, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 29 && bytes[offset + 28] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime28(bytes, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 28 && bytes[offset + 27] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime27(bytes, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 27 && bytes[offset + 26] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime26(bytes, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else {
            zdt = DateUtils.parseZonedDateTime(bytes, offset, len, context.zoneId);
        }

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
        if (ch != '"' && ch != '\'') {
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
    protected final LocalTime readLocalTime9() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime8(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 10;
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

        LocalDateTime ldt;
        if (bytes[offset + len - 1] == 'Z') {
            ZonedDateTime zdt = DateUtils.parseZonedDateTime(bytes, offset, len);
            ldt = zdt.toLocalDateTime();
        } else {
            ldt = DateUtils.parseLocalDateTimeX(bytes, offset, len);
        }

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

    public final BigDecimal readBigDecimal() {
        boolean valid = false;
        final byte[] bytes = this.bytes;

        boolean value = false;

        BigDecimal decimal = null;
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
                nextIfComma();
                return null;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = (char) bytes[offset++];
        } else {
            negative = false;
            if (ch == '+') {
                ch = (char) bytes[offset++];
            }
        }

        valueType = JSON_TYPE_INT;
        boolean overflow = false;
        long longValue = 0;
        while (ch >= '0' && ch <= '9') {
            valid = true;
            if (!overflow) {
                long r = longValue * 10;
                if ((longValue | 10) >>> 31 == 0L || (r / 10 == longValue)) {
                    longValue = r + (ch - '0');
                } else {
                    overflow = true;
                }
            }

            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = (char) bytes[offset++];
        }

        this.scale = 0;
        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = (char) bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                valid = true;
                this.scale++;
                if (!overflow) {
                    long r = longValue * 10;
                    if ((longValue | 10) >>> 31 == 0L || (r / 10 == longValue)) {
                        longValue = r + (ch - '0');
                    } else {
                        overflow = true;
                    }
                }

                if (offset == end) {
                    ch = EOI;
                    offset++;
                    break;
                }
                ch = (char) bytes[offset++];
            }
        }

        int expValue = 0;
        if (ch == 'e' || ch == 'E') {
            boolean negativeExp = false;
            ch = (char) bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = (char) bytes[offset++];
            } else if (ch == '+') {
                ch = (char) bytes[offset++];
            }

            while (ch >= '0' && ch <= '9') {
                valid = true;
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
                    valid = true;
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    valid = true;
                    value = true;
                    decimal = BigDecimal.ONE;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == 'f') {
                if (offset + 4 <= end && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE) {
                    valid = true;
                    offset += 4;
                    decimal = BigDecimal.ZERO;
                    value = true;
                    if (offset == end) {
                        ch = EOI;
                        offset++;
                    } else {
                        ch = (char) bytes[offset++];
                    }
                }
            } else if (ch == '{' && quote == 0) {
                valid = true;
                JSONObject jsonObject = new JSONObject();
                readObject(jsonObject, 0);
                decimal = decimal(jsonObject);
                value = true;
                wasNull = true;
            } else if (ch == '[' && quote == 0) {
                valid = true;
                List array = readArray();
                if (!array.isEmpty()) {
                    throw new JSONException(info());
                }
                value = true;
                wasNull = true;
            }
        }

        int len = offset - start;

        if (quote != 0) {
            if (ch != quote) {
                this.offset -= 1;
                this.ch = quote;
                String str = readString();
                try {
                    return TypeUtils.toBigDecimal(str);
                } catch (NumberFormatException e) {
                    throw new JSONException(info(e.getMessage()), e);
                }
            } else {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }
        if (!value) {
            if (expValue == 0 && !overflow && longValue != 0) {
                decimal = BigDecimal.valueOf(negative ? -longValue : longValue, scale);
                value = true;
            }

            if (!value) {
                decimal = TypeUtils.parseBigDecimal(bytes, start - 1, len);
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

        if (valid) {
            return decimal;
        } else {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final UUID readUUID() {
        char ch = this.ch;
        if (ch == 'n') {
            readNull();
            return null;
        }

        if (ch != '"' && ch != '\'') {
            throw new JSONException(info("syntax error, can not read uuid"));
        }
        final char quote = ch;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 36 < bytes.length && bytes[offset + 36] == quote) {
            char ch1 = (char) bytes[offset + 8];
            char ch2 = (char) bytes[offset + 13];
            char ch3 = (char) bytes[offset + 18];
            char ch4 = (char) bytes[offset + 23];
            if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                long hi = 0;
                for (int i = 0; i < 8; i++) {
                    hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }
                for (int i = 9; i < 13; i++) {
                    hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }
                for (int i = 14; i < 18; i++) {
                    hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }

                long lo = 0;
                for (int i = 19; i < 23; i++) {
                    lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }
                for (int i = 24; i < 36; i++) {
                    lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
                }

                UUID uuid = new UUID(hi, lo);
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

                this.offset = offset;
                if (comma = (ch == ',')) {
                    next();
                } else {
                    this.ch = ch;
                }

                return uuid;
            }
        } else if (offset + 32 < bytes.length && bytes[offset + 32] == quote) {
            long hi = 0;
            for (int i = 0; i < 16; i++) {
                hi = (hi << 4) + UUID_VALUES[bytes[offset + i] - '0'];
            }
            long lo = 0;
            for (int i = 16; i < 32; i++) {
                lo = (lo << 4) + UUID_VALUES[bytes[offset + i] - '0'];
            }
            UUID uuid = new UUID(hi, lo);
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

            this.offset = offset;
            if (comma = (ch == ',')) {
                next();
            } else {
                this.ch = ch;
            }
            return uuid;
        }

        String str = readString();
        if (str.isEmpty()) {
            return null;
        }
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
        } else if ((first == '"' || first == '\'') && offset < end && bytes[offset] == first) {
            offset++;
        } else {
            return false;
        }

        int ch = offset == end ? EOI : (char) bytes[offset];

        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                this.offset = offset;
                return true;
            }
            ch = bytes[offset];
        }

        if (comma = (ch == ',')) {
            offset++;
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = bytes[offset];
            }
        }

        if (offset >= end) {
            this.ch = EOI;
            this.offset = offset;
            return true;
        }

        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                this.ch = EOI;
                return true;
            }
            ch = bytes[offset];
        }

        if (ch >= 0) {
            this.offset = offset + 1;
            this.ch = (char) ch;
            return true;
        }

        ch &= 0xFF;
        switch (ch >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                offset += 2;
                int char2 = bytes[offset - 1];
                if ((char2 & 0xC0) != 0x80) {
                    throw new JSONException(
                            "malformed input around byte " + offset);
                }
                ch = (char) (((ch & 0x1F) << 6) | (char2 & 0x3F));
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
                        (((ch & 0x0F) << 12) |
                                ((char2 & 0x3F) << 6) |
                                ((char3 & 0x3F)));
                break;
            }
            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new JSONException("malformed input around byte " + offset);
        }
        this.offset = offset;
        this.ch = (char) ch;
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
        int ch = this.ch;
        int offset = this.offset;
        final byte[] bytes = this.bytes;
        final int quote = ch;
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
                ch = bytes[offset++];
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

        byte[] hex = new byte[len / 2];
        for (int i = 0; i < hex.length; ++i) {
            byte c0 = this.bytes[start + i * 2];
            byte c1 = this.bytes[start + i * 2 + 1];

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            hex[i] = (byte) ((b0 << 4) | b1);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) (bytes[offset++] & 0xff);
            }
        }

        if (ch != ',' || offset >= end) {
            this.offset = offset;
            this.ch = (char) ch;
            return hex;
        }

        comma = true;
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            offset++;
            if (offset >= end) {
                this.offset = offset;
                this.ch = EOI;
                return hex;
            }
            ch = bytes[offset];
        }

        if (ch >= 0) {
            offset++;
        } else {
            ch &= 0xFF;
            switch (ch >> 4) {
                case 12:
                case 13: {
                    /* 110x xxxx   10xx xxxx*/
                    offset += 2;
                    int char2 = bytes[offset - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new JSONException(
                                "malformed input around byte " + offset);
                    }
                    ch = ((ch & 0x1F) << 6) | (char2 & 0x3F);
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
                    ch = (((ch & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    break;
                }
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new JSONException("malformed input around byte " + offset);
            }
        }

        this.offset = offset;
        this.ch = (char) ch;
        while (this.ch == '/' && offset < bytes.length && bytes[offset] == '/') {
            skipLineComment();
        }

        return hex;
    }

    @Override
    public boolean isReference() {
        // should be codeSize <= FreqInlineSize 325
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        int offset = this.offset;

        if (ch != '{') {
            return false;
        }

        if (offset == end) {
            return false;
        }

        ch = bytes[offset];
        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = bytes[offset];
        }

        int quote = ch;
        if ((quote != '"' && quote != '\'')
                || offset + 6 >= end
                || (bytes[offset + 1] != '$'
                || bytes[offset + 2] != 'r'
                || bytes[offset + 3] != 'e'
                || bytes[offset + 4] != 'f'
                || bytes[offset + 5] != quote)
        ) {
            return false;
        }

        offset += 6;
        ch = bytes[offset];
        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = bytes[offset];
        }

        if (ch != ':' || offset + 1 >= end) {
            return false;
        }

        ch = bytes[++offset];
        while (ch >= 0 && ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = bytes[offset];
        }

        if (ch != quote) {
            return false;
        }

        this.referenceBegin = offset;
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

    public final boolean readBoolValue() {
        wasNull = false;
        boolean val;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        char ch = this.ch;
        if (ch == 't'
                && offset + 2 < bytes.length
                && bytes[offset] == 'r'
                && bytes[offset + 1] == 'u'
                && bytes[offset + 2] == 'e'
        ) {
            offset += 3;
            val = true;
        } else if (ch == 'f'
                && offset + 4 <= bytes.length
                && UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) == ALSE
        ) {
            offset += 4;
            val = false;
        } else if (ch == '-' || (ch >= '0' && ch <= '9')) {
            readNumber();
            if (valueType == JSON_TYPE_INT) {
                if ((context.features & Feature.NonZeroNumberCastToBooleanAsTrue.mask) != 0) {
                    return mag0 != 0 || mag1 != 0 || mag2 != 0 || mag3 != 0;
                } else {
                    return mag0 == 0
                            && mag1 == 0
                            && mag2 == 0
                            && mag3 == 1;
                }
            }
            return false;
        } else if (ch == 'n' && offset + 2 < bytes.length
                && bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l'
        ) {
            if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(info("boolean value not support input null"));
            }

            wasNull = true;
            offset += 3;
            val = false;
        } else if (ch == '"') {
            if (offset + 1 < bytes.length
                    && bytes[offset + 1] == '"'
            ) {
                byte c0 = bytes[offset];
                offset += 2;
                if (c0 == '0' || c0 == 'N') {
                    val = false;
                } else if (c0 == '1' || c0 == 'Y') {
                    val = true;
                } else {
                    throw new JSONException("can not convert to boolean : " + c0);
                }
            } else {
                String str = readString();
                if ("true".equalsIgnoreCase(str)) {
                    return true;
                }

                if ("false".equalsIgnoreCase(str)) {
                    return false;
                }

                if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                    wasNull = true;
                    return false;
                }
                throw new JSONException("can not convert to boolean : " + str);
            }
        } else {
            throw new JSONException("syntax error : " + ch);
        }

        ch = offset == end ? EOI : (char) bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            if (offset >= end) {
                ch = EOI;
            } else {
                ch = (char) bytes[offset++];
            }
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : (char) bytes[offset++];
            // next inline
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                if (offset >= end) {
                    ch = EOI;
                } else {
                    ch = (char) bytes[offset++];
                }
            }
        }
        this.offset = offset;
        this.ch = ch;

        return val;
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

        String str = new String(bytes, this.start, Math.min(length, 65535));
        buf.append(str);
        return buf.toString();
    }

    @Override
    public final void close() {
        if (cacheItem != null && bytes.length < CACHE_THRESHOLD) {
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

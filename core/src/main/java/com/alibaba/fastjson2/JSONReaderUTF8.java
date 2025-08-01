package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReaderJSONB.check3;
import static com.alibaba.fastjson2.util.DateUtils.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

class JSONReaderUTF8
        extends JSONReader {
    static final int REF = BIG_ENDIAN ? 0x24726566 : 0x66657224;
    static final int ESCAPE_INDEX_NOT_SET = -2;
    protected int nextEscapeIndex = ESCAPE_INDEX_NOT_SET;

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

    protected CacheItem cacheItem;
    protected char[] charBuf;

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

        if (this.ch == '/') {
            skipComment();
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

        if (this.ch == '/') {
            skipComment();
        }
    }

    JSONReaderUTF8(Context ctx, byte[] bytes, int offset, int length) {
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

    static int indexOfSlash(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        int slashIndex = jsonReader.nextEscapeIndex;
        if (slashIndex == ESCAPE_INDEX_NOT_SET || (slashIndex != -1 && slashIndex < offset)) {
            jsonReader.nextEscapeIndex = slashIndex = IOUtils.indexOfSlash(bytes, offset, end);
        }
        return slashIndex;
    }

    private void char_utf8(int ch, int offset) {
        final byte[] bytes = this.bytes;
        switch ((ch & 0xFF) >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                ch = char2_utf8(ch & 0xFF, bytes[offset++], offset);
                break;
            }
            case 14: {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                ch = char2_utf8(ch & 0xFF, bytes[offset], bytes[offset + 1], offset);
                offset += 2;
                break;
            }
            default: {
                if ((ch >> 3) == -2) {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    int c4 = bytes[offset + 2];
                    ch = ((ch << 18) ^
                            (c2 << 12) ^
                            (c3 << 6) ^
                            (c4 ^ (((byte) 0xF0 << 18) ^
                                    ((byte) 0x80 << 12) ^
                                    ((byte) 0x80 << 6) ^
                                    ((byte) 0x80 << 0))));
                    offset += 3;
                    break;
                }
                /* 10xx xxxx,  1111 xxxx */
                throw new JSONException("malformed input around byte " + offset);
            }
        }
        this.ch = (char) ch;
        this.offset = offset;
    }

    static int char2_utf8(int ch, int char2, int offset) {
        if ((char2 & 0xC0) != 0x80) {
            throw new JSONException("malformed input around byte " + offset);
        }
        return ((ch & 0x1F) << 6) | (char2 & 0x3F);
    }

    static int char2_utf8(int ch, int char2, int char3, int offset) {
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
            throw new JSONException("malformed input around byte " + offset);
        }
        return (((ch & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
    }

    static void char2_utf8(byte[] bytes, int offset, int c, char[] chars, int charPos) {
        if ((c >> 3) == -2) {
            int c2 = bytes[offset + 1];
            int c3 = bytes[offset + 2];
            int c4 = bytes[offset + 3];
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
                chars[charPos] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                chars[charPos + 1] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
            }
            return;
        }

        throw new JSONException("malformed input around byte " + offset);
    }

    @Override
    public final boolean nextIfMatch(char e) {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch != e) {
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.offset = offset;
        this.ch = (char) ch;
        if (ch == '/') {
            skipComment();
        }

        return true;
    }

    @Override
    public final boolean nextIfComma() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch != ',') {
            this.offset = offset;
            this.ch = (char) ch;
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.offset = offset;
        this.ch = (char) ch;
        if (ch == '/') {
            skipComment();
        }

        return true;
    }

    @Override
    public final boolean nextIfArrayStart() {
        int ch = this.ch;
        if (ch != '[') {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.ch = (char) ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfArrayEnd() {
        int ch = this.ch;
        byte[] bytes = this.bytes;
        int offset = this.offset;
        if (ch != ']') {
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.ch = (char) ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfSet() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        if (ch == 'S'
                && offset + 1 < end
                && bytes[offset] == 'e'
                && bytes[offset + 1] == 't') {
            offset += 2;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
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
        int ch = this.ch;
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
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
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

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.ch = (char) ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfObjectEnd() {
        int ch = this.ch;
        byte[] bytes = this.bytes;
        int offset = this.offset;

        if (ch != '}') {
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.ch = (char) ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public void next() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = offset >= end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return;
        }

        this.offset = offset;
        this.ch = (char) ch;
        if (ch == '/') {
            skipComment();
        }
    }

    public final void nextWithoutComment() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = offset >= end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return;
        }

        this.offset = offset;
        this.ch = (char) ch;
    }

    @Override
    public long readFieldNameHashCodeUnquote() {
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

            if (ch < 0) {
                switch ((ch & 0xFF) >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        ch = char2_utf8(ch & 0xFF, bytes[offset++], offset);
                        break;
                    }
                    case 14: {
                        ch = char2_utf8(ch & 0xFF, bytes[offset], bytes[offset + 1], offset);
                        offset += 2;
                        break;
                    }
                    default:
                        if ((ch >> 3) == -2) {
                            int c2 = bytes[offset];
                            int c3 = bytes[offset + 1];
                            int c4 = bytes[offset + 2];
                            ch = ((ch << 18) ^
                                    (c2 << 12) ^
                                    (c3 << 6) ^
                                    (c4 ^ (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80 << 0))));
                            break;
                        }
                        /* 10xx xxxx,  1111 xxxx */
                        throw new JSONException("malformed input around byte " + offset);
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

            ch = offset == end ? EOI : bytes[offset++];
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
                    ch = offset == end ? EOI : bytes[offset++];
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
                            ch = offset == end ? EOI : bytes[offset++];
                        }
                        break _for;
                    default:
                        break;
                }

                if (ch < 0) {
                    switch ((ch & 0xFF) >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            ch = char2_utf8(ch & 0xFF, bytes[offset++], offset);
                            nameAscii = false;
                            break;
                        }
                        case 14: {
                            ch = char2_utf8(ch & 0xFF, bytes[offset], bytes[offset + 1], offset);
                            offset += 2;
                            nameAscii = false;
                            break;
                        }
                        default:
                            if ((ch >> 3) == -2) {
                                int c2 = bytes[offset];
                                int c3 = bytes[offset + 1];
                                int c4 = bytes[offset + 2];
                                ch = ((ch << 18) ^
                                        (c2 << 12) ^
                                        (c3 << 6) ^
                                        (c4 ^ (((byte) 0xF0 << 18) ^
                                                ((byte) 0x80 << 12) ^
                                                ((byte) 0x80 << 6) ^
                                                ((byte) 0x80 << 0))));
                                offset += 3;
                                nameAscii = false;
                                break;
                            }
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }

                if (ch > 0xFFFF) {
                    int c0 = (ch >>> 10) + ('\uD800' - (0x010000 >>> 10)); // Character.highSurrogate(uc);
                    hashCode ^= c0;
                    hashCode *= Fnv.MAGIC_PRIME;

                    int c1 = (ch & 0x3ff) + '\uDC00'; // Character.lowSurrogate(uc);
                    hashCode ^= c1;
                } else {
                    hashCode ^= ch;
                }
                hashCode *= Fnv.MAGIC_PRIME;

                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (ch == ':') {
            ch = offset == end ? EOI : (char) bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.offset = offset;
        this.ch = (char) ch;

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

        if (offset >= end || bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
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

        if (offset >= end || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
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

        int c;
        while ((c = bytes[offset] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
        }

        this.offset = offset + 1;
        this.ch = (char) c;
        return true;
    }

    @Override
    public final boolean nextIfName4Match2() {
        byte[] bytes = this.bytes;
        int offset = this.offset + 4;
        if (offset >= end || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match3() {
        byte[] bytes = this.bytes;
        int offset = this.offset + 5;
        if (offset >= end || bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
        }

        this.offset = offset + 1;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match4(byte c4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 6;
        if (offset >= end || bytes[offset - 3] != c4 || bytes[offset - 2] != '"' || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match5(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 7;
        if (offset >= end || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name1) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match6(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 8;
        if (offset >= end
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name1
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match7(int name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 9;
        if (offset >= end
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name1
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match8(int name1, byte c8) {
        int offset = this.offset + 10;
        byte[] bytes = this.bytes;
        if (offset >= end
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name1
                || bytes[offset - 3] != c8
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match9(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 11;
        if (offset >= end || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name1) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match10(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 12;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name1
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match11(long name1) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 13;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name1
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match12(long name1, byte name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 14;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name1
                || bytes[offset - 3] != name2
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match13(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 15;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name2
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match14(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 16;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name2
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match15(long name1, int name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 17;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name2
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match16(long name1, int name2, byte name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 18;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name1
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name2
                || bytes[offset - 3] != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match17(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 19;
        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name2
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match18(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 20;
        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name2
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match19(long name1, long name2) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 21;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match20(long name1, long name2, byte name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 22;
        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 19) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name2
                || bytes[offset - 3] != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match21(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 23;
        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 20) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name3
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match22(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 24;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match23(long name1, long name2, int name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 25;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match24(long name1, long name2, int name3, byte name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 26;
        if (offset >= end) {
            return false;
        }

        if (UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 23) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 15) != name2
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 7) != name3
                || bytes[offset - 3] != name4
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match25(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 27;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 24) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name3) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match26(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 28;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 25) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name3
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match27(long name1, long name2, long name3) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 29;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 26) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 18) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 10) != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match28(long name1, long name2, long name3, byte c29) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 30;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 27) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 19) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 11) != name3
                || bytes[offset - 3] != c29
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match29(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 31;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 28) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 20) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 12) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 4) != name4) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match30(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 32;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 29) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 21) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 13) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 5) != name4
                || bytes[offset - 1] != ':') {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match31(long name1, long name2, long name3, int name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 33;
        if (offset >= end
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 30) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 22) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 14) != name3
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 6) != name4
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match32(long name1, long name2, long name3, int name4, byte c32) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 34;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match33(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 35;
        if (offset >= end
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 32) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 24) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 16) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 8) != name4) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match34(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 36;
        if (offset >= end
                || bytes[offset - 1] != ':'
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 33) != name1
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 25) != name2
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 17) != name3
                || UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset - 9) != name4
        ) {
            return false;
        }

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match35(long name1, long name2, long name3, long name4) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 37;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match36(long name1, long name2, long name3, long name4, byte c36) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 38;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match37(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 39;
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
        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match38(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 40;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match39(long name1, long name2, long name3, long name4, int name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 41;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match40(long name1, long name2, long name3, long name4, int name5, byte c40) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 42;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match41(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 43;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match42(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 44;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match43(long name1, long name2, long name3, long name4, long name5) {
        byte[] bytes = this.bytes;
        int offset = this.offset + 45;
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

        int c;
        while ((c = bytes[offset++] & 0xff) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty  loop
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match2() {
        byte[] bytes = this.bytes;
        int offset = this.offset + 3;
        if (offset >= end) {
            return false;
        }

        int c = bytes[offset++] & 0xff;
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            c = offset == end ? EOI : (bytes[offset++] & 0xff);
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            c = bytes[offset++] & 0xff;
        }

        this.offset = offset;
        this.ch = (char) c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match3() {
        byte[] bytes = this.bytes;
        int offset = this.offset + 4;
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
        int offset = this.offset + 5;
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
        int offset = this.offset + 6;
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
        int offset = this.offset + 7;
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
        int offset = this.offset + 8;
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
        int offset = this.offset + 9;
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
        int offset = this.offset + 10;
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
        int offset = this.offset + 11;
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
        int offset = this.offset + 12;
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
        int ch = this.ch;
        if (ch == '/') {
            skipComment();
            ch = this.ch;
        }
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
                errorMsg = "illegal fieldName input" + ch;
            }

            throw new JSONException(info(errorMsg));
        }

        final int quote = ch;

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;

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
                ch = bytes[offset];

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

                if (ch >= 0) {
                    offset++;
                } else {
                    ch &= 0xFF;
                    switch (ch >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            ch = char2_utf8(ch, bytes[offset + 1], offset);
                            offset += 2;
                            nameAscii = false;
                            break;
                        }
                        case 14: {
                            ch = char2_utf8(ch, bytes[offset + 1], bytes[offset + 2], offset);
                            offset += 3;
                            nameAscii = false;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }

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
        this.ch = (char) ch;

        return hashCode;
    }

    @Override
    public long readValueHashCode() {
        int ch = this.ch;
        if (ch != '"' && ch != '\'') {
            return -1;
        }

        final byte[] bytes = this.bytes;
        final int quote = ch;

        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;

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

                if (ch >= 0) {
                    offset++;
                } else {
                    switch ((ch & 0xFF) >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            ch = char2_utf8(ch, bytes[offset + 1], offset);
                            offset += 2;
                            nameAscii = false;
                            break;
                        }
                        case 14: {
                            ch = char2_utf8(ch, bytes[offset + 1], bytes[offset + 2], offset);
                            offset += 3;
                            nameAscii = false;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            if ((ch >> 3) == -2) {
                                offset++;
                                int c2 = bytes[offset++];
                                int c3 = bytes[offset++];
                                int c4 = bytes[offset++];
                                int uc = ((ch << 18) ^
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
        this.ch = (char) ch;

        return hashCode;
    }

    @Override
    public long getNameHashCodeLCase() {
        long hashCode = Fnv.MAGIC_HASH_CODE;
        int offset = nameBegin, end = this.end;
        byte quote = '"';
        final byte[] bytes = this.bytes;
        if (offset > 0 && bytes[offset - 1] == '\'') {
            quote = '\'';
        }
        long nameValue = 0;
        for (int i = 0; offset < end; offset++) {
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
                    default:
                        ch = char1(ch);
                        break;
                }
            } else if (ch == -61 || ch == -62) {
                ch = ((ch & 0x1F) << 6) | (bytes[++offset] & 0x3F);
            } else if (ch == quote) {
                break;
            }

            if (i >= 8 || ch > 0xFF || ch < 0 || (i == 0 && ch == 0)) {
                nameValue = 0;
                offset = this.nameBegin;
                break;
            }

            if (ch == '_' || ch == '-' || ch == ' ') {
                int c1 = bytes[offset + 1];
                if (c1 != '"' && c1 != '\'' && c1 != ch) {
                    continue;
                }
            }

            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
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
            ++i;
        }

        if (nameValue != 0) {
            return nameValue;
        }

        if (nameAscii && !nameEscape) {
            for (int i = nameBegin; i < nameEnd; ++i) {
                int ch = bytes[i];
                if (ch >= 'A' && ch <= 'Z') {
                    ch = ch + 32;
                }

                if (ch == '_' || ch == '-' || ch == ' ') {
                    byte c1 = bytes[i + 1];
                    if (c1 != '"' && c1 != '\'' && c1 != ch) {
                        continue;
                    }
                }

                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;
            }
            return hashCode;
        }

        for (; ; ) {
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
                    default:
                        ch = char1(ch);
                        break;
                }
                offset++;
            } else if (ch == quote) {
                break;
            } else {
                if (ch >= 0) {
                    if (ch >= 'A' && ch <= 'Z') {
                        ch = ch + 32;
                    }
                    offset++;
                } else {
                    ch &= 0xFF;
                    switch (ch >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            int c2 = bytes[offset + 1];
                            ch = ((ch & 0x1F) << 6) | (c2 & 0x3F);
                            offset += 2;
                            break;
                        }
                        case 14: {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            ch = ((ch & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F);

                            offset += 3;
                            break;
                        }
                        default:
                            /* 10xx xxxx,  1111 xxxx */
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }
            }

            if (ch == '_' || ch == '-' || ch == ' ') {
                continue;
            }

            hashCode ^= ch;
            hashCode *= Fnv.MAGIC_PRIME;
        }

        return hashCode;
    }

    final String getLatin1String(int offset, int length) {
        if (JDKUtils.ANDROID_SDK_INT >= 34) {
            return new String(bytes, offset, length, ISO_8859_1);
        }

        char[] charBuf = this.charBuf;
        if (charBuf == null) {
            if (cacheItem == null) {
                int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
                cacheItem = CACHE_ITEMS[cacheIndex];
            }

            this.charBuf = charBuf = CHARS_UPDATER.getAndSet(cacheItem, null);
        }
        if (charBuf == null || charBuf.length < length) {
            this.charBuf = charBuf = new char[length];
        }
        for (int i = 0; i < length; i++) {
            charBuf[i] = (char) (bytes[offset + i] & 0xFF);
        }
        return new String(charBuf, 0, length);
    }

    @Override
    public String getFieldName() {
        final byte[] bytes = this.bytes;
        int offset = nameBegin;
        int length = nameEnd - offset;
        if (!nameEscape) {
            if (nameAscii) {
                if (STRING_CREATOR_JDK8 != null) {
                    return JDKUtils.asciiStringJDK8(bytes, offset, length);
                } else if (STRING_CREATOR_JDK11 != null) {
                    return STRING_CREATOR_JDK11.apply(
                            Arrays.copyOfRange(bytes, offset, nameEnd),
                            LATIN1);
                } else if (ANDROID) {
                    return getLatin1String(offset, length);
                }
            }

            return new String(bytes, offset, length, nameAscii ? ISO_8859_1 : UTF_8);
        }

        char[] chars = new char[nameLength];

        for (int i = 0, end = this.end; offset < nameEnd; ++i) {
            int ch = bytes[offset];
            if (ch < 0) {
                ch &= 0xFF;
                switch (ch >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        ch = char2_utf8(ch, bytes[offset + 1], offset);
                        offset += 2;
                        break;
                    }
                    case 14: {
                        ch = char2_utf8(ch, bytes[offset + 1], bytes[offset + 2], offset);
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new JSONException("malformed input around byte " + offset);
                }
                chars[i] = (char) ch;
                continue;
            }

            if (ch == '\\') {
                ch = (char) bytes[++offset];
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
            } else if (ch == '"') {
                break;
            }
            chars[i] = (char) ch;
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
        this.nameAscii = true;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;
        final int nameBegin = this.nameBegin;
        for (int i = 0; offset < end; ++i) {
            int ch = bytes[offset];
            if (ch == '\\') {
                nameEscape = true;
                ch = bytes[offset + 1];
                offset += (ch == 'u' ? 6 : (ch == 'x' ? 4 : 2));
                continue;
            }

            if (ch == quote) {
                this.nameLength = i;
                this.nameEnd = offset;
                offset++;
                if (offset < end) {
                    ch = bytes[offset] & 0xff;
                } else {
                    ch = EOI;
                }

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    offset++;
                    ch = bytes[offset] & 0xff;
                }
                if (ch != ':') {
                    throw syntaxError(offset, ch);
                }

                ch = ++offset == end ? EOI : bytes[offset++];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }

                this.offset = offset;
                this.ch = (char) ch;
                break;
            }

            if (ch >= 0) {
                offset++;
            } else {
                if (nameAscii) {
                    nameAscii = false;
                }
                switch ((ch & 0xFF) >> 4) {
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
                        if ((ch >> 3) == -2) {
                            offset += 4;
                            break;
                        }
                        /* 10xx xxxx,  1111 xxxx */
                        throw syntaxError(offset, ch);
                }
            }
        }

        if (nameEnd < nameBegin) {
            throw syntaxError(offset, ch);
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
                                + (((long) bytes[nameBegin + 7]) << 8)
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
                                name = asciiStringJDK8(bytes, nameBegin, length);
                            } else if (ANDROID) {
                                name = getLatin1String(nameBegin, length);
                            } else {
                                name = new String(bytes, nameBegin, length, ISO_8859_1);
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
                                name = asciiStringJDK8(bytes, nameBegin, length);
                            } else if (ANDROID) {
                                name = getLatin1String(nameBegin, length);
                            } else {
                                name = new String(bytes, nameBegin, length, ISO_8859_1);
                            }

                            NAME_CACHE[indexMask] = new NameCacheEntry(name, nameValue0);
                            return name;
                        } else if (entry.value == nameValue0) {
                            return entry.name;
                        }
                    }
                }

                if (STRING_CREATOR_JDK8 != null) {
                    return asciiStringJDK8(bytes, nameBegin, length);
                } else if (ANDROID) {
                    return getLatin1String(nameBegin, nameEnd - nameBegin);
                } else if (STRING_CREATOR_JDK11 != null) {
                    return STRING_CREATOR_JDK11.apply(
                            Arrays.copyOfRange(bytes, nameBegin, nameEnd),
                            LATIN1);
                }
            }

            if (nameAscii && ANDROID) {
                return getLatin1String(nameBegin, length);
            }

            return new String(bytes, nameBegin, length,
                    nameAscii ? ISO_8859_1 : UTF_8
            );
        }

        return getFieldName();
    }

    @Override
    public final int readInt32Value() {
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        final byte[] bytes = this.bytes;

        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = bytes[offset++];
        }
        int fc = ch;
        int result = ch >= '0' && ch <= '9'
                ? '0' - ch
                : ch == '-' || ch == '+'
                ? 0
                : 1;  // or any value > 0
        int d;
        while (offset + 1 < end
                && (d = IOUtils.digit2(bytes, offset)) != -1
                && Integer.MIN_VALUE / 100 <= result & result <= 0) {
            result = result * 100 - d;  // overflow from d => result > 0
            offset += 2;
        }
        if (offset < end && IOUtils.isDigit(ch = bytes[offset]) && Integer.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset++;
        }

        ch = offset == end ? EOI : bytes[offset++];
        if (result <= 0 && (Integer.MIN_VALUE < result || fc == '-')
                && INT_VALUE_END[ch & 0xff]
                && (quote == 0 || ch == quote)
        ) {
            if (quote != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                ch = offset == end ? EOI : bytes[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = (ch == ',')) {
                ch = offset == end ? EOI : (char) bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;

            return fc == '-' ? result : -result;
        } else {
            return readInt32ValueOverflow();
        }
    }

    @Override
    public final Integer readInt32() {
        int ch = this.ch;
        if ((ch == '"' || ch == '\'' || ch == 'n') && nextIfNullOrEmptyString()) {
            return null;
        }
        return readInt32Value();
    }

    @Override
    public final long readInt64Value() {
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        final byte[] bytes = this.bytes;

        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = bytes[offset++];
        }
        int fc = ch;
        long result = ch >= '0' && ch <= '9'
                ? '0' - ch
                : ch == '-' || ch == '+'
                ? 0
                : 1;  // or any value > 0
        int d;
        while (offset + 1 < end
                && (d = IOUtils.digit2(bytes, offset)) != -1
                && Long.MIN_VALUE / 100 <= result & result <= 0) {
            result = result * 100 - d;  // overflow from d => result > 0
            offset += 2;
        }
        if (offset < end && IOUtils.isDigit(ch = bytes[offset]) && Long.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset++;
        }

        ch = offset == end ? EOI : bytes[offset++];
        if (result <= 0 && (Long.MIN_VALUE < result || fc == '-')
                && INT_VALUE_END[ch & 0xff]
                && (quote == 0 || ch == quote)
        ) {
            if (quote != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                ch = offset == end ? EOI : bytes[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (comma = (ch == ',')) {
                ch = offset == end ? EOI : (char) bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;

            return fc == '-' ? result : -result;
        } else {
            return readInt64ValueOverflow();
        }
    }

    @Override
    public final Long readInt64() {
        int ch = this.ch;
        if ((ch == '"' || ch == '\'' || ch == 'n') && nextIfNullOrEmptyString()) {
            return null;
        }
        return readInt64Value();
    }

    @Override
    public final double readDoubleValue() {
        double doubleValue = 0;

        final byte[] bytes = this.bytes;
        int quote = '\0';
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = offset == end ? EOI : bytes[offset++];
        }

        long result;
        boolean wasNull = false;
        if (quote != 0 && ch == quote) {
            ch = offset == end ? EOI : bytes[offset++];
            wasNull = true;
            result = 0;
        } else {
            int fc = ch;
            result = ch >= '0' && ch <= '9'
                    ? '0' - ch
                    : ch == '-' || ch == '+'
                    ? 0
                    : 1;  // or any value > 0

            int d;
            while (result <= 0
                    && offset + 1 < end
                    && (d = IOUtils.digit2(bytes, offset)) != -1) {
                if (Long.MIN_VALUE / 100 <= result) {
                    result = result * 100 - d;  // overflow from d => result > 0
                    offset += 2;
                } else {
                    result = 1; // overflow
                }
            }
            if (result <= 0 && offset < end && IOUtils.isDigit(ch = bytes[offset])) {
                if (Long.MIN_VALUE / 10 <= result) {
                    result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                    offset++;
                } else {
                    result = 1; // overflow
                }
            }

            int scale = 0;
            if (result <= 0
                    && offset < end
                    && bytes[offset] == '.'
            ) {
                offset++;
                while (result <= 0
                        && offset + 1 < end
                        && (d = IOUtils.digit2(bytes, offset)) != -1) {
                    if (Long.MIN_VALUE / 100 <= result) {
                        result = result * 100 - d;  // overflow from d => result > 0
                        offset += 2;
                        scale += 2;
                    } else {
                        result = 1; // overflow
                    }
                }
                if (result <= 0 && offset < end && IOUtils.isDigit(ch = bytes[offset])) {
                    if (Long.MIN_VALUE / 10 <= result) {
                        result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                        offset++;
                        scale++;
                    } else {
                        result = 1; // overflow
                    }
                }
            }
            if (result <= 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            int expValue;
            if (result <= 0) {
                if (ch == 'e' || ch == 'E') {
                    boolean negativeExp;
                    ch = offset == end ? EOI : bytes[offset++];
                    if ((negativeExp = (ch == '-')) || ch == '+') {
                        ch = offset == end ? EOI : bytes[offset++];
                    } else if (ch == ',') {
                        throw numberError();
                    }
                    if (IOUtils.isDigit(ch)) {
                        expValue = ch - '0';
                        while (offset < end
                                && IOUtils.isDigit((ch = bytes[offset]))
                        ) {
                            d = ch - '0';
                            expValue = expValue * 10 + d;
                            if (expValue > MAX_EXP) {
                                throw new JSONException("too large exp value : " + expValue);
                            }
                            offset++;
                        }
                        if (negativeExp) {
                            expValue = -expValue;
                        }
                        scale -= expValue;
                        ch = offset == end ? EOI : bytes[offset++];
                    } else {
                        result = 1; // invalid
                    }
                } else if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            if (result <= 0 && quote != 0) {
                if (ch == quote) {
                    ch = offset == end ? EOI : bytes[offset++];
                } else {
                    result = 1; // invalid
                }
            }

            if (result <= 0) {
                boolean value = true;
                if (scale == 0) {
                    doubleValue = (double) result;
                } else if ((long) (double) result == result) {
                    if (0 < scale && scale < DOUBLE_10_POW.length) {
                        doubleValue = (double) result / DOUBLE_10_POW[scale];
                    } else if (0 > scale && scale > -DOUBLE_10_POW.length) {
                        doubleValue = (double) result * DOUBLE_10_POW[-scale];
                    } else {
                        value = false;
                    }
                } else {
                    value = false;
                }
                if (!value) {
                    if (scale > 0 && scale < 64) {
                        doubleValue = TypeUtils.doubleValue(fc == '-' ? -1 : 1, Math.abs(result), scale);
                    } else {
                        result = 1; // invalid
                    }
                } else {
                    if (fc != '-') {
                        if (doubleValue != 0) {
                            doubleValue = -doubleValue;
                        }
                    } else if (result == 0) {
                        doubleValue = -doubleValue;
                    }
                }
            }
        }

        if (result > 0) {
            readNumber0();
            return getDoubleValue();
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.wasNull = wasNull;
        this.ch = (char) ch;
        this.offset = offset;
        return doubleValue;
    }

    @Override
    public final float readFloatValue() {
        float floatValue = 0;

        final byte[] bytes = this.bytes;
        int quote = '\0';
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = offset == end ? EOI : bytes[offset++];
        }

        long result;
        boolean wasNull = false;
        if (quote != 0 && ch == quote) {
            ch = offset == end ? EOI : bytes[offset++];
            wasNull = true;
            result = 0;
        } else {
            int fc = ch;
            result = ch >= '0' && ch <= '9'
                    ? '0' - ch
                    : ch == '-' || ch == '+'
                    ? 0
                    : 1;  // or any value > 0

            int d;
            while (result <= 0
                    && offset + 1 < end
                    && (d = IOUtils.digit2(bytes, offset)) != -1) {
                if (Long.MIN_VALUE / 100 <= result) {
                    result = result * 100 - d;  // overflow from d => result > 0
                    offset += 2;
                } else {
                    result = 1; // overflow
                }
            }
            if (result <= 0 && offset < end && IOUtils.isDigit(ch = bytes[offset])) {
                if (Long.MIN_VALUE / 10 <= result) {
                    result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                    offset++;
                } else {
                    result = 1; // overflow
                }
            }

            int scale = 0;
            if (result <= 0
                    && offset < end
                    && bytes[offset] == '.'
            ) {
                offset++;
                while (result <= 0
                        && offset + 1 < end
                        && (d = IOUtils.digit2(bytes, offset)) != -1) {
                    if (Long.MIN_VALUE / 100 <= result) {
                        result = result * 100 - d;  // overflow from d => result > 0
                        offset += 2;
                        scale += 2;
                    } else {
                        result = 1; // overflow
                    }
                }
                if (result <= 0 && offset < end && IOUtils.isDigit(ch = bytes[offset])) {
                    if (Long.MIN_VALUE / 10 <= result) {
                        result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                        offset++;
                        scale++;
                    } else {
                        result = 1; // overflow
                    }
                }
            }
            if (result <= 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            int expValue;
            if (result <= 0) {
                if (ch == 'e' || ch == 'E') {
                    boolean negativeExp;
                    ch = offset == end ? EOI : bytes[offset++];
                    if ((negativeExp = (ch == '-')) || ch == '+') {
                        ch = offset == end ? EOI : bytes[offset++];
                    } else if (ch == ',') {
                        throw numberError();
                    }
                    if (IOUtils.isDigit(ch)) {
                        expValue = ch - '0';
                        while (offset < end
                                && IOUtils.isDigit((ch = bytes[offset]))
                        ) {
                            d = ch - '0';
                            expValue = expValue * 10 + d;
                            if (expValue > MAX_EXP) {
                                throw new JSONException("too large exp value : " + expValue);
                            }
                            offset++;
                        }
                        if (negativeExp) {
                            expValue = -expValue;
                        }
                        scale -= expValue;
                        ch = offset == end ? EOI : bytes[offset++];
                    } else {
                        result = 1; // invalid
                    }
                } else if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            if (result <= 0 && quote != 0) {
                if (ch == quote) {
                    ch = offset == end ? EOI : bytes[offset++];
                } else {
                    result = 1; // invalid
                }
            }

            if (result <= 0) {
                boolean value = true;
                if (scale == 0) {
                    floatValue = (float) result;
                } else if ((long) (float) result == result) {
                    if (0 < scale && scale < FLOAT_10_POW.length) {
                        floatValue = (float) result / FLOAT_10_POW[scale];
                    } else if (0 > scale && scale > -FLOAT_10_POW.length) {
                        floatValue = (float) result * FLOAT_10_POW[-scale];
                    } else {
                        value = false;
                    }
                } else {
                    value = false;
                }
                if (!value) {
                    if (scale > 0 && scale < 128) {
                        floatValue = TypeUtils.floatValue(fc == '-' ? -1 : 1, Math.abs(result), scale);
                    } else {
                        result = 1; // invalid
                    }
                } else {
                    if (fc != '-') {
                        if (floatValue != 0) {
                            floatValue = -floatValue;
                        }
                    } else if (result == 0) {
                        floatValue = -floatValue;
                    }
                }
            }
        }

        if (result > 0) {
            readNumber0();
            return getFloatValue();
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.wasNull = wasNull;
        this.ch = (char) ch;
        this.offset = offset;
        return floatValue;
    }

    @Override
    public final void readString(ValueConsumer consumer, boolean quoted) {
        char quote = this.ch;
        int valueLength;
        int offset = this.offset;
        int start = offset;
        valueEscape = false;
        final byte[] bytes = this.bytes;

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
            for (int i = 0, end = this.end; ; ++i) {
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
                        chars[i] = (char) (
                                ((c & 0x1F) << 6) | (bytes[offset + 1] & 0x3F));
                        offset += 2;
                        break;
                    }
                    case 14: {
                        chars[i] = (char)
                                (((c & 0x0F) << 12) |
                                        ((bytes[offset + 1] & 0x3F) << 6) |
                                        ((bytes[offset + 2] & 0x3F)));
                        offset += 3;
                        break;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        if ((c >> 3) == -2) {
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            int c4 = bytes[offset + 3];
                            offset += 4;
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
                byte[] buf = jsonWriter.getBytes();
                consumer.accept(buf, 0, buf.length);
            } else {
                byte[] buf = new byte[bytesMaxiumLength];
                int bytesLength = IOUtils.encodeUTF8(chars, 0, chars.length, buf, 0);
                consumer.accept(buf, 0, bytesLength);
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
        int start = offset, end = this.end;
        boolean ascii = true;
        valueEscape = false;
        final byte[] bytes = this.bytes;

        for (int i = 0; ; ++i) {
            int c = bytes[offset];
            if (c == '\\') {
                valueEscape = true;
                c = bytes[offset + 1];
                offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
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
            int strlen = offset - this.offset;
            if (ANDROID) {
                str = getLatin1String(this.offset, strlen);
            } else {
                str = new String(bytes, this.offset, strlen, ISO_8859_1);
            }
        } else {
            str = new String(bytes, this.offset, offset - this.offset, UTF_8);
        }

        int ch = bytes[++offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = bytes[++offset];
        }

        this.comma = ch == ',';
        this.offset = offset + 1;
        if (ch == ',') {
            next();
        } else {
            this.ch = (char) ch;
        }

        stringValue = str;
    }

    @Override
    public final boolean skipName() {
        this.offset = skipName(this, bytes, offset, end);
        return true;
    }

    private static int skipName(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        int quote = jsonReader.ch;
        if (jsonReader.checkNameBegin(quote)) {
            return jsonReader.offset;
        }

        int index = IOUtils.indexOfQuote(bytes, quote, offset, end);
        if (index == -1) {
            throw jsonReader.error("invalid escape character EOI");
        }

        int ch;
        int slashIndex = indexOfSlash(jsonReader, bytes, offset, end);
        if (slashIndex == -1 || slashIndex > index) {
            offset = index + 1;
            ch = offset == end ? EOI : bytes[offset++];
        } else {
            offset = skipStringEscaped(jsonReader, bytes, slashIndex, quote);
            ch = offset == end ? EOI : bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (ch != ':') {
            throw syntaxError(ch);
        }

        ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipNumber(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        int ch = jsonReader.ch;
        if (ch == '-' || ch == '+') {
            if (offset < end) {
                ch = bytes[offset++];
            } else {
                throw jsonReader.error();
            }
        }
        boolean dot = ch == '.';
        boolean num = false;
        if (!dot && (ch >= '0' && ch <= '9')) {
            num = true;
            while (IOUtils.isDigit2(bytes, offset)) {
                offset += 2;
            }
            do {
                ch = offset == end ? EOI : bytes[offset++];
            } while (ch >= '0' && ch <= '9');
        }

        if (num && (ch == 'L' | ch == 'F' | ch == 'D' | ch == 'B' | ch == 'S')) {
            ch = bytes[offset++];
        } else {
            boolean small = false;
            if (ch == '.') {
                small = true;
                ch = offset == end ? EOI : bytes[offset++];

                while (ch >= '0' && ch <= '9') {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }

            if (!num && !small) {
                throw numberError(offset, ch);
            }

            if (ch == 'e' || ch == 'E') {
                ch = bytes[offset++];

                boolean eSign = false;
                if (ch == '+' || ch == '-') {
                    eSign = true;
                    if (offset < end) {
                        ch = bytes[offset++];
                    } else {
                        throw numberError(offset, ch);
                    }
                }

                if (ch >= '0' && ch <= '9') {
                    do {
                        ch = offset == end ? EOI : bytes[offset++];
                    } while (ch >= '0' && ch <= '9');
                } else if (eSign) {
                    throw numberError(offset, ch);
                }
            }

            if (ch == 'F' || ch == 'D') {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipString(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        int ch = jsonReader.ch;
        int quote = ch;
        int index = IOUtils.indexOfQuote(bytes, quote, offset, end);
        if (index == -1) {
            throw jsonReader.error("invalid escape character EOI");
        }
        int slashIndex = indexOfSlash(jsonReader, bytes, offset, end);
        if (slashIndex == -1 || slashIndex > index) {
            offset = index + 1;
            ch = offset == end ? EOI : bytes[offset++];
        } else {
            offset = skipStringEscaped(jsonReader, bytes, slashIndex, quote);
            ch = offset == end ? EOI : bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipStringEscaped(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int quote) {
        int ch = bytes[offset++];
        for (; ; ) {
            if (ch == '\\') {
                ch = bytes[offset++];
                if (ch == 'u') {
                    offset += 4;
                } else if (ch == 'x') {
                    offset += 2;
                } else if (ch != '\\' && ch != '"') {
                    jsonReader.char1(ch);
                }
                ch = bytes[offset++];
                continue;
            }
            if (ch == quote) {
                return offset;
            }

            ch = bytes[offset++];
        }
    }

    private static int skipObject(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        offset = next(jsonReader, bytes, offset, end);
        for (int i = 0; ; ++i) {
            if (jsonReader.ch == '}') {
                break;
            }
            if (i != 0 && !jsonReader.comma) {
                throw jsonReader.valueError();
            }
            offset = skipName(jsonReader, bytes, offset, end);
            offset = skipValue(jsonReader, bytes, offset, end);
        }

        int ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    public static int next(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            jsonReader.char_utf8(ch, offset);
            return jsonReader.offset;
        }

        jsonReader.ch = (char) ch;
        if (ch == '/') {
            jsonReader.offset = offset;
            jsonReader.skipComment();
            return jsonReader.offset;
        } else {
            return offset;
        }
    }

    private static int skipArray(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        offset = next(jsonReader, bytes, offset, end);
        for (int i = 0; ; ++i) {
            if (jsonReader.ch == ']') {
                break;
            }
            if (i != 0 && !jsonReader.comma) {
                throw jsonReader.valueError();
            }
            offset = skipValue(jsonReader, bytes, offset, end);
        }

        int ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (!comma && ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        if (comma && (ch == '}' || ch == ']' || ch == EOI)) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipFalse(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        if (offset + 4 > end || IOUtils.notALSE(bytes, offset)) {
            throw jsonReader.error();
        }
        offset += 4;
        int ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipTrue(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        if (offset + 3 > end || IOUtils.notTRUE(bytes, offset - 1)) {
            throw jsonReader.error();
        }
        offset += 3;
        int ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipNull(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        if (offset + 3 > end || IOUtils.notNULL(bytes, offset - 1)) {
            throw jsonReader.error();
        }
        offset += 3;
        int ch = offset == end ? EOI : bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        boolean comma = false;
        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if ((ch == '}' || ch == ']' || ch == EOI)) {
                throw jsonReader.error(offset, ch);
            }
        } else if (ch != '}' && ch != ']' && ch != EOI) {
            throw jsonReader.error(offset, ch);
        }

        jsonReader.comma = comma;
        jsonReader.ch = (char) ch;
        return offset;
    }

    private static int skipSet(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        if (nextIfSet(jsonReader, bytes, offset, end)) {
            return skipArray(jsonReader, bytes, jsonReader.offset, end);
        } else {
            throw jsonReader.error();
        }
    }

    private static boolean nextIfSet(JSONReaderUTF8 jsonReader, byte[] chars, int offset, int end) {
        if (offset + 1 < end && chars[offset] == 'e' && chars[offset + 1] == 't') {
            offset += 2;
            int ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
            jsonReader.offset = offset;
            jsonReader.ch = (char) ch;
            return true;
        }
        return false;
    }

    private static int skipValue(JSONReaderUTF8 jsonReader, byte[] bytes, int offset, int end) {
        switch (jsonReader.ch) {
            case 't':
                return skipTrue(jsonReader, bytes, offset, end);
            case 'f':
                return skipFalse(jsonReader, bytes, offset, end);
            case 'n':
                return skipNull(jsonReader, bytes, offset, end);
            case '"':
            case '\'':
                return skipString(jsonReader, bytes, offset, end);
            case '{':
                return skipObject(jsonReader, bytes, offset, end);
            case '[':
                return skipArray(jsonReader, bytes, offset, end);
            case 'S':
                return skipSet(jsonReader, bytes, offset, end);
            default:
                return skipNumber(jsonReader, bytes, offset, end);
        }
    }

    @Override
    public final void skipValue() {
        this.offset = skipValue(this, bytes, offset, end);
    }

    @Override
    public final String getString() {
        if (stringValue != null) {
            return stringValue;
        }
        final byte[] bytes = this.bytes;
        int offset = nameBegin, end = this.end;
        int length = nameEnd - offset;
        if (!nameEscape) {
            if (ANDROID) {
                return getLatin1String(offset, length);
            }
            return new String(bytes, offset, length,
                    nameAscii ? ISO_8859_1 : UTF_8
            );
        }

        char[] chars = new char[nameLength];

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
                            int c2 = bytes[offset + 1];
                            int c3 = bytes[offset + 2];
                            int c4 = bytes[offset + 3];
                            offset += 4;
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
                        c = hexDigit4(bytes, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        int c1 = bytes[offset + 1];
                        int c2 = bytes[offset + 2];
                        c = char2(c1, c2);
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
            chars[i] = (char) c;
            offset++;
        }

        return stringValue = new String(chars);
    }

    @Override
    public final void skipComment() {
        int offset = this.offset;
        if (offset + 1 >= this.end) {
            throw new JSONException(info());
        }

        final byte[] bytes = this.bytes;
        byte ch = bytes[offset++];

        boolean multi;
        if (ch == '*') {
            multi = true;
        } else if (ch == '/') {
            multi = false;
        } else {
            throw new JSONException(info("parse comment error"));
        }

        ch = bytes[offset++];

        while (true) {
            boolean endOfComment = false;
            if (multi) {
                if (ch == '*'
                        && offset <= end && bytes[offset] == '/') {
                    offset++;
                    endOfComment = true;
                }
            } else {
                endOfComment = ch == '\n';
            }

            if (endOfComment) {
                if (offset >= this.end) {
                    ch = EOI;
                    break;
                }

                ch = bytes[offset];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    offset++;
                    if (offset >= this.end) {
                        ch = EOI;
                        break;
                    }
                    ch = bytes[offset];
                }

                offset++;
                break;
            }

            if (offset >= this.end) {
                ch = EOI;
                break;
            }
            ch = bytes[offset++];
        }

        this.ch = (char) ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
    }

    static boolean containsSlashOrQuote(long v, long quote) {
        /*
          for (int i = 0; i < 8; ++i) {
            byte c = (byte) v;
            if (c == '"' || c == '\\') {
                return true;
            }
            v >>>= 8;
          }
          return false;
         */
        long x22 = v ^ quote; // " -> 0x22
        long x5c = v ^ 0x5C5C5C5C5C5C5C5CL; // \n -> 0x0a
        x22 = (x22 - 0x0101010101010101L) & ~x22;
        x5c = (x5c - 0x0101010101010101L) & ~x5c;
        return ((x22 | x5c) & 0x8080808080808080L) != 0;
    }

    @Override
    public String readString() {
        char quote = this.ch;
        if (quote == '"' || quote == '\'') {
            final byte[] bytes = this.bytes;
            int valueLength;
            int offset = this.offset;
            final int start = offset, end = this.end;
            final long byteVectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;
            boolean ascii = true;
            valueEscape = false;

            {
                int i = 0;
                int upperBound = offset + ((end - offset) & ~7);
                while (offset < upperBound) {
                    long v = getLongLE(bytes, offset);
                    if ((v & 0x8080808080808080L) != 0 || JSONReaderUTF8.containsSlashOrQuote(v, byteVectorQuote)) {
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

                    int c = bytes[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = bytes[offset + 1];
                        offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                        if (ascii && (c == 'u' || c == 'x')) {
                            ascii = false;
                        }
                        continue;
                    }

                    if (c >= 0) {
                        if (c == quote) {
                            valueLength = i;
                            break;
                        }
                        offset++;
                    } else {
                        ascii = false;
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
                            default: {
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
                }
            }

            String str;
            if (valueEscape) {
                if (ascii && STRING_CREATOR_JDK11 != null) {
                    byte[] chars = new byte[valueLength];
                    offset = start;
                    for (int i = 0; ; ++i) {
                        byte ch = bytes[offset];
                        if (ch == '\\') {
                            ch = bytes[++offset];
                            switch (ch) {
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
                                    ch = (byte) char1(ch);
                                    break;
                            }
                        }
                        else if (ch == quote) {
                            break;
                        }
                        chars[i] = ch;
                        offset++;
                    }

                    str = STRING_CREATOR_JDK11.apply(chars, LATIN1);
                } else {
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
                        }
                        else if (ch == quote) {
                            break;
                        }
                        else {
                            if (ch >= 0) {
                                chars[i] = (char) ch;
                                offset++;
                            }
                            else {
                                switch ((ch & 0xFF) >> 4) {
                                    case 12:
                                    case 13: {
                                        /* 110x xxxx   10xx xxxx*/
                                        chars[i] = (char) (((ch & 0x1F) << 6) | (bytes[offset + 1] & 0x3F));
                                        offset += 2;
                                        break;
                                    }
                                    case 14: {
                                        chars[i] = (char)
                                                (((ch & 0x0F) << 12) |
                                                        ((bytes[offset + 1] & 0x3F) << 6) |
                                                        ((bytes[offset + 2] & 0x3F) << 0));
                                        offset += 3;
                                        break;
                                    }
                                    default: {
                                        /* 10xx xxxx,  1111 xxxx */
                                        char2_utf8(bytes, offset, ch, chars, i);
                                        offset += 4;
                                        i++;
                                    }
                                }
                            }
                        }
                    }

                    str = new String(chars);
                }
            } else if (ascii) {
                int strlen = offset - start;
                if (strlen == 1) {
                    str = TypeUtils.toString((char) (bytes[start] & 0xff));
                } else if (strlen == 2) {
                    str = TypeUtils.toString(
                            (char) (bytes[start] & 0xff),
                            (char) (bytes[start + 1] & 0xff)
                    );
                } else if (STRING_CREATOR_JDK11 != null) {
                    str = STRING_CREATOR_JDK11.apply(
                            Arrays.copyOfRange(this.bytes, this.offset, offset),
                            LATIN1);
                } else {
                    str = new String(bytes, start, offset - start, StandardCharsets.US_ASCII);
                }
            } else {
                str = new String(bytes, start, offset - start, StandardCharsets.UTF_8);
            }

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

            this.ch = (char) ch;
            this.offset = offset;
            return str;
        } else if (quote == 'n') {
            readNull();
            return null;
        }

        return readStringNotMatch();
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

        final byte[] bytes = this.bytes;
        final int end = this.end;
        int ch = this.ch;
        int offset = this.offset;
        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = (char) bytes[offset++];

            if (ch == quote) {
                ch = offset == end ? EOI : bytes[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : bytes[offset++];
                }

                this.ch = (char) ch;
                this.offset = offset;
                comma = nextIfComma();
                wasNull = true;
                valueType = JSON_TYPE_NULL;
                return;
            }
        }

        final int start = offset;

        if (ch == '-') {
            if (offset == end) {
                throw new JSONException(info("illegal input"));
            }
            negative = true;
            ch = bytes[offset++];
        } else {
            if (ch == '+') {
                if (offset == end) {
                    throw new JSONException(info("illegal input"));
                }
                ch = bytes[offset++];
            }
        }

        // if (result < limit + digit) {
        boolean intOverflow = false;
        valueType = JSON_TYPE_INT;
        while (ch >= '0' && ch <= '9') {
            valid = true;
            if (!intOverflow) {
                int digit = ch - '0';
                mag3 *= 10;
                if (mag3 < Integer.MIN_VALUE / 10) {
                    intOverflow = true;
                } else {
                    mag3 -= digit;
                    if (mag3 < Integer.MIN_VALUE / 10) {
                        intOverflow = true;
                    }
                }
            }
            if (offset == end) {
                ch = EOI;
                offset++;
                break;
            }
            ch = bytes[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = bytes[offset++];
            while (ch >= '0' && ch <= '9') {
                valid = true;
                if (!intOverflow) {
                    int digit = ch - '0';
                    mag3 *= 10;
                    if (mag3 < Integer.MIN_VALUE / 10) {
                        intOverflow = true;
                    } else {
                        mag3 -= digit;
                        if (mag3 < Integer.MIN_VALUE / 10) {
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
                ch = bytes[offset++];
            }
        }

        if (intOverflow) {
            int numStart = negative ? start : start - 1;
            int numDigits = scale > 0 ? offset - 2 - numStart : offset - 1 - numStart;
            if (numDigits > 38) {
                valueType = JSON_TYPE_BIG_DEC;
                if (negative) {
                    numStart--;
                }
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
            ch = bytes[offset++];

            if (ch == '-') {
                negativeExp = true;
                ch = bytes[offset++];
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
                ch = bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n') {
                if (bytes[offset] == 'u'
                        && bytes[offset + 1] == 'l'
                        && bytes[offset + 2] == 'l'
                ) {
                    offset += 3;
                    valid = true;
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    ch = offset == end ? EOI : bytes[offset++];
                }
            } else if (ch == 't' && bytes[offset] == 'r' && bytes[offset + 1] == 'u' && bytes[offset + 2] == 'e') {
                offset += 3;
                valid = true;
                boolValue = true;
                valueType = JSON_TYPE_BOOL;
                ch = offset == end ? EOI : bytes[offset++];
            } else if (ch == 'f' && offset + 3 < end && IOUtils.isALSE(bytes, offset)) {
                valid = true;
                offset += 4;
                boolValue = false;
                valueType = JSON_TYPE_BOOL;
                ch = offset == end ? EOI : bytes[offset++];
            } else if (ch == 'N' && bytes[offset] == 'a' && bytes[offset + 1] == 'N') {
                offset += 2;
                valid = true;
                boolValue = true;
                valueType = JSON_TYPE_NaN;
                ch = offset == end ? EOI : bytes[offset++];
            } else if (ch == '{' && quote == 0) {
                this.offset = offset;
                this.ch = (char) ch;
                this.complex = readObject();
                valueType = JSON_TYPE_OBJECT;
                return;
            } else if (ch == '[' && quote == 0) {
                this.offset = offset;
                this.ch = (char) ch;
                this.complex = readArray();
                valueType = JSON_TYPE_ARRAY;
                return;
            }
        }

        if (quote != 0) {
            if (ch != quote) {
                this.offset = firstOffset;
                this.ch = (char) quote;
                readString0();
                valueType = JSON_TYPE_STRING;
                return;
            }

            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
            switch (ch) {
                case 'B':
                    if (!intOverflow && valueType != JSON_TYPE_DEC) {
                        valueType = JSON_TYPE_INT8;
                    }
                    break;
                case 'S':
                    if (!intOverflow && valueType != JSON_TYPE_DEC) {
                        valueType = JSON_TYPE_INT16;
                    }
                    break;
                case 'L':
                    if (offset - start < 19 && valueType != JSON_TYPE_DEC) {
                        valueType = JSON_TYPE_INT64;
                    }
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
            ch = offset == end ? EOI : bytes[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (!valid) {
            throw new JSONException(info("illegal input error"));
        }

        this.offset = offset;
        this.ch = (char) ch;
    }

    @Override
    public final void readNumber(ValueConsumer consumer, boolean quoted) {
        boolean valid = false;
        this.wasNull = false;
        this.boolValue = false;
        this.mag0 = 0;
        this.mag1 = 0;
        this.mag2 = 0;
        this.mag3 = 0;
        this.negative = false;
        this.exponent = 0;
        this.scale = 0;

        final int end = this.end;
        final byte[] bytes = this.bytes;
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
            valid = true;
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
                valid = true;
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
                valid = true;
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
                    valid = true;
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    ch = (char) bytes[offset++];
                }
            } else if (ch == 't') {
                if (bytes[offset++] == 'r'
                        && bytes[offset++] == 'u'
                        && bytes[offset++] == 'e'
                ) {
                    valid = true;
                    boolValue = true;
                    valueType = JSON_TYPE_BOOL;
                    ch = (char) bytes[offset++];
                }
            } else if (ch == 'f' && offset + 3 < end && IOUtils.isALSE(bytes, offset)) {
                valid = true;
                offset += 4;
                boolValue = false;
                valueType = JSON_TYPE_BOOL;
                ch = (char) bytes[offset++];
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
        if (!valid) {
            throw new JSONException(info("illegal input error"));
        }
    }

    @Override
    public final boolean readIfNull() {
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        int offset = this.offset;
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
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : (char) bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final boolean isNull() {
        return ch == 'n' && offset < end && bytes[offset] == 'u';
    }

    @Override
    public final Date readNullOrNewDate() {
        final byte[] bytes = this.bytes;
        int ch;
        int offset = this.offset;

        Date date = null;
        if (offset + 2 < end
                && bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = bytes[offset + 3];
            }
            offset += 4;
        } else if (offset + 1 < end
                && bytes[offset] == 'e'
                && bytes[offset + 1] == 'w') {
            if (offset + 3 == end) {
                ch = EOI;
            } else {
                ch = bytes[offset + 2];
            }
            offset += 3;

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (offset + 4 < end
                    && ch == 'D'
                    && bytes[offset] == 'a'
                    && bytes[offset + 1] == 't'
                    && bytes[offset + 2] == 'e') {
                if (offset + 3 == end) {
                    ch = EOI;
                } else {
                    ch = bytes[offset + 3];
                }
                offset += 4;
            } else {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            if (ch != '(' || offset >= end) {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = bytes[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }

            this.ch = (char) ch;
            this.offset = offset;
            long millis = readInt64Value();

            ch = this.ch;
            offset = this.offset;

            if (ch != ')') {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = offset >= end ? EOI : bytes[offset++];

            date = new Date(millis);
        } else {
            throw new JSONException("json syntax error, not match null or new Date" + offset);
        }

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
        this.ch = (char) ch;
        return date;
    }

    @Override
    public final boolean nextIfNull() {
        int offset = this.offset;
        if (ch == 'n' && offset + 2 < end && this.bytes[offset] == 'u') {
            this.readNull();
            return true;
        }
        return false;
    }

    @Override
    public final void readNull() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch;
        if (bytes[offset] == 'u'
                && bytes[offset + 1] == 'l'
                && bytes[offset + 2] == 'l') {
            offset += 3;
            ch = offset == end ? EOI : bytes[offset++];
        } else {
            throw new JSONException("json syntax error, not match null" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset >= end ? EOI : bytes[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset >= end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset >= end ? EOI : bytes[offset++];
            }
        }
        this.ch = (char) ch;
        this.offset = offset;
    }

    @Override
    public final double readNaN() {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch;
        if (bytes[offset] == 'a'
                && bytes[offset + 1] == 'N') {
            offset += 2;
            ch = offset == end ? EOI : bytes[offset++];
        } else {
            throw new JSONException("json syntax error, not NaN " + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset >= end ? EOI : bytes[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset >= end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset >= end ? EOI : bytes[offset++];
            }
        }
        this.ch = (char) ch;
        this.offset = offset;
        return Double.NaN;
    }

    @Override
    public final int getStringLength() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("string length only support string input " + ch);
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
        char quote = ch;
        if (quote == '"' || quote == '\'') {
            if (!this.context.formatComplex) {
                int yy;
                long ymd;
                int c10 = offset + 10;
                if (c10 < bytes.length
                        && c10 < end
                        && (yy = yy(bytes, offset)) != -1
                        && ((ymd = ymd(bytes, offset + 2))) != -1L
                        && bytes[offset + 10] == quote
                ) {
                    int year = yy + ((int) ymd & 0xFF);
                    int month = (int) (ymd >> 24) & 0xFF;
                    int dom = (int) (ymd >> 48) & 0xFF;

                    LocalDate ldt;
                    try {
                        ldt = (year | month | dom) == 0
                                ? null
                                : LocalDate.of(year, month, dom);
                    } catch (DateTimeException ex) {
                        throw error("read date error", ex);
                    }

                    this.offset = offset + 11;
                    next();
                    if (comma = (this.ch == ',')) {
                        next();
                    }
                    return ldt;
                }

                LocalDate localDate = readLocalDate0(offset, bytes, quote);
                if (localDate != null) {
                    return localDate;
                }
            }
        }
        return super.readLocalDate();
    }

    private LocalDate readLocalDate0(int offset, byte[] bytes, char quote) {
        int nextQuoteOffset = -1;
        for (int i = offset, end = Math.min(i + 17, this.end); i < end; ++i) {
            if (bytes[i] == quote) {
                nextQuoteOffset = i;
            }
        }
        if (nextQuoteOffset != -1
                && nextQuoteOffset - offset > 10
                && bytes[nextQuoteOffset - 6] == '-'
                && bytes[nextQuoteOffset - 3] == '-'
        ) {
            int year = TypeUtils.parseInt(bytes, offset, nextQuoteOffset - offset - 6);
            int month = IOUtils.digit2(bytes, nextQuoteOffset - 5);
            int dayOfMonth = IOUtils.digit2(bytes, nextQuoteOffset - 2);
            LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
            this.offset = nextQuoteOffset + 1;
            next();
            if (comma = (this.ch == ',')) {
                next();
            }
            return localDate;
        }
        return null;
    }

    public final OffsetDateTime readOffsetDateTime() {
        final byte[] bytes = this.bytes;
        int offset = this.offset, end = this.end;
        char quote = this.ch;
        if (quote == '"' || quote == '\'') {
            if (!this.context.formatComplex) {
                byte c10;
                int off21 = offset + 19;
                int yy;
                long ymd, hms;
                if (off21 < bytes.length
                        && off21 < end
                        && (yy = yy(bytes, offset)) != -1
                        && ((ymd = ymd(bytes, offset + 2))) != -1L
                        && ((c10 = bytes[offset + 10]) == ' ' || c10 == 'T')
                        && ((hms = hms(bytes, offset + 11))) != -1L
                ) {
                    int nanos = 0, nanoSize = 0;
                    offset += 19;
                    int ch = bytes[offset++];
                    if (ch == '.') {
                        ch = bytes[offset++];
                    }
                    while (ch >= '0' && ch <= '9') {
                        nanos = nanos * 10 + (ch - '0');
                        nanoSize++;
                        if (offset < end) {
                            ch = bytes[offset++];
                        } else {
                            break;
                        }
                    }
                    if (nanoSize != 0) {
                        nanos = DateUtils.nanos(nanos, nanoSize);
                    }
                    ZoneOffset zoneOffset = ZoneOffset.UTC;
                    if (ch == 'Z') {
                        ch = bytes[offset++];
                    } else if (ch != quote) {
                        int quoteIndex = IOUtils.indexOfChar(bytes, '"', offset, end);
                        if (quoteIndex != -1) {
                            zoneOffset = DateUtils.zoneOffset(bytes, offset - 1, quoteIndex - offset + 1);
                            offset = quoteIndex + 1;
                            ch = quote;
                        }
                    }
                    if (ch == quote) {
                        ch = offset >= end ? EOI : bytes[offset++];
                        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                            ch = offset == end ? EOI : bytes[offset++];
                        }
                        if (comma = (ch == ',')) {
                            ch = offset == end ? EOI : (char) bytes[offset++];
                            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                                ch = offset == end ? EOI : bytes[offset++];
                            }
                        }
                        if (ch < 0) {
                            char_utf8(ch, offset);
                        } else {
                            this.offset = offset;
                            this.ch = (char) ch;
                        }
                        return OffsetDateTime.of(
                                yy + ((int) ymd & 0xFF),
                                (int) (ymd >> 24) & 0xFF,
                                (int) (ymd >> 48) & 0xFF,
                                (int) hms & 0xFF,
                                (int) (hms >> 24) & 0xFF,
                                (int) (hms >> 48) & 0xFF,
                                nanos,
                                zoneOffset);
                    }
                }
            }
        }
        ZonedDateTime zdt = readZonedDateTime();
        return zdt == null ? null : zdt.toOffsetDateTime();
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
                    this.offset += len + 1;
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
    protected final LocalTime readLocalTime6() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime6(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 7;
        next();
        if (comma = (ch == ',')) {
            next();
        }

        return time;
    }

    @Override
    protected final LocalTime readLocalTime7() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime7(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 8;
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
    protected final LocalTime readLocalTime15() {
        if (!isString()) {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime15(bytes, offset);
        if (time == null) {
            return null;
        }

        offset += 16;
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
            ldt = zdt.toInstant().atZone(context.getZoneId()).toLocalDateTime();
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
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        boolean value = false;

        BigDecimal decimal = null;
        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = bytes[offset++];

            if (ch == quote) {
                this.ch = offset == end ? EOI : (char) bytes[offset++];
                this.offset = offset;
                nextIfComma();
                return null;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = bytes[offset++];
        } else {
            negative = false;
            if (ch == '+') {
                ch = bytes[offset++];
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
            ch = bytes[offset++];
        }

        if (longValue < 0) {
            overflow = true;
        }

        this.scale = 0;
        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = bytes[offset++];
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
                ch = bytes[offset++];
            }
        }

        int expValue = 0;
        if (ch == 'e' || ch == 'E') {
            boolean negativeExp;
            ch = bytes[offset++];
            if ((negativeExp = ch == '-') || ch == '+') {
                ch = bytes[offset++];
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
                ch = bytes[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n' && bytes[offset++] == 'u' && bytes[offset++] == 'l' && bytes[offset++] == 'l') {
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }

                wasNull = true;
                value = true;
                ch = offset == end ? EOI : bytes[offset];
                offset++;
                valid = true;
            } else if (ch == 't' && offset + 3 <= end && bytes[offset] == 'r' && bytes[offset + 1] == 'u' && bytes[offset + 2] == 'e') {
                valid = true;
                offset += 3;
                value = true;
                decimal = BigDecimal.ONE;
                ch = offset == end ? EOI : bytes[offset];
                offset++;
            } else if (ch == 'f' && offset + 4 <= end && IOUtils.isALSE(bytes, offset)) {
                valid = true;
                offset += 4;
                decimal = BigDecimal.ZERO;
                value = true;
                ch = offset == end ? EOI : bytes[offset];
                offset++;
            } else if (ch == '{' && quote == 0) {
                JSONObject jsonObject = new JSONObject();
                readObject(jsonObject, 0);
                wasNull = false;
                return decimal(jsonObject);
            } else if (ch == '[' && quote == 0) {
                List array = readArray();
                if (!array.isEmpty()) {
                    throw new JSONException(info());
                }
                wasNull = true;
                return null;
            }
        }

        int len = offset - start;

        if (quote != 0) {
            if (ch != quote) {
                String str = readString();
                try {
                    return TypeUtils.toBigDecimal(str);
                } catch (NumberFormatException e) {
                    throw new JSONException(info(e.getMessage()), e);
                }
            } else {
                ch = offset >= end ? EOI : bytes[offset++];
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
                ch = offset >= end ? EOI : bytes[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            // next inline
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        if (!valid) {
            throw new JSONException(info("illegal input error"));
        }

        this.ch = (char) ch;
        this.offset = offset;
        return decimal;
    }

    @Override
    public final UUID readUUID() {
        int ch = this.ch, end = this.end;
        if (ch == 'n') {
            readNull();
            return null;
        }

        if (ch != '"' && ch != '\'') {
            throw error("syntax error, can not read uuid");
        }
        final int quote = ch;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        UUID uuid;
        if (offset + 36 < end
                && bytes[offset + 36] == quote
                && bytes[offset + 8] == '-'
                && bytes[offset + 13] == '-'
                && bytes[offset + 18] == '-'
                && bytes[offset + 23] == '-'
        ) {
            uuid = readUUID36(bytes, offset);
            offset += 37;
        } else if (offset + 32 < end && bytes[offset + 32] == quote) {
            uuid = readUUID32(bytes, offset);
            offset += 33;
        } else {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }
            return UUID.fromString(str);
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        this.offset = offset;
        if (comma = (ch == ',')) {
            next();
        } else {
            this.ch = (char) ch;
        }

        return uuid;
    }

    static UUID readUUID32(byte[] bytes, int offset) {
        long msb1 = parse4Nibbles(bytes, offset);
        long msb2 = parse4Nibbles(bytes, offset + 4);
        long msb3 = parse4Nibbles(bytes, offset + 8);
        long msb4 = parse4Nibbles(bytes, offset + 12);
        long lsb1 = parse4Nibbles(bytes, offset + 16);
        long lsb2 = parse4Nibbles(bytes, offset + 20);
        long lsb3 = parse4Nibbles(bytes, offset + 24);
        long lsb4 = parse4Nibbles(bytes, offset + 28);

        if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) < 0) {
            throw new JSONException("Invalid UUID string:  ".concat(new String(bytes, offset, 32, ISO_8859_1)));
        }

        return new UUID(
                msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
    }

    static UUID readUUID36(byte[] bytes, int offset) {
        long msb1 = parse4Nibbles(bytes, offset);
        long msb2 = parse4Nibbles(bytes, offset + 4);
        long msb3 = parse4Nibbles(bytes, offset + 9);
        long msb4 = parse4Nibbles(bytes, offset + 14);
        long lsb1 = parse4Nibbles(bytes, offset + 19);
        long lsb2 = parse4Nibbles(bytes, offset + 24);
        long lsb3 = parse4Nibbles(bytes, offset + 28);
        long lsb4 = parse4Nibbles(bytes, offset + 32);

        if ((msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) < 0) {
            throw new JSONException("Invalid UUID string:  ".concat(new String(bytes, offset, 36, ISO_8859_1)));
        }

        return new UUID(
                msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
    }

    static long parse4Nibbles(byte[] bytes, int offset) {
        int x = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        byte[] ns = NIBBLES;
        return ns[x & 0xFF] << 12 | ns[(x >> 8) & 0xFF] << 8 | ns[(x >> 16) & 0xFF] << 4 | ns[(x >> 24) & 0xFF];
    }

    @Override
    public final String readPattern() {
        if (ch != '/') {
            throw new JSONException("illegal pattern");
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int start = offset;
        for (; offset < end; ++offset) {
            if (bytes[offset] == '/') {
                break;
            }
        }
        String str = new String(bytes, start, offset - start, UTF_8);
        int ch = ++offset == end ? EOI : bytes[offset++];
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
        this.ch = (char) ch;

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

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1) {
        if (ch != c0) {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 1 > end || bytes[offset] != c1) {
            return false;
        }

        offset += 1;
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (offset == this.offset + 2 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
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

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 2 > end || bytes[offset] != c1 || bytes[offset + 1] != c2) {
            return false;
        }

        offset += 2;
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (offset == this.offset + 3 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        if (ch != c0) {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 3 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3) {
            return false;
        }

        offset += 3;
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (offset == this.offset + 4 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        if (ch != c0) {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 4 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3
                || bytes[offset + 3] != c4) {
            return false;
        }

        offset += 4;
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (offset == this.offset + 5 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        if (ch != c0) {
            return false;
        }

        final byte[] bytes = this.bytes;
        int offset = this.offset;
        if (offset + 5 > end
                || bytes[offset] != c1
                || bytes[offset + 1] != c2
                || bytes[offset + 2] != c3
                || bytes[offset + 3] != c4
                || bytes[offset + 4] != c5) {
            return false;
        }

        offset += 5;
        int ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        if (offset == this.offset + 6 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    @Override
    public final byte[] readHex() {
        int offset = this.offset;
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        if (ch == 'x') {
            ch = offset == end ? EOI : bytes[offset++];
        }

        final int quote = ch;
        if (quote != '\'' && quote != '"') {
            throw syntaxError(offset, ch);
        }
        int start = offset;
        ch = offset == end ? EOI : bytes[offset++];

        for (; ; ) {
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                // continue;
            } else if (ch == quote) {
                ch = offset == end ? EOI : bytes[offset++];
                break;
            } else {
                throw syntaxError(offset, ch);
            }
            ch = offset == end ? EOI : bytes[offset++];
        }

        int len = offset - start - 2;
        if (ch == EOI) {
            len++;
        }

        if (len % 2 != 0) {
            throw syntaxError(offset, ch);
        }

        byte[] hex = new byte[len / 2];
        for (int i = 0; i < hex.length; ++i) {
            byte c0 = bytes[start + i * 2];
            byte c1 = bytes[start + i * 2 + 1];

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            hex[i] = (byte) ((b0 << 4) | b1);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch != ',' || offset >= end) {
            this.offset = offset;
            this.ch = (char) ch;
            return hex;
        }

        comma = true;
        ch = offset == end ? EOI : bytes[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : bytes[offset++];
        }
        this.offset = offset;
        this.ch = (char) ch;
        if (this.ch == '/') {
            skipComment();
        }

        return hex;
    }

    @Override
    public final boolean isReference() {
        // should be codeSize <= FreqInlineSize 325, current : 284
        if ((context.features & MASK_DISABLE_REFERENCE_DETECT) != 0) {
            return false;
        }
        final byte[] bytes = this.bytes;
        int ch = this.ch;
        if (ch != '{') {
            return false;
        }

        int offset = this.offset, end = this.end;
        if (offset == end) {
            return false;
        }

        ch = bytes[offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = bytes[offset];
        }

        if (offset + 6 >= end
                || bytes[offset + 5] != ch
                || UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset + 1) != REF
        ) {
            return false;
        }

        return isReference0(bytes, offset, end, ch);
    }

    private boolean isReference0(byte[] bytes, int offset, int end, int quote) {
        int ch;
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

        if (ch != quote
                || (offset + 1 < end && (ch = bytes[offset + 1]) != '$' && ch != '.' && ch != '@')
        ) {
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
        final byte[] chars = this.bytes;
        this.offset = referenceBegin;
        this.ch = (char) chars[offset++];

        String reference = readString();

        int ch = this.ch;
        int offset = this.offset;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch != '}') {
            throw new JSONException("illegal reference : ".concat(reference));
        }

        ch = offset == end ? EOI : chars[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = (char) ch;
        this.offset = offset;

        return reference;
    }

    public final boolean readBoolValue() {
        boolean val = false;
        int end = this.end;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        if (ch == 't'
                && offset + 2 < bytes.length
                && bytes[offset] == 'r'
                && bytes[offset + 1] == 'u'
                && bytes[offset + 2] == 'e'
        ) {
            offset += 3;
            val = true;
        } else if (ch == 'f' && offset + 3 < end && isALSE(bytes, offset)) {
            offset += 4;
        } else if ((ch == '1' || ch == '0') && offset < end && !IOUtils.isDigit(ch)) {
            val = ch == '1';
        } else {
            return readBoolValue0();
        }

        ch = offset == end ? EOI : (char) bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset >= end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset >= end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset >= end ? EOI : bytes[offset++];
            }
        }
        this.offset = offset;
        this.ch = (char) ch;

        return val;
    }

    private boolean readBoolValue0() {
        wasNull = false;
        boolean val;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        if (ch == 't'
                && offset + 2 < bytes.length
                && bytes[offset] == 'r'
                && bytes[offset + 1] == 'u'
                && bytes[offset + 2] == 'e'
        ) {
            offset += 3;
            val = true;
        } else if (ch == 'f' && offset + 3 < end && isALSE(bytes, offset)) {
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
        } else if (ch == '"' || ch == '\'') {
            if (offset + 1 < bytes.length
                    && bytes[offset + 1] == ch
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
        } else if (ch == '[') {
            next();
            val = readBoolValue();
            if (!nextIfMatch(']')) {
                throw new JSONException("not closed square brackets, expect ] but found : " + (char) ch);
            }
            return val;
        } else {
            throw new JSONException("syntax error : " + ch);
        }

        ch = offset == end ? EOI : (char) bytes[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset >= end ? EOI : bytes[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset >= end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset >= end ? EOI : bytes[offset++];
            }
        }
        this.offset = offset;
        this.ch = (char) ch;

        return val;
    }

    public final byte[] readBase64() {
        byte[] bytes = this.bytes;
        int offset = this.offset, end = this.end;
        int ch = this.ch;
        int index = IOUtils.indexOfQuote(bytes, ch, offset, end);
        if (index == -1) {
            throw error("invalid escape character EOI");
        }

        int slashIndex = indexOfSlash(this, bytes, offset, end);
        if (slashIndex != -1) {
            throw error("invalid base64 string");
        }

        byte[] decoded;
        if (index != offset) {
            String prefix = "data:image/";
            int p0, p1;
            String base64 = "base64";
            if (regionMatches(bytes, offset, prefix)
                    && (p0 = IOUtils.indexOfChar(bytes, ';', prefix.length() + 1, index)) != -1
                    && (p1 = IOUtils.indexOfChar(bytes, ',', p0 + 1, index)) != -1 && IOUtils.regionMatches(bytes, p0 + 1, base64)) {
                offset = p1 + 1;
            }

            byte[] src = Arrays.copyOfRange(bytes, offset, index);
            decoded = Base64.getDecoder().decode(src);
        } else {
            decoded = new byte[0];
        }

        offset = index + 1;

        ch = offset == end ? EOI : (char) bytes[offset++];
        if (comma = ch == ',') {
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        }

        this.ch = (char) ch;
        this.offset = offset;
        return decoded;
    }

    @Override
    public final String info(String message) {
        int line = 1, column = 0;
        for (int i = 0; i < offset && i < end; i++, column++) {
            if (bytes[i] == '\n') {
                column = 0;
                line++;
            }
        }

        StringBuilder buf = new StringBuilder();

        if (message != null && !message.isEmpty()) {
            buf.append(message).append(", ");
        }

        return buf.append("offset ").append(offset)
                .append(", character ").append(ch)
                .append(", line ").append(line)
                .append(", column ").append(column)
                .append(", fastjson-version ").append(JSON.VERSION)
                .append(line > 1 ? '\n' : ' ')
                .append(new String(bytes, this.start, Math.min(length, 65535)))
                .toString();
    }

    @Override
    public final void close() {
        if (cacheItem != null) {
            if (bytes.length < CACHE_THRESHOLD) {
                BYTES_UPDATER.lazySet(cacheItem, bytes);
            }

            if (charBuf != null && charBuf.length < CACHE_THRESHOLD) {
                CHARS_UPDATER.lazySet(cacheItem, charBuf);
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    public static JSONReaderUTF8 of(byte[] bytes, int off, int len, JSONReader.Context context) {
        boolean ascii = false;
        if (METHOD_HANDLE_HAS_NEGATIVE != null) {
            try {
                ascii = !(boolean) METHOD_HANDLE_HAS_NEGATIVE.invoke(bytes, off, len);
            } catch (Throwable ignored) {
                // ignored
            }
        } else {
            ascii = IOUtils.isASCII(bytes, off, len);
        }
        if (ascii) {
            return new JSONReaderASCII(context, null, bytes, off, len);
        }
        return new JSONReaderUTF8(context, bytes, off, len);
    }
}

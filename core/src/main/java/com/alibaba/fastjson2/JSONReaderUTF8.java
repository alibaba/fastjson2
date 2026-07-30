package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.DateUtils.*;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

class JSONReaderUTF8
        extends JSONReader {
    static final int REF = BIG_ENDIAN ? 0x24726566 : 0x66657224;
    static final int ESCAPE_INDEX_NOT_SET = -2;

    static final byte
            INPUT_CODE_ASCII_NORMAL = 0,
            INPUT_CODE_ASCII_ESCAPE = 1,
            INPUT_CODE_UTF8_2 = 2,
            INPUT_CODE_UTF8_3 = 3,
            INPUT_CODE_UTF8_4 = 4,
            INPUT_CODE_ERROR = -1;

    protected static final byte[] INPUT_CODES;
    protected static final byte[] INPUT_CODES_SINGLE_QUOTE;
    static {
        final byte[] table = new byte[256];
        Arrays.fill(table, 0, 128, INPUT_CODE_ASCII_NORMAL);
        table['"'] = INPUT_CODE_ASCII_ESCAPE;
        table['\\'] = INPUT_CODE_ASCII_ESCAPE;

        for (int c = 128; c < 256; ++c) {
            byte code;
            // We'll add number of bytes needed for decoding
            if ((c & 0xE0) == 0xC0) { // 2 bytes (0x0080 - 0x07FF)
                code = INPUT_CODE_UTF8_2;
            } else if ((c & 0xF0) == 0xE0) { // 3 bytes (0x0800 - 0xFFFF)
                code = INPUT_CODE_UTF8_3;
            } else if ((c & 0xF8) == 0xF0) {
                // 4 bytes; double-char with surrogates and all...
                code = INPUT_CODE_UTF8_4;
            } else {
                // And -1 seems like a good "universal" error marker...
                code = INPUT_CODE_ERROR;
            }
            table[c] = code;
        }
        INPUT_CODES = table;

        byte[] table2 = table.clone();
        table2['\''] = INPUT_CODE_ASCII_ESCAPE;
        INPUT_CODES_SINGLE_QUOTE = table2;
    }

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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        int offset = this.offset, end = this.end;

        if (ch != '}') {
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : bytes[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
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

            nameValue = (((long) ch) << (i << 3)) | nameValue;

            ch = offset == end ? EOI : bytes[offset++];
        }

        long hashCode;

        if (nameValue != 0) {
            hashCode = nameValue;
        } else {
            hashCode = MAGIC_HASH_CODE;
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
                    hashCode *= MAGIC_PRIME;
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
                    hashCode *= MAGIC_PRIME;

                    int c1 = (ch & 0x3ff) + '\uDC00'; // Character.lowSurrogate(uc);
                    hashCode ^= c1;
                } else {
                    hashCode ^= ch;
                }
                hashCode *= MAGIC_PRIME;

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
        boolean nameAscii = IOUtils.isASCII(bytes, start, name_len);
        long nameHashCode = Fnv.hashCode64(bytes, start, name_len, nameAscii);
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

        this.nameAscii = nameAscii;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    @Override
    public long readFieldNameHashCode(int keySize, int min, int max) {
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
        if (slashIndex == -1 || slashIndex > index) {
            offset = index + 1;
        } else {
            return readFieldNameHashCode0();
        }

        boolean nameAscii = false;
        long nameHashCode;
        int name_len = index - start;
        if ((name_len < min || name_len > max) && name_len != keySize) {
            nameHashCode = Long.MAX_VALUE;
        } else {
            nameAscii = IOUtils.isASCII(bytes, start, name_len);
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

        this.nameAscii = nameAscii;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    @Override
    public long readFieldNameHashCodeE(int size0, int size1, int size3) {
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
        if (slashIndex == -1 || slashIndex > index) {
            offset = index + 1;
        } else {
            return readFieldNameHashCode0();
        }

        boolean nameAscii = false;
        long nameHashCode;
        int name_len = index - start;
        if (name_len != size1 && name_len != size3 && name_len != size0) {
            nameHashCode = Long.MAX_VALUE;
        } else {
            nameAscii = IOUtils.isASCII(bytes, start, name_len);
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

        this.nameAscii = nameAscii;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    protected long readFieldNameHashCode0() {
        final byte[] bytes = this.bytes;
        final int quote = ch;
        int offset = this.offset, end = this.end;
        this.nameAscii = true;
        this.nameEscape = false;
        int ch;
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

                nameValue = (((long) ch) << (i << 3)) | nameValue;
            }
        }

        long hashCode;
        if (nameValue != 0) {
            hashCode = nameValue;
        } else {
            hashCode = MAGIC_HASH_CODE;
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
                    hashCode *= MAGIC_PRIME;
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
                hashCode *= MAGIC_PRIME;
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
        boolean nameAscii = IOUtils.isASCII(bytes, start, name_len);
        long nameHashCode = Fnv.hashCode64(bytes, start, name_len, nameAscii);
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

        this.nameAscii = nameAscii;
        this.nameEscape = false;
        this.nameLength = name_len;
        return nameHashCode;
    }

    private long readValueHashCode0() {
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
            hashCode = MAGIC_HASH_CODE;
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
                    hashCode *= MAGIC_PRIME;
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
                                    hashCode *= MAGIC_PRIME;

                                    hashCode ^= x2;
                                    hashCode *= MAGIC_PRIME;
                                    i++;
                                }
                                continue;
                            }
                            throw new JSONException("malformed input around byte " + offset);
                    }
                }

                hashCode ^= ch;
                hashCode *= MAGIC_PRIME;
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
        long hashCode = MAGIC_HASH_CODE;
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

            nameValue = (((long) ch) << (i << 3)) | nameValue;
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
                hashCode *= MAGIC_PRIME;
            }
            return hashCode;
        }

        for (; ; ) {
            int ch = bytes[offset];
            if (ch == '\\') {
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
            hashCode *= MAGIC_PRIME;
        }

        return hashCode;
    }

    final String getLatin1String(int offset, int length) {
        if (JDKUtils.ANDROID_SDK_INT >= 34) {
            return new String(bytes, offset, length, ISO_8859_1);
        }

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        char[] charBuf = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (charBuf == null || charBuf.length < length) {
            charBuf = new char[length];
        }

        for (int i = 0; i < length; i++) {
            charBuf[i] = (char) (bytes[offset + i] & 0xFF);
        }
        String str = new String(charBuf, 0, length);
        CHARS_UPDATER.lazySet(cacheItem, charBuf);
        return str;
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

        char[] chars = new char[nameLength + 4];
        int i = 0;
        for (int end = this.end; offset < nameEnd; ) {
            if (i + 2 >= chars.length) {
                chars = Arrays.copyOf(chars, chars.length + 8);
            }

            int ch = bytes[offset];
            if (ch < 0) {
                ch &= 0xFF;
                switch (ch >> 4) {
                    case 12:
                    case 13: {
                        /* 110x xxxx   10xx xxxx*/
                        ch = char2_utf8(ch, bytes[offset + 1], offset);
                        offset += 2;
                        chars[i++] = (char) ch;
                        continue;
                    }
                    case 14: {
                        ch = char2_utf8(ch, bytes[offset + 1], bytes[offset + 2], offset);
                        offset += 3;
                        chars[i++] = (char) ch;
                        continue;
                    }
                    case 15: {
                        if (offset + 3 >= bytes.length) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        int b1 = bytes[offset + 1] & 0xFF;
                        int b2 = bytes[offset + 2] & 0xFF;
                        int b3 = bytes[offset + 3] & 0xFF;
                        if ((b1 & 0xC0) != 0x80 || (b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        int codePoint = ((ch & 0x07) << 18)
                                | ((b1 & 0x3F) << 12)
                                | ((b2 & 0x3F) << 6)
                                | (b3 & 0x3F);
                        if (codePoint < 0x10000 || codePoint > 0x10FFFF) {
                            throw new JSONException("malformed input around byte " + offset);
                        }
                        offset += 4;
                        chars[i++] = Character.highSurrogate(codePoint);
                        chars[i++] = Character.lowSurrogate(codePoint);
                        continue;
                    }
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new JSONException("malformed input around byte " + offset);
                }
            }

            if (ch == '\\') {
                ch = (char) bytes[++offset];
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
            chars[i++] = (char) ch;
            offset++;
        }

        return new String(chars, 0, i);
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
                }

                if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
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
                }

                if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
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
    public void skipComment() {
        // no-op in tree mode
    }

    @Override
    public String readString() {
        if (ch == '"' || ch == '\'') {
            final byte[] bytes = this.bytes;
            char quote = ch;
            int start = offset;
            int i = offset;
            while (i < bytes.length && bytes[i] != quote) {
                if (bytes[i] == '\\') {
                    i += 2;
                } else {
                    i++;
                }
            }
            if (i < bytes.length) {
                String str = new String(bytes, start, i - start, java.nio.charset.StandardCharsets.UTF_8);
                offset = i + 1;
                if (offset < bytes.length) {
                    ch = (char) bytes[offset];
                }
                return str;
            }
        }
        // Fall back to reading field name or simple value
        return readFieldName();
    }

    @Override
    protected ZonedDateTime readZonedDateTimeX(int len) {
        String str = readString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(str);
    }

    @Override
    protected LocalDate readLocalDate8() { return null; }

    @Override
    protected LocalDate readLocalDate9() { return null; }

    @Override
    protected LocalDate readLocalDate10() { return null; }

    @Override
    protected LocalDate readLocalDate11() { return null; }

    @Override
    protected LocalTime readLocalTime5() { return null; }

    @Override
    protected LocalTime readLocalTime6() { return null; }

    @Override
    protected LocalTime readLocalTime7() { return null; }

    @Override
    protected LocalTime readLocalTime8() { return null; }

    @Override
    protected LocalTime readLocalTime9() { return null; }

    @Override
    protected LocalTime readLocalTime10() { return null; }

    @Override
    protected LocalTime readLocalTime11() { return null; }

    @Override
    protected LocalTime readLocalTime12() { return null; }

    @Override
    protected LocalTime readLocalTime15() { return null; }

    @Override
    protected LocalTime readLocalTime18() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime12() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime14() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime16() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime17() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime18() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime19() { return null; }

    @Override
    protected LocalDateTime readLocalDateTime20() { return null; }

    @Override
    protected LocalDateTime readLocalDateTimeX(int len) { return null; }

    @Override
    public long readMillis19() { return 0; }
    @Override
    protected void readNumber0() {
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
            if (offset == end) {
                throw new JSONException(info("illegal input"));
            }
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
            if (numDigits > MAX_NUMBER_DIGITS) {
                throw new JSONException("Number literal too long: " + numDigits + " digits (max " + MAX_NUMBER_DIGITS + ")");
            }
            if (numDigits > 38) {
                valueType = JSON_TYPE_BIG_DEC;
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
            if (valueType != JSON_TYPE_BIG_DEC) {
                valueType = JSON_TYPE_DEC;
            }
        }

        if (valueType == JSON_TYPE_BIG_DEC) {
            stringValue = new String(bytes, start - 1, offset - start);
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
            } else if (ch == 'f' && offset + 3 < end && isALSE(bytes, offset)) {
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
                readString();
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

        this.ch = (char) ch;
        this.offset = offset;
        if (!valid) {
            throw new JSONException(info("illegal input"));
        }
    }

    @Override
    public void readNull() {
        if (ch == 'n') {
            final byte[] bytes = this.bytes;
            int offset = this.offset;
            if (offset + 2 < bytes.length && bytes[offset] == 'u' && bytes[offset + 1] == 'l' && bytes[offset + 2] == 'l') {
                offset += 3;
                this.offset = offset;
                if (offset < bytes.length) {
                    this.ch = (char) bytes[offset];
                }
            }
        }
    }

    @Override
    public boolean readIfNull() {
        // check if current token is null
        if (ch == 'n') {
            readNull();
            return true;
        }
        return false;
    }

    @Override
    public String getString() {
        return readString();
    }

    @Override
    protected int getStringLength() {
        return -1;
    }

    @Override
    public void close() {
        // no-op, resources managed by caller
    }

    @Override
    public boolean readBoolValue() {
        wasNull = false;
        boolean val;
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        byte ch = (byte) this.ch;
        if (ch == 't' && offset + 2 < bytes.length && bytes[offset] == 'r' && bytes[offset + 1] == 'u' && bytes[offset + 2] == 'e') {
            offset += 3;
            val = true;
        } else if (ch == 'f' && offset + 3 < bytes.length && bytes[offset] == 'a' && bytes[offset + 1] == 'l' && bytes[offset + 2] == 's' && bytes[offset + 3] == 'e') {
            offset += 4;
            val = false;
        } else if (ch == 'n' && offset + 2 < bytes.length && bytes[offset] == 'u' && bytes[offset + 1] == 'l' && bytes[offset + 2] == 'l') {
            if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(info("boolean value not support input null"));
            }
            wasNull = true;
            offset += 3;
            val = false;
        } else {
            // Fallback: read as string and parse
            String str = readString();
            if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
                val = true;
            } else if ("false".equalsIgnoreCase(str) || "0".equals(str)) {
                val = false;
            } else {
                throw new JSONException(info("can not convert to boolean : " + str));
            }
        }
        this.offset = offset;
        ch = bytes[offset];
        this.ch = (char) ch;
        return val;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Date readNullOrNewDate() {
        return null;
    }

    @Override
    public boolean nextIfNull() {
        return false;
    }

    @Override
    public boolean nextIfNullOrEmptyString() {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public String readReference() {
        return null;
    }

    @Override
    public String readPattern() {
        return null;
    }

    @Override
    public boolean skipName() {
        return false;
    }

    @Override
    public void skipValue() {
    }

    @Override
    public byte[] readHex() {
        return null;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1) {
        return false;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2) {
        return false;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        return false;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        return false;
    }

    @Override
    public boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        return false;
    }

    @Override
    public BigDecimal readBigDecimal() {
        return null;
    }

    @Override
    public UUID readUUID() {
        return null;
    }

    @Override
    public OffsetDateTime readOffsetDateTime() {
        return null;
    }

    @Override
    public OffsetTime readOffsetTime() {
        return null;
    }
}

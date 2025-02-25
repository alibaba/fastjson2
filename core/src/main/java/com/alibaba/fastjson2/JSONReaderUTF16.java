package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONReaderJSONB.check3;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONReaderUTF16
        extends JSONReader {
    static final long CHAR_MASK = BIG_ENDIAN ? 0x00ff00ff00ff00ffL : 0xff00ff00ff00ff00L;

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

    JSONReaderUTF16(Context ctx, byte[] bytes, int offset, int length) {
        super(ctx, false, false);

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

        if (ch == '/') {
            skipComment();
        }
    }

    JSONReaderUTF16(Context ctx, Reader input) {
        super(ctx, false, false);
        this.input = input;

        cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        char[] chars = CHARS_UPDATER.getAndSet(cacheItem, null);
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

        if (this.ch == '/') {
            skipComment();
        }
    }

    JSONReaderUTF16(Context ctx, String str, int offset, int length) {
        super(ctx, false, false);

        cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        char[] chars = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (chars == null || chars.length < length) {
            chars = new char[Math.max(length, 8192)];
        }
        str.getChars(offset, offset + length, chars, 0);

        this.str = offset == 0 ? str : null;
        this.chars = chars;
        this.offset = 0;
        this.length = length;
        this.start = 0;
        this.end = this.length;

        // inline next();
        {
            if (this.offset >= end) {
                ch = EOI;
                return;
            }

            ch = chars[this.offset];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                this.offset++;
                if (this.offset >= this.length) {
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

        if (this.ch == '/') {
            skipComment();
        }
    }

    JSONReaderUTF16(Context ctx, String str, char[] chars, int offset, int length) {
        super(ctx, false, false);

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

        if (this.ch == '/') {
            skipComment();
        }
    }

    JSONReaderUTF16(Context ctx, InputStream input) {
        super(ctx, false, false);
        this.input = input;
        final int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        int bufferSize = ctx.bufferSize;
        if (bytes == null) {
            bytes = new byte[bufferSize];
        }

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
                    bytes = Arrays.copyOf(bytes, bytes.length + bufferSize);
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
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }

        int length = chars.length;
        this.str = null;
        this.chars = chars;
        this.offset = 0;
        this.length = length;
        this.start = 0;
        this.end = length;

        if (end == 0) {
            ch = EOI;
            return;
        }

        int offset = 0;
        char ch = chars[offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= length) {
                this.ch = EOI;
                return;
            }
            ch = chars[offset];
        }
        this.ch = ch;
        this.offset++;

        if (ch == '\uFFFE' || ch == '\uFEFF') {
            next();
        }

        if (this.ch == '/') {
            skipComment();
        }
    }

    @Override
    public final byte[] readHex() {
        char ch = this.ch;
        int offset = this.offset;
        char[] chars = this.chars;
        if (ch == 'x') {
            ch = offset == end ? EOI : chars[offset++];
        }

        char quote = ch;
        if (quote != '\'' && quote != '"') {
            throw syntaxError(offset, ch);
        }
        int start = offset;
        ch = offset == end ? EOI : chars[offset++];

        for (; ; ) {
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                // continue;
            } else if (ch == quote) {
                ch = offset == end ? EOI : chars[offset++];
                break;
            } else {
                throw syntaxError(offset, ch);
            }
            ch = offset == end ? EOI : chars[offset++];
        }

        int len = offset - start - 2;
        if (ch == EOI) {
            len++;
        }

        if (len % 2 != 0) {
            throw syntaxError(offset, ch);
        }

        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < bytes.length; ++i) {
            char c0 = chars[start + i * 2];
            char c1 = chars[start + i * 2 + 1];

            int b0 = c0 - (c0 <= 57 ? 48 : 55);
            int b1 = c1 - (c1 <= 57 ? 48 : 55);
            bytes[i] = (byte) ((b0 << 4) | b1);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch != ',' || offset >= end) {
            this.offset = offset;
            this.ch = ch;
            return bytes;
        }

        comma = true;

        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }
        this.offset = offset;
        this.ch = ch;
        if (this.ch == '/') {
            skipComment();
        }

        return bytes;
    }

    public final boolean isReference() {
        if ((context.features & MASK_DISABLE_REFERENCE_DETECT) != 0) {
            return false;
        }
        // should be codeSize <= FreqInlineSize 325, current is 276
        final char[] chars = this.chars;
        char ch = this.ch;
        if (ch != '{') {
            return false;
        }

        int offset = this.offset, end = this.end;
        if (offset == end) {
            return false;
        }

        ch = chars[offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = chars[offset];
        }

        char quote = ch;
        if (offset + 6 >= end
                || chars[offset + 1] != '$'
                || chars[offset + 2] != 'r'
                || chars[offset + 3] != 'e'
                || chars[offset + 4] != 'f'
                || chars[offset + 5] != quote
        ) {
            return false;
        }

        return isReference0(chars, offset, end, quote);
    }

    private boolean isReference0(char[] chars, int offset, int end, char quote) {
        char ch;
        offset += 6;
        ch = chars[offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = chars[offset];
        }

        if (ch != ':' || offset + 1 >= end) {
            return false;
        }

        ch = chars[++offset];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            offset++;
            if (offset >= end) {
                return false;
            }
            ch = chars[offset];
        }

        if (ch != quote
                || (offset + 1 < end && (ch = chars[offset + 1]) != '$' && ch != '.' && ch != '@')
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
        final char[] chars = this.chars;
        this.offset = referenceBegin;
        this.ch = chars[offset++];

        String reference = readString();

        char ch = this.ch;
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

        this.ch = ch;
        this.offset = offset;

        return reference;
    }

    @Override
    public final boolean nextIfMatch(char m) {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch != m) {
            return false;
        }

        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }
        this.offset = offset;
        this.ch = ch;
        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfComma() {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch != ',') {
            return false;
        }

        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }

        this.offset = offset;
        this.ch = ch;
        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfArrayStart() {
        char ch = this.ch;
        if (ch != '[') {
            return false;
        }

        char[] chars = this.chars;
        int offset = this.offset;
        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }

        this.ch = ch;
        this.offset = offset;
        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final boolean nextIfArrayEnd() {
        char ch = this.ch;
        if (ch != ']') {
            return false;
        }

        int offset = this.offset;
        char[] chars = this.chars;
        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : chars[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = ch;
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
        char[] chars = this.chars;
        if (first == 'n'
                && offset + 2 < end
                && chars[offset] == 'u'
                && chars[offset + 1] == 'l'
                && chars[offset + 2] == 'l'
        ) {
            offset += 3;
        } else if (first == '"' || first == '\'') {
            if (offset < end && chars[offset] == first) {
                offset++;
            } else if (offset + 4 < end
                    && IOUtils.isNULL(chars, offset)
                    && chars[offset + 4] == first
            ) {
                offset += 5;
            } else {
                return false;
            }
        } else {
            return false;
        }

        char ch = offset == end ? EOI : chars[offset++];

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1) {
        if (ch != c0) {
            return false;
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        if (offset + 1 > end || chars[offset] != c1) {
            return false;
        }

        offset += 1;
        char ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (offset == this.offset + 2 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2) {
        if (ch != c0) {
            return false;
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        if (offset + 2 > end || chars[offset] != c1 || chars[offset + 1] != c2) {
            return false;
        }

        offset += 2;
        char ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (offset == this.offset + 3 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3) {
        if (ch != c0) {
            return false;
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        if (offset + 3 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3) {
            return false;
        }

        offset += 3;
        char ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (offset == this.offset + 4 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4) {
        if (ch != c0) {
            return false;
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        if (offset + 4 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3
                || chars[offset + 3] != c4) {
            return false;
        }

        offset += 4;
        char ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (offset == this.offset + 5 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfMatchIdent(char c0, char c1, char c2, char c3, char c4, char c5) {
        if (ch != c0) {
            return false;
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        if (offset + 5 > end
                || chars[offset] != c1
                || chars[offset + 1] != c2
                || chars[offset + 2] != c3
                || chars[offset + 3] != c4
                || chars[offset + 4] != c5) {
            return false;
        }

        offset += 5;
        char ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (offset == this.offset + 6 && ch != EOI && ch != '(' && ch != '[' && ch != ']' && ch != ')' && ch != ':' && ch != ',') {
            return false;
        }

        this.offset = offset;
        this.ch = ch;
        return true;
    }

    @Override
    public final boolean nextIfSet() {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
        if (ch == 'S' && offset + 1 < end && chars[offset] == 'e' && chars[offset + 1] == 't') {
            offset += 2;
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
            this.offset = offset;
            this.ch = ch;
            return true;
        }
        return false;
    }

    @Override
    public final boolean nextIfInfinity() {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
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
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
            this.offset = offset;
            this.ch = ch;
            return true;
        }
        return false;
    }

    public final boolean nextIfObjectStart() {
        char ch = this.ch;
        if (ch != '{') {
            return false;
        }

        char[] chars = this.chars;
        int offset = this.offset;
        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }

        this.ch = ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    public final boolean nextIfObjectEnd() {
        char ch = this.ch;

        if (ch != '}') {
            return false;
        }

        int offset = this.offset;
        char[] chars = this.chars;
        ch = offset == end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : chars[offset++];
            while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
        }
        return true;
    }

    @Override
    public final void next() {
        int offset = this.offset;
        final char[] chars = this.chars;
        char ch = offset >= end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }
        this.offset = offset;
        this.ch = ch;
        if (ch == '/') {
            skipComment();
        }
    }

    @Override
    public final void nextWithoutComment() {
        int offset = this.offset;
        final char[] chars = this.chars;
        char ch = offset >= end ? EOI : chars[offset++];
        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
            ch = offset == end ? EOI : chars[offset++];
        }
        this.offset = offset;
        this.ch = ch;
    }

    @Override
    public final long readFieldNameHashCodeUnquote() {
        this.nameEscape = false;
        int offset = this.offset, end = this.end;
        final char[] chars = this.chars;
        char ch = this.ch;
        this.nameBegin = offset - 1;
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
                case '|':
                case '&':
                case EOI:
                    nameLength = i;
                    this.nameEnd = ch == EOI ? offset : offset - 1;
                    if (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                        ch = offset == end ? EOI : chars[offset++];
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
                        ch = (char) hexDigit4(chars, check3(offset, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        ch = char2(chars[offset], chars[offset + 1]);
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

            ch = offset == end ? EOI : chars[offset++];
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
                            ch = (char) hexDigit4(chars, check3(offset, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(chars[offset], chars[offset + 1]);
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
                    ch = offset == end ? EOI : chars[offset++];
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
                            ch = offset == end ? EOI : chars[offset++];
                        }
                        break _for;
                    default:
                        break;
                }

                hashCode ^= ch;
                hashCode *= Fnv.MAGIC_PRIME;

                ch = offset == end ? EOI : chars[offset++];
            }
        }

        if (ch == ':') {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.offset = offset;
        this.ch = ch;

        return hashCode;
    }

    @Override
    public final long readFieldNameHashCode() {
        final char[] chars = this.chars;
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

            String errorMsg;
            if (ch == '[' && nameBegin > 0) {
                errorMsg = "illegal fieldName input " + ch + ", previous fieldName " + getFieldName();
            } else {
                errorMsg = "illegal fieldName input" + ch;
            }

            throw new JSONException(info(errorMsg));
        }

        final char quote = ch;

        this.stringValue = null;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;

        long nameValue = 0;
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

        if (nameValue == 0) {
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
                            c = (char) hexDigit4(chars, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            c = char2(chars[offset + 1], chars[offset + 2]);
                            offset += 2;
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
                            c = (char) hexDigit4(chars, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            c = char2(chars[offset + 1], chars[offset + 2]);
                            offset += 2;
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

        char c = offset == end ? EOI : chars[offset++];
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            c = offset == end ? EOI : chars[offset++];
        }

        if (c != ':') {
            throw new JSONException(info("expect ':', but " + c));
        }

        c = offset == end ? EOI : chars[offset++];
        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            c = offset == end ? EOI : chars[offset++];
        }

        this.offset = offset;
        this.ch = c;

        return hashCode;
    }

    @Override
    public final long readValueHashCode() {
        final char quote = ch;
        if (quote != '"' && quote != '\'') {
            return -1;
        }

        final char[] chars = this.chars;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;

        long nameValue = 0;
        for (int i = 0; offset < end; offset++, i++) {
            char ch = chars[offset];

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
                ch = chars[++offset];
                switch (ch) {
                    case 'u': {
                        ch = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        ch = char2(chars[offset + 1], chars[offset + 2]);
                        offset += 2;
                        break;
                    }
                    case '\\':
                    case '"':
                    default:
                        ch = char1(ch);
                        break;
                }
            }

            if (ch > 0xFF || i >= 8 || (i == 0 && ch == 0)) {
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
                char c = chars[offset];
                if (c == '\\') {
                    nameEscape = true;
                    c = chars[++offset];
                    switch (c) {
                        case 'u': {
                            c = (char) hexDigit4(chars, check3(offset + 1, end));
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            c = char2(chars[offset + 1], chars[offset + 2]);
                            offset += 2;
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
    public final long getNameHashCodeLCase() {
        int offset = nameBegin;
        long nameValue = 0;
        final char[] chars = this.chars;
        for (int i = 0; offset < end; offset++) {
            char c = chars[offset];

            if (c == '\\') {
                c = chars[++offset];
                switch (c) {
                    case 'u': {
                        c = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(chars[offset + 1], chars[offset + 2]);
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

            if (c > 0xFF || i >= 8 || (i == 0 && c == 0)) {
                nameValue = 0;
                offset = this.nameBegin;
                break;
            }

            if (c == '_' || c == '-' || c == ' ') {
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

        long hashCode = Fnv.MAGIC_HASH_CODE;
        while (offset < end) {
            char c = chars[offset];

            if (c == '\\') {
                c = chars[++offset];
                switch (c) {
                    case 'u': {
                        c = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(chars[offset + 1], chars[offset + 2]);
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
    public final String getFieldName() {
        if (!nameEscape) {
            if (this.str != null) {
                return this.str.substring(nameBegin, nameEnd);
            } else {
                return new String(chars, nameBegin, nameEnd - nameBegin);
            }
        }

        char[] buf = new char[nameLength];
        final char[] chars = this.chars;
        int offset = nameBegin;
        for (int i = 0; offset < nameEnd; ++i) {
            char c = chars[offset];

            if (c == '\\') {
                c = chars[++offset];
                switch (c) {
                    case 'u': {
                        c = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(chars[offset + 1], chars[offset + 2]);
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
                    case ',':
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == '"') {
                break;
            }
            buf[i] = c;
            offset++;
        }

        return new String(buf);
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

        final char[] chars = this.chars;
        this.nameEscape = false;
        int offset = this.nameBegin = this.offset, end = this.end;
        final int nameBegin = this.nameBegin;
        for (int i = 0; offset < end; ++i) {
            int c = chars[offset];
            if (c == '\\') {
                nameEscape = true;
                c = chars[offset + 1];
                offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
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

                c = ++offset == end ? EOI : chars[offset];

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
                    return TypeUtils.toString(chars[nameBegin]);
                case 2:
                    return TypeUtils.toString(chars[nameBegin], chars[nameBegin + 1]);
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
                        nameValue0 = (c1 << 8)
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
                                = (c2 << 16)
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
                                = (c3 << 24)
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
                    long nameValue01 = nameValue0 ^ nameValue1;
                    int indexMask = ((int) (nameValue01 ^ (nameValue01 >>> 32))) & (NAME_CACHE2.length - 1);
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
                    } else if (entry.value0 == nameValue0 && entry.value1 == nameValue1) {
                        return entry.name;
                    }
                } else {
                    int indexMask = ((int) (nameValue0 ^ (nameValue0 >>> 32))) & (NAME_CACHE.length - 1);
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
    public final boolean skipName() {
        char quote = ch;
        if (quote == '\'' && ((context.features & Feature.DisableSingleQuote.mask) != 0)) {
            throw notSupportName();
        }

        if (quote != '"' && quote != '\'') {
            if ((context.features & Feature.AllowUnQuotedFieldNames.mask) != 0) {
                readFieldNameHashCodeUnquote();
                return true;
            }
            throw notSupportName();
        }

        int offset = this.offset;
        final char[] chars = this.chars;
        for (; ; ) {
            char ch = chars[offset++];
            if (ch == '\\') {
                ch = chars[offset];
                offset += (ch == 'u' ? 5 : (ch == 'x' ? 3 : 1));
                continue;
            }

            if (ch == quote) {
                ch = offset == end ? EOI : chars[offset++];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
                if (ch != ':') {
                    throw syntaxError(ch);
                }

                ch = offset == end ? EOI : chars[offset++];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }

                this.offset = offset;
                this.ch = ch;
                break;
            }
        }

        return true;
    }

    @Override
    public final int readInt32Value() {
        char ch = this.ch;
        int offset = this.offset, end = this.end;
        final char[] chars = this.chars;

        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];
        }
        int fc = ch;
        int result = ch >= '0' && ch <= '9'
                ? '0' - ch
                : ch == '-' || ch == '+'
                ? 0
                : 1;  // or any value > 0
        int d;
        while (offset + 1 < end
                && (d = IOUtils.digit2(chars, offset)) != -1
                && Integer.MIN_VALUE / 100 <= result & result <= 0) {
            result = result * 100 - d;  // overflow from d => result > 0
            offset += 2;
        }
        if (offset < end && IOUtils.isDigit(ch = chars[offset]) && Integer.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset++;
        }

        ch = offset == end ? EOI : chars[offset++];
        if (result <= 0 && (Integer.MIN_VALUE < result || fc == '-')
                && INT_VALUE_END[ch & 0xff]
                && (quote == 0 || ch == quote)
        ) {
            if (quote != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                ch = offset == end ? EOI : chars[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (comma = (ch == ',')) {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            this.ch = ch;
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

    public final long readInt64Value() {
        char ch = this.ch;
        int offset = this.offset, end = this.end;
        final char[] chars = this.chars;

        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];
        }
        int fc = ch;
        long result = ch >= '0' && ch <= '9'
                ? '0' - ch
                : ch == '-' || ch == '+'
                ? 0
                : 1;  // or any value > 0
        int d;
        while (offset + 1 < end
                && (d = IOUtils.digit2(chars, offset)) != -1
                && Long.MIN_VALUE / 100 <= result & result <= 0) {
            result = result * 100 - d;  // overflow from d => result > 0
            offset += 2;
        }
        if (offset < end && IOUtils.isDigit(ch = chars[offset]) && Long.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset++;
        }

        ch = offset == end ? EOI : chars[offset++];
        if (result <= 0 && (Long.MIN_VALUE < result || fc == '-')
                && INT_VALUE_END[ch & 0xff]
                && (quote == 0 || ch == quote)
        ) {
            if (quote != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                ch = offset == end ? EOI : chars[offset++];
            }

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (comma = (ch == ',')) {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            this.ch = ch;
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
    public double readDoubleValue() {
        double doubleValue = 0;

        final char[] chars = this.chars;
        int quote = '\0';
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = offset == end ? EOI : chars[offset++];
        }

        long result;
        boolean wasNull = false;
        if (quote != 0 && ch == quote) {
            ch = offset == end ? EOI : chars[offset++];
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
                    && (d = IOUtils.digit2(chars, offset)) != -1) {
                if (Long.MIN_VALUE / 100 <= result) {
                    result = result * 100 - d;  // overflow from d => result > 0
                    offset += 2;
                } else {
                    result = 1; // overflow
                }
            }
            if (result <= 0 && offset < end && IOUtils.isDigit(ch = chars[offset])) {
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
                    && chars[offset] == '.'
            ) {
                offset++;
                while (result <= 0
                        && offset + 1 < end
                        && (d = IOUtils.digit2(chars, offset)) != -1) {
                    if (Long.MIN_VALUE / 100 <= result) {
                        result = result * 100 - d;  // overflow from d => result > 0
                        offset += 2;
                        scale += 2;
                    } else {
                        result = 1; // overflow
                    }
                }
                if (result <= 0 && offset < end && IOUtils.isDigit(ch = chars[offset])) {
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
                ch = offset == end ? EOI : chars[offset++];
            }

            int expValue;
            if (result <= 0) {
                if (ch == 'e' || ch == 'E') {
                    boolean negativeExp;
                    ch = offset == end ? EOI : chars[offset++];
                    if ((negativeExp = (ch == '-')) || ch == '+') {
                        ch = offset == end ? EOI : chars[offset++];
                    } else if (ch == ',') {
                        throw numberError();
                    }
                    if (IOUtils.isDigit(ch)) {
                        expValue = ch - '0';
                        while (offset < end
                                && IOUtils.isDigit((ch = chars[offset]))
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
                        ch = offset == end ? EOI : chars[offset++];
                    } else {
                        result = 1; // invalid
                    }
                } else if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            if (result <= 0 && quote != 0) {
                if (ch == quote) {
                    ch = offset == end ? EOI : chars[offset++];
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
                    if (scale > -128 && scale < 128) {
                        doubleValue = TypeUtils.doubleValue(fc == '-' ? -1 : 1, Math.abs(result), scale);
                    } else {
                        result = 1; // invalid
                    }
                } else {
                    if (fc != '-' && doubleValue != 0) {
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
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.wasNull = wasNull;
        this.ch = (char) ch;
        this.offset = offset;
        return doubleValue;
    }

    @Override
    public float readFloatValue() {
        float floatValue = 0;

        final char[] chars = this.chars;
        int quote = '\0';
        int ch = this.ch;
        int offset = this.offset, end = this.end;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = offset == end ? EOI : chars[offset++];
        }

        long result;
        boolean wasNull = false;
        if (quote != 0 && ch == quote) {
            ch = offset == end ? EOI : chars[offset++];
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
                    && (d = IOUtils.digit2(chars, offset)) != -1) {
                if (Long.MIN_VALUE / 100 <= result) {
                    result = result * 100 - d;  // overflow from d => result > 0
                    offset += 2;
                } else {
                    result = 1; // overflow
                }
            }
            if (result <= 0 && offset < end && IOUtils.isDigit(ch = chars[offset])) {
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
                    && chars[offset] == '.'
            ) {
                offset++;
                while (result <= 0
                        && offset + 1 < end
                        && (d = IOUtils.digit2(chars, offset)) != -1) {
                    if (Long.MIN_VALUE / 100 <= result) {
                        result = result * 100 - d;  // overflow from d => result > 0
                        offset += 2;
                        scale += 2;
                    } else {
                        result = 1; // overflow
                    }
                }
                if (result <= 0 && offset < end && IOUtils.isDigit(ch = chars[offset])) {
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
                ch = offset == end ? EOI : chars[offset++];
            }

            int expValue;
            if (result <= 0) {
                if (ch == 'e' || ch == 'E') {
                    boolean negativeExp;
                    ch = offset == end ? EOI : chars[offset++];
                    if ((negativeExp = (ch == '-')) || ch == '+') {
                        ch = offset == end ? EOI : chars[offset++];
                    } else if (ch == ',') {
                        throw numberError();
                    }
                    if (IOUtils.isDigit(ch)) {
                        expValue = ch - '0';
                        while (offset < end
                                && IOUtils.isDigit((ch = chars[offset]))
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
                        ch = offset == end ? EOI : chars[offset++];
                    } else {
                        result = 1; // invalid
                    }
                } else if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            if (result <= 0 && quote != 0) {
                if (ch == quote) {
                    ch = offset == end ? EOI : chars[offset++];
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
                    if (scale > -128 && scale < 128) {
                        floatValue = TypeUtils.floatValue(fc == '-' ? -1 : 1, Math.abs(result), scale);
                    } else {
                        result = 1; // invalid
                    }
                } else {
                    if (fc != '-' && floatValue != 0) {
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
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.wasNull = wasNull;
        this.ch = (char) ch;
        this.offset = offset;
        return floatValue;
    }

    @Override
    public final String getString() {
        if (stringValue != null) {
            return stringValue;
        }

        int length = nameEnd - nameBegin;
        if (!nameEscape) {
            return new String(chars, nameBegin, length);
        }

        final char[] chars = this.chars;
        char[] buf = new char[nameLength];
        int offset = nameBegin;
        for (int i = 0; ; ++i) {
            char c = chars[offset];

            if (c == '\\') {
                c = chars[++offset];
                switch (c) {
                    case 'u': {
                        c = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(chars[offset + 1], chars[offset + 2]);
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
            buf[i] = c;
            offset++;
        }

        return stringValue = new String(buf);
    }

    protected final void readString0() {
        final char[] chars = this.chars;
        final char quote = this.ch;
        int offset = this.offset;
        int start = offset;
        int valueLength;
        valueEscape = false;

        for (int i = 0; ; ++i) {
            char c = chars[offset];
            if (c == '\\') {
                valueEscape = true;
                c = chars[offset + 1];
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
            char[] buf = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                char c = this.chars[offset];
                if (c == '\\') {
                    c = this.chars[++offset];
                    if (c == 'u') {
                        c = (char) hexDigit4(chars, check3(offset + 1, end));
                        offset += 4;
                    } else if (c == 'x') {
                        c = char2(chars[offset + 1], chars[offset + 2]);
                        offset += 2;
                    } else if (c != '\\' && c != '"') {
                        c = char1(c);
                    }
                } else if (c == '"') {
                    break;
                }
                buf[i] = c;
                offset++;
            }

            str = new String(buf);
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
        final char[] chars = this.chars;
        final char quote = ch;
        if (quote == '"' || quote == '\'') {
            final long byteVectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;

            int offset = this.offset;
            final int start = offset, end = this.end;
            int valueLength;
            boolean valueEscape = false;

            int upperBound = offset + ((end - offset) & ~7);
            {
                int i = 0;
                while (offset < upperBound) {
                    long v0 = getLongLE(chars, offset);
                    long v1 = getLongLE(chars, offset + 4);
                    if (((v0 | v1) & 0xFF00FF00FF00FF00L) != 0
                            || JSONReaderUTF8.containsSlashOrQuote((v0 << 8) | v1, byteVectorQuote)
                    ) {
                        break;
                    }

                    offset += 8;
                    i += 8;
                }

                for (; ; ++i) {
                    if (offset >= end) {
                        throw error("invalid escape character EOI");
                    }
                    char c = chars[offset];
                    if (c == '\\') {
                        valueEscape = true;
                        c = chars[offset + 1];
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
                char[] buf = new char[valueLength];
                offset = start;
                for (int i = 0; ; ++i) {
                    char c = chars[offset];
                    if (c == '\\') {
                        c = chars[++offset];
                        switch (c) {
                            case 'u': {
                                c = (char) hexDigit4(chars, check3(offset + 1, end));
                                offset += 4;
                                break;
                            }
                            case 'x': {
                                c = char2(chars[offset + 1], chars[offset + 2]);
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

                str = new String(buf);
            } else {
                char c0, c1;
                int strlen = offset - start;
                if (strlen == 1 && (c0 = chars[start]) < 128) {
                    str = TypeUtils.toString(c0);
                } else if (strlen == 2
                        && (c0 = chars[start]) < 128
                        && (c1 = chars[start + 1]) < 128
                ) {
                    str = TypeUtils.toString(c0, c1);
                } else if (this.str != null && (JVM_VERSION > 8 || ANDROID)) {
                    str = this.str.substring(start, offset);
                } else {
                    str = new String(chars, start, offset - start);
                }
            }

            long features = context.features;
            if ((features & (MASK_TRIM_STRING | MASK_EMPTY_STRING_AS_NULL)) != 0) {
                str = stringValue(str, features);
            }

            int ch = ++offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            if (comma = ch == ',') {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && (1L << ch & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }
            }

            this.ch = (char) ch;
            this.offset = offset;
            return str;
        }

        return readStringNotMatch();
    }

    @Override
    public final void skipValue() {
        final char[] chars = this.chars;
        char ch = this.ch;
        int offset = this.offset, end = this.end;
        comma = false;

        switch_:
        switch (ch) {
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
                        ch = chars[offset++];
                    } else {
                        throw numberError(offset, ch);
                    }
                }
                boolean dot = ch == '.';
                boolean num = false;
                if (!dot && (ch >= '0' && ch <= '9')) {
                    num = true;
                    do {
                        ch = offset == end ? EOI : chars[offset++];
                    } while (ch >= '0' && ch <= '9');
                }

                if (num && (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S')) {
                    ch = chars[offset++];
                }

                boolean small = false;
                if (ch == '.') {
                    small = true;
                    ch = offset == end ? EOI : chars[offset++];

                    if (ch >= '0' && ch <= '9') {
                        do {
                            ch = offset == end ? EOI : chars[offset++];
                        } while (ch >= '0' && ch <= '9');
                    }
                }

                if (!num && !small) {
                    throw numberError(offset, ch);
                }

                if (ch == 'e' || ch == 'E') {
                    ch = chars[offset++];

                    boolean eSign = false;
                    if (ch == '+' || ch == '-') {
                        eSign = true;
                        if (offset < end) {
                            ch = chars[offset++];
                        } else {
                            throw numberError(offset, ch);
                        }
                    }

                    if (ch >= '0' && ch <= '9') {
                        do {
                            ch = offset == end ? EOI : chars[offset++];
                        } while (ch >= '0' && ch <= '9');
                    } else if (eSign) {
                        throw numberError(offset, ch);
                    }
                }

                if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                    ch = offset == end ? EOI : chars[offset++];
                }
                break;
            case 't':
                if (offset + 3 > end) {
                    throw error(offset, ch);
                }
                if (chars[offset] != 'r' || chars[offset + 1] != 'u' || chars[offset + 2] != 'e') {
                    throw error(offset, ch);
                }
                offset += 3;
                ch = offset == end ? EOI : chars[offset++];
                break;
            case 'f':
                if (offset + 4 > end) {
                    throw error(offset, ch);
                }
                if (chars[offset] != 'a' || chars[offset + 1] != 'l' || chars[offset + 2] != 's' || chars[offset + 3] != 'e') {
                    throw error(offset, ch);
                }
                offset += 4;
                ch = offset == end ? EOI : chars[offset++];
                break;
            case 'n':
                if (offset + 3 > end) {
                    throw error(offset, ch);
                }
                if (chars[offset] != 'u' || chars[offset + 1] != 'l' || chars[offset + 2] != 'l') {
                    throw error(offset, ch);
                }
                offset += 3;
                ch = offset == end ? EOI : chars[offset++];
                break;
            case '"':
            case '\'': {
                char quote = ch;
                ch = chars[offset++];
                for (; ; ) {
                    if (ch == '\\') {
                        ch = chars[offset++];
                        if (ch == 'u') {
                            offset += 4;
                        } else if (ch == 'x') {
                            offset += 2;
                        } else if (ch != '\\' && ch != '"') {
                            char1(ch);
                        }
                        ch = chars[offset++];
                        continue;
                    }

                    if (ch == quote) {
                        ch = offset == end ? EOI : chars[offset++];
                        break;
                    }

                    ch = chars[offset++];
                }
                break;
            }
            default:
                if (ch == '[') {
                    next();
                    for (int i = 0; ; ++i) {
                        if (this.ch == ']') {
                            comma = false;
                            offset = this.offset;
                            ch = offset == end ? EOI : chars[offset++];
                            break switch_;
                        }
                        if (i != 0 && !comma) {
                            throw valueError();
                        }
                        comma = false;
                        skipValue();
                    }
                } else if (ch == '{') {
                    next();
                    for (; ; ) {
                        if (this.ch == '}') {
                            comma = false;
                            offset = this.offset;
                            ch = offset == end ? EOI : chars[offset++];
                            break switch_;
                        }
                        skipName();
                        skipValue();
                    }
                } else if (ch == 'S' && nextIfSet()) {
                    skipValue();
                } else {
                    throw error(offset, ch);
                }
                ch = this.ch;
                offset = this.offset;
                break;
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (ch == ',') {
            comma = true;
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        if (!comma && ch != '}' && ch != ']' && ch != EOI) {
            throw error(offset, ch);
        }

        if (comma && (ch == '}' || ch == ']' || ch == EOI)) {
            throw error(offset, ch);
        }

        this.ch = ch;
        this.offset = offset;
    }

    @Override
    public final void skipComment() {
        int offset = this.offset;
        if (offset + 1 >= this.end) {
            throw new JSONException(info());
        }

        final char[] chars = this.chars;
        char ch = chars[offset++];

        boolean multi;
        if (ch == '*') {
            multi = true;
        } else if (ch == '/') {
            multi = false;
        } else {
            throw new JSONException(info("parse comment error"));
        }

        ch = chars[offset++];

        while (true) {
            boolean endOfComment = false;
            if (multi) {
                if (ch == '*'
                        && offset <= end && chars[offset] == '/') {
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

                ch = chars[offset];

                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    offset++;
                    if (offset >= this.end) {
                        ch = EOI;
                        break;
                    }
                    ch = chars[offset];
                }

                offset++;
                break;
            }

            if (offset >= this.end) {
                ch = EOI;
                break;
            }
            ch = chars[offset++];
        }

        this.ch = ch;
        this.offset = offset;

        if (ch == '/') {
            skipComment();
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

        final int end = this.end;
        final char[] chars = this.chars;
        char ch = this.ch;
        int offset = this.offset;
        char quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];

            if (ch == quote) {
                ch = offset == end ? EOI : chars[offset++];
                while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                    ch = offset == end ? EOI : chars[offset++];
                }

                this.ch = ch;
                this.offset = offset;
                comma = nextIfComma();
                wasNull = true;
                valueType = JSON_TYPE_NULL;
                return;
            }
        }

        final int start = offset;

        final int multmin;
        if (ch == '-') {
            if (offset == end) {
                throw new JSONException(info("illegal input"));
            }
            multmin = -214748364; // limit / 10;
            negative = true;
            ch = chars[offset++];
        } else {
            if (ch == '+') {
                if (offset == end) {
                    throw new JSONException(info("illegal input"));
                }
                ch = chars[offset++];
            }
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
            ch = chars[offset++];
        }

        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = chars[offset++];
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
                ch = chars[offset++];
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
                if (chars[offset] == 'u'
                        && chars[offset + 1] == 'l'
                        && chars[offset + 2] == 'l'
                ) {
                    offset += 3;
                    valid = true;
                    wasNull = true;
                    valueType = JSON_TYPE_NULL;
                    ch = offset == end ? EOI : chars[offset++];
                }
            } else if (ch == 't' && chars[offset] == 'r' && chars[offset + 1] == 'u' && chars[offset + 2] == 'e') {
                offset += 3;
                valid = true;
                boolValue = true;
                valueType = JSON_TYPE_BOOL;
                ch = offset == end ? EOI : chars[offset++];
            } else if (ch == 'f' && offset + 3 < end && isALSE(chars, offset)) {
                valid = true;
                offset += 4;
                boolValue = false;
                valueType = JSON_TYPE_BOOL;
                ch = offset == end ? EOI : chars[offset++];
            } else if (ch == 'N' && chars[offset] == 'a' && chars[offset + 1] == 'N') {
                offset += 2;
                valid = true;
                boolValue = true;
                valueType = JSON_TYPE_NaN;
                ch = offset == end ? EOI : chars[offset++];
            } else if (ch == '{' && quote == 0) {
                this.offset = offset;
                this.ch = ch;
                this.complex = readObject();
                valueType = JSON_TYPE_OBJECT;
                return;
            } else if (ch == '[' && quote == 0) {
                this.offset = offset;
                this.ch = ch;
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

            ch = offset == end ? EOI : chars[offset++];
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
            ch = offset == end ? EOI : chars[offset++];
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        if (!valid) {
            throw new JSONException(info("illegal input error"));
        }

        this.offset = offset;
        this.ch = ch;
    }

    @Override
    public final boolean readIfNull() {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
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
            ch = offset == end ? EOI : chars[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = ch;
        this.offset = offset;
        return true;
    }

    @Override
    public final Date readNullOrNewDate() {
        Date date = null;
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch;
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
                ch = offset == end ? EOI : chars[offset++];
            }

            if (ch != '(' || offset >= end) {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }

            this.ch = ch;
            this.offset = offset;
            long millis = readInt64Value();
            ch = this.ch;
            offset = this.offset;

            if (ch != ')') {
                throw new JSONException("json syntax error, not match new Date" + offset);
            }
            ch = offset == end ? EOI : chars[offset++];

            date = new Date(millis);
        } else {
            throw new JSONException("json syntax error, not match null or new Date" + offset);
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = ch;
        this.offset = offset;

        return date;
    }

    @Override
    public final boolean isNull() {
        return ch == 'n' && offset < end && chars[offset] == 'u';
    }

    @Override
    public final boolean nextIfNull() {
        if (ch == 'n' && offset + 2 < end && chars[offset] == 'u') {
            this.readNull();
            return true;
        }
        return false;
    }

    @Override
    public final void readNull() {
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch;

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
            ch = offset == end ? EOI : chars[offset++];
        }
        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];

            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.ch = ch;
        this.offset = offset;
    }

    public final BigDecimal readBigDecimal() {
        boolean valid = false;
        final char[] chars = this.chars;
        char ch = this.ch;
        int offset = this.offset;
        boolean value = false;

        BigDecimal decimal = null;
        int quote = '\0';
        if (ch == '"' || ch == '\'') {
            quote = ch;
            ch = chars[offset++];

            if (ch == quote) {
                this.ch = offset == end ? EOI : chars[offset++];
                this.offset = offset;
                nextIfComma();
                return null;
            }
        }

        final int start = offset;
        if (ch == '-') {
            negative = true;
            ch = chars[offset++];
        } else {
            negative = false;
            if (ch == '+') {
                ch = chars[offset++];
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
            ch = chars[offset++];
        }

        if (longValue < 0) {
            overflow = true;
        }

        this.scale = 0;
        if (ch == '.') {
            valueType = JSON_TYPE_DEC;
            ch = chars[offset++];
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
                ch = chars[offset++];
            }
        }

        int expValue = 0;
        if (ch == 'e' || ch == 'E') {
            boolean negativeExp;
            ch = chars[offset++];
            if ((negativeExp = ch == '-') || ch == '+') {
                ch = chars[offset++];
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
                ch = chars[offset++];
            }

            if (negativeExp) {
                expValue = -expValue;
            }

            this.exponent = (short) expValue;
            valueType = JSON_TYPE_DEC;
        }

        if (offset == start) {
            if (ch == 'n' && chars[offset++] == 'u' && chars[offset++] == 'l' && chars[offset++] == 'l') {
                if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                    throw new JSONException(info("long value not support input null"));
                }

                wasNull = true;
                value = true;
                ch = offset == end ? EOI : chars[offset];
                offset++;
                valid = true;
            } else if (ch == 't' && offset + 3 <= end && chars[offset] == 'r' && chars[offset + 1] == 'u' && chars[offset + 2] == 'e') {
                valid = true;
                offset += 3;
                value = true;
                decimal = BigDecimal.ONE;
                ch = offset == end ? EOI : chars[offset];
                offset++;
            } else if (ch == 'f' && offset + 4 <= end
                    && chars[offset] == 'a'
                    && chars[offset + 1] == 'l'
                    && chars[offset + 2] == 's'
                    && chars[offset + 3] == 'e') {
                valid = true;
                offset += 4;
                decimal = BigDecimal.ZERO;
                value = true;
                ch = offset == end ? EOI : chars[offset];
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
                    throw new JSONException(info("cast to decimal error " + str), e);
                }
            } else {
                ch = offset >= end ? EOI : chars[offset++];
            }
        }
        if (!value) {
            if (expValue == 0 && !overflow && longValue != 0) {
                decimal = BigDecimal.valueOf(negative ? -longValue : longValue, scale);
                value = true;
            }

            if (!value) {
                decimal = TypeUtils.parseBigDecimal(chars, start - 1, len);
            }

            if (ch == 'L' || ch == 'F' || ch == 'D' || ch == 'B' || ch == 'S') {
                ch = offset >= end ? EOI : chars[offset++];
            }
        }

        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            // next inline
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        if (!valid) {
            throw new JSONException(info("illegal input error"));
        }

        this.ch = ch;
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
            throw new JSONException(info("syntax error, can not read uuid"));
        }
        final int quote = ch;
        final char[] chars = this.chars;
        int offset = this.offset;
        long msb1, msb2, msb3, msb4;
        long lsb1, lsb2, lsb3, lsb4;
        if (offset + 36 < end
                && chars[offset + 36] == quote
                && chars[offset + 8] == '-'
                && chars[offset + 13] == '-'
                && chars[offset + 18] == '-'
                && chars[offset + 23] == '-'
        ) {
            msb1 = parse4Nibbles(chars, offset);
            msb2 = parse4Nibbles(chars, offset + 4);
            msb3 = parse4Nibbles(chars, offset + 9);
            msb4 = parse4Nibbles(chars, offset + 14);
            lsb1 = parse4Nibbles(chars, offset + 19);
            lsb2 = parse4Nibbles(chars, offset + 24);
            lsb3 = parse4Nibbles(chars, offset + 28);
            lsb4 = parse4Nibbles(chars, offset + 32);
            offset += 37;
        } else if (offset + 32 < end && chars[offset + 32] == quote) {
            msb1 = parse4Nibbles(chars, offset);
            msb2 = parse4Nibbles(chars, offset + 4);
            msb3 = parse4Nibbles(chars, offset + 8);
            msb4 = parse4Nibbles(chars, offset + 12);
            lsb1 = parse4Nibbles(chars, offset + 16);
            lsb2 = parse4Nibbles(chars, offset + 20);
            lsb3 = parse4Nibbles(chars, offset + 24);
            lsb4 = parse4Nibbles(chars, offset + 28);
            offset += 33;
        } else {
            String str = readString();
            if (str.isEmpty()) {
                return null;
            }
            return UUID.fromString(str);
        }

        ch = offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        this.offset = offset;
        if (comma = (ch == ',')) {
            next();
        } else {
            this.ch = (char) ch;
        }

        return new UUID(
                msb1 << 48 | msb2 << 32 | msb3 << 16 | msb4,
                lsb1 << 48 | lsb2 << 32 | lsb3 << 16 | lsb4);
    }

    private static long parse4Nibbles(char[] chars, int offset) {
        byte[] ns = NIBBLES;
        char ch1 = chars[offset];
        char ch2 = chars[offset + 1];
        char ch3 = chars[offset + 2];
        char ch4 = chars[offset + 3];
        return (ch1 | ch2 | ch3 | ch4) > 0xff ?
                -1 : ns[ch1] << 12 | ns[ch2] << 8 | ns[ch3] << 4 | ns[ch4];
    }

    @Override
    public final int getStringLength() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("string length only support string input");
        }
        final char quote = ch;

        int len = 0;
        int i = offset;
        char[] chars = this.chars;

        final int i8 = i + 8;
        if (i8 < end && i8 < chars.length) {
            if (chars[i] != quote
                    && chars[i + 1] != quote
                    && chars[i + 2] != quote
                    && chars[i + 3] != quote
                    && chars[i + 4] != quote
                    && chars[i + 5] != quote
                    && chars[i + 6] != quote
                    && chars[i + 7] != quote
            ) {
                i += 8;
                len += 8;
            }
        }

        for (; i < end; ++i, ++len) {
            if (chars[i] == quote) {
                break;
            }
        }
        return len;
    }

    @Override
    protected final LocalDateTime readLocalDateTime14() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime14(chars, offset);
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
    protected final LocalDateTime readLocalDateTime12() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime12(chars, offset);
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
    protected final LocalDateTime readLocalDateTime16() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime16(chars, offset);
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
    protected final LocalDateTime readLocalDateTime17() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime17(chars, offset);
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
    protected final LocalDateTime readLocalDateTime18() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime18(chars, offset);

        offset += 19;
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

        LocalTime time = DateUtils.parseLocalTime5(chars, offset);
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

        LocalTime time = DateUtils.parseLocalTime6(chars, offset);
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

        LocalTime time = DateUtils.parseLocalTime7(chars, offset);
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

        LocalTime time = DateUtils.parseLocalTime8(chars, offset);
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

        LocalTime time = DateUtils.parseLocalTime8(chars, offset);
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
    public final LocalDate readLocalDate8() {
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate8(chars, offset);
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
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate9(chars, offset);
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
        if (ch != '"' && ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt;
        try {
            ldt = DateUtils.parseLocalDate10(chars, offset);
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
    public final LocalDate readLocalDate11() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localDate only support string input");
        }

        LocalDate ldt = DateUtils.parseLocalDate11(chars, offset);
        if (ldt == null) {
            return null;
        }

        offset += 12;
        next();
        if (comma = (ch == ',')) {
            next();
        }
        return ldt;
    }

    public final LocalDate readLocalDate() {
        final char[] chars = this.chars;
        int offset = this.offset;
        if (ch == '"' || ch == '\'') {
            if (!context.formatComplex) {
                char quote = ch;
                int c10 = offset + 10;
                if (c10 < chars.length
                        && c10 < end
                        && chars[offset + 4] == '-'
                        && chars[offset + 7] == '-'
                        && chars[offset + 10] == quote
                ) {
                    int year = IOUtils.digit4(chars, offset);
                    int month = IOUtils.digit2(chars, offset + 5);
                    int dom = IOUtils.digit2(chars, offset + 8);

                    if ((year | month | dom) < 0) {
                        throw new JSONException(info("read date error"));
                    }

                    LocalDate ldt;
                    try {
                        ldt = (year | month | dom) == 0
                                ? null
                                : LocalDate.of(year, month, dom);
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

                LocalDate localDate = readLocalDate0(offset, chars, quote);
                if (localDate != null) {
                    return localDate;
                }
            }
        }
        return super.readLocalDate();
    }

    private LocalDate readLocalDate0(int offset, char[] chars, char quote) {
        int nextQuoteOffset = -1;
        for (int i = offset, end = Math.min(i + 17, this.end); i < end; ++i) {
            if (chars[i] == quote) {
                nextQuoteOffset = i;
            }
        }
        if (nextQuoteOffset != -1
                && nextQuoteOffset - offset > 10
                && chars[nextQuoteOffset - 6] == '-'
                && chars[nextQuoteOffset - 3] == '-'
        ) {
            int year = TypeUtils.parseInt(chars, offset, nextQuoteOffset - offset - 6);
            int month = IOUtils.digit2(chars, nextQuoteOffset - 5);
            int dayOfMonth = IOUtils.digit2(chars, nextQuoteOffset - 2);
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
        final char[] chars = this.chars;
        int offset = this.offset, end = this.end;
        char quote = this.ch;
        if (quote == '"' || quote == '\'') {
            if (!context.formatComplex) {
                char c10;
                int off21 = offset + 19;
                if (off21 < chars.length
                        && off21 < end
                        && chars[offset + 4] == '-'
                        && chars[offset + 7] == '-'
                        && ((c10 = chars[offset + 10]) == ' ' || c10 == 'T')
                        && chars[offset + 13] == ':'
                        && chars[offset + 16] == ':'
                ) {
                    int year = IOUtils.digit4(chars, offset);
                    int month = IOUtils.digit2(chars, offset + 5);
                    int dom = IOUtils.digit2(chars, offset + 8);
                    int hour = IOUtils.digit2(chars, offset + 11);
                    int minute = IOUtils.digit2(chars, offset + 14);
                    int second = IOUtils.digit2(chars, offset + 17);
                    if ((year | month | dom | minute | second) < 0) {
                        ZonedDateTime zdt = readZonedDateTime();
                        return zdt == null ? null : zdt.toOffsetDateTime();
                    }

                    int nanos = 0, nanoSize = 0;
                    offset += 19;
                    char ch = chars[offset++];
                    if (ch == '.') {
                        ch = chars[offset++];
                    }
                    while (ch >= '0' && ch <= '9') {
                        nanos = nanos * 10 + (ch - '0');
                        nanoSize++;
                        if (offset < end) {
                            ch = chars[offset++];
                        } else {
                            break;
                        }
                    }
                    if (nanoSize != 0) {
                        nanos = DateUtils.nanos(nanos, nanoSize);
                    }
                    ZoneOffset zoneOffset = ZoneOffset.UTC;
                    if (ch == 'Z') {
                        ch = chars[offset++];
                    } else if (ch != quote) {
                        int quoteIndex = IOUtils.indexOfChar(chars, '"', offset, end);
                        if (quoteIndex != -1) {
                            zoneOffset = DateUtils.zoneOffset(chars, offset - 1, quoteIndex - offset + 1);
                            offset = quoteIndex + 1;
                            ch = quote;
                        }
                    }
                    if (ch == quote) {
                        ch = offset >= end ? EOI : chars[offset++];
                        while (ch == '\0' || (ch <= ' ' && ((1L << ch) & SPACE) != 0)) {
                            ch = offset == end ? EOI : chars[offset++];
                        }
                        if (comma = (ch == ',')) {
                            ch = offset == end ? EOI : chars[offset++];
                            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                                ch = offset == end ? EOI : chars[offset++];
                            }
                        }
                        this.offset = offset;
                        this.ch = ch;
                        return OffsetDateTime.of(year, month, dom, hour, minute, second, nanos, zoneOffset);
                    }
                }
            }
        }
        ZonedDateTime zdt = readZonedDateTime();
        return zdt == null ? null : zdt.toOffsetDateTime();
    }

    @Override
    public final OffsetTime readOffsetTime() {
        final char[] chars = this.chars;
        final int offset = this.offset;
        final Context context = this.context;
        if (this.ch == '"' || this.ch == '\'') {
            if (context.dateFormat == null) {
                char quote = this.ch;
                int off10 = offset + 8;
                if (off10 < chars.length
                        && off10 < end
                        && chars[offset + 2] == ':'
                        && chars[offset + 5] == ':'
                ) {
                    char h0 = chars[offset];
                    char h1 = chars[offset + 1];
                    char i0 = chars[offset + 3];
                    char i1 = chars[offset + 4];
                    char s0 = chars[offset + 6];
                    char s1 = chars[offset + 7];

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
                    for (int start = offset + 8, i = start, end = offset + 25; i < end && i < this.end && i < chars.length; ++i) {
                        char b = chars[i];
                        if (nanoSize == -1 && (b == 'Z' || b == '+' || b == '-')) {
                            nanoSize = i - start - 1;
                        }
                        if (b == quote) {
                            len = i - offset;
                            break;
                        }
                    }

                    int nano = nanoSize <= 0 ? 0 : DateUtils.readNanos(chars, nanoSize, offset + 9);

                    ZoneOffset zoneOffset;
                    int zoneOffsetSize = len - 9 - nanoSize;
                    if (zoneOffsetSize <= 1) {
                        zoneOffset = ZoneOffset.UTC;
                    } else {
                        String zonedId = new String(chars, offset + 9 + nanoSize, zoneOffsetSize);
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        if (len < 19) {
            return null;
        }

        final char[] chars = this.chars;
        ZonedDateTime zdt;
        if (len == 30 && chars[offset + 29] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime29(chars, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 29 && chars[offset + 28] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime28(chars, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 28 && chars[offset + 27] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime27(chars, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else if (len == 27 && chars[offset + 26] == 'Z') {
            LocalDateTime ldt = DateUtils.parseLocalDateTime26(chars, offset);
            zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);
        } else {
            zdt = DateUtils.parseZonedDateTime(chars, offset, len, context.zoneId);
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
    protected final LocalDateTime readLocalDateTime19() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime19(chars, offset);
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt = DateUtils.parseLocalDateTime20(chars, offset);
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

        long millis = DateUtils.parseMillis19(chars, offset, context.zoneId);

        if (chars[offset + 19] != quote) {
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("date only support string input");
        }

        LocalDateTime ldt;
        if (chars[offset + len - 1] == 'Z') {
            ZonedDateTime zdt = DateUtils.parseZonedDateTime(chars, offset, len);
            ldt = zdt.toInstant().atZone(context.getZoneId()).toLocalDateTime();
        } else {
            ldt = DateUtils.parseLocalDateTimeX(chars, offset, len);
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

    @Override
    protected final LocalTime readLocalTime10() {
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime10(chars, offset);
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime11(chars, offset);
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime12(chars, offset);
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
        if (this.ch != '"' && this.ch != '\'') {
            throw new JSONException("localTime only support string input");
        }

        LocalTime time = DateUtils.parseLocalTime18(chars, offset);
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
    public final String readPattern() {
        if (ch != '/') {
            throw new JSONException("illegal pattern");
        }

        final char[] chars = this.chars;
        int offset = this.offset;
        int start = offset;
        for (; offset < end; ++offset) {
            if (chars[offset] == '/') {
                break;
            }
        }
        String str = new String(chars, start, offset - start);
        char ch = ++offset == end ? EOI : chars[offset++];
        while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
            ch = offset == end ? EOI : chars[offset++];
        }

        if (comma = (ch == ',')) {
            ch = offset == end ? EOI : chars[offset++];
            while (ch <= ' ' && ((1L << ch) & SPACE) != 0) {
                ch = offset == end ? EOI : chars[offset++];
            }
        }

        this.offset = offset;
        this.ch = ch;

        return str;
    }

    public final boolean readBoolValue() {
        wasNull = false;
        boolean val;
        final char[] chars = this.chars;
        int offset = this.offset;
        char ch = this.ch;
        if (ch == 't'
                && offset + 2 < chars.length
                && chars[offset] == 'r'
                && chars[offset + 1] == 'u'
                && chars[offset + 2] == 'e'
        ) {
            offset += 3;
            val = true;
        } else if (ch == 'f' && offset + 3 < end && isALSE(chars, offset)) {
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
        } else if (ch == 'n' && offset + 2 < chars.length
                && chars[offset] == 'u'
                && chars[offset + 1] == 'l'
                && chars[offset + 2] == 'l'
        ) {
            if ((context.features & Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(info("boolean value not support input null"));
            }

            wasNull = true;
            offset += 3;
            val = false;
        } else if (ch == '"' || ch == '\'') {
            if (offset + 1 < chars.length
                    && chars[offset + 1] == ch
            ) {
                char c0 = chars[offset];
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
                throw new JSONException("not closed square brackets, expect ] but found : " + ch);
            }
            return val;
        } else {
            throw new JSONException("syntax error : " + ch);
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
        this.offset = offset;
        this.ch = ch;

        return val;
    }

    @Override
    public String info(String message) {
        int line = 1, column = 0;
        for (int i = 0; i < offset & i < end; i++, column++) {
            if (chars[i] == '\n') {
                column = 0;
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
        buf.append(chars, this.start, Math.min(length, MAX_OUTPUT_LENGTH));

        return buf.toString();
    }

    @Override
    public final void close() {
        if (cacheIndex != -1 && chars.length < CACHE_THRESHOLD) {
            final CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
            CHARS_UPDATER.lazySet(cacheItem, chars);
        }

        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    public final int getRawInt() {
        if (offset + 3 < chars.length) {
            return getInt(chars, offset - 1);
        }

        return 0;
    }

    static int getInt(char[] chars, int offset) {
        long int64Val = IOUtils.getLongUnaligned(chars, offset);

        if ((int64Val & CHAR_MASK) != 0) {
            return 0;
        }

        if (BIG_ENDIAN) {
            int64Val >>= 8;
        }

        return (int) ((int64Val & 0xff)
                | ((int64Val & 0xff_0000) >> 8)
                | ((int64Val & 0xff_0000_0000L) >> 16)
                | ((int64Val & 0xff_0000_0000_0000L) >> 24));
    }

    public final long getRawLong() {
        if (offset + 7 < chars.length) {
            return getLong(chars, offset - 1);
        }

        return 0;
    }

    static long getLong(char[] chars, int offset) {
        long int64Val0 = IOUtils.getLongUnaligned(chars, offset);
        long int64Val1 = IOUtils.getLongUnaligned(chars, offset + 4);

        if (((int64Val0 | int64Val1) & CHAR_MASK) != 0) {
            return 0;
        }

        if (BIG_ENDIAN) {
            int64Val0 >>= 8;
            int64Val1 >>= 8;
        }

        return (int64Val0 & 0xff)
                | ((int64Val0 & 0xff_0000) >> 8)
                | ((int64Val0 & 0xff_0000_0000L) >> 16)
                | ((int64Val0 & 0xff_0000_0000_0000L) >> 24)
                | ((int64Val1 & 0xff) << 32)
                | ((int64Val1 & 0xff_0000L) << 24)
                | ((int64Val1 & 0xff_0000_0000L) << 16)
                | ((int64Val1 & 0xff_0000_0000_0000L) << 8);
    }

    @Override
    public final boolean nextIfName8Match0() {
        char[] chars = this.chars;
        int offset = this.offset;
        offset += 7;

        if (offset == end) {
            this.ch = EOI;
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;
        return true;
    }

    @Override
    public final boolean nextIfName8Match1() {
        char[] chars = this.chars;
        int offset = this.offset;
        offset += 8;

        if (offset >= end || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName8Match2() {
        char[] chars = this.chars;
        int offset = this.offset;
        offset += 9;

        if (offset >= end || chars[offset - 2] != '"' || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match2() {
        char[] chars = this.chars;
        int offset = this.offset + 4;
        if (offset >= end || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match3() {
        char[] chars = this.chars;
        int offset = this.offset + 5;
        if (offset >= end || chars[offset - 2] != '"' || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset]) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match4(byte c4) {
        char[] chars = this.chars;
        int offset = this.offset + 6;
        if (offset >= end || chars[offset - 3] != c4 || chars[offset - 2] != '"' || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset]) <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match5(int name1) {
        char[] chars = this.chars;
        int offset = this.offset + 7;
        if (offset >= end || getInt(chars, offset - 4) != name1) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match6(int name1) {
        char[] chars = this.chars;
        int offset = this.offset + 8;
        if (offset >= end || getInt(chars, offset - 5) != name1 || chars[offset - 1] != ':') {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match7(int name1) {
        char[] chars = this.chars;
        int offset = this.offset + 9;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 6) != name1
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match8(int name1, byte c8) {
        char[] chars = this.chars;
        int offset = this.offset + 10;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 7) != name1
                || chars[offset - 3] != c8
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match9(long name1) {
        char[] chars = this.chars;
        int offset = this.offset + 11;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 8) != name1) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match10(long name1) {
        char[] chars = this.chars;
        int offset = this.offset + 12;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 9) != name1
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match11(long name1) {
        char[] chars = this.chars;
        int offset = this.offset + 13;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 10) != name1
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match12(long name1, byte name2) {
        char[] chars = this.chars;
        int offset = this.offset + 14;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 11) != name1
                || chars[offset - 3] != name2
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match13(long name1, int name2) {
        char[] chars = this.chars;
        int offset = this.offset + 15;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 12) != name1
                || getInt(chars, offset - 4) != name2
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match14(long name1, int name2) {
        char[] chars = this.chars;
        int offset = this.offset + 16;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 13) != name1
                || getInt(chars, offset - 5) != name2
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match15(long name1, int name2) {
        char[] chars = this.chars;
        int offset = this.offset + 17;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 14) != name1
                || getInt(chars, offset - 6) != name2
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match16(long name1, int name2, byte name3) {
        char[] chars = this.chars;
        int offset = this.offset + 18;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 15) != name1
                || getInt(chars, offset - 7) != name2
                || chars[offset - 3] != name3
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match17(long name1, long name2) {
        char[] chars = this.chars;
        int offset = this.offset + 19;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 16) != name1
                || getLong(chars, offset - 8) != name2
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match18(long name1, long name2) {
        char[] chars = this.chars;
        int offset = this.offset + 20;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 17) != name1
                || getLong(chars, offset - 9) != name2
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match19(long name1, long name2) {
        char[] chars = this.chars;
        int offset = this.offset + 21;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 18) != name1
                || getLong(chars, offset - 10) != name2
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match20(long name1, long name2, byte name3) {
        char[] chars = this.chars;
        int offset = this.offset + 22;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 19) != name1
                || getLong(chars, offset - 11) != name2
                || chars[offset - 3] != name3
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match21(long name1, long name2, int name3) {
        char[] chars = this.chars;
        int offset = this.offset + 23;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 20) != name1
                || getLong(chars, offset - 12) != name2
                || getInt(chars, offset - 4) != name3
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match22(long name1, long name2, int name3) {
        char[] chars = this.chars;
        int offset = this.offset + 24;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 21) != name1
                || getLong(chars, offset - 13) != name2
                || getInt(chars, offset - 5) != name3
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match23(long name1, long name2, int name3) {
        char[] chars = this.chars;
        int offset = this.offset + 25;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 22) != name1
                || getLong(chars, offset - 14) != name2
                || getInt(chars, offset - 6) != name3
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match24(long name1, long name2, int name3, byte name4) {
        char[] chars = this.chars;
        int offset = this.offset + 26;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 23) != name1
                || getLong(chars, offset - 15) != name2
                || getInt(chars, offset - 7) != name3
                || chars[offset - 3] != name4
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match25(long name1, long name2, long name3) {
        char[] chars = this.chars;
        int offset = this.offset + 27;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 24) != name1
                || getLong(chars, offset - 16) != name2
                || getLong(chars, offset - 8) != name3
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match26(long name1, long name2, long name3) {
        char[] bytes = this.chars;
        int offset = this.offset + 28;
        if (offset >= end) {
            return false;
        }

        if (getLong(bytes, offset - 25) != name1
                || getLong(bytes, offset - 17) != name2
                || getLong(bytes, offset - 9) != name3
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match27(long name1, long name2, long name3) {
        char[] bytes = this.chars;
        int offset = this.offset + 29;
        if (offset >= end) {
            return false;
        }

        if (getLong(bytes, offset - 26) != name1
                || getLong(bytes, offset - 18) != name2
                || getLong(bytes, offset - 10) != name3
                || bytes[offset - 2] != '"'
                || bytes[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match28(long name1, long name2, long name3, byte c29) {
        char[] chars = this.chars;
        int offset = this.offset + 30;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 27) != name1
                || getLong(chars, offset - 19) != name2
                || getLong(chars, offset - 11) != name3
                || chars[offset - 3] != c29
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match29(long name1, long name2, long name3, int name4) {
        char[] chars = this.chars;
        int offset = this.offset + 31;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 28) != name1
                || getLong(chars, offset - 20) != name2
                || getLong(chars, offset - 12) != name3
                || getInt(chars, offset - 4) != name4
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match30(long name1, long name2, long name3, int name4) {
        char[] chars = this.chars;
        int offset = this.offset + 32;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 29) != name1
                || getLong(chars, offset - 21) != name2
                || getLong(chars, offset - 13) != name3
                || getInt(chars, offset - 5) != name4
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match31(long name1, long name2, long name3, int name4) {
        char[] chars = this.chars;
        int offset = this.offset + 33;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 30) != name1
                || getLong(chars, offset - 22) != name2
                || getLong(chars, offset - 14) != name3
                || getInt(chars, offset - 6) != name4
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match32(long name1, long name2, long name3, int name4, byte c32) {
        char[] chars = this.chars;
        int offset = this.offset + 34;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 31) != name1
                || getLong(chars, offset - 23) != name2
                || getLong(chars, offset - 15) != name3
                || getInt(chars, offset - 7) != name4
                || chars[offset - 3] != c32
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match33(long name1, long name2, long name3, long name4) {
        char[] chars = this.chars;
        int offset = this.offset + 35;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 32) != name1
                || getLong(chars, offset - 24) != name2
                || getLong(chars, offset - 16) != name3
                || getLong(chars, offset - 8) != name4
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match34(long name1, long name2, long name3, long name4) {
        char[] chars = this.chars;
        int offset = this.offset + 36;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 33) != name1
                || getLong(chars, offset - 25) != name2
                || getLong(chars, offset - 17) != name3
                || getLong(chars, offset - 9) != name4
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match35(long name1, long name2, long name3, long name4) {
        char[] chars = this.chars;
        int offset = this.offset + 37;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 34) != name1
                || getLong(chars, offset - 26) != name2
                || getLong(chars, offset - 18) != name3
                || getLong(chars, offset - 10) != name4
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match36(long name1, long name2, long name3, long name4, byte c36) {
        char[] chars = this.chars;
        int offset = this.offset + 38;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 35) != name1
                || getLong(chars, offset - 27) != name2
                || getLong(chars, offset - 19) != name3
                || getLong(chars, offset - 11) != name4
                || chars[offset - 3] != c36
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match37(long name1, long name2, long name3, long name4, int name5) {
        char[] chars = this.chars;
        int offset = this.offset + 39;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 36) != name1
                || getLong(chars, offset - 28) != name2
                || getLong(chars, offset - 20) != name3
                || getLong(chars, offset - 12) != name4
                || getInt(chars, offset - 4) != name5
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match38(long name1, long name2, long name3, long name4, int name5) {
        char[] chars = this.chars;
        int offset = this.offset + 40;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 37) != name1
                || getLong(chars, offset - 29) != name2
                || getLong(chars, offset - 21) != name3
                || getLong(chars, offset - 13) != name4
                || getInt(chars, offset - 5) != name5
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match39(long name1, long name2, long name3, long name4, int name5) {
        char[] chars = this.chars;
        int offset = this.offset + 41;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 38) != name1
                || getLong(chars, offset - 30) != name2
                || getLong(chars, offset - 22) != name3
                || getLong(chars, offset - 14) != name4
                || getInt(chars, offset - 6) != name5
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match40(long name1, long name2, long name3, long name4, int name5, byte c40) {
        final char[] chars = this.chars;
        int offset = this.offset + 42;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 39) != name1
                || getLong(chars, offset - 31) != name2
                || getLong(chars, offset - 23) != name3
                || getLong(chars, offset - 15) != name4
                || getInt(chars, offset - 7) != name5
                || chars[offset - 3] != c40
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match41(long name1, long name2, long name3, long name4, long name5) {
        final char[] chars = this.chars;
        int offset = this.offset + 43;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 40) != name1
                || getLong(chars, offset - 32) != name2
                || getLong(chars, offset - 24) != name3
                || getLong(chars, offset - 16) != name4
                || getLong(chars, offset - 8) != name5
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match42(long name1, long name2, long name3, long name4, long name5) {
        char[] chars = this.chars;
        int offset = this.offset + 44;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 41) != name1
                || getLong(chars, offset - 33) != name2
                || getLong(chars, offset - 25) != name3
                || getLong(chars, offset - 17) != name4
                || getLong(chars, offset - 9) != name5
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfName4Match43(long name1, long name2, long name3, long name4, long name5) {
        char[] chars = this.chars;
        int offset = this.offset + 45;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 42) != name1
                || getLong(chars, offset - 34) != name2
                || getLong(chars, offset - 26) != name3
                || getLong(chars, offset - 18) != name4
                || getLong(chars, offset - 10) != name5
                || chars[offset - 2] != '"'
                || chars[offset - 1] != ':'
        ) {
            return false;
        }

        char c;
        while ((c = chars[offset++]) <= ' ' && ((1L << c) & SPACE) != 0) {
            // empty loop
        }

        this.offset = offset;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match2() {
        char[] chars = this.chars;
        int offset = this.offset + 3;
        if (offset >= end) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match3() {
        char[] chars = this.chars;
        int offset = this.offset + 4;
        if (offset >= end) {
            return false;
        }

        if (chars[offset - 1] != '"') {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match4(byte c4) {
        char[] chars = this.chars;
        int offset = this.offset + 5;
        if (offset >= end) {
            return false;
        }

        if (chars[offset - 2] != c4 || chars[offset - 1] != '"') {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match5(byte c4, byte c5) {
        char[] chars = this.chars;
        int offset = this.offset + 6;
        if (offset >= end) {
            return false;
        }

        if (chars[offset - 3] != c4
                || chars[offset - 2] != c5
                || chars[offset - 1] != '"'
        ) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match6(int name1) {
        char[] chars = this.chars;
        int offset = this.offset + 7;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 4) != name1) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match7(int name1) {
        char[] chars = this.chars;
        int offset = this.offset + 8;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 5) != name1
                || chars[offset - 1] != '"'
        ) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match8(int name1, byte c8) {
        char[] chars = this.chars;
        int offset = this.offset + 9;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 6) != name1
                || chars[offset - 2] != c8
                || chars[offset - 1] != '"'
        ) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match9(int name1, byte c8, byte c9) {
        char[] chars = this.chars;
        int offset = this.offset + 10;
        if (offset >= end) {
            return false;
        }

        if (getInt(chars, offset - 7) != name1
                || chars[offset - 3] != c8
                || chars[offset - 2] != c9
                || chars[offset - 1] != '"'
        ) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match10(long name1) {
        char[] chars = this.chars;
        int offset = this.offset + 11;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 8) != name1) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }

    @Override
    public final boolean nextIfValue4Match11(long name1) {
        char[] chars = this.chars;
        int offset = this.offset + 12;
        if (offset >= end) {
            return false;
        }

        if (getLong(chars, offset - 9) != name1
                || chars[offset - 1] != '"'
        ) {
            return false;
        }

        char c = chars[offset];
        if (c != ',' && c != '}' && c != ']') {
            return false;
        }

        if (c == ',') {
            comma = true;
            offset++;
            c = offset == end ? EOI : chars[offset];
        }

        while (c <= ' ' && ((1L << c) & SPACE) != 0) {
            offset++;
            c = chars[offset];
        }

        this.offset = offset + 1;
        this.ch = c;

        return true;
    }
}

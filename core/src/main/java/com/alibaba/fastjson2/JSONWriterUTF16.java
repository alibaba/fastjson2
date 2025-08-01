package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.NumberUtils;
import com.alibaba.fastjson2.util.StringUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

class JSONWriterUTF16
        extends JSONWriter {
    static final long BYTE_VEC_64_SINGLE_QUOTE = 0x00270027_00270027L;
    static final long BYTE_VEC_64_DOUBLE_QUOTE = 0x00220022_00220022L;

    static final long REF_0, REF_1;
    static final int QUOTE2_COLON, QUOTE_COLON;
    static final int[] HEX256;
    static {
        int[] digits = new int[16 * 16];

        for (int i = 0; i < 16; i++) {
            int hi = (short) (i < 10 ? i + '0' : i - 10 + 'a');

            for (int j = 0; j < 16; j++) {
                int lo = (short) (j < 10 ? j + '0' : j - 10 + 'a');
                digits[(i << 4) + j] = (hi | (lo << 16));
            }
        }

        if (BIG_ENDIAN) {
            for (int i = 0; i < digits.length; i++) {
                digits[i] = Integer.reverseBytes(digits[i] << 8);
            }
        }

        HEX256 = digits;

        // char[] chars = new char[] {'\"', ':'};
        char[] chars = {'{', '"', '$', 'r', 'e', 'f', '"', ':'};
        REF_0 = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET);
        REF_1 = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET + 8);
        QUOTE2_COLON = UNSAFE.getInt(chars, ARRAY_CHAR_BASE_OFFSET + 12);
        chars[6] = '\'';
        QUOTE_COLON = UNSAFE.getInt(chars, ARRAY_CHAR_BASE_OFFSET + 12);
    }

    protected char[] chars;
    final CacheItem cacheItem;
    protected final long byteVectorQuote;

    JSONWriterUTF16(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_16);
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        char[] chars = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (chars == null) {
            chars = new char[8192];
        }
        this.chars = chars;
        this.byteVectorQuote = this.useSingleQuote ? ~0x2727_2727_2727_2727L : ~0x2222_2222_2222_2222L;
    }

    public final void writeNull() {
        int off = this.off;
        int minCapacity = off + 4;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        putNULL(chars, off);
        this.off = off + 4;
    }

    @Override
    public final void flushTo(java.io.Writer to) {
        try {
            int off = this.off;
            if (off > 0) {
                to.write(chars, 0, off);
                this.off = 0;
            }
        } catch (IOException e) {
            throw new JSONException("flushTo error", e);
        }
    }

    @Override
    public final void close() {
        char[] chars = this.chars;
        if (chars.length > CACHE_THRESHOLD) {
            return;
        }

        CHARS_UPDATER.lazySet(cacheItem, chars);
    }

    @Override
    protected final void write0(char c) {
        int off = this.off;
        char[] chars = this.chars;
        if (off == chars.length) {
            chars = grow(off + 1);
        }
        chars[off] = c;
        this.off = off + 1;
    }

    @Override
    public final void writeColon() {
        int off = this.off;
        char[] chars = this.chars;
        if (off == chars.length) {
            chars = grow(off + 1);
        }
        chars[off] = ':';
        this.off = off + 1;
    }

    @Override
    public final void startObject() {
        if (++level > context.maxLevel) {
            overflowLevel();
        }

        startObject = true;

        int off = this.off;
        char[] chars = this.chars;
        int minCapacity = off + 3 + pretty * level;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = (byte) '{';

        if (pretty != PRETTY_NON) {
            off = indent(chars, off);
        }
        this.off = off;
    }

    @Override
    public final void endObject() {
        level--;
        int off = this.off;
        int minCapacity = off + 1 + (pretty == 0 ? 0 : pretty * level + 1);
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (pretty != PRETTY_NON) {
            off = indent(chars, off);
        }

        chars[off] = (byte) '}';
        this.off = off + 1;
        startObject = false;
    }

    @Override
    public final void writeComma() {
        startObject = false;
        int off = this.off;
        int minCapacity = off + 2 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = (byte) ',';
        if (pretty != PRETTY_NON) {
            off = indent(chars, off);
        }
        this.off = off;
    }

    @Override
    public final void startArray() {
        if (++level > context.maxLevel) {
            overflowLevel();
        }

        int off = this.off;
        int minCapacity = off + 3 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = (byte) '[';
        if (pretty != PRETTY_NON) {
            off = indent(chars, off);
        }
        this.off = off;
    }

    @Override
    public final void endArray() {
        level--;
        int off = this.off;
        int minCapacity = off + 1 + (pretty == 0 ? 0 : pretty * level + 1);
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (pretty != PRETTY_NON) {
            off = indent(chars, off);
        }
        chars[off] = (byte) ']';
        this.off = off + 1;
        startObject = false;
    }

    public final void writeString(List<String> list) {
        if (pretty != PRETTY_NON) {
            super.writeString(list);
            return;
        }
        // startArray();
        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = '[';

        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                if (off == chars.length) {
                    grow(off + 1);
                }
                chars[off++] = ',';
            }

            writeString(list.get(i));
        }

        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = ']';
    }

    public void writeStringLatin1(byte[] value) {
        if ((context.features & MASK_BROWSER_SECURE) != 0) {
            writeStringLatin1BrowserSecure(value);
            return;
        }

        boolean escape = false;
        int off = this.off;
        char[] chars = this.chars;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = quote;

        int coff = 0;
        final long vecQuote = this.byteVectorQuote;
        final int upperBound = (value.length - coff) & ~7;
        long vec64;
        for (; coff < upperBound && StringUtils.noneEscaped(vec64 = getLongLE(value, coff), vecQuote); coff += 8) {
            IOUtils.putLongLE(chars, off, expand(vec64));
            IOUtils.putLongLE(chars, off + 4, expand(vec64 >>> 32));
            off += 8;
        }
        if (!escape) {
            for (; coff < value.length; coff++) {
                byte c = value[coff];
                if (c == '\\' || c == quote || c < ' ') {
                    escape = true;
                    break;
                }

                chars[off++] = (char) c;
            }
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        minCapacity += value.length * 5;
        if (minCapacity >= chars.length) {
            chars = grow(minCapacity);
        }

        this.off = StringUtils.writeLatin1EscapedRest(chars, off, value, coff, this.quote, context.features);
    }

    static long expand(long i) {
        return (i & 0xFFL) | ((i & 0xFF00L) << 8) | ((i & 0xFF0000L) << 16) | ((i & 0xFF000000L) << 24);
    }

    protected final void writeStringLatin1BrowserSecure(byte[] value) {
        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        final int start = off;
        final char[] chars = this.chars;
        chars[off++] = quote;

        for (byte c : value) {
            if (c == '\\' || c == quote || c < ' ' || c == '<' || c == '>' || c == '(' || c == ')') {
                escape = true;
                break;
            }

            chars[off++] = (char) c;
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        this.off = start;
        writeStringEscape(value);
    }

    public void writeStringUTF16(final byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        if ((context.features & (BrowserSecure.mask | EscapeNoneAscii.mask)) != 0) {
            writeStringUTF16BrowserSecure(value);
            return;
        }

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        final long vecQuote = this.byteVectorQuote;
        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0, char_len = value.length >> 1; i < char_len;) {
            if (i + 8 < char_len) {
                long v0 = getLongLE(value, i << 1);
                long v1 = getLongLE(value, (i + 4) << 1);
                if (((v0 | v1) & 0xFF00FF00FF00FF00L) == 0 && StringUtils.noneEscaped((v0 << 8) | v1, vecQuote)) {
                    putLongLE(chars, off, v0);
                    putLongLE(chars, off + 4, v1);
                    i += 8;
                    off += 8;
                    continue;
                }
            }
            char c = getChar(value, i++);
            if (c == '\\' || c == quote || c < ' ') {
                escape = true;
                break;
            }

            chars[off++] = c;
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscapeUTF16(value);
    }

    final void writeStringBrowserSecure(char[] value) {
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0, char_len = value.length; i < char_len; i++) {
            char c = getChar(value, i);
            if (c == '\\'
                    || c == quote
                    || c < ' '
                    || c == '<'
                    || c == '>'
                    || c == '('
                    || c == ')'
                    || (escapeNoneAscii && c > 0x007F)
            ) {
                escape = true;
                break;
            }

            chars[off++] = c;
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscape(value);
    }

    final void writeStringUTF16BrowserSecure(byte[] value) {
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0, char_len = value.length >> 1; i < char_len; i++) {
            char c = getChar(value, i);
            if (c == '\\'
                    || c == quote
                    || c < ' '
                    || c == '<'
                    || c == '>'
                    || c == '('
                    || c == ')'
                    || (escapeNoneAscii && c > 0x007F)
            ) {
                escape = true;
                break;
            }

            chars[off++] = c;
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscapeUTF16(value);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;
        final char quote = this.quote;

        final int strlen = str.length();
        int minCapacity = off + strlen + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        for (int i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if (c == '\\'
                    || c == quote
                    || c < ' '
                    || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
                    || (escapeNoneAscii && c > 0x007F)
            ) {
                escape = true;
                break;
            }
        }

        if (!escape) {
            int off = this.off;
            final char[] chars = this.chars;
            chars[off++] = quote;
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscape(str);
    }

    protected final void writeStringEscape(String str) {
        final int strlen = str.length();
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacityInternal(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < strlen; ++i) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    StringUtils.writeEscapedChar(chars, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    StringUtils.writeU4Hex2(chars, off, ch);
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    protected final void writeStringEscapeUTF16(byte[] str) {
        final int strlen = str.length;
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacityInternal(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < strlen; i += 2) {
            char ch = UNSAFE.getChar(str, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + i);
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    StringUtils.writeEscapedChar(chars, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    StringUtils.writeU4Hex2(chars, off, ch);
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    protected final void writeStringEscape(char[] str) {
        final int strlen = str.length;
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacityInternal(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < str.length; i++) {
            char ch = str[i];
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    StringUtils.writeEscapedChar(chars, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    StringUtils.writeU4Hex2(chars, off, ch);
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    protected final void writeStringEscape(byte[] str) {
        int off = this.off;
        char[] chars = this.chars;

        int minCapacity = off + str.length * 6 + 2;
        if (minCapacity >= chars.length) {
            chars = grow(minCapacity);
        }

        char quote = this.quote;
        chars[off++] = quote;
        this.off = StringUtils.writeLatin1EscapedRest(chars, off, str, 0, quote, context.features);
    }

    @Override
    public final void writeString(char[] str, int offset, int len, boolean quoted) {
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        final char quote = this.quote;
        int off = this.off;
        int minCapacity = quoted ? off + 2 : off;
        if (escapeNoneAscii) {
            minCapacity += len * 6;
        } else {
            minCapacity += len * 2;
        }

        char[] chars = this.chars;
        if (minCapacity - chars.length > 0) {
            chars = grow(minCapacity);
        }

        if (quoted) {
            chars[off++] = quote;
        }

        for (int i = offset; i < len; ++i) {
            char ch = str[i];
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    StringUtils.writeEscapedChar(chars, off, ch);
                    off += 2;
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 11:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    StringUtils.writeU4Hex2(chars, off, ch);
                    off += 6;
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        StringUtils.writeU4HexU(chars, off, ch);
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }

        if (quoted) {
            chars[off++] = quote;
        }
        this.off = off;
    }

    public final void writeString(String[] strings) {
        if (pretty != PRETTY_NON || strings == null) {
            super.writeString(strings);
            return;
        }
        // startArray();
        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = '[';

        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                if (off == chars.length) {
                    grow(off + 1);
                }
                chars[off++] = ',';
            }

            writeString(strings[i]);
        }

        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = ']';
    }

    @Override
    public final void writeReference(String path) {
        this.lastReference = path;
        int off = this.off;
        char[] chars = this.chars;
        if (off + 9 > chars.length) {
            chars = grow(off + 9);
        }
        long address = ARRAY_BYTE_BASE_OFFSET + ((long) off << 1);
        UNSAFE.putLong(chars, address, REF_0);
        UNSAFE.putLong(chars, address + 8, REF_1);
        this.off = off + 8;
        writeString(path);
        off = this.off;
        chars = this.chars;
        if (off == chars.length) {
            chars = grow(off + 1);
        }
        chars[off] = '}';
        this.off = off + 1;
    }

    @Override
    public final void writeBase64(byte[] bytes) {
        if (bytes == null) {
            writeArrayNull();
            return;
        }

        int charsLen = ((bytes.length - 1) / 3 + 1) << 2; // base64 character count

        int off = this.off;
        ensureCapacityInternal(off + charsLen + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;

        int eLen = (bytes.length / 3) * 3; // Length of even 24-bits.

        for (int s = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);

            // Encode the int into four chars
            chars[off] = CA[(i >>> 18) & 0x3f];
            chars[off + 1] = CA[(i >>> 12) & 0x3f];
            chars[off + 2] = CA[(i >>> 6) & 0x3f];
            chars[off + 3] = CA[i & 0x3f];
            off += 4;
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = bytes.length - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((bytes[eLen] & 0xff) << 10) | (left == 2 ? ((bytes[bytes.length - 1] & 0xff) << 2) : 0);

            // Set last four chars
            chars[off] = CA[i >> 12];
            chars[off + 1] = CA[(i >>> 6) & 0x3f];
            chars[off + 2] = left == 2 ? CA[i & 0x3f] : '=';
            chars[off + 3] = '=';
            off += 4;
        }

        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeHex(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }

        int charsLen = bytes.length * 2 + 3;

        int off = this.off;
        char[] chars = this.chars;
        int minCapacity = off + charsLen + 2;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = 'x';
        chars[off + 1] = '\'';
        off += 2;

        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            int a = b & 0xFF;
            int b0 = a >> 4;
            int b1 = a & 0xf;

            chars[off] = (char) (b0 + (b0 < 10 ? 48 : 55));
            chars[off + 1] = (char) (b1 + (b1 < 10 ? 48 : 55));
            off += 2;
        }

        chars[off] = '\'';
        this.off = off + 1;
    }

    @Override
    public final void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        if (isInt64(value) && features == 0) {
            writeInt64(value.longValue());
            return;
        }

        String str = value.toString(10);

        boolean writeAsString = isWriteAsString(value, context.features | features);

        int off = this.off;
        int strlen = str.length();

        int minCapacity = off + strlen + (writeAsString ? 2 : 0);
        char[] chars = this.chars;
        if (minCapacity > this.chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
        }
        str.getChars(0, strlen, chars, off);
        off += strlen;
        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    public final void writeDecimal(BigDecimal value, long features, DecimalFormat format) {
        if (value == null) {
            writeDecimalNull();
            return;
        }

        if (format != null) {
            String str = format.format(value);
            writeRaw(str);
            return;
        }

        features |= context.features;

        int precision = value.precision();
        boolean writeAsString = isWriteAsString(value, features);

        int off = this.off;
        int minCapacity = off + precision + Math.abs(value.scale()) + 7;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
        }

        boolean asPlain = (features & WriteBigDecimalAsPlain.mask) != 0;
        long unscaleValue;
        int scale;
        if (precision < 19
                && (scale = value.scale()) >= 0
                && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1
                && (unscaleValue = UNSAFE.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET)) != Long.MIN_VALUE
                && !asPlain
        ) {
            off = IOUtils.writeDecimal(chars, off, unscaleValue, scale);
        } else {
            String str = asPlain ? value.toPlainString() : value.toString();
            str.getChars(0, str.length(), chars, off);
            off += str.length();
        }

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    static void putLong(char[] buf, int off, int b0, int b1) {
        long v = HEX256[b0 & 0xff] | (((long) HEX256[b1 & 0xff]) << 32);
        UNSAFE.putLong(
                buf,
                ARRAY_CHAR_BASE_OFFSET + ((long) off << 1),
                BIG_ENDIAN ? Long.reverseBytes(v << 8) : v
        );
    }

    @Override
    public final void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        long msb = value.getMostSignificantBits();
        long lsb = value.getLeastSignificantBits();

        int minCapacity = off + 38;
        char[] buf = this.chars;
        if (minCapacity > chars.length) {
            buf = grow(minCapacity);
        }

        final int off = this.off;
        buf[off] = '"';
        putLong(buf, off + 1, (int) (msb >> 56), (int) (msb >> 48));
        putLong(buf, off + 5, (int) (msb >> 40), (int) (msb >> 32));
        buf[off + 9] = '-';
        putLong(buf, off + 10, ((int) msb) >> 24, ((int) msb) >> 16);
        buf[off + 14] = '-';
        putLong(buf, off + 15, ((int) msb) >> 8, (int) msb);
        buf[off + 19] = '-';
        putLong(buf, off + 20, (int) (lsb >> 56), (int) (lsb >> 48));
        buf[off + 24] = '-';
        putLong(buf, off + 25, ((int) (lsb >> 40)), (int) (lsb >> 32));
        putLong(buf, off + 29, ((int) lsb) >> 24, ((int) lsb) >> 16);
        putLong(buf, off + 33, ((int) lsb) >> 8, (int) lsb);
        buf[off + 37] = '"';
        this.off += 38;
    }

    @Override
    public final void writeRaw(String str) {
        int strlen = str.length();
        int off = this.off;
        char[] chars = this.chars;
        if (off + strlen > chars.length) {
            chars = grow(off + strlen);
        }
        str.getChars(0, strlen, chars, off);
        this.off = off + strlen;
    }

    @Override
    public final void writeRaw(char[] str, int coff, int strlen) {
        int off = this.off;
        char[] chars = this.chars;
        if (off + strlen > chars.length) {
            chars = grow(off + strlen);
        }
        System.arraycopy(str, coff, chars, off, strlen);
        this.off = off + strlen;
    }

    @Override
    public final void writeChar(char ch) {
        int off = this.off;
        char[] chars = this.chars;
        if (off + 8 > chars.length) {
            chars = grow(off + 8);
        }

        chars[off++] = quote;
        switch (ch) {
            case '"':
            case '\'':
                if (ch == quote) {
                    chars[off++] = '\\';
                }
                chars[off++] = ch;
                break;
            case '\\':
            case '\r':
            case '\n':
            case '\b':
            case '\f':
            case '\t':
                StringUtils.writeEscapedChar(chars, off, ch);
                off += 2;
                break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                chars[off] = '\\';
                chars[off + 1] = 'u';
                chars[off + 2] = '0';
                chars[off + 3] = '0';
                chars[off + 4] = '0';
                chars[off + 5] = (char) ('0' + (int) ch);
                off += 6;
                break;
            case 11:
            case 14:
            case 15:
                chars[off] = '\\';
                chars[off + 1] = 'u';
                chars[off + 2] = '0';
                chars[off + 3] = '0';
                chars[off + 4] = '0';
                chars[off + 5] = (char) ('a' + (ch - 10));
                off += 6;
                break;
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
                chars[off] = '\\';
                chars[off + 1] = 'u';
                chars[off + 2] = '0';
                chars[off + 3] = '0';
                chars[off + 4] = '1';
                chars[off + 5] = (char) ('0' + (ch - 16));
                off += 6;
                break;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                chars[off] = '\\';
                chars[off + 1] = 'u';
                chars[off + 2] = '0';
                chars[off + 3] = '0';
                chars[off + 4] = '1';
                chars[off + 5] = (char) ('a' + (ch - 26));
                off += 6;
                break;
            default:
                chars[off++] = ch;
                break;
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeRaw(char ch) {
        if (off == chars.length) {
            grow0(off + 1);
        }
        chars[off++] = ch;
    }

    @Override
    public final void writeRaw(char c0, char c1) {
        int off = this.off;
        char[] chars = this.chars;
        if (off + 2 > chars.length) {
            chars = grow(off + 2);
        }
        chars[off] = c0;
        chars[off + 1] = c1;
        this.off = off + 2;
    }

    @Override
    public final void writeNameRaw(char[] name) {
        int off = this.off;
        int minCapacity = off + name.length + 2 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }
        System.arraycopy(name, 0, chars, off, name.length);
        this.off = off + name.length;
    }

    @Override
    public final void writeName2Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        this.off = off + 5;
    }

    @Override
    public final void writeName3Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        this.off = off + 6;
    }

    @Override
    public final void writeName4Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        this.off = off + 7;
    }

    @Override
    public final void writeName5Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        this.off = off + 8;
    }

    @Override
    public final void writeName6Raw(long name) {
        int off = this.off;
        int minCapacity = off + 11 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        chars[off + 8] = ':';
        this.off = off + 9;
    }

    @Override
    public final void writeName7Raw(long name) {
        int off = this.off;
        int minCapacity = off + 12 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name);
        putIntUnaligned(chars, off + 8, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 10;
    }

    @Override
    public final void writeName8Raw(long name) {
        int off = this.off;
        int minCapacity = off
                + 13 // 8 + quote 2 + comma 1 + colon 1 + pretty 1
                + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        chars[off++] = quote;
        putLong(chars, off, name);
        putIntUnaligned(chars, off + 8, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 10;
    }

    @Override
    public final void writeName9Raw(long name0, int name1) {
        int off = this.off;
        int minCapacity = off + 14 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        this.off = off + 12;
    }

    @Override
    public final void writeName10Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        this.off = off + 13;
    }

    @Override
    public final void writeName11Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        this.off = off + 14;
    }

    @Override
    public final void writeName12Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        this.off = off + 15;
    }

    @Override
    public final void writeName13Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        this.off = off + 16;
    }

    @Override
    public final void writeName14Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 19 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        chars[off + 16] = ':';
        this.off = off + 17;
    }

    @Override
    public final void writeName15Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 20 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        putLong(chars, off, name0, name1);
        putIntUnaligned(chars, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 18;
    }

    @Override
    public final void writeName16Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 21 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(chars, off);
            }
        }

        chars[off++] = quote;
        putLong(chars, off, name0, name1);
        putIntUnaligned(chars, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 18;
    }

    private static void putLong(char[] chars, int off, long name) {
        final long base = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        UNSAFE.putLong(chars, base,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        UNSAFE.putLong(chars, base + 8,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));
    }

    private static void putLong(char[] chars, int off, long name, int name1) {
        final long base = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        UNSAFE.putLong(chars, base,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        UNSAFE.putLong(chars, base + 8,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));

        UNSAFE.putLong(chars,
                base + 16,
                (name1 & 0xFFL)
                        | ((name1 & 0xFF00L) << 8)
                        | ((name1 & 0xFF0000L) << 16)
                        | ((name1 & 0xFF00_0000L) << 24));
    }

    private static void putLong(char[] chars, int off, long name, long name1) {
        final long base = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        UNSAFE.putLong(chars, base,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        UNSAFE.putLong(chars, base + 8,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));

        UNSAFE.putLong(chars, base + 16,
                (name1 & 0xFFL)
                        | ((name1 & 0xFF00L) << 8)
                        | ((name1 & 0xFF_0000L) << 16)
                        | ((name1 & 0xFF00_0000L) << 24));
        UNSAFE.putLong(chars, base + 24,
                ((name1 & 0xFF_0000_0000L) >> 32)
                        | ((name1 & 0xFF00_0000_0000L) >> 24)
                        | ((name1 & 0xFF_0000_0000_0000L) >> 16)
                        | ((name1 & 0xFF00_0000_0000_0000L) >> 8));
    }

    private int indent(char[] chars, int off) {
        chars[off] = '\n';
        int toIndex = off + 1 + pretty * level;
        Arrays.fill(chars, off + 1, toIndex, pretty == PRETTY_TAB ? '\t' : ' ');
        return toIndex;
    }

    @Override
    public final void writeNameRaw(char[] name, int coff, int len) {
        int off = this.off;
        int minCapacity = off + len + 2 + pretty * level;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            chars[off++] = ',';
        }
        System.arraycopy(name, coff, chars, off, len);
        this.off = off + len;
    }

    public final Object ensureCapacity(int minCapacity) {
        char[] chars = this.chars;
        if (minCapacity >= chars.length) {
            this.chars = chars = Arrays.copyOf(chars, newCapacity(minCapacity, chars.length));
        }
        return chars;
    }

    final void ensureCapacityInternal(int minCapacity) {
        if (minCapacity > chars.length) {
            grow0(minCapacity);
        }
    }

    private char[] grow(int minCapacity) {
        grow0(minCapacity);
        return chars;
    }

    protected final void grow0(int minCapacity) {
        chars = Arrays.copyOf(chars, newCapacity(minCapacity, chars.length));
    }

    public final void writeInt32(int[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + value.length * 13 + 2;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';

        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            if (writeAsString) {
                chars[off++] = quote;
            }
            off = IOUtils.writeInt32(chars, off, value[i]);
            if (writeAsString) {
                chars[off++] = quote;
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt8(byte i) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt8(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        }
        this.off = off;
    }

    public final void writeInt8(byte[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + value.length * 5 + 2;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';

        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            if (writeAsString) {
                chars[off++] = quote;
            }
            off = IOUtils.writeInt8(chars, off, value[i]);
            if (writeAsString) {
                chars[off++] = quote;
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt16(short i) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt16(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        }
        this.off = off;
    }

    @Override
    public final void writeInt32(int i) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + 13;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt32(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        }
        this.off = off;
    }

    @Override
    public final void writeInt32(Integer i) {
        if (i == null) {
            writeNumberNull();
        } else {
            writeInt32(i.intValue());
        }
    }

    @Override
    public final void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        int off = this.off;
        int minCapacity = off + 2 + values.length * 23;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            long v = values[i];
            boolean writeAsString = isWriteAsString(v, context.features);
            if (writeAsString) {
                chars[off++] = this.quote;
            }
            off = IOUtils.writeInt64(chars, off, v);
            if (writeAsString) {
                chars[off++] = this.quote;
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeListInt32(List<Integer> values) {
        if (values == null) {
            writeNull();
            return;
        }

        int size = values.size();
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;
        int off = this.off;
        int minCapacity = off + 2 + size * 23;
        if (minCapacity >= chars.length) {
            grow0(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = (byte) '[';

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                chars[off++] = (byte) ',';
            }
            Number item = values.get(i);
            if (item == null) {
                chars[off] = 'n';
                chars[off + 1] = 'u';
                chars[off + 2] = 'l';
                chars[off + 3] = 'l';
                off += 4;
                continue;
            }

            int v = item.intValue();
            if (writeAsString) {
                chars[off++] = quote;
            }
            off = IOUtils.writeInt32(chars, off, v);
            if (writeAsString) {
                chars[off++] = quote;
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeListInt64(List<Long> values) {
        if (values == null) {
            writeNull();
            return;
        }

        int size = values.size();
        int off = this.off;
        int minCapacity = off + 2 + size * 23;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = (byte) '[';

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                chars[off++] = (byte) ',';
            }
            Long item = values.get(i);
            if (item == null) {
                chars[off] = 'n';
                chars[off + 1] = 'u';
                chars[off + 2] = 'l';
                chars[off + 3] = 'l';
                off += 4;
                continue;
            }

            long v = item;
            boolean writeAsString = isWriteAsString(v, context.features);
            if (writeAsString) {
                chars[off++] = this.quote;
            }
            off = IOUtils.writeInt64(chars, off, v);
            if (writeAsString) {
                chars[off++] = this.quote;
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt64(long i) {
        long features = context.features;
        int off = this.off;
        int minCapacity = off + 23;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        boolean writeAsString = isWriteAsString(i, features);
        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt64(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        } else if ((features & MASK_WRITE_CLASS_NAME) != 0
                && (features & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                && i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE
        ) {
            chars[off++] = 'L';
        }
        this.off = off;
    }

    @Override
    public final void writeInt64(Long i) {
        if (i == null) {
            writeInt64Null();
        } else {
            writeInt64(i.longValue());
        }
    }

    @Override
    public final void writeFloat(float value) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 15;
        if (writeAsString) {
            minCapacity += 2;
        }

        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
        }

        off = NumberUtils.writeFloat(chars, off, value, true);

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeFloat(float[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * (writeAsString ? 16 : 18) + 1;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }

            if (writeAsString) {
                chars[off++] = '"';
            }

            off = NumberUtils.writeFloat(chars, off, values[i], true);

            if (writeAsString) {
                chars[off++] = '"';
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 24;
        if (writeAsString) {
            minCapacity += 2;
        }

        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
        }

        off = NumberUtils.writeDouble(chars, off, value, true);

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeDoubleArray(double value0, double value1) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 48 + 3;
        if (writeAsString) {
            minCapacity += 2;
        }

        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';

        if (writeAsString) {
            chars[off++] = '"';
        }
        off = NumberUtils.writeDouble(chars, off, value0, true);
        if (writeAsString) {
            chars[off++] = '"';
        }

        chars[off++] = ',';

        if (writeAsString) {
            chars[off++] = '"';
        }
        off = NumberUtils.writeDouble(chars, off, value1, true);
        if (writeAsString) {
            chars[off++] = '"';
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 27 + 1;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }

            if (writeAsString) {
                chars[off++] = '"';
            }

            off = NumberUtils.writeDouble(chars, off, values[i], true);

            if (writeAsString) {
                chars[off++] = '"';
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        int off = this.off;
        int minCapacity = off + 16;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off] = quote;
        if (year < 0 || year > 9999) {
            throw illegalYear(year);
        }
        int y01 = year / 100;
        int y23 = year - y01 * 100;
        writeDigitPair(chars, off + 1, y01);
        writeDigitPair(chars, off + 3, y23);
        writeDigitPair(chars, off + 5, month);
        writeDigitPair(chars, off + 7, dayOfMonth);
        writeDigitPair(chars, off + 9, hour);
        writeDigitPair(chars, off + 11, minute);
        writeDigitPair(chars, off + 13, second);
        chars[off + 15] = quote;
        this.off = off + 16;
    }

    @Override
    public final void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        char[] chars = this.chars;
        if (off + 21 > chars.length) {
            chars = grow(off + 21);
        }

        int off = this.off;
        chars[off] = quote;
        if (year < 0 || year > 9999) {
            throw illegalYear(year);
        }
        off = IOUtils.writeLocalDate(chars, off + 1, year, month, dayOfMonth);
        chars[off] = ' ';
        IOUtils.writeLocalTime(chars, off + 1, hour, minute, second);
        chars[off + 9] = quote;
        this.off = off + 10;
    }

    @Override
    public final void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        if (context.dateFormat != null
                && writeLocalDateWithFormat(date)) {
            return;
        }

        int off = this.off;
        int minCapacity = off + 18;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime dateTime) {
        int off = this.off;
        int minCapacity = off + 38;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off++] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off++] = ' ';
        off = IOUtils.writeLocalTime(chars, off, dateTime.toLocalTime());
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeDateTimeISO8601(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second,
            int millis,
            int offsetSeconds,
            boolean timeZone
    ) {
        int zonelen;
        if (timeZone) {
            zonelen = offsetSeconds == 0 ? 1 : 6;
        } else {
            zonelen = 0;
        }

        int off = this.off;
        int minCapacity = off + 25 + zonelen;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off] = quote;
        off = IOUtils.writeLocalDate(chars, off + 1, year, month, dayOfMonth);
        chars[off] = timeZone ? 'T' : ' ';
        IOUtils.writeLocalTime(chars, off + 1, hour, minute, second);
        off += 9;

        if (millis > 0) {
            int div = millis / 10;
            int div2 = div / 10;
            final int rem1 = millis - div * 10;

            if (rem1 != 0) {
                IOUtils.putLongLE(chars, off, (DIGITS_K_64[millis & 0x3ff] & 0xffffffffffff0000L) | DOT_X0);
                off += 4;
            } else {
                chars[off++] = '.';
                final int rem2 = div - div2 * 10;
                if (rem2 != 0) {
                    writeDigitPair(chars, off, div);
                    off += 2;
                } else {
                    chars[off++] = (char) (byte) (div2 + '0');
                }
            }
        }

        if (timeZone) {
            int offset = offsetSeconds / 3600;
            if (offsetSeconds == 0) {
                chars[off++] = 'Z';
            } else {
                int offsetAbs = Math.abs(offset);
                chars[off] = offset >= 0 ? '+' : '-';
                writeDigitPair(chars, off + 1, offsetAbs);
                chars[off + 3] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                writeDigitPair(chars, off + 4, offsetMinutes);
                off += 6;
            }
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 10;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = quote;
        if (year < 0 || year > 9999) {
            throw illegalYear(year);
        }
        int y01 = year / 100;
        int y23 = year - y01 * 100;
        writeDigitPair(chars, off + 1, y01);
        writeDigitPair(chars, off + 3, y23);
        writeDigitPair(chars, off + 5, month);
        writeDigitPair(chars, off + 7, dayOfMonth);
        chars[off + 9] = quote;
        this.off = off + 10;
    }

    @Override
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 13;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
        int off = this.off;
        int minCapacity = off + 10;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = (char) (byte) quote;
        writeDigitPair(chars, off + 1, hour);
        chars[off + 3] = ':';
        writeDigitPair(chars, off + 4, minute);
        chars[off + 6] = ':';
        writeDigitPair(chars, off + 7, second);
        chars[off + 9] = (char) (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeLocalTime(LocalTime time) {
        int off = this.off;
        int minCapacity = off + 20;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off++] = quote;
        off = IOUtils.writeLocalTime(chars, off, time);
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }
        ZoneId zone = dateTime.getZone();
        String zoneId = zone.getId();
        int zoneIdLength = zoneId.length();

        char firstZoneChar = '\0';
        int zoneSize;
        if (ZoneOffset.UTC == zone || (zoneIdLength <= 3 && ("UTC".equals(zoneId) || "Z".equals(zoneId)))) {
            zoneId = "Z";
            zoneSize = 1;
        } else if (zoneIdLength != 0 && ((firstZoneChar = zoneId.charAt(0)) == '+' || firstZoneChar == '-')) {
            zoneSize = zoneIdLength;
        } else {
            zoneSize = 2 + zoneIdLength;
        }

        int off = this.off;
        int minCapacity = off + zoneSize + 38;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off] = 'T';
        off = IOUtils.writeLocalTime(chars, off + 1, dateTime.toLocalTime());
        if (zoneSize == 1) {
            chars[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getChars(0, zoneIdLength, chars, off);
            off += zoneIdLength;
        } else {
            chars[off++] = '[';
            zoneId.getChars(0, zoneIdLength, chars, off);
            off += zoneIdLength;
            chars[off++] = ']';
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeOffsetDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int off = this.off;
        int minCapacity = off + 45;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = quote;
        LocalDateTime ldt = dateTime.toLocalDateTime();
        LocalDate date = ldt.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off + 1, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        chars[off] = 'T';
        off = IOUtils.writeLocalTime(chars, off + 1, ldt.toLocalTime());

        ZoneOffset offset = dateTime.getOffset();
        if (offset.getTotalSeconds() == 0) {
            chars[off++] = 'Z';
        } else {
            String zoneId = offset.getId();
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeOffsetTime(OffsetTime time) {
        if (time == null) {
            writeNull();
            return;
        }

        ZoneOffset offset = time.getOffset();
        int off = this.off;
        int minCapacity = off + 28;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }
        chars[off] = quote;
        off = IOUtils.writeLocalTime(chars, off + 1, time.toLocalTime());
        if (offset.getTotalSeconds() == 0) {
            chars[off++] = 'Z';
        } else {
            String zoneId = offset.getId();
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
        }
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeNameRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public final int flushTo(OutputStream out) throws IOException {
        if (out == null) {
            throw new JSONException("out is nulll");
        }

        boolean ascii = true;
        for (int i = 0; i < off; i++) {
            if (chars[i] >= 0x80) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            byte[] bytes = new byte[off];
            for (int i = 0; i < off; i++) {
                bytes[i] = (byte) chars[i];
            }
            out.write(bytes);
            off = 0;
            return bytes.length;
        }

        byte[] utf8 = new byte[off * 3];
        int utf8Length = encodeUTF8(chars, 0, off, utf8, 0);
        out.write(utf8, 0, utf8Length);
        off = 0;
        return utf8Length;
    }

    @Override
    public final int flushTo(OutputStream out, Charset charset) throws IOException {
        if (off == 0) {
            return 0;
        }

        if (out == null) {
            throw new JSONException("out is null");
        }

        byte[] bytes = getBytes(charset);
        out.write(bytes);
        off = 0;
        return bytes.length;
    }

    @Override
    public final String toString() {
        return new String(chars, 0, off);
    }

    @Override
    public final byte[] getBytes() {
        boolean ascii = true;
        for (int i = 0; i < off; i++) {
            if (chars[i] >= 0x80) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            byte[] bytes = new byte[off];
            for (int i = 0; i < off; i++) {
                bytes[i] = (byte) chars[i];
            }
            return bytes;
        }
        byte[] utf8 = new byte[off * 3];
        int utf8Length = encodeUTF8(chars, 0, off, utf8, 0);
        return Arrays.copyOf(utf8, utf8Length);
    }

    @Override
    public final int size() {
        return off;
    }

    @Override
    public final byte[] getBytes(Charset charset) {
        boolean ascii = true;
        for (int i = 0; i < off; i++) {
            if (chars[i] >= 0x80) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            if (charset == StandardCharsets.UTF_8
                    || charset == StandardCharsets.ISO_8859_1
                    || charset == StandardCharsets.US_ASCII
            ) {
                byte[] bytes = new byte[off];
                for (int i = 0; i < off; i++) {
                    bytes[i] = (byte) chars[i];
                }
                return bytes;
            }
        }

        return new String(chars, 0, off)
                .getBytes(charset != null ? charset : StandardCharsets.UTF_8);
    }

    @Override
    public final void writeRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public final void write(Map<?, ?> map) {
        if (pretty != PRETTY_NON) {
            super.write(map);
            return;
        }

        if (map == null) {
            this.writeNull();
            return;
        }

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(map.getClass());
            objectWriter.write(this, map, null, null, 0);
            return;
        }

        writeRaw('{');

        boolean first = true;
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null && (context.features & WriteMapNullValue.mask) == 0) {
                continue;
            }

            if (!first) {
                writeRaw(',');
            }

            first = false;
            Object key = entry.getKey();
            if (key instanceof String) {
                writeString((String) key);
            } else {
                writeAny(key);
            }

            writeRaw(':');

            if (value == null) {
                writeNull();
                continue;
            }

            Class<?> valueClass = value.getClass();
            if (valueClass == String.class) {
                writeString((String) value);
                continue;
            }

            if (valueClass == Integer.class) {
                writeInt32((Integer) value);
                continue;
            }

            if (valueClass == Long.class) {
                writeInt64((Long) value);
                continue;
            }

            if (valueClass == Boolean.class) {
                writeBool((Boolean) value);
                continue;
            }

            if (valueClass == BigDecimal.class) {
                writeDecimal((BigDecimal) value, 0, null);
                continue;
            }

            if (valueClass == JSONArray.class) {
                write((JSONArray) value);
                continue;
            }

            if (valueClass == JSONObject.class) {
                write((JSONObject) value);
                continue;
            }

            ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
            objectWriter.write(this, value, null, null, 0);
        }

        writeRaw('}');
    }

    @Override
    public final void write(List array) {
        if (array == null) {
            this.writeArrayNull();
            return;
        }

        final long NONE_DIRECT_FEATURES = ReferenceDetection.mask
                | PrettyFormat.mask
                | NotWriteEmptyArray.mask
                | NotWriteDefaultValue.mask;

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(array.getClass());
            objectWriter.write(this, array, null, null, 0);
            return;
        }

        if (off == chars.length) {
            grow0(off + 1);
        }
        chars[off++] = '[';

        boolean first = true;
        for (int i = 0; i < array.size(); i++) {
            Object o = array.get(i);
            if (!first) {
                if (off == chars.length) {
                    grow(off + 1);
                }
                chars[off++] = ',';
            }
            first = false;

            if (o == null) {
                writeNull();
                continue;
            }

            Class<?> valueClass = o.getClass();
            if (valueClass == String.class) {
                writeString((String) o);
                continue;
            }

            if (valueClass == Integer.class) {
                writeInt32((Integer) o);
                continue;
            }

            if (valueClass == Long.class) {
                writeInt64((Long) o);
                continue;
            }

            if (valueClass == Boolean.class) {
                writeBool((Boolean) o);
                continue;
            }

            if (valueClass == BigDecimal.class) {
                writeDecimal((BigDecimal) o, 0, null);
                continue;
            }

            if (valueClass == JSONArray.class) {
                write((JSONArray) o);
                continue;
            }

            if (valueClass == JSONObject.class) {
                write((JSONObject) o);
                continue;
            }

            ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
            objectWriter.write(this, o, null, null, 0);
        }
        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = ']';
    }

    @Override
    public final void writeString(boolean value) {
        chars[off++] = quote;
        writeBool(value);
        chars[off++] = quote;
    }

    @Override
    public final void writeString(byte value) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) == 0;
        if (writeAsString) {
            writeQuote();
        }
        writeInt8(value);
        if (writeAsString) {
            writeQuote();
        }
    }

    @Override
    public final void writeString(short value) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) == 0;
        if (writeAsString) {
            writeQuote();
        }
        writeInt16(value);
        if (writeAsString) {
            writeQuote();
        }
    }

    @Override
    public final void writeString(int value) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) == 0;
        if (writeAsString) {
            writeQuote();
        }
        writeInt32(value);
        if (writeAsString) {
            writeQuote();
        }
    }

    @Override
    public final void writeString(long value) {
        boolean writeAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) == 0;
        if (writeAsString) {
            writeQuote();
        }
        writeInt64(value);
        if (writeAsString) {
            writeQuote();
        }
    }

    private void writeQuote() {
        if (off == chars.length) {
            grow(off + 1);
        }
        chars[off++] = quote;
    }

    public final void writeString(final char[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        if ((context.features & (BrowserSecure.mask | EscapeNoneAscii.mask)) != 0) {
            writeStringBrowserSecure(value);
            return;
        }

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            grow(minCapacity);
        }

        final long vecQuote = this.byteVectorQuote;
        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0, char_len = value.length; i < char_len;) {
            if (i + 8 < char_len) {
                long v0 = getLongLE(value, i);
                long v1 = getLongLE(value, i + 4);
                if (((v0 | v1) & 0xFF00FF00FF00FF00L) == 0 && StringUtils.noneEscaped((v0 << 8) | v1, vecQuote)) {
                    putLongLE(chars, off, v0);
                    putLongLE(chars, off + 4, v1);
                    i += 8;
                    off += 8;
                    continue;
                }
            }
            char c = getChar(value, i++);
            if (c == '\\' || c == quote || c < ' ') {
                escape = true;
                break;
            }

            chars[off++] = c;
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscape(value);
    }

    public final void writeString(char[] str, int coff, int len) {
        if (str == null) {
            writeStringNull();
            return;
        }

        boolean special = (context.features & EscapeNoneAscii.mask) != 0;
        for (int i = coff; i < len; ++i) {
            char ch = str[i];
            if (ch == '\\' || ch == quote || ch < ' ') {
                special = true;
                break;
            }
        }

        if (!special) {
            // inline ensureCapacity
            int off = this.off;
            int minCapacity = off + len + 2;
            char[] chars = this.chars;
            if (minCapacity > chars.length) {
                chars = grow(minCapacity);
            }

            chars[off++] = quote;
            System.arraycopy(str, coff, chars, off, len);
            off += len;
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscape(new String(str, coff, len));
    }

    public void writeBool(boolean value) {
        int minCapacity = off + 5;
        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        int off = this.off;
        if ((context.features & WriteBooleanAsNumber.mask) != 0) {
            chars[off++] = value ? '1' : '0';
        } else {
            if (!value) {
                chars[off] = 'f';
                chars[off + 1] = 'a';
                chars[off + 2] = 'l';
                chars[off + 3] = 's';
                chars[off + 4] = 'e';
                off += 5;
            } else {
                chars[off] = 't';
                chars[off + 1] = 'r';
                chars[off + 2] = 'u';
                chars[off + 3] = 'e';
                off += 4;
            }
        }
        this.off = off;
    }
}

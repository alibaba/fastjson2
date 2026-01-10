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
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.internal.Conf.BYTES;
import static com.alibaba.fastjson2.internal.Conf.DECIMAL_INT_COMPACT;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

public class JSONWriterUTF16
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
        REF_0 = BYTES.getLongUnaligned(chars, 0);
        REF_1 = BYTES.getLongUnaligned(chars, 4);
        QUOTE2_COLON = BYTES.getIntUnaligned(chars, 6);
        chars[6] = '\'';
        QUOTE_COLON = BYTES.getIntUnaligned(chars, 6);
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
            BYTES.putLongLE(chars, off, expand(vec64));
            BYTES.putLongLE(chars, off + 4, expand(vec64 >>> 32));
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
                    BYTES.putLongLE(chars, off, v0);
                    BYTES.putLongLE(chars, off + 4, v1);
                    i += 8;
                    off += 8;
                    continue;
                }
            }
            char c = BYTES.getChar(value, i++);
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
            char c = BYTES.getChar(value, i);
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
            char c = BYTES.getChar(value, i);
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
        final int strlen = str.length >> 1;
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacityInternal(off + strlen * 12 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int charIndex = 0; charIndex < strlen; charIndex++) {
            char ch = BYTES.getChar(str, charIndex);
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

        for (int i = offset, end = Math.min(offset + len, str.length); i < end; ++i) {
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
        BYTES.putLongUnaligned(chars, off, REF_0);
        BYTES.putLongUnaligned(chars, off + 4, REF_1);
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
                && DECIMAL_INT_COMPACT != null
                && (unscaleValue = DECIMAL_INT_COMPACT.applyAsLong(value)) != Long.MIN_VALUE
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
        BYTES.putLongLE(buf, off, v);
    }

    @Override
    public final void writeUUID(UUID value) {
        int off = this.off;
        char[] buf = IO.ensureCapacity(this, off + JSONWriterUTF8.IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
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

        this.off = IO.writeValue(this, chars, off, ch, context.features);
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
        BYTES.putIntUnaligned(chars, off + 8, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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
        BYTES.putIntUnaligned(chars, off + 8, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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
        BYTES.putIntUnaligned(chars, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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
        BYTES.putIntUnaligned(chars, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 18;
    }

    private static void putLong(char[] chars, int off, long name) {
        BYTES.putLongUnaligned(chars, off,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        BYTES.putLongUnaligned(chars, off + 4,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));
    }

    private static void putLong(char[] chars, int off, long name, int name1) {
        BYTES.putLongUnaligned(chars, off,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        BYTES.putLongUnaligned(chars, off + 4,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));

        BYTES.putLongUnaligned(chars,
                off + 8,
                (name1 & 0xFFL)
                        | ((name1 & 0xFF00L) << 8)
                        | ((name1 & 0xFF0000L) << 16)
                        | ((name1 & 0xFF00_0000L) << 24));
    }

    private static void putLong(char[] chars, int off, long name, long name1) {
        BYTES.putLongUnaligned(chars, off,
                (name & 0xFFL)
                        | ((name & 0xFF00L) << 8)
                        | ((name & 0xFF_0000L) << 16)
                        | ((name & 0xFF00_0000L) << 24));
        BYTES.putLongUnaligned(chars, off + 4,
                ((name & 0xFF_0000_0000L) >> 32)
                        | ((name & 0xFF00_0000_0000L) >> 24)
                        | ((name & 0xFF_0000_0000_0000L) >> 16)
                        | ((name & 0xFF00_0000_0000_0000L) >> 8));

        BYTES.putLongUnaligned(chars, off + 8,
                (name1 & 0xFFL)
                        | ((name1 & 0xFF00L) << 8)
                        | ((name1 & 0xFF_0000L) << 16)
                        | ((name1 & 0xFF00_0000L) << 24));
        BYTES.putLongUnaligned(chars, off + 12,
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

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

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

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
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
    public final void writeInt64(long i, long features) {
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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;

        if (writeSpecialAsString && !Float.isFinite(value)) {
            writeAsString = false;
        }

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

        off = NumberUtils.writeFloat(chars, off, value, true, writeSpecialAsString);

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

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;

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

            if (!Float.isFinite(values[i])) {
                off = NumberUtils.writeFloat(chars, off, values[i], true, writeSpecialAsString);
            } else {
                if (writeAsString) {
                    chars[off++] = '"';
                }
                off = NumberUtils.writeFloat(chars, off, values[i], true, false);
                if (writeAsString) {
                    chars[off++] = '"';
                }
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;

        if (writeSpecialAsString && !Double.isFinite(value)) {
            writeAsString = false;
        }

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

        off = NumberUtils.writeDouble(chars, off, value, true, writeSpecialAsString);

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeDoubleArray(double value0, double value1) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + 48 + 3;
        if (writeAsString) {
            minCapacity += 4;
        }

        char[] chars = this.chars;
        if (minCapacity > chars.length) {
            chars = grow(minCapacity);
        }

        chars[off++] = '[';

        if (!Double.isFinite(value0)) {
            off = NumberUtils.writeDouble(chars, off, value0, true, writeSpecialAsString);
        } else {
            if (writeAsString) {
                chars[off++] = '"';
            }
            off = NumberUtils.writeDouble(chars, off, value0, true, false);
            if (writeAsString) {
                chars[off++] = '"';
            }
        }

        chars[off++] = ',';

        if (!Double.isFinite(value1)) {
            off = NumberUtils.writeDouble(chars, off, value1, true, writeSpecialAsString);
        } else {
            if (writeAsString) {
                chars[off++] = '"';
            }
            off = NumberUtils.writeDouble(chars, off, value1, true, false);
            if (writeAsString) {
                chars[off++] = '"';
            }
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

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;

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

            if (!Double.isFinite(values[i])) {
                off = NumberUtils.writeDouble(chars, off, values[i], true, writeSpecialAsString);
            } else {
                if (writeAsString) {
                    chars[off++] = '"';
                }
                off = NumberUtils.writeDouble(chars, off, values[i], true, false);
                if (writeAsString) {
                    chars[off++] = '"';
                }
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
                BYTES.putLongLE(chars, off, (DIGITS_K_64[millis & 0x3ff] & 0xffffffffffff0000L) | DOT_X0);
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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) == 0;
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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) == 0;
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
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) == 0;
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
        boolean writeAsString = (context.features & (MASK_WRITE_NON_STRING_VALUE_AS_STRING | WriteLongAsString.mask)) == 0;
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
                    BYTES.putLongLE(chars, off, v0);
                    BYTES.putLongLE(chars, off + 4, v1);
                    i += 8;
                    off += 8;
                    continue;
                }
            }
            char c = BYTES.getChar(value, i++);
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

    public abstract static class IO {
        public static int startObject(JSONWriterUTF16 writer, char[] buf, int off) {
            int level = writer.level++;
            if (level > writer.context.maxLevel) {
                throw overflowLevel(level);
            }

            writer.startObject = true;

            buf[off++] = '{';

            if (writer.pretty != PRETTY_NON) {
                off = writer.indent(buf, off);
            }
            return off;
        }

        public static int endObject(JSONWriterUTF16 writer, char[] buf, int off) {
            writer.level--;
            if (writer.pretty != PRETTY_NON) {
                off = writer.indent(buf, off);
            }

            buf[off] = '}';
            writer.startObject = false;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, byte value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }
            off = IOUtils.writeInt8(buf, off, value);
            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Byte value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.byteValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, byte[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            buf[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                if (writeAsString) {
                    buf[off++] = quote;
                }
                off = IOUtils.writeInt8(buf, off, value[i]);
                if (writeAsString) {
                    buf[off++] = quote;
                }
            }
            buf[off] = ']';

            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, short value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }
            off = IOUtils.writeInt16(buf, off, value);
            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Short value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.shortValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, int value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            if (writeAsString) {
                buf[off++] = quote;
            }
            off = IOUtils.writeInt32(buf, off, value);
            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Integer value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.intValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, int[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            buf[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                if (writeAsString) {
                    buf[off++] = quote;
                }
                off = IOUtils.writeInt32(buf, off, value[i]);
                if (writeAsString) {
                    buf[off++] = quote;
                }
            }
            buf[off] = ']';

            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, long value, long features) {
            char quote = writer.quote;

            boolean writeAsString = isWriteAsString(value, features);
            if (writeAsString) {
                buf[off++] = quote;
            }
            off = IOUtils.writeInt64(buf, off, value);
            if (writeAsString) {
                buf[off++] = quote;
            } else if ((features & MASK_WRITE_CLASS_NAME) != 0
                    && (features & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                    && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
            ) {
                buf[off++] = 'L';
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Long value, long features) {
            if (value == null) {
                return writeLongNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.longValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, long[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            char quote = writer.quote;

            buf[off++] = '[';

            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                long v = value[i];
                boolean writeAsString = isWriteAsString(v, features);
                if (writeAsString) {
                    buf[off++] = quote;
                }
                off = IOUtils.writeInt64(buf, off, v);
                if (writeAsString) {
                    buf[off++] = quote;
                }
            }

            buf[off] = ']';

            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, BigInteger value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            if (isInt64(value) && features == 0) {
                return writeValue(writer, buf, off, value.longValue(), features);
            }

            String str = value.toString(10);

            boolean writeAsString = isWriteAsString(value, features);

            int strlen = str.length();
            if (writeAsString) {
                buf[off++] = '"';
            }
            str.getChars(0, strlen, buf, off);
            off += strlen;
            if (writeAsString) {
                buf[off++] = '"';
            }
            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, BigDecimal value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }

            char quote = writer.quote;
            boolean writeAsString = isWriteAsString(value, features);
            if (writeAsString) {
                buf[off++] = quote;
            }

            boolean asPlain = (features & WriteBigDecimalAsPlain.mask) != 0;
            long unscaleValue;
            int scale;
            int precision = value.precision();
            if (precision < 19
                    && (scale = value.scale()) >= 0
                    && DECIMAL_INT_COMPACT != null
                    && (unscaleValue = DECIMAL_INT_COMPACT.applyAsLong(value)) != Long.MIN_VALUE
                    && !asPlain
            ) {
                off = IOUtils.writeDecimal(buf, off, unscaleValue, scale);
            } else {
                String str = asPlain ? value.toPlainString() : value.toString();
                str.getChars(0, str.length(), buf, off);
                off += str.length();
            }

            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, float value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }

            off = NumberUtils.writeFloat(buf, off, value, true, writeSpecialAsString);

            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Float value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.floatValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, float[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;
            buf[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }

                if (!Float.isFinite(value[i])) {
                    off = NumberUtils.writeFloat(buf, off, value[i], true, writeSpecialAsString);
                    continue;
                }
                if (writeAsString) {
                    buf[off++] = quote;
                }
                off = NumberUtils.writeFloat(buf, off, value[i], true, false);
                if (writeAsString) {
                    buf[off++] = quote;
                }
            }
            buf[off] = ']';

            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, double value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }

            off = NumberUtils.writeDouble(buf, off, value, true, writeSpecialAsString);

            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Double value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.doubleValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, double[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & MASK_WRITE_FLOAT_SPECIAL_AS_STRING) != 0;
            buf[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }

                if (!Double.isFinite(value[i])) {
                    off = NumberUtils.writeDouble(buf, off, value[i], true, writeSpecialAsString);
                    continue;
                }
                if (writeAsString) {
                    buf[off++] = quote;
                }
                off = NumberUtils.writeDouble(buf, off, value[i], true, false);
                if (writeAsString) {
                    buf[off++] = quote;
                }
            }
            buf[off] = ']';

            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, LocalDate value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalDate(buf, off + 1, value.getYear(), value.getMonthValue(), value.getDayOfMonth());
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, LocalTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalTime(buf, off + 1, value);
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, LocalDateTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            buf[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(buf, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            buf[off] = ' ';
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, OffsetDateTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            LocalDateTime ldt = value.toLocalDateTime();
            LocalDate date = ldt.toLocalDate();
            buf[off] = quote;
            off = IOUtils.writeLocalDate(buf, off + 1, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            buf[off] = 'T';
            off = IOUtils.writeLocalTime(buf, off + 1, ldt.toLocalTime());

            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                buf[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getChars(0, zoneId.length(), buf, off);
                off += zoneId.length();
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, OffsetTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());

            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                buf[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getChars(0, zoneId.length(), buf, off);
                off += zoneId.length();
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, ZonedDateTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            ZoneId zone = value.getZone();
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

            char quote = writer.quote;
            buf[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(buf, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            buf[off] = 'T';
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());
            if (zoneSize == 1) {
                buf[off++] = 'Z';
            } else if (firstZoneChar == '+' || firstZoneChar == '-') {
                zoneId.getChars(0, zoneIdLength, buf, off);
                off += zoneIdLength;
            } else {
                buf[off++] = '[';
                zoneId.getChars(0, zoneIdLength, buf, off);
                off += zoneIdLength;
                buf[off++] = ']';
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Instant value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            char quote = writer.quote;
            buf[off] = quote;
            String str = DateTimeFormatter.ISO_INSTANT.format(value);
            str.getChars(0, str.length(), buf, off + 1);
            off += str.length() + 1;
            buf[off] = quote;
            return off + 1;
        }

        public static int writeLongNull(JSONWriterUTF16 writer, char[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & (MASK_WRITE_NON_STRING_VALUE_AS_STRING | MASK_WRITE_LONG_AS_STRING)) != 0) {
                    char quote = writer.quote;
                    buf[off] = quote;
                    buf[off + 1] = '0';
                    buf[off + 2] = quote;
                    return off + 3;
                } else {
                    buf[off] = '0';
                    return off + 1;
                }
            } else {
                return writeNull(buf, off);
            }
        }

        public static int writeDoubleNull(JSONWriterUTF16 writer, char[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
                    char quote = writer.quote;
                    buf[off] = quote;
                    buf[off + 1] = '0';
                    buf[off + 2] = '.';
                    buf[off + 3] = '0';
                    buf[off + 4] = quote;
                    return off + 5;
                } else {
                    buf[off] = '0';
                    buf[off + 1] = '.';
                    buf[off + 2] = '0';
                    return off + 3;
                }
            } else {
                return writeNull(buf, off);
            }
        }

        public static int writeNumberNull(JSONWriterUTF16 writer, char[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
                    char quote = writer.quote;
                    buf[off] = quote;
                    buf[off + 1] = '0';
                    buf[off + 2] = quote;
                    return off + 3;
                } else {
                    buf[off] = '0';
                    return off + 1;
                }
            } else {
                return writeNull(buf, off);
            }
        }

        public static int writeArrayNull(char[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_LIST_AS_EMPTY)) != 0) {
                return writeEmptyArray(buf, off);
            } else {
                return writeNull(buf, off);
            }
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Boolean value, long features) {
            if (value == null) {
                return writeBooleanNull(buf, off, features);
            }
            return writeValue(writer, buf, off, value.booleanValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, boolean value, long features) {
            char quote = writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }
            if ((features & MASK_WRITE_BOOLEAN_AS_NUMBER) != 0) {
                buf[off++] = value ? '1' : '0';
            } else {
                off = IOUtils.putBoolean(buf, off, value);
            }
            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Character value, long features) {
            if (value == null) {
                return writeStringNull(buf, off, features);
            }
            return writeValue(writer, buf, off, value.charValue(), features);
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, char value, long features) {
            char quote = writer.quote;
            buf[off++] = quote;
            switch (value) {
                case '"':
                case '\'':
                    if (value == quote) {
                        buf[off++] = '\\';
                    }
                    buf[off++] = value;
                    break;
                case '\\':
                case '\r':
                case '\n':
                case '\b':
                case '\f':
                case '\t':
                    StringUtils.writeEscapedChar(buf, off, value);
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
                    buf[off] = '\\';
                    buf[off + 1] = 'u';
                    buf[off + 2] = '0';
                    buf[off + 3] = '0';
                    buf[off + 4] = '0';
                    buf[off + 5] = (char) ('0' + (int) value);
                    off += 6;
                    break;
                case 11:
                case 14:
                case 15:
                    buf[off] = '\\';
                    buf[off + 1] = 'u';
                    buf[off + 2] = '0';
                    buf[off + 3] = '0';
                    buf[off + 4] = '0';
                    buf[off + 5] = (char) ('a' + (value - 10));
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
                    buf[off] = '\\';
                    buf[off + 1] = 'u';
                    buf[off + 2] = '0';
                    buf[off + 3] = '0';
                    buf[off + 4] = '1';
                    buf[off + 5] = (char) ('0' + (value - 16));
                    off += 6;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    buf[off] = '\\';
                    buf[off + 1] = 'u';
                    buf[off + 2] = '0';
                    buf[off + 3] = '0';
                    buf[off + 4] = '1';
                    buf[off + 5] = (char) ('a' + (value - 26));
                    off += 6;
                    break;
                default:
                    buf[off++] = value;
                    break;
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, char[] buf, int off, UUID value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            long msb = value.getMostSignificantBits();
            long lsb = value.getLeastSignificantBits();

            char quote = writer.quote;

            buf[off] = quote;
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
            buf[off + 37] = quote;

            return off + 38;
        }

        public static int valueSize(boolean value) {
            return 5;
        }

        public static int valueSize(byte value) {
            return 5;
        }

        public static int valueSize(Byte value) {
            return 5;
        }

        public static int valueSize(short value) {
            return 7;
        }

        public static int valueSize(Short value) {
            return 7;
        }

        public static int valueSize(int value) {
            return 13;
        }

        public static int valueSize(Integer value) {
            return 13;
        }

        public static int valueSize(long value) {
            return 23;
        }

        public static int valueSize(Long value) {
            return 23;
        }

        public static int valueSize(float value) {
            return 17;
        }

        public static int valueSize(double value) {
            return 26;
        }

        public static int valueSize(UUID value) {
            return 38;
        }

        public static int valueSize(float[] value) {
            return value == null ? 4 : value.length * (17 /* float value size */ + 1);
        }

        public static int valueSize(double[] value) {
            return value == null ? 4 : value.length * (26 /* double value size */ + 1);
        }

        public static int valueSize(int[] value) {
            return value == null ? 4 : value.length * (13 /* int value size */ + 1);
        }

        public static int valueSize(byte[] value) {
            return value == null ? 4 : value.length * (23 /* long value size */ + 1);
        }

        public static int valueSize(long[] value) {
            return value == null ? 4 : value.length * (23 /* long value size */ + 1);
        }

        public static int valueSize(LocalDate value) {
            return 18;
        }

        public static int valueSize(LocalDateTime value) {
            return 38;
        }

        public static int valueSize(LocalTime value) {
            return 20;
        }

        public static int valueSize(OffsetDateTime value) {
            return 45;
        }

        public static int valueSize(OffsetTime value) {
            return 28;
        }

        public static int valueSize(ZonedDateTime value) {
            if (value == null) {
                return 4;
            }
            return value.getZone().getId().length() + 38;
        }

        public static int valueSize(BigInteger value) {
            return IOUtils.stringSize(value);
        }

        public static int valueSize(BigDecimal value) {
            return IOUtils.stringSize(value);
        }

        public static int nameSize(JSONWriterUTF8 writer, byte[] name) {
            return name.length + 2 + writer.pretty * writer.level;
        }

        public static int stringCapacity(String value) {
            if (value == null) {
                return 4;
            }
            return value.length() * 6 + 2;
        }

        public static int stringCapacity(String[] value) {
            if (value == null) {
                return 4;
            }
            int size = value.length + 2;
            for (String str : value) {
                size += str == null ? 4 : str.length() * 6 + 2;
            }
            return size;
        }

        public static int stringCapacity(Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.size();
            for (String string : strings) {
                size += stringCapacity(string);
            }
            return size;
        }

        public static int stringCapacity(List<String> strings) {
            if (strings == null) {
                return 1;
            }
            int stringsSize = strings.size();
            int size = stringsSize;
            for (int i = 0; i < stringsSize; i++) {
                String value = strings.get(i);
                size += value == null ? 4 : value.length() * 6 + 2;
            }
            return size;
        }

        public static int int64Capacity(Collection<Long> values) {
            return values == null ? 4 : values.size() * (23 /* long value size */ + 1);
        }

        public static int enumCapacity(Enum value, long features) {
            if (value == null) {
                return 4;
            }
            return value.name().length() * 6 + 2;
        }

        public static int valueSizeString(byte[] value, byte coder, boolean escaped) {
            if (value == null) {
                return 4;
            }
            int multi;
            if (coder == 0) {
                multi = escaped ? 1 : 6;
            } else {
                multi = escaped ? 3 : 6;
            }
            return value.length * multi + 2;
        }

        public static char[] buffer(JSONWriterUTF16 writer) {
            return writer.chars;
        }

        public static char[] ensureCapacity(JSONWriterUTF16 writer, char[] buf, int minCapacity) {
            buf = Arrays.copyOf(buf, writer.newCapacity(minCapacity, buf.length));
            writer.chars = buf;
            return buf;
        }

        public static char[] ensureCapacity(JSONWriterUTF16 writer, int minCapacity) {
            char[] buf = writer.chars;
            if (minCapacity > buf.length) {
                buf = ensureCapacity(writer, buf, minCapacity);
            }
            return buf;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, Enum value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            String str;
            if ((features & MASK_WRITE_ENUM_USING_TO_STRING) == 0) {
                final int ordinal = value.ordinal();
                if ((features & MASK_WRITE_ENUM_USING_ORDINAL) != 0) {
                    return writeValue(writer, buf, off, ordinal, features);
                }
                str = value.name();
            } else {
                str = value.toString();
            }

            char quote = writer.quote;
            buf[off++] = quote;
            str.getChars(0, str.length(), buf, off);
            off += str.length();
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValueJDK11(JSONWriterUTF16 writer, char[] buf, int off, List<String> values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }

            buf[off++] = '[';
            for (int i = 0; i < values.size(); i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                String value = values.get(i);
                if (value == null) {
                    off = writeStringNull(buf, off, features);
                } else {
                    byte[] valueBytes = STRING_VALUE.apply(value);
                    if (STRING_CODER.applyAsInt(value) == 0) {
                        off = writeStringLatin1(writer, buf, off, valueBytes, features);
                    } else {
                        off = writeStringUTF16(writer, buf, off, valueBytes, features);
                    }
                }
            }
            buf[off] = ']';

            return off + 1;
        }

        public static int writeValueJDK8(JSONWriterUTF16 writer, char[] buf, int off, List<String> values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }
            buf[off++] = '[';
            for (int i = 0; i < values.size(); i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                off = writeValueJDK8(writer, buf, off, values.get(i), features);
            }
            buf[off] = ']';
            return off + 1;
        }

        public static int writeValueJDK8(JSONWriterUTF16 writer, char[] buf, int off, String str, long features) {
            if (str == null) {
                return writeStringNull(buf, off, features);
            }

            boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;
            boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;
            boolean escape = false;
            final char quote = writer.quote;

            final int strlen = str.length();
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
                buf[off++] = quote;
                str.getChars(0, strlen, buf, off);
                off += strlen;
                buf[off] = quote;
                return off + 1;
            }

            return writeStringEscape(writer, buf, off, str, features);
        }

        public static int writeValueJDK11(JSONWriterUTF16 writer, char[] buf, int off, String[] values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;
            buf[off++] = '[';
            for (int i = 0; i < values.length; i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                String value = values[i];
                if (value == null) {
                    off = writeStringNull(buf, off, features);
                } else {
                    byte[] valueBytes = STRING_VALUE.apply(value);
                    if (STRING_CODER.applyAsInt(value) == 0) {
                        off = writeStringLatin1(writer, buf, off, valueBytes, features);
                    } else {
                        off = writeStringUTF16(writer, buf, off, valueBytes, features);
                    }
                }
            }
            buf[off] = ']';
            return off + 1;
        }

        public static int writeValueJDK11(JSONWriterUTF16 writer, char[] buf, int off, String value, long features) {
            if (value == null) {
                return writeStringNull(buf, off, features);
            }

            byte[] valueBytes = STRING_VALUE.apply(value);
            if (STRING_CODER.applyAsInt(value) == 0) {
                return writeStringLatin1(writer, buf, off, valueBytes, features);
            } else {
                return writeStringUTF16(writer, buf, off, valueBytes, features);
            }
        }

        public static int writeStringLatin1(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            if ((features & MASK_BROWSER_SECURE) != 0) {
                return writeStringLatin1BrowserSecure(writer, chars, off, value, features);
            }

            boolean escape = false;
            char quote = writer.quote;
            chars[off++] = quote;

            int coff = 0;
            final long vecQuote = writer.byteVectorQuote;
            final int upperBound = (value.length - coff) & ~7;
            long vec64;
            for (; coff < upperBound && StringUtils.noneEscaped(vec64 = getLongLE(value, coff), vecQuote); coff += 8) {
                BYTES.putLongLE(chars, off, expand(vec64));
                BYTES.putLongLE(chars, off + 4, expand(vec64 >>> 32));
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
                return off + 1;
            }

            return StringUtils.writeLatin1EscapedRest(chars, off, value, coff, quote, features);
        }

        public static int writeStringLatin1BrowserSecure(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            final int start = off;
            char quote = writer.quote;
            chars[off++] = quote;
            boolean escape = false;
            for (byte c : value) {
                if (c == '\\' || c == quote || c < ' ' || c == '<' || c == '>' || c == '(' || c == ')') {
                    escape = true;
                    break;
                }

                chars[off++] = (char) c;
            }

            if (!escape) {
                chars[off] = quote;
                return off + 1;
            }

            return writeStringEscape(writer, chars, start, value, features);
        }

        protected static int writeStringEscape(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            char quote = writer.quote;
            chars[off++] = quote;
            return StringUtils.writeLatin1EscapedRest(chars, off, value, 0, quote, features);
        }

        protected static int writeStringEscape(JSONWriterUTF16 writer, char[] chars, int off, String str, long features) {
            final int strlen = str.length();
            final char quote = writer.quote;
            boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;
            boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;

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
            return off + 1;
        }

        public static int writeStringUTF16(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            if (value == null) {
                return writeStringNull(chars, off, features);
            }

            if ((features & (BrowserSecure.mask | EscapeNoneAscii.mask)) != 0) {
                return writeStringUTF16BrowserSecure(writer, chars, off, value, features);
            }

            int start = off;
            final long vecQuote = writer.byteVectorQuote;
            char quote = writer.quote;
            boolean escape = false;
            chars[off++] = quote;
            for (int i = 0, char_len = value.length >> 1; i < char_len;) {
                if (i + 8 < char_len) {
                    long v0 = getLongLE(value, i << 1);
                    long v1 = getLongLE(value, (i + 4) << 1);
                    if (((v0 | v1) & 0xFF00FF00FF00FF00L) == 0 && StringUtils.noneEscaped((v0 << 8) | v1, vecQuote)) {
                        BYTES.putLongLE(chars, off, v0);
                        BYTES.putLongLE(chars, off + 4, v1);
                        i += 8;
                        off += 8;
                        continue;
                    }
                }
                char c = BYTES.getChar(value, i++);
                if (c == '\\' || c == quote || c < ' ') {
                    escape = true;
                    break;
                }

                chars[off++] = c;
            }

            if (!escape) {
                chars[off] = quote;
                return off + 1;
            }

            return writeStringEscapeUTF16(writer, chars, start, value, features);
        }

        static int writeStringUTF16BrowserSecure(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            boolean escapeNoneAscii = (features & EscapeNoneAscii.mask) != 0;

            boolean escape = false;
            char quote = writer.quote;
            chars[off++] = quote;
            for (int i = 0, char_len = value.length >> 1; i < char_len; i++) {
                char c = BYTES.getChar(value, i);
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
                return off + 1;
            }

            return writeStringEscapeUTF16(writer, chars, off, value, features);
        }

        protected static int writeStringEscapeUTF16(JSONWriterUTF16 writer, char[] chars, int off, byte[] value, long features) {
            final int strlen = value.length >> 1;
            final char quote = writer.quote;
            boolean escapeNoneAscii = (features & EscapeNoneAscii.mask) != 0;
            boolean browserSecure = (features & BrowserSecure.mask) != 0;

            chars[off++] = quote;
            for (int charIndex = 0; charIndex < strlen; charIndex++) {
                char ch = UNSAFE.getChar(value, (long) Unsafe.ARRAY_CHAR_BASE_OFFSET + ((long) charIndex << 1));
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
            return off + 1;
        }

        public static int writeNameBefore(JSONWriterUTF16 writer, char[] buf, int off) {
            if (writer.startObject) {
                writer.startObject = false;
            } else {
                buf[off++] = ',';
                if (writer.pretty != PRETTY_NON) {
                    off = writer.indent(buf, off);
                }
            }
            return off;
        }

        public static int writeValue(JSONWriterUTF16 writer, char[] buf, int off, UUID value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            long msb = value.getMostSignificantBits();
            long lsb = value.getLeastSignificantBits();

            char quote = writer.quote;

            buf[off] = quote;
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
            buf[off + 37] = quote;
            return off + 38;
        }

        public static int writeName(JSONWriterUTF16 writer, char[] buf, int off, char[] name) {
            if (writer.startObject) {
                writer.startObject = false;
            } else {
                buf[off++] = ',';
                if (writer.pretty != PRETTY_NON) {
                    off = writer.indent(buf, off);
                }
            }
            System.arraycopy(name, 0, buf, off, name.length);
            return off + name.length;
        }

        public static int writeBooleanNull(char[] buf, int off, long features) {
            String raw = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_BOOLEAN_AS_FALSE)) != 0 ? "false" : "null";
            raw.getChars(0, raw.length(), buf, off);
            return off + raw.length();
        }

        public static int writeStringNull(char[] buf, int off, long features) {
            String raw;
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) != 0) {
                raw = (features & MASK_USE_SINGLE_QUOTES) != 0 ? "''" : "\"\"";
            } else {
                raw = "null";
            }
            raw.getChars(0, raw.length(), buf, off);
            return off + raw.length();
        }

        public static int writeNull(char[] buf, int off) {
            IOUtils.putNULL(buf, off);
            return off + 4;
        }

        public static int writeEmptyArray(char[] buf, int off) {
            buf[off] = '[';
            buf[off + 1] = ']';
            return off + 2;
        }
    }
}

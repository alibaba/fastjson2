package com.alibaba.fastjson2;

import com.alibaba.fastjson2.time.*;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONWriterUTF16
        extends JSONWriter {
    protected char[] chars;
    final CacheItem cacheItem;

    JSONWriterUTF16(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_16);
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        char[] chars = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (chars == null) {
            chars = new char[8192];
        }
        this.chars = chars;
    }

    @Override
    public void flushTo(java.io.Writer to) {
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
    public void close() {
        char[] chars = this.chars;
        if (chars.length > CACHE_THRESHOLD) {
            return;
        }

        CHARS_UPDATER.lazySet(cacheItem, chars);
    }

    @Override
    protected void write0(char c) {
        int off = this.off;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off] = c;
        this.off = off + 1;
    }

    @Override
    public void writeColon() {
        int off = this.off;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off] = ':';
        this.off = off + 1;
    }

    @Override
    public void startObject() {
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
    public void endObject() {
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
    public void writeComma() {
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
    public void startArray() {
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
    public void endArray() {
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

    public void writeString(List<String> list) {
        if (pretty != PRETTY_NON) {
            super.writeString(list);
            return;
        }

        // startArray();
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '[';

        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                if (off == chars.length) {
                    ensureCapacity(off + 1);
                }
                chars[off++] = ',';
            }

            String str = list.get(i);
            writeString(str);
        }

        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ']';
    }

    public void writeStringLatin1(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final int start = off;
        final char[] chars = this.chars;
        chars[off++] = quote;

        for (int i = 0; i < value.length; i++) {
            byte c = value[i];
            if (c == '\\' || c == quote || c < ' ') {
                escape = true;
                break;
            }

            if (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')')) {
                escape = true;
                break;
            }

            chars[off++] = (char) c;
        }

        if (!escape) {
            chars[off++] = quote;
            this.off = off;
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

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;

        for (int i = 0; i < value.length; i += 2) {
            char c = UNSAFE.getChar(value, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + i);
            if (c == '\\'
                    || c == quote
                    || c < ' '
                    || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
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

        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;
        final char quote = this.quote;

        final int strlen = str.length();
        int minCapacity = off + strlen + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
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

    @Override
    public void writeString(boolean value) {
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
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) == 0;
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
            ensureCapacity(off + 1);
        }
        chars[off++] = quote;
    }

    protected final void writeStringEscape(String str) {
        final int strlen = str.length();
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacity(off + strlen * 6 + 2);

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
                    chars[off] = '\\';
                    chars[off + 1] = ch;
                    off += 2;
                    break;
                case '\r':
                    chars[off] = '\\';
                    chars[off + 1] = 'r';
                    off += 2;
                    break;
                case '\n':
                    chars[off] = '\\';
                    chars[off + 1] = 'n';
                    off += 2;
                    break;
                case '\b':
                    chars[off] = '\\';
                    chars[off + 1] = 'b';
                    off += 2;
                    break;
                case '\f':
                    chars[off] = '\\';
                    chars[off + 1] = 'f';
                    off += 2;
                    break;
                case '\t':
                    chars[off] = '\\';
                    chars[off + 1] = 't';
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
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = '0';
                        chars[off + 3] = '0';
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = DIGITS[(ch >>> 12) & 15];
                        chars[off + 3] = DIGITS[(ch >>> 8) & 15];
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
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
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacity(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < strlen; i += 2) {
            char ch = UNSAFE.getChar(str, (long) Unsafe.ARRAY_CHAR_BASE_OFFSET + i);
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                    chars[off] = '\\';
                    chars[off + 1] = ch;
                    off += 2;
                    break;
                case '\r':
                    chars[off] = '\\';
                    chars[off + 1] = 'r';
                    off += 2;
                    break;
                case '\n':
                    chars[off] = '\\';
                    chars[off + 1] = 'n';
                    off += 2;
                    break;
                case '\b':
                    chars[off] = '\\';
                    chars[off + 1] = 'b';
                    off += 2;
                    break;
                case '\f':
                    chars[off] = '\\';
                    chars[off + 1] = 'f';
                    off += 2;
                    break;
                case '\t':
                    chars[off] = '\\';
                    chars[off + 1] = 't';
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
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = '0';
                        chars[off + 3] = '0';
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = DIGITS[(ch >>> 12) & 15];
                        chars[off + 3] = DIGITS[(ch >>> 8) & 15];
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
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
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacity(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < strlen; ++i) {
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
                    chars[off] = '\\';
                    chars[off + 1] = ch;
                    off += 2;
                    break;
                case '\r':
                    chars[off] = '\\';
                    chars[off + 1] = 'r';
                    off += 2;
                    break;
                case '\n':
                    chars[off] = '\\';
                    chars[off + 1] = 'n';
                    off += 2;
                    break;
                case '\b':
                    chars[off] = '\\';
                    chars[off + 1] = 'b';
                    off += 2;
                    break;
                case '\f':
                    chars[off] = '\\';
                    chars[off + 1] = 'f';
                    off += 2;
                    break;
                case '\t':
                    chars[off] = '\\';
                    chars[off + 1] = 't';
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
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = '0';
                        chars[off + 3] = '0';
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = DIGITS[(ch >>> 12) & 15];
                        chars[off + 3] = DIGITS[(ch >>> 8) & 15];
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
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
        final int strlen = str.length;
        final char quote = this.quote;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int off = this.off;
        ensureCapacity(off + strlen * 6 + 2);

        final char[] chars = this.chars;
        chars[off++] = quote;
        for (int i = 0; i < strlen; ++i) {
            char ch = (char) (str[i] & 0xff);
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                    chars[off] = '\\';
                    chars[off + 1] = ch;
                    off += 2;
                    break;
                case '\r':
                    chars[off] = '\\';
                    chars[off + 1] = 'r';
                    off += 2;
                    break;
                case '\n':
                    chars[off] = '\\';
                    chars[off + 1] = 'n';
                    off += 2;
                    break;
                case '\b':
                    chars[off] = '\\';
                    chars[off + 1] = 'b';
                    off += 2;
                    break;
                case '\f':
                    chars[off] = '\\';
                    chars[off + 1] = 'f';
                    off += 2;
                    break;
                case '\t':
                    chars[off] = '\\';
                    chars[off + 1] = 't';
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
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = '0';
                        chars[off + 3] = '0';
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
                        off += 6;
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = '0';
                        chars[off + 3] = '0';
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
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

    @Override
    public void writeString(char[] str, int offset, int len, boolean quoted) {
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        final char quote = this.quote;
        int off = this.off;
        int minCapacity = quoted ? off + 2 : off;
        if (escapeNoneAscii) {
            minCapacity += len * 6;
        } else {
            minCapacity += len * 2;
        }

        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
                    chars[off] = '\\';
                    chars[off + 1] = ch;
                    off += 2;
                    break;
                case '\r':
                    chars[off] = '\\';
                    chars[off + 1] = 'r';
                    off += 2;
                    break;
                case '\n':
                    chars[off] = '\\';
                    chars[off + 1] = 'n';
                    off += 2;
                    break;
                case '\b':
                    chars[off] = '\\';
                    chars[off + 1] = 'b';
                    off += 2;
                    break;
                case '\f':
                    chars[off] = '\\';
                    chars[off + 1] = 'f';
                    off += 2;
                    break;
                case '\t':
                    chars[off] = '\\';
                    chars[off + 1] = 't';
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
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off] = '\\';
                        chars[off + 1] = 'u';
                        chars[off + 2] = DIGITS[(ch >>> 12) & 15];
                        chars[off + 3] = DIGITS[(ch >>> 8) & 15];
                        chars[off + 4] = DIGITS[(ch >>> 4) & 15];
                        chars[off + 5] = DIGITS[ch & 15];
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

    public void writeString(String[] strings) {
        if (strings == null) {
            writeArrayNull();
            return;
        }

        startArray();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                writeComma();
            }

            String item = strings[i];
            if (item == null) {
                if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                    writeString("");
                } else {
                    writeNull();
                }
                continue;
            }
            writeString(item);
        }
        endArray();
    }

    @Override
    public void writeReference(String path) {
        this.lastReference = path;
        int off = this.off;
        char[] chars = this.chars;
        if (off + 9 > chars.length) {
            chars = grow(off + 9);
        }
        chars[off] = '{';
        chars[off + 1] = '"';
        chars[off + 2] = '$';
        chars[off + 3] = 'r';
        chars[off + 4] = 'e';
        chars[off + 5] = 'f';
        chars[off + 6] = '"';
        chars[off + 7] = ':';
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
    public void writeBase64(byte[] bytes) {
        if (bytes == null) {
            writeArrayNull();
            return;
        }

        int charsLen = ((bytes.length - 1) / 3 + 1) << 2; // base64 character count

        int off = this.off;
        ensureCapacity(off + charsLen + 2);

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
    public void writeHex(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }

        int charsLen = bytes.length * 2 + 3;

        int off = this.off;
        ensureCapacity(off + charsLen + 2);
        final char[] chars = this.chars;
        chars[off] = 'x';
        chars[off + 1] = '\'';
        off += 2;

        for (int i = 0; i < bytes.length; ++i) {
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
    public void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        String str = value.toString(10);

        features |= context.features;
        boolean browserCompatible = (features & Feature.BrowserCompatible.mask) != 0
                && (value.compareTo(LOW_BIGINT) < 0 || value.compareTo(HIGH_BIGINT) > 0);
        boolean nonStringAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        boolean writeAsString = browserCompatible || nonStringAsString;

        final int strlen = str.length();
        ensureCapacity(off + strlen + 2);
        final char[] chars = this.chars;
        int off = this.off;
        if (writeAsString) {
            chars[off++] = '"';
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = '"';
        } else {
            str.getChars(0, strlen, chars, off);
            off += strlen;
        }
        this.off = off;
    }

    @Override
    public void writeDecimal(BigDecimal value, long features, DecimalFormat format) {
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
        boolean nonStringAsString = (features & WriteNonStringValueAsString.mask) != 0;
        boolean writeAsString = nonStringAsString
                || ((features & BrowserCompatible.mask) != 0
                && precision >= 16
                && (value.compareTo(LOW) < 0 || value.compareTo(HIGH) > 0));

        int off = this.off;
        int minCapacity = off + precision + Math.abs(value.scale()) + 7;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = '"';
        }

        boolean asPlain = (features & WriteBigDecimalAsPlain.mask) != 0;
        String str = asPlain ? value.toPlainString() : value.toString();
        str.getChars(0, str.length(), chars, off);
        off += str.length();

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        long hi = value.getMostSignificantBits();
        long lo = value.getLeastSignificantBits();

        int minCapacity = off + 38;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] lookup = JSONFactory.UUID_LOOKUP;
        final char[] bytes = this.chars;
        final int off = this.off;
        bytes[off] = '"';
        int i = lookup[((int) (hi >> 56)) & 255];
        int i1 = lookup[((int) (hi >> 48)) & 255];
        int i2 = lookup[((int) (hi >> 40)) & 255];
        int i3 = lookup[((int) (hi >> 32)) & 255];
        int i4 = lookup[(((int) hi) >> 24) & 255];
        int i5 = lookup[(((int) hi) >> 16) & 255];
        int i6 = lookup[(((int) hi) >> 8) & 255];
        int i7 = lookup[((int) hi) & 255];
        int i8 = lookup[(((int) (lo >> 56))) & 255];
        int i9 = lookup[(((int) (lo >> 48))) & 255];
        int i10 = lookup[(((int) (lo >> 40))) & 255];
        int i11 = lookup[((int) (lo >> 32)) & 255];
        int i12 = lookup[(((int) lo) >> 24) & 255];
        int i13 = lookup[(((int) lo) >> 16) & 255];
        int i14 = lookup[(((int) lo) >> 8) & 255];
        int i15 = lookup[((int) lo) & 255];

        bytes[off + 1] = (char) (byte) (i >> 8);
        bytes[off + 2] = (char) (byte) i;
        bytes[off + 3] = (char) (byte) (i1 >> 8);
        bytes[off + 4] = (char) (byte) i1;
        bytes[off + 5] = (char) (byte) (i2 >> 8);
        bytes[off + 6] = (char) (byte) i2;
        bytes[off + 7] = (char) (byte) (i3 >> 8);
        bytes[off + 8] = (char) (byte) i3;
        bytes[off + 9] = '-';
        bytes[off + 10] = (char) (byte) (i4 >> 8);
        bytes[off + 11] = (char) (byte) i4;
        bytes[off + 12] = (char) (byte) (i5 >> 8);
        bytes[off + 13] = (char) (byte) i5;
        bytes[off + 14] = '-';
        bytes[off + 15] = (char) (byte) (i6 >> 8);
        bytes[off + 16] = (char) (byte) i6;
        bytes[off + 17] = (char) (byte) (i7 >> 8);
        bytes[off + 18] = (char) (byte) i7;
        bytes[off + 19] = '-';
        bytes[off + 20] = (char) (byte) (i8 >> 8);
        bytes[off + 21] = (char) (byte) i8;
        bytes[off + 22] = (char) (byte) (i9 >> 8);
        bytes[off + 23] = (char) (byte) i9;
        bytes[off + 24] = '-';
        bytes[off + 25] = (char) (byte) (i10 >> 8);
        bytes[off + 26] = (char) (byte) i10;
        bytes[off + 27] = (char) (byte) (i11 >> 8);
        bytes[off + 28] = (char) (byte) i11;
        bytes[off + 29] = (char) (byte) (i12 >> 8);
        bytes[off + 30] = (char) (byte) i12;
        bytes[off + 31] = (char) (byte) (i13 >> 8);
        bytes[off + 32] = (char) (byte) i13;
        bytes[off + 33] = (char) (byte) (i14 >> 8);
        bytes[off + 34] = (char) (byte) i14;
        bytes[off + 35] = (char) (byte) (i15 >> 8);
        bytes[off + 36] = (char) (byte) i15;
        bytes[off + 37] = '"';
        this.off += 38;
    }

    @Override
    public void writeRaw(String str) {
        ensureCapacity(off + str.length());
        str.getChars(0, str.length(), chars, off);
        off += str.length();
    }

    @Override
    public void writeRaw(char[] chars, int off, int charslen) {
        {
            // inline ensureCapacity
            int minCapacity = this.off + charslen;
            if (minCapacity >= this.chars.length) {
                ensureCapacity(minCapacity);
            }
        }
        System.arraycopy(chars, off, this.chars, this.off, charslen);
        this.off += charslen;
    }

    @Override
    public void writeChar(char ch) {
        int off = this.off;
        int minCapacity = off + 8;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
                chars[off] = '\\';
                chars[off + 1] = ch;
                off += 2;
                break;
            case '\r':
                chars[off] = '\\';
                chars[off + 1] = 'r';
                off += 2;
                break;
            case '\n':
                chars[off] = '\\';
                chars[off + 1] = 'n';
                off += 2;
                break;
            case '\b':
                chars[off] = '\\';
                chars[off + 1] = 'b';
                off += 2;
                break;
            case '\f':
                chars[off] = '\\';
                chars[off + 1] = 'f';
                off += 2;
                break;
            case '\t':
                chars[off] = '\\';
                chars[off + 1] = 't';
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
    public void writeRaw(char ch) {
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ch;
    }

    @Override
    public void writeRaw(char c0, char c1) {
        int off = this.off;
        if (off + 1 >= chars.length) {
            ensureCapacity(off + 2);
        }
        chars[off] = c0;
        chars[off + 1] = c1;
        this.off = off + 2;
    }

    @Override
    public void writeNameRaw(char[] name) {
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

    private int indent(char[] chars, int off) {
        chars[off] = '\n';
        int toIndex = off + 1 + pretty * level;
        Arrays.fill(chars, off + 1, toIndex, pretty == PRETTY_TAB ? '\t' : ' ');
        return toIndex;
    }

    @Override
    public void writeNameRaw(char[] name, int coff, int len) {
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

    void ensureCapacity(int minCapacity) {
        if (minCapacity > chars.length) {
            grow0(minCapacity);
        }
    }

    private char[] grow(int minCapacity) {
        grow0(minCapacity);
        return chars;
    }

    private void grow0(int minCapacity) {
        chars = Arrays.copyOf(chars, newCapacity(minCapacity, chars.length));
    }

    @Override
    public void writeName2Raw(long name) {
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
    public void writeName4Raw(long name) {
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
    public void writeName5Raw(long name) {
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
    public void writeName6Raw(long name) {
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
    public void writeName7Raw(long name) {
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
        chars[off + 8] = quote;
        chars[off + 9] = ':';
        this.off = off + 10;
    }

    @Override
    public void writeName8Raw(long name) {
        int off = this.off;
        int minCapacity = off + 13 + pretty * level;
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
        chars[off + 8] = quote;
        chars[off + 9] = ':';
        this.off = off + 10;
    }

    @Override
    public void writeName9Raw(long name0, int name1) {
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
    public void writeName10Raw(long name0, long name1) {
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
    public void writeName11Raw(long name0, long name1) {
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
    public void writeName12Raw(long name0, long name1) {
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
    public void writeName13Raw(long name0, long name1) {
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
    public void writeName14Raw(long name0, long name1) {
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
    public void writeName15Raw(long name0, long name1) {
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
        chars[off + 16] = quote;
        chars[off + 17] = ':';
        this.off = off + 18;
    }

    @Override
    public void writeName16Raw(long name0, long name1) {
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
        chars[off + 16] = quote;
        chars[off + 17] = ':';
        this.off = off + 18;
    }

    private static void putLong(char[] chars, int off, long name) {
        final long base = ARRAY_CHAR_BASE_OFFSET + (off << 1);
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

    public static void putIntUnaligned(char[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    private static void putLong(char[] chars, int off, long name, int name1) {
        final long base = ARRAY_CHAR_BASE_OFFSET + (off << 1);
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
        final long base = ARRAY_CHAR_BASE_OFFSET + (off << 1);
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

    private static int indent(char[] chars, int off, int indent) {
        chars[off++] = '\n';
        int end = off + indent;
        while (off < end) {
            chars[off++] = '\t';
        }
        return off;
    }

    public void writeInt32(int[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + value.length * 13 + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
    public void writeInt8(byte i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
    public void writeInt16(short i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
    public void writeInt32(int i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 13;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt32(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        }
        this.off = off;
    }

    public void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        long features = context.features;
        boolean browserCompatible = (features & BrowserCompatible.mask) != 0;
        boolean nonStringAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;

        int off = this.off;
        int minCapacity = off + 2 + values.length * 23;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            long v = values[i];
            boolean writeAsString = nonStringAsString
                    || (browserCompatible && v <= 9007199254740991L && v >= -9007199254740991L);
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
    public void writeInt64(long i) {
        final long features = context.features;
        boolean writeAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                || ((features & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));
        int off = this.off;
        int minCapacity = off + 23;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = quote;
        }
        off = IOUtils.writeInt64(chars, off, i);
        if (writeAsString) {
            chars[off++] = quote;
        } else if ((features & WriteClassName.mask) != 0
                && (features & NotWriteNumberClassName.mask) == 0
                && i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE
        ) {
            chars[off++] = 'L';
        }
        this.off = off;
    }

    @Override
    public void writeFloat(float value) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        if (writeSpecialAsString && !Float.isFinite(value)) {
            writeAsString = false;
        }

        int off = this.off;
        int minCapacity = off + 15;
        if (writeAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, chars, off, true, writeSpecialAsString);
        off += len;

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public void writeFloat(float[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * (writeAsString ? 16 : 18) + 1;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }

            if (!Float.isFinite(values[i])) {
                int len = DoubleToDecimal.toString(values[i], chars, off, true, writeSpecialAsString);
                off += len;
            } else {
                if (writeAsString) {
                    chars[off++] = '"';
                }
                int len = DoubleToDecimal.toString(values[i], chars, off, true, false);
                off += len;
                if (writeAsString) {
                    chars[off++] = '"';
                }
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public void writeDouble(double value) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        if (writeSpecialAsString && !Double.isFinite(value)) {
            writeAsString = false;
        }

        int off = this.off;
        int minCapacity = off + 24;
        if (writeAsString) {
            minCapacity += 2;
        }

        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, chars, off, true, writeSpecialAsString);
        off += len;

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public void writeDoubleArray(double value0, double value1) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 48 + 3;
        if (writeAsString) {
            minCapacity += 4;
        }

        ensureCapacity(minCapacity);

        final char[] chars = this.chars;
        chars[off++] = '[';

        if (!Double.isFinite(value0)) {
            int len0 = DoubleToDecimal.toString(value0, chars, off, true, writeSpecialAsString);
            off += len0;
        } else {
            if (writeAsString) {
                chars[off++] = '"';
            }
            int len0 = DoubleToDecimal.toString(value0, chars, off, true, false);
            off += len0;
            if (writeAsString) {
                chars[off++] = '"';
            }
        }

        chars[off++] = ',';

        if (!Double.isFinite(value1)) {
            int len1 = DoubleToDecimal.toString(value1, chars, off, true, writeSpecialAsString);
            off += len1;
        } else {
            if (writeAsString) {
                chars[off++] = '"';
            }
            int len1 = DoubleToDecimal.toString(value1, chars, off, true, false);
            off += len1;
            if (writeAsString) {
                chars[off++] = '"';
            }
        }

        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 27 + 1;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }

            if (!Double.isFinite(values[i])) {
                int len = DoubleToDecimal.toString(values[i], chars, off, true, writeSpecialAsString);
                off += len;
            } else {
                if (writeAsString) {
                    chars[off++] = '"';
                }
                int len = DoubleToDecimal.toString(values[i], chars, off, true, false);
                off += len;
                if (writeAsString) {
                    chars[off++] = '"';
                }
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        int off = this.off;
        int minCapacity = off + 16;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] bytes = this.chars;
        bytes[off] = quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        bytes[off + 1] = (char) (byte) (q + '0');
        bytes[off + 2] = (char) (byte) (v >> 16);
        bytes[off + 3] = (char) (byte) (v >> 8);
        bytes[off + 4] = (char) (byte) v;
        v = DIGITS_K[month];
        bytes[off + 5] = (char) (byte) (v >> 8);
        bytes[off + 6] = (char) (byte) v;
        v = DIGITS_K[dayOfMonth];
        bytes[off + 7] = (char) (byte) (v >> 8);
        bytes[off + 8] = (char) (byte) v;
        v = DIGITS_K[hour];
        bytes[off + 9] = (char) (byte) (v >> 8);
        bytes[off + 10] = (char) (byte) v;
        v = DIGITS_K[minute];
        bytes[off + 11] = (char) (byte) (v >> 8);
        bytes[off + 12] = (char) (byte) v;
        v = DIGITS_K[second];
        bytes[off + 13] = (char) (byte) (v >> 8);
        bytes[off + 14] = (char) (byte) v;
        bytes[off + 15] = quote;
        this.off = off + 16;
    }

    @Override
    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        ensureCapacity(off + 21);

        final char[] chars = this.chars;
        int off = this.off;
        chars[off] = quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        chars[off + 1] = (char) (byte) (q + '0');
        chars[off + 2] = (char) (byte) (v >> 16);
        chars[off + 3] = (char) (byte) (v >> 8);
        chars[off + 4] = (char) (byte) v;
        chars[off + 5] = '-';
        v = DIGITS_K[month];
        chars[off + 6] = (char) (byte) (v >> 8);
        chars[off + 7] = (char) (byte) v;
        chars[off + 8] = '-';
        v = DIGITS_K[dayOfMonth];
        chars[off + 9] = (char) (byte) (v >> 8);
        chars[off + 10] = (char) (byte) v;
        chars[off + 11] = ' ';
        v = DIGITS_K[hour];
        chars[off + 12] = (char) (byte) (v >> 8);
        chars[off + 13] = (char) (byte) v;
        chars[off + 14] = ':';
        v = DIGITS_K[minute];
        chars[off + 15] = (char) (byte) (v >> 8);
        chars[off + 16] = (char) (byte) v;
        chars[off + 17] = ':';
        v = DIGITS_K[second];
        chars[off + 18] = (char) (byte) (v >> 8);
        chars[off + 19] = (char) (byte) v;
        chars[off + 20] = (char) (byte) quote;
        this.off = off + 21;
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        int off = this.off;
        int minCapacity = off + 38;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        LocalDate localDate = dateTime.date;
        off = IOUtils.writeLocalDate(chars, off, localDate.year, localDate.monthValue, localDate.dayOfMonth);
        chars[off++] = ' ';
        off = IOUtils.writeLocalTime(chars, off, dateTime.time);
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public void writeDateTimeISO8601(
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
        if (off + minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] bytes = this.chars;
        bytes[off] = quote;
        off = IOUtils.writeInt32(bytes, off + 1, year);
        bytes[off] = '-';
        int v = DIGITS_K[month];
        bytes[off + 1] = (char) (byte) (v >> 8);
        bytes[off + 2] = (char) (byte) v;
        bytes[off + 3] = '-';
        v = DIGITS_K[dayOfMonth];
        bytes[off + 4] = (char) (byte) (v >> 8);
        bytes[off + 5] = (char) (byte) v;
        bytes[off + 6] = (char) (byte) (timeZone ? 'T' : ' ');
        v = DIGITS_K[hour];
        bytes[off + 7] = (char) (byte) (v >> 8);
        bytes[off + 8] = (char) (byte) v;
        bytes[off + 9] = ':';
        v = DIGITS_K[minute];
        bytes[off + 10] = (char) (byte) (v >> 8);
        bytes[off + 11] = (char) (byte) v;
        bytes[off + 12] = ':';
        v = DIGITS_K[second];
        bytes[off + 13] = (char) (byte) (v >> 8);
        bytes[off + 14] = (char) (byte) v;
        off += 15;

        if (millis > 0) {
            bytes[off++] = '.';
            int div = millis / 10;
            int div2 = div / 10;
            final int rem1 = millis - div * 10;

            if (rem1 != 0) {
                v = DIGITS_K[millis];
                bytes[off] = (char) (byte) (v >> 16);
                bytes[off + 1] = (char) (byte) (v >> 8);
                bytes[off + 2] = (char) (byte) v;
                off += 3;
            } else {
                final int rem2 = div - div2 * 10;
                if (rem2 != 0) {
                    v = DIGITS_K[div];
                    bytes[off] = (char) (byte) (v >> 8);
                    bytes[off + 1] = (char) (byte) v;
                    off += 2;
                } else {
                    bytes[off++] = (char) (byte) (div2 + '0');
                }
            }
        }

        if (timeZone) {
            int offset = offsetSeconds / 3600;
            if (offsetSeconds == 0) {
                bytes[off++] = 'Z';
            } else {
                int offsetAbs = Math.abs(offset);
                bytes[off] = offset >= 0 ? '+' : '-';
                v = DIGITS_K[offsetAbs];
                bytes[off + 1] = (char) (byte) (v >> 8);
                bytes[off + 2] = (char) (byte) v;
                bytes[off + 3] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                v = DIGITS_K[offsetMinutes];
                bytes[off + 4] = (char) (byte) (v >> 8);
                bytes[off + 5] = (char) (byte) v;
                off += 6;
            }
        }
        bytes[off] = quote;
        this.off = off + 1;
    }

    @Override
    public void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 10;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off] = quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        chars[off + 1] = (char) (byte) (q + '0');
        chars[off + 2] = (char) (byte) (v >> 16);
        chars[off + 3] = (char) (byte) (v >> 8);
        chars[off + 4] = (char) (byte) v;
        v = DIGITS_K[month];
        chars[off + 5] = (char) (byte) (v >> 8);
        chars[off + 6] = (char) (byte) v;
        v = DIGITS_K[dayOfMonth];
        chars[off + 7] = (char) (byte) (v >> 8);
        chars[off + 8] = (char) (byte) v;
        chars[off + 9] = quote;
        this.off = off + 10;
    }

    @Override
    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 13;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
        chars[off] = quote;
        this.off = off + 1;
    }

    @Override
    public void writeTimeHHMMSS8(int hour, int minute, int second) {
        int off = this.off;
        int minCapacity = off + 10;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off] = (char) (byte) quote;
        int v = DIGITS_K[hour];
        chars[off + 1] = (char) (byte) (v >> 8);
        chars[off + 2] = (char) (byte) v;
        chars[off + 3] = ':';
        v = DIGITS_K[minute];
        chars[off + 4] = (char) (byte) (v >> 8);
        chars[off + 5] = (char) (byte) v;
        chars[off + 6] = ':';
        v = DIGITS_K[second];
        chars[off + 7] = (char) (byte) (v >> 8);
        chars[off + 8] = (char) (byte) v;
        chars[off + 9] = (char) (byte) quote;
        this.off = off + 10;
    }

    @Override
    public void writeNameRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public int flushTo(OutputStream out) throws IOException {
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
    public int flushTo(OutputStream out, Charset charset) throws IOException {
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
    public String toString() {
        return new String(chars, 0, off);
    }

    @Override
    public byte[] getBytes() {
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
    public int size() {
        return off;
    }

    @Override
    public byte[] getBytes(Charset charset) {
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

        String str = new String(chars, 0, off);
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return str.getBytes(charset);
    }

    @Override
    public void writeRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void write(JSONObject map) {
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
    public void write(List array) {
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
            ensureCapacity(off + 1);
        }
        chars[off++] = '[';

        boolean first = true;
        for (int i = 0, size = array.size(); i < size; i++) {
            if (!first) {
                if (off == chars.length) {
                    ensureCapacity(off + 1);
                }
                chars[off++] = ',';
            }
            first = false;
            Object value = array.get(i);

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
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ']';
    }

    public void writeString(final char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean special = (context.features & EscapeNoneAscii.mask) != 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c == '\\' || c == quote || c < ' ') {
                special = true;
                break;
            }

            if (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')')) {
                special = true;
                break;
            }
        }

        if (!special) {
            // inline ensureCapacity
            int minCapacity = this.off + chars.length + 2;
            if (minCapacity > this.chars.length) {
                ensureCapacity(minCapacity);
            }

            this.chars[this.off++] = quote;
            System.arraycopy(chars, 0, this.chars, this.off, chars.length);
            this.off += chars.length;
            this.chars[this.off++] = quote;
            return;
        }

        writeStringEscape(chars);
    }

    public void writeString(char[] chars, int off, int len) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean special = (context.features & EscapeNoneAscii.mask) != 0;
        for (int i = off; i < len; ++i) {
            char c = chars[i];
            if (c == '\\' || c == quote || c < ' ') {
                special = true;
                break;
            }
        }

        if (!special) {
            // inline ensureCapacity
            int minCapacity = this.off + len + 2;
            if (minCapacity >= this.chars.length) {
                ensureCapacity(minCapacity);
            }

            this.chars[this.off++] = quote;
            System.arraycopy(chars, off, this.chars, this.off, len);
            this.off += len;
            this.chars[this.off++] = quote;
            return;
        }

        writeStringEscape(new String(chars, off, len));
    }
}

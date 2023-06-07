package com.alibaba.fastjson2;

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
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.FIELD_DECIMAL_INT_COMPACT_OFFSET;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

class JSONWriterUTF16
        extends JSONWriter {
    static final char[] REF_PREF = "{\"$ref\":".toCharArray();

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
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off] = c;
        this.off = off + 1;
    }

    @Override
    public final void writeColon() {
        int off = this.off;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off] = ':';
        this.off = off + 1;
    }

    @Override
    public final void startObject() {
        level++;
        startObject = true;

        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = (byte) '{';

        if (pretty) {
            indent++;
            chars[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                chars[off++] = (byte) '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void endObject() {
        level--;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (pretty) {
            indent--;
            chars[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                chars[off++] = (byte) '\t';
            }
        }

        chars[off++] = (byte) '}';
        this.off = off;
        startObject = false;
    }

    @Override
    public final void writeComma() {
        startObject = false;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = (byte) ',';
        if (pretty) {
            chars[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                chars[off++] = (byte) '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void startArray() {
        level++;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = (byte) '[';
        if (pretty) {
            indent++;
            chars[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                chars[off++] = (byte) '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void endArray() {
        level--;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (pretty) {
            indent--;
            chars[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                chars[off++] = (byte) '\t';
            }
        }
        chars[off++] = (byte) ']';
        this.off = off;
        startObject = false;
    }

    public final void writeString(List<String> list) {
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

        for (byte c : value) {
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
            chars[off++] = quote;
            this.off = off;
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
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
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
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
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
        for (char ch : str) {
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
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
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
        for (byte b : str) {
            char ch = (char) (b & 0xff);
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
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
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

    @Override
    public final void writeString(char[] str, int offset, int len, boolean quoted) {
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

    public final void writeString(String[] strings) {
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
    public final void writeReference(String path) {
        this.lastReference = path;

        writeRaw(REF_PREF, 0, REF_PREF.length);
        writeString(path);
        int off = this.off;
        if (off == chars.length) {
            ensureCapacity(off + 1);
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

        chars[off++] = quote;
        this.off = off;
    }

    @Override
    public final void writeHex(byte[] bytes) {
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

        for (byte b : bytes) {
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
    public final void writeDecimal(BigDecimal value, long features, DecimalFormat format) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        if (format != null) {
            String str = format.format(value);
            writeRaw(str);
            return;
        }

        features |= context.features;

        int precision = value.precision();
        boolean browserCompatible = (features & BrowserCompatible.mask) != 0
                && precision >= 16
                && (value.compareTo(LOW) < 0 || value.compareTo(HIGH) > 0);
        boolean nonStringAsString = (features & WriteNonStringValueAsString.mask) != 0;
        boolean writeAsString = browserCompatible || nonStringAsString;

        int minCapacity = off + precision + 7;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        if (writeAsString) {
            chars[off++] = '"';
        }

        boolean asPlain = (features & WriteBigDecimalAsPlain.mask) != 0;
        long unscaleValue;
        int scale;
        if (precision < 19
                && (scale = value.scale()) >= 0
                && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1
                && (unscaleValue = UnsafeUtils.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET)) != Long.MIN_VALUE
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
    }

    @Override
    public final void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        long hi = value.getMostSignificantBits();
        long lo = value.getLeastSignificantBits();

        final int hi1 = (int) (hi >> 32);
        final int hi2 = (int) hi;
        final int lo1 = (int) (lo >> 32);
        final int lo2 = (int) lo;

        int minCapacity = off + 38;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] UUID_LOOKUP = JSONFactory.UUID_LOOKUP;
        final char[] chars = this.chars;
        final int off = this.off;
        chars[off] = '"';
        int l = UUID_LOOKUP[(hi1 >> 24) & 255];
        chars[off + 1] = (char) (byte) (l >> 8);
        chars[off + 2] = (char) (byte) l;
        l = UUID_LOOKUP[(hi1 >> 16) & 255];
        chars[off + 3] = (char) (byte) (l >> 8);
        chars[off + 4] = (char) (byte) l;
        l = UUID_LOOKUP[(hi1 >> 8) & 255];
        chars[off + 5] = (char) (byte) (l >> 8);
        chars[off + 6] = (char) (byte) l;
        l = UUID_LOOKUP[hi1 & 255];
        chars[off + 7] = (char) (byte) (l >> 8);
        chars[off + 8] = (char) (byte) l;
        chars[off + 9] = '-';
        l = UUID_LOOKUP[(hi2 >> 24) & 255];
        chars[off + 10] = (char) (byte) (l >> 8);
        chars[off + 11] = (char) (byte) l;
        l = UUID_LOOKUP[(hi2 >> 16) & 255];
        chars[off + 12] = (char) (byte) (l >> 8);
        chars[off + 13] = (char) (byte) l;
        chars[off + 14] = '-';
        l = UUID_LOOKUP[(hi2 >> 8) & 255];
        chars[off + 15] = (char) (byte) (l >> 8);
        chars[off + 16] = (char) (byte) l;
        l = UUID_LOOKUP[hi2 & 255];
        chars[off + 17] = (char) (byte) (l >> 8);
        chars[off + 18] = (char) (byte) l;
        chars[off + 19] = '-';
        l = UUID_LOOKUP[(lo1 >> 24) & 255];
        chars[off + 20] = (char) (byte) (l >> 8);
        chars[off + 21] = (char) (byte) l;
        l = UUID_LOOKUP[(lo1 >> 16) & 255];
        chars[off + 22] = (char) (byte) (l >> 8);
        chars[off + 23] = (char) (byte) l;
        chars[off + 24] = '-';
        l = UUID_LOOKUP[(lo1 >> 8) & 255];
        chars[off + 25] = (char) (byte) (l >> 8);
        chars[off + 26] = (char) (byte) l;
        l = UUID_LOOKUP[lo1 & 255];
        chars[off + 27] = (char) (byte) (l >> 8);
        chars[off + 28] = (char) (byte) l;
        l = UUID_LOOKUP[(lo2 >> 24) & 255];
        chars[off + 29] = (char) (byte) (l >> 8);
        chars[off + 30] = (char) (byte) l;
        l = UUID_LOOKUP[(lo2 >> 16) & 255];
        chars[off + 31] = (char) (byte) (l >> 8);
        chars[off + 32] = (char) (byte) l;
        l = UUID_LOOKUP[(lo2 >> 8) & 255];
        chars[off + 33] = (char) (byte) (l >> 8);
        chars[off + 34] = (char) (byte) l;
        l = UUID_LOOKUP[lo2 & 255];
        chars[off + 35] = (char) (byte) (l >> 8);
        chars[off + 36] = (char) (byte) l;
        chars[off + 37] = '"';
        this.off += 38;
    }

    @Override
    public final void writeRaw(String str) {
        ensureCapacity(off + str.length());
        str.getChars(0, str.length(), chars, off);
        off += str.length();
    }

    @Override
    public final void writeRaw(char[] chars, int off, int charslen) {
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
    public final void writeChar(char ch) {
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
    public final void writeRaw(char ch) {
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ch;
    }

    @Override
    public final void writeRaw(char c0, char c1) {
        int off = this.off;
        if (off + 1 >= chars.length) {
            ensureCapacity(off + 2);
        }
        chars[off] = c0;
        chars[off + 1] = c1;
        this.off = off + 2;
    }

    @Override
    public final void writeNameRaw(char[] name) {
        int off = this.off;
        int minCapacity = off + name.length + 1;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            final char[] chars = this.chars;
            chars[off++] = ',';
            if (pretty) {
                chars[off++] = (byte) '\n';
                for (int i = 0; i < indent; ++i) {
                    chars[off++] = (byte) '\t';
                }
            }
        }
        System.arraycopy(name, 0, chars, off, name.length);
        this.off = off + name.length;
    }

    @Override
    public final void writeNameRaw(char[] chars, int off, int len) {
        int minCapacity = this.off + len + 1;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            this.chars[this.off++] = ',';
        }
        System.arraycopy(chars, off, this.chars, this.off, len);
        this.off += len;
    }

    final void ensureCapacity(int minCapacity) {
        if (minCapacity - chars.length > 0) {
            int oldCapacity = chars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - maxArraySize > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            chars = Arrays.copyOf(chars, newCapacity);
        }
    }

    public final void writeInt32(int[] value) {
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

        chars[off++] = ']';
        this.off = off;
    }

    @Override
    public final void writeInt8(byte i) {
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
    public final void writeInt16(short i) {
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
    public final void writeInt32(int i) {
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

    public final void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean browserCompatible = (context.features & BrowserCompatible.mask) != 0;
        boolean nonStringAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;

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

        chars[off++] = ']';
        this.off = off;
    }

    @Override
    public final void writeInt64(long i) {
        boolean writeAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                || ((context.features & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));

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
        } else if ((context.features & WriteClassName.mask) != 0
                        && (context.features & NotWriteNumberClassName.mask) == 0
                        && i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE
        ) {
            chars[off++] = 'L';
        }
        this.off = off;
    }

    @Override
    public final void writeFloat(float value) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

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

        int len = DoubleToDecimal.toString(value, chars, off, true);
        off += len;

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

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

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

            if (writeAsString) {
                chars[off++] = '"';
            }

            float value = values[i];
            int len = DoubleToDecimal.toString(value, chars, off, true);
            off += len;

            if (writeAsString) {
                chars[off++] = '"';
            }
        }
        chars[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

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

        int len = DoubleToDecimal.toString(value, chars, off, true);
        off += len;

        if (writeAsString) {
            chars[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeDoubleArray(double value0, double value1) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 48 + 3;
        if (writeAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);

        final char[] chars = this.chars;
        chars[off++] = '[';

        if (writeAsString) {
            chars[off++] = '"';
        }
        int len0 = DoubleToDecimal.toString(value0, chars, off, true);
        off += len0;
        if (writeAsString) {
            chars[off++] = '"';
        }

        chars[off++] = ',';

        if (writeAsString) {
            chars[off++] = '"';
        }
        int len1 = DoubleToDecimal.toString(value1, chars, off, true);
        off += len1;
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

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

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

            if (writeAsString) {
                chars[off++] = '"';
            }

            double value = values[i];
            int len = DoubleToDecimal.toString(value, chars, off, true);
            off += len;

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
    public final void writeDateTime19(
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
    public final void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        final Context context = this.context;
        if (context.dateFormat != null) {
            if (writeLocalDateWithFormat(date, context)) {
                return;
            }
        }

        int off = this.off;
        int minCapacity = off + 18;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }
        final char[] chars = this.chars;
        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        chars[off++] = quote;
        this.off = off;
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime dateTime) {
        int off = this.off;
        int minCapacity = off + 38;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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

                if (offset >= 0) {
                    bytes[off++] = '+';
                } else {
                    bytes[off++] = '-';
                }
                v = DIGITS_K[offsetAbs];
                bytes[off] = (char) (byte) (v >> 8);
                bytes[off + 1] = (char) (byte) v;
                off += 2;

                bytes[off++] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                v = DIGITS_K[offsetMinutes];
                bytes[off] = (char) (byte) (v >> 8);
                bytes[off + 1] = (char) (byte) v;
                off += 2;
            }
        }
        bytes[off] = quote;
        this.off = off + 1;
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
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
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
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
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
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
    public final void writeLocalTime(LocalTime time) {
        int off = this.off;
        int minCapacity = off + 20;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
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
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off++] = 'T';
        off = IOUtils.writeLocalTime(chars, off, dateTime.toLocalTime());
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

        ZoneOffset offset = dateTime.getOffset();
        String zoneId = offset.getId();
        int zoneIdLength = zoneId.length();
        boolean utc = ZoneOffset.UTC == offset
                || (zoneIdLength <= 3 && ("UTC".equals(zoneId) || "Z".equals(zoneId)));
        if (utc) {
            zoneId = "Z";
        }

        int off = this.off;
        int minCapacity = off + zoneIdLength + 40;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;
        LocalDateTime ldt = dateTime.toLocalDateTime();
        LocalDate date = ldt.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        chars[off++] = 'T';
        LocalTime time = ldt.toLocalTime();
        off = IOUtils.writeLocalTime(chars, off, time);
        if (utc) {
            chars[off++] = 'Z';
        } else {
            zoneId.getChars(0, zoneIdLength, chars, off);
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

        String str = new String(chars, 0, off);
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return str.getBytes(charset);
    }

    @Override
    public final void writeRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public final void write(JSONObject map) {
        if (map == null) {
            this.writeNull();
            return;
        }

        final long NONE_DIRECT_FEATURES = ReferenceDetection.mask
                | PrettyFormat.mask
                | NotWriteEmptyArray.mask
                | NotWriteDefaultValue.mask;

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(map.getClass());
            objectWriter.write(this, map, null, null, 0);
            return;
        }

        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '{';

        boolean first = true;
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null && (context.features & Feature.WriteMapNullValue.mask) == 0) {
                continue;
            }

            if (!first) {
                if (off == chars.length) {
                    ensureCapacity(off + 1);
                }
                chars[off++] = ',';
            }

            first = false;
            Object key = entry.getKey();
            if (key instanceof String) {
                writeString((String) key);
            } else {
                writeAny(key);
            }

            if (off == chars.length) {
                ensureCapacity(off + 1);
            }
            chars[off++] = ':';

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
        chars[off++] = '}';
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
            ensureCapacity(off + 1);
        }
        chars[off++] = '[';

        boolean first = true;
        for (Object o : array) {
            if (!first) {
                if (off == chars.length) {
                    ensureCapacity(off + 1);
                }
                chars[off++] = ',';
            }
            first = false;
            Object value = o;

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

    public final void writeString(final char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean special = false;
        for (char c : chars) {
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

    public final void writeString(char[] chars, int off, int len) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean special = false;
        for (int i = off; i < len; ++i) {
            if (chars[i] == '\\' || chars[i] == '"') {
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

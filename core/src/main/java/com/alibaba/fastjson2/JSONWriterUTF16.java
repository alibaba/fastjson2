package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriter;

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
import static com.alibaba.fastjson2.JSONWriter.Feature.NotWriteDefaultValue;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.FIELD_DECIMAL_INT_COMPACT_OFFSET;

class JSONWriterUTF16
        extends JSONWriter {
    static final char[] REF_PREF = "{\"$ref\":".toCharArray();

    protected char[] chars;
    final CacheItem cacheItem;

    JSONWriterUTF16(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_16);
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        chars = CHARS_UPDATER.getAndSet(cacheItem, null);
        if (chars == null) {
            chars = new char[8192];
        }
    }

    @Override
    public final void flushTo(java.io.Writer to) {
        try {
            if (off > 0) {
                to.write(chars, 0, off);
                off = 0;
            }
        } catch (IOException e) {
            throw new JSONException("flushTo error", e);
        }
    }

    @Override
    public final void close() {
        if (chars.length > CACHE_THRESHOLD) {
            return;
        }

        CHARS_UPDATER.lazySet(cacheItem, chars);
    }

    @Override
    protected final void write0(char c) {
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = c;
    }

    @Override
    public final void writeColon() {
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ':';
    }

    @Override
    public final void startObject() {
        level++;
        startObject = true;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '{';
    }

    @Override
    public final void endObject() {
        level--;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '}';
        startObject = false;
    }

    @Override
    public final void writeComma() {
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ',';
    }

    @Override
    public final void startArray() {
        level++;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '[';
    }

    @Override
    public final void endArray() {
        level--;
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ']';
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

    protected void writeStringLatin1(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;
        int minCapacity = off + value.length + 2;
        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }

        final int mark = off;
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
            return;
        }
        off = mark;

        writeStringEscape(value);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;

        if (STRING_VALUE != null) {
            int coder = STRING_CODER.applyAsInt(str);
            if (coder == 0) {
                byte[] value = STRING_VALUE.apply(str);
                int minCapacity = off + value.length + 2;
                if (minCapacity - chars.length > 0) {
                    ensureCapacity(minCapacity);
                }

                final int mark = off;
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
                    return;
                }
                off = mark;
            }
        }

        final int strlen = str.length();
        {
            int i = 0;
            // vector optimize 8
            while (i + 8 <= strlen) {
                char c0 = str.charAt(i);
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                char c3 = str.charAt(i + 3);
                char c4 = str.charAt(i + 4);
                char c5 = str.charAt(i + 5);
                char c6 = str.charAt(i + 6);
                char c7 = str.charAt(i + 7);

                if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\' || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                        || c0 == quote || c1 == quote || c2 == quote || c3 == quote || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                        || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ' || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' ') {
                    escape = true;
                    break;
                }

                if (browserSecure) {
                    if (c0 == '<' || c1 == '<' || c2 == '<' || c3 == '<' || c4 == '<' || c5 == '<' || c6 == '<' || c7 == '<'
                            || c0 == '>' || c1 == '>' || c2 == '>' || c3 == '>' || c4 == '>' || c5 == '>' || c6 == '>' || c7 == '>'
                            || c0 == '(' || c1 == '(' || c2 == '(' || c3 == '(' || c4 == '(' || c5 == '(' || c6 == '(' || c7 == '('
                            || c0 == ')' || c1 == ')' || c2 == ')' || c3 == ')' || c4 == ')' || c5 == ')' || c6 == ')' || c7 == ')'
                    ) {
                        escape = true;
                        break;
                    }
                }

                if (escapeNoneAscii) {
                    if (c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F || c4 > 0x007F || c5 > 0x007F || c6 > 0x007F || c7 > 0x007F) {
                        escape = true;
                        break;
                    }
                }

                i += 8;
            }

            if (!escape) {
                // vector optimize 4
                while (i + 4 <= strlen) {
                    char c0 = str.charAt(i);
                    char c1 = str.charAt(i + 1);
                    char c2 = str.charAt(i + 2);
                    char c3 = str.charAt(i + 3);
                    if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                            || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                            || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    ) {
                        escape = true;
                        break;
                    }

                    if (browserSecure) {
                        if (c0 == '<' || c1 == '<' || c2 == '<' || c3 == '<'
                                || c0 == '>' || c1 == '>' || c2 == '>' || c3 == '>'
                                || c0 == '(' || c1 == '(' || c2 == '(' || c3 == '('
                                || c0 == ')' || c1 == ')' || c2 == ')' || c3 == ')'
                        ) {
                            escape = true;
                            break;
                        }
                    }

                    if (escapeNoneAscii) {
                        if (c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F) {
                            escape = true;
                            break;
                        }
                    }

                    i += 4;
                }
            }

            if (!escape && i + 2 <= strlen) {
                char c0 = str.charAt(i);
                char c1 = str.charAt(i + 1);
                if (c0 == quote || c1 == quote || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' ') {
                    escape = true;
                } else if (browserSecure
                        && (c0 == '<' || c1 == '<'
                        || c0 == '>' || c1 == '>'
                        || c0 == '(' || c1 == '(')
                        || c0 == ')' || c1 == ')') {
                    escape = true;
                } else if (escapeNoneAscii && (c0 > 0x007F || c1 > 0x007F)) {
                    escape = true;
                } else {
                    i += 2;
                }
            }
            if (!escape && i + 1 == strlen) {
                char c0 = str.charAt(i);
                escape = c0 == '"' || c0 == '\\' || c0 < ' '
                        || (escapeNoneAscii && c0 > 0x007F)
                        || (browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'));
            }
        }

        if (!escape) {
            // inline ensureCapacity(off + strlen + 2);
            int minCapacity = off + strlen + 2;
            if (minCapacity - chars.length > 0) {
                ensureCapacity(minCapacity);
            }

            chars[off++] = quote;
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = quote;
            return;
        }

        writeStringEscape(str);
    }

    protected final void writeStringEscape(String str) {
        final int strlen = str.length();
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        ensureCapacity(off + strlen * 6 + 2);

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
                    chars[off++] = '\\';
                    chars[off++] = ch;
                    break;
                case '\r':
                    chars[off++] = '\\';
                    chars[off++] = 'r';
                    break;
                case '\n':
                    chars[off++] = '\\';
                    chars[off++] = 'n';
                    break;
                case '\b':
                    chars[off++] = '\\';
                    chars[off++] = 'b';
                    break;
                case '\f':
                    chars[off++] = '\\';
                    chars[off++] = 'f';
                    break;
                case '\t':
                    chars[off++] = '\\';
                    chars[off++] = 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('a' + (ch - 10));
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
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('a' + (ch - 26));
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off++] = quote;
    }

    protected final void writeStringEscape(char[] str) {
        final int strlen = str.length;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        ensureCapacity(off + strlen * 6 + 2);

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
                    chars[off++] = '\\';
                    chars[off++] = ch;
                    break;
                case '\r':
                    chars[off++] = '\\';
                    chars[off++] = 'r';
                    break;
                case '\n':
                    chars[off++] = '\\';
                    chars[off++] = 'n';
                    break;
                case '\b':
                    chars[off++] = '\\';
                    chars[off++] = 'b';
                    break;
                case '\f':
                    chars[off++] = '\\';
                    chars[off++] = 'f';
                    break;
                case '\t':
                    chars[off++] = '\\';
                    chars[off++] = 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('a' + (ch - 10));
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
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('a' + (ch - 26));
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off++] = quote;
    }

    protected final void writeStringEscape(byte[] str) {
        final int strlen = str.length;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        ensureCapacity(off + strlen * 6 + 2);

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
                    chars[off++] = '\\';
                    chars[off++] = ch;
                    break;
                case '\r':
                    chars[off++] = '\\';
                    chars[off++] = 'r';
                    break;
                case '\n':
                    chars[off++] = '\\';
                    chars[off++] = 'n';
                    break;
                case '\b':
                    chars[off++] = '\\';
                    chars[off++] = 'b';
                    break;
                case '\f':
                    chars[off++] = '\\';
                    chars[off++] = 'f';
                    break;
                case '\t':
                    chars[off++] = '\\';
                    chars[off++] = 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('a' + (ch - 10));
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
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('a' + (ch - 26));
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')')) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }
        chars[off++] = quote;
    }

    @Override
    public final void writeString(char[] str, int offset, int len, boolean quote) {
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int minCapacity = quote ? this.off + 2 : this.off;
        if (escapeNoneAscii) {
            minCapacity += len * 6;
        } else {
            minCapacity += len * 2;
        }

        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (quote) {
            chars[off++] = this.quote;
        }

        for (int i = offset; i < len; ++i) {
            char ch = str[i];
            switch (ch) {
                case '"':
                case '\'':
                    if (ch == this.quote) {
                        chars[off++] = '\\';
                    }
                    chars[off++] = ch;
                    break;
                case '\\':
                    chars[off++] = '\\';
                    chars[off++] = ch;
                    break;
                case '\r':
                    chars[off++] = '\\';
                    chars[off++] = 'r';
                    break;
                case '\n':
                    chars[off++] = '\\';
                    chars[off++] = 'n';
                    break;
                case '\b':
                    chars[off++] = '\\';
                    chars[off++] = 'b';
                    break;
                case '\f':
                    chars[off++] = '\\';
                    chars[off++] = 'f';
                    break;
                case '\t':
                    chars[off++] = '\\';
                    chars[off++] = 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = (char) ('a' + (ch - 10));
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
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    chars[off++] = '\\';
                    chars[off++] = 'u';
                    chars[off++] = '0';
                    chars[off++] = '0';
                    chars[off++] = '1';
                    chars[off++] = (char) ('a' + (ch - 26));
                    break;
                default:
                    if (escapeNoneAscii && ch > 0x007F) {
                        chars[off++] = '\\';
                        chars[off++] = 'u';
                        chars[off++] = DIGITS[(ch >>> 12) & 15];
                        chars[off++] = DIGITS[(ch >>> 8) & 15];
                        chars[off++] = DIGITS[(ch >>> 4) & 15];
                        chars[off++] = DIGITS[ch & 15];
                    } else {
                        chars[off++] = ch;
                    }
                    break;
            }
        }

        if (quote) {
            chars[off++] = this.quote;
        }
    }

    @Override
    public final void writeReference(String path) {
        this.lastReference = path;

        writeRaw(REF_PREF, 0, REF_PREF.length);
        writeString(path);
        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = (byte) '}';
    }

    @Override
    public final void writeBase64(byte[] bytes) {
        if (bytes == null) {
            writeArrayNull();
            return;
        }

        int charsLen = ((bytes.length - 1) / 3 + 1) << 2; // base64 character count

        ensureCapacity(off + charsLen + 2);
        chars[off++] = quote;

        int eLen = (bytes.length / 3) * 3; // Length of even 24-bits.

        for (int s = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);

            // Encode the int into four chars
            chars[off++] = CA[(i >>> 18) & 0x3f];
            chars[off++] = CA[(i >>> 12) & 0x3f];
            chars[off++] = CA[(i >>> 6) & 0x3f];
            chars[off++] = CA[i & 0x3f];
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = bytes.length - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((bytes[eLen] & 0xff) << 10) | (left == 2 ? ((bytes[bytes.length - 1] & 0xff) << 2) : 0);

            // Set last four chars
            chars[off++] = CA[i >> 12];
            chars[off++] = CA[(i >>> 6) & 0x3f];
            chars[off++] = left == 2 ? CA[i & 0x3f] : '=';
            chars[off++] = '=';
        }

        chars[off++] = quote;
    }

    @Override
    public final void writeHex(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }

        int charsLen = bytes.length * 2 + 3;

        ensureCapacity(off + charsLen + 2);
        chars[off++] = 'x';
        chars[off++] = '\'';

        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];

            int a = b & 0xFF;
            int b0 = a >> 4;
            int b1 = a & 0xf;

            chars[off++] = (char) (b0 + (b0 < 10 ? 48 : 55));
            chars[off++] = (char) (b1 + (b1 < 10 ? 48 : 55));
        }

        chars[off++] = '\'';
    }

    @Override
    public final void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        String str = value.toString(10);

        boolean browserCompatible = ((context.features | features) & Feature.BrowserCompatible.mask) != 0;
        if (browserCompatible && (value.compareTo(LOW_BIGINT) < 0 || value.compareTo(HIGH_BIGINT) > 0)) {
            final int strlen = str.length();
            ensureCapacity(off + strlen + 2);
            chars[off++] = '"';
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = '"';
        } else {
            final int strlen = str.length();
            ensureCapacity(off + strlen);
            str.getChars(0, strlen, chars, off);
            off += strlen;
        }
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

        int minCapacity = off + precision + 7;
        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }

        if (browserCompatible) {
            chars[off++] = '"';
        }

        long unscaleValue;
        if ((features & Feature.WriteBigDecimalAsPlain.mask) != 0) {
            if (precision < 19
                    && value.scale() >= 0
                    && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1
                    && (unscaleValue = UnsafeUtils.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET)) != Long.MIN_VALUE
            ) {
                int scale = value.scale();
                off += getDecimalChars(unscaleValue, scale, chars, off);
            } else {
                String str = value.toPlainString();
                str.getChars(0, str.length(), chars, off);
                off += str.length();
            }
        } else {
            String str = value.toString();
            int strlen = str.length();
            str.getChars(0, strlen, chars, off);
            off += strlen;
        }

        if (browserCompatible) {
            chars[off++] = '"';
        }
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
        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = '"';
        formatUnsignedLong0(lsb, chars, off + 24, 12);
        formatUnsignedLong0(lsb >>> 48, chars, off + 19, 4);
        formatUnsignedLong0(msb, chars, off + 14, 4);
        formatUnsignedLong0(msb >>> 16, chars, off + 9, 4);
        formatUnsignedLong0(msb >>> 32, chars, off + 0, 8);

        chars[off + 23] = '-';
        chars[off + 18] = '-';
        chars[off + 13] = '-';
        chars[off + 8] = '-';
        off += 36;
        chars[off++] = '"';
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
            if (minCapacity - this.chars.length > 0) {
                ensureCapacity(minCapacity);
            }
        }
        System.arraycopy(chars, off, this.chars, this.off, charslen);
        this.off += charslen;
    }

    @Override
    public final void writeChar(char ch) {
        int minCapacity = this.off + 8;
        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
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
                chars[off++] = '\\';
                chars[off++] = ch;
                break;
            case '\r':
                chars[off++] = '\\';
                chars[off++] = 'r';
                break;
            case '\n':
                chars[off++] = '\\';
                chars[off++] = 'n';
                break;
            case '\b':
                chars[off++] = '\\';
                chars[off++] = 'b';
                break;
            case '\f':
                chars[off++] = '\\';
                chars[off++] = 'f';
                break;
            case '\t':
                chars[off++] = '\\';
                chars[off++] = 't';
                break;
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                chars[off++] = '\\';
                chars[off++] = 'u';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = (char) ('0' + (int) ch);
                break;
            case 11:
            case 14:
            case 15:
                chars[off++] = '\\';
                chars[off++] = 'u';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = (char) ('a' + (ch - 10));
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
                chars[off++] = '\\';
                chars[off++] = 'u';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = '1';
                chars[off++] = (char) ('0' + (ch - 16));
                break;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                chars[off++] = '\\';
                chars[off++] = 'u';
                chars[off++] = '0';
                chars[off++] = '0';
                chars[off++] = '1';
                chars[off++] = (char) ('a' + (ch - 26));
                break;
            default:
                chars[off++] = ch;
                break;
        }
        chars[off++] = quote;
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
        if (off + 1 >= chars.length) {
            ensureCapacity(off + 2);
        }
        chars[off++] = c0;
        chars[off++] = c1;
    }

    @Override
    public final void writeNameRaw(char[] chars) {
        {
            // inline ensureCapacity
            int minCapacity = off + chars.length + (startObject ? 0 : 1);
            if (minCapacity - this.chars.length > 0) {
                ensureCapacity(minCapacity);
            }
        }

        if (startObject) {
            startObject = false;
        } else {
            this.chars[off++] = ',';
        }
        System.arraycopy(chars, 0, this.chars, this.off, chars.length);
        off += chars.length;
    }

    @Override
    public final void writeNameRaw(char[] chars, int off, int len) {
        {
            // inline ensureCapacity
            int minCapacity = this.off + len + (startObject ? 0 : 1);
            if (minCapacity - this.chars.length > 0) {
                ensureCapacity(minCapacity);
            }
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

        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = '[';

        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                if (off == chars.length) {
                    ensureCapacity(off + 1);
                }
                chars[off++] = ',';
            }
            writeInt32(value[i]);
        }

        if (off == chars.length) {
            ensureCapacity(off + 1);
        }
        chars[off++] = ']';
    }

    @Override
    public final void writeInt32(int i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 13;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
            off = IOUtils.writeInt32(chars, off, i);
            chars[off++] = '"';
        } else {
            off = IOUtils.writeInt32(chars, off, i);
        }
    }

    public final void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean browserCompatible = (context.features & BrowserCompatible.mask) != 0;
        boolean noneStringAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        boolean writeAsString = noneStringAsString || browserCompatible;

        int minCapacity = off + 2 + values.length * (writeAsString ? 23 : 21);
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = '[';
        if (writeAsString) {
            for (int i = 0; i < values.length; i++) {
                if (i != 0) {
                    chars[off++] = ',';
                }
                long v = values[i];

                if (!noneStringAsString && browserCompatible && v <= 9007199254740991L && v >= -9007199254740991L) {
                    off = IOUtils.writeInt64(chars, off, v);
                } else {
                    chars[off++] = '"';
                    off = IOUtils.writeInt64(chars, off, v);
                    chars[off++] = '"';
                }
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (i != 0) {
                    chars[off++] = ',';
                }
                long v = values[i];
                off = IOUtils.writeInt64(chars, off, v);
            }
        }

        chars[off++] = ']';
    }

    @Override
    public final void writeInt64(long i) {
        boolean writeAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                || ((context.features & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));
        int minCapacity = off + 23;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
            off = IOUtils.writeInt64(chars, off, i);
            chars[off++] = '"';
        } else {
            off = IOUtils.writeInt64(chars, off, i);
        }
    }

    @Override
    public final void writeFloat(float value) {
        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 15;
        if (writeAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);
        if (writeAsString) {
            chars[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, chars, off, true);
        off += len;

        if (writeAsString) {
            chars[off++] = '"';
        }
    }

    @Override
    public final void writeFloat(float[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + values.length * (writeAsString ? 16 : 18) + 1;
        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }

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
        chars[off++] = ']';
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 24;
        if (writeAsString) {
            minCapacity += 2;
        }

        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }

        if (writeAsString) {
            chars[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, chars, off, true);
        off += len;

        if (writeAsString) {
            chars[off++] = '"';
        }
    }

    @Override
    public final void writeDoubleArray(double value0, double value1) {
        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 48 + 3;
        if (writeAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);

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

        chars[off++] = ']';
    }

    @Override
    public final void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + values.length * (writeAsString ? 25 : 27) + 1;
        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }
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
        chars[off++] = ']';
    }

    @Override
    public final void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        ensureCapacity(off + 16);

        chars[off++] = quote;

        IOUtils.write4(year, chars, off);
        off += 4;
        IOUtils.write2(month, chars, off);
        off += 2;
        chars[off++] = (char) (dayOfMonth / 10 + '0');
        chars[off++] = (char) (dayOfMonth % 10 + '0');
        chars[off++] = (char) (hour / 10 + '0');
        chars[off++] = (char) (hour % 10 + '0');
        chars[off++] = (char) (minute / 10 + '0');
        chars[off++] = (char) (minute % 10 + '0');
        chars[off++] = (char) (second / 10 + '0');
        chars[off++] = (char) (second % 10 + '0');

        chars[off++] = quote;
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

        chars[off++] = quote;

        IOUtils.write4(year, chars, off);
        off += 4;
        chars[off++] = '-';
        IOUtils.write2(month, chars, off);
        off += 2;
        chars[off++] = '-';
        chars[off++] = (char) (dayOfMonth / 10 + '0');
        chars[off++] = (char) (dayOfMonth % 10 + '0');
        chars[off++] = ' ';
        chars[off++] = (char) (hour / 10 + '0');
        chars[off++] = (char) (hour % 10 + '0');
        chars[off++] = ':';
        chars[off++] = (char) (minute / 10 + '0');
        chars[off++] = (char) (minute % 10 + '0');
        chars[off++] = ':';
        chars[off++] = (char) (second / 10 + '0');
        chars[off++] = (char) (second % 10 + '0');

        chars[off++] = quote;
    }

    @Override
    public final void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        if (context.dateFormat != null) {
            if (context.dateFormatUnixTime) {
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIN);
                long millis = dateTime.atZone(context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                writeInt64(millis / 1000);
                return;
            }

            if (context.dateFormatMillis) {
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.MIN);
                long millis = dateTime.atZone(context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                writeInt64(millis);
                return;
            }

            DateTimeFormatter formatter = context.getDateFormatter();
            if (formatter != null) {
                String str;
                if (context.isDateFormatHasHour()) {
                    str = formatter.format(LocalDateTime.of(date, LocalTime.MIN));
                } else {
                    str = formatter.format(date);
                }
                writeString(str);
                return;
            }
        }

        int minCapacity = off + 18;
        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }
        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        chars[off++] = quote;
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime dateTime) {
        int minCapacity = off + 38;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off++] = ' ';
        off = IOUtils.writeLocalTime(chars, off, dateTime.toLocalTime());
        chars[off++] = quote;
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

        int minCapacity = off + 25 + zonelen;
        if (off + minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = quote;
        IOUtils.write4(year, chars, off);
        off += 4;
        chars[off++] = '-';
        IOUtils.write2(month, chars, off);
        off += 2;
        chars[off++] = '-';
        IOUtils.write2(dayOfMonth, chars, off);
        off += 2;
        chars[off++] = timeZone ? 'T' : ' ';
        IOUtils.write2(hour, chars, off);
        off += 2;
        chars[off++] = ':';
        IOUtils.write2(minute, chars, off);
        off += 2;
        chars[off++] = ':';
        IOUtils.write2(second, chars, off);
        off += 2;

        if (millis > 0) {
            chars[off++] = '.';
            int div = millis / 10;
            int div2 = div / 10;
            final int rem1 = millis - div * 10;

            if (rem1 != 0) {
                IOUtils.write3(millis, chars, off);
                off += 3;
            } else {
                final int rem2 = div - div2 * 10;
                if (rem2 != 0) {
                    IOUtils.write2(div, chars, off);
                    off += 2;
                } else {
                    chars[off++] = (char) (div2 + '0');
                }
            }
        }

        if (timeZone) {
            int offset = offsetSeconds / 3600;
            if (offsetSeconds == 0) {
                chars[off++] = 'Z';
            } else {
                int offsetAbs = Math.abs(offset);

                if (offset >= 0) {
                    chars[off++] = '+';
                } else {
                    chars[off++] = '-';
                }
                IOUtils.write2(offsetAbs, chars, off);
                off += 2;

                chars[off++] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                IOUtils.write2(offsetMinutes, chars, off);
                off += 2;
            }
        }
        chars[off++] = quote;
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        ensureCapacity(off + 10);

        chars[off++] = quote;
        IOUtils.write4(year, chars, off);
        off += 4;
        IOUtils.write2(month, chars, off);
        off += 2;
        IOUtils.write2(dayOfMonth, chars, off);
        off += 2;
        chars[off++] = quote;
    }

    @Override
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int minCapacity = off + 13;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = quote;
        off = IOUtils.writeLocalDate(chars, off, year, month, dayOfMonth);
        chars[off++] = quote;
    }

    @Override
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
        ensureCapacity(off + 10);

        chars[off++] = quote;
        IOUtils.write2(hour, chars, off);
        off += 2;
        chars[off++] = ':';
        IOUtils.write2(minute, chars, off);
        off += 2;
        chars[off++] = ':';
        IOUtils.write2(second, chars, off);
        off += 2;
        chars[off++] = quote;
    }

    @Override
    public final void writeLocalTime(LocalTime time) {
        int minCapacity = off + 20;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }
        chars[off++] = quote;
        off = IOUtils.writeLocalTime(chars, off, time);
        chars[off++] = quote;
    }

    @Override
    public final void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }
        ZoneId zone = dateTime.getZone();
        String zoneId = zone.getId();

        char firstZoneChar = '\0';
        int zoneSize;
        if (ZoneOffset.UTC == zone || (zoneId.length() <= 3 && ("UTC".equals(zoneId) || "Z".equals(zoneId)))) {
            zoneId = "Z";
            zoneSize = 1;
        } else if (zoneId.length() != 0 && ((firstZoneChar = zoneId.charAt(0)) == '+' || firstZoneChar == '-')) {
            zoneSize = zoneId.length();
        } else {
            zoneSize = 2 + zoneId.length();
        }

        int minCapacity = off + zoneSize + 38;
        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off++] = 'T';
        off = IOUtils.writeLocalTime(chars, off, dateTime.toLocalTime());
        if (zoneSize == 1) {
            chars[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
        } else {
            chars[off++] = '[';
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
            chars[off++] = ']';
        }
        chars[off++] = quote;
    }

    @Override
    public final void writeOffsetDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        ZoneOffset offset = dateTime.getOffset();
        String zoneId = offset.getId();

        char firstZoneChar = '\0';
        int zoneSize;
        if (ZoneOffset.UTC == offset || (zoneId.length() <= 3 && ("UTC".equals(zoneId) || "Z".equals(zoneId)))) {
            zoneId = "Z";
            zoneSize = 1;
        } else if (zoneId.length() != 0 && ((firstZoneChar = zoneId.charAt(0)) == '+' || firstZoneChar == '-')) {
            zoneSize = zoneId.length();
        } else {
            zoneSize = 2 + zoneId.length();
        }

        int minCapacity = off + zoneSize + 38;
        if (minCapacity > chars.length) {
            ensureCapacity(minCapacity);
        }

        chars[off++] = quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(chars, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        chars[off++] = 'T';
        off = IOUtils.writeLocalTime(chars, off, dateTime.toLocalTime());
        if (zoneSize == 1) {
            chars[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
        } else {
            chars[off++] = '[';
            zoneId.getChars(0, zoneId.length(), chars, off);
            off += zoneId.length();
            chars[off++] = ']';
        }
        chars[off++] = quote;
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

    static void formatUnsignedLong0(long val, char[] buf, int offset, int len) { // for uuid
        int charPos = offset + len;
        do {
            buf[--charPos] = DIGITS[((int) val) & 15];
            val >>>= 4;
        } while (charPos > offset);
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
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry next = it.next();
            Object value = next.getValue();
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
            Object key = next.getKey();
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

    public final void writeString(final char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean special = false;
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
            if (minCapacity - this.chars.length > 0) {
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
            if (minCapacity - this.chars.length > 0) {
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

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
import static com.alibaba.fastjson2.util.JDKUtils.FIELD_DECIMAL_INT_COMPACT_OFFSET;

class JSONWriterUTF8
        extends JSONWriter {
    static final byte[] REF_PREF = "{\"$ref\":".getBytes(StandardCharsets.ISO_8859_1);

    final CacheItem cacheItem;
    protected byte[] bytes;

    JSONWriterUTF8(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_8);
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
    }

    @Override
    public final void writeReference(String path) {
        this.lastReference = path;

        writeRaw(REF_PREF);
        writeString(path);
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '}';
    }

    @Override
    public final void writeBase64(byte[] bytes) {
        int charsLen = ((bytes.length - 1) / 3 + 1) << 2; // base64 character count

        ensureCapacity(off + charsLen + 2);
        this.bytes[off++] = (byte) quote;

        int eLen = (bytes.length / 3) * 3; // Length of even 24-bits.

        for (int s = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);

            // Encode the int into four chars
            this.bytes[off++] = (byte) CA[(i >>> 18) & 0x3f];
            this.bytes[off++] = (byte) CA[(i >>> 12) & 0x3f];
            this.bytes[off++] = (byte) CA[(i >>> 6) & 0x3f];
            this.bytes[off++] = (byte) CA[i & 0x3f];
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = bytes.length - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((bytes[eLen] & 0xff) << 10) | (left == 2 ? ((bytes[bytes.length - 1] & 0xff) << 2) : 0);

            // Set last four chars
            this.bytes[off++] = (byte) CA[i >> 12];
            this.bytes[off++] = (byte) CA[(i >>> 6) & 0x3f];
            this.bytes[off++] = left == 2 ? (byte) CA[i & 0x3f] : (byte) '=';
            this.bytes[off++] = '=';
        }

        this.bytes[off++] = (byte) quote;
    }

    @Override
    public final void writeHex(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }

        int charsLen = bytes.length * 2 + 3;

        ensureCapacity(off + charsLen + 2);
        this.bytes[off++] = 'x';
        this.bytes[off++] = '\'';

        for (int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];

            int a = b & 0xFF;
            int b0 = a >> 4;
            int b1 = a & 0xf;

            this.bytes[off++] = (byte) (b0 + (b0 < 10 ? 48 : 55));
            this.bytes[off++] = (byte) (b1 + (b1 < 10 ? 48 : 55));
        }

        this.bytes[off++] = '\'';
    }

    @Override
    public final void close() {
        if (bytes.length > CACHE_THRESHOLD) {
            return;
        }

        BYTES_UPDATER.lazySet(cacheItem, bytes);
    }

    public final int size() {
        return off;
    }

    @Override
    public final byte[] getBytes() {
        return Arrays.copyOf(bytes, off);
    }

    @Override
    public final byte[] getBytes(Charset charset) {
        if (charset == StandardCharsets.UTF_8) {
            return Arrays.copyOf(bytes, off);
        }

        String str = toString();
        return str.getBytes(charset);
    }

    @Override
    public final int flushTo(OutputStream to) throws IOException {
        int len = off;
        to.write(bytes, 0, off);
        off = 0;
        return len;
    }

    @Override
    protected final void write0(char c) {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) c;
    }

    @Override
    public final void writeColon() {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = ':';
    }

    @Override
    public final void startObject() {
        level++;
        startObject = true;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '{';
    }

    @Override
    public final void endObject() {
        level--;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '}';
        startObject = false;
    }

    @Override
    public final void writeComma() {
        startObject = false;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) ',';
    }

    @Override
    public final void startArray() {
        level++;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '[';
    }

    @Override
    public final void endArray() {
        level--;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) ']';
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

        char[] chars = JDKUtils.getCharArray(str);

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += chars.length * 3;
        }

        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;

        int i = 0;

        // vector optimize 8
        while (i + 8 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            char c4 = chars[i + 4];
            char c5 = chars[i + 5];
            char c6 = chars[i + 6];
            char c7 = chars[i + 7];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || c4 > 0x007F || c5 > 0x007F || c6 > 0x007F || c7 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'
                    || c4 == '<' || c4 == '>' || c4 == '(' || c4 == ')'
                    || c5 == '<' || c5 == '>' || c5 == '(' || c5 == ')'
                    || c6 == '<' || c6 == '>' || c6 == '(' || c6 == ')'
                    || c7 == '<' || c7 == '>' || c7 == '(' || c7 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            bytes[off + 4] = (byte) c4;
            bytes[off + 5] = (byte) c5;
            bytes[off + 6] = (byte) c6;
            bytes[off + 7] = (byte) c7;
            off += 8;
            i += 8;
        }

        // vector optimize 4
        while (i + 4 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            off += 4;
            i += 4;
        }

        if (i + 2 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];

            if (!(c0 == quote || c1 == quote
                    || c0 == '\\' || c1 == '\\'
                    || c0 < ' ' || c1 < ' '
                    || c0 > 0x007F || c1 > 0x007F)
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '('
                    || c0 == ')' || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'))
            ) {
                bytes[off] = (byte) c0;
                bytes[off + 1] = (byte) c1;
                off += 2;
                i += 2;
            }
        }
        if (i + 1 == chars.length) {
            char c0 = chars[i];
            if (c0 != quote
                    && c0 != '\\'
                    && c0 >= ' '
                    && c0 <= 0x007F
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
            ) {
                bytes[off++] = (byte) c0;
                bytes[off++] = (byte) quote;
                return;
            }
        }

        int rest = chars.length - i;
        minCapacity = off + rest * 6 + 2;
        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (i < chars.length) {
            writeStringEscapedRest(chars, chars.length, browserSecure, escapeNoneAscii, i);
        }

        bytes[off++] = (byte) quote;
    }

    protected void writeStringLatin1(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean escape = false;
        final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        int valueOffset = 0;
        // vector optimize 8
        while (valueOffset + 8 <= value.length) {
            byte c0 = value[valueOffset];
            byte c1 = value[valueOffset + 1];
            byte c2 = value[valueOffset + 2];
            byte c3 = value[valueOffset + 3];
            byte c4 = value[valueOffset + 4];
            byte c5 = value[valueOffset + 5];
            byte c6 = value[valueOffset + 6];
            byte c7 = value[valueOffset + 7];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\' || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ' || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' '
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'
                    || c4 == '<' || c4 == '>' || c4 == '(' || c4 == ')'
                    || c5 == '<' || c5 == '>' || c5 == '(' || c5 == ')'
                    || c6 == '<' || c6 == '>' || c6 == '(' || c6 == ')'
                    || c7 == '<' || c7 == '>' || c7 == '(' || c7 == ')'))
            ) {
                escape = true;
                break;
            }
            valueOffset += 8;
        }

        // vector optimize 4
        if (!escape) {
            while (valueOffset + 4 <= value.length) {
                byte c0 = value[valueOffset];
                byte c1 = value[valueOffset + 1];
                byte c2 = value[valueOffset + 2];
                byte c3 = value[valueOffset + 3];
                if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                        || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                        || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                        || (browserSecure
                        && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                        || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                        || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                        || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
                ) {
                    escape = true;
                    break;
                }
                valueOffset += 4;
            }
        }

        if (!escape && valueOffset + 2 <= value.length) {
            byte c0 = value[valueOffset];
            byte c1 = value[valueOffset + 1];
            if (c0 == quote || c1 == quote || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' '
                    || (browserSecure && (c0 == '<' || c0 == '>' || c0 == '('
                    || c0 == ')' || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'))
            ) {
                escape = true;
            } else {
                valueOffset += 2;
            }
        }
        if (!escape && valueOffset + 1 == value.length) {
            byte c0 = value[valueOffset];
            escape = c0 == quote || c0 == '\\' || c0 < ' '
                    || (browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'));
        }

        int minCapacity = off
                + (escape ? value.length * 4 : value.length)
                + 2;
        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (!escape) {
            bytes[off++] = (byte) quote;
            System.arraycopy(value, 0, bytes, off, value.length);
            off += value.length;
            bytes[off++] = (byte) quote;
            return;
        }
        writeStringEscaped(value);
    }

    public final void writeString(final char[] chars) {
        if (chars == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += chars.length * 3;
        }

        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;

        int i = 0;

        // vector optimize 8
        while (i + 8 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            char c4 = chars[i + 4];
            char c5 = chars[i + 5];
            char c6 = chars[i + 6];
            char c7 = chars[i + 7];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || c4 > 0x007F || c5 > 0x007F || c6 > 0x007F || c7 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'
                    || c4 == '<' || c4 == '>' || c4 == '(' || c4 == ')'
                    || c5 == '<' || c5 == '>' || c5 == '(' || c5 == ')'
                    || c6 == '<' || c6 == '>' || c6 == '(' || c6 == ')'
                    || c7 == '<' || c7 == '>' || c7 == '(' || c7 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            bytes[off + 4] = (byte) c4;
            bytes[off + 5] = (byte) c5;
            bytes[off + 6] = (byte) c6;
            bytes[off + 7] = (byte) c7;
            off += 8;
            i += 8;
        }

        // vector optimize 4
        while (i + 4 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            off += 4;
            i += 4;
        }

        if (i + 2 <= chars.length) {
            char c0 = chars[i];
            char c1 = chars[i + 1];

            if (!(c0 == quote || c1 == quote
                    || c0 == '\\' || c1 == '\\'
                    || c0 < ' ' || c1 < ' '
                    || c0 > 0x007F || c1 > 0x007F)
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '('
                    || c0 == ')' || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'))
            ) {
                bytes[off] = (byte) c0;
                bytes[off + 1] = (byte) c1;
                off += 2;
                i += 2;
            }
        }
        if (i + 1 == chars.length) {
            char c0 = chars[i];
            if (c0 != quote
                    && c0 != '\\'
                    && c0 >= ' '
                    && c0 <= 0x007F
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
            ) {
                bytes[off++] = (byte) c0;
                bytes[off++] = (byte) quote;
                return;
            }
        }

        int rest = chars.length - i;
        minCapacity = off + rest * 6 + 2;
        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (i < chars.length) {
            writeStringEscapedRest(chars, chars.length, browserSecure, escapeNoneAscii, i);
        }

        bytes[off++] = (byte) quote;
    }

    public final void writeString(final char[] chars, int stroff, int strlen) {
        if (chars == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        int end = stroff + strlen;

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        // ensureCapacity
        int minCapacity = this.off
                + strlen * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += strlen * 3;
        }

        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        bytes[this.off++] = (byte) quote;

        int i = stroff;

        // vector optimize 8
        while (i + 8 <= end) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            char c4 = chars[i + 4];
            char c5 = chars[i + 5];
            char c6 = chars[i + 6];
            char c7 = chars[i + 7];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || c4 > 0x007F || c5 > 0x007F || c6 > 0x007F || c7 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'
                    || c4 == '<' || c4 == '>' || c4 == '(' || c4 == ')'
                    || c5 == '<' || c5 == '>' || c5 == '(' || c5 == ')'
                    || c6 == '<' || c6 == '>' || c6 == '(' || c6 == ')'
                    || c7 == '<' || c7 == '>' || c7 == '(' || c7 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            bytes[off + 4] = (byte) c4;
            bytes[off + 5] = (byte) c5;
            bytes[off + 6] = (byte) c6;
            bytes[off + 7] = (byte) c7;
            off += 8;
            i += 8;
        }

        // vector optimize 4
        while (i + 4 <= end) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                    || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                    || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                    || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
            ) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            off += 4;
            i += 4;
        }

        if (i + 2 <= end) {
            char c0 = chars[i];
            char c1 = chars[i + 1];

            if (!(c0 == quote || c1 == quote
                    || c0 == '\\' || c1 == '\\'
                    || c0 < ' ' || c1 < ' '
                    || c0 > 0x007F || c1 > 0x007F)
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '('
                    || c0 == ')' || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'))
            ) {
                bytes[off] = (byte) c0;
                bytes[off + 1] = (byte) c1;
                off += 2;
                i += 2;
            }
        }
        if (i + 1 == end) {
            char c0 = chars[i];
            if (c0 != quote
                    && c0 != '\\'
                    && c0 >= ' '
                    && c0 <= 0x007F
                    && !(browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
            ) {
                bytes[off++] = (byte) c0;
                bytes[off++] = (byte) quote;
                return;
            }
        }

        int rest = end - i;
        minCapacity = off + rest * 6 + 2;
        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (i < end) {
            writeStringEscapedRest(chars, end, browserSecure, escapeNoneAscii, i);
        }

        bytes[off++] = (byte) quote;
    }

    protected final void writeStringEscaped(byte[] value) {
        final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        bytes[off++] = (byte) quote;
        for (int i = 0; i < value.length; ++i) {
            byte ch = value[i];
            switch (ch) {
                case '\\':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) '\\';
                    break;
                case '\n':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'n';
                    break;
                case '\r':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'r';
                    break;
                case '\f':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'f';
                    break;
                case '\b':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'b';
                    break;
                case '\t':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = (byte) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = (byte) ('a' + (ch - 10));
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
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '1';
                    bytes[off++] = (byte) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '1';
                    bytes[off++] = (byte) ('a' + (ch - 26));
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = (byte) DIGITS[(ch >>> 12) & 15];
                        bytes[off++] = (byte) DIGITS[(ch >>> 8) & 15];
                        bytes[off++] = (byte) DIGITS[(ch >>> 4) & 15];
                        bytes[off++] = (byte) DIGITS[ch & 15];
                    } else {
                        bytes[off++] = ch;
                    }
                    break;
                default:
                    if (ch == quote) {
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) quote;
                    } else if (ch < 0) {
                        // latin
                        int c = ch & 0xFF;
                        bytes[off++] = (byte) (0xc0 | (c >> 6));
                        bytes[off++] = (byte) (0x80 | (c & 0x3f));
                    } else {
                        bytes[off++] = (byte) ch;
                    }
                    break;
            }
        }
        bytes[off++] = (byte) quote;
    }

    private final void writeStringEscapedRest(
            char[] chars,
            int end,
            boolean browserSecure,
            boolean escapeNoneAscii,
            int i
    ) {
        for (; i < end; ++i) { // ascii none special fast write
            char ch = chars[i];
            if ((ch >= 0x0000) && (ch <= 0x007F)) {
                switch (ch) {
                    case '\\':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) '\\';
                        break;
                    case '\n':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'n';
                        break;
                    case '\r':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'r';
                        break;
                    case '\f':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'f';
                        break;
                    case '\b':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'b';
                        break;
                    case '\t':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 't';
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('0' + (int) ch);
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('a' + (ch - 10));
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
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('0' + (ch - 16));
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('a' + (ch - 26));
                        break;
                    case '<':
                    case '>':
                    case '(':
                    case ')':
                        if (browserSecure) {
                            bytes[off++] = '\\';
                            bytes[off++] = 'u';
                            bytes[off++] = (byte) DIGITS[(ch >>> 12) & 15];
                            bytes[off++] = (byte) DIGITS[(ch >>> 8) & 15];
                            bytes[off++] = (byte) DIGITS[(ch >>> 4) & 15];
                            bytes[off++] = (byte) DIGITS[ch & 15];
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off++] = (byte) '\\';
                            bytes[off++] = (byte) quote;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                bytes[off++] = '\\';
                bytes[off++] = 'u';
                bytes[off++] = (byte) DIGITS[(ch >>> 12) & 15];
                bytes[off++] = (byte) DIGITS[(ch >>> 8) & 15];
                bytes[off++] = (byte) DIGITS[(ch >>> 4) & 15];
                bytes[off++] = (byte) DIGITS[ch & 15];
            } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                final int uc;
                if (ch >= '\uD800' && ch < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                    if (chars.length - i < 2) {
                        uc = -1;
                    } else {
                        char d = chars[i + 1];
                        // d >= '\uDC00' && d < ('\uDFFF' + 1)
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                            uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                        } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                            bytes[off++] = (byte) '?';
                            continue;
                        }
                    }
                } else {
                    //
                    if (ch >= '\uDC00' && ch < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                        bytes[off++] = (byte) '?';
                        continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                    } else {
                        uc = ch;
                    }
                }

                if (uc < 0) {
                    bytes[off++] = (byte) '?';
                } else {
                    bytes[off++] = (byte) (0xf0 | ((uc >> 18)));
                    bytes[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    bytes[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    bytes[off++] = (byte) (0x80 | (uc & 0x3f));
                    i++; // 2 chars
                }
            } else if (ch > 0x07FF) {
                bytes[off++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
            }
        }
    }

    @Override
    public final void writeString(char[] chars, int offset, int len, boolean quoted) {
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii) {
            minCapacity += len * 3;
        }

        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (quoted) {
            bytes[off++] = (byte) quote;
        }

        int i = 0;

        // vector optimize 8
        while (i + 8 <= len) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            char c4 = chars[i + 4];
            char c5 = chars[i + 5];
            char c6 = chars[i + 6];
            char c7 = chars[i + 7];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote || c4 == quote || c5 == quote || c6 == quote || c7 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\' || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ' || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F || c4 > 0x007F || c5 > 0x007F || c6 > 0x007F || c7 > 0x007F) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            bytes[off + 4] = (byte) c4;
            bytes[off + 5] = (byte) c5;
            bytes[off + 6] = (byte) c6;
            bytes[off + 7] = (byte) c7;
            off += 8;
            i += 8;
        }

        // vector optimize 4
        while (i + 4 <= len) {
            char c0 = chars[i];
            char c1 = chars[i + 1];
            char c2 = chars[i + 2];
            char c3 = chars[i + 3];
            if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                    || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                    || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                    || c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F) {
                break;
            }

            bytes[off] = (byte) c0;
            bytes[off + 1] = (byte) c1;
            bytes[off + 2] = (byte) c2;
            bytes[off + 3] = (byte) c3;
            off += 4;
            i += 4;
        }

        if (i + 2 <= len) {
            char c0 = chars[i];
            char c1 = chars[i + 1];

            if (!(c0 == quote || c1 == quote
                    || c0 == '\\' || c1 == '\\'
                    || c0 < ' ' || c1 < ' '
                    || c0 > 0x007F || c1 > 0x007F)
            ) {
                bytes[off] = (byte) c0;
                bytes[off + 1] = (byte) c1;
                off += 2;
                i += 2;
            }
        }
        if (i + 1 == len) {
            char c0 = chars[i];
            if (c0 != quote
                    && c0 != '\\'
                    && c0 >= ' '
                    && c0 <= 0x007F
            ) {
                bytes[off++] = (byte) c0;
                if (quoted) {
                    bytes[off++] = (byte) quote;
                }
                return;
            }
        }

        for (; i < len; ++i) { // ascii none special fast write
            char ch = chars[i];
            if ((ch >= 0x0000) && (ch <= 0x007F)) {
                switch (ch) {
                    case '\\':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) '\\';
                        break;
                    case '\n':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'n';
                        break;
                    case '\r':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'r';
                        break;
                    case '\f':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'f';
                        break;
                    case '\b':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 'b';
                        break;
                    case '\t':
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) 't';
                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('0' + (int) ch);
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = (byte) ('a' + (ch - 10));
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
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('0' + (ch - 16));
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off++] = '\\';
                        bytes[off++] = 'u';
                        bytes[off++] = '0';
                        bytes[off++] = '0';
                        bytes[off++] = '1';
                        bytes[off++] = (byte) ('a' + (ch - 26));
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off++] = (byte) '\\';
                            bytes[off++] = (byte) quote;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                bytes[off++] = '\\';
                bytes[off++] = 'u';
                bytes[off++] = (byte) DIGITS[(ch >>> 12) & 15];
                bytes[off++] = (byte) DIGITS[(ch >>> 8) & 15];
                bytes[off++] = (byte) DIGITS[(ch >>> 4) & 15];
                bytes[off++] = (byte) DIGITS[ch & 15];
            } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                final int uc;
                if (ch >= '\uD800' && ch < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                    if (chars.length - i < 2) {
                        uc = -1;
                    } else {
                        char d = chars[i + 1];
                        // d >= '\uDC00' && d < ('\uDFFF' + 1)
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                            uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                        } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                            bytes[off++] = (byte) '?';
                            continue;
                        }
                    }
                } else {
                    //
                    if (ch >= '\uDC00' && ch < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                        bytes[off++] = (byte) '?';
                        continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                    } else {
                        uc = ch;
                    }
                }

                if (uc < 0) {
                    bytes[off++] = (byte) '?';
                } else {
                    bytes[off++] = (byte) (0xf0 | ((uc >> 18)));
                    bytes[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    bytes[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    bytes[off++] = (byte) (0x80 | (uc & 0x3f));
                    i++; // 2 chars
                }
            } else if (ch > 0x07FF) {
                bytes[off++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
            }
        }

        if (quoted) {
            bytes[off++] = (byte) quote;
        }
    }

    @Override
    public final void writeChar(char ch) {
        int minCapacity = this.off + 8;
        if (minCapacity - bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;
        if ((ch >= 0x0000) && (ch <= 0x007F)) {
            switch (ch) {
                case '\\':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) '\\';
                    break;
                case '\n':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'n';
                    break;
                case '\r':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'r';
                    break;
                case '\f':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'f';
                    break;
                case '\b':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 'b';
                    break;
                case '\t':
                    bytes[off++] = (byte) '\\';
                    bytes[off++] = (byte) 't';
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = (byte) ('0' + (int) ch);
                    break;
                case 11:
                case 14:
                case 15:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = (byte) ('a' + (ch - 10));
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
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '1';
                    bytes[off++] = (byte) ('0' + (ch - 16));
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    bytes[off++] = '\\';
                    bytes[off++] = 'u';
                    bytes[off++] = '0';
                    bytes[off++] = '0';
                    bytes[off++] = '1';
                    bytes[off++] = (byte) ('a' + (ch - 26));
                    break;
                default:
                    if (ch == quote) {
                        bytes[off++] = (byte) '\\';
                        bytes[off++] = (byte) quote;
                    } else {
                        bytes[off++] = (byte) ch;
                    }
                    break;
            }
        } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
            throw new JSONException("illegal char " + ch);
        } else if (ch > 0x07FF) {
            bytes[off++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
            bytes[off++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
            bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
        } else {
            bytes[off++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
            bytes[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
        }

        bytes[off++] = (byte) quote;
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
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }
        bytes[off++] = '"';
        formatUnsignedLong0(lsb, bytes, off + 24, 12);
        formatUnsignedLong0(lsb >>> 48, bytes, off + 19, 4);
        formatUnsignedLong0(msb, bytes, off + 14, 4);
        formatUnsignedLong0(msb >>> 16, bytes, off + 9, 4);
        formatUnsignedLong0(msb >>> 32, bytes, off + 0, 8);

        bytes[off + 23] = '-';
        bytes[off + 18] = '-';
        bytes[off + 13] = '-';
        bytes[off + 8] = '-';
        off += 36;
        bytes[off++] = '"';
    }

    @Override
    public final void writeRaw(String str) {
        char[] chars = JDKUtils.getCharArray(str);
        {
            int minCapacity = off
                    + chars.length * 3; // utf8 3 bytes

            if (minCapacity - this.bytes.length > 0) {
                ensureCapacity(minCapacity);
            }
        }
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytes[off++] = (byte) c;
            } else if (c > 0x07FF) {
                bytes[off++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }
    }

    @Override
    public final void writeRaw(byte[] bytes) {
        {
            // inline ensureCapacity
            int minCapacity = off + bytes.length;
            if (minCapacity - this.bytes.length > 0) {
                ensureCapacity(minCapacity);
            }
        }
        System.arraycopy(bytes, 0, this.bytes, this.off, bytes.length);
        off += bytes.length;
    }

    @Override
    public final void writeNameRaw(byte[] bytes) {
        {
            // inline ensureCapacity
            int minCapacity = off + bytes.length + (startObject ? 0 : 1);
            if (minCapacity - this.bytes.length > 0) {
                ensureCapacity(minCapacity);
            }
        }
        if (startObject) {
            startObject = false;
        } else {
            this.bytes[off++] = ',';
        }
        System.arraycopy(bytes, 0, this.bytes, this.off, bytes.length);
        off += bytes.length;
    }

    @Override
    public final void writeRaw(char ch) {
        if (ch > 128) {
            throw new JSONException("not support " + ch);
        }

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) ch;
    }

    @Override
    public final void writeRaw(char c0, char c1) {
        if (c0 > 128) {
            throw new JSONException("not support " + c0);
        }
        if (c1 > 128) {
            throw new JSONException("not support " + c1);
        }

        if (off + 1 >= bytes.length) {
            ensureCapacity(off + 2);
        }
        bytes[off++] = (byte) c0;
        bytes[off++] = (byte) c1;
    }

    @Override
    public final void writeNameRaw(byte[] bytes, int off, int len) {
        {
            // inline ensureCapacity
            int minCapacity = this.off + len + (startObject ? 0 : 1);
            if (minCapacity - this.bytes.length > 0) {
                ensureCapacity(minCapacity);
            }
        }

        if (startObject) {
            startObject = false;
        } else {
            this.bytes[this.off++] = ',';
        }
        System.arraycopy(bytes, off, this.bytes, this.off, len);
        this.off += len;
    }

    final void ensureCapacity(int minCapacity) {
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - maxArraySize > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
    }

    public final void writeInt32(int[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '[';

        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = (byte) ',';
            }
            writeInt32(value[i]);
        }

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) ']';
    }

    @Override
    public final void writeInt32(int i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        if (i == Integer.MIN_VALUE) {
            writeRaw(writeAsString ? "\"-2147483648\"" : "-2147483648");
            return;
        }

        boolean negative = i < 0;
        if (negative) {
            i = -i;
        }

        int size;
        if (i <= 9) {
            size = 1;
        } else if (i <= 99) {
            size = 2;
        } else if (i <= 999) {
            size = 3;
        } else if (i <= 9999) {
            size = 4;
        } else if (i <= 99999) {
            size = 5;
        } else if (i <= 999999) {
            size = 6;
        } else if (i <= 9999999) {
            size = 7;
        } else if (i <= 99999999) {
            size = 8;
        } else if (i <= 999999999) {
            size = 9;
        } else {
            size = 10;
        }
        if (negative) {
            size++;
        }

        // reduce getfield
        byte[] buffer = bytes;
        {
            // inline code to reduce invokespecial
            int minCapacity = off + size;
            if (writeAsString) {
                minCapacity += 2;
            }
            if (minCapacity - buffer.length > 0) {
                ensureCapacity(minCapacity);
                buffer = bytes;
            }
        }

        if (writeAsString) {
            buffer[off++] = '"';
        }

        int q, r;
        int pos = off += size;

        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buffer[--pos] = DigitOnes[r];
            buffer[--pos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        do {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buffer[--pos] = digits[r];
            i = q;
        } while (i != 0);

        if (negative) {
            buffer[--pos] = '-';
        }

        if (writeAsString) {
            buffer[off++] = '"';
        }
    }

    public final void writeInt64(long[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) '[';

        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = (byte) ',';
            }
            writeInt64(value[i]);
        }
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = (byte) ']';
    }

    @Override
    public final void writeInt64(long i) {
        boolean writeAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                || ((context.features & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));

        if (i == Long.MIN_VALUE) {
            writeRaw(writeAsString ? "\"-9223372036854775808\"" : "-9223372036854775808");
            return;
        }

        boolean negative = i < 0;
        if (negative) {
            i = -i;
        }

        int size;
        if (i <= 9) {
            size = 1;
        } else if (i <= 99L) {
            size = 2;
        } else if (i <= 999L) {
            size = 3;
        } else if (i <= 9999L) {
            size = 4;
        } else if (i <= 99999L) {
            size = 5;
        } else if (i <= 999999L) {
            size = 6;
        } else if (i <= 9999999L) {
            size = 7;
        } else if (i <= 99999999L) {
            size = 8;
        } else if (i <= 999999999L) {
            size = 9;
        } else if (i <= 9999999999L) {
            size = 10;
        } else if (i <= 99999999999L) {
            size = 11;
        } else if (i <= 999999999999L) {
            size = 12;
        } else if (i <= 9999999999999L) {
            size = 13;
        } else if (i <= 99999999999999L) {
            size = 14;
        } else if (i <= 999999999999999L) {
            size = 15;
        } else if (i <= 9999999999999999L) {
            size = 16;
        } else if (i <= 99999999999999999L) {
            size = 17;
        } else if (i <= 999999999999999999L) {
            size = 18;
        } else {
            size = 19;
        }

        if (negative) {
            size++;
        }

        // reduce getfield
        byte[] buffer = bytes;
        {
            // inline code to reduce invokespecial
            int minCapacity = off + size;
            if (writeAsString) {
                minCapacity += 2;
            }
            if (minCapacity - buffer.length > 0) {
                ensureCapacity(minCapacity);
                buffer = bytes;
            }
        }

        if (writeAsString) {
            buffer[off++] = '"';
        }

        long q;
        int r, pos = off += size;

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buffer[--pos] = DigitOnes[r];
            buffer[--pos] = DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2, i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buffer[--pos] = DigitOnes[r];
            buffer[--pos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        do {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buffer[--pos] = digits[r];
            i2 = q2;
        } while (i2 != 0);

        if (negative) {
            buffer[--pos] = '-';
        }

        if (writeAsString) {
            buffer[off++] = '"';
        }
    }

    @Override
    public final void writeFloat(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            writeNull();
            return;
        }

        boolean writeNonStringValueAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 15;
        if (writeNonStringValueAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);

        if (writeNonStringValueAsString) {
            bytes[off++] = '"';
        }

        int len = RyuDouble.toString(value, bytes, off);
        off += len;

        if (writeNonStringValueAsString) {
            bytes[off++] = '"';
        }
    }

    @Override
    public final void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            writeNull();
            return;
        }

        boolean writeNonStringValueAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 24;
        if (writeNonStringValueAsString) {
            minCapacity += 2;
        }

        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (writeNonStringValueAsString) {
            bytes[off++] = '"';
        }

        int len = RyuDouble.toString(value, bytes, off);
        off += len;

        if (writeNonStringValueAsString) {
            bytes[off++] = '"';
        }
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

        bytes[off++] = (byte) quote;

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
        bytes[off++] = (byte) (hour / 10 + '0');
        bytes[off++] = (byte) (hour % 10 + '0');
        bytes[off++] = (byte) (minute / 10 + '0');
        bytes[off++] = (byte) (minute % 10 + '0');
        bytes[off++] = (byte) (second / 10 + '0');
        bytes[off++] = (byte) (second % 10 + '0');

        bytes[off++] = (byte) quote;
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

        bytes[off++] = (byte) quote;

        bytes[off++] = (byte) (year / 1000 + '0');
        bytes[off++] = (byte) ((year / 100) % 10 + '0');
        bytes[off++] = (byte) ((year / 10) % 10 + '0');
        bytes[off++] = (byte) (year % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (month / 10 + '0');
        bytes[off++] = (byte) (month % 10 + '0');
        bytes[off++] = '-';
        bytes[off++] = (byte) (dayOfMonth / 10 + '0');
        bytes[off++] = (byte) (dayOfMonth % 10 + '0');
        bytes[off++] = ' ';
        bytes[off++] = (byte) (hour / 10 + '0');
        bytes[off++] = (byte) (hour % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (minute / 10 + '0');
        bytes[off++] = (byte) (minute % 10 + '0');
        bytes[off++] = ':';
        bytes[off++] = (byte) (second / 10 + '0');
        bytes[off++] = (byte) (second % 10 + '0');

        bytes[off++] = (byte) quote;
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
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }
        bytes[off++] = (byte) quote;
        writeLocalDate0(date);
        bytes[off++] = (byte) quote;
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime dateTime) {
        int minCapacity = off + 38;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;
        writeLocalDate0(dateTime.toLocalDate());
        bytes[off++] = ' ';
        writeLocalTime0(dateTime.toLocalTime());
        bytes[off++] = (byte) quote;
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int minCapacity = off + 10;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off] = (byte) quote;
        bytes[off + 1] = (byte) (year / 1000 + '0');
        bytes[off + 2] = (byte) ((year / 100) % 10 + '0');
        bytes[off + 3] = (byte) ((year / 10) % 10 + '0');
        bytes[off + 4] = (byte) (year % 10 + '0');
        bytes[off + 5] = (byte) (month / 10 + '0');
        bytes[off + 6] = (byte) (month % 10 + '0');
        bytes[off + 7] = (byte) (dayOfMonth / 10 + '0');
        bytes[off + 8] = (byte) (dayOfMonth % 10 + '0');
        bytes[off + 9] = (byte) quote;
        off += 10;
    }

    @Override
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int minCapacity = off + 12;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off] = (byte) quote;
        bytes[off + 1] = (byte) (year / 1000 + '0');
        bytes[off + 2] = (byte) ((year / 100) % 10 + '0');
        bytes[off + 3] = (byte) ((year / 10) % 10 + '0');
        bytes[off + 4] = (byte) (year % 10 + '0');
        bytes[off + 5] = '-';
        bytes[off + 6] = (byte) (month / 10 + '0');
        bytes[off + 7] = (byte) (month % 10 + '0');
        bytes[off + 8] = '-';
        bytes[off + 9] = (byte) (dayOfMonth / 10 + '0');
        bytes[off + 10] = (byte) (dayOfMonth % 10 + '0');
        bytes[off + 11] = (byte) quote;
        off += 12;
    }

    @Override
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
        int minCapacity = off + 10;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off] = (byte) quote;
        bytes[off + 1] = (byte) (hour / 10 + '0');
        bytes[off + 2] = (byte) (hour % 10 + '0');
        bytes[off + 3] = ':';
        bytes[off + 4] = (byte) (minute / 10 + '0');
        bytes[off + 5] = (byte) (minute % 10 + '0');
        bytes[off + 6] = ':';
        bytes[off + 7] = (byte) (second / 10 + '0');
        bytes[off + 8] = (byte) (second % 10 + '0');
        bytes[off + 9] = (byte) quote;

        off += 10;
    }

    @Override
    public final void writeLocalTime(LocalTime time) {
        int minCapacity = off + 20;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }
        bytes[off++] = (byte) quote;
        writeLocalTime0(time);
        bytes[off++] = (byte) quote;
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
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;
        writeLocalDate0(dateTime.toLocalDate());
        bytes[off++] = 'T';
        writeLocalTime0(dateTime.toLocalTime());
        if (zoneSize == 1) {
            bytes[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getBytes(0, zoneId.length(), bytes, off);
            off += zoneId.length();
        } else {
            bytes[off++] = '[';
            zoneId.getBytes(0, zoneId.length(), bytes, off);
            off += zoneId.length();
            bytes[off++] = ']';
        }
        bytes[off++] = (byte) quote;
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
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        bytes[off++] = (byte) quote;
        LocalDateTime ldt = dateTime.toLocalDateTime();
        writeLocalDate0(ldt.toLocalDate());
        bytes[off++] = 'T';
        writeLocalTime0(ldt.toLocalTime());
        if (zoneSize == 1) {
            bytes[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getBytes(0, zoneId.length(), bytes, off);
            off += zoneId.length();
        } else {
            bytes[off++] = '[';
            zoneId.getBytes(0, zoneId.length(), bytes, off);
            off += zoneId.length();
            bytes[off++] = ']';
        }
        bytes[off++] = (byte) quote;
    }

    final void writeLocalDate0(LocalDate localDate) {
        int year = localDate.getYear();
        if (year >= 1000 && year < 10000) {
            bytes[off++] = (byte) (year / 1000 + '0');
            bytes[off++] = (byte) ((year / 100) % 10 + '0');
            bytes[off++] = (byte) ((year / 10) % 10 + '0');
            bytes[off++] = (byte) (year % 10 + '0');
        } else {
            int yearSize = year > 0 ? IOUtils.stringSize(year) : IOUtils.stringSize(-year) + 1;
            IOUtils.getChars(year, off + yearSize, bytes);
            off += yearSize;
        }
        bytes[off++] = '-';

        int month = localDate.getMonthValue();
        if (month < 10) {
            bytes[off++] = '0';
            bytes[off++] = (byte) (month + '0');
        } else {
            int m0 = month / 10;
            int m1 = month % 10;
            bytes[off++] = (byte) (m0 + '0');
            bytes[off++] = (byte) (m1 + '0');
        }
        bytes[off++] = '-';

        int dayOfMonth = localDate.getDayOfMonth();
        if (dayOfMonth < 10) {
            bytes[off++] = '0';
            bytes[off++] = (byte) (dayOfMonth + '0');
        } else {
            int d0 = dayOfMonth / 10;
            int d1 = dayOfMonth % 10;
            bytes[off++] = (byte) (d0 + '0');
            bytes[off++] = (byte) (d1 + '0');
        }
    }

    final void writeLocalTime0(LocalTime time) {
        int hour = time.getHour();
        if (hour < 10) {
            bytes[off++] = '0';
            bytes[off++] = (byte) (hour + '0');
        } else {
            int h0 = hour / 10;
            int h1 = hour % 10;
            bytes[off++] = (byte) (h0 + '0');
            bytes[off++] = (byte) (h1 + '0');
        }
        bytes[off++] = ':';

        int minute = time.getMinute();
        if (minute < 10) {
            bytes[off++] = '0';
            bytes[off++] = (byte) (minute + '0');
        } else {
            int i0 = minute / 10;
            int i1 = minute % 10;
            bytes[off++] = (byte) (i0 + '0');
            bytes[off++] = (byte) (i1 + '0');
        }
        bytes[off++] = ':';

        int second = time.getSecond();
        if (second < 10) {
            bytes[off++] = '0';
            bytes[off++] = (byte) (second + '0');
        } else {
            int s0 = second / 10;
            int s1 = second % 10;
            bytes[off++] = (byte) (s0 + '0');
            bytes[off++] = (byte) (s1 + '0');
        }

        int nano = time.getNano();
        if (nano != 0) {
            int small, size;
            int m0 = nano % 1000_000;
            if (m0 == 0) {
                small = nano / 1000_000 + 1000;
                size = 4;
                IOUtils.getChars(small, off + size, bytes);
                bytes[off] = '.';
                off += size;
                return;
            }

            if (m0 % 1000 == 0) {
                small = nano / 1000 + 1000_000;
                size = 7;
                IOUtils.getChars(small, off + size, bytes);
                bytes[off] = '.';
                off += size;
                return;
            }

            if (nano >= 100_000_000) {
                bytes[off++] = '.';
                IOUtils.getChars(nano, off + 9, bytes);
                off += 9;
            } else {
                small = nano + 1000_000_000;
                size = 10;
                IOUtils.getChars(small, off + size, bytes);
                bytes[off] = '.';
                off += size;
            }
        }
    }

    @Override
    public final void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        String str = value.toString(10);

        if (((context.features | features) & Feature.BrowserCompatible.mask) != 0
                && (value.compareTo(LOW_BIGINT) < 0 || value.compareTo(HIGH_BIGINT) > 0)) {
            writeString(str);
            return;
        }

        int strlen = str.length();
        {
            // inline ensureCapacity
            int minCapacity = off + strlen;
            if (minCapacity - this.bytes.length > 0) {
                ensureCapacity(minCapacity);
            }
        }
        str.getBytes(0, strlen, this.bytes, off);
        off += strlen;
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
        int millislen = millis == 0 ? 0 : IOUtils.stringSize(millis) + 1;
        if (millis == 0) {
            millislen = 0;
        } else if (millis < 10) {
            millislen = 4;
        } else {
            if (millis % 100 == 0) {
                millislen = 2;
            } else if (millis % 10 == 0) {
                millislen = 3;
            } else {
                millislen = 4;
            }
        }
        int zonelen;
        if (timeZone) {
            zonelen = offsetSeconds == 0 ? 1 : 6;
        } else {
            zonelen = 0;
        }
        int offset = offsetSeconds / 3600;
        int len = 21 + millislen + zonelen;
        ensureCapacity(off + len);

        bytes[off] = '"';
        bytes[off + 1] = (byte) (year / 1000 + '0');
        bytes[off + 2] = (byte) ((year / 100) % 10 + '0');
        bytes[off + 3] = (byte) ((year / 10) % 10 + '0');
        bytes[off + 4] = (byte) (year % 10 + '0');
        bytes[off + 5] = '-';
        bytes[off + 6] = (byte) (month / 10 + '0');
        bytes[off + 7] = (byte) (month % 10 + '0');
        bytes[off + 8] = '-';
        bytes[off + 9] = (byte) (dayOfMonth / 10 + '0');
        bytes[off + 10] = (byte) (dayOfMonth % 10 + '0');
        bytes[off + 11] = timeZone ? (byte) 'T' : (byte) ' ';
        bytes[off + 12] = (byte) (hour / 10 + '0');
        bytes[off + 13] = (byte) (hour % 10 + '0');
        bytes[off + 14] = ':';
        bytes[off + 15] = (byte) (minute / 10 + '0');
        bytes[off + 16] = (byte) (minute % 10 + '0');
        bytes[off + 17] = ':';
        bytes[off + 18] = (byte) (second / 10 + '0');
        bytes[off + 19] = (byte) (second % 10 + '0');
        if (millislen > 0) {
            bytes[off + 20] = '.';
            Arrays.fill(bytes, off + 21, off + 20 + millislen, (byte) '0');
            if (millis < 10) {
                IOUtils.getChars(millis, off + 20 + millislen, bytes);
            } else {
                if (millis % 100 == 0) {
                    IOUtils.getChars(millis / 100, off + 20 + millislen, bytes);
                } else if (millis % 10 == 0) {
                    IOUtils.getChars(millis / 10, off + 20 + millislen, bytes);
                } else {
                    IOUtils.getChars(millis, off + 20 + millislen, bytes);
                }
            }
        }

        if (timeZone) {
            if (offsetSeconds == 0) {
                bytes[off + 20 + millislen] = 'Z';
            } else {
                int offsetAbs = Math.abs(offset);

                if (offset >= 0) {
                    bytes[off + 20 + millislen] = '+';
                } else {
                    bytes[off + 20 + millislen] = '-';
                }
                bytes[off + 20 + millislen + 1] = '0';
                IOUtils.getChars(offsetAbs, off + 20 + millislen + 3, bytes);
                bytes[off + 20 + millislen + 3] = ':';
                bytes[off + 20 + millislen + 4] = '0';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                IOUtils.getChars(offsetMinutes, off + 20 + millislen + zonelen, bytes);
            }
        }
        bytes[off + len - 1] = '"';
        off += len;
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

        int minCapacity = off + precision + 4;
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (browserCompatible) {
            bytes[off++] = '"';
        }

        long unscaleValue;
        if ((features & Feature.WriteBigDecimalAsPlain.mask) != 0) {
            if (precision < 19
                    && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1
                    && (unscaleValue = UnsafeUtils.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET)) != Long.MIN_VALUE
            ) {
                int scale = value.scale();
                off += getDecimalChars(unscaleValue, scale, bytes, off);
            } else {
                String str = value.toPlainString();
                str.getBytes(0, str.length(), bytes, off);
                off += str.length();
            }
        } else {
            String str = value.toString();
            int strlen = str.length();
            str.getBytes(0, strlen, bytes, off);
            off += strlen;
        }

        if (browserCompatible) {
            bytes[off++] = '"';
        }
    }

    @Override
    public final void writeNameRaw(char[] chars) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public final void writeNameRaw(char[] bytes, int offset, int len) {
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

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = '{';

        boolean first = true;
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            if (!first) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = ',';
            }

            Map.Entry<String, Object> next = it.next();
            Object value = next.getValue();
            if (value == null && (context.features & Feature.WriteMapNullValue.mask) == 0) {
                continue;
            }

            first = false;
            writeString(next.getKey());

            if (off == bytes.length) {
                ensureCapacity(off + 1);
            }
            bytes[off++] = ':';

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

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = '}';
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

        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = '[';

        boolean first = true;
        for (int i = 0, size = array.size(); i < size; i++) {
            if (!first) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = ',';
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
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = ']';
    }

    @Override
    public final String toString() {
        return new String(bytes, 0, off, StandardCharsets.UTF_8);
    }

    static void formatUnsignedLong0(long val, byte[] buf, int offset, int len) { // for uuid
        int charPos = offset + len;
        int radix = 16;
        int mask = radix - 1;
        do {
            buf[--charPos] = (byte) DIGITS[((int) val) & mask];
            val >>>= 4;
        } while (charPos > offset);
    }

    @Override
    public final int flushTo(OutputStream out, Charset charset) throws IOException {
        if (charset != null && charset != StandardCharsets.UTF_8) {
            throw new JSONException("UnsupportedOperation");
        }
        if (off == 0) {
            return 0;
        }

        int len = off;
        out.write(bytes, 0, off);
        off = 0;
        return len;
    }
}

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
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
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
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
        this.bytes = bytes;
    }

    @Override
    public final void writeReference(String path) {
        this.lastReference = path;

        writeRaw(REF_PREF);
        writeString(path);
        int off = this.off;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off] = '}';
        this.off = off + 1;
    }

    @Override
    public final void writeBase64(byte[] value) {
        int charsLen = ((value.length - 1) / 3 + 1) << 2; // base64 character count

        int off = this.off;
        ensureCapacity(off + charsLen + 2);
        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;

        int eLen = (value.length / 3) * 3; // Length of even 24-bits.

        for (int s = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (value[s++] & 0xff) << 16 | (value[s++] & 0xff) << 8 | (value[s++] & 0xff);

            // Encode the int into four chars
            bytes[off++] = (byte) CA[(i >>> 18) & 0x3f];
            bytes[off++] = (byte) CA[(i >>> 12) & 0x3f];
            bytes[off++] = (byte) CA[(i >>> 6) & 0x3f];
            bytes[off++] = (byte) CA[i & 0x3f];
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = value.length - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((value[eLen] & 0xff) << 10) | (left == 2 ? ((value[value.length - 1] & 0xff) << 2) : 0);

            // Set last four chars
            bytes[off++] = (byte) CA[i >> 12];
            bytes[off++] = (byte) CA[(i >>> 6) & 0x3f];
            bytes[off++] = left == 2 ? (byte) CA[i & 0x3f] : (byte) '=';
            bytes[off++] = '=';
        }

        bytes[off++] = (byte) quote;
        this.off = off;
    }

    @Override
    public final void writeHex(byte[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        int charsLen = value.length * 2 + 3;

        int off = this.off;
        ensureCapacity(off + charsLen + 2);
        final byte[] bytes = this.bytes;
        bytes[off++] = 'x';
        bytes[off++] = '\'';

        for (byte b : value) {
            int a = b & 0xFF;
            int b0 = a >> 4;
            int b1 = a & 0xf;

            bytes[off++] = (byte) (b0 + (b0 < 10 ? 48 : 55));
            bytes[off++] = (byte) (b1 + (b1 < 10 ? 48 : 55));
        }

        bytes[off++] = '\'';
        this.off = off;
    }

    @Override
    public final void close() {
        byte[] bytes = this.bytes;
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
        int off = this.off;
        if (off > 0) {
            to.write(bytes, 0, off);
            this.off = 0;
        }
        return off;
    }

    @Override
    protected final void write0(char c) {
        int off = this.off;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off] = (byte) c;
        this.off = off + 1;
    }

    @Override
    public final void writeColon() {
        int off = this.off;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off] = ':';
        this.off = off + 1;
    }

    @Override
    public final void startObject() {
        level++;
        startObject = true;

        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) '{';

        if (pretty) {
            indent++;
            bytes[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                bytes[off++] = (byte) '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void endObject() {
        level--;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (pretty) {
            indent--;
            bytes[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                bytes[off++] = (byte) '\t';
            }
        }

        bytes[off++] = (byte) '}';
        this.off = off;
        startObject = false;
    }

    @Override
    public final void writeComma() {
        startObject = false;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = ',';
        if (pretty) {
            bytes[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                bytes[off++] = '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void startArray() {
        level++;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) '[';
        if (pretty) {
            indent++;
            bytes[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                bytes[off++] = (byte) '\t';
            }
        }
        this.off = off;
    }

    @Override
    public final void endArray() {
        level--;
        int off = this.off;
        int minCapacity = off + (pretty ? 2 + indent : 1);
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (pretty) {
            indent--;
            bytes[off++] = (byte) '\n';
            for (int i = 0; i < indent; ++i) {
                bytes[off++] = (byte) '\t';
            }
        }
        bytes[off++] = (byte) ']';
        this.off = off;
        startObject = false;
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        char[] chars = JDKUtils.getCharArray(str);

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int off = this.off;
        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += chars.length * 3;
        }

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;

        int i = 0;
        for (; i < chars.length; i++) {
            char c0 = chars[i];
            if (c0 == quote
                    || c0 == '\\'
                    || c0 < ' '
                    || c0 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
            ) {
                break;
            }
            bytes[off++] = (byte) c0;
        }

        if (i == chars.length) {
            bytes[off] = (byte) quote;
            this.off = off + 1;
            return;
        }

        this.off = off;
        if (i < chars.length) {
            writeStringEscapedRest(chars, chars.length, browserSecure, escapeNoneAscii, i);
        }

        this.bytes[this.off++] = (byte) quote;
    }

    public void writeStringLatin1(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean escape = false;
        final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;

        final byte quote = (byte) this.quote;
        for (byte c : value) {
            if (c == quote
                    || c == '\\'
                    || c < ' '
                    || (browserSecure
                    && (c == '<' || c == '>' || c == '(' || c == ')'))
            ) {
                escape = true;
                break;
            }
        }

        int off = this.off;
        if (!escape) {
            int minCapacity = off + value.length + 2;
            if (minCapacity >= this.bytes.length) {
                ensureCapacity(minCapacity);
            }
            final byte[] bytes = this.bytes;
            bytes[off] = quote;
            System.arraycopy(value, 0, bytes, off + 1, value.length);
            off += value.length + 1;
            bytes[off] = quote;
            this.off = off + 1;
            return;
        }
        writeStringEscaped(value);
    }

    public final void writeStringUTF16(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int off = this.off;
        int minCapacity = off + value.length * 4 + 2;
        if (escapeNoneAscii) {
            minCapacity += value.length * 2;
        }

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;

        int valueOffset = 0;
        while (valueOffset < value.length) {
            byte b0 = value[valueOffset++];
            byte b1 = value[valueOffset++];

            if (b1 == 0 && b0 >= 0) {
//                bytes[off++] = b0;
                switch (b0) {
                    case '\\':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) '\\';
                        off += 2;
                        break;
                    case '\n':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'n';
                        off += 2;
                        break;
                    case '\r':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'r';
                        off += 2;
                        break;
                    case '\f':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'f';
                        off += 2;
                        break;
                    case '\b':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'b';
                        off += 2;
                        break;
                    case '\t':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 't';
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('0' + (int) b0);
                        off += 6;
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('a' + (b0 - 10));
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('0' + (b0 - 16));
                        off += 6;
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('a' + (b0 - 26));
                        off += 6;
                        break;
                    default:
                        if (b0 == quote) {
                            bytes[off] = (byte) '\\';
                            bytes[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            bytes[off++] = b0;
                        }
                        break;
                }
            } else {
                char c = (char) ((b0 & 0xff) | ((b1 & 0xff) << 8));
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    bytes[off] = (byte) (0xc0 | (c >> 6));
                    bytes[off + 1] = (byte) (0x80 | (c & 0x3f));
                    off += 2;
                } else if (escapeNoneAscii) {
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = (byte) DIGITS[(c >>> 12) & 15];
                    bytes[off + 3] = (byte) DIGITS[(c >>> 8) & 15];
                    bytes[off + 4] = (byte) DIGITS[(c >>> 4) & 15];
                    bytes[off + 5] = (byte) DIGITS[c & 15];
                    off += 6;
                } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) { //Character.isSurrogate(c) but 1.7
                    final int uc;
                    int ip = valueOffset - 1;
                    if (c >= '\uD800' && c < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                        if (value.length - ip < 2) {
                            uc = -1;
                        } else {
                            b0 = value[ip + 1];
                            b1 = value[ip + 2];
                            char d = (char) ((b0 & 0xff) | ((b1 & 0xff) << 8));
                            // d >= '\uDC00' && d < ('\uDFFF' + 1)
                            if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                valueOffset += 2;
                                uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                            } else {
                                bytes[off++] = '?';
                                continue;
                            }
                        }
                    } else {
                        //
                        if (c >= '\uDC00' && c < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                            bytes[off++] = '?';
                            continue;
                        } else {
                            uc = c;
                        }
                    }

                    if (uc < 0) {
                        bytes[off++] = (byte) '?';
                    } else {
                        bytes[off] = (byte) (0xf0 | ((uc >> 18)));
                        bytes[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        bytes[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        bytes[off + 3] = (byte) (0x80 | (uc & 0x3f));
                        off += 4;
                    }
                } else {
                    // 3 bytes, 16 bits
                    bytes[off] = (byte) (0xe0 | ((c >> 12)));
                    bytes[off + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    bytes[off + 2] = (byte) (0x80 | (c & 0x3f));
                    off += 3;
                }
            }
        }

        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    public final void writeString(final char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int off = this.off;
        int minCapacity = off
                + chars.length * 3 /* utf8 3 bytes */
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += chars.length * 3;
        }

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;

        int i = 0;
        for (; i < chars.length; i++) {
            char c = chars[i];
            if (c == quote
                    || c == '\\'
                    || c < ' '
                    || c > 0x007F
                    || (browserSecure
                    && (c == '<' || c == '>' || c == '(' || c == ')'))
            ) {
                break;
            }
            bytes[off++] = (byte) c;
        }

        this.off = off;
        int rest = chars.length - i;
        minCapacity = off + rest * 6 + 2;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (i < chars.length) {
            writeStringEscapedRest(chars, chars.length, browserSecure, escapeNoneAscii, i);
        }

        this.bytes[this.off++] = (byte) quote;
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

        int off = this.off;
        // ensureCapacity
        int minCapacity = off
                + strlen * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += strlen * 3;
        }

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;

        int i = stroff;
        for (; i < end; i++) {
            char c0 = chars[i];
            if (c0 == quote
                    || c0 == '\\'
                    || c0 < ' '
                    || c0 > 0x007F
                    || (browserSecure
                    && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
            ) {
                break;
            }
            bytes[off++] = (byte) c0;
        }
        this.off = off;

        int rest = end - i;
        minCapacity = off + rest * 6 + 2;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (i < end) {
            writeStringEscapedRest(chars, end, browserSecure, escapeNoneAscii, i);
        }

        this.bytes[this.off++] = (byte) quote;
    }

    protected final void writeStringEscaped(byte[] value) {
        int minCapacity = off + value.length * 4 + 2;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        final byte[] bytes = this.bytes;
        int off = this.off;
        bytes[off++] = (byte) quote;
        for (byte ch : value) {
            switch (ch) {
                case '\\':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) '\\';
                    off += 2;
                    break;
                case '\n':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'n';
                    off += 2;
                    break;
                case '\r':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'r';
                    off += 2;
                    break;
                case '\f':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'f';
                    off += 2;
                    break;
                case '\b':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'b';
                    off += 2;
                    break;
                case '\t':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 't';
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
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '0';
                    bytes[off + 5] = (byte) ('0' + (int) ch);
                    off += 6;
                    break;
                case 11:
                case 14:
                case 15:
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '0';
                    bytes[off + 5] = (byte) ('a' + (ch - 10));
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
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '1';
                    bytes[off + 5] = (byte) ('0' + (ch - 16));
                    off += 6;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '1';
                    bytes[off + 5] = (byte) ('a' + (ch - 26));
                    off += 6;
                    break;
                case '<':
                case '>':
                case '(':
                case ')':
                    if (browserSecure) {
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = (byte) DIGITS[(ch >>> 12) & 15];
                        bytes[off + 3] = (byte) DIGITS[(ch >>> 8) & 15];
                        bytes[off + 4] = (byte) DIGITS[(ch >>> 4) & 15];
                        bytes[off + 5] = (byte) DIGITS[ch & 15];
                        off += 6;
                    } else {
                        bytes[off++] = ch;
                    }
                    break;
                default:
                    if (ch == quote) {
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) quote;
                        off += 2;
                    } else if (ch < 0) {
                        // latin
                        int c = ch & 0xFF;
                        bytes[off] = (byte) (0xc0 | (c >> 6));
                        bytes[off + 1] = (byte) (0x80 | (c & 0x3f));
                        off += 2;
                    } else {
                        bytes[off++] = ch;
                    }
                    break;
            }
        }
        bytes[off++] = (byte) quote;
        this.off = off;
    }

    protected final void writeStringEscapedRest(
            char[] chars,
            int end,
            boolean browserSecure,
            boolean escapeNoneAscii,
            int i
    ) {
        int rest = chars.length - i;
        int minCapacity = off + rest * 6 + 2;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        for (; i < end; ++i) { // ascii none special fast write
            char ch = chars[i];
            if ((ch >= 0x0000) && (ch <= 0x007F)) {
                switch (ch) {
                    case '\\':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) '\\';
                        off += 2;
                        break;
                    case '\n':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'n';
                        off += 2;
                        break;
                    case '\r':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'r';
                        off += 2;
                        break;
                    case '\f':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'f';
                        off += 2;
                        break;
                    case '\b':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'b';
                        off += 2;
                        break;
                    case '\t':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 't';
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('0' + (int) ch);
                        off += 6;
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('a' + (ch - 10));
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('0' + (ch - 16));
                        off += 6;
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('a' + (ch - 26));
                        off += 6;
                        break;
                    case '<':
                    case '>':
                    case '(':
                    case ')':
                        if (browserSecure) {
                            bytes[off] = '\\';
                            bytes[off + 1] = 'u';
                            bytes[off + 2] = (byte) DIGITS[(ch >>> 12) & 15];
                            bytes[off + 3] = (byte) DIGITS[(ch >>> 8) & 15];
                            bytes[off + 4] = (byte) DIGITS[(ch >>> 4) & 15];
                            bytes[off + 5] = (byte) DIGITS[ch & 15];
                            off += 6;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off] = (byte) '\\';
                            bytes[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                bytes[off] = '\\';
                bytes[off + 1] = 'u';
                bytes[off + 2] = (byte) DIGITS[(ch >>> 12) & 15];
                bytes[off + 3] = (byte) DIGITS[(ch >>> 8) & 15];
                bytes[off + 4] = (byte) DIGITS[(ch >>> 4) & 15];
                bytes[off + 5] = (byte) DIGITS[ch & 15];
                off += 6;
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
                    bytes[off] = (byte) (0xf0 | ((uc >> 18)));
                    bytes[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    bytes[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    bytes[off + 3] = (byte) (0x80 | (uc & 0x3f));
                    off += 4;
                    i++; // 2 chars
                }
            } else if (ch > 0x07FF) {
                bytes[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                bytes[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                bytes[off + 2] = (byte) (0x80 | (ch & 0x3F));
                off += 3;
            } else {
                bytes[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                bytes[off + 1] = (byte) (0x80 | (ch & 0x3F));
                off += 2;
            }
        }
        this.off = off;
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

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        if (quoted) {
            bytes[off++] = (byte) quote;
        }

        int end = offset + len;

        int i = offset;
        for (; i < end; i++) {
            char c0 = chars[i];
            if (c0 == quote
                    || c0 == '\\'
                    || c0 < ' '
                    || c0 > 0x007F) {
                break;
            }
            bytes[off++] = (byte) c0;
        }

        if (i == end) {
            if (quoted) {
                bytes[off++] = (byte) quote;
            }
            this.off = off;
            return;
        }

        for (; i < len; ++i) { // ascii none special fast write
            char ch = chars[i];
            if ((ch >= 0x0000) && (ch <= 0x007F)) {
                switch (ch) {
                    case '\\':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) '\\';
                        off += 2;
                        break;
                    case '\n':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'n';
                        off += 2;
                        break;
                    case '\r':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'r';
                        off += 2;
                        break;
                    case '\f':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'f';
                        off += 2;
                        break;
                    case '\b':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 'b';
                        off += 2;
                        break;
                    case '\t':
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) 't';
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('0' + (int) ch);
                        off += 6;
                        break;
                    case 11:
                    case 14:
                    case 15:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '0';
                        bytes[off + 5] = (byte) ('a' + (ch - 10));
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
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('0' + (ch - 16));
                        off += 6;
                        break;
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                        bytes[off] = '\\';
                        bytes[off + 1] = 'u';
                        bytes[off + 2] = '0';
                        bytes[off + 3] = '0';
                        bytes[off + 4] = '1';
                        bytes[off + 5] = (byte) ('a' + (ch - 26));
                        off += 6;
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off] = (byte) '\\';
                            bytes[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                bytes[off] = '\\';
                bytes[off + 1] = 'u';
                bytes[off + 2] = (byte) DIGITS[(ch >>> 12) & 15];
                bytes[off + 3] = (byte) DIGITS[(ch >>> 8) & 15];
                bytes[off + 4] = (byte) DIGITS[(ch >>> 4) & 15];
                bytes[off + 5] = (byte) DIGITS[ch & 15];
                off += 6;
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
                    bytes[off] = (byte) (0xf0 | ((uc >> 18)));
                    bytes[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    bytes[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    bytes[off + 3] = (byte) (0x80 | (uc & 0x3f));
                    off += 4;
                    i++; // 2 chars
                }
            } else if (ch > 0x07FF) {
                bytes[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                bytes[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                bytes[off + 2] = (byte) (0x80 | (ch & 0x3F));
                off += 3;
            } else {
                bytes[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                bytes[off + 1] = (byte) (0x80 | (ch & 0x3F));
                off += 2;
            }
        }

        if (quoted) {
            bytes[off++] = (byte) quote;
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
                if (isEnabled(JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) {
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
    public final void writeChar(char ch) {
        int off = this.off;
        int minCapacity = off + 8;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        if ((ch >= 0x0000) && (ch <= 0x007F)) {
            switch (ch) {
                case '\\':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) '\\';
                    off += 2;
                    break;
                case '\n':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'n';
                    off += 2;
                    break;
                case '\r':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'r';
                    off += 2;
                    break;
                case '\f':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'f';
                    off += 2;
                    break;
                case '\b':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 'b';
                    off += 2;
                    break;
                case '\t':
                    bytes[off] = (byte) '\\';
                    bytes[off + 1] = (byte) 't';
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
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '0';
                    bytes[off + 5] = (byte) ('0' + (int) ch);
                    off += 6;
                    break;
                case 11:
                case 14:
                case 15:
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '0';
                    bytes[off + 5] = (byte) ('a' + (ch - 10));
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
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '1';
                    bytes[off + 5] = (byte) ('0' + (ch - 16));
                    off += 6;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    bytes[off] = '\\';
                    bytes[off + 1] = 'u';
                    bytes[off + 2] = '0';
                    bytes[off + 3] = '0';
                    bytes[off + 4] = '1';
                    bytes[off + 5] = (byte) ('a' + (ch - 26));
                    off += 6;
                    break;
                default:
                    if (ch == quote) {
                        bytes[off] = (byte) '\\';
                        bytes[off + 1] = (byte) quote;
                        off += 2;
                    } else {
                        bytes[off++] = (byte) ch;
                    }
                    break;
            }
        } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
            throw new JSONException("illegal char " + ch);
        } else if (ch > 0x07FF) {
            bytes[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
            bytes[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
            bytes[off + 2] = (byte) (0x80 | (ch & 0x3F));
            off += 3;
        } else {
            bytes[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
            bytes[off + 1] = (byte) (0x80 | (ch & 0x3F));
            off += 2;
        }

        bytes[off] = (byte) quote;
        this.off = off + 1;
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final char[] UUID_LOOKUP = JSONFactory.UUID_LOOKUP;
        final byte[] bytes = this.bytes;
        final int off = this.off;
        bytes[off] = '"';
        int l = UUID_LOOKUP[(hi1 >> 24) & 255];
        bytes[off + 1] = (byte) (l >> 8);
        bytes[off + 2] = (byte) l;
        l = UUID_LOOKUP[(hi1 >> 16) & 255];
        bytes[off + 3] = (byte) (l >> 8);
        bytes[off + 4] = (byte) l;
        l = UUID_LOOKUP[(hi1 >> 8) & 255];
        bytes[off + 5] = (byte) (l >> 8);
        bytes[off + 6] = (byte) l;
        l = UUID_LOOKUP[hi1 & 255];
        bytes[off + 7] = (byte) (l >> 8);
        bytes[off + 8] = (byte) l;
        bytes[off + 9] = '-';
        l = UUID_LOOKUP[(hi2 >> 24) & 255];
        bytes[off + 10] = (byte) (l >> 8);
        bytes[off + 11] = (byte) l;
        l = UUID_LOOKUP[(hi2 >> 16) & 255];
        bytes[off + 12] = (byte) (l >> 8);
        bytes[off + 13] = (byte) l;
        bytes[off + 14] = '-';
        l = UUID_LOOKUP[(hi2 >> 8) & 255];
        bytes[off + 15] = (byte) (l >> 8);
        bytes[off + 16] = (byte) l;
        l = UUID_LOOKUP[hi2 & 255];
        bytes[off + 17] = (byte) (l >> 8);
        bytes[off + 18] = (byte) l;
        bytes[off + 19] = '-';
        l = UUID_LOOKUP[(lo1 >> 24) & 255];
        bytes[off + 20] = (byte) (l >> 8);
        bytes[off + 21] = (byte) l;
        l = UUID_LOOKUP[(lo1 >> 16) & 255];
        bytes[off + 22] = (byte) (l >> 8);
        bytes[off + 23] = (byte) l;
        bytes[off + 24] = '-';
        l = UUID_LOOKUP[(lo1 >> 8) & 255];
        bytes[off + 25] = (byte) (l >> 8);
        bytes[off + 26] = (byte) l;
        l = UUID_LOOKUP[lo1 & 255];
        bytes[off + 27] = (byte) (l >> 8);
        bytes[off + 28] = (byte) l;
        l = UUID_LOOKUP[(lo2 >> 24) & 255];
        bytes[off + 29] = (byte) (l >> 8);
        bytes[off + 30] = (byte) l;
        l = UUID_LOOKUP[(lo2 >> 16) & 255];
        bytes[off + 31] = (byte) (l >> 8);
        bytes[off + 32] = (byte) l;
        l = UUID_LOOKUP[(lo2 >> 8) & 255];
        bytes[off + 33] = (byte) (l >> 8);
        bytes[off + 34] = (byte) l;
        l = UUID_LOOKUP[lo2 & 255];
        bytes[off + 35] = (byte) (l >> 8);
        bytes[off + 36] = (byte) l;
        bytes[off + 37] = '"';
        this.off += 38;
    }

    @Override
    public final void writeRaw(String str) {
        char[] chars = JDKUtils.getCharArray(str);
        int off = this.off;
        int minCapacity = off
                + chars.length * 3; // utf8 3 bytes

        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        for (char c : chars) {
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytes[off++] = (byte) c;
            } else if (c > 0x07FF) {
                bytes[off++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[off++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[off++] = (byte) (0x80 | (c & 0x3F));
            } else {
                bytes[off++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[off++] = (byte) (0x80 | (c & 0x3F));
            }
        }
        this.off = off;
    }

    @Override
    public final void writeRaw(byte[] bytes) {
        {
            // inline ensureCapacity
            int minCapacity = off + bytes.length;
            if (minCapacity >= this.bytes.length) {
                ensureCapacity(minCapacity);
            }
        }
        System.arraycopy(bytes, 0, this.bytes, this.off, bytes.length);
        off += bytes.length;
    }

    @Override
    public final void writeNameRaw(byte[] name) {
        int off = this.off;
        int minCapacity = off + name.length + 2 + indent;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            final byte[] bytes = this.bytes;
            bytes[off++] = ',';
            if (pretty) {
                bytes[off++] = '\n';
                for (int i = 0; i < indent; ++i) {
                    bytes[off++] = '\t';
                }
            }
        }
        System.arraycopy(name, 0, bytes, off, name.length);
        this.off = off + name.length;
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

        int off = this.off;
        if (off + 1 >= bytes.length) {
            ensureCapacity(off + 2);
        }
        bytes[off] = (byte) c0;
        bytes[off + 1] = (byte) c1;
        this.off = off + 2;
    }

    @Override
    public final void writeNameRaw(byte[] bytes, int off, int len) {
        int minCapacity = this.off + len + 2 + indent;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }
        System.arraycopy(bytes, off, this.bytes, this.off, len);
        this.off += len;
    }

    final void ensureCapacity(int minCapacity) {
        if (minCapacity >= bytes.length) {
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

    public final void writeInt32(int[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 13 + 2;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
            off = IOUtils.writeInt32(bytes, off, values[i]);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
        }

        bytes[off++] = ']';
        this.off = off;
    }

    @Override
    public final void writeInt8(byte i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 5;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt32(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        this.off = off;
    }

    @Override
    public final void writeInt16(short i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt32(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        this.off = off;
    }

    @Override
    public final void writeInt32(int i) {
        boolean writeAsString = (context.features & Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 13;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt32(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        this.off = off;
    }

    public final void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean browserCompatible = (context.features & BrowserCompatible.mask) != 0;
        boolean noneStringAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        int off = this.off;
        int minCapacity = off + 2 + values.length * 23;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = (byte) ',';
            }
            long v = values[i];
            boolean writeAsString = noneStringAsString
                    || (browserCompatible && v <= 9007199254740991L && v >= -9007199254740991L);
            if (writeAsString) {
                bytes[off++] = (byte) this.quote;
            }
            off = IOUtils.writeInt64(bytes, off, v);
            if (writeAsString) {
                bytes[off++] = (byte) this.quote;
            }
        }

        bytes[off++] = ']';
        this.off = off;
    }

    @Override
    public final void writeInt64(long i) {
        boolean writeAsString = (context.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0
                || ((context.features & BrowserCompatible.mask) != 0 && (i > 9007199254740991L || i < -9007199254740991L));
        int off = this.off;
        int minCapacity = off + 23;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt64(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        } else if ((context.features & WriteClassName.mask) != 0
                && (context.features & NotWriteNumberClassName.mask) == 0
                && i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE
        ) {
            bytes[off++] = 'L';
        }
        this.off = off;
    }

    @Override
    public final void writeFloat(float value) {
        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 17;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        if (writeAsString) {
            bytes[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, bytes, off, true);
        off += len;

        if (writeAsString) {
            bytes[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 26;

        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (writeAsString) {
            bytes[off++] = '"';
        }

        int len = DoubleToDecimal.toString(value, bytes, off, true);
        off += len;

        if (writeAsString) {
            bytes[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeFloat(float[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * (writeAsString ? 16 : 18) + 1;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }

            if (writeAsString) {
                bytes[off++] = '"';
            }

            float value = values[i];
            int len = DoubleToDecimal.toString(value, bytes, off, true);
            off += len;

            if (writeAsString) {
                bytes[off++] = '"';
            }
        }
        bytes[off++] = ']';
        this.off = off;
    }

    @Override
    public final void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 27 + 1;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }
        final byte[] bytes = this.bytes;
        bytes[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }

            if (writeAsString) {
                bytes[off++] = '"';
            }

            double value = values[i];
            int len = DoubleToDecimal.toString(value, bytes, off, true);
            off += len;

            if (writeAsString) {
                bytes[off++] = '"';
            }
        }
        bytes[off++] = ']';
        this.off = off;
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
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off] = (byte) quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        bytes[off + 1] = (byte) (q + '0');
        bytes[off + 2] = (byte) (v >> 16);
        bytes[off + 3] = (byte) (v >> 8);
        bytes[off + 4] = (byte) v;
        v = DIGITS_K[month];
        bytes[off + 5] = (byte) (v >> 8);
        bytes[off + 6] = (byte) v;
        v = DIGITS_K[dayOfMonth];
        bytes[off + 7] = (byte) (v >> 8);
        bytes[off + 8] = (byte) v;
        v = DIGITS_K[hour];
        bytes[off + 9] = (byte) (v >> 8);
        bytes[off + 10] = (byte) v;
        v = DIGITS_K[minute];
        bytes[off + 11] = (byte) (v >> 8);
        bytes[off + 12] = (byte) v;
        v = DIGITS_K[second];
        bytes[off + 13] = (byte) (v >> 8);
        bytes[off + 14] = (byte) v;
        bytes[off + 15] = (byte) quote;
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
        int off = this.off;
        int minCapacity = off + 21;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off] = (byte) quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        bytes[off + 1] = (byte) (q + '0');
        bytes[off + 2] = (byte) (v >> 16);
        bytes[off + 3] = (byte) (v >> 8);
        bytes[off + 4] = (byte) v;
        bytes[off + 5] = '-';
        v = DIGITS_K[month];
        bytes[off + 6] = (byte) (v >> 8);
        bytes[off + 7] = (byte) v;
        bytes[off + 8] = '-';
        v = DIGITS_K[dayOfMonth];
        bytes[off + 9] = (byte) (v >> 8);
        bytes[off + 10] = (byte) v;
        bytes[off + 11] = ' ';
        v = DIGITS_K[hour];
        bytes[off + 12] = (byte) (v >> 8);
        bytes[off + 13] = (byte) v;
        bytes[off + 14] = ':';
        v = DIGITS_K[minute];
        bytes[off + 15] = (byte) (v >> 8);
        bytes[off + 16] = (byte) v;
        bytes[off + 17] = ':';
        v = DIGITS_K[second];
        bytes[off + 18] = (byte) (v >> 8);
        bytes[off + 19] = (byte) v;
        bytes[off + 20] = (byte) quote;
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }
        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        off = IOUtils.writeLocalDate(bytes, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        bytes[off++] = (byte) quote;
        this.off = off;
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime dateTime) {
        int off = this.off;
        int minCapacity = off + 38;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(bytes, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        bytes[off++] = ' ';
        off = IOUtils.writeLocalTime(bytes, off, dateTime.toLocalTime());
        bytes[off++] = (byte) quote;
        this.off = off;
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 10;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off] = (byte) quote;
        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException("Only 4 digits numbers are supported. Provided: " + year);
        }
        final int q = year / 1000;
        int v = DIGITS_K[year - q * 1000];
        bytes[off + 1] = (byte) (q + '0');
        bytes[off + 2] = (byte) (v >> 16);
        bytes[off + 3] = (byte) (v >> 8);
        bytes[off + 4] = (byte) v;
        v = DIGITS_K[month];
        bytes[off + 5] = (byte) (v >> 8);
        bytes[off + 6] = (byte) v;
        v = DIGITS_K[dayOfMonth];
        bytes[off + 7] = (byte) (v >> 8);
        bytes[off + 8] = (byte) v;
        bytes[off + 9] = (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 13;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        off = IOUtils.writeLocalDate(bytes, off, year, month, dayOfMonth);
        bytes[off++] = (byte) quote;
        this.off = off;
    }

    @Override
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
        int off = this.off;
        int minCapacity = off + 10;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off] = (byte) quote;
        int v = DIGITS_K[hour];
        bytes[off + 1] = (byte) (v >> 8);
        bytes[off + 2] = (byte) v;
        bytes[off + 3] = ':';
        v = DIGITS_K[minute];
        bytes[off + 4] = (byte) (v >> 8);
        bytes[off + 5] = (byte) v;
        bytes[off + 6] = ':';
        v = DIGITS_K[second];
        bytes[off + 7] = (byte) (v >> 8);
        bytes[off + 8] = (byte) v;
        bytes[off + 9] = (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeLocalTime(LocalTime time) {
        int off = this.off;
        int minCapacity = off + 20;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        off = IOUtils.writeLocalTime(bytes, off, time);
        bytes[off++] = (byte) quote;
        this.off = off;
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = (byte) quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(bytes, off, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        bytes[off++] = 'T';
        off = IOUtils.writeLocalTime(bytes, off, dateTime.toLocalTime());
        if (zoneSize == 1) {
            bytes[off++] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getBytes(0, zoneIdLength, bytes, off);
            off += zoneIdLength;
        } else {
            bytes[off++] = '[';
            zoneId.getBytes(0, zoneIdLength, bytes, off);
            off += zoneIdLength;
            bytes[off++] = ']';
        }
        bytes[off++] = (byte) quote;
        this.off = off;
    }

    @Override
    public final void writeOffsetDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        ZoneOffset offset = dateTime.getOffset();
        String zoneId = offset.getId();
        final int zoneIdLength;
        boolean utc = ZoneOffset.UTC == offset
                || ("UTC".equals(zoneId) || "Z".equals(zoneId));
        if (utc) {
            zoneId = "Z";
            zoneIdLength = 1;
        } else {
            zoneIdLength = zoneId.length();
        }

        int minCapacity = off + zoneIdLength + 40;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        bytes[off++] = (byte) quote;
        LocalDateTime ldt = dateTime.toLocalDateTime();
        LocalDate date = ldt.toLocalDate();
        off = IOUtils.writeLocalDate(bytes, off, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        bytes[off++] = 'T';
        LocalTime time = ldt.toLocalTime();
        off = IOUtils.writeLocalTime(bytes, off, time);
        if (utc) {
            bytes[off++] = 'Z';
        } else {
            zoneId.getBytes(0, zoneIdLength, bytes, off);
        }
        bytes[off++] = (byte) quote;
        this.off = off;
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
            if (minCapacity >= this.bytes.length) {
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
        int zonelen;
        if (timeZone) {
            zonelen = offsetSeconds == 0 ? 1 : 6;
        } else {
            zonelen = 0;
        }

        int minCapacity = off + 25 + zonelen;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        bytes[off] = (byte) quote;
        off = IOUtils.writeInt32(bytes, off + 1, year);
        bytes[off] = '-';
        int v = DIGITS_K[month];
        bytes[off + 1] = (byte) (v >> 8);
        bytes[off + 2] = (byte) v;
        bytes[off + 3] = '-';
        v = DIGITS_K[dayOfMonth];
        bytes[off + 4] = (byte) (v >> 8);
        bytes[off + 5] = (byte) v;
        bytes[off + 6] = (byte) (timeZone ? 'T' : ' ');
        v = DIGITS_K[hour];
        bytes[off + 7] = (byte) (v >> 8);
        bytes[off + 8] = (byte) v;
        bytes[off + 9] = ':';
        v = DIGITS_K[minute];
        bytes[off + 10] = (byte) (v >> 8);
        bytes[off + 11] = (byte) v;
        bytes[off + 12] = ':';
        v = DIGITS_K[second];
        bytes[off + 13] = (byte) (v >> 8);
        bytes[off + 14] = (byte) v;
        off += 15;

        if (millis > 0) {
            bytes[off++] = '.';
            int div = millis / 10;
            int div2 = div / 10;
            final int rem1 = millis - div * 10;

            if (rem1 != 0) {
                v = DIGITS_K[millis];
                bytes[off] = (byte) (v >> 16);
                bytes[off + 1] = (byte) (v >> 8);
                bytes[off + 2] = (byte) v;
                off += 3;
            } else {
                final int rem2 = div - div2 * 10;
                if (rem2 != 0) {
                    v = DIGITS_K[div];
                    bytes[off] = (byte) (v >> 8);
                    bytes[off + 1] = (byte) v;
                    off += 2;
                } else {
                    bytes[off++] = (byte) (div2 + '0');
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
                bytes[off] = (byte) (v >> 8);
                bytes[off + 1] = (byte) v;
                off += 2;

                bytes[off++] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                v = DIGITS_K[offsetMinutes];
                bytes[off] = (byte) (v >> 8);
                bytes[off + 1] = (byte) v;
                off += 2;
            }
        }
        bytes[off++] = (byte) quote;
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

        int off = this.off;
        int minCapacity = off + precision + 7;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (browserCompatible) {
            bytes[off++] = '"';
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
            off = IOUtils.writeDecimal(bytes, off, unscaleValue, scale);
        } else {
            String str = asPlain ? value.toPlainString() : value.toString();
            str.getBytes(0, str.length(), bytes, off);
            off += str.length();
        }

        if (browserCompatible) {
            bytes[off++] = '"';
        }
        this.off = off;
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
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = ',';
            }

            Object value = entry.getValue();
            if (value == null && (context.features & Feature.WriteMapNullValue.mask) == 0) {
                continue;
            }

            first = false;
            Object key = entry.getKey();
            if (key instanceof String) {
                writeString((String) key);
            } else {
                writeAny(key);
            }

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
        for (Object o : array) {
            if (!first) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }
                bytes[off++] = ',';
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
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = ']';
    }

    @Override
    public final String toString() {
        return new String(bytes, 0, off, StandardCharsets.UTF_8);
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

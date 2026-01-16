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
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.isInt64;

@SuppressWarnings("ALL")
public final class JSONWriterUTF8
        extends JSONWriter {
    static final long REF;
    static final short QUOTE2_COLON, QUOTE_COLON;

    static {
        byte[] chars = {'{', '"', '$', 'r', 'e', 'f', '"', ':'};
        REF = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET);
        QUOTE2_COLON = UNSAFE.getShort(chars, ARRAY_CHAR_BASE_OFFSET + 6);
        chars[6] = '\'';
        QUOTE_COLON = UNSAFE.getShort(chars, ARRAY_CHAR_BASE_OFFSET + 6);
    }

    final CacheItem cacheItem;
    byte[] bytes;
    private final long byteVectorQuote;

    JSONWriterUTF8(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_8);
        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
        this.bytes = bytes;
        this.byteVectorQuote = this.useSingleQuote ? ~0x2727_2727_2727_2727L : ~0x2222_2222_2222_2222L;
    }

    public final void writeNull() {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 4 > bytes.length) {
            bytes = grow(off + 4);
        }
        IOUtils.putNULL(bytes, off);
        this.off = off + 4;
    }

    @Override
    public final void writeReference(String path) {
        this.lastReference = path;
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, REF);
        this.off = off + 8;
        writeString(path);
        writeRaw((byte) '}');
    }

    @Override
    public final void writeBase64(byte[] value) {
        int charsLen = ((value.length - 1) / 3 + 1) << 2; // base64 character count

        int off = this.off;
        int minCapacity = off + charsLen + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = (byte) quote;

        int eLen = (value.length / 3) * 3; // Length of even 24-bits.

        for (int s = 0; s < eLen; ) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (value[s++] & 0xff) << 16 | (value[s++] & 0xff) << 8 | (value[s++] & 0xff);

            // Encode the int into four chars
            bytes[off] = (byte) CA[(i >>> 18) & 0x3f];
            bytes[off + 1] = (byte) CA[(i >>> 12) & 0x3f];
            bytes[off + 2] = (byte) CA[(i >>> 6) & 0x3f];
            bytes[off + 3] = (byte) CA[i & 0x3f];
            off += 4;
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = value.length - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((value[eLen] & 0xff) << 10) | (left == 2 ? ((value[value.length - 1] & 0xff) << 2) : 0);

            // Set last four chars
            bytes[off] = (byte) CA[i >> 12];
            bytes[off + 1] = (byte) CA[(i >>> 6) & 0x3f];
            bytes[off + 2] = left == 2 ? (byte) CA[i & 0x3f] : (byte) '=';
            bytes[off + 3] = '=';
            off += 4;
        }

        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    @Override
    public final void writeHex(byte[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        int charsLen = values.length * 2 + 3;

        int off = this.off;
        int minCapacity = off + charsLen + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        putShortLE(bytes, off, (short) ('x' | ('\'' << 8)));
        off += 2;

        for (int i = 0; i < values.length; i++) {
            putShortLE(bytes, off, hex2U(values[i]));
            off += 2;
        }

        bytes[off] = '\'';
        this.off = off + 1;
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

        return toString()
                .getBytes(charset);
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
            grow0(off + 1);
        }
        bytes[off] = (byte) c;
        this.off = off + 1;
    }

    @Override
    public final void writeColon() {
        int off = this.off;
        grow1(off)[off] = ':';
        this.off = off + 1;
    }

    @Override
    public final void startObject() {
        if (++level > context.maxLevel) {
            overflowLevel();
        }

        startObject = true;

        int off = this.off;
        int minCapacity = off + 3 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '{';

        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        this.off = off;
    }

    @Override
    public final void endObject() {
        level--;
        int off = this.off;
        int minCapacity = off + 1 + (pretty == 0 ? 0 : pretty * level + 1);
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }

        bytes[off] = '}';
        this.off = off + 1;
        startObject = false;
    }

    @Override
    public final void writeComma() {
        startObject = false;
        int off = this.off;
        int minCapacity = off + 2 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = ',';
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        this.off = off;
    }

    @Override
    public final void endArray() {
        level--;
        int off = this.off;
        int minCapacity = off + 1 + (pretty == 0 ? 0 : pretty * level + 1);
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        bytes[off] = ']';
        this.off = off + 1;
        startObject = false;
    }

    public final void writeString(String[] strings) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.stringCapacity(strings));
        if (STRING_VALUE != null) {
            off = IO.writeValueJDK11(this, buf, off, strings, context.features);
        } else {
            off = IO.writeValueJDK8(this, buf, off, strings, context.features);
        }
        this.off = off;
    }

    public final void writeString(List<String> list) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.stringCapacity(this, list));
        if (STRING_VALUE != null) {
            off = IO.writeValueJDK11(this, buf, off, list, context.features);
        } else {
            off = IO.writeValueJDK8(this, buf, off, list, context.features);
        }
        this.off = off;
    }

    @Override
    public final void writeString(boolean value) {
        byte quote = (byte) this.quote;
        bytes[off++] = quote;
        writeBool(value);
        bytes[off++] = quote;
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
        writeRaw((byte) quote);
    }

    @Override
    public void writeString(String value) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.stringCapacity(value));
        if (STRING_VALUE != null) {
            off = IO.writeValue(this, buf, off, value, context.features);
        } else {
            off = IO.writeValue(this, buf, off, value, context.features);
        }
        this.off = off;
    }

    public final void writeStringLatin1(byte[] value) {
        if ((context.features & MASK_BROWSER_SECURE) != 0) {
            writeStringLatin1BrowserSecure(value);
            return;
        }

        byte quote = (byte) this.quote;
        if (StringUtils.escaped(value, quote, byteVectorQuote)) {
            writeStringEscaped(value);
            return;
        }

        int off = this.off;
        int minCapacity = off + value.length + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = StringUtils.writeLatin1(bytes, off, value, quote);
    }

    protected final void writeStringLatin1BrowserSecure(byte[] value) {
        final byte quote = (byte) this.quote;
        int i = 0;
        for (; i < value.length; i++) {
            byte c = value[i];
            if (c == quote || c == '\\' || c < ' ' || c == '<' || c == '>' || c == '(' || c == ')') {
                break;
            }
        }

        int off = this.off;
        if (i == value.length) {
            int minCapacity = off + value.length + 2;
            byte[] bytes = this.bytes;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }
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

        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + value.length * 6 + 2;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = StringUtils.writeUTF16(bytes, off, value, (byte) quote, context.features);
    }

    public final void writeString(final char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        int off = this.off;
        int minCapacity = off
                + chars.length * 3 /* utf8 3 bytes */
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += chars.length * 3;
        }

        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
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
        if (i < chars.length) {
            writeStringEscapedRest(chars, chars.length, browserSecure, escapeNoneAscii, i);
        }

        this.bytes[this.off++] = (byte) quote;
    }

    public final void writeString(final char[] chars, int stroff, int strlen) {
        if (chars == null) {
            if (isEnabled(NullAsDefaultValue.mask | WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        int end = stroff + strlen;

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        int off = this.off;
        // ensureCapacity
        int minCapacity = off
                + strlen * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii || browserSecure) {
            minCapacity += strlen * 3;
        }

        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
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
        if (i < end) {
            writeStringEscapedRest(chars, end, browserSecure, escapeNoneAscii, i);
        }

        this.bytes[this.off++] = (byte) quote;
    }

    protected final void writeStringEscaped(byte[] values) {
        int minCapacity = off + values.length * 6 + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = StringUtils.writeLatin1Escaped(bytes, off, values, (byte) quote, context.features);
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        int off = this.off;
        for (; i < end; ++i) { // ascii none special fast write
            char ch = chars[i];
            if (ch <= 0x007F) {
                switch (ch) {
                    case '\\':
                    case '\n':
                    case '\r':
                    case '\f':
                    case '\b':
                    case '\t':
                        StringUtils.writeEscapedChar(bytes, off, ch);
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
                        StringUtils.writeU4Hex2(bytes, off, ch);
                        off += 6;
                        break;
                    case '<':
                    case '>':
                    case '(':
                    case ')':
                        if (browserSecure) {
                            StringUtils.writeU4HexU(bytes, off, ch);
                            off += 6;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off] = '\\';
                            bytes[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                StringUtils.writeU4HexU(bytes, off, ch);
                off += 6;
            } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                final int uc;
                if (ch < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                    if (chars.length - i < 2) {
                        uc = -1;
                    } else {
                        char d = chars[i + 1];
                        // d >= '\uDC00' && d < ('\uDFFF' + 1)
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                            uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                        } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                            bytes[off++] = '?';
                            continue;
                        }
                    }
                } else {
                    //
                    // Character.isLowSurrogate(c)
                    bytes[off++] = '?';
                    continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                }

                if (uc < 0) {
                    bytes[off++] = '?';
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
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;

        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
                + 2;

        if (escapeNoneAscii) {
            minCapacity += len * 3;
        }

        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
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

        for (; i < end; ++i) { // ascii none special fast write
            char ch = chars[i];
            if (ch <= 0x007F) {
                switch (ch) {
                    case '\\':
                    case '\n':
                    case '\r':
                    case '\f':
                    case '\b':
                    case '\t':
                        StringUtils.writeEscapedChar(bytes, off, ch);
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
                        StringUtils.writeU4Hex2(bytes, off, ch);
                        off += 6;
                        break;
                    default:
                        if (ch == quote) {
                            bytes[off] = '\\';
                            bytes[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            bytes[off++] = (byte) ch;
                        }
                        break;
                }
            } else if (escapeNoneAscii) {
                StringUtils.writeU4HexU(bytes, off, ch);
                off += 6;
            } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                final int uc;
                if (ch < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                    if (chars.length - i < 2) {
                        uc = -1;
                    } else {
                        char d = chars[i + 1];
                        // d >= '\uDC00' && d < ('\uDFFF' + 1)
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                            uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                        } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                            bytes[off++] = '?';
                            continue;
                        }
                    }
                } else {
                    //
                    // Character.isLowSurrogate(c)
                    bytes[off++] = (byte) '?';
                    continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                }

                if (uc < 0) {
                    bytes[off++] = '?';
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

    @Override
    public final void writeChar(char ch) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }
        bytes[off++] = (byte) quote;
        if (ch <= 0x007F) {
            switch (ch) {
                case '\\':
                case '\n':
                case '\r':
                case '\f':
                case '\b':
                case '\t':
                    StringUtils.writeEscapedChar(bytes, off, ch);
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
                    StringUtils.writeU4Hex2(bytes, off, ch);
                    off += 6;
                    break;
                default:
                    if (ch == quote) {
                        bytes[off] = '\\';
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
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
    }

    /**
     * Extract the least significant 4 bytes from the input integer i, convert each byte into its corresponding 2-digit
     * hexadecimal representation, concatenate these hexadecimal strings into one continuous string, and then interpret
     * this string as a hexadecimal number to form and return a long value.
     */
    private static long hex8(long i) {
        i = expand(i);
        /*
            Use long to simulate vector operations and generate 8 hexadecimal characters at a time.
            ------------
            0  = 0b0000_0000 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '0'
            1  = 0b0000_0001 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '1'
            2  = 0b0000_0010 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '2'
            3  = 0b0000_0011 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '3'
            4  = 0b0000_0100 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '4'
            5  = 0b0000_0101 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '5'
            6  = 0b0000_0110 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '6'
            7  = 0b0000_0111 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '7'
            8  = 0b0000_1000 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '8'
            9  = 0b0000_1001 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '9'
            10 = 0b0000_1010 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'a'
            11 = 0b0000_1011 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'b'
            12 = 0b0000_1100 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'c'
            13 = 0b0000_1101 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'd'
            14 = 0b0000_1110 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'e'
            15 = 0b0000_1111 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'f'
         */
        long m = (i + 0x0606_0606_0606_0606L) & 0x1010_1010_1010_1010L;
        long v = ((m << 1) + (m >> 1) - (m >> 4)) + 0x3030_3030_3030_3030L + i;
        if (!BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }

    /**
     * A faster alternative that is functionally equivalent to Long.expand(i, 0x0F0F_0F0F_0F0F_0F0FL)
     */
    private static long expand(long i) {
        long t = i << 16;
        i = (i & ~0xFFFF00000000L) | (t & 0xFFFF00000000L);
        t = i << 8;
        i = (i & ~0xFF000000FF0000L) | (t & 0xFF000000FF0000L);
        t = i << 4;
        i = (i & ~0xF000F000F000F00L) | (t & 0xF000F000F000F00L);
        return i & 0x0F0F_0F0F_0F0F_0F0FL;
    }

    @Override
    public final void writeRaw(String str) {
        char[] chars = getCharArray(str);
        int off = this.off;
        int minCapacity = off
                + chars.length * 3; // utf8 3 bytes

        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytes[off++] = (byte) c;
            } else if (c > 0x07FF) {
                bytes[off] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[off + 1] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[off + 2] = (byte) (0x80 | (c & 0x3F));
                off += 3;
            } else {
                bytes[off] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[off + 1] = (byte) (0x80 | (c & 0x3F));
                off += 2;
            }
        }
        this.off = off;
    }

    @Override
    public final void writeRaw(byte[] raw) {
        int off = this.off;
        int minCapacity = off + raw.length;
        if (minCapacity > bytes.length) {
            grow(minCapacity);
        }
        System.arraycopy(raw, 0, bytes, off, raw.length);
        this.off = off + raw.length;
    }

    @Override
    public final void writeNameRaw(byte[] name) {
        int off = this.off;
        int minCapacity = off + name.length + 2 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }
        System.arraycopy(name, 0, bytes, off, name.length);
        this.off = off + name.length;
    }

    @Override
    public final void writeName2Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 5;
    }

    private int indent(byte[] bytes, int off) {
        bytes[off] = '\n';
        int toIndex = off + 1 + pretty * level;
        Arrays.fill(bytes, off + 1, toIndex, pretty == PRETTY_TAB ? (byte) '\t' : (byte) ' ');
        return toIndex;
    }

    @Override
    public final void writeName3Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 6;
    }

    @Override
    public final void writeName4Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 7;
    }

    @Override
    public final void writeName5Raw(long name) {
        int off = this.off;
        int minCapacity = off + 10 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 8;
    }

    @Override
    public final void writeName6Raw(long name) {
        int off = this.off;
        int minCapacity = off + 11 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        bytes[off + 8] = ':';
        this.off = off + 9;
    }

    @Override
    public final void writeName7Raw(long name) {
        int off = this.off;
        int minCapacity = off + 12 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        bytes[off + 8] = (byte) quote;
        bytes[off + 9] = ':';
        this.off = off + 10;
    }

    @Override
    public void writeName8Raw(long name) {
        int off = this.off;
        int minCapacity = off
                + 13 // 8 + quote 2 + comma 1 + colon 1 + pretty 1
                + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        bytes[off] = (byte) quote;
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 1, name);
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 9, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 11;
    }

    @Override
    public final void writeName9Raw(long name0, int name1) {
        int off = this.off;
        int minCapacity = off + 14 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 12;
    }

    @Override
    public final void writeName10Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 13;
    }

    @Override
    public final void writeName11Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 14;
    }

    @Override
    public final void writeName12Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 15;
    }

    @Override
    public final void writeName13Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 18 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 16;
    }

    @Override
    public final void writeName14Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 19 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        bytes[off + 16] = ':';
        this.off = off + 17;
    }

    @Override
    public final void writeName15Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 20 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 18;
    }

    @Override
    public final void writeName16Raw(long name0, long name1) {
        int off = this.off;
        int minCapacity = off + 21 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }

        bytes[off++] = (byte) quote;
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
        this.off = off + 18;
    }

    public final void writeRaw(byte b) {
        int off = this.off;
        grow1(off)[off] = b;
        this.off = off + 1;
    }

    @Override
    public final void writeRaw(char ch) {
        if (ch > 128) {
            throw new JSONException("not support " + ch);
        }

        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off++] = (byte) ch;
    }

    @Override
    public final void writeRaw(char c0, char c1) {
        if (c0 > 128 || c1 > 128) {
            throw new JSONException("not support " + c0 + ", " + c1);
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 2 > bytes.length) {
            bytes = grow(off + 2);
        }
        bytes[off] = (byte) c0;
        bytes[off + 1] = (byte) c1;
        this.off = off + 2;
    }

    @Override
    public final void writeNameRaw(byte[] name, int coff, int len) {
        int off = this.off;
        int minCapacity = off + len + 2 + pretty * level;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (!startObject) {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }
        startObject = false;
        System.arraycopy(name, coff, bytes, off, len);
        this.off = off + len;
    }

    public final byte[] ensureCapacity(int minCapacity) {
        byte[] bytes = this.bytes;
        if (minCapacity >= bytes.length) {
            this.bytes = bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, bytes.length));
        }
        return bytes;
    }

    private byte[] grow(int minCapacity) {
        grow0(minCapacity);
        return bytes;
    }

    private byte[] grow1(int off) {
        byte[] bytes = this.bytes;
        if (off == bytes.length) {
            bytes = grow(off + 1);
        }
        return bytes;
    }

    private void grow0(int minCapacity) {
        // minCapacity is usually close to size, so this is a win:
        bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, bytes.length));
    }

    public final void writeInt32(int[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 13 + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
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

        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt8(byte i) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt8(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        this.off = off;
    }

    @Override
    public final void writeInt8(byte[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 5 + 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
            off = IOUtils.writeInt8(bytes, off, values[i]);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
        }

        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt16(short i) {
        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + 7;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt16(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
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
    public final void writeInt32(int i) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

        int off = this.off;
        int minCapacity = off + 13;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
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
    public final void writeListInt32(List<Integer> values) {
        if (values == null) {
            writeNull();
            return;
        }

        int size = values.size();
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        int off = this.off;
        int minCapacity = off + 2 + size * 23;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }
            Number item = values.get(i);
            if (item == null) {
                IOUtils.putNULL(bytes, off);
                off += 4;
                continue;
            }

            int v = item.intValue();
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
            off = IOUtils.writeInt32(bytes, off, v);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
        }

        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt64(long[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        int off = this.off;
        int minCapacity = off + 2 + values.length * 23;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }
            long v = values[i];
            boolean writeAsString = isWriteAsString(v, context.features);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
            off = IOUtils.writeInt64(bytes, off, v);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
        }

        bytes[off] = ']';
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }
            Long item = values.get(i);
            if (item == null) {
                IOUtils.putNULL(bytes, off);
                off += 4;
                continue;
            }

            long v = item;
            boolean writeAsString = isWriteAsString(v, context.features);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
            off = IOUtils.writeInt64(bytes, off, v);
            if (writeAsString) {
                bytes[off++] = (byte) quote;
            }
        }

        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeInt64(long i) {
        final long features = context.features;
        int off = this.off;
        int minCapacity = off + 23;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        boolean writeAsString = isWriteAsString(i, features);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        }
        off = IOUtils.writeInt64(bytes, off, i);
        if (writeAsString) {
            bytes[off++] = (byte) quote;
        } else if ((features & MASK_WRITE_CLASS_NAME) != 0
                && (features & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                && i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE
        ) {
            bytes[off++] = 'L';
        }
        this.off = off;
    }

    @Override
    public final void writeInt64(Long i) {
        if (i == null) {
            writeNumberNull();
        } else {
            writeInt64(i.longValue());
        }
    }

    @Override
    public final void writeFloat(float value) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        if (writeSpecialAsString && !Float.isFinite(value)) {
            writeAsString = false;
        }

        int off = this.off;
        int minCapacity = off + 17;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (writeAsString) {
            bytes[off++] = '"';
        }

        off = NumberUtils.writeFloat(bytes, off, value, true, writeSpecialAsString);

        if (writeAsString) {
            bytes[off++] = '"';
        }
        this.off = off;
    }

    @Override
    public final void writeDouble(double value) {
        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        if (writeSpecialAsString && !Double.isFinite(value)) {
            writeAsString = false;
        }

        int off = this.off;
        int minCapacity = off + 26;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (writeAsString) {
            bytes[off++] = '"';
        }

        off = NumberUtils.writeDouble(bytes, off, value, true, writeSpecialAsString);

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

        boolean writeAsString = (context.features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * (writeAsString ? 16 : 18) + 1;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        bytes[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }

            if (!Float.isFinite(values[i])) {
                off = NumberUtils.writeFloat(bytes, off, values[i], true, writeSpecialAsString);
            } else {
                if (writeAsString) {
                    bytes[off++] = '"';
                }
                off = NumberUtils.writeFloat(bytes, off, values[i], true, false);
                if (writeAsString) {
                    bytes[off++] = '"';
                }
            }
        }
        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        boolean writeAsString = (context.features & WriteNonStringValueAsString.mask) != 0;
        boolean writeSpecialAsString = (context.features & WriteFloatSpecialAsString.mask) != 0;

        int off = this.off;
        int minCapacity = off + values.length * 27 + 1;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                bytes[off++] = ',';
            }

            if (!Double.isFinite(values[i])) {
                off = NumberUtils.writeDouble(bytes, off, values[i], true, writeSpecialAsString);
            } else {
                if (writeAsString) {
                    bytes[off++] = '"';
                }
                off = NumberUtils.writeDouble(bytes, off, values[i], true, false);
                if (writeAsString) {
                    bytes[off++] = '"';
                }
            }
        }
        bytes[off] = ']';
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        if (year < 0 || year > 9999) {
            throw illegalYear(year);
        }
        int y01 = year / 100;
        int y23 = year - y01 * 100;
        writeDigitPair(bytes, off + 1, y01);
        writeDigitPair(bytes, off + 3, y23);
        writeDigitPair(bytes, off + 5, month);
        writeDigitPair(bytes, off + 7, dayOfMonth);
        writeDigitPair(bytes, off + 9, hour);
        writeDigitPair(bytes, off + 11, minute);
        writeDigitPair(bytes, off + 13, second);
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        off = IOUtils.writeLocalDate(bytes, off + 1, year, month, dayOfMonth);
        bytes[off] = ' ';
        IOUtils.writeLocalTime(bytes, off + 1, hour, minute, second);
        bytes[off + 9] = (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeLocalDate(LocalDate value) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime value) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
    }

    @Override
    public final void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 10;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        if (year < 0 || year > 9999) {
            throw illegalYear(year);
        }
        int y01 = year / 100;
        int y23 = year - y01 * 100;
        writeDigitPair(bytes, off + 1, y01);
        writeDigitPair(bytes, off + 3, y23);
        writeDigitPair(bytes, off + 5, month);
        writeDigitPair(bytes, off + 7, dayOfMonth);
        bytes[off + 9] = (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        int off = this.off;
        int minCapacity = off + 13;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = (byte) quote;
        off = IOUtils.writeLocalDate(bytes, off, year, month, dayOfMonth);
        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    @Override
    public final void writeTimeHHMMSS8(int hour, int minute, int second) {
        int off = this.off;
        int minCapacity = off + 10;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        IOUtils.writeLocalTime(bytes, off + 1, hour, minute, second);
        bytes[off + 9] = (byte) quote;
        this.off = off + 10;
    }

    @Override
    public final void writeLocalTime(LocalTime value) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        LocalDate localDate = dateTime.toLocalDate();
        off = IOUtils.writeLocalDate(bytes, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        bytes[off] = 'T';
        off = IOUtils.writeLocalTime(bytes, off + 1, dateTime.toLocalTime());
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
        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    @Override
    public final void writeOffsetDateTime(OffsetDateTime value) {
        int off = this.off;
        byte[] buf = ensureCapacity(off + IO.valueSize(value));
        this.off = IO.writeValue(this, buf, off, value, context.features);
    }

    public final void writeOffsetTime(OffsetTime time) {
        if (time == null) {
            writeNull();
            return;
        }

        int off = this.off;
        int minCapacity = off + 28;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        off = IOUtils.writeLocalTime(bytes, off + 1, time.toLocalTime());

        ZoneOffset offset = time.getOffset();
        if (offset.getTotalSeconds() == 0) {
            bytes[off++] = 'Z';
        } else {
            String zoneId = offset.getId();
            zoneId.getBytes(0, zoneId.length(), bytes, off);
            off += zoneId.length();
        }
        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    @Override
    public final void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNumberNull(features);
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (writeAsString) {
            bytes[off++] = '"';
        }
        str.getBytes(0, strlen, bytes, off);
        off += strlen;
        if (writeAsString) {
            bytes[off++] = '"';
        }
        this.off = off;
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off] = (byte) quote;
        off = IOUtils.writeLocalDate(bytes, off + 1, year, month, dayOfMonth);
        bytes[off] = (byte) (timeZone ? 'T' : ' ');
        IOUtils.writeLocalTime(bytes, off + 1, hour, minute, second);
        off += 9;

        if (millis > 0) {
            int div = millis / 10;
            int div2 = div / 10;
            final int rem1 = millis - div * 10;

            if (rem1 != 0) {
                putIntLE(bytes, off, DIGITS_K_32[millis & 0x3ff] & 0xffffff00 | '.');
                off += 4;
            } else {
                bytes[off++] = '.';
                final int rem2 = div - div2 * 10;
                if (rem2 != 0) {
                    writeDigitPair(bytes, off, div);
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
                bytes[off] = offset >= 0 ? (byte) '+' : (byte) '-';
                writeDigitPair(bytes, off + 1, offsetAbs);
                bytes[off + 3] = ':';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                writeDigitPair(bytes, off + 4, offsetMinutes);
                off += 6;
            }
        }
        bytes[off] = (byte) quote;
        this.off = off + 1;
    }

    @Override
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
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (writeAsString) {
            bytes[off++] = '"';
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
            off = IOUtils.writeDecimal(bytes, off, unscaleValue, scale);
        } else {
            String str = asPlain ? value.toPlainString() : value.toString();
            str.getBytes(0, str.length(), bytes, off);
            off += str.length();
        }

        if (writeAsString) {
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
    public final void write(Map<?, ?> map) {
        if (pretty != PRETTY_NON) {
            super.write(map);
            return;
        }

        if (map == null) {
            this.writeNull();
            return;
        }

        long features = context.features;
        if ((features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(map.getClass());
            objectWriter.write(this, map, null, null, 0);
            return;
        }

        if (off == bytes.length) {
            grow(off + 1);
        }
        bytes[off++] = '{';

        boolean first = true;
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null && (features & MASK_WRITE_MAP_NULL_VALUE) == 0) {
                continue;
            }

            if (!first) {
                if (off == bytes.length) {
                    grow0(off + 1);
                }
                bytes[off++] = ',';
            }

            first = false;
            Object key = entry.getKey();
            if (key instanceof String) {
                writeString((String) key);
            } else {
                writeAny(key);
            }

            if (off == bytes.length) {
                grow0(off + 1);
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
            grow(off + 1);
        }
        bytes[off++] = '}';
    }

    @Override
    public final void write(List array) {
        if (array == null) {
            this.writeArrayNull();
            return;
        }

        if ((context.features & (MASK_REFERENCE_DETECTION | MASK_PRETTY_FORMAT | MASK_NOT_WRITE_EMPTY_ARRAY | MASK_NOT_WRITE_DEFAULT_VALUE)) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(array.getClass());
            objectWriter.write(this, array, null, null, 0);
            return;
        }

        if (off == bytes.length) {
            grow(off + 1);
        }
        bytes[off++] = '[';

        boolean first = true;
        for (int i = 0; i < array.size(); i++) {
            Object o = array.get(i);
            if (!first) {
                if (off == bytes.length) {
                    grow(off + 1);
                }
                bytes[off++] = ',';
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
        if (off == bytes.length) {
            grow(off + 1);
        }
        bytes[off++] = ']';
    }

    public void writeBool(boolean value) {
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int off = this.off;
        if ((context.features & MASK_WRITE_BOOLEAN_AS_NUMBER) != 0) {
            bytes[off++] = (byte) (value ? '1' : '0');
        } else {
            off = IOUtils.putBoolean(bytes, off, value);
        }
        this.off = off;
    }

    @Override
    public final String toString() {
        return new String(bytes, 0, off, StandardCharsets.UTF_8);
    }

    @Override
    public final int flushTo(OutputStream out, Charset charset) throws IOException {
        if (off == 0) {
            return 0;
        }

        if (charset == null || charset == StandardCharsets.UTF_8 || charset == StandardCharsets.US_ASCII) {
            int len = off;
            out.write(bytes, 0, off);
            off = 0;
            return len;
        }

        if (charset == StandardCharsets.ISO_8859_1) {
            boolean hasNegative = false;
            if (METHOD_HANDLE_HAS_NEGATIVE != null) {
                try {
                    hasNegative = (Boolean) METHOD_HANDLE_HAS_NEGATIVE.invoke(bytes, 0, bytes.length);
                } catch (Throwable ignored) {
                    // ignored
                }
            }
            if (!hasNegative) {
                int len = off;
                out.write(bytes, 0, off);
                off = 0;
                return len;
            }
        }

        String str = new String(bytes, 0, off);
        byte[] encodedBytes = str.getBytes(charset);
        out.write(encodedBytes);
        return encodedBytes.length;
    }

    public abstract static class IO {
        public static int startObject(JSONWriterUTF8 writer, byte[] buf, int off) {
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

        public static int endObject(JSONWriterUTF8 writer, byte[] buf, int off) {
            int level = writer.level--;
            if (writer.pretty != PRETTY_NON) {
                off = writer.indent(buf, off);
            }

            buf[off] = '}';
            writer.startObject = false;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, byte value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Byte value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.byteValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, byte[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, short value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Short value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.shortValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, int value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Integer value, long features) {
            if (value == null) {
                return writeNumberNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.intValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, int[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, long value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Long value, long features) {
            if (value == null) {
                return writeLongNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.longValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, long[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

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

        public static int writeInt64(JSONWriterUTF8 writer, byte[] buf, int off, List<Long> value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

            buf[off++] = '[';

            for (int i = 0; i < value.size(); i++) {
                if (i != 0) {
                    buf[off++] = ',';
                }
                Long v = value.get(i);
                if (v == null) {
                    off = writeNumberNull(writer, buf, off, features);
                    continue;
                }
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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, BigInteger value, long features) {
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
            str.getBytes(0, strlen, buf, off);
            off += strlen;
            if (writeAsString) {
                buf[off++] = '"';
            }
            return off;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, BigDecimal value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }

            byte quote = (byte) writer.quote;
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
                    && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1
                    && (unscaleValue = UNSAFE.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET)) != Long.MIN_VALUE
                    && !asPlain
            ) {
                off = IOUtils.writeDecimal(buf, off, unscaleValue, scale);
            } else {
                String str = asPlain ? value.toPlainString() : value.toString();
                str.getBytes(0, str.length(), buf, off);
                off += str.length();
            }

            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, float value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Float value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.floatValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, float[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, double value, long features) {
            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Double value, long features) {
            if (value == null) {
                return writeDoubleNull(writer, buf, off, features);
            }
            return writeValue(writer, buf, off, value.doubleValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, double[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            byte quote = (byte) writer.quote;

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

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, LocalDate value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            if (writer.context.dateFormat != null) {
                return writeValueWithFormat(writer, buf, off, value, features);
            }
            byte quote = (byte) writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalDate(buf, off + 1, value.getYear(), value.getMonthValue(), value.getDayOfMonth());
            buf[off] = quote;
            return off + 1;
        }

        private static int writeValueWithFormat(JSONWriterUTF8 writer, byte[] buf, int off, LocalDate value, long features) {
            Context context = writer.context;
            if (context.isDateFormatUnixTime()) {
                long millis = value.atStartOfDay(context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                return writeValue(writer, buf, off, millis / 1000, features);
            }
            if (context.isDateFormatMillis()) {
                long millis = value.atStartOfDay(context.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                return writeValue(writer, buf, off, millis, features);
            }
            if (context.isFormatyyyyMMddhhmmss19()) {
                return writeDateTime19(writer, buf, off, value.getYear(), value.getMonthValue(), value.getDayOfMonth(), 0, 0, 0);
            }
            switch (context.dateFormat) {
                case "yyyyMMdd":
                    return writeDateTime8(writer, buf, off, value.getYear(), value.getMonthValue(), value.getDayOfMonth());
                case "iso8601":
                case "yyyy-MM-dd":
                    return writeDateTime10(writer, buf, off, value.getYear(), value.getMonthValue(), value.getDayOfMonth());
                default: {
                    DateTimeFormatter formatter = context.getDateFormatter();
                    String str;
                    if (context.isDateFormatHasHour()) {
                        str = formatter.format(LocalDateTime.of(value, LocalTime.MIN));
                    } else {
                        str = formatter.format(value);
                    }
                    return writeValue(writer, buf, off, str, features);
                }
            }
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, LocalTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            if (writer.context.dateFormat != null) {
                return writeValueWithFormat(writer, buf, off, value, features);
            }
            byte quote = (byte) writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalTime(buf, off + 1, value);
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValueWithFormat(JSONWriterUTF8 writer, byte[] buf, int off, LocalTime value, long features) {
            Context context = writer.context;
            switch (context.dateFormat) {
                case "millis": {
                    LocalDateTime dateTime = LocalDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            value
                    );
                    Instant instant = dateTime.atZone(context.getZoneId()).toInstant();
                    long millis = instant.toEpochMilli();
                    return writeValue(writer, buf, off, millis, features);
                }
                case "unixtime": {
                    LocalDateTime dateTime = LocalDateTime.of(
                            LocalDate.of(1970, 1, 1),
                            value
                    );
                    Instant instant = dateTime.atZone(context.getZoneId()).toInstant();
                    long millis = instant.toEpochMilli();
                    return writeValue(writer, buf, off, millis / 1000, features);
                }
                default:
                    DateTimeFormatter formatter = context.getDateFormatter();
                    String str;
                    if (context.formatHasDay) {
                        str = formatter.format(LocalDateTime.of(LocalDate.of(1970, 1, 1), value));
                    } else {
                        str = formatter.format(value);
                    }
                    return writeValue(writer, buf, off, str, features);
            }
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, LocalDateTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            if (writer.context.dateFormat != null) {
                return writeLocalDateTimeWithFormat(writer, buf, off, value, features);
            }
            return writeLocalDateTime(writer, buf, off, value);
        }

        private static int writeLocalDateTime(JSONWriterUTF8 writer, byte[] buf, int off, LocalDateTime value) {
            byte quote = (byte) writer.quote;
            buf[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(buf, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            buf[off] = ' ';
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());
            buf[off] = quote;
            return off + 1;
        }

        private static int writeLocalDateTimeWithFormat(
                JSONWriterUTF8 writer,
                byte[] buf,
                int off,
                LocalDateTime ldt,
                long features) {
            Context ctx = writer.context;
            if (ctx.dateFormatUnixTime) {
                long millis = ldt.atZone(ctx.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                return writeValue(writer, buf, off, millis / 1000, features);
            }

            String format = ctx.dateFormat;

            if (ctx.dateFormat == null && ctx.isDateFormatMillis()) {
                long millis = ldt.atZone(ctx.getZoneId())
                        .toInstant()
                        .toEpochMilli();
                return writeValue(writer, buf, off, millis, features);
            }

            int year = ldt.getYear();
            if (year >= 0 && year <= 9999) {
                if (ctx.isDateFormatISO8601()) {
                    int nano = ldt.getNano() / 1000_000;
                    int offsetSeconds = ctx.getZoneId().getRules().getOffset(ldt).getTotalSeconds();
                    return writeDateTimeISO8601(writer, buf, off, year,
                            ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond(), nano, offsetSeconds, true);
                }

                switch (format) {
                    case "yyyy-MM-dd HH:mm:ss":
                        return writeDateTime19(
                                writer, buf, off,
                                year,
                                ldt.getMonthValue(),
                                ldt.getDayOfMonth(),
                                ldt.getHour(),
                                ldt.getMinute(),
                                ldt.getSecond());
                    case "yyyyMMddHHmmss":
                        return writeDateTime19(
                                writer, buf, off,
                                year,
                                ldt.getMonthValue(),
                                ldt.getDayOfMonth(),
                                ldt.getHour(),
                                ldt.getMinute(),
                                ldt.getSecond());
                    case "yyyy-MM-dd":
                        return writeDateTime10(
                                writer, buf, off,
                                year,
                                ldt.getMonthValue(),
                                ldt.getDayOfMonth());
                    case "yyyyMMdd":
                        return writeDateTime8(
                                writer, buf, off,
                                year,
                                ldt.getMonthValue(),
                                ldt.getDayOfMonth());
                    case "iso8601":
                        return writeDateTimeISO8601(writer, buf, off, year,
                                ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond(), ldt.getNano() / 1000_000, 0, false);
                    case "millis":
                        return writeValue(writer, buf, off, ldt.atZone(ctx.getZoneId())
                                .toInstant()
                                .toEpochMilli(), features);
                    default:
                        break;
                }
            }

            DateTimeFormatter formatter = ctx.getDateFormatter();
            if (formatter == null) {
                return writeLocalDateTime(writer, buf, off, ldt);
            }

            boolean useSimpleDateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX".equals(format);
            String str;
            if (useSimpleDateFormat) {
                Instant instant = ldt.toInstant(ctx.getZoneId().getRules().getOffset(ldt));
                Date date = new Date(instant.toEpochMilli());
                str = new SimpleDateFormat(format).format(date);
            } else {
                if (ctx.locale != null) {
                    ZonedDateTime zdt = ZonedDateTime.of(ldt, ctx.getZoneId());
                    str = formatter.format(zdt);
                } else {
                    str = formatter.format(ldt);
                }
            }
            return writeValue(writer, buf, off, str, features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, OffsetDateTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            byte quote = (byte) writer.quote;
            LocalDateTime ldt = value.toLocalDateTime();
            LocalDate date = ldt.toLocalDate();
            buf[off] = quote;
            off = IOUtils.writeLocalDate(buf, off + 1, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            buf[off++] = 'T';
            LocalTime time = ldt.toLocalTime();
            IOUtils.writeLocalTime(buf, off, time.getHour(), time.getMinute(), time.getSecond());
            off += 8;
            int nano = time.getNano();
            if (nano != 0) {
                off = IOUtils.writeNano(buf, off, nano);
            }
            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                buf[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getBytes(0, zoneId.length(), buf, off);
                off += zoneId.length();
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, OffsetTime value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            byte quote = (byte) writer.quote;
            buf[off] = quote;
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());

            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                buf[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getBytes(0, zoneId.length(), buf, off);
                off += zoneId.length();
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, ZonedDateTime value, long features) {
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

            byte quote = (byte) writer.quote;
            buf[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(buf, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            buf[off] = 'T';
            off = IOUtils.writeLocalTime(buf, off + 1, value.toLocalTime());
            if (zoneSize == 1) {
                buf[off++] = 'Z';
            } else if (firstZoneChar == '+' || firstZoneChar == '-') {
                zoneId.getBytes(0, zoneIdLength, buf, off);
                off += zoneIdLength;
            } else {
                buf[off++] = '[';
                zoneId.getBytes(0, zoneIdLength, buf, off);
                off += zoneIdLength;
                buf[off++] = ']';
            }
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Instant value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }
            if (writer.context.dateFormat != null) {
                return writeInstanceWithFormat(writer, buf, off, value, features);
            }
            byte quote = (byte) writer.quote;
            buf[off] = quote;
            String str = DateTimeFormatter.ISO_INSTANT.format(value);
            str.getBytes(0, str.length(), buf, off + 1);
            off += str.length() + 1;
            buf[off] = quote;
            return off + 1;
        }

        private static int writeInstanceWithFormat(JSONWriterUTF8 writer, byte[] buf, int off, Instant value, long features) {
            Context context = writer.context;
            String format = context.dateFormat;
            boolean yyyyMMddhhmmss14 = false, yyyyMMdd10 = false, yyyyMMdd8 = false;
            boolean formatyyyyMMddhhmmss19 = context.isFormatyyyyMMddhhmmss19();
            if (formatyyyyMMddhhmmss19
                    || (yyyyMMddhhmmss14 = "yyyyMMddHHmmss".equals(format))
                    || (yyyyMMdd10 = ("yyyy-MM-dd".equals(format)))
                    || (yyyyMMdd8 = ("yyyyMMdd".equals(format)))
            ) {
                final long SECONDS_PER_DAY = 60 * 60 * 24;
                ZoneId zoneId = context.getZoneId();
                long epochSecond = value.getEpochSecond();
                int offsetTotalSeconds;
                if (zoneId == DateUtils.SHANGHAI_ZONE_ID || zoneId.getRules() == DateUtils.SHANGHAI_ZONE_RULES) {
                    offsetTotalSeconds = DateUtils.getShanghaiZoneOffsetTotalSeconds(epochSecond);
                } else {
                    offsetTotalSeconds = zoneId.getRules().getOffset(value).getTotalSeconds();
                }

                long localSecond = epochSecond + offsetTotalSeconds;
                long localEpochDay = Math.floorDiv(localSecond, SECONDS_PER_DAY);
                int secsOfDay = (int) Math.floorMod(localSecond, SECONDS_PER_DAY);
                int year, month, dayOfMonth;
                {
                    final int DAYS_PER_CYCLE = 146097;
                    final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

                    long zeroDay = localEpochDay + DAYS_0000_TO_1970;
                    // find the march-based year
                    zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four year cycle
                    long adjust = 0;
                    if (zeroDay < 0) {
                        // adjust negative years to positive for calculation
                        long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
                        adjust = adjustCycles * 400;
                        zeroDay += -adjustCycles * DAYS_PER_CYCLE;
                    }
                    long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
                    long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
                    if (doyEst < 0) {
                        // fix estimate
                        yearEst--;
                        doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
                    }
                    yearEst += adjust;  // reset any negative year
                    int marchDoy0 = (int) doyEst;

                    // convert march-based values back to january-based
                    int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
                    month = (marchMonth0 + 2) % 12 + 1;
                    dayOfMonth = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
                    yearEst += marchMonth0 / 10;

                    // check year now we are certain it is correct
                    if (yearEst < Year.MIN_VALUE || yearEst > Year.MAX_VALUE) {
                        throw new DateTimeException("Invalid year " + yearEst);
                    }

                    year = (int) yearEst;
                }

                int hour, minute, second;
                {
                    final int MINUTES_PER_HOUR = 60;
                    final int SECONDS_PER_MINUTE = 60;
                    final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

                    long secondOfDay = secsOfDay;
                    if (secondOfDay < 0 || secondOfDay > 86399) {
                        throw new DateTimeException("Invalid secondOfDay " + secondOfDay);
                    }
                    int hours = (int) (secondOfDay / SECONDS_PER_HOUR);
                    secondOfDay -= hours * SECONDS_PER_HOUR;
                    int minutes = (int) (secondOfDay / SECONDS_PER_MINUTE);
                    secondOfDay -= minutes * SECONDS_PER_MINUTE;

                    hour = hours;
                    minute = minutes;
                    second = (int) secondOfDay;
                }

                if (formatyyyyMMddhhmmss19) {
                    return writeDateTime19(
                            writer,
                            buf,
                            off,
                            year,
                            month,
                            dayOfMonth,
                            hour,
                            minute,
                            second
                    );
                }

                if (yyyyMMddhhmmss14) {
                    return writeDateTime14(
                            writer,
                            buf,
                            off,
                            year,
                            month,
                            dayOfMonth,
                            hour,
                            minute,
                            second
                    );
                }

                if (yyyyMMdd10) {
                    return writeDateTime10(
                            writer,
                            buf,
                            off,
                            year,
                            month,
                            dayOfMonth
                    );
                }

                return writeDateTime8(
                        writer,
                        buf,
                        off,
                        year,
                        month,
                        dayOfMonth
                );
            }

            if (context.isDateFormatUnixTime()) {
                return writeValue(writer, buf, off, value.toEpochMilli() / 1000, features);
            }

            if (context.isDateFormatMillis()) {
                return writeValue(writer, buf, off, value.toEpochMilli(), features);
            }

            ZonedDateTime zdt = ZonedDateTime.ofInstant(value, context.getZoneId());
            int year = zdt.getYear();
            if (year >= 0 && year <= 9999) {
                if (context.isDateFormatISO8601()) {
                    return writeDateTimeISO8601(
                            writer,
                            buf,
                            off,
                            year,
                            zdt.getMonthValue(),
                            zdt.getDayOfMonth(),
                            zdt.getHour(),
                            zdt.getMinute(),
                            zdt.getSecond(),
                            zdt.getNano() / 1000_000,
                            zdt.getOffset().getTotalSeconds(),
                            true
                    );
                }
            }
            DateTimeFormatter formatter = context.getDateFormatter();
            return writeValue(writer, buf, off, formatter.format(zdt), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Enum value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            String str;
            if ((features & MASK_WRITE_ENUM_USING_TO_STRING) == 0) {
                if ((features & MASK_WRITE_ENUM_USING_ORDINAL) != 0) {
                    return writeValue(writer, buf, off, value.ordinal(), features);
                }
                str = value.name();
            } else {
                str = value.toString();
            }

            byte quote = (byte) writer.quote;
            buf[off++] = quote;
            byte[] utf8Bytes;
            if (STRING_CODER != null && STRING_CODER.applyAsInt(str) == 0) {
                utf8Bytes = STRING_VALUE.apply(str);
            } else {
                if (JVM_VERSION == 8 && FIELD_STRING_VALUE_OFFSET != -1) {
                    int start = off;
                    char[] chars = JDKUtils.toCharArrayJDK8(str);
                    boolean ascii = true;
                    for (int i = 0; i < chars.length; i++) {
                        char ch = chars[i];
                        if (ch > 0x7F) {
                            ascii = false;
                            off = start;
                            break;
                        }
                        buf[off++] = (byte) ch;
                    }
                    if (ascii) {
                        buf[off] = quote;
                        return off + 1;
                    }
                }
                utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
            }
            System.arraycopy(utf8Bytes, 0, buf, off, utf8Bytes.length);
            off += utf8Bytes.length;
            buf[off] = quote;
            return off + 1;
        }

        public static int writeValueJDK11(JSONWriterUTF8 writer, byte[] buf, int off, String[] values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }

            off = startArray(writer, buf, off);
            for (int i = 0; i < values.length; i++) {
                if (i != 0) {
                    off = writeComma(writer, buf, off);
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
            return endArray(writer, buf, off);
        }

        public static int writeValueJDK8(JSONWriterUTF8 writer, byte[] buf, int off, String[] value, long features) {
            if (value == null) {
                return writeArrayNull(buf, off, features);
            }

            off = startArray(writer, buf, off);
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    off = writeComma(writer, buf, off);
                }
                String str = value[i];
                if (FIELD_STRING_VALUE_OFFSET != -1) {
                    off = writeValueJDK8(writer, buf, off, JDKUtils.toCharArrayJDK8(str), features);
                } else {
                    off = writeValueJDK8(writer, buf, off, str, features);
                }
            }
            return endArray(writer, buf, off);
        }

        public static int writeValueJDK8(JSONWriterUTF8 writer, byte[] buf, int off, char[] value, long features) {
            if (value == null) {
                return writeStringNull(buf, off, features);
            }

            boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;
            boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;

            byte quote = (byte) writer.quote;
            buf[off++] = (byte) quote;

            int i = 0;
            for (; i < value.length; i++) {
                char c = value[i];
                if (c == quote
                        || c == '\\'
                        || c < ' '
                        || c > 0x007F
                        || (browserSecure
                        && (c == '<' || c == '>' || c == '(' || c == ')'))
                ) {
                    break;
                }
                buf[off++] = (byte) c;
            }

            if (i < value.length) {
                off = writeStringEscapedRest(writer, buf, off, value, value.length, browserSecure, escapeNoneAscii, i);
            }

            buf[off] = (byte) quote;
            return off + 1;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, String value, long features) {
            if (STRING_VALUE != null) {
                return writeValueJDK11(writer, buf, off, value, features);
            } else {
                return writeValueJDK8(writer, buf, off, value, features);
            }
        }

        public static int writeValueJDK11(JSONWriterUTF8 writer, byte[] buf, int off, String value, long features) {
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

        public static int writeStringLatin1(JSONWriterUTF8 writer, byte[] buf, int off, byte[] value, long features) {
            if ((features & MASK_BROWSER_SECURE) != 0) {
                return writeStringLatin1BrowserSecure(writer, buf, off, value, features);
            }

            byte quote = (byte) writer.quote;
            if (StringUtils.escaped(value, quote, writer.byteVectorQuote)) {
                return StringUtils.writeLatin1Escaped(buf, off, value, quote, features);
            }

            int strlen = value.length;
            buf[off] = quote;
            System.arraycopy(value, 0, buf, off + 1, strlen);
            buf[off + strlen + 1] = quote;
            return off + strlen + 2;
        }

        public static int writeStringLatin1BrowserSecure(JSONWriterUTF8 writer, byte[] buf, int off, byte[] value, long features) {
            final byte quote = (byte) writer.quote;
            int i = 0;
            for (; i < value.length; i++) {
                byte c = value[i];
                if (c == quote || c == '\\' || c < ' ' || c == '<' || c == '>' || c == '(' || c == ')') {
                    break;
                }
            }

            if (i == value.length) {
                buf[off] = quote;
                System.arraycopy(value, 0, buf, off + 1, value.length);
                off += value.length + 1;
                buf[off] = quote;
                return off + 1;
            }
            return StringUtils.writeLatin1Escaped(buf, off, value, (byte) quote, features);
        }

        public static int writeStringUTF16(JSONWriterUTF8 writer, byte[] buf, int off, byte[] value, long features) {
            return StringUtils.writeUTF16(buf, off, value, (byte) writer.quote, features);
        }

        public static int writeValueJDK8(JSONWriterUTF8 writer, byte[] buf, int off, String value, long features) {
            if (value == null) {
                return writeStringNull(buf, off, features);
            }
            char[] chars = getCharArray(value);

            boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;
            boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;

            byte quote = (byte) writer.quote;
            buf[off++] = (byte) quote;

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

                buf[off++] = (byte) c0;
            }

            if (i == chars.length) {
                buf[off] = (byte) quote;
                return off + 1;
            }

            if (i < chars.length) {
                off = writeStringEscapedRest(writer, buf, off, chars, chars.length, browserSecure, escapeNoneAscii, i);
            }

            buf[off] = quote;
            return off + 1;
        }

        static int writeStringEscapedRest(
                JSONWriterUTF8 writer, byte[] buf, int off,
                char[] chars,
                int end,
                boolean browserSecure,
                boolean escapeNoneAscii,
                int i
        ) {
            byte quote = (byte) writer.quote;
            int rest = chars.length - i;
            for (; i < end; ++i) { // ascii none special fast write
                char ch = chars[i];
                if (ch <= 0x007F) {
                    switch (ch) {
                        case '\\':
                        case '\n':
                        case '\r':
                        case '\f':
                        case '\b':
                        case '\t':
                            StringUtils.writeEscapedChar(buf, off, ch);
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
                            StringUtils.writeU4Hex2(buf, off, ch);
                            off += 6;
                            break;
                        case '<':
                        case '>':
                        case '(':
                        case ')':
                            if (browserSecure) {
                                StringUtils.writeU4HexU(buf, off, ch);
                                off += 6;
                            } else {
                                buf[off++] = (byte) ch;
                            }
                            break;
                        default:
                            if (ch == quote) {
                                buf[off] = '\\';
                                buf[off + 1] = (byte) quote;
                                off += 2;
                            } else {
                                buf[off++] = (byte) ch;
                            }
                            break;
                    }
                } else if (escapeNoneAscii) {
                    StringUtils.writeU4HexU(buf, off, ch);
                    off += 6;
                } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                    final int uc;
                    if (ch < '\uDBFF' + 1) { // Character.isHighSurrogate(c)
                        if (chars.length - i < 2) {
                            uc = -1;
                        } else {
                            char d = chars[i + 1];
                            // d >= '\uDC00' && d < ('\uDFFF' + 1)
                            if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                            } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                buf[off++] = '?';
                                continue;
                            }
                        }
                    } else {
                        //
                        // Character.isLowSurrogate(c)
                        buf[off++] = '?';
                        continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                    }

                    if (uc < 0) {
                        buf[off++] = '?';
                    } else {
                        buf[off] = (byte) (0xf0 | ((uc >> 18)));
                        buf[off + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                        buf[off + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                        buf[off + 3] = (byte) (0x80 | (uc & 0x3f));
                        off += 4;
                        i++; // 2 chars
                    }
                } else if (ch > 0x07FF) {
                    buf[off] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                    buf[off + 1] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                    buf[off + 2] = (byte) (0x80 | (ch & 0x3F));
                    off += 3;
                } else {
                    buf[off] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                    buf[off + 1] = (byte) (0x80 | (ch & 0x3F));
                    off += 2;
                }
            }
            return off;
        }

        public static int writeValueJDK11(JSONWriterUTF8 writer, byte[] buf, int off, List<String> values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }

            off = startArray(writer, buf, off);
            for (int i = 0; i < values.size(); i++) {
                if (i != 0) {
                    off = writeComma(writer, buf, off);
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
            return endArray(writer, buf, off);
        }

        public static int writeValueJDK8(JSONWriterUTF8 writer, byte[] buf, int off, List<String> values, long features) {
            if (values == null) {
                return writeArrayNull(buf, off, features);
            }

            off = startArray(writer, buf, off);
            for (int i = 0; i < values.size(); i++) {
                if (i != 0) {
                    off = writeComma(writer, buf, off);
                }
                String value = values.get(i);
                if (value == null) {
                    off = writeStringNull(buf, off, features);
                } else {
                    if (FIELD_STRING_VALUE_OFFSET != -1) {
                        off = writeValueJDK8(writer, buf, off, JDKUtils.toCharArrayJDK8(value), features);
                    } else {
                        off = writeValueJDK8(writer, buf, off, value, features);
                    }
                }
            }
            return endArray(writer, buf, off);
        }

        public static int startArray(JSONWriterUTF8 writer, byte[] buf, int off) {
            int level = ++writer.level;
            if (level > writer.context.maxLevel) {
                throw overflowLevel(level);
            }
            buf[off++] = '[';
            if (writer.pretty != PRETTY_NON) {
                off = indent(writer, buf, off);
            }
            return off;
        }

        public static int endArray(JSONWriterUTF8 writer, byte[] buf, int off) {
            writer.level--;
            if (writer.pretty != PRETTY_NON) {
                off = indent(writer, buf, off);
            }
            buf[off] = ']';
            writer.startObject = false;
            return off + 1;
        }

        public static int writeComma(JSONWriterUTF8 writer, byte[] buf, int off) {
            buf[off++] = ',';
            if (writer.pretty != PRETTY_NON) {
                off = indent(writer, buf, off);
            }
            return off;
        }

        private static int indent(JSONWriterUTF8 writer, byte[] chars, int off) {
            chars[off] = '\n';
            int toIndex = off + 1 + writer.pretty * writer.level;
            Arrays.fill(chars, off + 1, toIndex, (byte) (writer.pretty == PRETTY_TAB ? '\t' : ' '));
            return toIndex;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Character value, long features) {
            if (value == null) {
                return IO.writeStringNull(buf, off, features);
            }
            return writeValue(writer, buf, off, value.charValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, char value, long features) {
            byte quote = (byte) writer.quote;
            buf[off++] = (byte) quote;
            if (value <= 0x007F) {
                switch (value) {
                    case '\\':
                    case '\n':
                    case '\r':
                    case '\f':
                    case '\b':
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
                        StringUtils.writeU4Hex2(buf, off, value);
                        off += 6;
                        break;
                    default:
                        if (value == quote) {
                            buf[off] = '\\';
                            buf[off + 1] = (byte) quote;
                            off += 2;
                        } else {
                            buf[off++] = (byte) value;
                        }
                        break;
                }
            } else if (value >= '\uD800' && value < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                throw new JSONException("illegal char " + value);
            } else if (value > 0x07FF) {
                buf[off] = (byte) (0xE0 | ((value >> 12) & 0x0F));
                buf[off + 1] = (byte) (0x80 | ((value >> 6) & 0x3F));
                buf[off + 2] = (byte) (0x80 | (value & 0x3F));
                off += 3;
            } else {
                buf[off] = (byte) (0xC0 | ((value >> 6) & 0x1F));
                buf[off + 1] = (byte) (0x80 | (value & 0x3F));
                off += 2;
            }

            buf[off] = (byte) quote;
            return off + 1;
        }

        public static int writeBooleanNull(byte[] buf, int off, long features) {
            String raw = (features & WriteBooleanAsNumber.mask) != 0 ? "0"
                    : ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_BOOLEAN_AS_FALSE)) != 0 ? "false" : "null");
            raw.getBytes(0, raw.length(), buf, off);
            return off + raw.length();
        }

        public static int writeStringNull(byte[] buf, int off, long features) {
            String raw;
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) != 0) {
                raw = (features & MASK_USE_SINGLE_QUOTES) != 0 ? "''" : "\"\"";
            } else {
                raw = "null";
            }
            raw.getBytes(0, raw.length(), buf, off);
            return off + raw.length();
        }

        public static int writeLongNull(JSONWriterUTF8 writer, byte[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & (MASK_WRITE_NON_STRING_VALUE_AS_STRING | MASK_WRITE_LONG_AS_STRING)) != 0) {
                    byte quote = (byte) writer.quote;
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

        public static int writeDoubleNull(JSONWriterUTF8 writer, byte[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
                    byte quote = (byte) writer.quote;
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

        public static int writeNumberNull(JSONWriterUTF8 writer, byte[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
                    byte quote = (byte) writer.quote;
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

        public static int writeArrayNull(byte[] buf, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_LIST_AS_EMPTY)) != 0) {
                return writeEmptyArray(buf, off);
            } else {
                return writeNull(buf, off);
            }
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, Boolean value, long features) {
            if (value == null) {
                return writeBooleanNull(buf, off, features);
            }
            return writeValue(writer, buf, off, value.booleanValue(), features);
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, boolean value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                buf[off++] = quote;
            }
            if ((features & MASK_WRITE_BOOLEAN_AS_NUMBER) != 0) {
                buf[off++] = (byte) (value ? '1' : '0');
            } else {
                off = IOUtils.putBoolean(buf, off, value);
            }
            if (writeAsString) {
                buf[off++] = quote;
            }

            return off;
        }

        public static int writeValue(JSONWriterUTF8 writer, byte[] buf, int off, UUID value, long features) {
            if (value == null) {
                return writeNull(buf, off);
            }

            byte quote = (byte) writer.quote;

            final long base = ARRAY_BYTE_BASE_OFFSET + off;
            UNSAFE.putByte(buf, base, quote);
            UNSAFE.putByte(buf, base + 9, (byte) '-');
            UNSAFE.putByte(buf, base + 14, (byte) '-');
            UNSAFE.putByte(buf, base + 19, (byte) '-');
            UNSAFE.putByte(buf, base + 24, (byte) '-');
            UNSAFE.putByte(buf, base + 37, quote);
            long msb = value.getMostSignificantBits();
            long lsb = value.getLeastSignificantBits();
            long x = msb, x0 = hex8(x >>> 32), x1 = hex8(x);
            UNSAFE.putLong(buf, base + 1, x0);
            UNSAFE.putInt(buf, base + 10, (int) x1);
            UNSAFE.putInt(buf, base + 15, (int) (x1 >>> 32));
            x = lsb;
            x0 = hex8(x >>> 32);
            x1 = hex8(x);
            UNSAFE.putInt(buf, base + 20, (int) (x0));
            UNSAFE.putInt(buf, base + 25, (int) (x0 >>> 32));
            UNSAFE.putLong(buf, base + 29, x1);

            return off + 38;
        }

        public static int valueSize(boolean value) {
            return 7;
        }

        public static int valueSize(byte value) {
            return 6;
        }

        public static int valueSize(Byte value) {
            return 6;
        }

        public static int valueSize(short value) {
            return 8;
        }

        public static int valueSize(Short value) {
            return 8;
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
            return IOUtils.stringSize(value) + 2;
        }

        public static int valueSize(BigDecimal value) {
            return IOUtils.stringSize(value) + 2;
        }

        public static int nameSize(JSONWriterUTF8 writer, byte[] name) {
            return name.length + 2 + writer.pretty * writer.level;
        }

        public static int stringCapacity(String value) {
            return value == null ? 4 : value.length() * 6 + 2;
        }

        public static int stringCapacityLatin1(byte[] value) {
            return value == null ? 4 : value.length * 2 + 2;
        }

        public static int stringCapacityUTF16(byte[] value) {
            return value == null ? 4 : value.length * 3 + 2;
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

        public static int writeDateTime10(
                JSONWriterUTF8 writer,
                byte[] chars,
                int off,
                int year,
                int month,
                int dayOfMonth
        ) {
            chars[off] = (byte) writer.quote;
            off = IOUtils.writeLocalDate(chars, off + 1, year, month, dayOfMonth);
            chars[off] = (byte) writer.quote;
            return off + 1;
        }

        public static int writeDateTime8(
                JSONWriterUTF8 writer,
                byte[] chars,
                int off,
                int year,
                int month,
                int dayOfMonth
        ) {
            chars[off] = (byte) writer.quote;
            if (year < 0 || year > 9999) {
                throw illegalYear(year);
            }
            int y01 = year / 100;
            int y23 = year - y01 * 100;
            writeDigitPair(chars, off + 1, y01);
            writeDigitPair(chars, off + 3, y23);
            writeDigitPair(chars, off + 5, month);
            writeDigitPair(chars, off + 7, dayOfMonth);
            chars[off + 9] = (byte) writer.quote;
            return off + 10;
        }

        public static int writeDateTime14(
                JSONWriterUTF8 writer,
                byte[] chars,
                int off,
                int year,
                int month,
                int dayOfMonth,
                int hour,
                int minute,
                int second
        ) {
            chars[off] = (byte) writer.quote;
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
            chars[off + 15] = (byte) writer.quote;
            return off + 16;
        }

        public static int writeDateTime19(
                JSONWriterUTF8 writer,
                byte[] chars,
                int off,
                int year,
                int month,
                int dayOfMonth,
                int hour,
                int minute,
                int second
        ) {
            chars[off] = (byte) writer.quote;
            if (year < 0 || year > 9999) {
                throw illegalYear(year);
            }
            off = IOUtils.writeLocalDate(chars, off + 1, year, month, dayOfMonth);
            chars[off] = ' ';
            IOUtils.writeLocalTime(chars, off + 1, hour, minute, second);
            chars[off + 9] = (byte) writer.quote;
            return off + 10;
        }

        public static int writeDateTimeISO8601(
                JSONWriterUTF8 writer,
                byte[] chars,
                int off,
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
            chars[off] = (byte) writer.quote;
            off = IOUtils.writeLocalDate(chars, off + 1, year, month, dayOfMonth);
            chars[off] = (byte) (timeZone ? 'T' : ' ');
            IOUtils.writeLocalTime(chars, off + 1, hour, minute, second);
            off += 9;

            if (millis > 0) {
                int div = millis / 10;
                int div2 = div / 10;
                final int rem1 = millis - div * 10;

                if (rem1 != 0) {
                    IOUtils.putIntLE(chars, off, DIGITS_K_32[millis & 0x3ff] & 0xffffff00 | '.');
                    off += 4;
                } else {
                    chars[off++] = '.';
                    final int rem2 = div - div2 * 10;
                    if (rem2 != 0) {
                        writeDigitPair(chars, off, div);
                        off += 2;
                    } else {
                        chars[off++] = (byte) (byte) (div2 + '0');
                    }
                }
            }

            if (timeZone) {
                int offset = offsetSeconds / 3600;
                if (offsetSeconds == 0) {
                    chars[off++] = 'Z';
                } else {
                    int offsetAbs = Math.abs(offset);
                    chars[off] = (byte) (offset >= 0 ? '+' : '-');
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
            chars[off] = (byte) writer.quote;
            return off + 1;
        }

        public static byte[] buffer(JSONWriterUTF8 writer) {
            return writer.bytes;
        }

        public static byte[] ensureCapacity(JSONWriterUTF8 writer, byte[] buf, int minCapacity) {
            buf = Arrays.copyOf(buf, writer.newCapacity(minCapacity, buf.length));
            writer.bytes = buf;
            return buf;
        }

        public static byte[] ensureCapacity(JSONWriterUTF8 writer, int minCapacity) {
            byte[] buf = writer.bytes;
            if (minCapacity > buf.length) {
                buf = ensureCapacity(writer, buf, minCapacity);
            }
            return buf;
        }

        public static int int64Capacity(JSONWriterUTF8 writer, Collection<Long> values) {
            return values == null ? 4 : values.size() * (23 /* long value size */ + 1);
        }

        public static int int64Capacity(JSONWriterUTF8 writer, List<Long> values) {
            return values == null ? 4 : values.size() * (23 /* long value size */ + 1);
        }

        public static int stringCapacityJDK8(String value) {
            return value == null ? 4 : value.length() * 6 + 2;
        }

        public static int stringCapacityJDK11(String value) {
            return value == null ? 4 : value.length() * 6 + 2;
        }

        public static int stringCapacityJDK11(JSONWriterUTF8 writer, Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.size();
            for (String string : strings) {
                size += string == null ? 4 : string.length() * 6 + 2;
            }
            return size;
        }

        public static int stringCapacityJDK8(JSONWriterUTF8 writer, List<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.size() * 3;
            for (int i = 0; i < strings.size(); i++) {
                String string = strings.get(i);
                size += string == null ? 4 : string.length() * 6;
            }
            return size;
        }

        public static int stringCapacityJDK11(JSONWriterUTF8 writer, List<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.size();
            for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                String string = strings.get(i);
                size += string == null ? 4 : string.length() * 6 + 2;
            }
            return size;
        }

        public static int stringCapacity(JSONWriterUTF8 writer, Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.size();
            for (String value : strings) {
                size += value == null ? 4 : value.length() * 6 + 2;
            }
            return size;
        }

        public static int stringCapacity(JSONWriterUTF8 writer, List<String> strings) {
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

        public static int stringCapacityJDK8(JSONWriterUTF8 writer, String[] value) {
            if (value == null) {
                return 4;
            }
            int size = value.length + 2;
            for (String str : value) {
                size += str == null ? 4 : str.length() * 6 + 2;
            }
            return size;
        }

        public static int stringCapacityJDK11(JSONWriterUTF8 writer, String[] value) {
            if (value == null) {
                return 4;
            }
            int size = value.length + 2;
            for (String str : value) {
                size += str == null ? 4 : str.length() * 6 + 2;
            }
            return size;
        }

        public static int writeNameBefore(JSONWriterUTF8 writer, byte[] buf, int off) {
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

        public static int writeName(JSONWriterUTF8 writer, byte[] buf, int off, byte[] name) {
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

        public static int writeNull(byte[] buf, int off) {
            IOUtils.putNULL(buf, off);
            return off + 4;
        }

        public static int writeEmptyArray(byte[] buf, int off) {
            buf[off] = '[';
            buf[off + 1] = ']';
            return off + 2;
        }

        /**
         * Extract the least significant 4 bytes from the input integer i, convert each byte into its corresponding 2-digit
         * hexadecimal representation, concatenate these hexadecimal strings into one continuous string, and then interpret
         * this string as a hexadecimal number to form and return a long value.
         */
        private static long hex8(long i) {
            i = expand(i);
        /*
            Use long to simulate vector operations and generate 8 hexadecimal characters at a time.
            ------------
            0  = 0b0000_0000 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '0'
            1  = 0b0000_0001 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '1'
            2  = 0b0000_0010 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '2'
            3  = 0b0000_0011 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '3'
            4  = 0b0000_0100 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '4'
            5  = 0b0000_0101 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '5'
            6  = 0b0000_0110 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '6'
            7  = 0b0000_0111 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '7'
            8  = 0b0000_1000 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '8'
            9  = 0b0000_1001 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 0  + 0x30 + (i & 0xF) => '9'
            10 = 0b0000_1010 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'a'
            11 = 0b0000_1011 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'b'
            12 = 0b0000_1100 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'c'
            13 = 0b0000_1101 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'd'
            14 = 0b0000_1110 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'e'
            15 = 0b0000_1111 => m = ((i + 6) & 0x10); (m << 1) + (m >> 1) - (m >> 4) => 39 + 0x30 + (i & 0xF) => 'f'
         */
            long m = (i + 0x0606_0606_0606_0606L) & 0x1010_1010_1010_1010L;
            long v = ((m << 1) + (m >> 1) - (m >> 4)) + 0x3030_3030_3030_3030L + i;
            if (!BIG_ENDIAN) {
                v = Long.reverseBytes(v);
            }
            return v;
        }

        /**
         * A faster alternative that is functionally equivalent to Long.expand(i, 0x0F0F_0F0F_0F0F_0F0FL)
         */
        private static long expand(long i) {
            long t = i << 16;
            i = (i & ~0xFFFF00000000L) | (t & 0xFFFF00000000L);
            t = i << 8;
            i = (i & ~0xFF000000FF0000L) | (t & 0xFF000000FF0000L);
            t = i << 4;
            i = (i & ~0xF000F000F000F00L) | (t & 0xF000F000F000F00L);
            return i & 0x0F0F_0F0F_0F0F_0F0FL;
        }
    }
}

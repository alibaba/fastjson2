package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.NumberUtils;
import com.alibaba.fastjson2.util.StringUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

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
import static com.alibaba.fastjson2.internal.Conf.BYTES;
import static com.alibaba.fastjson2.internal.Conf.DECIMAL_INT_COMPACT;
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
        REF = BYTES.getLongUnaligned(chars, 0);
        QUOTE2_COLON = BYTES.getShortUnaligned(chars, 6);
        chars[6] = '\'';
        QUOTE_COLON = BYTES.getShortUnaligned(chars, 6);
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
        BYTES.putLongUnaligned(bytes, off, REF);
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
        BYTES.putShortLE(bytes, off, (short) ('x' | ('\'' << 8)));
        off += 2;

        for (int i = 0; i < values.length; i++) {
            BYTES.putShortLE(bytes, off, hex2U(values[i]));
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
        if (pretty != PRETTY_NON || strings == null) {
            super.writeString(strings);
            return;
        }
        int off = this.off;
        byte[] bytes = grow1(off);
        bytes[off] = '[';
        this.off = off + 1;

        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                off = this.off;
                bytes = grow1(off);
                bytes[off] = ',';
                this.off = off + 1;
            }

            writeString(
                    strings[i]);
        }

        off = this.off;
        bytes = grow1(off);
        bytes[off] = ']';
        this.off = off + 1;
    }

    public final void writeString(List<String> list) {
        if (pretty != PRETTY_NON) {
            super.writeString(list);
            return;
        }
        int off = this.off;
        byte[] bytes = grow1(off);
        bytes[off] = (byte) '[';
        this.off = off + 1;

        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                off = this.off;
                bytes = grow1(off);
                bytes[off] = ',';
                this.off = off + 1;
            }

            String str = list.get(i);
            if (str == null) {
                writeStringNull();
            } else {
                if (STRING_VALUE != null) {
                    byte[] value = STRING_VALUE.apply(str);
                    if (STRING_CODER.applyAsInt(str) == 0) {
                        writeStringLatin1(value);
                    } else {
                        writeStringUTF16(value);
                    }
                } else {
                    writeStringJDK8(str);
                }
            }
        }

        off = this.off;
        bytes = grow1(off);
        bytes[off] = ']';
        this.off = off + 1;
    }

    @Override
    public final void writeString(boolean value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features | MASK_WRITE_NON_STRING_VALUE_AS_STRING);
    }

    @Override
    public final void writeString(byte value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features | MASK_WRITE_NON_STRING_VALUE_AS_STRING);
    }

    @Override
    public final void writeString(short value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features | MASK_WRITE_NON_STRING_VALUE_AS_STRING);
    }

    @Override
    public final void writeString(int value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features | MASK_WRITE_NON_STRING_VALUE_AS_STRING);
    }

    @Override
    public final void writeString(long value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features | MASK_WRITE_NON_STRING_VALUE_AS_STRING);
    }

    private void writeQuote() {
        writeRaw((byte) quote);
    }

    public void writeString(byte[] name, String str, long features) {
        if (STRING_VALUE != null) {
            byte[] value = STRING_VALUE.apply(str);
            if (STRING_CODER.applyAsInt(str) == 0) {
                writeStringLatin1(name, value, features);
            } else {
                writeStringUTF16(name, value, features);
            }
        } else {
            writeStringJDK8(name, str, features);
        }
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        if (STRING_VALUE != null) {
            byte[] value = STRING_VALUE.apply(str);
            if (STRING_CODER.applyAsInt(str) == 0) {
                writeStringLatin1(value);
            } else {
                writeStringUTF16(value);
            }
        } else {
            writeStringJDK8(str);
        }
    }

    public void writeStringJDK8(byte[] name, String str, long features) {
        writeNameRaw(name);
        writeStringJDK8(str);
    }

    private void writeStringJDK8(String str) {
        char[] chars = getCharArray(str);

        long features = context.features;
        boolean browserSecure = (features & MASK_BROWSER_SECURE) != 0;
        boolean escapeNoneAscii = (features & MASK_ESCAPE_NONE_ASCII) != 0;

        int off = this.off;
        // ensureCapacity
        int minCapacity = off
                + chars.length * 3 // utf8 3 bytes
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

    public final void writeStringLatin1(byte[] name, byte[] value, long features) {
        if ((features & MASK_BROWSER_SECURE) != 0) {
            writeStringLatin1BrowserSecure(name, value);
            return;
        }

        byte quote = (byte) this.quote;
        if (StringUtils.escaped(value, quote, byteVectorQuote)) {
            writeStringEscaped(name, value);
            return;
        }

        int off = this.off;
        int minCapacity = off + name.length + 2 + pretty * level + 23;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        off = writeNameBefore(bytes, off);
        System.arraycopy(name, 0, bytes, off, name.length);
        off += name.length;
        this.off = StringUtils.writeLatin1(bytes, off, value, quote);
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

    protected final void writeStringLatin1BrowserSecure(byte[] name, byte[] value) {
        writeNameRaw(name);
        writeStringLatin1BrowserSecure(value);
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

    public final void writeStringUTF16(byte[] name, byte[] value, long features) {
        writeNameRaw(name);
        writeStringUTF16(value);
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

    protected final void writeStringEscaped(byte[] name, byte[] values) {
        writeNameRaw(name);
        writeStringEscaped(values);
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

    public final void writeUUID(byte[] name, UUID value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeUUID(UUID value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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

    public int writeNameBefore(byte[] bytes, int off) {
        if (startObject) {
            startObject = false;
        } else {
            bytes[off++] = ',';
            if (pretty != PRETTY_NON) {
                off = indent(bytes, off);
            }
        }
        return off;
    }

    public final byte[] ensureCapacityForNameName(int nameLength) {
        return ensureCapacity(off + nameLength + 2 + pretty * level);
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

        BYTES.putLongUnaligned(bytes, off, name);
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

        BYTES.putLongUnaligned(bytes, off, name);
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

        BYTES.putLongUnaligned(bytes, off, name);
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

        BYTES.putLongUnaligned(bytes, off, name);
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

        BYTES.putLongUnaligned(bytes, off, name);
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

        BYTES.putLongUnaligned(bytes, off, name);
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
        BYTES.putLongUnaligned(bytes, off + 1, name);
        BYTES.putShortUnaligned(bytes, off + 9, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putIntUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
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

        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
        BYTES.putShortUnaligned(bytes, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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
        BYTES.putLongUnaligned(bytes, off, name0);
        BYTES.putLongUnaligned(bytes, off + 8, name1);
        BYTES.putShortUnaligned(bytes, off + 16, useSingleQuote ? QUOTE_COLON : QUOTE2_COLON);
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
    public final void writeInt8(byte value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeInt8(byte[] value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeInt16(short value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeInt32(Integer value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeInt32(int value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    public final void writeInt32(byte[] name, int value, long features) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.nameSize(this, name) + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, features);
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
            Integer item = values.get(i);
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
    public final void writeInt64(long[] value) {
        int off = this.off;
        int minCapacity = off + IO.valueSize(value);
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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

    public final void writeInt64(byte[] name, long value, long features) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.nameSize(this, name) + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, features);
    }

    @Override
    public final void writeInt64(long value, long features) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, features);
    }

    @Override
    public final void writeInt64(Long value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeFloat(float value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    public final void writeDouble(byte[] name, double value, long features) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.nameSize(this, name) + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, features);
    }

    @Override
    public final void writeDouble(double value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeFloat(float[] value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeDouble(double[] value) {
        int off = this.off;
        int minCapacity = off + IO.valueSize(value);
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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
        if (value == null) {
            writeNull();
            return;
        }

        if (context.dateFormat != null
                && writeLocalDateWithFormat(value)) {
            return;
        }

        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    public final void writeLocalDate(byte[] name, LocalDate value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.nameSize(this, name) + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeLocalDateTime(LocalDateTime value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeZonedDateTime(ZonedDateTime value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    public final void writeOffsetDateTime(byte[] name, OffsetDateTime value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.nameSize(this, name) + IO.valueSize(value));
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeOffsetDateTime(OffsetDateTime value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    public final void writeOffsetTime(OffsetTime value) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
    }

    @Override
    public final void writeBigInt(BigInteger value, long features) {
        int off = this.off;
        byte[] bytes = IO.ensureCapacity(this, off + IO.valueSize(value));
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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
                BYTES.putIntLE(bytes, off, DIGITS_K_32[millis & 0x3ff] & 0xffffff00 | '.');
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
            String str = value == null ? "null" : format.format(value);
            writeRaw(str);
            return;
        }

        int off = this.off;
        int minCapacity = off + value.precision() + Math.abs(value.scale()) + 7;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        this.off = IO.writeValue(this, bytes, off, value, features | context.features);
    }

    public final void writeDecimal(byte[] name, BigDecimal value, long features) {
        int precision = value.precision();

        int off = this.off;
        int minCapacity = off + name.length + 2 + pretty * level + precision + Math.abs(value.scale()) + 30;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        off = IO.writeName(this, bytes, off, name);
        this.off = IO.writeValue(this, bytes, off, value, features);
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
        int off = this.off;
        int minCapacity = off + IO.valueSize(value);
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeValue(this, bytes, off, value, context.features);
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

    interface IO {
        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, byte value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                bytes[off++] = quote;
            }
            off = IOUtils.writeInt8(bytes, off, value);
            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Byte value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.byteValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, byte[] value, long features) {
            if (value == null) {
                return writeArrayNull(bytes, off, features);
            }

            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            bytes[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    bytes[off++] = ',';
                }
                if (writeAsString) {
                    bytes[off++] = quote;
                }
                off = IOUtils.writeInt8(bytes, off, value[i]);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
            }
            bytes[off] = ']';

            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, short value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                bytes[off++] = quote;
            }
            off = IOUtils.writeInt16(bytes, off, value);
            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Short value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.shortValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, int value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            if (writeAsString) {
                bytes[off++] = quote;
            }
            off = IOUtils.writeInt32(bytes, off, value);
            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Integer value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.intValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, int[] value, long features) {
            if (value == null) {
                return writeArrayNull(bytes, off, features);
            }

            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;

            bytes[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    bytes[off++] = ',';
                }
                if (writeAsString) {
                    bytes[off++] = quote;
                }
                off = IOUtils.writeInt32(bytes, off, value[i]);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
            }
            bytes[off] = ']';

            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, long value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = isWriteAsString(value, features);
            if (writeAsString) {
                bytes[off++] = quote;
            }
            off = IOUtils.writeInt64(bytes, off, value);
            if (writeAsString) {
                bytes[off++] = quote;
            } else if ((features & MASK_WRITE_CLASS_NAME) != 0
                    && (features & MASK_NOT_WRITE_NUMBER_CLASS_NAME) == 0
                    && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
            ) {
                bytes[off++] = 'L';
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Long value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.longValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, long[] value, long features) {
            if (value == null) {
                return writeArrayNull(bytes, off, features);
            }

            byte quote = (byte) writer.quote;

            bytes[off++] = '[';

            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    bytes[off++] = ',';
                }
                long v = value[i];
                boolean writeAsString = isWriteAsString(v, features);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
                off = IOUtils.writeInt64(bytes, off, v);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
            }

            bytes[off] = ']';

            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, BigInteger value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }

            if (isInt64(value) && features == 0) {
                return writeValue(writer, bytes, off, value.longValue(), features);
            }

            String str = value.toString(10);

            boolean writeAsString = isWriteAsString(value, features);

            int strlen = str.length();
            if (writeAsString) {
                bytes[off++] = '"';
            }
            str.getBytes(0, strlen, bytes, off);
            off += strlen;
            if (writeAsString) {
                bytes[off++] = '"';
            }
            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, BigDecimal value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }

            byte quote = (byte) writer.quote;
            boolean writeAsString = isWriteAsString(value, features);
            if (writeAsString) {
                bytes[off++] = quote;
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
                off = IOUtils.writeDecimal(bytes, off, unscaleValue, scale);
            } else {
                String str = asPlain ? value.toPlainString() : value.toString();
                str.getBytes(0, str.length(), bytes, off);
                off += str.length();
            }

            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, float value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & WriteFloatSpecialAsString.mask) != 0;
            if (writeAsString) {
                bytes[off++] = quote;
            }

            off = NumberUtils.writeFloat(bytes, off, value, true, writeSpecialAsString);

            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Float value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.floatValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, float[] value, long features) {
            if (value == null) {
                return writeArrayNull(bytes, off, features);
            }

            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & WriteFloatSpecialAsString.mask) != 0;
            bytes[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    bytes[off++] = ',';
                }

                if (!Float.isFinite(value[i])) {
                    off = NumberUtils.writeFloat(bytes, off, value[i], true, writeSpecialAsString);
                    continue;
                }
                if (writeAsString) {
                    bytes[off++] = quote;
                }
                off = NumberUtils.writeFloat(bytes, off, value[i], true, false);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
            }
            bytes[off] = ']';

            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, double value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & WriteFloatSpecialAsString.mask) != 0;
            if (writeAsString) {
                bytes[off++] = quote;
            }

            off = NumberUtils.writeDouble(bytes, off, value, true, writeSpecialAsString);

            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, Double value, long features) {
            if (value == null) {
                return writeNumberNull(writer, bytes, off, features);
            }
            return writeValue(writer, bytes, off, value.doubleValue(), features);
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, double[] value, long features) {
            if (value == null) {
                return writeArrayNull(bytes, off, features);
            }

            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            boolean writeSpecialAsString = (features & WriteFloatSpecialAsString.mask) != 0;
            bytes[off++] = '[';
            for (int i = 0; i < value.length; i++) {
                if (i != 0) {
                    bytes[off++] = ',';
                }

                if (!Double.isFinite(value[i])) {
                    off = NumberUtils.writeDouble(bytes, off, value[i], true, writeSpecialAsString);
                    continue;
                }
                if (writeAsString) {
                    bytes[off++] = quote;
                }
                off = NumberUtils.writeDouble(bytes, off, value[i], true, false);
                if (writeAsString) {
                    bytes[off++] = quote;
                }
            }
            bytes[off] = ']';

            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, LocalDate value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }
            byte quote = (byte) writer.quote;
            bytes[off] = quote;
            off = IOUtils.writeLocalDate(bytes, off + 1, value.getYear(), value.getMonthValue(), value.getDayOfMonth());
            bytes[off] = quote;
            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, LocalTime value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }
            byte quote = (byte) writer.quote;
            bytes[off] = quote;
            off = IOUtils.writeLocalTime(bytes, off + 1, value);
            bytes[off] = quote;
            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, LocalDateTime value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }
            byte quote = (byte) writer.quote;
            bytes[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(bytes, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            bytes[off] = ' ';
            off = IOUtils.writeLocalTime(bytes, off + 1, value.toLocalTime());
            bytes[off] = quote;
            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, OffsetDateTime value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }
            byte quote = (byte) writer.quote;
            LocalDateTime ldt = value.toLocalDateTime();
            LocalDate date = ldt.toLocalDate();
            bytes[off] = quote;
            off = IOUtils.writeLocalDate(bytes, off + 1, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            bytes[off] = 'T';
            off = IOUtils.writeLocalTime(bytes, off + 1, ldt.toLocalTime());

            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                bytes[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getBytes(0, zoneId.length(), bytes, off);
                off += zoneId.length();
            }
            bytes[off] = quote;
            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, OffsetTime value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }
            byte quote = (byte) writer.quote;
            bytes[off] = quote;
            off = IOUtils.writeLocalTime(bytes, off + 1, value.toLocalTime());

            ZoneOffset offset = value.getOffset();
            if (offset.getTotalSeconds() == 0) {
                bytes[off++] = 'Z';
            } else {
                String zoneId = offset.getId();
                zoneId.getBytes(0, zoneId.length(), bytes, off);
                off += zoneId.length();
            }
            bytes[off] = quote;
            return off + 1;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, ZonedDateTime value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
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
            bytes[off] = quote;
            LocalDate localDate = value.toLocalDate();
            off = IOUtils.writeLocalDate(bytes, off + 1, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            bytes[off] = 'T';
            off = IOUtils.writeLocalTime(bytes, off + 1, value.toLocalTime());
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
            bytes[off] = quote;
            return off + 1;
        }

        static int writeNumberNull(JSONWriterUTF8 writer, byte[] bytes, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                if ((features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0) {
                    byte quote = (byte) writer.quote;
                    bytes[off++] = quote;
                    bytes[off++] = '0';
                    bytes[off] = quote;
                } else {
                    bytes[off] = '0';
                }
                return off + 1;
            } else {
                return writeNull(bytes, off);
            }
        }

        static int writeArrayNull(byte[] bytes, int off, long features) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_LIST_AS_EMPTY)) != 0) {
                return writeEmptyArray(bytes, off);
            } else {
                return writeNull(bytes, off);
            }
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, boolean value, long features) {
            byte quote = (byte) writer.quote;

            boolean writeAsString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0;
            if (writeAsString) {
                bytes[off++] = quote;
            }
            if ((features & MASK_WRITE_BOOLEAN_AS_NUMBER) != 0) {
                bytes[off++] = (byte) (value ? '1' : '0');
            } else {
                off = IOUtils.putBoolean(bytes, off, value);
            }
            if (writeAsString) {
                bytes[off++] = quote;
            }

            return off;
        }

        static int writeValue(JSONWriterUTF8 writer, byte[] bytes, int off, UUID value, long features) {
            if (value == null) {
                return writeNull(bytes, off);
            }

            byte quote = (byte) writer.quote;

            final long base = ARRAY_BYTE_BASE_OFFSET + off;
            UNSAFE.putByte(bytes, base, quote);
            UNSAFE.putByte(bytes, base + 9, (byte) '-');
            UNSAFE.putByte(bytes, base + 14, (byte) '-');
            UNSAFE.putByte(bytes, base + 19, (byte) '-');
            UNSAFE.putByte(bytes, base + 24, (byte) '-');
            UNSAFE.putByte(bytes, base + 37, quote);
            long msb = value.getMostSignificantBits();
            long lsb = value.getLeastSignificantBits();
            long x = msb, x0 = hex8(x >>> 32), x1 = hex8(x);
            UNSAFE.putLong(bytes, base + 1, x0);
            UNSAFE.putInt(bytes, base + 10, (int) x1);
            UNSAFE.putInt(bytes, base + 15, (int) (x1 >>> 32));
            x = lsb;
            x0 = hex8(x >>> 32);
            x1 = hex8(x);
            UNSAFE.putInt(bytes, base + 20, (int) (x0));
            UNSAFE.putInt(bytes, base + 25, (int) (x0 >>> 32));
            UNSAFE.putLong(bytes, base + 29, x1);

            return off + 38;
        }

        static int valueSize(boolean value) {
            return 5;
        }

        static int valueSize(byte value) {
            return 5;
        }

        static int valueSize(Byte value) {
            return 5;
        }

        static int valueSize(short value) {
            return 7;
        }

        static int valueSize(Short value) {
            return 7;
        }

        static int valueSize(int value) {
            return 13;
        }

        static int valueSize(Integer value) {
            return 13;
        }

        static int valueSize(long value) {
            return 23;
        }

        static int valueSize(Long value) {
            return 23;
        }

        static int valueSize(float value) {
            return 17;
        }

        static int valueSize(double value) {
            return 26;
        }

        static int valueSize(UUID value) {
            return 38;
        }

        static int valueSize(float[] value) {
            return value == null ? 4 : value.length * (17 /* float value size */ + 1);
        }

        static int valueSize(double[] value) {
            return value == null ? 4 : value.length * (26 /* double value size */ + 1);
        }

        static int valueSize(int[] value) {
            return value == null ? 4 : value.length * (13 /* int value size */ + 1);
        }

        static int valueSize(byte[] value) {
            return value == null ? 4 : value.length * (23 /* long value size */ + 1);
        }

        static int valueSize(long[] value) {
            return value == null ? 4 : value.length * (23 /* long value size */ + 1);
        }

        static int valueSize(LocalDate value) {
            return 18;
        }

        static int valueSize(LocalDateTime value) {
            return 38;
        }

        static int valueSize(LocalTime value) {
            return 20;
        }

        static int valueSize(OffsetDateTime value) {
            return 45;
        }

        static int valueSize(OffsetTime value) {
            return 28;
        }

        static int valueSize(ZonedDateTime value) {
            if (value == null) {
                return 4;
            }
            return value.getZone().getId().length() + 38;
        }

        static int valueSize(BigInteger value) {
            return IOUtils.stringSize(value);
        }

        static int valueSize(BigDecimal value) {
            return IOUtils.stringSize(value);
        }

        static int nameSize(JSONWriterUTF8 writer, byte[] name) {
            return name.length + 2 + writer.pretty * writer.level;
        }

        static byte[] ensureCapacity(JSONWriterUTF8 writer, byte[] bytes, int minCapacity) {
            bytes = Arrays.copyOf(bytes, writer.newCapacity(minCapacity, bytes.length));
            writer.bytes = bytes;
            return bytes;
        }

        static byte[] ensureCapacity(JSONWriterUTF8 writer, int minCapacity) {
            byte[] bytes = writer.bytes;
            if (minCapacity > bytes.length) {
                bytes = ensureCapacity(writer, bytes, minCapacity);
            }
            return bytes;
        }

        static int writeName(JSONWriterUTF8 writer, byte[] bytes, int off, byte[] name) {
            if (writer.startObject) {
                writer.startObject = false;
            } else {
                bytes[off++] = ',';
                if (writer.pretty != PRETTY_NON) {
                    off = writer.indent(bytes, off);
                }
            }
            System.arraycopy(name, 0, bytes, off, name.length);
            return off + name.length;
        }

        static int writeNull(byte[] bytes, int off) {
            IOUtils.putNULL(bytes, off);
            return off + 4;
        }

        static int writeEmptyArray(byte[] bytes, int off) {
            bytes[off] = '[';
            bytes[off + 1] = ']';
            return off + 2;
        }

        /**
         * Extract the least significant 4 bytes from the input integer i, convert each byte into its corresponding 2-digit
         * hexadecimal representation, concatenate these hexadecimal strings into one continuous string, and then interpret
         * this string as a hexadecimal number to form and return a long value.
         */
        static long hex8(long i) {
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
        static long expand(long i) {
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

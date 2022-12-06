package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.NotWriteDefaultValue;
import static com.alibaba.fastjson2.util.IOUtils.*;

class JSONWriterUTF16
        extends JSONWriter {
    static final char[] REF_PREF = "{\"$ref\":".toCharArray();

    protected char[] chars;
    private final int cachedIndex;

    JSONWriterUTF16(Context ctx) {
        super(ctx, null, false, StandardCharsets.UTF_16);

        cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);
        chars = JSONFactory.allocateCharArray(cachedIndex);
        if (chars == null) {
            chars = new char[1024];
        }
    }

    @Override
    public void flushTo(java.io.Writer to) {
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
    public void close() {
        JSONFactory.releaseCharArray(cachedIndex, chars);
    }

    @Override
    protected final void write0(char c) {
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = c;
    }

    @Override
    public void writeColon() {
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = ':';
    }

    @Override
    public void startObject() {
        level++;
        startObject = true;
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = '{';
    }

    @Override
    public void endObject() {
        level--;
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = '}';
        startObject = false;
    }

    @Override
    public void writeComma() {
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = ',';
    }

    @Override
    public void startArray() {
        level++;
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = '[';
    }

    @Override
    public void endArray() {
        level--;
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = ']';
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

        if (JDKUtils.JVM_VERSION > 8 && JDKUtils.UNSAFE_SUPPORT) {
            byte coder = UnsafeUtils.getStringCoder(str);
            if (coder == 0) {
                byte[] value = UnsafeUtils.getStringValue(str);
                int minCapacity = off + value.length + 2;
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

            chars[off++] = quote;
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = quote;
            return;
        }

        if (escapeNoneAscii || browserSecure) {
            ensureCapacity(off + strlen * 6 + 2);
        } else {
            ensureCapacity(off + strlen * 2 + 2);
        }

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

    @Override
    public void writeString(char[] str, int offset, int len, boolean quote) {
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        int minCapacity = quote ? this.off + 2 : this.off;
        if (escapeNoneAscii) {
            minCapacity += len * 6;
        } else {
            minCapacity += len * 2;
        }

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
    public void writeReference(String path) {
        this.lastReference = path;

        writeRaw(REF_PREF, 0, REF_PREF.length);
        writeString(path);
        if (off == chars.length) {
            int oldCapacity = chars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - maxArraySize > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            chars = Arrays.copyOf(chars, newCapacity);
        }
        chars[off++] = (byte) '}';
    }

    @Override
    public void writeBase64(byte[] bytes) {
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
    public void writeHex(byte[] bytes) {
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
    public void writeBigInt(BigInteger value, long features) {
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
    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            writeNull();
            return;
        }

        String str = value.toString();

        if ((context.features & Feature.BrowserCompatible.mask) != 0
                && (value.compareTo(LOW) < 0 || value.compareTo(HIGH) > 0)) {
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
    public void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        long msb = value.getMostSignificantBits();
        long lsb = value.getLeastSignificantBits();

        ensureCapacity(off + 38);
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
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
            }
        }
        System.arraycopy(chars, off, this.chars, this.off, charslen);
        this.off += charslen;
    }

    @Override
    public void writeChar(char ch) {
        int minCapacity = chars.length + 8;
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
    public void writeRaw(char ch) {
        if (off == chars.length) {
            int minCapacity = off + 1;
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
        chars[off++] = ch;
    }

    @Override
    public void writeRaw(char c0, char c1) {
        if (off + 1 >= chars.length) {
            int minCapacity = off + 2;
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
        chars[off++] = c0;
        chars[off++] = c1;
    }

    @Override
    public void writeNameRaw(char[] chars) {
        {
            // inline ensureCapacity
            int minCapacity = off + chars.length + (startObject ? 0 : 1);
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
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
    public void writeNameRaw(char[] chars, int off, int len) {
        {
            // inline ensureCapacity
            int minCapacity = this.off + len + (startObject ? 0 : 1);
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
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

    void ensureCapacity(int minCapacity) {
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

    @Override
    public void writeInt32(int i) {
        if ((context.features & Feature.WriteNonStringValueAsString.mask) != 0) {
            writeString(Integer.toString(i));
            return;
        }
        if (i == Integer.MIN_VALUE) {
            writeRaw("-2147483648");
            return;
        }

        int size;
        {
            int x = i < 0 ? -i : i;
            if (x <= 9) {
                size = 1;
            } else if (x <= 99) {
                size = 2;
            } else if (x <= 999) {
                size = 3;
            } else if (x <= 9999) {
                size = 4;
            } else if (x <= 99999) {
                size = 5;
            } else if (x <= 999999) {
                size = 6;
            } else if (x <= 9999999) {
                size = 7;
            } else if (x <= 99999999) {
                size = 8;
            } else if (x <= 999999999) {
                size = 9;
            } else {
                size = 10;
            }
            if (i < 0) {
                size++;
            }
        }

        {
            // inline ensureCapacity
            int minCapacity = off + size;
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
            }
        }

        // getChars(i, off + size, chars);
        {
            int index = off + size;

            int q, r, p = index;
            char sign = 0;

            if (i < 0) {
                sign = '-';
                i = -i;
            }

            while (i >= 65536) {
                q = i / 100;
                // really: r = i - (q * 100);
                r = i - ((q << 6) + (q << 5) + (q << 2));
                i = q;
                chars[--p] = (char) DigitOnes[r];
                chars[--p] = (char) DigitTens[r];
            }

            // Fall thru to fast mode for smaller numbers
            // assert(i <= 65536, i);
            for (; ; ) {
                q = (i * 52429) >>> (16 + 3);
                r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
                chars[--p] = (char) digits[r];
                i = q;
                if (i == 0) {
                    break;
                }
            }
            if (sign != 0) {
                chars[--p] = sign;
            }
        }
        off += size;
    }

    @Override
    public void writeInt64(long i) {
        boolean writeAsString = false;
        if ((context.features & (Feature.WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0) {
            writeAsString = true;
        } else if ((context.features & Feature.BrowserCompatible.mask) != 0
                && (i > 9007199254740991L || i < -9007199254740991L)) {
            writeAsString = true;
        }

        if (i == Long.MIN_VALUE) {
            writeRaw("-9223372036854775808");
            return;
        }

        int size;
        {
            long x = i < 0 ? -i : i;
            if (x <= 9) {
                size = 1;
            } else if (x <= 99L) {
                size = 2;
            } else if (x <= 999L) {
                size = 3;
            } else if (x <= 9999L) {
                size = 4;
            } else if (x <= 99999L) {
                size = 5;
            } else if (x <= 999999L) {
                size = 6;
            } else if (x <= 9999999L) {
                size = 7;
            } else if (x <= 99999999L) {
                size = 8;
            } else if (x <= 999999999L) {
                size = 9;
            } else if (x <= 9999999999L) {
                size = 10;
            } else if (x <= 99999999999L) {
                size = 11;
            } else if (x <= 999999999999L) {
                size = 12;
            } else if (x <= 9999999999999L) {
                size = 13;
            } else if (x <= 99999999999999L) {
                size = 14;
            } else if (x <= 999999999999999L) {
                size = 15;
            } else if (x <= 9999999999999999L) {
                size = 16;
            } else if (x <= 99999999999999999L) {
                size = 17;
            } else if (x <= 999999999999999999L) {
                size = 18;
            } else {
                size = 19;
            }
            if (i < 0) {
                size++;
            }
        }

        {
            // inline ensureCapacity
            int minCapacity = off + size;
            if (writeAsString) {
                minCapacity += 2;
            }
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - maxArraySize > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
            }
        }

        if (writeAsString) {
            chars[off++] = '"';
        }

        {
            int index = off + size;
            long q;
            int r;
            int charPos = index;
            char sign = 0;

            if (i < 0) {
                sign = '-';
                i = -i;
            }

            // Get 2 digits/iteration using longs until quotient fits into an int
            while (i > Integer.MAX_VALUE) {
                q = i / 100;
                // really: r = i - (q * 100);
                r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
                i = q;
                chars[--charPos] = (char) DigitOnes[r];
                chars[--charPos] = (char) DigitTens[r];
            }

            // Get 2 digits/iteration using ints
            int q2;
            int i2 = (int) i;
            while (i2 >= 65536) {
                q2 = i2 / 100;
                // really: r = i2 - (q * 100);
                r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
                i2 = q2;
                chars[--charPos] = (char) DigitOnes[r];
                chars[--charPos] = (char) DigitTens[r];
            }

            // Fall thru to fast mode for smaller numbers
            // assert(i2 <= 65536, i2);
            for (; ; ) {
                q2 = (i2 * 52429) >>> (16 + 3);
                r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
                chars[--charPos] = (char) digits[r];
                i2 = q2;
                if (i2 == 0) {
                    break;
                }
            }
            if (sign != 0) {
                chars[--charPos] = sign;
            }
        }
        off += size;
        if (writeAsString) {
            chars[off++] = '"';
        }
    }

    @Override
    public void writeFloat(float value) {
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
            chars[off++] = '"';
        }

        int len = RyuFloat.toString(value, chars, off);
        off += len;

        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }
    }

    @Override
    public void writeFloat(float[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        int minCapacity = off + values.length * 16 + 1;
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
        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            float value = values[i];
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                chars[off] = 'n';
                chars[off + 1] = 'u';
                chars[off + 2] = 'l';
                chars[off + 3] = 'l';
                off += 4;
            } else {
                int len = RyuFloat.toString(value, chars, off);
                off += len;
            }
        }
        chars[off++] = ']';
    }

    @Override
    public void writeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            writeNull();
            return;
        }

        boolean writeNonStringValueAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 24;
        if (writeNonStringValueAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);
        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }

        int len = RyuDouble.toString(value, chars, off);
        off += len;

        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }
    }

    @Override
    public void writeDoubleArray(double value0, double value1) {
        boolean writeNonStringValueAsString = (context.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        int minCapacity = off + 48 + 3;
        if (writeNonStringValueAsString) {
            minCapacity += 2;
        }

        ensureCapacity(minCapacity);

        chars[off++] = '[';

        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }
        int len0 = RyuDouble.toString(value0, chars, off);
        off += len0;
        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }

        chars[off++] = ',';

        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }
        int len1 = RyuDouble.toString(value1, chars, off);
        off += len1;
        if (writeNonStringValueAsString) {
            chars[off++] = '"';
        }

        chars[off++] = ']';
    }

    @Override
    public void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        int minCapacity = off + values.length * 25 + 1;
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
        chars[off++] = '[';
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }

            double value = values[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                chars[off] = 'n';
                chars[off + 1] = 'u';
                chars[off + 2] = 'l';
                chars[off + 3] = 'l';
                off += 4;
            } else {
                int len = RyuDouble.toString(value, chars, off);
                off += len;
            }
        }
        chars[off++] = ']';
    }

    @Override
    public void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        ensureCapacity(off + 16);

        chars[off++] = quote;

        chars[off++] = (char) (year / 1000 + '0');
        chars[off++] = (char) ((year / 100) % 10 + '0');
        chars[off++] = (char) ((year / 10) % 10 + '0');
        chars[off++] = (char) (year % 10 + '0');
        chars[off++] = (char) (month / 10 + '0');
        chars[off++] = (char) (month % 10 + '0');
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
    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        ensureCapacity(off + 21);

        chars[off++] = quote;

        chars[off++] = (char) (year / 1000 + '0');
        chars[off++] = (char) ((year / 100) % 10 + '0');
        chars[off++] = (char) ((year / 10) % 10 + '0');
        chars[off++] = (char) (year % 10 + '0');
        chars[off++] = '-';
        chars[off++] = (char) (month / 10 + '0');
        chars[off++] = (char) (month % 10 + '0');
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
    public void writeLocalDate(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int dayOfMonth = date.getDayOfMonth();

        int yearSize = IOUtils.stringSize(year);
        int len = 8 + yearSize;
        ensureCapacity(off + len);
        chars[off] = quote;
        Arrays.fill(chars, off + 1, off + len - 1, '0');
        IOUtils.getChars(year, off + yearSize + 1, chars);
        chars[off + yearSize + 1] = '-';
        IOUtils.getChars(month, off + yearSize + 4, chars);
        chars[off + yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, off + yearSize + 7, chars);
        chars[off + len - 1] = quote;
        off += len;
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        int nano = dateTime.getNano();

        int yearSize = IOUtils.stringSize(year);
        int len = 17 + yearSize;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        ensureCapacity(off + len);
        chars[off] = quote;
        Arrays.fill(chars, off + 1, off + len - 1, '0');
        IOUtils.getChars(year, off + yearSize + 1, chars);
        chars[off + yearSize + 1] = '-';
        IOUtils.getChars(month, off + yearSize + 4, chars);
        chars[off + yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, off + yearSize + 7, chars);
        chars[off + yearSize + 7] = ' ';
        IOUtils.getChars(hour, off + yearSize + 10, chars);
        chars[off + yearSize + 10] = ':';
        IOUtils.getChars(minute, off + yearSize + 13, chars);
        chars[off + yearSize + 13] = ':';
        IOUtils.getChars(second, off + yearSize + 16, chars);
        if (small != 0) {
            chars[off + yearSize + 16] = '.';
            IOUtils.getChars(small, off + len - 1, chars);
        }
        chars[off + len - 1] = quote;
        off += len;
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
        int millislen;
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

        chars[off + 0] = quote;
        chars[off + 1] = (char) (year / 1000 + '0');
        chars[off + 2] = (char) ((year / 100) % 10 + '0');
        chars[off + 3] = (char) ((year / 10) % 10 + '0');
        chars[off + 4] = (char) (year % 10 + '0');
        chars[off + 5] = '-';
        chars[off + 6] = (char) (month / 10 + '0');
        chars[off + 7] = (char) (month % 10 + '0');
        chars[off + 8] = '-';
        chars[off + 9] = (char) (dayOfMonth / 10 + '0');
        chars[off + 10] = (char) (dayOfMonth % 10 + '0');
        chars[off + 11] = timeZone ? 'T' : ' ';
        chars[off + 12] = (char) (hour / 10 + '0');
        chars[off + 13] = (char) (hour % 10 + '0');
        chars[off + 14] = ':';
        chars[off + 15] = (char) (minute / 10 + '0');
        chars[off + 16] = (char) (minute % 10 + '0');
        chars[off + 17] = ':';
        chars[off + 18] = (char) (second / 10 + '0');
        chars[off + 19] = (char) (second % 10 + '0');
        if (millislen > 0) {
            chars[off + 20] = '.';
            Arrays.fill(chars, off + 21, off + 20 + millislen, '0');
            if (millis < 10) {
                IOUtils.getChars(millis, off + 20 + millislen, chars);
            } else {
                if (millis % 100 == 0) {
                    IOUtils.getChars(millis / 100, off + 20 + millislen, chars);
                } else if (millis % 10 == 0) {
                    IOUtils.getChars(millis / 10, off + 20 + millislen, chars);
                } else {
                    IOUtils.getChars(millis, off + 20 + millislen, chars);
                }
            }
        }
        if (timeZone) {
            if (offsetSeconds == 0) {
                chars[off + 20 + millislen] = 'Z';
            } else {
                int offsetAbs = Math.abs(offset);

                if (offset >= 0) {
                    chars[off + 20 + millislen] = '+';
                } else {
                    chars[off + 20 + millislen] = '-';
                }
                chars[off + 20 + millislen + 1] = '0';
                IOUtils.getChars(offsetAbs, off + 20 + millislen + 3, chars);
                chars[off + 20 + millislen + 3] = ':';
                chars[off + 20 + millislen + 4] = '0';
                int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
                if (offsetMinutes < 0) {
                    offsetMinutes = -offsetMinutes;
                }
                IOUtils.getChars(offsetMinutes, off + 20 + millislen + zonelen, chars);
            }
        }
        chars[off + len - 1] = quote;
        off += len;
    }

    @Override
    public void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        ensureCapacity(off + 10);

        chars[off] = quote;
        chars[off + 1] = (char) (year / 1000 + '0');
        chars[off + 2] = (char) ((year / 100) % 10 + '0');
        chars[off + 3] = (char) ((year / 10) % 10 + '0');
        chars[off + 4] = (char) (year % 10 + '0');
        chars[off + 5] = (char) (month / 10 + '0');
        chars[off + 6] = (char) (month % 10 + '0');
        chars[off + 7] = (char) (dayOfMonth / 10 + '0');
        chars[off + 8] = (char) (dayOfMonth % 10 + '0');
        chars[off + 9] = quote;
        off += 10;
    }

    @Override
    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        ensureCapacity(off + 12);

        chars[off] = quote;
        chars[off + 1] = (char) (year / 1000 + '0');
        chars[off + 2] = (char) ((year / 100) % 10 + '0');
        chars[off + 3] = (char) ((year / 10) % 10 + '0');
        chars[off + 4] = (char) (year % 10 + '0');
        chars[off + 5] = '-';
        chars[off + 6] = (char) (month / 10 + '0');
        chars[off + 7] = (char) (month % 10 + '0');
        chars[off + 8] = '-';
        chars[off + 9] = (char) (dayOfMonth / 10 + '0');
        chars[off + 10] = (char) (dayOfMonth % 10 + '0');
        chars[off + 11] = quote;
        off += 12;
    }

    @Override
    public void writeTimeHHMMSS8(int hour, int minute, int second) {
        ensureCapacity(off + 10);

        chars[off] = quote;
        chars[off + 1] = (char) (hour / 10 + '0');
        chars[off + 2] = (char) (hour % 10 + '0');
        chars[off + 3] = ':';
        chars[off + 4] = (char) (minute / 10 + '0');
        chars[off + 5] = (char) (minute % 10 + '0');
        chars[off + 6] = ':';
        chars[off + 7] = (char) (second / 10 + '0');
        chars[off + 8] = (char) (second % 10 + '0');
        chars[off + 9] = quote;

        off += 10;
    }

    @Override
    public void writeLocalTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        int nano = time.getNano();

        int len = 10;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        ensureCapacity(off + len);
        chars[off] = quote;
        Arrays.fill(chars, off + 1, off + len - 1, '0');
        IOUtils.getChars(hour, off + 3, chars);
        chars[off + 3] = ':';
        IOUtils.getChars(minute, off + 6, chars);
        chars[off + 6] = ':';
        IOUtils.getChars(second, off + 9, chars);
        if (small != 0) {
            chars[off + 9] = '.';
            IOUtils.getChars(small, off + len - 1, chars);
        }
        chars[off + len - 1] = quote;

        off += len;
    }

    @Override
    public void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        int nano = dateTime.getNano();
        String zoneId = dateTime.getZone().getId();

        int len = 17;

        char firstZoneChar = '\0';
        int zoneSize;
        if ("UTC".equals(zoneId)) {
            zoneId = "Z";
            zoneSize = 1;
        } else if (zoneId.length() != 0 && ((firstZoneChar = zoneId.charAt(0)) == '+' || firstZoneChar == '-')) {
            zoneSize = zoneId.length();
        } else {
            zoneSize = 2 + zoneId.length();
        }
        len += zoneSize;

        int yearSize = IOUtils.stringSize(year);
        len += yearSize;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        ensureCapacity(off + len);
        chars[off] = quote;
        Arrays.fill(chars, off + 1, off + len - 1, '0');
        IOUtils.getChars(year, off + yearSize + 1, chars);
        chars[off + yearSize + 1] = '-';
        IOUtils.getChars(month, off + yearSize + 4, chars);
        chars[off + yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, off + yearSize + 7, chars);
        chars[off + yearSize + 7] = 'T';
        IOUtils.getChars(hour, off + yearSize + 10, chars);
        chars[off + yearSize + 10] = ':';
        IOUtils.getChars(minute, off + yearSize + 13, chars);
        chars[off + yearSize + 13] = ':';
        IOUtils.getChars(second, off + yearSize + 16, chars);
        if (small != 0) {
            chars[off + yearSize + 16] = '.';
            IOUtils.getChars(small, off + len - 1 - zoneSize, chars);
        }
        if (zoneSize == 1) {
            chars[off + len - 2] = 'Z';
        } else if (firstZoneChar == '+' || firstZoneChar == '-') {
            zoneId.getChars(0, zoneId.length(), chars, off + len - zoneSize - 1);
        } else {
            chars[off + len - zoneSize - 1] = '[';
            zoneId.getChars(0, zoneId.length(), chars, off + len - zoneSize);
            chars[off + len - 2] = ']';
        }
        chars[off + len - 1] = quote;
        off += len;
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

    static void formatUnsignedLong0(long val, char[] buf, int offset, int len) { // for uuid
        int charPos = offset + len;
        do {
            buf[--charPos] = DIGITS[((int) val) & 15];
            val >>>= 4;
        } while (charPos > offset);
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
        throw new JSONException("UnsupportedOperation");
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
            int minCapacity = off + 1;
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
        chars[off++] = '{';

        boolean first = true;
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> next = it.next();
            Object value = next.getValue();
            if (value == null && (context.features & Feature.WriteMapNullValue.mask) == 0) {
                continue;
            }

            if (!first) {
                if (off == chars.length) {
                    int minCapacity = off + 1;
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
                chars[off++] = ',';
            }

            first = false;
            writeString(next.getKey());

            if (off == chars.length) {
                int minCapacity = off + 1;
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
                writeDecimal((BigDecimal) value);
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
            int minCapacity = off + 1;
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
            int minCapacity = off + 1;
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
        chars[off++] = '[';

        boolean first = true;
        for (int i = 0, size = array.size(); i < size; i++) {
            if (!first) {
                if (off == chars.length) {
                    int minCapacity = off + 1;
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
                writeDecimal((BigDecimal) value);
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
            int minCapacity = off + 1;
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
        chars[off++] = ']';
    }
}

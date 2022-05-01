package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.RyuDouble;
import com.alibaba.fastjson2.util.RyuFloat;

import static com.alibaba.fastjson2.util.IOUtils.DigitOnes;
import static com.alibaba.fastjson2.util.IOUtils.DigitTens;
import static com.alibaba.fastjson2.util.IOUtils.digits;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONFactory.*;

class JSONWriterUTF16 extends JSONWriter {
    protected char[] chars;

    JSONWriterUTF16(Context ctx) {
        super(ctx, StandardCharsets.UTF_16);

        chars = JSONFactory.CHARS_UPDATER.getAndSet(JSONFactory.CACHE, null);
        if (chars == null) {
            chars = new char[1024];
        }
    }

    @Override
    public void close() {
        if (chars.length > CACHE_THREAD) {
            return;
        }
        JSONFactory.CHARS_UPDATER.set(JSONFactory.CACHE, chars);
    }

    @Override
    protected void write0(char c) {
        if (off == chars.length) {
            int minCapacity = off + 1;
            int oldCapacity = chars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            chars = Arrays.copyOf(chars, newCapacity);
        }
        chars[off++] = c;
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (isEnabled(Feature.NullAsDefaultValue.mask)) {
                writeString("");
                return;
            }

            writeNull();
            return;
        }

        final int strlen = str.length();

        boolean special = false;
        {
            int i = 0;
            // vector optimize
            while (i + 4 <= strlen) {
                char c0 = str.charAt(i);
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                char c3 = str.charAt(i + 3);
                if (c0 == '"' || c1 == '"' || c2 == '"' || c3 == '"') {
                    special = true;
                    break;
                }
                if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\') {
                    special = true;
                    break;
                }
                if (c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ') {
                    special = true;
                    break;
                }
                i += 4;
            }
            if (!special && i + 2 <= strlen) {
                char c0 = str.charAt(i);
                char c1 = str.charAt(i + 1);
                if (c0 == '"' || c1 == '"' || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' ') {
                    special = true;
                } else {
                    i += 2;
                }
            }
            if (!special && i + 1 == strlen) {
                char c0 = str.charAt(i);
                special = c0 == '"' || c0 == '\\' || c0 < ' ';
            }
        }


        if (!special) {
            // inline ensureCapacity(off + strlen + 2);
            int minCapacity = off + strlen + 2;
            if (minCapacity - chars.length > 0) {
                int oldCapacity = chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                chars = Arrays.copyOf(chars, newCapacity);
            }

            chars[off++] = '"';
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = '"';
            return;
        }

        ensureCapacity(off + strlen * 2 + 2);
        chars[off++] = '"';
        for (int i = 0; i < strlen; ++i) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"':
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
                default:
                    chars[off++] = ch;
                    break;
            }
        }
        chars[off++] = '"';
    }

    @Override
    public void writeReference(String path) {
        this.lastReference = path;

        startObject();
        writeName("$ref");
        writeColon();
        writeString(path);
        endObject();
    }

    @Override
    public void writeBase64(byte[] bytes) {
        int charsLen = ((bytes.length - 1) / 3 + 1) << 2; // base64 character count

        ensureCapacity(off + charsLen + 2);
        chars[off++] = '"';

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

        chars[off++] = '"';
    }

    @Override
    public void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNull();
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
        int utflen = 0;
        final int strlen = str.length();
        for (int i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        ensureCapacity(off + utflen);
        str.getChars(0, strlen, chars, off);
        off += utflen;
    }

    @Override
    public void writeRaw(char[] chars) {
        {
            // inline ensureCapacity
            int minCapacity = off + chars.length;
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
            }
        }
        System.arraycopy(chars, 0, this.chars, this.off, chars.length);
        off += chars.length;
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
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
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
        if ((context.features & Feature.WriteNonStringValueAsString.mask) != 0
                || ((context.features & Feature.BrowserCompatible.mask) != 0
                && (i > 9007199254740991L || i < -9007199254740991L))) {
            String str = Long.toString(i);
            writeString(str);
            return;
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
            if (minCapacity - this.chars.length > 0) {
                int oldCapacity = this.chars.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.chars = Arrays.copyOf(this.chars, newCapacity);
            }
        }

//        getChars(i, off + size, chars);
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
    }

    static void getChars(long i, int index, char[] chars) {
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

    @Override
    public void writeFloat(float value) {
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
    public void writeFloat(float[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        int minCapacity = value.length * 16 + 1;
        if (minCapacity - chars.length > 0) {
            int oldCapacity = chars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            chars = Arrays.copyOf(chars, newCapacity);
        }
        chars[off++] = '[';
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            int len = RyuFloat.toString(value[i], chars, off);
            off += len;
        }
        chars[off++] = ']';
    }

    @Override
    public void writeDouble(double value) {
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
    public void writeDouble(double[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        int minCapacity = value.length * 25 + 1;
        if (minCapacity - chars.length > 0) {
            int oldCapacity = chars.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            chars = Arrays.copyOf(chars, newCapacity);
        }
        chars[off++] = '[';
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                chars[off++] = ',';
            }
            int len = RyuDouble.toString(value[i], chars, off);
            off += len;
        }
        chars[off++] = ']';
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

        chars[off++] = '"';

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

        chars[off++] = '"';
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
}

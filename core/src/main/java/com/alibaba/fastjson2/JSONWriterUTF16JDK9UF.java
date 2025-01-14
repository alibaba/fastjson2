package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import sun.misc.Unsafe;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteBooleanAsNumber;
import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONWriterUTF16JDK9UF
        extends JSONWriterUTF16 {
    JSONWriterUTF16JDK9UF(Context ctx) {
        super(ctx);
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

        final int coder = STRING_CODER.applyAsInt(str);
        final byte[] value = STRING_VALUE.apply(str);

        int off = this.off;
        final char[] chars = this.chars;
        chars[off++] = quote;

        for (int i = 0; i < strlen; ++i) {
            int c;
            if (coder == 0) {
                c = value[i];
            } else {
                c = UNSAFE.getChar(str, (long) Unsafe.ARRAY_CHAR_BASE_OFFSET + i * 2);
            }
            if (c == '\\'
                    || c == quote
                    || c < ' '
                    || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
                    || (escapeNoneAscii && c > 0x007F)
            ) {
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

        writeStringEscape(str);
    }

    public void writeBool(boolean value) {
        int minCapacity = off + 5;
        if (minCapacity >= this.chars.length) {
            ensureCapacity(minCapacity);
        }

        char[] chars = this.chars;
        int off = this.off;
        if ((context.features & WriteBooleanAsNumber.mask) != 0) {
            chars[off++] = value ? '1' : '0';
        } else {
            if (value) {
                IOUtils.putTrue(chars, off);
                off += 4;
            } else {
                IOUtils.putFalse(chars, off);
                off += 5;
            }
        }
        this.off = off;
    }
}

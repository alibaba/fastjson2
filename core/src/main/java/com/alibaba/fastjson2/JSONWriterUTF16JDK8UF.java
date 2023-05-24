package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriter.Feature.EscapeNoneAscii;

public final class JSONWriterUTF16JDK8UF
        extends JSONWriterUTF16 {
    JSONWriterUTF16JDK8UF(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        char[] value = (char[]) UnsafeUtils.UNSAFE.getObject(str, JDKUtils.FIELD_STRING_VALUE_OFFSET);
        final int strlen = value.length;

        boolean escape = false;
        for (int i = 0; i < strlen; i++) {
            char c0 = value[i];
            if (c0 == quote || c0 == '\\' || c0 < ' '
                    || (browserSecure && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'))
                    || (escapeNoneAscii && c0 > 0x007F)
            ) {
                escape = true;
                break;
            }
        }

        if (!escape) {
            int off = this.off;
            // inline ensureCapacity(off + strlen + 2);
            int minCapacity = off + strlen + 2;
            if (minCapacity >= chars.length) {
                ensureCapacity(minCapacity);
            }

            final char[] chars = this.chars;
            chars[off++] = quote;
            System.arraycopy(value, 0, chars, off, value.length);
            off += strlen;
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscape(str);
    }
}

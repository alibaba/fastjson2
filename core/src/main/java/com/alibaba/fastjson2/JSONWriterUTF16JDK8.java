package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriter.Feature.EscapeNoneAscii;

final class JSONWriterUTF16JDK8
        extends JSONWriterUTF16 {
    JSONWriterUTF16JDK8(Context ctx) {
        super(ctx);
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

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & EscapeNoneAscii.mask) != 0;
        char[] value = JDKUtils.getCharArray(str);
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
            // inline ensureCapacity(off + strlen + 2);
            int minCapacity = off + strlen + 2;
            if (minCapacity >= chars.length) {
                ensureCapacity(minCapacity);
            }

            chars[off++] = quote;
            System.arraycopy(value, 0, chars, off, value.length);
            off += strlen;
            chars[off++] = quote;
            return;
        }

        writeStringEscape(str);
    }
}

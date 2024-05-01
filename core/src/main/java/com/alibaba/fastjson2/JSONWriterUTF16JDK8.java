package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

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
        for (int i = 0; i < value.length; i++) {
            char ch = value[i];
            if (ch == quote || ch == '\\' || ch < ' '
                    || (browserSecure && (ch == '<' || ch == '>' || ch == '(' || ch == ')'))
                    || (escapeNoneAscii && ch > 0x007F)
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
            if (!value) {
                chars[off] = 'f';
                chars[off + 1] = 'a';
                chars[off + 2] = 'l';
                chars[off + 3] = 's';
                chars[off + 4] = 'e';
                off += 5;
            } else {
                chars[off] = 't';
                chars[off + 1] = 'r';
                chars[off + 2] = 'u';
                chars[off + 3] = 'e';
                off += 4;
            }
        }
        this.off = off;
    }
}

package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;

import java.util.Arrays;

final class JSONWriterUTF16JDK8 extends JSONWriterUTF16 {
    JSONWriterUTF16JDK8(Context ctx) {
        super(ctx);
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

        char[] value = JDKUtils.getCharArray(str);

        final int strlen = value.length;

        boolean special = false;
        {
            int i = 0;
            // vector optimize
            while (i + 4 <= strlen) {
                char c0 = value[i];
                char c1 = value[i + 1];
                char c2 = value[i + 2];
                char c3 = value[i + 3];
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
                char c0 = value[i];
                char c1 = value[i + 1];
                if (c0 == '"' || c1 == '"' || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' ') {
                    special = true;
                } else {
                    i += 2;
                }
            }
            if (!special && i + 1 == strlen) {
                char c0 = value[i];
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
            System.arraycopy(value, 0, chars, off, value.length);
            off += strlen;
            chars[off++] = '"';
            return;
        }

        ensureCapacity(off + strlen * 2 + 2);
        chars[off++] = '"';
        for (int i = 0; i < strlen; ++i) {
            char ch = value[i];
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
}

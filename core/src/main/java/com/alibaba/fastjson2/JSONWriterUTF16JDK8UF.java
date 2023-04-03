package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.util.Arrays;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriter.Feature.EscapeNoneAscii;

public class JSONWriterUTF16JDK8UF
        extends JSONWriterUTF16 {
    JSONWriterUTF16JDK8UF(Context ctx) {
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
        char[] value = (char[]) UnsafeUtils.UNSAFE.getObject(str, JDKUtils.FIELD_STRING_VALUE_OFFSET);
        final int strlen = value.length;

        boolean escape = false;
        {
            int i = 0;
            // vector optimize 8
            while (i + 8 <= strlen) {
                char c0 = value[i];
                char c1 = value[i + 1];
                char c2 = value[i + 2];
                char c3 = value[i + 3];
                char c4 = value[i + 4];
                char c5 = value[i + 5];
                char c6 = value[i + 6];
                char c7 = value[i + 7];

                if (c0 == quote || c1 == quote || c2 == quote || c3 == quote || c4 == quote || c5 == quote || c6 == quote || c7 == quote) {
                    escape = true;
                    break;
                }

                if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\' || c4 == '\\' || c5 == '\\' || c6 == '\\' || c7 == '\\') {
                    escape = true;
                    break;
                }

                if (c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ' || c4 < ' ' || c5 < ' ' || c6 < ' ' || c7 < ' ') {
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

            // vector optimize 4
            if (!escape) {
                while (i + 4 <= strlen) {
                    char c0 = value[i];
                    char c1 = value[i + 1];
                    char c2 = value[i + 2];
                    char c3 = value[i + 3];
                    if (c0 == quote || c1 == quote || c2 == quote || c3 == quote) {
                        escape = true;
                        break;
                    }
                    if (c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\') {
                        escape = true;
                        break;
                    }
                    if (c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' ') {
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
                char c0 = value[i];
                char c1 = value[i + 1];
                if (c0 == quote || c1 == quote || c0 == '\\' || c1 == '\\' || c0 < ' ' || c1 < ' ') {
                    escape = true;
                } else if (escapeNoneAscii && (c0 > 0x007F || c1 > 0x007F)) {
                    escape = true;
                } else if (browserSecure
                        && (c0 == '<' || c1 == '<'
                        || c0 == '>' || c1 == '>'
                        || c0 == '(' || c1 == '(')
                        || c0 == ')' || c1 == ')') {
                    escape = true;
                } else {
                    i += 2;
                }
            }

            if (!escape && i + 1 == strlen) {
                char c0 = value[i];
                escape = c0 == quote
                        || c0 == '\\'
                        || c0 < ' '
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
            System.arraycopy(value, 0, chars, off, value.length);
            off += strlen;
            chars[off++] = quote;
            return;
        }

        writeStringEscape(str);
    }
}

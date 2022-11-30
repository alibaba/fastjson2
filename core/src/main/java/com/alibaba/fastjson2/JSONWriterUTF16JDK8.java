package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;

import java.util.Arrays;

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

        if (escapeNoneAscii) {
            ensureCapacity(off + strlen * 6 + 2);
        } else {
            ensureCapacity(off + strlen * 2 + 2);
        }
        chars[off++] = quote;
        for (int i = 0; i < strlen; ++i) {
            char ch = value[i];
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
}

package com.alibaba.fastjson2;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.Vector;

import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CODER;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_VALUE;

final class JSONWriterUTF16Vector
        extends JSONWriterUTF16 {
    static final Vector<Byte> V_BYTE_64_SPACE = ByteVector.SPECIES_64.broadcast(' ');
    static final Vector<Byte> V_BYTE_64_SLASH = ByteVector.SPECIES_64.broadcast('\\');
    static final Vector<Byte> V_BYTE_64_DOUBLE_QUOTE = ByteVector.SPECIES_64.broadcast('"');
    static final Vector<Byte> V_BYTE_64_SINGLE_QUOTE = ByteVector.SPECIES_64.broadcast('\'');
    static final Vector<Byte> V_BYTE_64_LT = ByteVector.SPECIES_64.broadcast('<');
    static final Vector<Byte> V_BYTE_64_GT = ByteVector.SPECIES_64.broadcast('>');
    static final Vector<Byte> V_BYTE_64_LB = ByteVector.SPECIES_64.broadcast('(');
    static final Vector<Byte> V_BYTE_64_RB = ByteVector.SPECIES_64.broadcast(')');

    static final Vector<Short> V_SHORT_128_SLASH = ShortVector.SPECIES_128.broadcast('\\');
    static final Vector<Short> V_SHORT_128_DOUBLE_QUOTE = ShortVector.SPECIES_128.broadcast('"');
    static final Vector<Short> V_SHORT_128_SINGLE_QUOTE = ShortVector.SPECIES_128.broadcast('\'');

    static final Vector<Short> V_SHORT_64_SPACE = ShortVector.SPECIES_64.broadcast(' ');
    static final Vector<Short> V_SHORT_64_SLASH = ShortVector.SPECIES_64.broadcast('\\');
    static final Vector<Short> V_SHORT_64_DOUBLE_QUOTE = ShortVector.SPECIES_64.broadcast('"');
    static final Vector<Short> V_SHORT_64_SINGLE_QUOTE = ShortVector.SPECIES_64.broadcast('\'');
    static final Vector<Short> V_SHORT_64_LT = ShortVector.SPECIES_64.broadcast('<');
    static final Vector<Short> V_SHORT_64_GT = ShortVector.SPECIES_64.broadcast('>');
    static final Vector<Short> V_SHORT_64_LB = ShortVector.SPECIES_64.broadcast('(');
    static final Vector<Short> V_SHORT_64_RB = ShortVector.SPECIES_64.broadcast(')');

    JSONWriterUTF16Vector(Context ctx) {
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

        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;

        if (STRING_VALUE != null) {
            int coder = STRING_CODER.applyAsInt(str);
            if (coder == 0) {
                byte[] value = STRING_VALUE.apply(str);
                int minCapacity = off + value.length + 2;
                if (minCapacity - chars.length > 0) {
                    ensureCapacity(minCapacity);
                }

                final int mark = off;
                chars[off++] = quote;

                int i = 0;
                for (; i + 8 <= value.length; i += 8) {
                    ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(value, i);

                    if (v.eq(V_BYTE_64_DOUBLE_QUOTE)
                            .or(v.eq(V_BYTE_64_SLASH))
                            .or(v.lt(V_BYTE_64_SPACE))
                            .anyTrue()
                    ) {
                        escape = true;
                        break;
                    }

                    if (browserSecure
                            && v.eq(V_BYTE_64_LT)
                            .or(v.eq(V_BYTE_64_GT))
                            .or(v.eq(V_BYTE_64_LB))
                            .or(v.eq(V_BYTE_64_RB))
                            .anyTrue()
                    ) {
                        escape = true;
                        break;
                    }

                    ShortVector sv = (ShortVector) v.castShape(ShortVector.SPECIES_128, 0);
                    sv.intoCharArray(chars, off);
                    off += 8;
                }

                if (!escape) {
                    for (; i + 4 <= value.length; i += 4) {
                        byte c0 = value[i];
                        byte c1 = value[i + 1];
                        byte c2 = value[i + 2];
                        byte c3 = value[i + 3];

                        if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                                || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                                || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                                || (browserSecure
                                && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                                || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                                || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                                || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
                        ) {
                            escape = true;
                            break;
                        }
                        chars[off] = (char) c0;
                        chars[off + 1] = (char) c1;
                        chars[off + 2] = (char) c2;
                        chars[off + 3] = (char) c3;
                        off += 4;
                    }
                }

                if (!escape) {
                    for (; i < value.length; i++) {
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
                ensureCapacity(minCapacity);
            }

            chars[off++] = quote;
            str.getChars(0, strlen, chars, off);
            off += strlen;
            chars[off++] = quote;
            return;
        }

        writeStringEscape(str);
    }

    @Override
    protected void writeStringLatin1(byte[] value) {
        if (value == null) {
            if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                writeString("");
                return;
            }

            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;

        int minCapacity = off + value.length + 2;
        if (minCapacity - chars.length > 0) {
            ensureCapacity(minCapacity);
        }

        final int mark = off;
        chars[off++] = quote;

        int i = 0;
        for (; i + 8 <= value.length; i += 8) {
            ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(value, i);

            if (v.eq(V_BYTE_64_DOUBLE_QUOTE)
                    .or(v.eq(V_BYTE_64_SLASH))
                    .or(v.lt(V_BYTE_64_SPACE))
                    .anyTrue()
            ) {
                escape = true;
                break;
            }

            if (browserSecure
                    && v.eq(V_BYTE_64_LT)
                    .or(v.eq(V_BYTE_64_GT))
                    .or(v.eq(V_BYTE_64_LB))
                    .or(v.eq(V_BYTE_64_RB))
                    .anyTrue()
            ) {
                escape = true;
                break;
            }

            ShortVector sv = (ShortVector) v.castShape(ShortVector.SPECIES_128, 0);
            sv.intoCharArray(chars, off);
            off += 8;
        }

        if (!escape) {
            for (; i + 4 <= value.length; i += 4) {
                byte c0 = value[i];
                byte c1 = value[i + 1];
                byte c2 = value[i + 2];
                byte c3 = value[i + 3];

                if (c0 == quote || c1 == quote || c2 == quote || c3 == quote
                        || c0 == '\\' || c1 == '\\' || c2 == '\\' || c3 == '\\'
                        || c0 < ' ' || c1 < ' ' || c2 < ' ' || c3 < ' '
                        || (browserSecure
                        && (c0 == '<' || c0 == '>' || c0 == '(' || c0 == ')'
                        || c1 == '<' || c1 == '>' || c1 == '(' || c1 == ')'
                        || c2 == '<' || c2 == '>' || c2 == '(' || c2 == ')'
                        || c3 == '<' || c3 == '>' || c3 == '(' || c3 == ')'))
                ) {
                    escape = true;
                    break;
                }
                chars[off] = (char) c0;
                chars[off + 1] = (char) c1;
                chars[off + 2] = (char) c2;
                chars[off + 3] = (char) c3;
                off += 4;
            }
        }

        if (!escape) {
            for (; i < value.length; i++) {
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
        }

        if (!escape) {
            chars[off++] = quote;
            return;
        }

        off = mark;
        writeStringEscape(value);
    }

    public static class Factory
            implements Function {
        @Override
        public Object apply(Object context) {
            return new JSONWriterUTF16Vector((Context) context);
        }
    }
}

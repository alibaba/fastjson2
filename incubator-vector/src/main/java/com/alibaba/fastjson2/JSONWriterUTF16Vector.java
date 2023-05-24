package com.alibaba.fastjson2;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.Vector;
import sun.misc.Unsafe;

import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CODER;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_VALUE;
import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

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
    static final Vector<Short> V_SHORT_128_SPACE = ShortVector.SPECIES_128.broadcast(' ');
    static final Vector<Short> V_SHORT_128_LT = ShortVector.SPECIES_128.broadcast('<');
    static final Vector<Short> V_SHORT_128_GT = ShortVector.SPECIES_128.broadcast('>');
    static final Vector<Short> V_SHORT_128_LB = ShortVector.SPECIES_128.broadcast('(');
    static final Vector<Short> V_SHORT_128_RB = ShortVector.SPECIES_128.broadcast(')');
    static final Vector<Short> V_SHORT_128_7F = ShortVector.SPECIES_128.broadcast(0x7F);

    JSONWriterUTF16Vector(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        int coder = STRING_CODER.applyAsInt(str);
        byte[] value = STRING_VALUE.apply(str);
        if (coder == 0) {
            writeStringLatin1(value);
        } else {
            writeStringUTF16(value);
        }
    }

    @Override
    public void writeStringLatin1(byte[] value) {
        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;

        int off = this.off;
        final int start = off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;

        int i = 0;
        final int upperBound = (value.length - i) & ~7;
        for (; i < upperBound; i += 8) {
            ByteVector v = (ByteVector) ByteVector.SPECIES_64.fromArray(value, i);

            if (v.eq(V_BYTE_64_DOUBLE_QUOTE)
                    .or(v.eq(V_BYTE_64_SLASH))
                    .or(v.lt(V_BYTE_64_SPACE))
                    .anyTrue()
                    || (browserSecure
                    && v.eq(V_BYTE_64_LT)
                    .or(v.eq(V_BYTE_64_GT))
                    .or(v.eq(V_BYTE_64_LB))
                    .or(v.eq(V_BYTE_64_RB))
                    .anyTrue())
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
                if (c == '\\'
                        || c == quote
                        || c < ' '
                        || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
                ) {
                    escape = true;
                    break;
                }
                chars[off++] = (char) c;
            }
        }

        if (!escape) {
            for (; i < value.length; i++) {
                byte c = value[i];
                if (c == '\\'
                        || c == quote
                        || c < ' '
                        || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
                ) {
                    escape = true;
                    break;
                }

                chars[off++] = (char) c;
            }
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        this.off = start;
        writeStringEscape(value);
    }

    public void writeStringUTF16(final byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escapeNoneAscii = (context.features & Feature.EscapeNoneAscii.mask) != 0;

        boolean escape = false;
        int off = this.off;
        int minCapacity = off + value.length + 2;
        if (minCapacity >= chars.length) {
            ensureCapacity(minCapacity);
        }

        final char[] chars = this.chars;
        chars[off++] = quote;

        int i = 0;
        final int upperBound = (value.length - i) & ~15;
        for (; i < upperBound; i += 16) {
            ShortVector v = (ShortVector) ByteVector.SPECIES_128.fromArray(value, i)
                    .castShape(ShortVector.SPECIES_128, 0);

            if (v.eq(V_SHORT_128_DOUBLE_QUOTE)
                    .or(v.eq(V_SHORT_128_SINGLE_QUOTE))
                    .or(v.lt(V_SHORT_128_SPACE))
                    .anyTrue()
                    || (browserSecure
                    && v.eq(V_SHORT_128_LT)
                    .or(v.eq(V_SHORT_128_GT))
                    .or(v.eq(V_SHORT_128_LB))
                    .or(v.eq(V_SHORT_128_RB))
                    .anyTrue())
                    || (escapeNoneAscii && V_SHORT_128_7F.lt(v).anyTrue())
            ) {
                escape = true;
                break;
            }

            v.intoCharArray(chars, off);
            off += 8;
        }

        if (!escape) {
            for (; i < value.length; i += 2) {
                char c = UNSAFE.getChar(value, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + i);
                if (c == '\\'
                        || c == quote
                        || c < ' '
                        || (browserSecure && (c == '<' || c == '>' || c == '(' || c == ')'))
                        || (escapeNoneAscii && c > 0x007F)
                ) {
                    escape = true;
                    break;
                }

                chars[off++] = c;
            }
        }

        if (!escape) {
            chars[off] = quote;
            this.off = off + 1;
            return;
        }

        writeStringEscapeUTF16(value);
    }

    public static class Factory
            implements Function {
        @Override
        public Object apply(Object context) {
            return new JSONWriterUTF16Vector((Context) context);
        }
    }
}

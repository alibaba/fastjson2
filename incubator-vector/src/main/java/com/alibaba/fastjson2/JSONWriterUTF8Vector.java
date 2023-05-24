package com.alibaba.fastjson2;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.Vector;

import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriterUTF16Vector.*;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CODER;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_VALUE;

final class JSONWriterUTF8Vector
        extends JSONWriterUTF8JDK9 {
    JSONWriterUTF8Vector(Context ctx) {
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
            final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
            boolean escape = false;

            int i = 0;
            // vector optimize 8
            final int upperBound = (value.length - i) & ~7;
            for (; i < upperBound; i += 8) {
                Vector<Byte> v = ByteVector.SPECIES_64.fromArray(value, i);
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
            }

            if (!escape) {
                for (; i < value.length; ++i) {
                    byte c = value[i];
                    if (c == quote || c == '\\' || c < ' '
                            || (browserSecure && (c == '<' || c == '>' || c == '('
                            || c == ')'))
                    ) {
                        escape = true;
                        break;
                    }
                }
            }

            if (!escape) {
                int off = this.off;
                int minCapacity = off + value.length + 2;
                if (minCapacity >= this.bytes.length) {
                    ensureCapacity(minCapacity);
                }

                final byte[] bytes = this.bytes;
                bytes[off++] = (byte) this.quote;
                System.arraycopy(value, 0, bytes, off, value.length);
                off += value.length;
                bytes[off] = (byte) this.quote;
                this.off = off + 1;
                return;
            }
            writeStringEscaped(value);
            return;
            // end of latin
        }

        writeStringUTF16(value);
    }

    @Override
    public void writeStringLatin1(byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        final boolean browserSecure = (context.features & BrowserSecure.mask) != 0;
        boolean escape = false;

        int i = 0;
        // vector optimize 8
        final int upperBound = (value.length - i) & ~7;
        for (; i < upperBound; i += 8) {
            Vector<Byte> v = ByteVector.SPECIES_64.fromArray(value, i);
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
        }

        if (!escape) {
            for (; i < value.length; ++i) {
                byte c = value[i];
                if (c == quote || c == '\\' || c < ' '
                        || (browserSecure && (c == '<' || c == '>' || c == '('
                        || c == ')'))
                ) {
                    escape = true;
                    break;
                }
            }
        }

        if (!escape) {
            int off = this.off;
            int minCapacity = off + value.length + 2;
            if (minCapacity >= this.bytes.length) {
                ensureCapacity(minCapacity);
            }

            final byte[] bytes = this.bytes;
            bytes[off++] = (byte) this.quote;
            System.arraycopy(value, 0, bytes, off, value.length);
            off += value.length;
            bytes[off] = (byte) this.quote;
            this.off = off + 1;
            return;
        }
        writeStringEscaped(value);
    }

    public static class Factory
            implements Function {
        @Override
        public Object apply(Object context) {
            return new JSONWriterUTF8Vector((Context) context);
        }
    }
}

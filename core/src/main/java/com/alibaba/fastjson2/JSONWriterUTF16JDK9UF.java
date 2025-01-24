package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;

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

        final byte[] value = STRING_VALUE.apply(str);
        if (STRING_CODER.applyAsInt(str) == 0) {
            writeStringLatin1(value);
        } else {
            writeStringUTF16(value);
        }
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
            off = IOUtils.putBoolean(chars, off, value);
        }
        this.off = off;
    }
}

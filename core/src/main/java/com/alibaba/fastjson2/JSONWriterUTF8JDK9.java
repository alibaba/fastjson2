package com.alibaba.fastjson2;

import static com.alibaba.fastjson2.util.JDKUtils.*;

final class JSONWriterUTF8JDK9
        extends JSONWriterUTF8 {
    JSONWriterUTF8JDK9(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeStringNull();
            return;
        }

        byte[] value = STRING_VALUE.apply(str);
        if (STRING_CODER.applyAsInt(str) == 0) {
            writeStringLatin1(value);
        } else {
            writeStringUTF16(value);
        }
    }
}

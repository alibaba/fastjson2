package com.alibaba.fastjson2;

import static com.alibaba.fastjson2.util.JDKUtils.*;

class JSONWriterUTF8JDK9
        extends JSONWriterUTF8 {
    JSONWriterUTF8JDK9(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        if (STRING_VALUE == null) {
            super.writeString(str);
            return;
        }

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
}

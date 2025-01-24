package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public final class JSONWriterUTF16JDK8UF
        extends JSONWriterUTF16 {
    JSONWriterUTF16JDK8UF(Context ctx) {
        super(ctx);
    }

    @Override
    public void writeString(String str) {
        writeString(
                str == null
                        ? null
                        : (char[]) UNSAFE.getObject(str, JDKUtils.FIELD_STRING_VALUE_OFFSET));
    }
}

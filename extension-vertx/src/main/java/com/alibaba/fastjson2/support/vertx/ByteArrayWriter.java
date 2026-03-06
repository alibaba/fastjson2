package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.util.Base64;

public class ByteArrayWriter implements ObjectWriter<byte[]> {
    public static final ByteArrayWriter INSTANCE = new ByteArrayWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        byte[] bytes = (byte[]) object;
        String base64Str = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // 无填充的 Base64
        jsonWriter.writeString(base64Str);
    }
}

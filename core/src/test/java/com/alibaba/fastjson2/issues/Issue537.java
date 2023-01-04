package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class Issue537 {
    @Test
    public void test() throws Exception {
        JSONWriter writer = JSONWriter.ofJSONB();
        Field field = writer.getClass().getDeclaredField("bytes");
        field.setAccessible(true);
        field.set(writer, new byte[1024 * 1024 + 5]);

        byte[] bytes = new byte[1024 * 1024];
        writer.writeBinary(bytes);
    }
}

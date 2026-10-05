package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmptyGzipArrayTest {
    @Test
    public void emptyRootArrayCanBeGzipped() throws Exception {
        try (JSONWriter writer = JSONWriter.of()) {
            writer.getContext().setDateFormat("gzip,base64");
            writer.writeAny(new byte[0]);
            assertEmptyGzip(JSON.parseObject(writer.toString(), String.class));
        }
    }

    @Test
    public void emptyFieldArrayCanBeGzipped() throws Exception {
        String json = JSON.toJSONString(new Bean());
        assertEmptyGzip(JSON.parseObject(json).getString("value"));
    }

    private static void assertEmptyGzip(String base64) throws Exception {
        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64)))) {
            assertEquals(-1, input.read());
        }
    }

    public static class Bean {
        @JSONField(format = "gzip,base64")
        public byte[] value = new byte[0];
    }
}

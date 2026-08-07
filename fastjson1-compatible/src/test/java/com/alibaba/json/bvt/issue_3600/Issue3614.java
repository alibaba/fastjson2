package com.alibaba.json.bvt.issue_3600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3614 {
    @Test
    public void test_for_issue() throws Exception {
        byte[] gzipBytes = gzip(JSON.toJSONString(Collections.singletonMap("key", "value")).getBytes());

        Object o = JSON.parseObject(gzipBytes, JSONObject.class);
        assertEquals("{\"key\":\"value\"}", JSON.toJSONString(o));
    }

    private static byte[] gzip(byte[] source) throws IOException {
        if (source == null) return null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(source);
        gzip.finish();
        byte[] bytes = bos.toByteArray();
        gzip.close();
        return bytes;
    }
}

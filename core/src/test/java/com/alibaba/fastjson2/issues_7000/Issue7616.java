package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue 7616: JSONB.toBytes throws ArrayIndexOutOfBoundsException when serializing
 * a Bean field that holds a large UTF-16 String (regression introduced in 2.0.56
 * by the ASM direct-write path under-counting BC_OBJECT/BC_OBJECT_END bytes).
 */
public class Issue7616 {
    public static class Request {
        public List<Message> messages;
    }

    public static class Message {
        public String payload;
    }

    @Test
    public void testLargeUtf16StringInNestedBean() {
        int len = 232768;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) (0x4E00 + (i % 100)));
        }
        Request r = new Request();
        Message m = new Message();
        m.payload = sb.toString();
        r.messages = new ArrayList<>();
        r.messages.add(m);

        byte[] bytes = JSONB.toBytes(r);
        Request parsed = JSONB.parseObject(bytes, Request.class);
        assertEquals(m.payload, parsed.messages.get(0).payload);
    }
}

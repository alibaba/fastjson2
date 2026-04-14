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

    public static class MultiField {
        public String a;
        public String b;
        public String c;
    }

    @Test
    public void testLargeUtf16MultiField() {
        // Three fields whose UTF-16 capacities are tightly packed; the previous
        // off-by-two in BC_OBJECT/BC_OBJECT_END accounting failed here too.
        MultiField mf = new MultiField();
        mf.a = repeat('\u4E00', 100000);
        mf.b = repeat('\u4E01', 100000);
        mf.c = repeat('\u4E02', 100000);
        byte[] bytes = JSONB.toBytes(mf);
        MultiField parsed = JSONB.parseObject(bytes, MultiField.class);
        assertEquals(mf.a, parsed.a);
        assertEquals(mf.b, parsed.b);
        assertEquals(mf.c, parsed.c);
    }

    public static class Inner {
        public int x;
    }

    public static class SplitGroupBean {
        public Inner head;     // non-direct (Bean)
        public String middle;  // direct (String)
        public Inner tail;     // non-direct (Bean)
    }

    @Test
    public void testSplitGroupWithDirectMiddle() {
        SplitGroupBean b = new SplitGroupBean();
        b.head = new Inner();
        b.head.x = 1;
        b.middle = repeat('\u4E00', 50000);
        b.tail = new Inner();
        b.tail.x = 2;
        byte[] bytes = JSONB.toBytes(b);
        SplitGroupBean parsed = JSONB.parseObject(bytes, SplitGroupBean.class);
        assertEquals(b.head.x, parsed.head.x);
        assertEquals(b.middle, parsed.middle);
        assertEquals(b.tail.x, parsed.tail.x);
    }

    private static String repeat(char ch, int n) {
        char[] arr = new char[n];
        java.util.Arrays.fill(arr, ch);
        return new String(arr);
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

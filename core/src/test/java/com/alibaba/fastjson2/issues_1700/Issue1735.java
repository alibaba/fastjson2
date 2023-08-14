package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1735 {
    String str = "{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}";
    String expected = "{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}";

    @Test
    public void test() {
        Object object = JSON.parseObject(str);
        assertEquals(expected, JSON.toJSONString(object));
    }

    @Test
    public void testBytesUTF8() {
        byte[] bytes = str.getBytes();
        Object object = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8);
        assertEquals(expected, JSON.toJSONString(object));
    }

    @Test
    public void testBytesLatin1() {
        byte[] bytes = str.getBytes();
        Object object = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
        assertEquals(expected, JSON.toJSONString(object));
    }

    @Test
    public void testChars() {
        Object object = JSON.parseObject(str.toCharArray());
        assertEquals(expected, JSON.toJSONString(object));
    }
}

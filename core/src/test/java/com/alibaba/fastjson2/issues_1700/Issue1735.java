package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1735 {
    String str = "{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}";

    @Test
    public void test() {
        Object object = JSON.parseObject(str);
        assertEquals("{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}", JSON.toJSONString(object));
    }

    @Test
    public void testBytes() {
        Object object = JSON.parseObject(str.getBytes());
        assertEquals("{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}", JSON.toJSONString(object));
    }

    @Test
    public void testChars() {
        Object object = JSON.parseObject(str.toCharArray());
        assertEquals("{\"DG_GridMain\":[{\"PKFIELDNAME\":1},{\"FIELDNAME\":2}]}", JSON.toJSONString(object));
    }
}

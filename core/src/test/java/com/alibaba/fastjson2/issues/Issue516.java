package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue516 {
    @Test
    public void test() {
        Date date = new Date(1656479550452L);
        String str = JSON.toJSONString(date, "yyyy-MM-dd HH:mm:ss.SSS");
        assertEquals("\"2022-06-29 13:12:30.452\"", str);
    }

    @Test
    public void testUTF8() {
        Date date = new Date(1656479550452L);
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.getContext().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        jsonWriter.writeAny(date);
        String str = jsonWriter.toString();
        assertEquals("\"2022-06-29 13:12:30.452\"", str);
    }

    @Test
    public void testUTF16() {
        Date date = new Date(1656479550452L);
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter.getContext().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        jsonWriter.writeAny(date);
        String str = jsonWriter.toString();
        assertEquals("\"2022-06-29 13:12:30.452\"", str);
    }
}

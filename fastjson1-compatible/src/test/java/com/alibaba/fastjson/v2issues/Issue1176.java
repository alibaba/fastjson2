package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1176 {
    @Test
    public void test() throws Exception {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormatSerializer serializer = new SimpleDateFormatSerializer(pattern);

        JSONSerializer jsonSerializer = new JSONSerializer();

        Date date = new Date();
        serializer.write(jsonSerializer, date, null, Date.class, 0);

        assertEquals(
                "\"" + new SimpleDateFormat(pattern).format(date) + "\"",
                jsonSerializer.toString()
        );
    }

    @Test
    public void testNull() throws Exception {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormatSerializer serializer = new SimpleDateFormatSerializer(pattern);

        JSONSerializer jsonSerializer = new JSONSerializer();
        serializer.write(jsonSerializer, null, null, Date.class, 0);
        assertEquals("null", jsonSerializer.toString());
    }
}

package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @Test
    public void test() {
        long millis = 1680386797050L;
        String str = "{\"@type\":\"java.util.Date\",\"val\":" + millis + "}";
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();

        assertEquals(
                millis,
                ((Date) JSON.parseObject(
                        str,
                        Object.class,
                        JSONReader.Feature.SupportAutoType)
                ).getTime()
        );

        assertEquals(
                millis,
                ((Date) JSON.parseObject(
                        chars,
                        0,
                        chars.length,
                        Object.class,
                        JSONReader.Feature.SupportAutoType)
                ).getTime()
        );

        assertEquals(
                millis,
                ((Date) JSON.parseObject(
                        bytes,
                        Object.class,
                        JSONReader.Feature.SupportAutoType)
                ).getTime()
        );
    }
}

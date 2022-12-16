package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderTest_2 {
    @Test
    public void test_read_integer() throws Exception {
        String text = "1001";
        JSONReader reader = new JSONReader(new StringReader(text));
        assertEquals(Integer.valueOf(1001), reader.readInteger());
        reader.close();
    }

    @Test
    public void test_read_Long() throws Exception {
        String text = "1001";
        JSONReader reader = new JSONReader(new StringReader(text));
        assertEquals(Long.valueOf(1001), reader.readLong());
        reader.close();
    }
}

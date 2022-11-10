package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONReaderTest_3 {
    @Test
    public void test_read_Long() throws Exception {
        String text = "1001";
        JSONReader reader = new JSONReader(new StringReader(text));
        assertTrue(reader.hasNext());
    }
}

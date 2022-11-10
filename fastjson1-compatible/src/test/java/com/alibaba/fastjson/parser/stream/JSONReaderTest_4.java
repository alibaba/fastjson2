package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class JSONReaderTest_4 {
    @Test
    public void test_read_Long() throws Exception {
        String text = "1001";
        JSONReader reader = new JSONReader(new MyReader(text));
    }

    public static class MyReader
            extends BufferedReader {
        public MyReader(String s) {
            super(new StringReader(s));
        }

        public void close() throws IOException {
            throw new IOException();
        }
    }
}

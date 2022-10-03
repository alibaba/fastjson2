package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.Clob;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcSupportTest {
    String[] strings = new String[]{
            "abc", "中国", "\0\1\2\3\4\5"
    };

    String[] jsonStrings = new String[]{
            "\"abc\"", "\"中国\"", "\"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\""
    };

    @Test
    public void isClob() {
        JdbcSupport.CLASS_CLOB = null;
        assertTrue(JdbcSupport.isClob(MyClob.class));
        assertTrue(JdbcSupport.isClob(MyClob.class));
        assertTrue(JdbcSupport.isClob(java.sql.Clob.class));

        JdbcSupport.CLASS_CLOB = null;
        assertTrue(JdbcSupport.isClob(java.sql.Clob.class));
        assertFalse(JdbcSupport.isClob(Object.class));

        JdbcSupport.CLASS_CLOB = null;
        assertFalse(JdbcSupport.isClob(Object.class));
    }

    @Test
    public void write() {
        MyClob clob = new MyClob("abc");
        assertEquals("\"abc\"", JSON.toJSONString(clob));

        JdbcSupport.CLASS_CLOB = null;
        JdbcSupport.createClobWriter(MyClob.class);
    }

    @Test
    public void writePretty() {
        MyClob clob = new MyClob("abc");
        assertEquals("\"abc\"", JSON.toJSONString(clob, JSONWriter.Feature.PrettyFormat));
    }

    @Test
    public void writeUTF16() {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            String json = jsonStrings[i];

            MyClob clob = new MyClob(string);
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeAny(clob);
            assertEquals(json, jsonWriter.toString());
        }
    }

    @Test
    public void writeUTF8() {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            String json = jsonStrings[i];

            MyClob clob = new MyClob(string);
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeAny(clob);
            assertEquals(json, jsonWriter.toString());
        }
    }

    public static class MyClob
            implements java.sql.Clob {
        final String value;

        public MyClob(String value) {
            this.value = value;
        }

        @Override
        public long length() {
            return 0;
        }

        @Override
        public String getSubString(long pos, int length) {
            return null;
        }

        @Override
        public Reader getCharacterStream() {
            return new StringReader(value);
        }

        @Override
        public InputStream getAsciiStream() {
            return null;
        }

        @Override
        public long position(String searchstr, long start) {
            return 0;
        }

        @Override
        public long position(Clob searchstr, long start) {
            return 0;
        }

        @Override
        public int setString(long pos, String str) {
            return 0;
        }

        @Override
        public int setString(long pos, String str, int offset, int len) {
            return 0;
        }

        @Override
        public OutputStream setAsciiStream(long pos) {
            return null;
        }

        @Override
        public Writer setCharacterStream(long pos) {
            return null;
        }

        @Override
        public void truncate(long len) {
        }

        @Override
        public void free() {
        }

        @Override
        public Reader getCharacterStream(long pos, long length) {
            return null;
        }
    }
}

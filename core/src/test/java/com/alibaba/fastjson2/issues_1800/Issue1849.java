package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.support.csv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Issue1849 {
    static final String str = "\"Indosat\",\"JAN6B41111111111\",\"1111111111\",\"1\",\"成功\",\"\",\"\",\"3\",\"3\",\"6\",\"1365000\",\"11\",\"15015000\"\n" +
            "\"Indosat\",\"JAZ6A72222222222\",\"2222222222\",\"1\",\"失败\",\"3\",\"IOT_SHell_Login: err: connect rt:-215\",\"\",\"\",\"\",\"\",\"\",\"\"\n";

    static final String str1 = "C1,C2,C3,C4,C5,C6,C7,C8,C9,C10,C11,C12,C13\n" +
            "\"Indosat\",\"JAN6B41111111111\",\"1111111111\",\"1\",\"成功\",\"\",\"\",\"3\",\"3\",\"6\",\"1365000\",\"11\",\"15015000\"\n" +
            "\"Indosat\",\"JAZ6A72222222222\",\"2222222222\",\"1\",\"失败\",\"3\",\"IOT_SHell_Login: err: connect rt:-215\",\"\",\"\",\"\",\"\",\"\",\"\"\n";

    static final String str2 = "\"姓名\",\"年龄\",\"薪酬\"\n" +
            "\"怪兽\",\"23\",\"12345\"\n" +
            "\"大美丽\",\"34\",\"54321\"";

    @Test
    public void test() {
        StringReader stringReader = new StringReader(str);
        CSVReader<?> reader = CSVReader.of(
                stringReader,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                Integer.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class
        );

        while (true) {
            Object[] values = reader.readLineValues();
            if (values == null) {
                break;
            }
            assertEquals(13, values.length);
        }
    }

    @Test
    public void testUTF8() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        CSVReader<?> reader = CSVReader.of(
                inputStream,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                Integer.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class
        );

        while (true) {
            Object[] values = reader.readLineValues();
            if (values == null) {
                break;
            }
            assertEquals(13, values.length);
        }
    }

    @Test
    public void test1() {
        StringReader stringReader = new StringReader(str1);
        CSVReader<?> reader = CSVReader.of(
                stringReader,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                Integer.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class
        );
        List<String> headers = reader.readHeader();
        assertEquals(13, headers.size());
        while (true) {
            Object[] values = reader.readLineValues();
            if (values == null) {
                break;
            }
            assertEquals(13, values.length);
        }
    }

    @Test
    public void test1UTF8() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(str1.getBytes(StandardCharsets.UTF_8));
        CSVReader<?> reader = CSVReader.of(
                inputStream,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                Integer.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class
        );
        List<String> headers = reader.readHeader();
        assertEquals(13, headers.size());
        while (true) {
            Object[] values = reader.readLineValues();
            if (values == null) {
                break;
            }
            assertEquals(13, values.length);
        }
    }

    @Test
    public void test2() {
        byte[] bytes = str2.getBytes(StandardCharsets.UTF_8);
        CSVReader<?> reader = CSVReader.of(
                new ByteArrayInputStream(bytes),
                StandardCharsets.UTF_8,
                String.class,
                int.class,
                Integer.class
        );
        List<String> header = reader.readHeader();
        assertEquals(3, header.size());
        Object[] line0 = reader.readLineValues();
        assertEquals(23, line0[1]);
        assertEquals(12345, line0[2]);
    }

    @Test
    public void test2UTF16() {
        byte[] bytes = str2.getBytes(StandardCharsets.UTF_16);
        CSVReader<?> reader = CSVReader.of(
                new ByteArrayInputStream(bytes),
                StandardCharsets.UTF_16,
                String.class,
                int.class,
                Integer.class
        );
        List<String> header = reader.readHeader();
        assertEquals(3, header.size());
        Object[] line0 = reader.readLineValues();
        assertEquals(23, line0[1]);
        assertEquals(12345, line0[2]);
    }
}

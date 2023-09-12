package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.support.csv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Issue1849 {
    static final String str = "\"Indosat\",\"JAN6B41111111111\",\"1111111111\",\"1\",\"成功\",\"\",\"\",\"3\",\"3\",\"6\",\"1365000\",\"11\",\"15015000\"\n" +
            "\"Indosat\",\"JAZ6A72222222222\",\"2222222222\",\"1\",\"失败\",\"3\",\"IOT_SHell_Login: err: connect rt:-215\",\"\",\"\",\"\",\"\",\"\",\"\"\n";

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
}

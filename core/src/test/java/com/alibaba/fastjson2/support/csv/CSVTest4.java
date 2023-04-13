package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.stream.StreamReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVTest4 {
    @Test
    public void test() throws Exception {
        String str = "1,101\n2,abc";
        byte[] bytes = str.getBytes();
        InputStream in = new ByteArrayInputStream(bytes);
        CSVReader parser = CSVReader.of(in, Integer.class, Integer.class);
        parser.config(StreamReader.Feature.ErrorAsNull);
        parser.config(StreamReader.Feature.ErrorAsNull, false);
        parser.config(StreamReader.Feature.ErrorAsNull, true);
        Object[] line0 = parser.readLineValues();
        assertEquals(1, line0[0]);
        assertEquals(101, line0[1]);
        Object[] line1 = parser.readLineValues();
        assertEquals(2, line1[0]);
        assertEquals(null, line1[1]);
    }

    @Test
    public void test1() throws Exception {
        String str = "1,a\n2,a";
        {
            CSVReader csvReader = CSVReader.of(str.toCharArray());
            String[] line0 = csvReader.readLine();
            assertEquals("1", line0[0]);
            assertEquals("a", line0[1]);

            String[] line1 = csvReader.readLine();
            assertEquals("2", line1[0]);
            assertEquals("a", line1[1]);
        }
        {
            byte[] bytes = str.getBytes();
            CSVReader csvReader = new CSVReaderUTF8(bytes, 0, bytes.length, StandardCharsets.UTF_8, (Class) null);
            String[] line0 = csvReader.readLine();
            assertEquals("1", line0[0]);
            assertEquals("a", line0[1]);

            String[] line1 = csvReader.readLine();
            assertEquals("2", line1[0]);
            assertEquals("a", line1[1]);
        }
    }

    @Test
    public void test2() throws Exception {
        String str = "\"1\",\"a\"\n2,\"a\"";
        {
            CSVReader csvReader = CSVReader.of(str.toCharArray());
            String[] line0 = csvReader.readLine();
            assertEquals("1", line0[0]);
            assertEquals("a", line0[1]);

            String[] line1 = csvReader.readLine();
            assertEquals("2", line1[0]);
            assertEquals("a", line1[1]);
        }
        {
            byte[] bytes = str.getBytes();
            CSVReader csvReader = new CSVReaderUTF8(bytes, 0, bytes.length, StandardCharsets.UTF_8, (Class) null);
            String[] line0 = csvReader.readLine();
            assertEquals("1", line0[0]);
            assertEquals("a", line0[1]);

            String[] line1 = csvReader.readLine();
            assertEquals("2", line1[0]);
            assertEquals("a", line1[1]);
        }
    }
}

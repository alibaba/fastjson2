package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.reader.CharArrayValueConsumer;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CSVWriterTest {
    @Test
    public void writeDecimals() {
        String[] strings = new String[]{
                "123456.78",
                "-123456.78",
                "123",
                "123.45",
                "-123",
                "-123.45",
                "0.01",
                "-0.01",
                "0.123",
                "-0.123",
                "-922337203685477580",
                "-9223372036854775.80",
        };

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            BigDecimal decimal = new BigDecimal(string);

            CSVWriter writer = CSVWriter.of();
            long unscaledVal = decimal.unscaledValue().longValue();
            int scale = decimal.scale();
            writer.writeDecimal(unscaledVal, scale);
            assertEquals(decimal.toString(), writer.toString());
        }
    }

    @Test
    public void writeDecimals1() {
        String[] strings = new String[]{
                "123456.78",
                "-123456.78",
                "123",
                "123.45",
                "-123",
                "-123.45",
                "0.01",
                "-0.01",
                "0.123",
                "-0.123",
                "-9223372036854775808",
                "-9223372036854775.808",
        };

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            BigDecimal decimal = new BigDecimal(string);

            CSVWriter writer = CSVWriter.of();
            writer.writeDecimal(decimal);
            assertEquals(decimal.toString(), writer.toString());
        }
    }

    @Test
    public void writeDecimal1() {
        CSVWriter writer = CSVWriter.of();
        writer.writeDecimal(12345, 2);
        assertEquals("123.45", writer.toString());
    }

    @Test
    public void writeRow() {
        CSVWriter writer = CSVWriter.of();
        String[] strings = new String[]{"id", "name"};
        writer.writeRow(strings.length, i -> strings[i]);
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow1() {
        CSVWriter writer = CSVWriter.of();
        List<String> strings = Arrays.asList("id", "name");
        writer.writeRow(strings.size(), i -> strings.get(i));
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow2() {
        CSVWriter writer = CSVWriter.of();
        String[] strings = new String[]{"id", "name"};
        writer.writeRow(strings);
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow3() {
        CSVWriter writer = CSVWriter.of();
        Object[] values = new Object[]{101, "abc", true};
        writer.writeRow(values);
        assertEquals("101,abc,true\n", writer.toString());
    }

    @Test
    public void writeDate() {
        Date date = new Date();
        CSVWriter writer = CSVWriter.of();
        writer.writeDate(date);
        assertEquals(DateUtils.toString(date), writer.toString());
    }

    @Test
    public void writeDate1() {
        Date date = new Date(1681134782000L);
        CSVWriter writer = CSVWriter.of();
        writer.writeDate(date);
        assertEquals(DateUtils.toString(date), writer.toString());
    }

    @Test
    public void writeDate2() {
        long millis = LocalDate.of(2023, 4, 10).atStartOfDay()
                .atZone(DateUtils.DEFAULT_ZONE_ID)
                .toInstant()
                .toEpochMilli();
        Date date = new Date(millis);
        CSVWriter writer = CSVWriter.of();
        writer.writeDate(date);
        assertEquals("2023-04-10", writer.toString());
    }

    @Test
    public void writeFloat() {
        Random r = new Random();
        float value = r.nextFloat();
        CSVWriter writer = CSVWriter.of();
        writer.writeFloat(value);
        assertEquals(Float.toString(value), writer.toString());
    }

    @Test
    public void writeDouble() {
        Random r = new Random();
        double value = r.nextDouble();
        CSVWriter writer = CSVWriter.of();
        writer.writeDouble(value);
        assertEquals(Double.toString(value), writer.toString());
    }

    @Test
    public void test1() throws Exception {
        File file = File.createTempFile("tmp", "csv");
        try (CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8)) {
            String str = "123,101,abc";
            byte[] bytes = str.getBytes();
            writer.writeDirect(bytes, 4, bytes.length - 4);
        }

        try (CSVReader reader = CSVReader.of(file)) {
            String[] strings = reader.readLine();
            assertEquals("101", strings[0]);
            assertEquals("abc", strings[1]);
        }

        try (CSVReader reader = CSVReader.of(file, (CharArrayValueConsumer) null)) {
            assertTrue(reader instanceof CSVReaderUTF16);
            String[] strings = reader.readLine();
            assertEquals("101", strings[0]);
            assertEquals("abc", strings[1]);
        }
    }

    @Test
    public void test2() throws Exception {
        File file = File.createTempFile("tmp", "csv");
        try (CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_8)) {
            writer.writeInt32(101);
            writer.writeComma();
            writer.writeString("abc");
            writer.writeLine();
        }

        try (CSVReader<Bean> reader = CSVReader.of(file, Bean.class)) {
            Bean bean = reader.readLineObject();
            assertEquals(101, bean.id);
            assertEquals("abc", bean.name);
        }

        try (CSVReader reader = CSVReader.of(file, (ByteArrayValueConsumer) null)) {
            assertTrue(reader instanceof CSVReaderUTF8);
            String[] strings = reader.readLine();
            assertEquals("101", strings[0]);
            assertEquals("abc", strings[1]);
        }
    }

    public static class Bean {
        public int id;
        public String name;
    }
}

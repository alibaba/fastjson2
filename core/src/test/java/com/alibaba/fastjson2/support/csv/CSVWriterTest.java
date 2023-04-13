package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.reader.CharArrayValueConsumer;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

            CSVWriter writer2 = CSVWriter.of(new StringWriter());
            writer2.writeDecimal(unscaledVal, scale);
            assertEquals(decimal.toString(), writer2.toString());
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

            CSVWriter writer2 = CSVWriter.of(new StringWriter());
            writer2.writeDecimal(decimal);
            assertEquals(decimal.toString(), writer2.toString());
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
        writer.writeLine(strings.length, i -> strings[i]);
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow1() {
        CSVWriter writer = CSVWriter.of();
        List<String> strings = Arrays.asList("id", "name");
        writer.writeLine(strings.size(), i -> strings.get(i));
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow2() {
        CSVWriter writer = CSVWriter.of();
        String[] strings = new String[]{"id", "name"};
        writer.writeLine(strings);
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow2List() {
        CSVWriter writer = CSVWriter.of();
        String[] strings = new String[]{"id", "name"};
        writer.writeLine(Arrays.asList(strings));
        assertEquals("id,name\n", writer.toString());
    }

    @Test
    public void writeRow3() {
        CSVWriter writer = CSVWriter.of();
        Object[] values = new Object[]{101, "abc", true};
        writer.writeLine(values);
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
        try (CSVWriterUTF8 writer = (CSVWriterUTF8) CSVWriter.of(file, StandardCharsets.UTF_8)) {
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
    public void test1UTF16() throws Exception {
        File file = File.createTempFile("tmp", "csv");
        try (CSVWriterUTF16 writer = (CSVWriterUTF16) CSVWriter.of(file, StandardCharsets.UTF_16)) {
            String str = "123,101,abc";
            char[] bytes = str.toCharArray();
            writer.writeDirect(bytes, 4, bytes.length - 4);
        }

        try (CSVReader reader = CSVReader.of(file, StandardCharsets.UTF_16)) {
            String[] strings = reader.readLine();
            assertEquals("101", strings[0]);
            assertEquals("abc", strings[1]);
        }

        try (CSVReader reader = CSVReader.of(file, StandardCharsets.UTF_16, (CharArrayValueConsumer) null)) {
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

    @Test
    public void test2UTF16() throws Exception {
        File file = File.createTempFile("tmp", "csv");
        try (CSVWriter writer = CSVWriter.of(file, StandardCharsets.UTF_16)) {
            writer.writeInt32(101);
            writer.writeComma();
            writer.writeString("abc");
            writer.writeLine();
        }

        try (CSVReader<Bean> reader = CSVReader.of(file, StandardCharsets.UTF_16, Bean.class)) {
            Bean bean = reader.readLineObject();
            assertEquals(101, bean.id);
            assertEquals("abc", bean.name);
        }

        try (CSVReader reader = CSVReader.of(file, StandardCharsets.UTF_16, (CharArrayValueConsumer) null)) {
            assertTrue(reader instanceof CSVReaderUTF16);
            String[] strings = reader.readLine();
            assertEquals("101", strings[0]);
            assertEquals("abc", strings[1]);
        }
    }

    @Test
    public void test3() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new StringWriter())) {
            csvWriter.writeLine(123, BigInteger.valueOf(102), BigDecimal.valueOf(12345, 2), "abc");
            assertEquals("123,102,123.45,abc\n", csvWriter.toString());
        }
    }

    @Test
    public void test4UTF8() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new ByteArrayOutputStream())) {
            csvWriter.writeQuote();
            csvWriter.writeInt64(101);
            csvWriter.writeQuote();
            assertEquals("\"101\"", csvWriter.toString());
        }
    }

    @Test
    public void test4() throws Exception {
        try (CSVWriter writer = CSVWriter.of(new StringWriter())) {
            writer.writeQuote();
            writer.writeInt64(101);
            writer.writeQuote();
            writer.writeComma();
            writer.writeBoolean(true);
            writer.writeComma();
            writer.writeDateYYYMMDD10(2012, 1, 24);
            writer.writeComma();
            writer.writeDateTime19(1978, 7, 15, 12, 13, 14);
            writer.writeComma();
            writer.writeString("abc".getBytes());
            assertEquals("\"101\",true,2012-01-24,1978-07-15 12:13:14,abc", writer.toString());
        }
    }

    @Test
    public void test5() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new StringWriter())) {
            csvWriter.writeLine(1.1F, 2.1D);
            assertEquals("1.1,2.1\n", csvWriter.toString());
        }
    }

    @Test
    public void test6() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new StringWriter())) {
            csvWriter.writeString("\"abc".getBytes());
            assertEquals("\"\"\"abc\"", csvWriter.toString());
        }
    }

    @Test
    public void test6UTF8() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new ByteArrayOutputStream())) {
            csvWriter.writeString("\"abc".getBytes());
            assertEquals("\"\"\"abc\"", csvWriter.toString());
        }
    }

    @Test
    public void testWriteInstant() throws Exception {
        LocalDate date = LocalDate.of(2018, 7, 12);
        LocalTime time = LocalTime.of(11, 9, 10);
        LocalTime time1 = LocalTime.of(11, 9, 10, 1000000);
        LocalTime time2 = LocalTime.of(11, 9, 10, 1000);
        try (CSVWriter csvWriter = CSVWriter.of(new StringWriter())) {
            csvWriter.writeInstant(LocalDateTime.of(date, time).atZone(DateUtils.DEFAULT_ZONE_ID).toInstant());
            csvWriter.writeComma();
            csvWriter.writeInstant(LocalDateTime.of(date, time1).atZone(DateUtils.DEFAULT_ZONE_ID).toInstant());
            csvWriter.writeComma();
            csvWriter.writeInstant(LocalDateTime.of(date, time2).atZone(DateUtils.DEFAULT_ZONE_ID).toInstant());
            assertEquals(
                    "2018-07-12 11:09:10,2018-07-12 11:09:10.001,2018-07-12 11:09:10.000001",
                    csvWriter.toString()
            );
        }
    }

    @Test
    public void testWriteInstant1() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new StringWriter())) {
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1000000));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1000));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1));
            assertEquals(
                    "2018-07-12 11:09:10,2018-07-12 11:09:10.001,2018-07-12 11:09:10.000001,2018-07-12 11:09:10.000000001",
                    csvWriter.toString()
            );
        }
    }

    @Test
    public void testWriteInstant1UTF8() throws Exception {
        try (CSVWriter csvWriter = CSVWriter.of(new ByteArrayOutputStream())) {
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1000000));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1000));
            csvWriter.writeComma();
            csvWriter.writeLocalDateTime(LocalDateTime.of(2018, 7, 12, 11, 9, 10, 1));
            assertEquals(
                    "2018-07-12 11:09:10,2018-07-12 11:09:10.001,2018-07-12 11:09:10.000001,2018-07-12 11:09:10.000000001",
                    csvWriter.toString()
            );
        }
    }

    public static class Bean {
        public int id;
        public String name;
    }
}

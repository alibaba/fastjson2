package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.reader.CharArrayValueConsumer;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CSVReaderTest3 {
    @Test
    public void test() {
        String str = "id,name\n101,DataWorks\n";
        CSVReader parser = CSVReader.of(str, Bean.class);
        parser.readHeader();
        Object[] line = parser.readLineValues();
        assertEquals(2, line.length);
        assertEquals(101, line[0]);
        assertEquals("DataWorks", line[1]);
    }

    @Test
    public void testLines() {
        String str = "id,name\n101,DataWorks\n102\n103,a,b\n104\n";
        CSVReader parser = CSVReader.of(str, Bean.class);
        parser.readHeader();
        {
            Object[] line = parser.readLineValues();
            assertEquals(2, line.length);
            assertEquals(101, line[0]);
            assertEquals("DataWorks", line[1]);
        }
        {
            Object[] line = parser.readLineValues();
            assertEquals(2, line.length);
            assertEquals(102, line[0]);
            assertEquals(null, line[1]);
        }
        {
            Object[] line = parser.readLineValues();
            assertEquals(2, line.length);
            assertEquals(103, line[0]);
            assertEquals("a", line[1]);
        }
        {
            Object[] line = parser.readLineValues();
            assertEquals(2, line.length);
            assertEquals(104, line[0]);
            assertEquals(null, line[1]);
        }
        assertNull(parser.readLineValues());
    }

    @Test
    public void testObject() {
        String str = "id,name\n101,DataWorks\n";
        CSVReader<Bean> parser = CSVReader.of(str, Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    @Test
    public void test1() {
        String str = "name,id\nDataWorks,101\n";
        CSVReader parser = CSVReader.of(str, Bean.class);
        parser.readHeader();
        Object[] line = parser.readLineValues();
        assertEquals(2, line.length);
        assertEquals("DataWorks", line[0]);
        assertEquals(101, line[1]);
    }

    @Test
    public void testObject1() {
        String str = "name,id\n" +
                "DataWorks,101\n";
        CSVReader<Bean> parser = CSVReader.of(str, Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    @Test
    public void testObject2() {
        String str = "101,DataWorks\n";
        CSVReader<Bean> parser = CSVReader.of(str.toCharArray(), Bean.class);
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);

        CSVReader.of(str, Bean.class)
                .readLineObjectAll(false, o -> {});
    }

    @Test
    public void testObject2UTF8() {
        String str = "101,DataWorks\n";
        byte[] bytes = str.getBytes();
        CSVReader<Bean> parser = new CSVReaderUTF8(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean.class);
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);

        CSVReader.of(str, Bean.class)
                .readLineObjectAll(false, o -> {});
    }

    @Test
    public void testObject3() {
        String str = "id,name\n101,DataWorks\n";
        CSVReader<Bean> parser = CSVReader.of(str.toCharArray(), Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
        assertEquals(Integer.class, parser.getColumnType(0));
        assertEquals(String.class, parser.getColumnType(1));

        CSVReader.of(str, Bean.class)
                .readLineObjectAll(
                        o -> {
                            assertEquals(101, bean.id);
                            assertEquals("DataWorks", bean.name);
                        }
                );
    }

    @Test
    public void testObject3UTF8() {
        String str = "id,name\n101,DataWorks\n";
        byte[] bytes = str.getBytes();
        CSVReader<Bean> parser = new CSVReaderUTF8<>(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
        assertEquals(Integer.class, parser.getColumnType(0));
        assertEquals(String.class, parser.getColumnType(1));

        CSVReader.of(str, Bean.class)
                .readLineObjectAll(
                        o -> {
                            assertEquals(101, bean.id);
                            assertEquals("DataWorks", bean.name);
                        }
                );
    }

    @Test
    public void testObject4() {
        String str = "id,name\n101,DataWorks\n";
        CSVReader<Bean> parser = CSVReader.of(str.getBytes(), Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);

        assertEquals(Integer.class, parser.getColumnType(0));
        assertEquals(String.class, parser.getColumnType(1));

        assertEquals(Integer.class, parser.getColumnType(0));
        assertEquals(String.class, parser.getColumnType(1));

        assertEquals("id", parser.getColumn(0));
        assertEquals("name", parser.getColumn(1));
    }

    @Test
    public void testObject5() {
        String str = "name,id\nDataWorks,101\n";
        CSVReader<Bean> parser = CSVReader.of(str.getBytes(), Bean.class);
        parser.readHeader();
        Bean bean = parser.readLineObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);

        assertEquals(String.class, parser.getColumnType(0));
        assertEquals(Integer.class, parser.getColumnType(1));

        assertEquals(String.class, parser.getColumnType(0));
        assertEquals(Integer.class, parser.getColumnType(1));

        assertEquals("name", parser.getColumn(0));
        assertEquals("id", parser.getColumn(1));

        assertEquals(2, parser.getColumns().size());
    }

    @Test
    public void testObject6() throws Exception {
        String str = "\nname,id\nDataWorks,101\n";
        CSVReader<Bean> parser = CSVReader.of(str.getBytes(), Bean.class);
        parser.skipLines(1);
        parser.readHeader();
        parser.statAll();
        assertEquals(2, parser.getColumnStats().size());
        assertEquals(Integer.class, parser.getColumnStat("id").getInferType());
        assertEquals(String.class, parser.getColumnStat("name").getInferType());
        assertEquals(0, parser.errorCount());
    }

    @JSONType(orders = {"id", "name"})
    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void testBean2UTF8() {
        String str = "101,DataWorks\n";
        byte[] bytes = str.getBytes();
        CSVReaderUTF8<Bean2> parser = new CSVReaderUTF8(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean2.class);

        parser.new ByteArrayConsumerImpl(
                (Consumer) o -> {}
        );

        ByteArrayValueConsumer consumer = new ByteArrayValueConsumer() {
            @Override
            public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
            }
        };
        consumer.beforeRow(1);
        consumer.afterRow(1);
    }

    @Test
    public void testBean2UTF16() {
        String str = "101,DataWorks\n";
        char[] chars = str.toCharArray();
        CSVReaderUTF16<Bean2> parser = new CSVReaderUTF16(chars, 0, chars.length, Bean2.class);

        parser.new CharArrayConsumerImpl<Bean2>(
                e -> {}
        );

        CharArrayValueConsumer consumer = new CharArrayValueConsumer() {
            @Override
            public void accept(int row, int column, char[] bytes, int off, int len) {
            }
        };
        consumer.beforeRow(1);
        consumer.afterRow(1);
    }

    @JSONType(orders = {"id", "name"})
    public static class Bean2 {
        public int id;
        public String name;
    }
}

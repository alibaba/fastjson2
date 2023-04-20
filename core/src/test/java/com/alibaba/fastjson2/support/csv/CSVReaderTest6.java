package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.reader.CharArrayValueConsumer;
import com.alibaba.fastjson2.stream.StreamReader;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class CSVReaderTest6 {
    @Test
    public void test() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean> csvReader = CSVReader.of(
                new InputStreamReader(
                        new ByteArrayInputStream(bytes), StandardCharsets.UTF_8
                ),
                Bean.class
        );
        AtomicInteger count = new AtomicInteger();
        csvReader.readLineObjectAll(e -> {
            assertEquals(1001, e.id);
            assertEquals(68, e.age);
            assertEquals("abc", e.name);
            assertEquals(1.0F, e.f0);
            assertEquals(2.0D, e.d0);
            assertEquals(DateUtils.parseDate("2018-07-14"), e.d1);
            assertEquals(new BigDecimal("3.1"), e.d2);
            assertEquals(true, e.f1);
            assertEquals(Boolean.TRUE, e.f2);
            assertEquals(4.2F, e.f3);
            assertEquals(12.6D, e.d3);
            assertEquals(500L, e.l1);
            assertEquals(100, e.s0);
            assertEquals((short) 200, e.s1);
            assertEquals(11, e.b0);
            assertEquals((byte) 21, e.b1);
            assertEquals(LocalDate.of(2018, 7, 15), e.d4);

            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Test
    public void testUTF8() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean> csvReader = new CSVReaderUTF8<>(
                new ByteArrayInputStream(bytes),
                StandardCharsets.UTF_8,
                Bean.class
        );
        AtomicInteger count = new AtomicInteger();
        csvReader.readLineObjectAll(e -> {
            assertEquals(1001, e.id);
            assertEquals(68, e.age);
            assertEquals("abc", e.name);
            assertEquals(1.0F, e.f0);
            assertEquals(2.0D, e.d0);
            assertEquals(DateUtils.parseDate("2018-07-14"), e.d1);
            assertEquals(new BigDecimal("3.1"), e.d2);
            assertEquals(true, e.f1);
            assertEquals(Boolean.TRUE, e.f2);
            assertEquals(4.2F, e.f3);
            assertEquals(12.6D, e.d3);
            assertEquals(500L, e.l1);
            assertEquals(100, e.s0);
            assertEquals((short) 200, e.s1);
            assertEquals(11, e.b0);
            assertEquals((byte) 21, e.b1);
            assertEquals(LocalDate.of(2018, 7, 15), e.d4);

            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Data
    public static class Bean {
        private long id;
        private int age;
        private String name;
        private float f0;
        private double d0;
        private Date d1;
        private BigDecimal d2;
        private boolean f1;
        private Boolean f2;
        private float f3;
        private double d3;
        private Long l1;
        private short s0;
        private Short s1;
        private byte b0;
        private Byte b1;
        private LocalDate d4;
    }

    @Test
    public void test1() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean1> csvReader = CSVReader.of(
                new InputStreamReader(
                        new ByteArrayInputStream(bytes),
                        StandardCharsets.UTF_8
                ),
                Bean1.class
        );
        AtomicInteger count = new AtomicInteger();
        csvReader.readLineObjectAll(e -> {
            assertEquals(1001, e.id);
            assertEquals(68, e.age);
            assertEquals("abc", e.name);
            assertEquals(1.0F, e.f0);
            assertEquals(2.0D, e.d0);
            assertEquals(DateUtils.parseDate("2018-07-14"), e.d1);
            assertEquals(new BigDecimal("3.1"), e.d2);
            assertEquals(true, e.f1);
            assertEquals(Boolean.TRUE, e.f2);
            assertEquals(4.2F, e.f3);
            assertEquals(12.6D, e.d3);
            assertEquals(500L, e.l1);
            assertEquals(100, e.s0);
            assertEquals((short) 200, e.s1);
            assertEquals(11, e.b0);
            assertEquals((byte) 21, e.b1);
            assertEquals(LocalDate.of(2018, 7, 15), e.d4);

            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Test
    public void test1UTF8() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean1> csvReader = new CSVReaderUTF8<>(
                new ByteArrayInputStream(bytes),
                StandardCharsets.UTF_8,
                Bean1.class
        );
        AtomicInteger count = new AtomicInteger();
        csvReader.readLineObjectAll(e -> {
            assertEquals(1001, e.id);
            assertEquals(68, e.age);
            assertEquals("abc", e.name);
            assertEquals(1.0F, e.f0);
            assertEquals(2.0D, e.d0);
            assertEquals(DateUtils.parseDate("2018-07-14"), e.d1);
            assertEquals(new BigDecimal("3.1"), e.d2);
            assertEquals(true, e.f1);
            assertEquals(Boolean.TRUE, e.f2);
            assertEquals(4.2F, e.f3);
            assertEquals(12.6D, e.d3);
            assertEquals(500L, e.l1);
            assertEquals(100, e.s0);
            assertEquals((short) 200, e.s1);
            assertEquals(11, e.b0);
            assertEquals((byte) 21, e.b1);
            assertEquals(LocalDate.of(2018, 7, 15), e.d4);

            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    public static class Bean1 {
        public long id;
        public int age;
        public String name;
        public float f0;
        public double d0;
        public Date d1;
        public BigDecimal d2;
        public boolean f1;
        public Boolean f2;
        public float f3;
        public double d3;
        public Long l1;
        public short s0;
        public Short s1;
        public byte b0;
        public Byte b1;
        public LocalDate d4;
    }

    @Test
    public void test2() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean3> csvReader = CSVReader.of(new ByteArrayInputStream(bytes), Bean3.class);
        AtomicInteger count = new AtomicInteger();
        csvReader.readLineObjectAll(e -> {
            assertEquals(1001, e.id);
            assertEquals(68, e.age);
            assertEquals("abc", e.name);
            assertEquals(1.0F, e.f0);
            assertEquals(2.0D, e.d0);
            assertEquals(DateUtils.parseDate("2018-07-14"), e.d1);
            assertEquals(new BigDecimal("3.1"), e.d2);
            assertEquals(true, e.f1);
            assertEquals(Boolean.TRUE, e.f2);
            assertEquals(4.2F, e.f3);
            assertEquals(12.6D, e.d3);
            assertEquals(500L, e.l1);
            assertEquals(100, e.s0);
            assertEquals((short) 200, e.s1);
            assertEquals(11, e.b0);
            assertEquals((byte) 21, e.b1);
            assertEquals(LocalDate.of(2018, 7, 15), e.d4);

            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Data
    private static class Bean3 {
        private long id;
        private int age;
        private String name;
        private float f0;
        private double d0;
        private Date d1;
        private BigDecimal d2;
        private boolean f1;
        private Boolean f2;
        private float f3;
        private double d3;
        private Long l1;
        private short s0;
        private Short s1;
        private byte b0;
        private Byte b1;
        private LocalDate d4;
    }

    @Test
    public void test3() {
        String str = "101,abc";
        byte[] bytes = str.getBytes();
        char[] chars = str.toCharArray();

        CSVReader csvReader = CSVReader.of(bytes, (ByteArrayValueConsumer) null);
        assertTrue(csvReader instanceof CSVReaderUTF8);
        assertFalse(csvReader.isEnd());

        CSVReader<Object> csvReader1 = CSVReader.of(chars, 0, chars.length, (CharArrayValueConsumer) null);
        assertTrue(csvReader1 instanceof CSVReaderUTF16);
        assertFalse(csvReader1.isEnd());
    }

    @Test
    public void test4() {
        byte[] bytes = "101,abc".getBytes();
        CSVReader<Bean4> csvReader = CSVReader.of(bytes, Bean4.class);
        Bean4 bean4 = csvReader.readLineObject();
        assertEquals(101, bean4.id);
        assertEquals("abc", bean4.name);
    }

    @Test
    public void test4_charset() {
        byte[] bytes = "101,abc".getBytes();
        CSVReader<Bean4> csvReader = CSVReader.of(bytes, StandardCharsets.UTF_8, Bean4.class);
        Bean4 bean4 = csvReader.readLineObject();
        assertEquals(101, bean4.id);
        assertEquals("abc", bean4.name);
    }

    @Test
    public void test4_x1() {
        byte[] bytes = "101,abc".getBytes();
        CSVReader<Bean4> csvReader = CSVReader.of(bytes, 0, bytes.length, Bean4.class);
        Bean4 bean4 = csvReader.readLineObject();
        assertEquals(101, bean4.id);
        assertEquals("abc", bean4.name);
        assertTrue(csvReader.isEnd());
    }

    @Test
    public void test4_utf16() {
        char[] chars = "101,abc".toCharArray();
        CSVReader<Bean4> csvReader = CSVReader.of(chars, 0, chars.length, Bean4.class);
        Bean4 bean4 = csvReader.readLineObject();
        assertEquals(101, bean4.id);
        assertEquals("abc", bean4.name);
        assertTrue(csvReader.isEnd());
    }

    static class Bean4 {
        public int id;
        public String name;
    }

    @Test
    public void statAll() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        char[] chars = str.toCharArray();
        CSVReader csvReader = CSVReader.of(chars);
        csvReader.readHeader();
        csvReader.statAll();
        List<StreamReader.ColumnStat> columns = csvReader.getColumnStats();
        assertEquals(3, columns.size());
        assertEquals("INT", columns.get(0).getInferSQLType());
        assertEquals("STRING", columns.get(1).getInferSQLType());
        assertEquals("DATETIME", columns.get(2).getInferSQLType());
    }

    @Test
    public void statAll_r() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        char[] chars = str.toCharArray();
        CSVReader csvReader = CSVReader.of(chars);
        csvReader.readHeader();
        csvReader.statAll(1);
        List<StreamReader.ColumnStat> columns = csvReader.getColumnStats();
        assertEquals(3, columns.size());
        assertEquals("INT", columns.get(0).getInferSQLType());
        assertEquals("STRING", columns.get(1).getInferSQLType());
        assertEquals("DATETIME", columns.get(2).getInferSQLType());
    }

    @Test
    public void readAll() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        char[] chars = str.toCharArray();
        CSVReader csvReader = CSVReader.of(chars);
        csvReader.readHeader();
        assertThrows(Exception.class, () -> csvReader.readAll());
    }

    @Test
    public void readRows() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        char[] chars = str.toCharArray();
        CSVReader csvReader = CSVReader.of(chars);
        csvReader.readHeader();
        assertThrows(Exception.class, () -> csvReader.readAll(100));
    }

    @Test
    public void readAllUTF8() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        CSVReaderUTF8 csvReader = new CSVReaderUTF8(bytes, 0, bytes.length, new Type[0]);
        csvReader.readHeader();
        assertThrows(Exception.class, () -> csvReader.readAll());

        Consumer consumer = o -> {};
        CSVReaderUTF8.ByteArrayConsumerImpl byteArrayConsumer = csvReader.new ByteArrayConsumerImpl(consumer);
        byteArrayConsumer.beforeRow(0);
        byteArrayConsumer.afterRow(0);
    }

    @Test
    public void readRowsUTF8() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        CSVReaderUTF8 csvReader = new CSVReaderUTF8(bytes, 0, bytes.length, new Type[0]);
        csvReader.readHeader();
        assertThrows(Exception.class, () -> csvReader.readAll(1000));

        Consumer consumer = o -> {};
        CSVReaderUTF8.ByteArrayConsumerImpl byteArrayConsumer = csvReader.new ByteArrayConsumerImpl(consumer);
        byteArrayConsumer.beforeRow(0);
        byteArrayConsumer.afterRow(0);
    }

    @Test
    public void readAllUTF8Consumer() {
        String str = "id,name,date\n101,abc,2-Feb-07";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        CSVReaderUTF8<Bean5> csvReader = new CSVReaderUTF8(bytes, 0, bytes.length, Bean5.class);

        CSVReaderUTF8.valueConsumerCreators.put(
                Fnv.hashCode64(Bean5.class.getName(), "id", "name", "date"),
                (o) -> csvReader.new ByteArrayConsumerImpl(o)
        );

        csvReader.readLineObjectAll(
                o -> {
                    assertEquals(101, o.id);
                    assertEquals("abc", o.name);
                }
        );
    }

    public static class Bean5 {
        public int id;
        public String name;
        public Date date;
    }
}

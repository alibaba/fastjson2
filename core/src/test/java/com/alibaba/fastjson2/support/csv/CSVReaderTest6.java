package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.util.DateUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVReaderTest6 {
    @Test
    public void test() {
        String str = "id,age,name,f0,d0,d1,d2,f1,f2,f3,d3,l1,s0,s1,b0,b1,d4\n" +
                "1001,68,abc,1.0,2.0,2018-07-14,3.1,true,true,4.2,12.6,500,100,200,11,21,2018-07-15";
        byte[] bytes = str.getBytes();
        CSVReader<Bean> csvReader = CSVReader.of(new ByteArrayInputStream(bytes), Bean.class);
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
        CSVReader<Bean1> csvReader = CSVReader.of(new ByteArrayInputStream(bytes), Bean1.class);
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
}

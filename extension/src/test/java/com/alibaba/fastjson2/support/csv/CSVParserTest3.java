package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CSVParserTest3 {
    @Test
    public void test() {
        String str = "id,name\n101,DataWorks\n";
        CSVParser parser = CSVParser.of(str, Bean.class);
        parser.readHeader();
        Object[] line = parser.readLineValues();
        assertEquals(2, line.length);
        assertEquals(101, line[0]);
        assertEquals("DataWorks", line[1]);
    }

    @Test
    public void testLines() {
        String str = "id,name\n101,DataWorks\n102\n103,a,b\n104\n";
        CSVParser parser = CSVParser.of(str, Bean.class);
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
        CSVParser parser = CSVParser.of(str, Bean.class);
        parser.readHeader();
        Bean bean = parser.readLoneObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    @Test
    public void test1() {
        String str = "name,id\nDataWorks,101\n";
        CSVParser parser = CSVParser.of(str, Bean.class);
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
        CSVParser parser = CSVParser.of(str, Bean.class);
        parser.readHeader();
        Bean bean = parser.readLoneObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    @Test
    public void testObject2() {
        String str = "101,DataWorks\n";
        CSVParser parser = CSVParser.of(str, Bean.class);
        Bean bean = parser.readLoneObject();
        assertEquals(101, bean.id);
        assertEquals("DataWorks", bean.name);
    }

    @JSONType(orders = {"id", "name"})
    public static class Bean {
        public int id;
        public String name;
    }
}

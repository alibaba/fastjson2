package com.alibaba.fastjson2.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserTest {
    @Test
    public void test() {
        CSVParser parser = CSVParser.of(str);
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        String[][] lines = new String[][]{
                new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
                new String[]{"1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00"},
                new String[]{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00"},
                new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\n" +
                        "air, moon roof, loaded", "4799.00"},
        };
        for (int i = 0; ; ++i) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            assertArrayEquals(lines[i], line);
        }
    }

    @Test
    public void testBytes() {
        CSVParser parser = CSVParser.of(str.getBytes());
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        {
            String[] line = parser.readLine();
            assertEquals(5, line.length);
            assertEquals("1997", line[0]);
            assertEquals("Ford", line[1]);
            assertEquals("E350", line[2]);
            assertEquals("ac, abs, moon", line[3]);
            assertEquals("3000.00", line[4]);
        }
        {
            String[] line = parser.readLine();
            assertEquals(5, line.length);
            assertEquals("1999", line[0]);
            assertEquals("Chevy", line[1]);
            assertEquals("Venture \"Extended Edition\"", line[2]);
            assertEquals("", line[3]);
            assertEquals("4900.00", line[4]);
        }
    }

    final String str = "Year,Make,Model,Description,Price\n" +
            "1997,Ford,E350,\"ac, abs, moon\",3000.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00\n" +
            "1996,Jeep,Grand Cherokee,\"MUST SELL!\n" +
            "air, moon roof, loaded\",4799.00";
}

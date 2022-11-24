package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserTest4 {
    @Test
    public void test() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("states.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());

        int rowCount = CSVParser.rowCount(file);
        assertEquals(53, rowCount);

        CSVParser parser = CSVParser.of(file);
        List<String> columns = parser.readHeader();
        assertEquals(3, columns.size());

        String[] line0 = parser.readLine();
        assertEquals(3, line0.length);
        assertEquals("Alabama", line0[0]);
        assertEquals("Ala.", line0[1]);
        assertEquals("AL", line0[2]);

        String[] line1 = parser.readLine();
        assertEquals(3, line1.length);
        assertEquals("Alaska", line1[0]);
        assertEquals("Alaska", line1[1]);
        assertEquals("AK", line1[2]);

        int rows = 3;
        while (true) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            rows++;
        }
        assertEquals(rowCount, rows);
    }
}

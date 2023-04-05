package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BankListTest {
    static final String file = "csv/banklist.csv";

    @Test
    public void readLineValues() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                String.class,
                String.class,
                String.class,
                Integer.class,

                String.class,
                Date.class,
                Integer.class
        };
        CSVParser parser = CSVParser.of(file, types);
        parser.readHeader();
        int rowCount = 0;
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            rowCount++;
        }
        assertEquals(565, rowCount);
    }

    @Test
    public void readLineValuesUTF16() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                String.class,
                String.class,
                String.class,
                Integer.class,

                String.class,
                Date.class,
                Integer.class
        };
        CSVParser parser = CSVParser.of(new FileReader(file), types);
        parser.readHeader();
        int rowCount = 0;
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            rowCount++;
        }
        assertEquals(565, rowCount);
    }
}

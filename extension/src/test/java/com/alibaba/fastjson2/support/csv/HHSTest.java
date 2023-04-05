package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HHSTest {
    static final String file = "csv/HHS_IDs.csv";

    @Test
    public void readLineValues() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                String.class, // hhs_id
                String.class, // ccn
                String.class, // facility_name
                String.class, // address
                String.class, // city

                String.class, // zip
                Integer.class, // fips_code
                String.class, // state
                String.class, // geohash
                String.class, // geocoded_hospital_address
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
        assertEquals(7354, rowCount);
    }

    @Test
    public void readLineValuesUTF16() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                String.class, // hhs_id
                String.class, // ccn
                String.class, // facility_name
                String.class, // address
                String.class, // city

                String.class, // zip
                Integer.class, // fips_code
                String.class, // state
                String.class, // geohash
                String.class, // geocoded_hospital_address
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
        assertEquals(7354, rowCount);
    }
}

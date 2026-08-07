package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.support.csv.CSVReader;

import java.io.File;
import java.text.NumberFormat;

public class CSVPublicSchoolCharacteristicsTest {
    // EPA_SmartLocationDatabase_V3_Jan_2021_Final.csv
    static final File file = new File("/Users/wenshao/Downloads/Public_School_Characteristics_2020-21.csv");

    public static void statAll() throws Exception {
        System.out.println("file size : " + NumberFormat.getNumberInstance().format(file.length()));
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();

            CSVReader parser = CSVReader.of(file);
            parser.readHeader();
            parser.statAll();
            int rowCount = parser.rowCount();

            long millis = System.currentTimeMillis() - start;
            System.out.println("rowCount : " + rowCount + ", millis " + millis);
        }
        // zulu8.68.0.21 : 185
//        System.out.println("rowCount : " + parser.rowCount());
//        System.out.println("columns : " + JSON.toJSONString(parser.getColumnStats(), JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.PrettyFormat));
    }

    public static void rowCount() throws Exception {
        for (int i = 0; i < 5; i++) {
            long start = System.currentTimeMillis();
            int rowCount = CSVReader.rowCount(file);

            long millis = System.currentTimeMillis() - start;
            System.out.println("rowCount : " + rowCount + ", millis " + millis);
        }
        // zulu8.68.0.21 : 32
    }

    public static void main(String[] args) throws Exception {
        rowCount();
//        statAll();
    }
}

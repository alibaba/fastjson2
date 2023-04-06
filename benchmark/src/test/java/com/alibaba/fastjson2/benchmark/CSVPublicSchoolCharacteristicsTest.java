package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.csv.CSVParser;

import java.io.File;

public class CSVPublicSchoolCharacteristicsTest {
    public static void detect() throws Exception {
        File file = new File("/Users/wenshao/Downloads/EPA_SmartLocationDatabase_V3_Jan_2021_Final.csv");
        CSVParser parser = CSVParser.of(file);
        parser.readHeader();
        parser.statAll();
        System.out.println("rowCount : " + parser.rowCount());
        System.out.println("columns : " + JSON.toJSONString(parser.getColumnStats(), JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.PrettyFormat));
    }

    public static void main(String[] args) throws Exception {
//        readLineValues();
        long start = System.currentTimeMillis();
        detect();
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
    }
}

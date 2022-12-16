package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.support.csv.CSVParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;

public class CSVBig38M {
    @Benchmark
    public void rowCount(Blackhole bh) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        FileInputStream fileIn = new FileInputStream(file);
        int rowCount = CSVParser.rowCount(fileIn);
        bh.consume(rowCount);
    }

    @Benchmark
    public void readLines(Blackhole bh) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        CSVParser parser = CSVParser.of(file);
        int rowCount = 0;
        while (true) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            rowCount++;
        }
        bh.consume(rowCount);
    }

    @Benchmark
    public void readLineValues(Blackhole bh) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, BigDecimal.class
        };
        CSVParser parser = CSVParser.of(file, types);
        int rowCount = 0;
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            rowCount++;
        }
        bh.consume(rowCount);
    }
}

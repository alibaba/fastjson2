package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.csv.CSVParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
}

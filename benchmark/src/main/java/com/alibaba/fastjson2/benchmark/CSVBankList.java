package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.support.csv.CSVReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;

public class CSVBankList {
    static final String file = "csv/banklist.csv";

    @Benchmark
    public void rowCount(Blackhole bh) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        FileInputStream fileIn = new FileInputStream(file);
        int rowCount = CSVReader.rowCount(fileIn);
        bh.consume(rowCount);
    }

    @Benchmark
    public void readLines(Blackhole bh) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        CSVReader parser = CSVReader.of(file);
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
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        Type[] types = new Type[] {
                String.class, String.class, String.class, Integer.class, String.class, Date.class, Integer.class
        };
        CSVReader parser = CSVReader.of(file, types);
        parser.readHeader();
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

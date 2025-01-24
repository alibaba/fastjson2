package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import com.alibaba.fastjson2.support.csv.CSVReader;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

public class CSVBank {
    static final String file = "csv/banklist.csv";
    static byte[] byteArray;
    static {
        String str = UTF8Encode.readFromClasspath(file);
        byteArray = str.getBytes();
    }

    @Benchmark
    public void fastjson2(Blackhole BH) {
        CSVReader
                .of(new ByteArrayInputStream(byteArray), Bank.class)
                .readLineObjectAll(BH::consume);
    }

    @Benchmark
    public void univocity(Blackhole BH) {
        CsvParserSettings settings = new CsvParserSettings();
        CsvRoutines processor = new CsvRoutines(settings);
        settings.getFormat().setLineSeparator("\n");
        settings.setNumberOfRowsToSkip(1);
        processor.iterate(Bank.class, new ByteArrayInputStream(byteArray))
                .forEach(BH::consume);
    }

    public void cainiao(Blackhole BH) {
//        com.cainiao.ai.seq.csv.CsvType.of(Bank.class, false)
//                .csvReader(',')
//                .read(com.cainiao.ai.seq.InputSource.of(byteArray), 1)
//                .supply(BH::consume);
    }

    public static class Bank {
        @Parsed(index = 0)
        public String bankName;

        @Parsed(index = 1)
        public String city;

        @Parsed(index = 2)
        public String state;

        @Parsed(index = 3)
        public Integer cert;

        @Parsed(index = 4)
        public String acquiringInstitution;

        @Parsed(index = 5)
        public String closingDate;

        @Parsed(index = 6)
        public Integer fund;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(CSVBank.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}

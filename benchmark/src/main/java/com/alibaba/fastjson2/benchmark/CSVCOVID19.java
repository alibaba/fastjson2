package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import com.alibaba.fastjson2.support.csv.CSVReader;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class CSVCOVID19 {
    static final String file = "csv/COVID-19_Public_Therapeutic_Locator.csv";
    static byte[] byteArray;
    static {
        try (InputStream is = EishayParseBinary.class.getClassLoader().getResourceAsStream(file)) {
            String str = IOUtils.toString(is, "UTF-8");
            byteArray = str.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void fastjson2(Blackhole BH) {
        CSVReader
                .of(new ByteArrayInputStream(byteArray), Covid19.class)
                .readLineObjectAll(BH::consume);
    }

    @Benchmark
    public void univocity(Blackhole BH) {
        CsvParserSettings settings = new CsvParserSettings();
        CsvRoutines processor = new CsvRoutines(settings);
        settings.getFormat().setLineSeparator("\n");
        settings.setNumberOfRowsToSkip(1);
        processor.iterate(Covid19.class, new ByteArrayInputStream(byteArray))
                .forEach(BH::consume);
    }

    public void cainiao(Blackhole BH) {
//        com.cainiao.ai.seq.csv.CsvType.of(Covid19.class, false)
//                .csvReader(',')
//                .read(com.cainiao.ai.seq.InputSource.of(byteArray), 1)
//                .supply(BH::consume);
    }

    public static class Covid19 {
        @Parsed(index = 0)
        public String providerName;

        @Parsed(index = 1)
        public String address1;

        @Parsed(index = 2)
        public String address2;

        @Parsed(index = 3)
        public String city;

        @Parsed(index = 4)
        public String county;

        @Parsed(index = 5)
        public String stateCode;

        @Parsed(index = 6)
        public Integer zip;

        @Parsed(index = 7)
        public String nationalDrugCode;

        @Parsed(index = 8)
        public String orderLabel;

        @Parsed(index = 9)
        public Integer coursesAvailable;

        @Parsed(index = 10)
        public String geocodedAddress;

        @Parsed(index = 11)
        public String npi;

        @Parsed(index = 12)
        public String lastReportDate;

        @Parsed(index = 13)
        public String providerStatus;

        @Parsed(index = 14)
        public String providerNote;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(CSVCOVID19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .measurementTime(TimeValue.seconds(30))
                .build();
        new Runner(options).run();
    }
}

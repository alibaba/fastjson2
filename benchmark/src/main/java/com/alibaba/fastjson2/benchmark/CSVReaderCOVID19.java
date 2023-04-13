package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import com.alibaba.fastjson2.support.csv.CSVReader;
import com.csvreader.CsvReader;
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
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class CSVReaderCOVID19 {
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
    public void fastjson2(Blackhole BH) throws IOException {
        CSVReader.of(new ByteArrayInputStream(byteArray))
                .readLineObjectAll(BH::consume);
    }

    @Benchmark
    public void csvReader(Blackhole BH) throws IOException {
        CsvReader csvReader = new CsvReader(new InputStreamReader(new ByteArrayInputStream(byteArray)));
        while (true) {
            if (!csvReader.readRecord()) {
                break;
            }
            String[] line = csvReader.getValues();
            BH.consume(line);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(CSVReaderCOVID19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(3)
                .forks(1)
                .measurementTime(TimeValue.seconds(30))
                .build();
        new Runner(options).run();
    }
}

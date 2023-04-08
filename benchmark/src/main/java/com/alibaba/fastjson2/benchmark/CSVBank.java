package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import com.alibaba.fastjson2.support.csv.CSVReader;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CSVBank {
    static final String file = "csv/banklist.csv";
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
        CSVReader reader = CSVReader.of(new ByteArrayInputStream(byteArray), Bank.class);
        reader.readHeader();
        while (true) {
            Bank object = reader.readLineObject();
            if (object == null) {
                break;
            }
            BH.consume(object);
        }
    }

    @Benchmark
    public void univocity(Blackhole BH) {
        CsvParserSettings settings = new CsvParserSettings();
        CsvRoutines processor = new CsvRoutines(settings);
        settings.getFormat().setLineSeparator("\n");
        settings.setNumberOfRowsToSkip(1);
        processor.iterate(Bank.class, new ByteArrayInputStream(byteArray))
                .forEach(t -> BH.consume(t));
    }

    public void cainiao(Blackhole BH) {
//        com.cainiao.ai.seq.csv.CsvType.of(Bank.class, false)
//                .csvReader(',')
//                .read(com.cainiao.ai.seq.InputSource.of(byteArray), 1)
//                .supply(p -> BH.consume(p));
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
}

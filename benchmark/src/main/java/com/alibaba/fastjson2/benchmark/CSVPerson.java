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

public class CSVPerson {
    static final String file = "csv/person.csv";
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
        CSVReader reader = CSVReader.of(new ByteArrayInputStream(byteArray), Person.class);
        reader.readHeader();
        while (true) {
            Person person = reader.readLineObject();
            if (person == null) {
                break;
            }
            BH.consume(person);
        }
    }

    @Benchmark
    public void univocity(Blackhole BH) {
        CsvParserSettings settings = new CsvParserSettings();
        CsvRoutines processor = new CsvRoutines(settings);
        settings.getFormat().setLineSeparator("\n");
        settings.setNumberOfRowsToSkip(1);
        processor.iterate(Person.class, new ByteArrayInputStream(byteArray))
                .forEach(t -> BH.consume(t));
    }

    public void cainiao(Blackhole BH) {
//        com.cainiao.ai.seq.csv.CsvType.of(Person.class, false)
//                .csvReader(',')
//                .read(com.cainiao.ai.seq.InputSource.of(byteArray), 1)
//                .supply(p -> BH.consume(p));
    }

    public static class Person {
        @Parsed(index = 0)
        public String name;
        @Parsed(index = 1)
        public Double weight;
        @Parsed(index = 2)
        public Integer age;
        @Parsed(index = 3)
        public String gender;
        @Parsed(index = 4)
        public Integer height;
        @Parsed(index = 5)
        public String address;
        @Parsed(index = 6)
        public Integer id;
        @Parsed(index = 7)
        public Boolean single;
    }
}

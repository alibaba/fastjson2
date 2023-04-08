package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.eishay.EishayParseBinary;
import com.alibaba.fastjson2.reader.ByteArrayValueConsumer;
import com.alibaba.fastjson2.support.csv.CSVReader;
import com.alibaba.fastjson2.util.TypeUtils;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

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

    static class PersonByteArrayConsumer
            implements ByteArrayValueConsumer {
        Person object;
        final Consumer<Person> consumer;

        public PersonByteArrayConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        public void beforeRow(int row) {
            object = new Person();
        }

        @Override
        public void accept(int row, int column, byte[] bytes, int off, int len, Charset charset) {
            if (column >= 8 || len == 0) {
                return;
            }

            switch (column) {
                case 0:
                    object.name = new String(bytes, off, len, charset);
                    break;
                case 1:
                    object.weight = TypeUtils.parseDouble(bytes, off, len);
                    break;
                case 2:
                    object.age = TypeUtils.parseInt(bytes, off, len);
                    break;
                case 3:
                    object.gender = new String(bytes, off, len, charset);
                    break;
                case 4:
                    object.height = TypeUtils.parseInt(bytes, off, len);
                    break;
                case 5:
                    object.address = new String(bytes, off, len, charset);
                    break;
                case 6:
                    object.id = TypeUtils.parseInt(bytes, off, len);
                    break;
                case 7:
                    object.single = TypeUtils.parseBoolean(bytes, off, len);
                    break;
                default:
                    break;
            }
        }

        public void afterRow(int row) {
            consumer.accept(object);
            object = null;
        }
    }

    @Benchmark
    public void fastjson2(Blackhole BH) {
        CSVReader
                .of(new ByteArrayInputStream(byteArray), StandardCharsets.UTF_8, Person.class)
                .readLineObjectAll(BH::consume);
    }

    @Benchmark
    public void univocity(Blackhole BH) {
        CsvParserSettings settings = new CsvParserSettings();
        CsvRoutines processor = new CsvRoutines(settings);
        settings.getFormat().setLineSeparator("\n");
        settings.setNumberOfRowsToSkip(1);
        processor.iterate(Person.class, new ByteArrayInputStream(byteArray))
                .forEach(BH::consume);
    }

    public void cainiao(Blackhole BH) {
//        com.cainiao.ai.seq.csv.CsvType.of(Person.class, false)
//                .csvReader(',')
//                .read(com.cainiao.ai.seq.InputSource.of(byteArray), 1)
//                .supply(BH::consume);
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

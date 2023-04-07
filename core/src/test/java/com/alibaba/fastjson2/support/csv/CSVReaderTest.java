package com.alibaba.fastjson2.support.csv;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVReaderTest {
    String[][] lines = new String[][]{
            new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00"},
            new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\n" +
                    "air, moon roof, loaded", "4799.00"},
    };

    @Test
    public void test0() {
        CSVReader parser = CSVReader.of(str);
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        for (int i = 0; ; ++i) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            assertArrayEquals(lines[i], line);
        }
    }

    @Test
    public void test0Chars() {
        CSVReader parser = CSVReader.of(str.toCharArray());
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        for (int i = 0; ; ++i) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            assertArrayEquals(lines[i], line);
        }
    }

    @Test
    public void test0Bytes() {
        CSVReader parser = CSVReader.of(str.getBytes(StandardCharsets.UTF_8));
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        for (int i = 0; ; ++i) {
            String[] line = parser.readLine();
            if (line == null) {
                break;
            }
            assertArrayEquals(lines[i], line);
        }
    }

    @Test
    public void testBytes() {
        CSVReader parser = CSVReader.of(str.getBytes());
        List<String> columns = parser.readHeader();
        assertEquals(5, columns.size());

        {
            String[] line = parser.readLine();
            assertEquals(5, line.length);
            assertEquals("1997", line[0]);
            assertEquals("Ford", line[1]);
            assertEquals("E350", line[2]);
            assertEquals("ac, abs, moon", line[3]);
            assertEquals("3000.00", line[4]);
        }
        {
            String[] line = parser.readLine();
            assertEquals(5, line.length);
            assertEquals("1999", line[0]);
            assertEquals("Chevy", line[1]);
            assertEquals("Venture \"Extended Edition\"", line[2]);
            assertEquals("", line[3]);
            assertEquals("4900.00", line[4]);
        }
    }

    @Test
    public void testFile() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16BE
        };

        for (Charset charset : charsets) {
            File file = File.createTempFile("abc", "txt");
            FileOutputStream out = new FileOutputStream(file);
            out.write(str.getBytes(charset));
            out.flush();
            out.close();

            CSVReader parser = CSVReader.of(file, charset);
            List<String> columns = parser.readHeader();
            assertEquals(5, columns.size());

            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }

            parser.close();
        }
    }

    @Test
    public void testFileObject() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16BE
        };

        for (Charset charset : charsets) {
            File file = File.createTempFile("abc", "txt");
            FileOutputStream out = new FileOutputStream(file);
            out.write(str.getBytes(charset));
            out.flush();
            out.close();

            CSVReader parser = CSVReader.of(file, charset, Item.class);
            List<String> columns = parser.readHeader();
            assertEquals(5, columns.size());

            for (int i = 0; ; ++i) {
                Item item = parser.readLineObject();
                if (item == null) {
                    break;
                }
                String[] line = new String[] {item.year, item.make, item.model, item.description, item.price.toString()};
                assertArrayEquals(lines[i], line);
            }

            parser.close();
        }
    }

    @Test
    public void testInputStreamFile() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16BE
        };

        for (Charset charset : charsets) {
            byte[] bytes = str.getBytes(charset);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            CSVReader parser = CSVReader.of(in, charset);
            List<String> columns = parser.readHeader();
            assertEquals(5, columns.size());

            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
    }

    public static <T> Stream<T> stream(Consumer<Consumer<? super T>> consumer) {
        Iterator<T> iterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T next() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                consumer.accept(action);
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static <T> Stream<T> readAsStream(CSVReader parser) {
        return readAsStream(parser, Integer.MAX_VALUE);
    }

    public static <T> Stream<T> readAsStream(CSVReader parser, int limit) {
        return stream(c -> {
            T t;
            for (int left = limit; left > 0 && (t = parser.readLineObject()) != null; left--) {
                c.accept(t);
            }
        });
    }

    @Test
    public void testFileAsStream() {
        CSVReader csvReader = CSVReader.of(getClass().getResourceAsStream("/person.csv"), Person.class);
        System.out.println(csvReader.readHeader());
        Stream<Person> stream = readAsStream(csvReader);
        stream.forEach(System.out::println);
    }

    @Test
    public void testFileAsStream1() {
        CSVReader csvReader = CSVReader.of(new InputStreamReader(getClass().getResourceAsStream("/person.csv")), Person.class);
        System.out.println(csvReader.readHeader());
        Stream<Person> stream = readAsStream(csvReader);
        stream.forEach(System.out::println);
    }

    final String str = "Year,Make,Model,Description,Price\n" +
            "1997,Ford,E350,\"ac, abs, moon\",3000.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00\n" +
            "1996,Jeep,Grand Cherokee,\"MUST SELL!\n" +
            "air, moon roof, loaded\",4799.00";

    public static class Item {
        public String year;
        public String make;
        public String model;
        public String description;
        public BigDecimal price;
    }

    @Data
    static class Person {
        private String name;
        private String weight;
        private String age;
    }
}

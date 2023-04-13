package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVTest2 {
    @Test
    public void testRowCount1() {
        String[] strings = new String[]{
                "abc",
                "abc\n",
                "1997,Ford,E350,\"Go get one now\n" +
                        "they are going fast\"",
                "1997,Ford,E350,\"Go get one now\n" +
                        "they are going fast\"\n",
                "1997,Ford,E350,\"Go get one now\n" +
                        "they are going fast\"\r\n",
                "1997,Ford,E350,\"Go get one now\n" +
                        "they are going fast\"\n\n",
                "1997,Ford,E350,\"Go get one now\n" +
                        "they are going fast\"\n\n\n",
                "1997,Ford,E350,\"Go get one now\r\n" +
                        "they are going fast\"\r\n",
                "1997,Ford,E350,\"Go get one now\r\n" +
                        "they are going fast\"\r"
        };

        for (String string : strings) {
            assertEquals(
                    1,
                    CSVReader.rowCount(string, CSVReader.Feature.IgnoreEmptyLine),
                    string
            );
        }

        for (String string : strings) {
            assertEquals(
                    1,
                    CSVReader.rowCount(
                            string.toCharArray(),
                            CSVReader.Feature.IgnoreEmptyLine
                    ),
                    string
            );
        }

        for (String string : strings) {
            assertEquals(1, CSVReader.rowCount(string.getBytes(), CSVReader.Feature.IgnoreEmptyLine));
        }
    }

    @Test
    public void testRowCount2() {
        String[] strings = new String[]{
                "\"State\",\"Abbrev\",\"Code\"\n\n\"Alabama\",\"Ala.\",\"AL\"",
                "\"State\",\"Abbrev\",\"Code\"\n\"Alabama\",\"Ala.\",\"AL\"\n",
                "\"State\",\"Abbrev\",\"Code\"\r\n\"Alabama\",\"Ala.\",\"AL\"\r\n",
                "\"State\",\"Abbrev\",\"Code\"\n\"Alabama\",\"Ala.\",\"AL\"\n",
                "\"State\",\"Abbrev\",\"Code\"\n\"Alabama\",\"Ala.\",\"AL\"\n"
        };
        for (String string : strings) {
            assertEquals(2, CSVReader.rowCount(string, CSVReader.Feature.IgnoreEmptyLine));
        }

        for (String string : strings) {
            assertEquals(2, CSVReader.rowCount(string.toCharArray(), CSVReader.Feature.IgnoreEmptyLine));
        }

        for (String string : strings) {
            assertEquals(2, CSVReader.rowCount(string.getBytes(StandardCharsets.UTF_8), CSVReader.Feature.IgnoreEmptyLine));
        }
    }

    @Test
    public void testRowCount() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("states.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        assertEquals(53, CSVReader.rowCount(file));
    }

    @Test
    public void testRowCountFile() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv.zip");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        FileInputStream fileIn = new FileInputStream(file);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        zipIn.getNextEntry();
        assertEquals(496774, CSVReader.rowCount(zipIn));
    }

    @Test
    public void testReadLines() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv.zip");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        FileInputStream fileIn = new FileInputStream(file);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        zipIn.getNextEntry();

        int rowCount = 0;
        CSVReader parser = CSVReader.of(zipIn);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Integer id = TypeUtils.toIntValue(line[0]);
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void testReadLines1() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv.zip");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        FileInputStream fileIn = new FileInputStream(file);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        zipIn.getNextEntry();

        InputStreamReader inputReader = new InputStreamReader(zipIn);

        int rowCount = 0;
        CSVReader parser = CSVReader.of(inputReader);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Integer id = TypeUtils.toIntValue(line[0]);
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void testReadLines2() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        long start = System.currentTimeMillis();
        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(file);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Integer id = TypeUtils.toIntValue(line[0]);
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);

        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
    }

    @Test
    public void testReadLines3() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        long start = System.currentTimeMillis();
        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(new FileReader(file));
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Integer id = TypeUtils.toIntValue(line[0]);
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);

        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
    }

    @Test
    public void readLineValues() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        Type[] types = new Type[] {
                Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, BigDecimal.class
        };

        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(file, types);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Integer id = (Integer) line[0];
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void readLineValues1() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        Type[] types = new Type[] {
                Long.class, Integer.class, Integer.class, String.class, String.class, String.class, BigDecimal.class
        };

        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(file, types);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Long id = (Long) line[0];
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void readLineValues2() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        Type[] types = new Type[] {
                Long.class, Long.class, Long.class, String.class, String.class, String.class, Float.class
        };

        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(file, types);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Long id = (Long) line[0];
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void readLineValues3() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        Type[] types = new Type[] {
                Long.class, Long.class, Long.class, String.class, String.class, String.class, Double.class
        };

        File file = new File(resource.getFile());
        int rowCount = 0;
        CSVReader parser = CSVReader.of(file, types);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }
            Long id = (Long) line[0];
            if (rowCount < 41214) {
                assertEquals(rowCount, id);
            }
            rowCount++;
        }
        assertEquals(496774, rowCount);
    }

    @Test
    public void readLineValues3ReadAndWrite() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("organised_Gen.csv");
        if (resource == null) {
            return;
        }

        Type[] types = new Type[] {
                Long.class, Long.class, Long.class, String.class, String.class, String.class, Double.class
        };

        File file = new File(resource.getFile());
        File tempFile = File.createTempFile("fastjson", "csv");

        int rowCount = 0;
        CSVReader parser = CSVReader.of(file, types);
        CSVWriter writer = CSVWriter.of(tempFile);
        while (true) {
            Object[] line = parser.readLineValues();
            if (line == null) {
                break;
            }

            writer.writeLine(line);
            rowCount++;
        }
        writer.close();

        assertEquals(496774, rowCount);
    }
}

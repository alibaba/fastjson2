package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserTest {
    String[][] lines = new String[][]{
            new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00"},
            new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\n" +
                    "air, moon roof, loaded", "4799.00"},
    };

    @Test
    public void test0() {
        CSVParser parser = CSVParser.of(str);
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
        CSVParser parser = CSVParser.of(str.toCharArray());
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
        CSVParser parser = CSVParser.of(str.getBytes(StandardCharsets.UTF_8));
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
        CSVParser parser = CSVParser.of(str.getBytes());
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

            CSVParser parser = CSVParser.of(file, charset);
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

            CSVParser parser = CSVParser.of(in, charset);
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

    final String str = "Year,Make,Model,Description,Price\n" +
            "1997,Ford,E350,\"ac, abs, moon\",3000.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00\n" +
            "1996,Jeep,Grand Cherokee,\"MUST SELL!\n" +
            "air, moon roof, loaded\",4799.00";
}

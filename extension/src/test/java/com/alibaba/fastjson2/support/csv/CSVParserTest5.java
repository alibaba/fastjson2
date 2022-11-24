package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVParserTest5 {
    @Test
    public void testInputStreamFile() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII
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

    @Test
    public void testInputStreamFile2() throws Exception {
        Charset[] charsets = new Charset[] {
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

    @Test
    public void testInputStreamFileW() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII
        };

        for (Charset charset : charsets) {
            byte[] bytes = strW.getBytes(charset);
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

    @Test
    public void testInputStreamFile2W() throws Exception {
        Charset[] charsets = new Charset[] {
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16BE
        };

        for (Charset charset : charsets) {
            byte[] bytes = strW.getBytes(charset);
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

    String[][] lines = new String[][]{
            new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00"},
            new String[]{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00"},
            new String[]{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\n" +
                    "air, moon roof, loaded", "4799.00"},
            new String[5],
    };

    final String str = "Year,Make,Model,Description,Price\n" +
            "1997,Ford,E350,\"ac, abs, moon\",3000.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00\n" +
            "1996,Jeep,Grand Cherokee,\"MUST SELL!\n" +
            "air, moon roof, loaded\",4799.00\n\n";

    final String strW = "Year,Make,Model,Description,Price\r\n" +
            "1997,Ford,E350,\"ac, abs, moon\",3000.00\r\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00\r\n" +
            "1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00\r\n" +
            "1996,Jeep,Grand Cherokee,\"MUST SELL!\n" +
            "air, moon roof, loaded\",4799.00\r\n\r\n";
}

package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CSVParserTest1 {
    @Test
    public void test() {
        String str = "1997,Ford,E350";

        String[][] lines = new String[][]{
                new String[]{"1997", "Ford", "E350"}
        };

        {
            CSVParser parser = CSVParser.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVParser parser = CSVParser.of(str.getBytes());
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
    public void test1() {
        String str = "1997,Ford,E350\n";
        String[][] lines = new String[][]{
                new String[]{"1997", "Ford", "E350"}
        };

        {
            CSVParser parser = CSVParser.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVParser parser = CSVParser.of(str.getBytes());
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
    public void test2() {
        String str = "\"1997\",\"Ford\",\"E350\"\n";
        String[][] lines = new String[][]{
                new String[]{"1997", "Ford", "E350"}
        };

        {
            CSVParser parser = CSVParser.of(str.toCharArray());
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVParser parser = CSVParser.of(str.getBytes());
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
    public void test3() {
        String str = "1997, Ford, E350";

        String[][] lines = new String[][]{
                new String[]{"1997", " Ford", " E350"}
        };

        {
            CSVParser parser = CSVParser.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVParser parser = CSVParser.of(str.getBytes());
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
    }
}

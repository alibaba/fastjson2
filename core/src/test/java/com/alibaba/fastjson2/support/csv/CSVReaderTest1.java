package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CSVReaderTest1 {
    @Test
    public void test() {
        String str = "1997,Ford,E350";

        String[][] lines = new String[][]{
                new String[]{"1997", "Ford", "E350"}
        };

        {
            CSVReader parser = CSVReader.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVReader parser = CSVReader.of(str.getBytes());
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
            CSVReader parser = CSVReader.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVReader parser = CSVReader.of(str.getBytes());
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
            CSVReader parser = CSVReader.of(str.toCharArray());
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVReader parser = CSVReader.of(str.getBytes());
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
            CSVReader parser = CSVReader.of(str);
            for (int i = 0; ; ++i) {
                String[] line = parser.readLine();
                if (line == null) {
                    break;
                }
                assertArrayEquals(lines[i], line);
            }
        }
        {
            CSVReader parser = CSVReader.of(str.getBytes());
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

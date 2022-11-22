package com.alibaba.fastjson2.csv;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVTest2 {
    @Test
    public void testRowCount1() {
        String[] strings = new String[] {
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
            assertEquals(1, CSVParser.rowCount(string));
        }

        for (String string : strings) {
            assertEquals(1, CSVParser.rowCount(string.toCharArray()));
        }

        for (String string : strings) {
            assertEquals(1, CSVParser.rowCount(string.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void testRowCount2() {
        String[] strings = new String[]{
                "\"State\",\"Abbrev\",\"Code\"\n" +
                        "\"Alabama\",\"Ala.\",\"AL\"",
                "\"State\",\"Abbrev\",\"Code\"\n" +
                        "\"Alabama\",\"Ala.\",\"AL\"\n",
                "\"State\",\"Abbrev\",\"Code\"\r\n" +
                        "\"Alabama\",\"Ala.\",\"AL\"\r\n",
                "\"State\",\"Abbrev\",\"Code\"\n" +
                        "\"Alabama\",\"Ala.\",\"AL\"\n\n",
                "\"State\",\"Abbrev\",\"Code\"\n" +
                        "\"Alabama\",\"Ala.\",\"AL\"\n\n\n"
        };
        for (String string : strings) {
            assertEquals(2, CSVParser.rowCount(string));
        }

        for (String string : strings) {
            assertEquals(2, CSVParser.rowCount(string.toCharArray()));
        }

        for (String string : strings) {
            assertEquals(2, CSVParser.rowCount(string.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void testRowCount() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("states.csv");
        if (resource == null) {
            return;
        }

        File file = new File(resource.getFile());
        assertEquals(52, CSVParser.rowCount(file));
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
        assertEquals(496774, CSVParser.rowCount(zipIn));
    }
}

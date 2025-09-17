package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.support.csv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Issue3784 {
    @Test
    public void test() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("issue3784.csv");
        try (CSVReader reader = CSVReader.of(inputStream, StandardCharsets.UTF_8, String.class)) {
            reader.readLineAll();
        }

        InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream("issue3784.csv");
        try (CSVReader reader = CSVReader.of(inputStream2, StandardCharsets.UTF_16, String.class)) {
            reader.readLineAll();
        }
    }
}

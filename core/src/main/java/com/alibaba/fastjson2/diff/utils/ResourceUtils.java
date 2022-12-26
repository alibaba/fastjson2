package com.alibaba.fastjson2.diff.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class ResourceUtils {

    public static String loadResourceLine(String resourcePath) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourcePath);
        Objects.requireNonNull(inputStream);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing file.", e);
        }
        return content.toString();
    }


}

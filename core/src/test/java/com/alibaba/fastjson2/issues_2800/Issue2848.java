package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.support.csv.CSVWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class Issue2848 {
    @Test
    public void test() throws Exception {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 65531; i++) {
            buf.append('1');
        }

        String str = buf.toString();

        try (CSVWriter writer = CSVWriter.of()) {
            writer.writeValue(str);
            writer.writeComma();
            writer.writeValue(new BigDecimal("1.00"));
            writer.writeComma();
        }

        try (CSVWriter writer = CSVWriter.of(new ByteArrayOutputStream(), StandardCharsets.UTF_16)) {
            writer.writeValue(str);
            writer.writeComma();
            writer.writeValue(new BigDecimal("1.00"));
            writer.writeComma();
        }
    }
}

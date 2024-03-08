package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSONWriter;
import org.openjdk.jmh.infra.Blackhole;

import java.time.LocalDate;

public class LocalDateBench {
    private static final LocalDate VALUE = LocalDate.of(2024, 2, 20);

    public void utf8(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();

        jsonWriter.startArray();
        for (int i = 0; i < 1000; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeLocalDate(VALUE);
        }
        jsonWriter.endArray();

        BH.consume(jsonWriter.getBytes());
        jsonWriter.close();
    }

    public void utf16(Blackhole BH) {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();

        jsonWriter.startArray();
        for (int i = 0; i < 1000; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeLocalDate(VALUE);
        }
        jsonWriter.endArray();

        BH.consume(jsonWriter.toString());
        jsonWriter.close();
    }
}

package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue114 {
    @Test
    public void jsonPathTest() {
        String seatString = "{\n" +
                "\t\"flightId\":\"MH8633\",\n" +
                "\t\"column\":\"C\",\n" +
                "\t\"row\":\"19\"\n" +
                "}";
        JSONPath rowPath = JSONPath.of("$.row");
        JSONPath colPath = JSONPath.of("$.column");
        String row = (String)rowPath.extract(JSONReader.of(seatString));
        assertEquals("19", row);

        String col = (String)colPath.extract(JSONReader.of(seatString));
        assertEquals("C", col);
    }
}

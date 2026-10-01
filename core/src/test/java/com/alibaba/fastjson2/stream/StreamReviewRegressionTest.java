package com.alibaba.fastjson2.stream;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.support.csv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class StreamReviewRegressionTest {
    @Test
    public void finalJsonRecordWithoutNewline() {
        for (Charset charset : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16}) {
            byte[] input = "{\"id\":1}\n{\"id\":2}".getBytes(charset);
            JSONStreamReader reader = JSONStreamReader.of(new ByteArrayInputStream(input), charset);
            List<JSONObject> records = (List<JSONObject>) reader.stream().collect(Collectors.toList());
            assertEquals(2, records.size());
            assertEquals(2, records.get(1).getIntValue("id"));
        }
    }

    @Test
    public void untypedCsvStreamAndEof() throws Exception {
        try (CSVReader reader = CSVReader.of("a,b\nc,d")) {
            List<Object[]> rows = (List<Object[]>) reader.stream().collect(Collectors.toList());
            assertEquals(2, rows.size());
            assertArrayEquals(new String[]{"c", "d"}, rows.get(1));
        }
        try (CSVReader reader = CSVReader.of("a,b\nc,d\n")) {
            List<Object[]> rows = (List<Object[]>) reader.stream(Object[].class).collect(Collectors.toList());
            assertEquals(2, rows.size());
        }
        try (CSVReader reader = CSVReader.of("")) {
            assertEquals(0, reader.stream(Object[].class).count());
        }
    }

    @Test
    public void countEachStatValueOnce() {
        JSONStreamReader reader = JSONStreamReader.of(new ByteArrayInputStream(new byte[0]), StandardCharsets.UTF_8);
        reader.statLine(JSONObject.of("value", "123"));
        reader.statLine(JSONObject.of("value", null));
        StreamReader.ColumnStat stat = reader.getColumnStat("value");
        assertEquals(2, stat.values);
        assertEquals(1, stat.nulls);
        assertEquals(1, stat.integers);
        assertEquals(Integer.class, stat.getInferType());
    }

    @Test
    public void arrayStatsUseElementPaths() {
        JSONStreamReader reader = JSONStreamReader.of(new ByteArrayInputStream(new byte[0]), StandardCharsets.UTF_8);
        reader.statLine(JSONObject.of("items", JSONArray.of(1, true)));
        assertEquals(1, reader.getColumnStat("items").values);
        assertEquals(1, reader.getColumnStat("items").arrays);
        assertEquals(1, reader.getColumnStat("items[0]").integers);
        assertEquals(1, reader.getColumnStat("items[1]").booleans);
    }
}

package com.alibaba.fastjson2.flink;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonRowDeserializationSchemaTest {
    @Test
    public void test() {
        long id = 1238123899121L;
        String name = "asdlkjasjkdla998y1122";
        byte[] bytes = new byte[1024];
        ThreadLocalRandom.current().nextBytes(bytes);
        Timestamp timestamp = Timestamp.valueOf("1990-10-14 12:12:43");
        Date date = Date.valueOf("1990-10-14");
        Time time = Time.valueOf("12:12:43");

        Map<String, Long> map = new HashMap<>();
        map.put("flink", 123L);

        Map<String, Map<String, Integer>> nestedMap = new HashMap<>();
        Map<String, Integer> innerMap = new HashMap<>();
        innerMap.put("key", 234);
        nestedMap.put("inner_map", innerMap);

        JSONObject root = new JSONObject();
        root.put("id", id);
        root.put("name", name);
        root.put("bytes", bytes);
        root.put("date1", "1990-10-14");
        root.put("date2", "1990-10-14");
        root.put("time1", "12:12:43Z");
        root.put("time2", "12:12:43Z");
        root.put("timestamp1", "1990-10-14T12:12:43Z");
        root.put("timestamp2", "1990-10-14T12:12:43Z");
        root.putObject("map").put("flink", 123);
        root.putObject("map2map").putObject("inner_map").put("key", 234);

        String[] names = new String[]{
                "id",
                "name",
                "bytes",
                "date1",
                "date2",
                "time1",
                "time2",
                "timestamp1",
                "timestamp2",
                "map",
                "map2map"
        };
        Type[] types = new Type[]{
                Long.class,
                String.class,
                byte[].class,
                java.sql.Date.class,
                LocalDate.class,
                java.sql.Time.class,
                LocalTime.class,
                java.sql.Timestamp.class,
                LocalDateTime.class,
                TypeReference.mapType(String.class, Long.class),
                TypeReference.mapType(String.class, TypeReference.mapType(String.class, Integer.class))
        };
        ObjectReader<Row> objectReader = JSONFactory.getDefaultObjectReaderProvider()
                .createObjectReader(
                        names,
                        types,
                        () -> new Row(11),
                        (r, i, v) -> r.setField(i, v)
                );

        byte[] serializedJson = JSON.toJSONBytes(root);

        Row row = new Row(11);
        row.setField(0, id);
        row.setField(1, name);
        row.setField(2, bytes);
        row.setField(3, date);
        row.setField(4, date.toLocalDate());
        row.setField(5, time);
        row.setField(6, time.toLocalTime());
        row.setField(7, timestamp);
        row.setField(8, timestamp.toLocalDateTime());
        row.setField(9, map);
        row.setField(10, nestedMap);

        ObjectWriter<Row> objectWriter = JSONFactory.getDefaultObjectWriterProvider()
                .getCreator()
                .createObjectWriter(
                        names,
                        types,
                        (Row r, int i) -> r.getField(i)
                );

        JSONReader jsonReader = JSONReader.of(serializedJson);
        Row row1 = objectReader.readObject(jsonReader);

        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, row);

        String str = objectWriter.toJSONString(row, JSONWriter.Feature.PrettyFormat);
        String str1 = objectWriter.toJSONString(row1, JSONWriter.Feature.PrettyFormat);

        System.out.println(str);
        System.out.println(str1);
        assertEquals(str, str1);
    }
}

package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONStreamingTest {
    @Test
    public void test_stream_utf8() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("test1.txt");
        assertNotNull(is);

        List list = new ArrayList<>();
        Consumer consumer = (Object row) -> {
            list.add(row);
        };
        JSON.parseObject(is, StandardCharsets.UTF_8, '\n', JSONObject.class, consumer);

        assertEquals(3, list.size());

        assertEquals("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}", list.get(0).toString());
        assertEquals("{\"one\":false,\"three\":[\"red\",\"yellow\",[\"citrus\",\"blue\",\"cobalt\"],\"black\"],\"two\":129.5,\"four\":\"stars\"}", list.get(1).toString());
        assertEquals("{\"one\":false,\"three\":[\"pink\",[\"citrus\",\"blue\"],\"gold\"],\"two\":222,\"four\":\"fiat\"}", list.get(2).toString());
    }

    @Test
    public void test_stream_utf8_2() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("test1.txt");
        assertNotNull(is);

        List list = new ArrayList<>();
        Consumer consumer = (Object row) -> {
            list.add(row);
        };
        JSON.parseObject(is, JSONObject.class, consumer);

        assertEquals(3, list.size());

        assertEquals("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}", list.get(0).toString());
        assertEquals("{\"one\":false,\"three\":[\"red\",\"yellow\",[\"citrus\",\"blue\",\"cobalt\"],\"black\"],\"two\":129.5,\"four\":\"stars\"}", list.get(1).toString());
        assertEquals("{\"one\":false,\"three\":[\"pink\",[\"citrus\",\"blue\"],\"gold\"],\"two\":222,\"four\":\"fiat\"}", list.get(2).toString());
    }

    @Test
    public void test_stream_ascii() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("test1.txt");
        assertNotNull(is);

        List list = new ArrayList<>();
        Consumer consumer = (Object row) -> {
            list.add(row);
        };
        JSON.parseObject(is, StandardCharsets.US_ASCII, '\n', JSONObject.class, consumer);

        assertEquals(3, list.size());

        assertEquals("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}", list.get(0).toString());
        assertEquals("{\"one\":false,\"three\":[\"red\",\"yellow\",[\"citrus\",\"blue\",\"cobalt\"],\"black\"],\"two\":129.5,\"four\":\"stars\"}", list.get(1).toString());
        assertEquals("{\"one\":false,\"three\":[\"pink\",[\"citrus\",\"blue\"],\"gold\"],\"two\":222,\"four\":\"fiat\"}", list.get(2).toString());
    }

    @Test
    public void test_stream_reader() {
        Reader is = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("test1.txt"));
        assertNotNull(is);

        List list = new ArrayList<>();
        Consumer consumer = (Object row) -> {
            list.add(row);
        };
        JSON.parseObject(is, '\n', JSONObject.class, consumer);

        assertEquals(3, list.size());

        assertEquals("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}", list.get(0).toString());
        assertEquals("{\"one\":false,\"three\":[\"red\",\"yellow\",[\"citrus\",\"blue\",\"cobalt\"],\"black\"],\"two\":129.5,\"four\":\"stars\"}", list.get(1).toString());
        assertEquals("{\"one\":false,\"three\":[\"pink\",[\"citrus\",\"blue\"],\"gold\"],\"two\":222,\"four\":\"fiat\"}", list.get(2).toString());
    }
}

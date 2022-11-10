package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReader_map {
    @Test
    public void test_array() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("[{\"id\":123}]"));

        reader.startArray();

        Map<String, Object> map = new HashMap<String, Object>();
        reader.readObject(map);

        assertEquals(123, map.get("id"));
        reader.endArray();
        reader.close();
    }

    @Test
    public void test_map() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("{\"id\":123}"));
        Map<String, Object> map = new HashMap<String, Object>();
        reader.readObject(map);
        assertEquals(123, map.get("id"));
        reader.close();
    }
}

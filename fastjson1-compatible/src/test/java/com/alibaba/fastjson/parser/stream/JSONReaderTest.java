package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONReaderTest {
    @Test
    public void test_read() throws Exception {
        String resource = "2.json";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

        JSONReader reader = new JSONReader(new InputStreamReader(is, "UTF-8"));

        reader.startObject();

        assertEquals("company", reader.readString());
        assertTrue(reader.readObject() instanceof JSONObject);

        assertEquals("count", reader.readString());
        assertEquals(5, reader.readObject());

        assertEquals("pagecount", reader.readString());
        assertEquals(0, reader.readObject());

        assertEquals("pageindex", reader.readString());
        assertEquals(0, reader.readObject());

        assertEquals("resultList", reader.readString());
        assertTrue(reader.readObject() instanceof JSONArray);

        assertEquals("totalCount", reader.readString());
        assertEquals(0, reader.readObject());

        reader.endObject();

        reader.close();
    }
}

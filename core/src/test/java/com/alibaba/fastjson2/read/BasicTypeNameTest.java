package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTypeNameTest {
    @Test
    public void test0() {
        String json = "[1,2L,3F,4D]";
        JSONArray array = JSON.parseArray(json);
        assertEquals(4, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
        assertEquals(4, array.get(3));

        JSONArray array2 = JSON.parseArray(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(4, array2.size());
        assertEquals(1, array2.get(0));
        assertEquals(2, array2.get(1));
        assertEquals(3, array2.get(2));
        assertEquals(4, array2.get(3));

        JSONReader jsonReaderStr = TestUtils.createJSONReaderStr(json);
        List array3 = jsonReaderStr.readArray();
        assertEquals(4, array3.size());
        assertEquals(1, array3.get(0));
        assertEquals(2, array3.get(1));
        assertEquals(3, array3.get(2));
        assertEquals(4, array3.get(3));
    }

    @Test
    public void test1() {
        String[] strings = new String[]{"1", "2L", "3F", "4D"};

        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    Object o = jsonReader.readAny();
                    assertEquals(i + 1, o);
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    int o = jsonReader.readInt32();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    int o = jsonReader.readInt32Value();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    long o = jsonReader.readInt64();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    long o = jsonReader.readInt64Value();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    float o = jsonReader.readFloat();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    float o = jsonReader.readFloatValue();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    double o = jsonReader.readDouble();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
            {
                JSONReader[] jsonReaders = TestUtils.createJSONReaders(string);
                for (JSONReader jsonReader : jsonReaders) {
                    double o = jsonReader.readDoubleValue();
                    assertEquals(i + 1, o);
                    assertTrue(jsonReader.isEnd());
                }
            }
        }
    }
}

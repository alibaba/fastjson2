package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreNullPropertyValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IgnoreNullPropertyValueTest {
    @Test
    public void test() {
        String str = "{\"val\":null}";
        assertTrue(JSON.parseObject(str, IgnoreNullPropertyValue).isEmpty());

        {
            HashMap<Object, Object> map = new HashMap<>();
            JSONReader.of(str).read(map, IgnoreNullPropertyValue.mask);
            assertTrue(map.isEmpty());
        }

        {
            HashMap<Object, Object> map = new HashMap<>();
            JSONReader.of(str, JSONFactory.createReadContext(IgnoreNullPropertyValue)).read(map, 0);
            assertTrue(map.isEmpty());
        }
    }
}

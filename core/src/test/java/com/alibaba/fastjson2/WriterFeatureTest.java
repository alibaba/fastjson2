package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.AtomicIntegerArray3;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteMapNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class WriterFeatureTest {
    @Test
    public void test_0() {
        AtomicIntegerArray3 vo = new AtomicIntegerArray3();
        vo.setV0000(new AtomicIntegerArray(1));
        vo.setV0002(new AtomicIntegerArray(2));

        {
            String str = JSON.toJSONString(vo, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
            System.out.println(str);
            assertEquals("{\"v0000\":[0],\"v0001\":[],\"v0002\":[0,0]}", str);

            Map<String, Object> map = JSON.parseObject(str);
            assertEquals(3, map.size());
            assertNotNull(map.get("v0000"));
            assertNotNull(map.get("v0001"));
            assertNotNull(map.get("v0002"));
        }

        {
            String str = JSON.toJSONString(vo);

            Map<String, Object> map = JSON.parseObject(str);
            assertEquals(2, map.size());
            assertNotNull(map.get("v0000"));
            assertNotNull(map.get("v0002"));
        }

        {
            String str = JSON.toJSONString(vo, JSONWriter.Feature.WriteNulls);

            Map<String, Object> map = JSON.parseObject(str);
            assertEquals(3, map.size());
            assertNotNull(map.get("v0000"));
            assertNull(map.get("v0001"));
            assertNotNull(map.get("v0002"));
        }
    }

    @Test
    public void test_1() {
        JSONWriter.Feature[] values = JSONWriter.Feature.values();
        int bits = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == WriteMapNullValue) {
                continue;
            }
            assertEquals(1L << bits, values[i].mask);
            bits++;
        }
    }
}

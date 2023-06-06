package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.MapMultiValueType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapMultiValueTypeTest {
    @Test
    public void test() {
        String str = "{\"data\":{\"id\":123}}";
        JSONObject jsonObject = JSON.parseObject(str, MapMultiValueType.of("data", Bean.class));
        Bean bean = (Bean) jsonObject.get("data");
        assertEquals(123, bean.id);
    }

    @Test
    public void testUTF8() {
        String str = "{\"data\":{\"id\":123}}";
        JSONObject jsonObject = JSON.parseObject(str.getBytes(), MapMultiValueType.of("data", Bean.class));
        Bean bean = (Bean) jsonObject.get("data");
        assertEquals(123, bean.id);
    }

    @Test
    public void testx() {
        String str = "{\"data\":{\"id\":123}}";
        LinkedHashMap map = JSON.parseObject(str, MapMultiValueType.of(LinkedHashMap.class, "data", Bean.class));
        Bean bean = (Bean) map.get("data");
        assertEquals(123, bean.id);
    }

    @Test
    public void testy() {
        String str = "{\"data\":{\"id\":123}}";
        com.alibaba.fastjson.JSONObject map = JSON.parseObject(str, MapMultiValueType.of(com.alibaba.fastjson.JSONObject.class, "data", Bean.class));
        Bean bean = (Bean) map.get("data");
        assertEquals(123, bean.id);
    }

    public static class Bean {
        public int id;
    }

    @Test
    public void test1() {
        String str = "{\"data\":{\"id\":123},\"data1\":{\"id\":234}}";
        Map<String, Type> types = new HashMap<>();
        types.put("data", Bean.class);
        types.put("data1", Bean1.class);
        JSONObject jsonObject = JSON.parseObject(str, MapMultiValueType.of(types));

        Bean bean = (Bean) jsonObject.get("data");
        assertEquals(123, bean.id);

        Bean1 bean1 = (Bean1) jsonObject.get("data1");
        assertEquals(234, bean1.id);
    }

    public static class Bean1 {
        public int id;
    }
}

package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("reader")
public class ObjectReaderImplMapTypedTest {
    @Test
    public void test() {
        ObjectReaderImplMapTyped reader = new ObjectReaderImplMapTyped(
                Map.class,
                TreeMap.class,
                String.class,
                Long.class,
                0,
                null
        );
        HashMap map = new HashMap();
        map.put("value", null);
        TreeMap instance = (TreeMap) reader.createInstance(map, 0);
        assertEquals(1, instance.size());
        assertNull(instance.get("value"));
    }

    static class Param {
        Map<String, Val> varsMap;

        public Map<String, Val> getVarsMap() {
            return varsMap;
        }

        public void setVarsMap(Map<String, Val> varsMap) {
            this.varsMap = varsMap;
        }
    }

    static class Val {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void testMapStringKeyInnerAutoTypeWithWhitelist() {
        String json = "{\"varsMap\":{\"@type\":\"java.util.LinkedHashMap\",\"1\":{\"name\":\"x\"}}}";
        Param param = JSON.parseObject(
                json,
                Param.class,
                JSONReader.autoTypeFilter(false, "java.util.", "com.alibaba.fastjson2.reader.")
        );
        assertTrue(param.getVarsMap() instanceof LinkedHashMap);
    }
}

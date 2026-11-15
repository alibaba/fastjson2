package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void testAllowlistedContainerType() {
        String param = Param.class.getName();
        String withType = "[{\"@type\":\"" + param
                + "\",\"varsMap\":{\"@type\":\"java.util.LinkedHashMap\",\"1\":{\"name\":\"x\"},\"2\":{\"name\":\"y\"}}}]";
        String withoutType = "[{\"@type\":\"" + param
                + "\",\"varsMap\":{\"1\":{\"name\":\"x\"},\"2\":{\"name\":\"y\"}}}]";
        String rejectedContainer = "[{\"@type\":\"" + param
                + "\",\"varsMap\":{\"@type\":\"java.util.TreeMap\",\"1\":{\"name\":\"x\"},\"2\":{\"name\":\"y\"}}}]";
        String rejectedPojo = "[{\"@type\":\"java.util.TreeMap\",\"a\":1}]";
        Class<?>[] mapClasses = new Class<?>[]{LinkedHashMap.class, HashMap.class, HashMap.class};
        String[] jsons = new String[]{withType, withoutType, rejectedContainer};

        for (ObjectReaderCreator creator : new ObjectReaderCreator[]{
                ObjectReaderCreatorASM.INSTANCE, ObjectReaderCreator.INSTANCE}) {
            ObjectReaderProvider provider = new ObjectReaderProvider(creator);
            provider.setAutoTypeBeforeHandler(JSONReader.autoTypeFilter(
                    param, Val.class.getName(), "java.util.LinkedHashMap"));
            for (int i = 0; i < jsons.length; i++) {
                for (int reader = 0; reader < 2; reader++) {
                    JSONReader.Context context = new JSONReader.Context(provider);
                    String json = jsons[i];
                    try (JSONReader jsonReader = reader == 0
                            ? JSONReader.of(json, context)
                            : JSONReader.of(json.toCharArray(), context)) {
                        List<?> list = jsonReader.read(List.class);
                        assertTrue(jsonReader.isEnd());
                        Param parsed = (Param) list.get(0);
                        assertEquals(mapClasses[i], parsed.varsMap.getClass());
                        assertEquals(2, parsed.varsMap.size());
                        assertEquals(Val.class, parsed.varsMap.get("1").getClass());
                        assertEquals("x", parsed.varsMap.get("1").name);
                        assertEquals(Val.class, parsed.varsMap.get("2").getClass());
                        assertEquals("y", parsed.varsMap.get("2").name);
                    }
                }
            }

            for (int reader = 0; reader < 2; reader++) {
                JSONReader.Context context = new JSONReader.Context(provider);
                try (JSONReader jsonReader = reader == 0
                        ? JSONReader.of(rejectedPojo, context)
                        : JSONReader.of(rejectedPojo.toCharArray(), context)) {
                    List<?> list = jsonReader.read(List.class);
                    assertTrue(jsonReader.isEnd());
                    assertFalse(list.get(0) instanceof TreeMap);
                    assertFalse(list.get(0) instanceof Param);
                }
            }
        }
    }

    public static class Val {
        public String name;
    }

    public static class Param {
        public Map<String, Val> varsMap;
    }
}

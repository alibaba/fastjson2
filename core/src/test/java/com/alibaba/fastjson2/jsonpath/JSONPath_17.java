package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_17 {
    @Test
    public void test_for_jsonpath() throws Exception {
        String str = "cartGroups[0].cartItemGroups[0].cartItems[0].item.category.categoryExtra.stdCategoryDO.pathList";
        JSONPath path = JSONPath.of(str);
        assertEquals("com.alibaba.fastjson2.JSONPathPathMulti", path.getClass().getName());
        Class<?> pathClass = Class.forName("com.alibaba.fastjson2.JSONPathPathMulti");
        Field field = pathClass.getDeclaredField("segments");
        field.setAccessible(true);
        List segments = (List) field.get(path);
        assertEquals(11, segments.size());
    }

    @Test
    public void test_for_jsonpath_enumKey() {
        Map map = new HashMap();
        map.put(TimeUnit.DAYS, "101");

        assertEquals("101",
                JSONPath.of("$.DAYS")
                        .eval(map));
    }

    @Test
    public void test_for_jsonpath_enumKey_1() {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(TimeUnit.DAYS, "101");
        root.put("values", map);

        assertEquals("101",
                JSONPath.of("$.values.DAYS")
                        .eval(root));
    }

    @Test
    public void test_for_jsonpath_longKey() {
        Map map = new HashMap();
        map.put(2748734281L, "101");

        assertEquals("101",
                JSONPath.of("$.2748734281")
                        .eval(map));
    }

    @Test
    public void test_for_jsonpath_longKey_1() {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(2748734281L, "101");
        root.put("values", map);

        assertEquals("101",
                JSONPath.of("$.values.2748734281")
                        .eval(root));
    }

    @Test
    public void test_for_jsonpath_longKey_2() {
        Map map = new HashMap();
        map.put(1773193982L, "101");

        assertEquals("101",
                JSONPath.of("$.1773193982")
                        .eval(map));
    }

    @Test
    public void test_for_jsonpath_longKey_3() {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(1773193982L, "101");
        root.put("values", map);

        assertEquals("101",
                JSONPath.of("$.values.1773193982")
                        .eval(root));
    }
}

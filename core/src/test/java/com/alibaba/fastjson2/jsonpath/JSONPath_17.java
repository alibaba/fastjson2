package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.*;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JSONPath_17 extends TestCase {
    public void test_for_jsonpath() throws Exception {
        String str = "cartGroups[0].cartItemGroups[0].cartItems[0].item.category.categoryExtra.stdCategoryDO.pathList";
        JSONPath path = JSONPath.of(str);
        assertEquals("com.alibaba.fastjson2.JSONPath$MultiSegmentPath", path.getClass().getName());
        Class<?> pathClass = Class.forName("com.alibaba.fastjson2.JSONPath$MultiSegmentPath");
        Field field = pathClass.getDeclaredField("segments");
        field.setAccessible(true);
        List segments = (List) field.get(path);
        assertEquals(11, segments.size());
    }

    public void test_for_jsonpath_enumKey() throws Exception {
        Map map = new HashMap();
        map.put(TimeUnit.DAYS, "101");

        assertEquals("101"
                , JSONPath.of("$.DAYS")
                        .eval(map));
    }

    public void test_for_jsonpath_enumKey_1() throws Exception {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(TimeUnit.DAYS, "101");
        root.put("values", map);

        assertEquals("101"
                , JSONPath.of("$.values.DAYS")
                        .eval(root));
    }

    public void test_for_jsonpath_longKey() throws Exception {
        Map map = new HashMap();
        map.put(2748734281L, "101");

        assertEquals("101"
                , JSONPath.of("$.2748734281")
                        .eval(map));
    }

    public void test_for_jsonpath_longKey_1() throws Exception {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(2748734281L, "101");
        root.put("values", map);

        assertEquals("101"
                , JSONPath.of("$.values.2748734281")
                        .eval(root));
    }

    public void test_for_jsonpath_longKey_2() throws Exception {
        Map map = new HashMap();
        map.put(1773193982L, "101");

        assertEquals("101"
                , JSONPath.of("$.1773193982")
                        .eval(map));
    }

    public void test_for_jsonpath_longKey_3() throws Exception {
        Map root = new HashMap();
        Map map = new HashMap();
        map.put(1773193982L, "101");
        root.put("values", map);

        assertEquals("101"
                , JSONPath.of("$.values.1773193982")
                        .eval(root));
    }

}

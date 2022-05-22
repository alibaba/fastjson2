package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPath_9 {
    @Test
    public void test_remove_error() throws Exception {
        Int1 vo = new Int1();
        vo.setV0000(1001);
        assertFalse(
                JSONPath.of("$.v0001")
                        .remove(vo));
        assertEquals(1001, vo.getV0000());
        assertTrue(
                JSONPath.of("$.v0000")
                        .remove(vo));
        assertEquals(0, vo.getV0000());
    }

    @Test
    public void test_paths() throws Exception {
        Model m = new Model();
        m.f0 = 101;
        m.f1 = 102;

        Map<String, Object> paths = JSONPath.paths(m);
        assertEquals(3, paths.size());

        assertNotNull(m.f1);
        JSONPath.of("$.f1").remove(m);
        assertNull(m.f1);
    }

    @Test
    public void test_paths_1() throws Exception {
        Map map = new HashMap();
        map.put("f0", 1001);
        map.put("f1", 1002);

        Map<String, Object> paths = JSONPath.paths(map);
        assertEquals(3, paths.size());
    }

    @Test
    public void test_paths_2() throws Exception {
        Map map = new HashMap();
        map.put("f0", 1001);
        map.put("f1", 1002);

        JSONPath path = JSONPath.of("$.f0");
        assertEquals("$.f0", path.toString());
        assertEquals(1001, path.eval(map));

        path.remove(null);

        assertEquals(2, map.size());
        path.remove(map);
        assertEquals(1, map.size());
    }

    @Test
    public void test_paths_3() throws Exception {
        JSONPath.paths(null);
        JSONPath.paths(1);
        JSONPath.paths("1");
        JSONPath.paths(TimeUnit.DAYS);
    }

    public static class Model {
        public Integer f0;
        public Integer f1;
    }
}

package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONPath_between_int {
    @Test
    public void test_between() throws Exception {
        List list = new ArrayList();
        list.add(new Entity(101, "kiki"));
        list.add(new Entity(102, "ljw2083"));
        list.add(new Entity(103, "ljw2083"));
        List<Object> result = (List<Object>) JSONPath.eval(list, "$[?(@.id between 101 and 101)]");
        assertEquals(1, result.size());
        assertSame(list.get(0), result.get(0));
    }

    @Test
    public void test_between_2() throws Exception {
        List list = new ArrayList();
        list.add(new Entity(101, "kiki"));
        list.add(new Entity(102, "ljw2083"));
        list.add(new Entity(103, "ljw2083"));
        List<Object> result = (List<Object>) JSONPath.eval(list, "$[?(@.id between 101 and 102)]");
        assertEquals(2, result.size());
        assertSame(list.get(0), result.get(0));
        assertSame(list.get(1), result.get(1));
    }

    @Test
    public void test_between_not() throws Exception {
        List list = new ArrayList();
        list.add(new Entity(101, "kiki"));
        list.add(new Entity(102, "ljw2083"));
        list.add(new Entity(103, "ljw2083"));
        List<Object> result = (List<Object>) JSONPath.eval(list, "$[?(@.id not between 101 and 102)]");
        assertEquals(1, result.size());
        assertSame(list.get(2), result.get(0));
    }

    @Test
    public void test_between_3() throws Exception {
        JSONArray array = new JSONArray()
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigInteger.valueOf(101))
                                .fluentPut("name", "DataWorks")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigInteger.valueOf(102))
                                .fluentPut("name", "MaxCompute")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigInteger.valueOf(103))
                                .fluentPut("name", "EMR")
                );
        assertEquals("[{\"id\":101,\"name\":\"DataWorks\"},{\"id\":102,\"name\":\"MaxCompute\"}]",
                JSONPath.of("$[?(@.id between 101 and 102)]")
                        .eval(array)
                        .toString()
        );
    }

    @Test
    public void test_between_4() throws Exception {
        JSONArray array = new JSONArray()
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(101))
                                .fluentPut("name", "DataWorks")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(102))
                                .fluentPut("name", "MaxCompute")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(103))
                                .fluentPut("name", "EMR")
                );
        assertEquals("[{\"id\":101,\"name\":\"DataWorks\"},{\"id\":102,\"name\":\"MaxCompute\"}]",
                JSONPath.of("$[?(@.id between 101 and 102)]")
                        .eval(array)
                        .toString()
        );
    }

    @Test
    public void test_and_0() throws Exception {
        JSONArray array = new JSONArray()
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(101))
                                .fluentPut("name", "DataWorks")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(102))
                                .fluentPut("name", "MaxCompute")
                )
                .fluentAdd(
                        new JSONObject()
                                .fluentPut("id", BigDecimal.valueOf(103))
                                .fluentPut("name", "EMR")
                );
        assertEquals("[{\"id\":101,\"name\":\"DataWorks\"},{\"id\":102,\"name\":\"MaxCompute\"}]",
                JSONPath.of("$[?(@.id >= 101 and @.id <= 102)]")
                        .eval(array)
                        .toString()
        );
    }

    @Test
    public void test_1() throws Exception {
        JSONObject object = new JSONObject()
                .fluentPut("id", BigDecimal.valueOf(101))
                .fluentPut("name", "DataWorks");
        assertEquals("{\"id\":101,\"name\":\"DataWorks\"}",
                JSONPath.of("$[?(@.id >= 101 and @.id <= 102)]")
                        .eval(object)
                        .toString()
        );
        assertEquals("{\"id\":101,\"name\":\"DataWorks\"}",
                JSONPath.of("$[?(@.id <= 101 or @.id >= 102)]")
                        .eval(object)
                        .toString()
        );
    }

    public static class Entity {
        private Integer id;
        private String name;

        public Entity(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

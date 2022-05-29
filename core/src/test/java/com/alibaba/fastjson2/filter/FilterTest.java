package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class FilterTest {
    @Test
    public void test_0() {
        assertEquals("{\"Id\":123}", JSON.toJSONString(Collections.singletonMap("id", 123), new PascalNameFilter()));
        assertEquals("{\"\":123}", JSON.toJSONString(Collections.singletonMap("", 123), new PascalNameFilter()));
    }

    @Test
    public void test_1() {
        A a = new A();
        a.id = 123;
        assertEquals("{\"Id\":123}", JSON.toJSONString(a, new PascalNameFilter()));
    }

    @Test
    public void test_2() {
        assertEquals("{\"id\":123}", JSON.toJSONString(
                new JSONObject()
                        .fluentPut("id", 123)
                        .fluentPut("name", "DataWorks"),
                new SimplePropertyPreFilter("id")));

        assertEquals("{\"id\":123,\"name\":\"DataWorks\"}", JSON.toJSONString(
                new JSONObject()
                        .fluentPut("id", 123)
                        .fluentPut("name", "DataWorks"),
                new SimplePropertyPreFilter(JSONObject.class)));
    }

    @Test
    public void test_3() {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        assertNull(filter.getClazz());
        assertEquals(0, filter.getMaxLevel());
        filter.setMaxLevel(1);
        assertEquals(1, filter.getMaxLevel());
        assertEquals("{\"value\":{}}",
                JSON.toJSONString(
                        new JSONObject()
                                .fluentPut("value",
                                        new JSONObject()
                                                .fluentPut("id", 123)
                                                .fluentPut("name", "DataWorks")),
                        filter)
        );
    }

    @Test
    public void test_4() {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("name");
        assertEquals("{\"id\":123}", JSON.toJSONString(
                new JSONObject()
                        .fluentPut("id", 123)
                        .fluentPut("name", "DataWorks"),
                filter));
    }

    @Test
    public void test_5() {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getIncludes().add("name");
        assertEquals("{\"name\":\"DataWorks\"}", JSON.toJSONString(
                new JSONObject()
                        .fluentPut("id", 123)
                        .fluentPut("name", "DataWorks"),
                filter));
    }

    @Test
    public void test_6() {
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(JSONArray.class);
        assertNotNull(filter.getClazz());
        assertEquals("{}",
                JSON.toJSONString(
                        new JSONObject()
                                .fluentPut("value",
                                        new JSONObject()
                                                .fluentPut("id", 123)
                                                .fluentPut("name", "DataWorks")),
                        filter)
        );
    }

    public static class A {
        public int id;
    }

    @Test
    public void afterTest() {
        AfterFilter filter = new AfterFilter() {
            @Override
            public void writeAfter(Object object) {
                writeKeyValue("id", 123);
            }
        };

        assertEquals("{\"id\":123}",
                JSON.toJSONString(
                        JSONObject.of(),
                        filter
                )
        );

        assertEquals("{\"value\":\"xx\",\"id\":123}",
                JSON.toJSONString(
                        JSONObject.of("value", "xx"),
                        filter
                )
        );
    }

    @Test
    public void afterTest1() {
        A a = new A();
        a.id = 123;

        AfterFilter filter = new AfterFilter() {
            @Override
            public void writeAfter(Object object) {
                writeKeyValue("oid", 101);
            }
        };
        assertEquals("{\"id\":123,\"oid\":101}",
                JSON.toJSONString(a, filter)
        );
    }

    @Test
    public void beforeTest() {
        BeforeFilter filter = new BeforeFilter() {
            @Override
            public void writeBefore(Object object) {
                writeKeyValue("id", 123);
            }
        };

        assertEquals("{\"id\":123}",
                JSON.toJSONString(
                        JSONObject.of(),
                        filter
                )
        );

        assertEquals("{\"id\":123,\"value\":\"xx\"}",
                JSON.toJSONString(
                        JSONObject.of("value", "xx"),
                        filter
                )
        );
    }

    @Test
    public void BeforeTest1() {
        A a = new A();
        a.id = 123;

        BeforeFilter filter = new BeforeFilter() {
            @Override
            public void writeBefore(Object object) {
                writeKeyValue("oid", 101);
            }
        };
        assertEquals("{\"oid\":101,\"id\":123}",
                JSON.toJSONString(a, filter)
        );
    }
}

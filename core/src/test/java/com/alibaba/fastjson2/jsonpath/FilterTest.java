package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterTest {
    @Test
    public void test() {
        JSONArray doc = JSONArray.of(
                JSONObject.of("a", 100, "b", 100),
                JSONObject.of("a", 200, "b", 201),
                JSONObject.of("a", 300, "b", 300),
                JSONObject.of("a", 400, "b", 401)
        );

        Object results = JSONPath.of("$[?@.a==@.b]").eval(doc);
        assertEquals("[{\"a\":100,\"b\":100},{\"a\":300,\"b\":300}]", JSON.toJSONString(results));
    }

    @Test
    public void test1() {
        JSONArray doc = JSONArray.of(
                JSONObject.of("a", 100, "b", 100),
                JSONObject.of("a", 200, "b", 1e2),
                JSONObject.of("a", 300, "b", 300),
                JSONObject.of("a", 400, "b", 401)
        );

        Object results = JSONPath.of("$[?@.b==1e2]").eval(doc);
        assertEquals("[{\"a\":100,\"b\":100},{\"a\":200,\"b\":100.0}]", JSON.toJSONString(results));
    }

    @Test
    public void test2() {
        JSONArray doc = JSONArray.of(
                JSONObject.of("0", 100, "b", 100),
                JSONObject.of("0", 200, "b", 200),
                JSONObject.of("0", 300, "b", 300),
                JSONObject.of("0", 200, "b", 401)
        );

        Object results = JSONPath.of("$[?@['0']==200]")
                .eval(doc);
        assertEquals("[{\"0\":200,\"b\":200},{\"0\":200,\"b\":401}]", JSON.toJSONString(results));
    }

    @Test
    public void test3() {
        JSONArray doc = JSONArray.of(
                JSONArray.of(1, 2),
                JSONArray.of(3, 4),
                JSONArray.of(5, 6),
                JSONArray.of(7, 8)
        );
        assertEquals("[[5,6],[7,8]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] >= 5]")
                                .eval(doc)
                )
        );
        assertEquals("[[7,8]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] > 5]")
                                .eval(doc)
                )
        );
        assertEquals("[[1,2],[3,4]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] < 5]")
                                .eval(doc)
                )
        );
        assertEquals("[[1,2],[3,4],[5,6]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] <= 5]")
                                .eval(doc)
                )
        );
    }

    @Test
    public void test4() {
        JSONArray doc = JSONArray.of(
                JSONArray.of(1, 2),
                JSONArray.of(4, 3),
                JSONArray.of(5, 6),
                JSONArray.of(8, 7),
                JSONArray.of(9, 9)
        );

        assertEquals("[[4,3],[8,7]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] > @[1]]")
                                .eval(doc)
                )
        );
        assertEquals("[[1,2],[5,6]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] < @[1]]")
                                .eval(doc)
                )
        );
        assertEquals("[[9,9]]",
                JSON.toJSONString(
                        JSONPath.of("$[?@[0] == @[1]]")
                                .eval(doc)
                )
        );
    }

    @Test
    public void test5() {
        JSONArray doc = JSONArray.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        assertEquals("[1]",
                JSON.toJSONString(
                        JSONPath.of("$[?@ == 1]")
                                .eval(doc)
                )
        );

        assertEquals("[1,2]",
                JSON.toJSONString(
                        JSONPath.of("$[?@ < 3]")
                                .eval(doc)
                )
        );

        assertEquals("[12,13,14,15]",
                JSON.toJSONString(
                        JSONPath.of("$[?@ >= 12]")
                                .eval(doc)
                )
        );
    }

    @Test
    public void test6() {
        JSONArray doc = JSONArray.of(
                JSONObject.of("a", 1, "d", 4),
                JSONObject.of("b", 2, "d", 5),
                JSONObject.of("a", 3, "b", 6),
                JSONObject.of("d", 3, "e", 6)
        );

        assertEquals("[{\"a\":1,\"d\":4},{\"a\":3,\"b\":6}]",
                JSON.toJSONString(
                        JSONPath.of("$[?@.a]")
                                .eval(doc)
                )
        );

        assertEquals("[{\"a\":1,\"d\":4},{\"b\":2,\"d\":5},{\"a\":3,\"b\":6}]",
                JSON.toJSONString(
                        JSONPath.of("$[?@.a||@.b]")
                                .eval(doc)
                )
        );
        assertEquals("[{\"a\":3,\"b\":6}]",
                JSON.toJSONString(
                        JSONPath.of("$[?@.a&&@.b]")
                                .eval(doc)
                )
        );
        assertEquals("[{\"a\":3,\"b\":6}]",
                JSON.toJSONString(
                        JSONPath.of("$[?@.a&&@.b]")
                                .eval(doc)
                )
        );
    }
}

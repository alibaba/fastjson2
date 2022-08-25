package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue708 {
    @Test
    public void test() {
        JSONObject root = new JSONObject();

        Object[] values = {100d, 80.05d};
        JSONPath
                .of("$.user.score")
                .arrayAdd(root, values);

        assertEquals(
                "{\"user\":{\"score\":[100.0,80.05]}}",
                root.toString()
        );
    }

    @Test
    public void test1() {
        JSONObject root = new JSONObject();

        Object[] values = {100d, 80.05d};
        JSONPath
                .of("$.s0")
                .arrayAdd(root, values);

        assertEquals(
                "{\"s0\":[100.0,80.05]}",
                root.toString()
        );
    }

    @Test
    public void test3() {
        JSONObject root = new JSONObject();

        Object[] values = {100d, 80.05d};
        JSONPath
                .of("$.s0.s1.s2")
                .arrayAdd(root, values);

        assertEquals(
                "{\"s0\":{\"s1\":{\"s2\":[100.0,80.05]}}}",
                root.toString()
        );
    }
}

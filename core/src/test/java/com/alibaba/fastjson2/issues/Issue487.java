package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue487 {
    @Test
    public void test() {
        assertEquals("[1]",
                JSON.toJSONString(
                        JSONPath.of("$.v1[*].v2[*].v3.v4")
                                .extract(
                                        JSONReader.of("{\"v1\":[{\"v2\":[{\"v3\":{\"v4\":1}}]}]}")
                                )
                )
        );
    }

    @Test
    public void test1() {
        assertEquals("[1]",
                JSON.toJSONString(
                        JSONPath.of("$.v1[*].v2[*].v3.v4")
                                .eval(
                                        JSON.parseObject("{\"v1\":[{\"v2\":[{\"v3\":{\"v4\":1}}]}]}")
                                )
                )
        );
    }
}

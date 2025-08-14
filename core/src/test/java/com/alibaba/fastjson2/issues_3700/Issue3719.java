package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3719 {
    @Test
    public void test() {
        assertEquals("[{\"a\":700000,\"b\":1}]", JSONArray.parse("[{\n" +
                "                    // 注释a\n" +
                "                    \"a\":700000, \n" +
                "                    // 注释b\n" +
                "                    \"b\":1 \n" +
                "                }]").toString());
    }
}

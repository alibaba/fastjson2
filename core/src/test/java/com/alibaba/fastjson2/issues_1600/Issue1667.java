package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1667 {
    @Test
    public void test() {
        JSONObject root = JSONObject.of(
                "indexDatas",
                JSONArray.of(
                        JSONObject.of("isSummary", 1, "title", "ss"),
                        JSONObject.of("title", "ss")
                )
        );

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary != 1)]")
                    .eval(root);
            assertEquals("[{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary != 1.0)]")
                    .eval(root);
            assertEquals("[{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary != '1')]")
                    .eval(root);
            assertEquals("[{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary not between 1 and 2)]")
                    .eval(root);
            assertEquals("[{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary not in (1))]")
                    .eval(root);
            assertEquals("[{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }

        {
            Object result = JSONPath.of("$.indexDatas[?(@.isSummary not in ('1'))]")
                    .eval(root);
            assertEquals("[{\"isSummary\":1,\"title\":\"ss\"},{\"title\":\"ss\"}]", JSON.toJSONString(result));
        }
    }
}

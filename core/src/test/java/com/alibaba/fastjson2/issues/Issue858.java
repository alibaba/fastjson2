package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue858 {
    @Test
    public void test() {
        String tmpJson = "{\"arr\": [{ \"key\": \"value1\" }, { \"key\": \"value2\" }]}";
        String tmpPath = "$.arr[?(@.key='value1')]";
        Object tmpObj = JSONPath.extract(tmpJson, tmpPath);
        assertEquals("[{\"key\":\"value1\"}]", JSON.toJSONString(tmpObj));
    }

    @Test
    public void test1() {
        String tmpJson = "{\"arr\": [{ \"key\": \"value1\" }, { \"key\": \"value2\" }]}";
        String tmpPath = "$.arr[?(key='value1')]";
        Object tmpObj = JSONPath.extract(tmpJson, tmpPath);
        assertEquals("[{\"key\":\"value1\"}]", JSON.toJSONString(tmpObj));
    }
}

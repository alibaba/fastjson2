package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Issue223 {
    @Test
    public void test() {
        class app {
            String appPath;
        }
        String message = "{\"type\":\"APP_SYNC\",\"appList\":[{\"appPath\":\"C:\\\\Users\\\\apple\\\\AppData\"}]}";

        // 这里报异常 unclosed.str.lit U
        List<app> test = JSON.parseObject(
                JSONPath.eval(
                        JSON.parseObject(message), "$.appList"
                ).toString(),
                new TypeReference<List<app>>() {
                }.getType()
        );
    }

    @Test
    public void test_utf8() {
        class app {
            String appPath;
        }
        String message = "{\"type\":\"APP_SYNC\",\"appList\":[{\"appPath\":\"C:\\\\Users\\\\apple\\\\AppData\"}]}";

        // 这里报异常 unclosed.str.lit U
        List<app> test = JSON.parseObject(
                JSONPath.eval(
                        JSON.parseObject(message.getBytes(StandardCharsets.UTF_8)), "$.appList"
                ).toString(),
                new TypeReference<List<app>>() {
                }.getType()
        );
    }
}

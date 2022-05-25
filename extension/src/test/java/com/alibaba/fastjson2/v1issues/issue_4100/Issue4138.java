package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class Issue4138 {
    // 配置AutoTypeFilter
    static JSONReader.Filter filter = JSONReader.autoTypeFilter(
            "org.springframework.security.core.authority.SimpleGrantedAuthority" // 这里可以配置多个前缀
    );

    public Object deserialize(byte[] bytes) {
        return JSON.parseObject(bytes, Object.class, filter);
    }

    @Test
    public void test() {
        String json = "{\"@type\":\"org.springframework.security.core.authority.SimpleGrantedAuthority\",\"role\":\"abc\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        deserialize(bytes);
    }
}

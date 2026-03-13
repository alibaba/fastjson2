package com.alibaba.fastjson2.v1issues.issue_3500;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("regression")
@Tag("compat-fastjson1")
public class Issue3544 {
    @Test
    public void test_errorType() {
        assertNull(JSON.toJavaObject(
                JSON.parseObject("{\"result\":\"\"}"), TestVO.class).result);

        assertNull(JSON.toJavaObject(
                JSON.parseObject("{\"result\":\"null\"}"), TestVO.class).result);
    }

    @Getter
    @Setter
    static class TestVO {
        Map<String, String> result;
    }
}

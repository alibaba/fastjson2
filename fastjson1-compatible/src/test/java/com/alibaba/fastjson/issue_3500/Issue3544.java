package com.alibaba.fastjson.issue_3500;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

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

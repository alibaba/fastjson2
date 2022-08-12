package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue669 {
    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray("[\"3330354\"]", Bean.class)
        );
    }

    @Data
    public static class Bean {
        private String a;
    }
}

package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue2879 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{}");
        assertNull(object.to(Void.class));
        assertNull(object.to(void.class));
    }
}

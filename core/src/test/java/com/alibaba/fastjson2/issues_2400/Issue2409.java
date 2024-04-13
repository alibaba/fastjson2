package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author milabi
 * @since 2024-04-08
 */
public class Issue2409 {
    @Test
    void test() {
        String s = "{\"CABCB9281415LR\":{\"key\":\"CABCB9281415LR\",\"type\":\"分类1\"},\"CABCB9261415LR\":{\"key\":\"CABCB9261415LR\",\"type\":\"分类2\"}}";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(bytes);
        Assertions.assertEquals(s, JSON.toJSONString(jsonObject));
    }
}

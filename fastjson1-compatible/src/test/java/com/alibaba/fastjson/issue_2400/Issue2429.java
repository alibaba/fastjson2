package com.alibaba.fastjson.issue_2400;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

public class Issue2429 {
    @Test
    public void testForIssue() {
        String str = "{\"schema\":{$ref:\"111\"},\"name\":\"ft\",\"age\":12,\"address\":\"杭州\"}";
        JSON.parseObject(str);
    }
}

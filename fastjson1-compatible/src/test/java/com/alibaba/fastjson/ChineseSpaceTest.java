package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 2016/10/14.
 */
public class ChineseSpaceTest {
    @Test
    public void test_for_chinese_space() throws Exception {
        Map<String, String> map = Collections.singletonMap("v", " ");
        String json = JSON.toJSONString(map);
        assertEquals("{\"v\":\" \"}", json);

        JSONObject jsonObject = JSON.parseObject(json);
        assertEquals(map.get("v"), jsonObject.get("v"));
    }
}

package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue355 {
    @Test
    public void test() {
        String str = " {\"code\": \"00\", \"msg\": \"\\u64cd\\u4f5c\\u6210\\u529f.\", \"result\": [{\"id\": \"d2303af53f444d9fa965d499d4976c69\", \"tag_list\": [], \"tip_tag_list\": [{\"tag\": \"\\u89e3\\u51b3\", \"tag_weight\": 0.683}]}]}";
        JSONObject object = JSON.parseObject(str);
        assertEquals("00", object.get("code"));
    }
}

package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue334 {
    @Test
    public void test() {
        String a = "{\"resultCode\":\"200013\",\"message\":\"查询成功\",\"data\":{\"clientName\":\"张三\",\"clientId\":\"3002674743\",\"phoneNumber\":\"13412345678\",\"gender\":2,\"age\":59}}";
        JSONObject jsonObject = JSONObject.parseObject(a, Feature.OrderedField);
        assertEquals("{\"resultCode\":\"200013\",\"message\":\"查询成功\",\"data\":{\"clientName\":\"张三\",\"clientId\":\"3002674743\",\"phoneNumber\":\"13412345678\",\"gender\":2,\"age\":59}}", jsonObject.toJSONString());
    }
}

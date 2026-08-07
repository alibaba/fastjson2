package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest5 {
    @Test
    public void test() throws Exception {
        JSONObject jsonObject = new JSONObject(3, true);
        jsonObject.put("name", "J.K.SAGE");
        jsonObject.put("age", 21);
        jsonObject.put("msg", "Hello!");
        JSONObject cloneObject = (JSONObject) jsonObject.clone();
        assertEquals(JSON.toJSONString(jsonObject), JSON.toJSONString(cloneObject));
    }
}

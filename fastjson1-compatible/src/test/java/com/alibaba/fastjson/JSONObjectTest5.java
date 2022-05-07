package com.alibaba.fastjson;

import junit.framework.TestCase;

public class JSONObjectTest5 extends TestCase {

    public void test() throws Exception {
        JSONObject jsonObject = new JSONObject(3, true);
        jsonObject.put("name", "J.K.SAGE");
        jsonObject.put("age", 21);
        jsonObject.put("msg", "Hello!");
        JSONObject cloneObject = (JSONObject) jsonObject.clone();
        assertEquals(JSON.toJSONString(jsonObject), JSON.toJSONString(cloneObject));
    }
}

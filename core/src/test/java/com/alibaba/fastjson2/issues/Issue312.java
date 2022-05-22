package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue312 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("file", new File("/User/xxx/JsonTest.java"));
        String json = object.toJSONString();
        assertEquals("{\"file\":\"/User/xxx/JsonTest.java\"}", json);
        File file = JSON.parseObject(json).getObject("file", File.class);
        assertEquals("/User/xxx/JsonTest.java", file.toString());
    }
}

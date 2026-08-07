package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("regression")
@Tag("jsonb")
public class Issue1636 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("to_type", 1, "tf_type", 2);
        byte[] jsonbBytes = object.toJSONBBytes();
        JSONObject object1 = JSONB.parseObject(jsonbBytes);
        assertEquals(object, object1);
    }
}

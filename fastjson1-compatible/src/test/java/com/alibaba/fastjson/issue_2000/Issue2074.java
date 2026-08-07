package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2074 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject object = new JSONObject();
        object.put("name", null);

        assertEquals("{\"name\":null}",
                object.toString(SerializerFeature.WriteMapNullValue));
    }
}

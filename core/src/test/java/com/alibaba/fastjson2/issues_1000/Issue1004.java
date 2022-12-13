package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1004 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("id", null);
        String str = JSON.toJSONString(object, (NameFilter) (object1, name, value) -> "xid", JSONWriter.Feature.WriteNulls);
        assertEquals("{\"xid\":null}", str);
    }
}

package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_get_2 {
    @Test
    public void test_get() {
        JSONObject obj = JSON.parseObject("{\"value\":{}}");
        JSONObject value = (JSONObject) obj.getObject("value", Object.class);
        assertEquals(0, value.size());
    }

    public static interface VO {
        @JSONField()
        int getId();

        @JSONField()
        void setId(int val);
    }
}

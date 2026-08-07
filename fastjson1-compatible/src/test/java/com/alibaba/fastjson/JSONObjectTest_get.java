package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_get {
    @Test
    public void test_get() {
        JSONObject obj = JSON.parseObject("{id:123}");
        assertEquals(123, obj.getObject("id", Object.class));
    }

    public interface VO {
        @JSONField
        int getId();

        @JSONField
        void setId(int val);
    }
}

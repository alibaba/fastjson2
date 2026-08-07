package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_get_2 {
    @Test
    public void test_get() throws Exception {
        if (TestUtils.GRAALVM) {
            return;
        }
        JSONObject obj = JSON.parseObject("{\"value\":{}}");
        JSONObject value = (JSONObject) obj.getObject("value", Object.class);
        assertEquals(0, value.size());
    }

    @Test
    public void test_get_obj() throws Exception {
        if (TestUtils.GRAALVM) {
            return;
        }

        JSONObject obj = new JSONObject();
        {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("@type", "com.alibaba.fastjson.JSONObjectTest_get_2$VO");
            value.put("id", 1001);
            obj.put("value", value);
        }
        VO value = (VO) obj.getObject("value", Object.class, Feature.SupportAutoType);
        assertEquals(1001, value.getId());
    }

    public interface VO {
        @JSONField
        int getId();

        @JSONField
        void setId(int val);
    }
}

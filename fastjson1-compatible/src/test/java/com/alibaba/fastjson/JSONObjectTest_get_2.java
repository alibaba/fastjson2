package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

public class JSONObjectTest_get_2 extends TestCase {
    public void test_get() throws Exception {
        JSONObject obj = JSON.parseObject("{\"value\":{}}");
        JSONObject value = (JSONObject) obj.getObject("value", Object.class);
        Assert.assertEquals(0, value.size());
    }

    public void test_get_obj() throws Exception {
        JSONObject obj = new JSONObject();
        {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("@type", "com.alibaba.fastjson.JSONObjectTest_get_2$VO");
            value.put("id", 1001);
            obj.put("value", value);
        }
        VO value = (VO) obj.getObject("value", Object.class, Feature.SupportAutoType);
        Assert.assertEquals(1001, value.getId());
    }

    public static interface VO {
        @JSONField()
        int getId();

        @JSONField()
        void setId(int val);
    }
}

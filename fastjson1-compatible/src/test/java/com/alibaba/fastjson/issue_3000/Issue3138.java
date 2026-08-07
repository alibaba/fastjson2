package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3138 {
    @Test
    public void test_0() throws Exception {
        VO vo = JSON.parseObject("{\"value\":{\"@type\":\"aa\"}}", VO.class);
        assertEquals("aa", vo.value.get("@type"));
    }

    @Test
    public void test_1() throws Exception {
        JSONObject object = (JSONObject) JSON.parse("{\"@type\":\"aa\"}");
        assertEquals("aa", object.get("@type"));
    }

    public static class VO {
        @JSONField(parseFeatures = Feature.DisableSpecialKeyDetect)
        public Map<String, Object> value;
    }
}

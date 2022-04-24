package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class Issue3138 {
    @Test
    public void test_0() throws Exception {
        VO vo = JSON.parseObject("{\"value\":{\"@type\":\"aa\"}}", VO.class);
    }

    public static class VO {
        @JSONField(parseFeatures = Feature.DisableSpecialKeyDetect)
        public Map<String, Object> value;
    }
}

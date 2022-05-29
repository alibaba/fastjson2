package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2240 {
    @Test
    public void test_for_issue() throws Exception {
        ResultMap resultMap = new ResultMap();
        resultMap.setA(Collections.<Long, Integer>emptyMap());
        resultMap.setB(Collections.<Long, Integer>emptyMap());
        String json = JSON.toJSONString(resultMap);
        System.out.println(json);
        assertTrue("{\"a\":{},\"b\":{}}".equals(json) || "{\"a\":{},\"b\":{\"$ref\":\"$.a\"}}".equals(json));
    }

    public static class ResultMap {
        private Map<Long, Integer> a;
        private Map<Long, Integer> b;

        public Map<Long, Integer> getA() {
            return a;
        }

        public void setA(Map<Long, Integer> a) {
            this.a = a;
        }

        public Map<Long, Integer> getB() {
            return b;
        }

        public void setB(Map<Long, Integer> b) {
            this.b = b;
        }
    }
}

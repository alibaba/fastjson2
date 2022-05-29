package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

public class Issue1425 {
    @Test
    public void test_for_issue() throws Exception {
        DicDomain dicDomain = new DicDomain();
        dicDomain.setCode("A001");
        dicDomain.setName("测试");

        JSONWriter.Feature[] features = new JSONWriter.Feature[]{
                JSONWriter.Feature.NotWriteRootClassName,
                JSONWriter.Feature.WriteClassName
        };

        System.out.println(JSON.toJSONString(dicDomain, features));
    }

    public static class DicDomain {
        private String code;

        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

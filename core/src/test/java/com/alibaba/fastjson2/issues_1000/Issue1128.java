package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

public class Issue1128 {
    @Test
    public void test() {
        Bean test1 = JSON.parseObject("{\"code\": \"test\",\"name\": null}", Bean.class, JSONReader.Feature.InitStringFieldAsEmpty);
//        System.out.println(test1.getName());

        Bean test2 = JSON.parseObject("{\"code\": \"test\"}", Bean.class, JSONReader.Feature.InitStringFieldAsEmpty);
//        System.out.println(test2.getName());
    }

    public static class Bean {
        private String code;
        private String name;

        public Bean() {
        }

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

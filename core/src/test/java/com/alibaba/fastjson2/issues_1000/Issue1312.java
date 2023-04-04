package com.alibaba.fastjson2.issues_1000;

import org.junit.jupiter.api.Test;

import java.util.List;

public class Issue1312 {
    @Test
    public void test() {
        String s = "{\"values\":[[123,456]]}";

        Data data2 = com.alibaba.fastjson2.JSON.parseObject(s, Data.class);
        System.out.println("fastjson2 data:" + com.alibaba.fastjson2.JSON.toJSONString(data2));
    }

    public static class Data {
        private List<List<Object>> values;

        public List<List<Object>> getValues() {
            return values;
        }

        public void setValues(List<List<Object>> values) {
            this.values = values;
        }
    }
}

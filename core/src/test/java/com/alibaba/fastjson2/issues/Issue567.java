package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue567 {
    @Test
    public void test() {
        Bean.CatConfig catConfig = new Bean.CatConfig();
        catConfig.setHeight("200");
        catConfig.setWidth("100");
        Bean test = new Bean();
        test.setConfig(catConfig);
        String json = JSON.toJSONString(test, JSONWriter.Feature.WriteClassName);
        //JSON结果 {"@type":"com.example.fastjson2.Test","config":{"@type":"com.example.fastjson2.Test$CatConfig","height":"200","width":"100"}}
        Bean t = (Bean) JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType);
        assertTrue(t.getConfig() instanceof Bean.CatConfig);
        //对象结果：Test.Config()
    }

    @Data
    public static class Bean {
        private Config config;

        @Data
        public static class Config {
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class CatConfig
                extends Config {
            private String height;
            private String width;
        }
    }
}

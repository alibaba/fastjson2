package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue341 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = Arrays.asList("cacheObject", "cacheObject2", "cacheObject3");

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean bean1 = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(bean.values.size(), bean1.values.size());
    }

    private static class Bean {
        private List<String> values;

        public Bean() {
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }
}

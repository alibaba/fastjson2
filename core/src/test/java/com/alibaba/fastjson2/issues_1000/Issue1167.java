package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1167 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001L;
        assertEquals("{\"id\":\"1001\"}", JSON.toJSONString(bean));
        assertEquals("{'id':'1001'}", JSON.toJSONString(bean, JSONWriter.Feature.UseSingleQuotes));
    }

    public static class Bean {
        @JSONField(format = "string")
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}

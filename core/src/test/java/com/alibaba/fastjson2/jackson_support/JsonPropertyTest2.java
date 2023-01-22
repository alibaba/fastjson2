package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonPropertyTest2 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.jobId = "abc";

        String str = JSON.toJSONString(bean);
        assertEquals("{\"jid\":\"abc\"}", str);
    }

    public static class Bean {
        @JsonProperty("jid")
        private String jobId;

        @JsonIgnore
        public String getJobId() {
            return jobId;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = "001";
        bean.name = "DataWorks";

        String str = JSON.toJSONString(bean);
        assertEquals("{\"id\":\"001\",\"name\":\"DataWorks\"}", str);

        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }

    public static class Bean1 {
        @JsonProperty(required = true)
        public String id;
        @JsonProperty(required = true)
        public String name;
    }
}

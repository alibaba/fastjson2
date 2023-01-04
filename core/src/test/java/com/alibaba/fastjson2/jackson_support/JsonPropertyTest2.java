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
}

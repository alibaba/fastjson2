package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue648 {
    @Test
    public void test() {
        SimpleDtoChild rankQuery = new SimpleDtoChild();
        ((SimpleDto) rankQuery).guId = "123";
        rankQuery.setGuId("abc");

        String str = JSON.toJSONString(rankQuery);
        assertEquals("{\"guId\":\"abc\"}", str);

        SimpleDtoChild dto2 = JSON.parseObject(str, SimpleDtoChild.class);
        assertEquals("abc", dto2.getGuId());
    }

    public static class SimpleDto {
        public String guId;
    }

    public static class SimpleDtoChild
            extends SimpleDto{
        private String guId;

        public String getGuId() {
            return guId;
        }

        public void setGuId(String guId) {
            this.guId = guId;
        }
    }
}

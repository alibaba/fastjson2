package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1019 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.age = 20;
        bean.oAuthName = "xxx";
        String str = JSON.toJSONString(bean);
        assertEquals("{\"age\":20,\"oAuthName\":\"xxx\"}", str);
    }

    @Data
    static class Bean {
        @JSONField(name = "oAuthName")
        private String oAuthName;
        private Integer age;
    }
}

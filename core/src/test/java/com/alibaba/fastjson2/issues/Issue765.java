package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue765 {
    @Test
    public void test() {
        SysTestData sysTestData = new SysTestData();
        sysTestData.setD_ORDER(111);
        sysTestData.setCODE("111");
        assertEquals("{\"code\":\"111\",\"order\":111}", JSON.toJSONString(sysTestData));
    }

    @NoArgsConstructor
    @Data
    public class SysTestData {
        @JSONField(name = "order")
        private Integer D_ORDER;
        @JSONField(name = "code")
        private String CODE;
    }
}

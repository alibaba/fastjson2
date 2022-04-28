package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue89_2 {
    @Test
    public void test_for_issue89_2() {
        String responseBody = "{\"data\":{\"address\":\"四川省达州市宣汉县\"}}";
        AlipayAuthenticationMO alipayAuthenticationMO = JSON.parseObject(responseBody, AlipayAuthenticationMO.class);
        assertEquals(responseBody, JSON.toJSONString(alipayAuthenticationMO));
    }

    @Data
    public static class AlipayAuthenticationMO {
        private Boolean success;
        private String msg;
        private Integer code;
        private AlipayAuthenticationDetailVO data;
    }

    @Data
    public static class AlipayAuthenticationDetailVO {
        private String address;
    }
}

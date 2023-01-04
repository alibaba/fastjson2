package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1049 {
    @Test
    public void test() {
        SimpleResponse data = new SimpleResponse();
        data.setData(BigDecimal.ONE);
        assertEquals("{\"code\":0,\"data\":1}", JSON.toJSONString(data));
    }

    @Data
    public static class SimpleResponse<T> {
        private int code;
        private String msg;
        private T data;
    }
}

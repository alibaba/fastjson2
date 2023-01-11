package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1067 {
    @Test
    public void test() {
        OrderProductVO bean = JSON.parseObject(
                "{\"predictDeliveryTime\":\"2023-01-11 09:38:41\"}",
                OrderProductVO.class,
                JSONReader.Feature.SupportSmartMatch
        );
        assertNotNull(bean.predictDeliveryTime);
    }

    @Test
    public void test1() {
        System.out.println(new BigDecimal("123456789012345678901234567890123456789012345678901234567890"));
    }

    @Data
    public class OrderProductVO
            implements Serializable {
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
        private Date predictDeliveryTime;
    }
}

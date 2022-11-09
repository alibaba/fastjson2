package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue859 {
    @Test
    public void test() {
        PayoutBill payoutBill = PayoutBill.builder().currency(Currency.CNY).build();
        String str = JSON.toJSONString(payoutBill, JSONWriter.Feature.WriteEnumsUsingName);
        assertEquals("{\"currency\":\"CNY\"}", str);
    }

    @Getter
    @AllArgsConstructor
    public enum Currency {
        CNY("人民币"),
        USD("美元"),
        EUR("欧元"),
        GBP("英镑"),
        HKD("港币");

        private final String desc;
    }

    @Builder
    @Getter
    public static class PayoutBill{
        private Currency currency;
    }
}

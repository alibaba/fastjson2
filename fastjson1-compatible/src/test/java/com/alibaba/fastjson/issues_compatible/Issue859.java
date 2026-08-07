package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue859 {
    @Test
    public void test() {
        PayoutBill payoutBill = PayoutBill.builder().currency(Currency.CNY).build();
        String str = JSON.toJSONString(payoutBill, SerializerFeature.WriteEnumUsingName);
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

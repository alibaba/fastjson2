package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue730 {
    @Test
    public void test0() {
        String text = JSON.toJSONString(OrderType.PayOrder);
        assertEquals("{\"remark\":\"支付订单\",\"value\":1}", text);
    }

    @JSONType(writeEnumAsJavaBean = true)
    public enum OrderType {
        PayOrder(1, "支付订单"),
        SettleBill(2, "结算单");

        public final int value;
        public final String remark;

        OrderType(int value, String remark) {
            this.value = value;
            this.remark = remark;
        }
    }

    @Test
    public void test1() {
        String text = JSON.toJSONString(OrderType1.PayOrder);
        assertEquals("{\"remark\":\"支付订单\",\"value\":1}", text);
    }

    @com.alibaba.fastjson.annotation.JSONType(serializeEnumAsJavaBean = true)
    public enum OrderType1 {
        PayOrder(1, "支付订单"),
        SettleBill(2, "结算单");

        public final int value;
        public final String remark;

        OrderType1(int value, String remark) {
            this.value = value;
            this.remark = remark;
        }
    }
}

package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 08/01/2017.
 */
public class SerializeEnumAsJavaBeanTest {
    @Test
    public void test_serializeEnumAsJavaBean() throws Exception {
        String text = JSON.toJSONString(OrderType.PayOrder);
        assertEquals("{\"remark\":\"支付订单\",\"value\":1}", text);
    }

    @Test
    public void test_field() throws Exception {
        Model model = new Model();
        model.orderType = OrderType.SettleBill;
        String text = JSON.toJSONString(model);
        assertEquals("{\"orderType\":{\"remark\":\"结算单\",\"value\":2}}", text);
    }

    @Test
    public void test_field_2() throws Exception {
        Model model = new Model();
        model.orderType = OrderType.SettleBill;
        model.orderType1 = OrderType.SettleBill;
        String text = JSON.toJSONString(model);
        assertEquals("{\"orderType\":{\"remark\":\"结算单\",\"value\":2},\"orderType1\":{\"remark\":\"结算单\",\"value\":2}}", text);
    }

    @Test
    public void test_field_3() throws Exception {
        Model1 model = new Model1();
        model.orderType = OrderType.SettleBill;
        model.orderType1 = OrderType.SettleBill;
        String text = JSON.toJSONString(model);
        assertEquals("{\"orderType\":{\"remark\":\"结算单\",\"value\":2},\"orderType1\":{\"remark\":\"结算单\",\"value\":2}}", text);
    }

    @JSONType(serializeEnumAsJavaBean = true)
    public static enum OrderType {
        PayOrder(1, "支付订单"), //
        SettleBill(2, "结算单");

        public final int value;
        public final String remark;

        private OrderType(int value, String remark) {
            this.value = value;
            this.remark = remark;
        }
    }

    public static class Model {
        public OrderType orderType;
        public OrderType orderType1;
    }

    private static class Model1 {
        public OrderType orderType;
        public OrderType orderType1;
    }
}

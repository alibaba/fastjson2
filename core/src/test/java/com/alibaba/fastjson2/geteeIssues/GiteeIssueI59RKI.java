package com.alibaba.fastjson2.geteeIssues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GiteeIssueI59RKI {
    @Test
    public void test() {
        String str = "[101,102]";
        List<Integer> list = JSON.parseObject(str, new TypeReference<List<Integer>>() {});
        assertEquals(2, list.size());
        assertEquals(101, list.get(0));
        assertEquals(102, list.get(1));
    }

    @Test
    public void test1() {
        String str = "[101,102]";
        List<Integer> list = JSONObject.parseObject(str, new TypeReference<List<Integer>>() {});
        assertEquals(2, list.size());
        assertEquals(101, list.get(0));
        assertEquals(102, list.get(1));
    }

    @Test
    public void test2() {
        String str = "[101,102]";
        List<Integer> list = JSONObject.parseObject(str, new TypeReference<List<Integer>>() {}.getType());
        assertEquals(2, list.size());
        assertEquals(101, list.get(0));
        assertEquals(102, list.get(1));
    }

    @Test
    public void test10() {
        PayOrder order = new PayOrder();
        order.setOrderNo("订单号");
        order.setActualCharge(100L);

        Set<PayOrder> payOrders = Sets.newHashSet(order);
        String json = JSON.toJSONString(payOrders);

        Set<PayOrder> o = JSON.parseObject(json, new TypeReference<Set<PayOrder>>() {});
        assertFalse(o.isEmpty());
        assertEquals(order.getOrderNo(), o.iterator().next().getOrderNo());
        assertEquals(order.getActualCharge(), o.iterator().next().getActualCharge());
    }

    public static class PayOrder {
        private String orderNo;
        private long actualCharge;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public long getActualCharge() {
            return actualCharge;
        }

        public void setActualCharge(long actualCharge) {
            this.actualCharge = actualCharge;
        }
    }
}

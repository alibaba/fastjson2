package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1933 {
    @Test
    public void test_for_issue() throws Exception {
        OrderInfoVO v0 = JSON.parseObject("{\"orderStatus\":1}", OrderInfoVO.class);
        assertEquals(1, v0.orderStatus);
        assertEquals(0, v0.oldStatus);
        assertEquals(0, v0.oldOrderStatus);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        OrderInfoVO v0 = JSON.parseObject("{\"oldStatus\":1}", OrderInfoVO.class);
        assertEquals(0, v0.orderStatus);
        assertEquals(1, v0.oldStatus);
        assertEquals(0, v0.oldOrderStatus);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        OrderInfoVO v0 = JSON.parseObject("{\"oldOrderStatus\":1}", OrderInfoVO.class);
        assertEquals(0, v0.orderStatus);
        assertEquals(0, v0.oldStatus);
        assertEquals(1, v0.oldOrderStatus);
    }

    public static class OrderInfoVO {
        private int orderStatus;
        private int oldStatus;
        private int oldOrderStatus;

        public int getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(int orderStatus) {
            this.orderStatus = orderStatus;
        }

        public int getOldStatus() {
            return oldStatus;
        }

        public void setOldStatus(int oldStatus) {
            this.oldStatus = oldStatus;
        }

        public int getOldOrderStatus() {
            return oldOrderStatus;
        }

        public void setOldOrderStatus(int oldOrderStatus) {
            this.oldOrderStatus = oldOrderStatus;
        }
    }
}

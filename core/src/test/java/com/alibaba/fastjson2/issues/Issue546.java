package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue546 {
    @Test
    public void test() {
        A vo = new A();
        vo.setCId("2");
        assertEquals("{\"CId\":\"2\"}", JSON.toJSONString(vo));
    }

    @Data
    public static class A {
        private String uId;
        private String cId;
    }

    @Test
    public void test1() {
        B vo = new B();
        vo.cId = "2";
        assertEquals("{\"cId\":\"2\"}", JSON.toJSONString(vo));
    }

    public static class B {
        public String uId;
        public String cId;
    }
}

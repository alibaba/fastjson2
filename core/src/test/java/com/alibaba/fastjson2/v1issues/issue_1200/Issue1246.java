package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by kimmking on 06/06/2017.
 */
public class Issue1246 {
    @Test
    public void test_for_issue() throws Exception {
        B b = new B();
        b.setX("xx");

        String test = JSON.toJSONString(b);
        System.out.println(test);
        assertEquals("{}", test);

        C c = new C();
        c.ab = b;

        String testC = JSON.toJSONString(c);
        System.out.println(testC);
        assertEquals("{\"ab\":{}}", testC);

        D d = new D();
        d.setAb(b);

        String testD = JSON.toJSONString(d);
        System.out.println(testD);
        assertEquals("{\"ab\":{}}", testD);
    }

    public static class C {
        public A ab;
    }

    public static class D {
        private A ab;

        public A getAb() {
            return ab;
        }

        public void setAb(A ab) {
            this.ab = ab;
        }
    }

    public static class A {
        private String x;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }
    }

    public static class B
            extends A {
        private String x;

        @Override
        @JSONField(serialize = false)
        public String getX() {
            return x;
        }

        @Override
        public void setX(String x) {
            this.x = x;
        }
    }
}

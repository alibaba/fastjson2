package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1500 {
    @Test
    public void test_for_issue() throws Exception {
        // test aa
        Aa aa = new Aa();
        aa.setName("aa");
        String jsonAa = JSON.toJSONString(aa);
//        System.out.println(jsonAa);

        Aa aa1 = JSON.parseObject(jsonAa, Aa.class);
        assertEquals("aa", aa1.getName());

        // test C
        C c = new C();
        c.setE(aa);
        String jsonC = JSON.toJSONString(c, JSONWriter.Feature.WriteClassName);

        C c1 = JSON.parseObject(jsonC, C.class);

        C c2 = JSON.parseObject(jsonC, C.class, JSONReader.Feature.SupportAutoType);

        assertEquals("Aa", c2.getE().getClass().getSimpleName());
        assertEquals("aa", ((Aa) c2.getE()).getName());
    }

    public static class Aa
            extends Exception {
        public Aa() {
        }

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class C {
        private Exception e;

        public Exception getE() {
            return e;
        }

        public void setE(Exception e) {
            this.e = e;
        }
    }
}

package com.alibaba.fastjson.generic;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericTypeTest {
    @Test
    public void testEmbeddedGenericType() throws Exception {
        String jsonString = "{\"g\":{\"b\":{\"s\":'1'}}}";

        Str t = JSON.parseObject(jsonString, Str.class);
        Object o = t.getG().getB();
        System.out.println("Inner Class EmbeddedGenericType test => " + o.getClass().getName());
        assertEquals(Bean.class.getName(), o.getClass().getName());

        TStr t1 = JSON.parseObject(jsonString, TStr.class);
        Object o1 = t1.getG().getB();
        System.out.println("Public Class EmbeddedGenericType test => " + o1.getClass().getName());
        assertEquals(TBean.class.getName(), o1.getClass().getName());
    }

    static class Str
            implements Serializable {
        Gen<Bean> g;

        public Gen<Bean> getG() {
            return g;
        }

        public void setG(Gen<Bean> g) {
            this.g = g;
        }
    }

    static class Gen<G>
            implements Serializable {
        G b;

        public G getB() {
            return b;
        }

        public void setB(G b) {
            this.b = b;
        }
    }

    static class Bean
            implements Serializable {
        String s;

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }
    }
}

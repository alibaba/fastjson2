package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue351 {
    @Test
    public void test() {
        B b = new B();
        b.setC("1");
        b.setA(null);

        ValueFilter valueFilter = (o, s, o1) -> {
            if (Objects.isNull(o1)) {
                return "";
            } else {
                return o1;
            }
        };

        assertEquals("{\"a\":\"\",\"c\":\"1\"}", JSON.toJSONString(b, valueFilter, JSONWriter.Feature.WriteNulls));
    }

    public static class A {
        String a;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }
    }

    public static class B {
        String c;
        A a;

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }

    @Test
    public void test1() {
        ValueFilter valueFilter = (o, s, o1) -> {
            if (Objects.isNull(o1)) {
                return "";
            } else {
                return o1;
            }
        };

        Bean bean = new Bean();
        assertEquals("{\"values\":\"\"}", JSON.toJSONString(bean, valueFilter, JSONWriter.Feature.WriteNulls));
    }

    public static class Bean {
        public List<String> values;
    }
}

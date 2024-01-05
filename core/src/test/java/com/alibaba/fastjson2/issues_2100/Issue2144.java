package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class Issue2144 {
    @Test
    public void test() {
        final A a = new A();
        B b = a.buildB();
        Object json = JSON.toJSON(b);
        System.out.println(JSON.toJSONString(json, JSONWriter.Feature.ReferenceDetection));
    }

    @Data
    public class A {
        public B buildB() {
            return buildB(new B());
        }

        public <T extends B> T buildB(T b) {
            return b;
        }
    }

    @Data
    public class B {
        public B buildB() {
            return buildB(new B());
        }

        public <T extends B> T buildB(T b) {
            return b;
        }
    }
}

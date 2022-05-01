package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JSONBTableTest6 {
    @Test
    public void test_0() {
        JSONObject object = JSONObject.of("id", 1001);
        A a = new A(object, object);
        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        A a1 = JSONB.parseObject(bytes, A.class);

        assertNotNull(a1);
        assertSame(a1.a, a1.b);
    }

    @Test
    public void test_1() {
        JSONObject object = JSONObject.of("id", 1001);
        A a = new A(object, object);
        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);

        JSONBDump.dump(bytes);

        A a1 = (A) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);

        assertNotNull(a1);
        assertSame(a1.a, a1.b);
    }

    static class A {
        private JSONObject a;
        private JSONObject b;

        public A(JSONObject a, JSONObject b) {
            this.a = a;
            this.b = b;
        }

        public JSONObject getA() {
            return a;
        }

        public JSONObject getB() {
            return b;
        }

        public void setA(JSONObject a) {
            this.a = a;
        }

        public void setB(JSONObject b) {
            this.b = b;
        }
    }
}

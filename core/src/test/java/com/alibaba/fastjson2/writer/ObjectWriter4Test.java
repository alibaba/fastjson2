package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectWriter4Test {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);

        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals(str, jsonObject.toString());

        assertEquals(bean.f0, JSONPath.eval(bean, "$.f0"));
        assertEquals(bean.f1, JSONPath.eval(bean, "$.f1"));
        assertEquals(bean.f2, JSONPath.eval(bean, "$.f2"));
        assertEquals(bean.f3, JSONPath.eval(bean, "$.f3"));
        assertNull(JSONPath.eval(bean, "$.f100"));
    }

    @Test
    public void testJsonb() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
    }

    @Test
    public void testJsonbArray() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
    }

    static class Bean {
        private int f0;
        private int f1;
        private int f2;
        private int f3;

        public int getF0() {
            return f0;
        }

        public void setF0(int f0) {
            this.f0 = f0;
        }

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public int getF3() {
            return f3;
        }

        public void setF3(int f3) {
            this.f3 = f3;
        }
    }
}

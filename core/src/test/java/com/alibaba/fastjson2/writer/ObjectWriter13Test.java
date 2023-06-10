package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectWriter13Test {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        bean.f4 = 14;
        bean.f5 = 15;
        bean.f6 = 16;
        bean.f7 = 17;
        bean.f8 = 18;
        bean.f9 = 19;
        bean.f10 = 20;
        bean.f11 = 21;
        bean.f12 = 22;
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
        assertEquals(bean.f4, bean1.f4);
        assertEquals(bean.f5, bean1.f5);
        assertEquals(bean.f6, bean1.f6);
        assertEquals(bean.f7, bean1.f7);
        assertEquals(bean.f8, bean1.f8);
        assertEquals(bean.f9, bean1.f9);
        assertEquals(bean.f10, bean1.f10);
        assertEquals(bean.f11, bean1.f11);
        assertEquals(bean.f12, bean1.f12);

        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals(str, jsonObject.toString());

        assertEquals(bean.f0, TestUtils.eval(bean, "$.f0"));
        assertEquals(bean.f1, TestUtils.eval(bean, "$.f1"));
        assertEquals(bean.f2, TestUtils.eval(bean, "$.f2"));
        assertEquals(bean.f3, TestUtils.eval(bean, "$.f3"));
        assertEquals(bean.f4, TestUtils.eval(bean, "$.f4"));
        assertEquals(bean.f5, TestUtils.eval(bean, "$.f5"));
        assertEquals(bean.f6, TestUtils.eval(bean, "$.f6"));
        assertEquals(bean.f7, TestUtils.eval(bean, "$.f7"));
        assertEquals(bean.f8, TestUtils.eval(bean, "$.f8"));
        assertEquals(bean.f9, TestUtils.eval(bean, "$.f9"));
        assertEquals(bean.f10, TestUtils.eval(bean, "$.f10"));
        assertEquals(bean.f11, TestUtils.eval(bean, "$.f11"));
        assertEquals(bean.f12, TestUtils.eval(bean, "$.f12"));
        assertNull(TestUtils.eval(bean, "$.f100"));

        ObjectWriter objectWriter = JSONFactory.defaultObjectWriterProvider.getObjectWriter(Bean.class);
        assertEquals(
                Bean.class.getName(),
                objectWriter.toString()
        );
        assertEquals(0, objectWriter.getFeatures());
        objectWriter.setPropertyFilter(null);
        objectWriter.setPropertyPreFilter(null);
    }

    @Test
    public void testJsonb() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        bean.f4 = 14;
        bean.f5 = 15;
        bean.f6 = 16;
        bean.f7 = 17;
        bean.f8 = 18;
        bean.f9 = 19;
        bean.f10 = 20;
        bean.f11 = 21;
        bean.f12 = 22;
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
        assertEquals(bean.f4, bean1.f4);
        assertEquals(bean.f5, bean1.f5);
        assertEquals(bean.f6, bean1.f6);
        assertEquals(bean.f7, bean1.f7);
        assertEquals(bean.f8, bean1.f8);
        assertEquals(bean.f9, bean1.f9);
        assertEquals(bean.f10, bean1.f10);
        assertEquals(bean.f11, bean1.f11);
        assertEquals(bean.f12, bean1.f12);
    }

    @Test
    public void testJsonbArray() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;
        bean.f3 = 13;
        bean.f4 = 14;
        bean.f5 = 15;
        bean.f6 = 16;
        bean.f7 = 17;
        bean.f8 = 18;
        bean.f9 = 19;
        bean.f10 = 20;
        bean.f11 = 21;
        bean.f12 = 22;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
        assertEquals(bean.f4, bean1.f4);
        assertEquals(bean.f5, bean1.f5);
        assertEquals(bean.f6, bean1.f6);
        assertEquals(bean.f7, bean1.f7);
        assertEquals(bean.f8, bean1.f8);
        assertEquals(bean.f9, bean1.f9);
        assertEquals(bean.f10, bean1.f10);
        assertEquals(bean.f11, bean1.f11);
        assertEquals(bean.f12, bean1.f12);
    }

    static class Bean {
        private int f0;
        private int f1;
        private int f2;
        private int f3;
        private int f4;
        private int f5;
        private int f6;
        private int f7;
        private int f8;
        private int f9;
        private int f10;
        private int f11;
        private int f12;

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

        public int getF4() {
            return f4;
        }

        public void setF4(int f4) {
            this.f4 = f4;
        }

        public int getF5() {
            return f5;
        }

        public void setF5(int f5) {
            this.f5 = f5;
        }

        public int getF6() {
            return f6;
        }

        public void setF6(int f6) {
            this.f6 = f6;
        }

        public int getF7() {
            return f7;
        }

        public void setF7(int f7) {
            this.f7 = f7;
        }

        public int getF8() {
            return f8;
        }

        public void setF8(int f8) {
            this.f8 = f8;
        }

        public int getF9() {
            return f9;
        }

        public void setF9(int f9) {
            this.f9 = f9;
        }

        public int getF10() {
            return f10;
        }

        public void setF10(int f10) {
            this.f10 = f10;
        }

        public int getF11() {
            return f11;
        }

        public void setF11(int f11) {
            this.f11 = f11;
        }

        public int getF12() {
            return f12;
        }

        public void setF12(int f12) {
            this.f12 = f12;
        }
    }
}

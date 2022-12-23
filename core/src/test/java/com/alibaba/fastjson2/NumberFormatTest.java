package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONCompiler;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberFormatTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean {
        @JSONField(format = "#.00")
        public double value;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean1 {
        @JSONField(format = "#.00")
        public float value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean2 {
        @JSONField(format = "#.00")
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean3 {
        @JSONField(format = "#.00")
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean4 {
        @JSONField(format = "#.00")
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean5 {
        @JSONField(format = "#.00")
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    @Test
    public void test6() {
        Bean6 bean = new Bean6();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean6 {
        @JSONField(format = "#.00")
        public Double value;
    }

    @Test
    public void test7() {
        Bean7 bean = new Bean7();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean7 {
        @JSONField(format = "#.00")
        public Float value;
    }

    @Test
    public void test8() {
        Bean8 bean = new Bean8();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean8 {
        @JSONField(format = "#.00")
        private Double value;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Test
    public void test9() {
        Bean9 bean = new Bean9();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean9 {
        @JSONField(format = "#.00")
        private Float value;

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }
    }

    @Test
    public void test10() {
        Bean10 bean = new Bean10();
        bean.value = 12.3423789;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean10 {
        @JSONField(format = "#.00")
        private Double value;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Test
    public void test11() {
        Bean11 bean = new Bean11();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    public static class Bean11 {
        @JSONField(format = "#.00")
        private Float value;

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }
    }
}

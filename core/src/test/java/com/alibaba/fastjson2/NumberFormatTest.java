package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONCompiler;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void test12() {
        Bean12 bean = new Bean12();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean12 {
        @JSONField(format = "#.00")
        public BigDecimal value;
    }

    @Test
    public void test13() {
        Bean13 bean = new Bean13();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean13 {
        @JSONField(format = "#.00")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void test14() {
        Bean14 bean = new Bean14();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean14 {
        @JSONField(format = "#.00")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void test15() {
        Bean15 bean = new Bean15();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean15 {
        @JSONField(format = "#.00")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void test16() {
        Bean16 bean = new Bean16();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
    private static class Bean16 {
        @JSONField(format = "#.00")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void test17() {
        Bean17 bean = new Bean17();
        bean.value = new BigDecimal("12.3423789");
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean17 {
        @JSONField(format = "#.00")
        private BigDecimal value;
    }

    @Test
    public void test18() {
        Bean18 bean = new Bean18();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean18 {
        @JSONField(format = "#.00")
        private Float value;
    }

    @Test
    public void test19() {
        Bean19 bean = new Bean19();
        bean.value = 12.3423789F;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean19 {
        @JSONField(format = "#.00")
        private float value;
    }

    @Test
    public void test20() {
        Bean20 bean = new Bean20();
        bean.value = 12.3423789D;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean20 {
        @JSONField(format = "#.00")
        private Double value;
    }

    @Test
    public void test21() {
        Bean21 bean = new Bean21();
        bean.value = 12.3423789D;
        assertEquals("{\"value\":12.34}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[12.34]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean21 {
        @JSONField(format = "#.00")
        private Double value;
    }

    @Test
    public void test22() {
        Bean22 bean = new Bean22();
        bean.value = Arrays.asList(12.3423789D);
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean22 {
        @JSONField(format = "#.00")
        private List<Double> value;
    }

    @Test
    public void test23() {
        Bean23 bean = new Bean23();
        bean.value = Arrays.asList(12.3423789D);
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean23 {
        @JSONField(format = "#.00")
        private List<Double> value;
    }

    @Test
    public void test24() {
        Bean24 bean = new Bean24();
        bean.value = Arrays.asList(12.3423789D);
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean24 {
        @JSONField(format = "#.00")
        private List<Double> value;

        public List<Double> getValue() {
            return value;
        }

        public void setValue(List<Double> value) {
            this.value = value;
        }
    }

    @Test
    public void test25() {
        Bean25 bean = new Bean25();
        bean.value = Arrays.asList(12.3423789F);
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean25 {
        @JSONField(format = "#.00")
        private List<Float> value;
    }

    @Test
    public void test26() {
        Bean26 bean = new Bean26();
        bean.value = Arrays.asList(new BigDecimal("12.3423789"));
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean26 {
        @JSONField(format = "#.00")
        private List<BigDecimal> value;
    }

    @Test
    public void test27() {
        Bean27 bean = new Bean27();
        bean.value = new BigDecimal[]{new BigDecimal("12.3423789")};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean27 {
        @JSONField(format = "#.00")
        private BigDecimal[] value;
    }

    @Test
    public void test28() {
        Bean28 bean = new Bean28();
        bean.value = new Double[]{12.3423789D};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean28 {
        @JSONField(format = "#.00")
        private Double[] value;
    }

    @Test
    public void test29() {
        Bean29 bean = new Bean29();
        bean.value = new Float[]{12.3423789F};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean29 {
        @JSONField(format = "#.00")
        private Float[] value;
    }

    @Test
    public void test30() {
        Bean30 bean = new Bean30();
        bean.value = new Float[]{12.3423789F};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean30 {
        @JSONField(format = "#.00")
        private Float[] value;

        public Float[] getValue() {
            return value;
        }
    }

    @Test
    public void test31() {
        Bean31 bean = new Bean31();
        bean.value = new Double[]{12.3423789D};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean31 {
        @JSONField(format = "#.00")
        private Double[] value;

        public Double[] getValue() {
            return value;
        }
    }

    @Test
    public void test32() {
        Bean32 bean = new Bean32();
        bean.value = new BigDecimal[]{new BigDecimal("12.3423789")};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean32 {
        @JSONField(format = "#.00")
        private BigDecimal[] value;

        public BigDecimal[] getValue() {
            return value;
        }
    }

    @Test
    public void test33() {
        Bean33 bean = new Bean33();
        bean.value = new Float[]{12.3423789F};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean33 {
        @JSONField(format = "#.00")
        private Float[] value;

        public Float[] getValue() {
            return value;
        }
    }

    @Test
    public void test34() {
        Bean34 bean = new Bean34();
        bean.value = new Double[]{12.3423789D};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean34 {
        @JSONField(format = "#.00")
        private Double[] value;

        public Double[] getValue() {
            return value;
        }
    }

    @Test
    public void test35() {
        Bean35 bean = new Bean35();
        bean.value = new BigDecimal[]{new BigDecimal("12.3423789")};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    public static class Bean35 {
        @JSONField(format = "#.00")
        private BigDecimal[] value;

        public BigDecimal[] getValue() {
            return value;
        }
    }

    @Test
    public void test36() {
        Bean36 bean = new Bean36();
        bean.value = new float[]{12.3423789F};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean36 {
        @JSONField(format = "#.00")
        private float[] value;

        public float[] getValue() {
            return value;
        }
    }

    @Test
    public void test37() {
        Bean37 bean = new Bean37();
        bean.value = new double[]{12.3423789D};
        assertEquals("{\"value\":[12.34]}", JSON.toJSONString(bean));
        assertEquals("[[12.34]]", JSON.toJSONString(bean, JSONWriter.Feature.BeanToArray));
    }

    private static class Bean37 {
        @JSONField(format = "#.00")
        private double[] value;

        public double[] getValue() {
            return value;
        }
    }
}

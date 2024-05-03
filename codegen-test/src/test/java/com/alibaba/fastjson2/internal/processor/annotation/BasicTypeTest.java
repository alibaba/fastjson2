package com.alibaba.fastjson2.internal.processor.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
    }

    @JSONCompiled
    public static class Bean {
        public byte f0;
        public short f1;
        public int f2;
        public long f3;
        public float f4;
        public double f5;
        public char f6;
        public boolean f7;
        public String f8;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
    }

    @JSONCompiled
    public static class Bean1 {
        private byte f0;
        private short f1;
        private int f2;
        private long f3;
        private float f4;
        private double f5;
        private char f6;
        private boolean f7;
        private String f8;

        public byte getF0() {
            return f0;
        }

        public void setF0(byte f0) {
            this.f0 = f0;
        }

        public short getF1() {
            return f1;
        }

        public void setF1(short f1) {
            this.f1 = f1;
        }

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public long getF3() {
            return f3;
        }

        public void setF3(long f3) {
            this.f3 = f3;
        }

        public float getF4() {
            return f4;
        }

        public void setF4(float f4) {
            this.f4 = f4;
        }

        public double getF5() {
            return f5;
        }

        public void setF5(double f5) {
            this.f5 = f5;
        }

        public char getF6() {
            return f6;
        }

        public void setF6(char f6) {
            this.f6 = f6;
        }

        public boolean isF7() {
            return f7;
        }

        public void setF7(boolean f7) {
            this.f7 = f7;
        }

        public String getF8() {
            return f8;
        }

        public void setF8(String f8) {
            this.f8 = f8;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.f0 = Byte.MIN_VALUE;
        bean.f1 = Short.MIN_VALUE;
        bean.f2 = Integer.MIN_VALUE;
        bean.f3 = Long.MIN_VALUE;
        bean.f4 = 1F;
        bean.f5 = 1D;
        bean.f6 = 'A';
        bean.f7 = true;
        String str = JSON.toJSONString(bean);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean2 {
        public Byte f0;
        public Short f1;
        public Integer f2;
        public Long f3;
        public Float f4;
        public Double f5;
        public Character f6;
        public Boolean f7;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.f0 = Byte.MIN_VALUE;
        bean.f1 = Short.MIN_VALUE;
        bean.f2 = Integer.MIN_VALUE;
        bean.f3 = Long.MIN_VALUE;
        bean.f4 = 1F;
        bean.f5 = 1D;
        bean.f6 = 'A';
        bean.f7 = true;
        String str = JSON.toJSONString(bean);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean3 {
        private Byte f0;
        private Short f1;
        private Integer f2;
        private Long f3;
        private Float f4;
        private Double f5;
        private Character f6;
        private Boolean f7;

        public Byte getF0() {
            return f0;
        }

        public void setF0(Byte f0) {
            this.f0 = f0;
        }

        public Short getF1() {
            return f1;
        }

        public void setF1(Short f1) {
            this.f1 = f1;
        }

        public Integer getF2() {
            return f2;
        }

        public void setF2(Integer f2) {
            this.f2 = f2;
        }

        public Long getF3() {
            return f3;
        }

        public void setF3(Long f3) {
            this.f3 = f3;
        }

        public Float getF4() {
            return f4;
        }

        public void setF4(Float f4) {
            this.f4 = f4;
        }

        public Double getF5() {
            return f5;
        }

        public void setF5(Double f5) {
            this.f5 = f5;
        }

        public Character getF6() {
            return f6;
        }

        public void setF6(Character f6) {
            this.f6 = f6;
        }

        public Boolean getF7() {
            return f7;
        }

        public void setF7(Boolean f7) {
            this.f7 = f7;
        }
    }
}

package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class PrimitiveArrayTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = new boolean[] {true};
        bean.v02 = new char[] {2};
        bean.v03 = new byte[] {3};
        bean.v04 = new short[] {4};
        bean.v05 = new int[] {5};
        bean.v06 = new long[] {6};
        bean.v07 = new float[] {7};
        bean.v08 = new double[] {8L};

        String str = JSON.toJSONString(bean);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertArrayEquals(bean.v01, bean1.v01);
        assertArrayEquals(bean.v02, bean1.v02);
        assertArrayEquals(bean.v03, bean1.v03);
        assertArrayEquals(bean.v04, bean1.v04);
        assertArrayEquals(bean.v05, bean1.v05);
        assertArrayEquals(bean.v06, bean1.v06);
        assertArrayEquals(bean.v07, bean1.v07);
        assertArrayEquals(bean.v07, bean1.v07);
        assertArrayEquals(bean.v08, bean1.v08);
    }

    @JSONCompiled
    public static class Bean {
        public boolean[] v01;
        public char[] v02;
        public byte[] v03;
        public short[] v04;
        public int[] v05;
        public long[] v06;
        public float[] v07;
        public double[] v08;
    }
}

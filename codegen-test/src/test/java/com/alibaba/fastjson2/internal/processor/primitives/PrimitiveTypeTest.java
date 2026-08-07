package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = true;
        bean.v02 = 2;
        bean.v03 = 3;
        bean.v04 = 4;
        bean.v05 = 5;
        bean.v06 = 6;
        bean.v07 = 7;
        bean.v08 = 8L;

        String str = JSON.toJSONString(bean);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
        assertEquals(bean.v02, bean1.v02);
        assertEquals(bean.v03, bean1.v03);
        assertEquals(bean.v04, bean1.v04);
        assertEquals(bean.v05, bean1.v05);
        assertEquals(bean.v06, bean1.v06);
        assertEquals(bean.v07, bean1.v07);
        assertEquals(bean.v07, bean1.v07);
        assertEquals(bean.v08, bean1.v08);
    }

    @JSONCompiled
    public static class Bean {
        public boolean v01;
        public char v02;
        public byte v03;
        public short v04;
        public int v05;
        public long v06;
        public float v07;
        public double v08;
    }
}

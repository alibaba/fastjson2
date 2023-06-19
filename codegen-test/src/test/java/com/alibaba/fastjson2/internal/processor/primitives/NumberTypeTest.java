package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = 1;
        bean.v02 = 2;
        bean.v03 = 3;
        bean.v04 = 4;
        bean.v05 = 5;
        bean.v06 = 7;
        bean.v07 = 7;
        bean.v08 = 8L;
        bean.v09 = 9;
        bean.v10 = 10F;
        bean.v11 = 11;
        bean.v12 = 12D;
        bean.v13 = BigDecimal.valueOf(13);
        bean.v14 = BigInteger.valueOf(14);
        bean.v15 = 15;
        bean.v16 = new AtomicInteger(16);
        bean.v17 = new AtomicLong(17);

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
        assertEquals(bean.v09, bean1.v09);
        assertEquals(bean.v10, bean1.v10);
        assertEquals(bean.v11, bean1.v11);
        assertEquals(bean.v12, bean1.v12);
        assertEquals(bean.v13, bean1.v13);
        assertEquals(bean.v14, bean1.v14);
        assertEquals(bean.v15, bean1.v15);
        assertEquals(bean.v16.get(), bean1.v16.get());
        assertEquals(bean.v17.get(), bean1.v17.get());
    }

    @JSONCompiled
    public static class Bean {
        public byte v01;
        public Byte v02;
        public short v03;
        public Short v04;
        public int v05;
        public Integer v06;
        public long v07;
        public Long v08;
        public float v09;
        public Float v10;
        public double v11;
        public Double v12;
        public BigDecimal v13;
        public BigInteger v14;
        public Number v15;
        public AtomicInteger v16;
        public AtomicLong v17;
    }
}

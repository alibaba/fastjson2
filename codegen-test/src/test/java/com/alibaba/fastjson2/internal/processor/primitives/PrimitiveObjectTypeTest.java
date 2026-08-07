package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveObjectTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = true;
        bean.v02 = 2;
        bean.v03 = 3;
        bean.v04 = 4;
        bean.v05 = 5;
        bean.v06 = 6L;
        bean.v07 = 7F;
        bean.v08 = 8D;
        bean.v09 = "09";

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
    }

    @JSONCompiled
    public static class Bean {
        public Boolean v01;
        public Character v02;
        public Byte v03;
        public Short v04;
        public Integer v05;
        public Long v06;
        public Float v07;
        public Double v08;
        public String v09;
    }
}

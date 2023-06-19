package com.alibaba.fastjson2.internal.processor.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTypeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.v01 = Calendar.getInstance();
        bean.v02 = Currency.getInstance("CNY");
        bean.v03 = new Date();
        bean.v04 = Locale.US;
        bean.v05 = UUID.randomUUID();

        String str = JSON.toJSONString(bean);

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.v01, bean1.v01);
        assertEquals(bean.v02, bean1.v02);
        assertEquals(bean.v03, bean1.v03);
        assertEquals(bean.v04, bean1.v04);
        assertEquals(bean.v05, bean1.v05);
    }

    @JSONCompiled
    public static class Bean {
        public Calendar v01;
        public Currency v02;
        public Date v03;
        public Locale v04;
        public UUID v05;
    }
}

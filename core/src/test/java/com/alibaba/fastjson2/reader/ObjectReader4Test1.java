package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader4Test1 {
    @Test
    public void test() {
        String str = JSONObject
                .of()
                .fluentPut("f01", 1)
                .fluentPut("f02", 2)
                .fluentPut("f03", 3)
                .fluentPut("f04", 4)
                .toString();

        {
            Bean bean = JSON.parseObject(str, Bean.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
        }
        {
            Bean bean = JSON.parseObject(str).to(Bean.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
        }
        {
            Bean1 bean = JSON.parseObject(str, Bean1.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
        }
        {
            Bean2 bean = JSON.parseObject(str, Bean2.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
        }
    }

    public static class Bean {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
    }

    private static class Bean1 {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
    }

    public static class Bean2 {
        public final int f01;
        public final int f02;
        public final int f03;
        public final int f04;

        @JSONCreator
        public Bean2(int f01,
                     int f02,
                     int f03,
                     int f04) {
            this.f01 = f01;
            this.f02 = f02;
            this.f03 = f03;
            this.f04 = f04;
        }
    }
}

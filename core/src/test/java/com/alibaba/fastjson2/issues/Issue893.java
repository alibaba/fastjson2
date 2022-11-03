package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue893 {
    @Test
    public void test() {
        String str = JSONObject
                .of()
                .fluentPut("f01", 1)
                .fluentPut("f02", 2)
                .fluentPut("f03", 3)
                .fluentPut("f04", 4)
                .fluentPut("f05", 5)
                .fluentPut("f06", 6)
                .fluentPut("f07", 7)
                .fluentPut("f08", 8)
                .fluentPut("f09", 9)
                .fluentPut("f10", 10)
                .fluentPut("f11", 11)
                .fluentPut("f12", 12)
                .toString();

        {
            Bean bean = JSON.parseObject(str, Bean.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
            assertEquals(5, bean.f05);
            assertEquals(6, bean.f06);
            assertEquals(7, bean.f07);
            assertEquals(8, bean.f08);
            assertEquals(9, bean.f09);
            assertEquals(10, bean.f10);
            assertEquals(11, bean.f11);
            assertEquals(12, bean.f12);
        }
        {
            Bean1 bean = JSON.parseObject(str, Bean1.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
            assertEquals(5, bean.f05);
            assertEquals(6, bean.f06);
            assertEquals(7, bean.f07);
            assertEquals(8, bean.f08);
            assertEquals(9, bean.f09);
            assertEquals(10, bean.f10);
            assertEquals(11, bean.f11);
            assertEquals(12, bean.f12);
        }
        {
            Bean2 bean = JSON.parseObject(str, Bean2.class);
            assertEquals(1, bean.f01);
            assertEquals(2, bean.f02);
            assertEquals(3, bean.f03);
            assertEquals(4, bean.f04);
            assertEquals(5, bean.f05);
            assertEquals(6, bean.f06);
            assertEquals(7, bean.f07);
            assertEquals(8, bean.f08);
            assertEquals(9, bean.f09);
            assertEquals(10, bean.f10);
            assertEquals(11, bean.f11);
            assertEquals(12, bean.f12);
        }
    }

    public static class Bean {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
        public int f05;
        public int f06;
        public int f07;
        public int f08;
        public int f09;
        public int f10;
        public int f11;
        public int f12;
    }

    private static class Bean1 {
        public int f01;
        public int f02;
        public int f03;
        public int f04;
        public int f05;
        public int f06;
        public int f07;
        public int f08;
        public int f09;
        public int f10;
        public int f11;
        public int f12;
    }

    public static class Bean2 {
        public final int f01;
        public final int f02;
        public final int f03;
        public final int f04;
        public final int f05;
        public final int f06;
        public final int f07;
        public final int f08;
        public final int f09;
        public final int f10;
        public final int f11;
        public final int f12;

        @JSONCreator
        public Bean2(int f01,
                     int f02,
                     int f03,
                     int f04,
                     int f05,
                     int f06,
                     int f07,
                     int f08,
                     int f09,
                     int f10,
                     int f11,
                     int f12) {
            this.f01 = f01;
            this.f02 = f02;
            this.f03 = f03;
            this.f04 = f04;
            this.f05 = f05;
            this.f06 = f06;
            this.f07 = f07;
            this.f08 = f08;
            this.f09 = f09;
            this.f10 = f10;
            this.f11 = f11;
            this.f12 = f12;
        }
    }
}

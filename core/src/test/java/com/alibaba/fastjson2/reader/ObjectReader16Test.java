package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader16Test {
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
                .fluentPut("f13", 13)
                .fluentPut("f14", 14)
                .fluentPut("f15", 15)
                .fluentPut("f16", 16)
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
            assertEquals(13, bean.f13);
            assertEquals(14, bean.f14);
            assertEquals(15, bean.f15);
            assertEquals(16, bean.f16);
        }
        {
            Bean bean = JSON.parseObject(str).to(Bean.class);
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
            assertEquals(13, bean.f13);
            assertEquals(14, bean.f14);
            assertEquals(15, bean.f15);
            assertEquals(16, bean.f16);
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
            assertEquals(13, bean.f13);
            assertEquals(14, bean.f14);
            assertEquals(15, bean.f15);
            assertEquals(16, bean.f16);
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
            assertEquals(13, bean.f13);
            assertEquals(14, bean.f14);
            assertEquals(15, bean.f15);
            assertEquals(16, bean.f16);
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
        public int f13;
        public int f14;
        public int f15;
        public int f16;
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
        public int f13;
        public int f14;
        public int f15;
        public int f16;
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
        public final int f13;
        public final int f14;
        public final int f15;
        public final int f16;

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
                     int f12,
                     int f13,
                     int f14,
                     int f15,
                     int f16) {
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
            this.f13 = f13;
            this.f14 = f14;
            this.f15 = f15;
            this.f16 = f16;
        }
    }
}

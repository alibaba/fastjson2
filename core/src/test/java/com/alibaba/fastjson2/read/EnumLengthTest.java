package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumLengthTest {
    @Test
    public void test_0() {
        Bean bean = JSON.parseObject("{\"type0\":\"A1\",\"type1\":\"A20\",\"type2\":\"A300\",\"type3\":\"A4000\",\"type4\":\"A50000\"}".getBytes(), Bean.class);
        assertEquals(Type.A1, bean.type0);
        assertEquals(Type.A20, bean.type1);
        assertEquals(Type.A300, bean.type2);
        assertEquals(Type.A4000, bean.type3);
        assertEquals(Type.A50000, bean.type4);
    }

    @Test
    public void test_1() {
        Bean bean = JSON.parseObject("{\"type0\":\"A50000\",\"type1\":\"A600000\",\"type2\":\"A7000000\",\"type3\":\"A80000000\",\"type4\":\"A900000000\"}".getBytes(), Bean.class);
        assertEquals(Type.A50000, bean.type0);
        assertEquals(Type.A600000, bean.type1);
        assertEquals(Type.A7000000, bean.type2);
        assertEquals(Type.A80000000, bean.type3);
        assertEquals(Type.A900000000, bean.type4);
    }

    @Test
    public void test_2() {
        Bean bean = JSON.parseObject("{\"type0\":\"A600000\",\"type1\":\"A7000000\",\"type2\":\"A80000000\",\"type3\":\"A900000000\",\"type4\":\"AA000000000\"}".getBytes(), Bean.class);
        assertEquals(Type.A600000, bean.type0);
        assertEquals(Type.A7000000, bean.type1);
        assertEquals(Type.A80000000, bean.type2);
        assertEquals(Type.A900000000, bean.type3);
        assertEquals(Type.AA000000000, bean.type4);
    }

    @Test
    public void test_3() {
        Bean bean = JSON.parseObject("{\"type0\":\"A7000000\",\"type1\":\"A80000000\",\"type2\":\"A900000000\",\"type3\":\"AA000000000\",\"type4\":\"A1\"}".getBytes(), Bean.class);
        assertEquals(Type.A7000000, bean.type0);
        assertEquals(Type.A80000000, bean.type1);
        assertEquals(Type.A900000000, bean.type2);
        assertEquals(Type.AA000000000, bean.type3);
        assertEquals(Type.A1, bean.type4);
    }

    @Test
    public void test_4() {
        Bean bean = JSON.parseObject("{\"type0\":\"A80000000\",\"type1\":\"A900000000\",\"type2\":\"AA000000000\",\"type3\":\"A1\",\"type4\":\"A20\"}".getBytes(), Bean.class);
        assertEquals(Type.A80000000, bean.type0);
        assertEquals(Type.A900000000, bean.type1);
        assertEquals(Type.AA000000000, bean.type2);
        assertEquals(Type.A1, bean.type3);
        assertEquals(Type.A20, bean.type4);
    }

    @Test
    public void test_5() {
        Bean bean = JSON.parseObject("{\"type0\":\"A900000000\",\"type1\":\"AA000000000\",\"type2\":\"A1\",\"type3\":\"A20\",\"type4\":\"A300\"}".getBytes(), Bean.class);
        assertEquals(Type.A900000000, bean.type0);
        assertEquals(Type.AA000000000, bean.type1);
        assertEquals(Type.A1, bean.type2);
        assertEquals(Type.A20, bean.type3);
        assertEquals(Type.A300, bean.type4);
    }

    public static class Bean {
        public Type type0;
        public Type type1;
        public Type type2;
        public Type type3;
        public Type type4;
    }

    public enum Type {
        A1,
        A20,
        A300,
        A4000,
        A50000,
        A600000,
        A7000000,
        A80000000,
        A900000000,
        AA000000000
    }
}

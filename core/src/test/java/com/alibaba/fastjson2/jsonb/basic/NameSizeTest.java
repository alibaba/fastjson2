package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameSizeTest {
    @Test
    public void name1() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name1.class).n);
    }

    @Test
    public void name1Symbol() {
        Name1 bean = new Name1();
        bean.n = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNameAsSymbol);
        assertEquals(bean.n, JSONB.parseObject(jsonbBytes, Name1.class).n);
    }

    public static class Name1 {
        public int n;
    }

    @Test
    public void name2() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n2", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name2.class).n2);
    }

    @Test
    public void name2Symbol() {
        Name2 bean = new Name2();
        bean.n2 = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNameAsSymbol);
        assertEquals(bean.n2, JSONB.parseObject(jsonbBytes, Name2.class).n2);
    }

    public static class Name2 {
        public int n2;
    }

    @Test
    public void name3() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n23", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name3.class).n23);
    }

    @Test
    public void name3Symbol() {
        Name3 bean = new Name3();
        bean.n23 = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNameAsSymbol);
        assertEquals(bean.n23, JSONB.parseObject(jsonbBytes, Name3.class).n23);
    }

    public static class Name3 {
        public int n23;
    }

    @Test
    public void name4() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n234", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name4.class).n234);
    }

    @Test
    public void name4Symbol() {
        Name4 bean = new Name4();
        bean.n234 = 13;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNameAsSymbol);
        assertEquals(bean.n234, JSONB.parseObject(jsonbBytes, Name4.class).n234);
    }

    @Test
    public void name4Symbol2() {
        Name4[] beans = new Name4[3];
        beans[0] = new Name4();
        beans[0].n234 = 13;

        beans[1] = new Name4();
        beans[1].n234 = 14;

        beans[2] = new Name4();
        beans[2].n234 = 15;

        byte[] jsonbBytes = JSONB.toBytes(beans, JSONWriter.Feature.WriteNameAsSymbol);

        System.out.println(JSONB.toJSONString(jsonbBytes, true));

        Name4[] beans2 = JSONB.parseObject(jsonbBytes, Name4[].class);
        assertEquals(beans.length, beans2.length);
        for (int i = 0; i < beans.length; i++) {
            assertEquals(beans[i].n234, beans2[i].n234);
        }
    }

    public static class Name4 {
        public int n234;
    }

    @Test
    public void name5() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n2345", value).toJSONBBytes();
        Name5 bean = JSONB.parseObject(jsonbBytes, Name5.class);
        assertEquals(value, bean.n2345);
        byte[] bytes = JSONB.toBytes(bean);
        assertArrayEquals(jsonbBytes, bytes);
    }

    public static class Name5 {
        public int n2345;
    }

    @Test
    public void name6() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n23456", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name6.class).n23456);
    }

    public static class Name6 {
        public int n23456;
    }

    @Test
    public void name7() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n234567", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name7.class).n234567);
    }

    public static class Name7 {
        public int n234567;
    }

    @Test
    public void name8() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n2345678", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name8.class).n2345678);
    }

    public static class Name8 {
        public int n2345678;
    }

    @Test
    public void name9() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n23456789", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name9.class).n23456789);
    }

    public static class Name9 {
        public int n23456789;
    }

    @Test
    public void name10() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n234567890", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name10.class).n234567890);
    }

    public static class Name10 {
        public int n234567890;
    }

    @Test
    public void name11() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of("n2345678901", value).toJSONBBytes();
        assertEquals(value, JSONB.parseObject(jsonbBytes, Name11.class).n2345678901);
    }

    public static class Name11 {
        public int n2345678901;
    }

    @Test
    public void nameX11() {
        int value = 13;
        byte[] jsonbBytes = JSONObject.of(
                "n32", value,
                "n432", value,
                "n5432", value
        ).toJSONBBytes();
        NameX4 bean = JSONB.parseObject(jsonbBytes, NameX4.class);
        assertEquals(value, bean.n32);
        assertEquals(value, bean.n432);
        assertEquals(value, bean.n5432);
    }

    @Test
    public void nameX11Symbol() {
        NameX4[] beans = new NameX4[10];
        for (int i = 0; i < beans.length; i++) {
            beans[i] = new NameX4();
            beans[i].n32 = 11;
            beans[i].n432 = 12;
            beans[i].n5432 = 13;
        }

        byte[] jsonbBytes = JSONB.toBytes(beans, JSONWriter.Feature.WriteNameAsSymbol);
        NameX4[] beans2 = JSONB.parseObject(jsonbBytes, NameX4[].class);
        assertEquals(beans.length, beans2.length);
        for (int i = 0; i < beans.length; i++) {
            assertEquals(beans[i].n32, beans[i].n32);
            assertEquals(beans[i].n432, beans[i].n432);
            assertEquals(beans[i].n5432, beans[i].n5432);
        }
    }

    public static class NameX4 {
        public int n32;
        public int n432;
        public int n5432;
    }
}

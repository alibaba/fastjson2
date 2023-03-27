package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumSetTest {
    @Test
    public void test0() {
        EnumSet<TimeUnit> enumSet = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);

        {
            String json = JSON.toJSONString(enumSet);
            assertEquals("[\"SECONDS\",\"DAYS\"]", json);

            EnumSet<TimeUnit> enumSet2 = JSON.parseObject(json, new TypeReference<EnumSet<TimeUnit>>() {
            });
            assertEquals(enumSet.size(), enumSet2.size());
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(enumSet);
            EnumSet<TimeUnit> enumSet2 = JSONB.parseObject(jsonbBytes, new TypeReference<EnumSet<TimeUnit>>() {
            });
            assertEquals(enumSet.size(), enumSet2.size());
        }
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.units = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);
        String json = JSON.toJSONString(bean);
        assertEquals("{\"units\":[\"SECONDS\",\"DAYS\"]}", json);
        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.units.size(), bean1.units.size());
    }

    @Test
    public void test1_jsonb() {
        Bean bean = new Bean();
        bean.units = EnumSet.of(TimeUnit.DAYS, TimeUnit.SECONDS);
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.units.size(), bean1.units.size());
    }

    public static class Bean {
        public EnumSet<TimeUnit> units;
    }

    @Test
    public void test2_jsonb() {
        Bean2 bean = new Bean2();
        bean.types = EnumSet.of(XType.X100, XType.values());
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean2 bean1 = JSONB.parseObject(jsonbBytes, Bean2.class);
        assertEquals(bean.types.size(), bean1.types.size());
    }

    @Test
    public void test3_jsonb() {
        EnumSet<XType> set = EnumSet.of(XType.X100, XType.values());
        byte[] jsonbBytes = JSONB.toBytes(set, JSONWriter.Feature.WriteClassName);
//        System.out.println(JSONB.toJSONString(jsonbBytes));
        EnumSet<XType> set1 = (EnumSet<XType>) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(set, set1);
    }

    @Test
    public void test3_jsonb1() {
        EnumSet<TimeUnit> set = EnumSet.of(TimeUnit.DAYS, TimeUnit.values());
        byte[] jsonbBytes = JSONB.toBytes(set, JSONWriter.Feature.WriteClassName);
//        System.out.println(JSONB.toJSONString(jsonbBytes));
        EnumSet<TimeUnit> set1 = (EnumSet<TimeUnit>) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(set, set1);
    }

    public static class Bean2 {
        public EnumSet<XType> types;
    }

    public enum XType {
        X100,
        X101,
        X102,
        X103,
        X104,
        X105,
        X106,
        X107,
        X108,
        X109,
        X110,
        X111,
        X112,
        X113,
        X114,
        X115,
        X116,
        X117,
        X118,
        X119,
        X120,
        X121,
        X122,
        X123,
        X124,
        X125,
        X126,
        X127,
        X128,
        X129,
        X130,
        X131,
        X132,
        X133,
        X134,
        X135,
        X136,
        X137,
        X138,
        X139,
        X140,
        X141,
        X142,
        X143,
        X144,
        X145,
        X146,
        X147,
        X148,
        X149,
        X150,
        X151,
        X152,
        X153,
        X154,
        X155,
        X156,
        X157,
        X158,
        X159,
        X160,
        X161,
        X162,
        X163,
        X164,
        X165,
        X166,
        X167,
        X168,
        X169,
        X170,
        X171,
        X172,
        X173,
        X174,
        X175,
        X176,
        X177,
        X178,
        X179,
        X180,
        X181,
        X182,
        X183,
        X184,
        X185,
        X186,
        X187,
        X188,
        X189,
        X190,
        X191,
        X192,
        X193,
        X194,
        X195,
        X196,
        X197,
        X198,
        X199
    }
}

package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2431 {
    @Test
    public void test() {
        Bean0 bean = new Bean0();
        bean.setB((byte) 1);
        bean.setS((short) 1);
        bean.setI(1);
        bean.setL(1L);
        bean.setF(1.0F);
        bean.setD(1.0);
        bean.setZ(Boolean.FALSE);
        assertEquals(
                "{\"b\":\"1\",\"d\":\"1.0\",\"f\":\"1.0\",\"i\":\"1\",\"l\":\"1\",\"s\":\"1\",\"z\":\"false\"}",
                JSON.toJSONString(bean));

        assertEquals(
                "{\"b\":\"1\",\"d\":\"1.0\",\"f\":\"1.0\",\"i\":\"1\",\"l\":\"1\",\"s\":\"1\",\"z\":\"false\"}",
                new String(JSON.toJSONBytes(bean)));

        byte[] bytes = JSONB.toBytes(bean);
        String str2 = JSONB.toJSONString(bytes);
        assertEquals("{\n" +
                        "\t\"b\":\"1\",\n" +
                        "\t\"d\":\"1.0\",\n" +
                        "\t\"f\":\"1.0\",\n" +
                        "\t\"i\":\"1\",\n" +
                        "\t\"l\":\"1\",\n" +
                        "\t\"s\":\"1\",\n" +
                        "\t\"z\":\"false\"\n" +
                        "}",
                str2);
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean0 {
        private Byte b;
        private Short s;
        private Integer i;
        private Long l;
        private Float f;
        private Double d;
        private Boolean z;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.setB((byte) 1);
        bean.setS((short) 1);
        bean.setI(1);
        bean.setL(1L);
        bean.setF(1.0F);
        bean.setD(1.0);
        assertEquals(
                "{\"b\":\"1\",\"d\":\"1.0\",\"f\":\"1.0\",\"i\":\"1\",\"l\":\"1\",\"s\":\"1\",\"z\":\"false\"}",
                JSON.toJSONString(bean));

        assertEquals(
                "{\"b\":\"1\",\"d\":\"1.0\",\"f\":\"1.0\",\"i\":\"1\",\"l\":\"1\",\"s\":\"1\",\"z\":\"false\"}",
                new String(JSON.toJSONBytes(bean)));

        byte[] bytes = JSONB.toBytes(bean);
        String str2 = JSONB.toJSONString(bytes);
        assertEquals("{\n" +
                        "\t\"b\":\"1\",\n" +
                        "\t\"d\":\"1.0\",\n" +
                        "\t\"f\":\"1.0\",\n" +
                        "\t\"i\":\"1\",\n" +
                        "\t\"l\":\"1\",\n" +
                        "\t\"s\":\"1\",\n" +
                        "\t\"z\":\"false\"\n" +
                        "}",
                str2);
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean1 {
        private byte b;
        private short s;
        private int i;
        private long l;
        private float f;
        private double d;
        private boolean z;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.setBs(Arrays.asList((byte) 1));
        bean.setSs(Arrays.asList((short) 1));
        bean.setIs(Arrays.asList(1));
        bean.setLs(Arrays.asList(1L));
        bean.setFs(Arrays.asList(1.0F));
        bean.setDs(Arrays.asList(1.0));
        bean.setZs(Arrays.asList(Boolean.FALSE));
        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                JSON.toJSONString(bean));

        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                new String(JSON.toJSONBytes(bean)));

        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                new String(JSON.toJSONBytes(bean)));

        byte[] bytes = JSONB.toBytes(bean);
        String str2 = JSONB.toJSONString(bytes);
        assertEquals("{\n" +
                        "\t\"bs\":[\"1\"],\n" +
                        "\t\"ds\":[\"1.0\"],\n" +
                        "\t\"fs\":[\"1.0\"],\n" +
                        "\t\"is\":[\"1\"],\n" +
                        "\t\"ls\":[\"1\"],\n" +
                        "\t\"ss\":[\"1\"],\n" +
                        "\t\"zs\":[\"false\"]\n" +
                        "}",
                str2);
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean2 {
        private List<Byte> bs;
        private List<Short> ss;
        private List<Integer> is;
        private List<Long> ls;
        private List<Float> fs;
        private List<Double> ds;
        private List<Boolean> zs;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.setBs(new byte[]{1});
        bean.setSs(new short[]{1});
        bean.setIs(new int[]{1});
        bean.setLs(new long[]{1});
        bean.setFs(new float[]{1});
        bean.setDs(new double[]{1});
        bean.setZs(new boolean[]{false});
        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                JSON.toJSONString(bean));

        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                new String(JSON.toJSONBytes(bean)));

        byte[] bytes = JSONB.toBytes(bean);
        String str2 = JSONB.toJSONString(bytes);
        assertEquals("{\n" +
                        "\t\"bs\":[\"1\"],\n" +
                        "\t\"ds\":[\"1.0\"],\n" +
                        "\t\"fs\":[\"1.0\"],\n" +
                        "\t\"is\":[\"1\"],\n" +
                        "\t\"ls\":[\"1\"],\n" +
                        "\t\"ss\":[\"1\"],\n" +
                        "\t\"zs\":[\"false\"]\n" +
                        "}",
                str2);
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean3 {
        private byte[] bs;
        private short[] ss;
        private int[] is;
        private long[] ls;
        private float[] fs;
        private double[] ds;
        private boolean[] zs;
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.setBs(new Byte[]{1});
        bean.setSs(new Short[]{1});
        bean.setIs(new Integer[]{1});
        bean.setLs(new Long[]{1L});
        bean.setFs(new Float[]{1F});
        bean.setDs(new Double[]{1D});
        bean.setZs(new Boolean[]{Boolean.FALSE});
        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                JSON.toJSONString(bean));

        assertEquals(
                "{\"bs\":[\"1\"],\"ds\":[\"1.0\"],\"fs\":[\"1.0\"],\"is\":[\"1\"],\"ls\":[\"1\"],\"ss\":[\"1\"],\"zs\":[\"false\"]}",
                new String(JSON.toJSONBytes(bean)));

        byte[] bytes = JSONB.toBytes(bean);
        String str2 = JSONB.toJSONString(bytes);
        assertEquals("{\n" +
                        "\t\"bs\":[\"1\"],\n" +
                        "\t\"ds\":[\"1.0\"],\n" +
                        "\t\"fs\":[\"1.0\"],\n" +
                        "\t\"is\":[\"1\"],\n" +
                        "\t\"ls\":[\"1\"],\n" +
                        "\t\"ss\":[\"1\"],\n" +
                        "\t\"zs\":[\"false\"]\n" +
                        "}",
                str2);
    }

    @Data
    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
    public static class Bean4 {
        private Byte[] bs;
        private Short[] ss;
        private Integer[] is;
        private Long[] ls;
        private Float[] fs;
        private Double[] ds;
        private Boolean[] zs;
    }
}

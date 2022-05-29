package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrettyTest {
    @Test
    public void test_pretty_0() {
        Int1 v = new Int1();
        v.setV0000(100);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":100\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_Int64() {
        Long1 v = new Long1();
        v.setV0000(100L);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":100\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_Float() {
        Float1 v = new Float1();
        v.setV0000(100F);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":100.0\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_Double() {
        Double1 v = new Double1();
        v.setV0000(100D);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":100.0\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_Decimal() {
        BigDecimal1 v = new BigDecimal1();
        v.setId(new BigDecimal(100));

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"id\":100\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_BigInt() {
        BigInteger1 v = new BigInteger1();
        v.setId(BigInteger.valueOf(100));

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"id\":100\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_UUID() {
        UUID1 v = new UUID1();
        v.setId(UUID.randomUUID());

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"id\":\"" + v.getId().toString() + "\"\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_0_utf8() {
        Int1 v = new Int1();
        v.setV0000(100);

        JSONWriter jw = JSONWriter.ofPretty(JSONWriter.ofUTF8());
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":100\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_1() {
        Int1 v = new Int1();
        v.setV0000(1234567890);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":1234567890\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_1_utf8() {
        Int1 v = new Int1();
        v.setV0000(1234567890);

        JSONWriter jw = JSONWriter.ofPretty(JSONWriter.ofUTF8());
        jw.writeAny(v);

        System.out.println(jw);
        assertEquals("{\n" +
                        "\t\"v0000\":1234567890\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_object() {
        Int1 v = new Int1();
        v.setV0000(1000);

        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(v);

        assertEquals("{\n" +
                        "\t\"v0000\":1000\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_object_empty() {
        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(Collections.emptyMap());

        assertEquals("{\n" +
                        "\t\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_map_2() {
        LinkedHashMap map = new LinkedHashMap();
        map.put("id", 1001);
        map.put("name", "DataWorks");
        map.put("array", Collections.emptyList());
        map.put("object", Collections.emptyMap());
        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(map);

        assertEquals("{\n" +
                        "\t\"id\":1001,\n" +
                        "\t\"name\":\"DataWorks\",\n" +
                        "\t\"array\":[\n" +
                        "\t\t\n" +
                        "\t],\n" +
                        "\t\"object\":{\n" +
                        "\t\t\n" +
                        "\t}\n" +
                        "}",
                jw.toString());
    }

    @Test
    public void test_pretty_array() {
        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(Collections.emptyList());
        assertEquals("[\n\t\n]", jw.toString());
    }

    @Test
    public void test_pretty_single() {
        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(Collections.singleton(1001));
        assertEquals("[\n" +
                "\t1001\n" +
                "]", jw.toString());
    }

    @Test
    public void test_pretty_two() {
        JSONWriter jw = JSONWriter.ofPretty();
        jw.writeAny(new Integer[]{1001, 1002});
        assertEquals("[\n" +
                "\t1001,\n" +
                "\t1002\n" +
                "]", jw.toString());
    }
}

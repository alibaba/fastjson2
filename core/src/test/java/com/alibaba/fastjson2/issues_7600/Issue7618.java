package com.alibaba.fastjson2.issues_7600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue 7618: JSONReader.Feature.IgnoreSetNullValue is silently ignored by
 * specialized FieldReader subclasses (FieldReaderString, FieldReaderInt32 etc.)
 * because the null-skip gate present in FieldReaderObject was not duplicated
 * into them. The feature must work both via @JSONType.deserializeFeatures and
 * via JSONReader.Feature passed at parse time.
 */
public class Issue7618 {
    @JSONType(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue)
    public static class StringSample {
        private String name = "Default";

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }
    }

    @Test
    public void testStringAnnotation() {
        StringSample s = JSON.parseObject("{\"name\":null}", StringSample.class);
        assertEquals("Default", s.getName());
    }

    @Test
    public void testStringJSONBAnnotation() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("name", null);
        byte[] bytes = JSONB.toBytes(map);
        StringSample s = JSONB.parseObject(bytes, StringSample.class);
        assertEquals("Default", s.getName());
    }

    public static class StringSamplePlain {
        private String name = "Default";

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }
    }

    @Test
    public void testStringContextFeature() {
        StringSamplePlain s = JSON.parseObject(
                "{\"name\":null}",
                StringSamplePlain.class,
                JSONReader.Feature.IgnoreSetNullValue);
        assertEquals("Default", s.getName());
    }

    @JSONType(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue)
    public static class BoxedSample {
        private Integer i = 42;
        private Long l = 100L;
        private Boolean b = Boolean.TRUE;
        private BigDecimal bd = new BigDecimal("3.14");
        private BigInteger bi = BigInteger.TEN;
        private Date d = new Date(0L);
        private Float f = 1.5f;
        private Double db = 2.5d;
        private Short sh = 7;
        private Byte by = 3;
        private Number n = 99;

        public Integer getI() { return i; }
        public Long getL() { return l; }
        public Boolean getB() { return b; }
        public BigDecimal getBd() { return bd; }
        public BigInteger getBi() { return bi; }
        public Date getD() { return d; }
        public Float getF() { return f; }
        public Double getDb() { return db; }
        public Short getSh() { return sh; }
        public Byte getBy() { return by; }
        public Number getN() { return n; }

        public void setI(Integer i) { this.i = i; }
        public void setL(Long l) { this.l = l; }
        public void setB(Boolean b) { this.b = b; }
        public void setBd(BigDecimal bd) { this.bd = bd; }
        public void setBi(BigInteger bi) { this.bi = bi; }
        public void setD(Date d) { this.d = d; }
        public void setF(Float f) { this.f = f; }
        public void setDb(Double db) { this.db = db; }
        public void setSh(Short sh) { this.sh = sh; }
        public void setBy(Byte by) { this.by = by; }
        public void setN(Number n) { this.n = n; }
    }

    @Test
    public void testBoxedAnnotation() {
        BoxedSample s = JSON.parseObject(
                "{\"i\":null,\"l\":null,\"b\":null,\"bd\":null,\"bi\":null,\"d\":null,"
                        + "\"f\":null,\"db\":null,\"sh\":null,\"by\":null,\"n\":null}",
                BoxedSample.class);
        assertEquals(Integer.valueOf(42), s.getI());
        assertEquals(Long.valueOf(100L), s.getL());
        assertEquals(Boolean.TRUE, s.getB());
        assertEquals(new BigDecimal("3.14"), s.getBd());
        assertEquals(BigInteger.TEN, s.getBi());
        assertEquals(new Date(0L), s.getD());
        assertEquals(Float.valueOf(1.5f), s.getF());
        assertEquals(Double.valueOf(2.5d), s.getDb());
        assertEquals(Short.valueOf((short) 7), s.getSh());
        assertEquals(Byte.valueOf((byte) 3), s.getBy());
        assertEquals(99, s.getN());
    }

    public static class BoxedSamplePlain {
        private Integer i = 42;
        private Long l = 100L;
        private Boolean b = Boolean.TRUE;
        private String s = "kept";
        private Float f = 1.5f;
        private Double db = 2.5d;
        private Short sh = 7;
        private Byte by = 3;
        private Date d = new Date(0L);

        public Integer getI() { return i; }
        public Long getL() { return l; }
        public Boolean getB() { return b; }
        public String getS() { return s; }
        public Float getF() { return f; }
        public Double getDb() { return db; }
        public Short getSh() { return sh; }
        public Byte getBy() { return by; }
        public Date getD() { return d; }

        public void setI(Integer i) { this.i = i; }
        public void setL(Long l) { this.l = l; }
        public void setB(Boolean b) { this.b = b; }
        public void setS(String s) { this.s = s; }
        public void setF(Float f) { this.f = f; }
        public void setDb(Double db) { this.db = db; }
        public void setSh(Short sh) { this.sh = sh; }
        public void setBy(Byte by) { this.by = by; }
        public void setD(Date d) { this.d = d; }
    }

    @Test
    public void testBoxedContextFeature() {
        BoxedSamplePlain s = JSON.parseObject(
                "{\"i\":null,\"l\":null,\"b\":null,\"s\":null,"
                        + "\"f\":null,\"db\":null,\"sh\":null,\"by\":null,\"d\":null}",
                BoxedSamplePlain.class,
                JSONReader.Feature.IgnoreSetNullValue);
        assertEquals(Integer.valueOf(42), s.getI());
        assertEquals(Long.valueOf(100L), s.getL());
        assertEquals(Boolean.TRUE, s.getB());
        assertEquals("kept", s.getS());
        assertEquals(Float.valueOf(1.5f), s.getF());
        assertEquals(Double.valueOf(2.5d), s.getDb());
        assertEquals(Short.valueOf((short) 7), s.getSh());
        assertEquals(Byte.valueOf((byte) 3), s.getBy());
        assertEquals(new Date(0L), s.getD());
    }

    @Test
    public void testNonNullValueStillSet() {
        StringSamplePlain s = JSON.parseObject(
                "{\"name\":\"hello\"}",
                StringSamplePlain.class,
                JSONReader.Feature.IgnoreSetNullValue);
        assertEquals("hello", s.getName());
    }
}

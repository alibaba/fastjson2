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

        public Integer getI() { return i; }
        public Long getL() { return l; }
        public Boolean getB() { return b; }
        public BigDecimal getBd() { return bd; }
        public BigInteger getBi() { return bi; }
        public Date getD() { return d; }

        public void setI(Integer i) { this.i = i; }
        public void setL(Long l) { this.l = l; }
        public void setB(Boolean b) { this.b = b; }
        public void setBd(BigDecimal bd) { this.bd = bd; }
        public void setBi(BigInteger bi) { this.bi = bi; }
        public void setD(Date d) { this.d = d; }
    }

    @Test
    public void testBoxedAnnotation() {
        BoxedSample s = JSON.parseObject(
                "{\"i\":null,\"l\":null,\"b\":null,\"bd\":null,\"bi\":null,\"d\":null}",
                BoxedSample.class);
        assertEquals(Integer.valueOf(42), s.getI());
        assertEquals(Long.valueOf(100L), s.getL());
        assertEquals(Boolean.TRUE, s.getB());
        assertEquals(new BigDecimal("3.14"), s.getBd());
        assertEquals(BigInteger.TEN, s.getBi());
        assertEquals(new Date(0L), s.getD());
    }

    public static class BoxedSamplePlain {
        private Integer i = 42;
        private Long l = 100L;
        private Boolean b = Boolean.TRUE;
        private String s = "kept";

        public Integer getI() { return i; }
        public Long getL() { return l; }
        public Boolean getB() { return b; }
        public String getS() { return s; }

        public void setI(Integer i) { this.i = i; }
        public void setL(Long l) { this.l = l; }
        public void setB(Boolean b) { this.b = b; }
        public void setS(String s) { this.s = s; }
    }

    @Test
    public void testBoxedContextFeature() {
        BoxedSamplePlain s = JSON.parseObject(
                "{\"i\":null,\"l\":null,\"b\":null,\"s\":null}",
                BoxedSamplePlain.class,
                JSONReader.Feature.IgnoreSetNullValue);
        assertEquals(Integer.valueOf(42), s.getI());
        assertEquals(Long.valueOf(100L), s.getL());
        assertEquals(Boolean.TRUE, s.getB());
        assertEquals("kept", s.getS());
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

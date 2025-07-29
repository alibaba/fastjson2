package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3515 {
    @Test
    public void test() throws Exception {
        String jsonString = JSON.toJSONString(new Bean(), JSONWriter.Feature.NullAsDefaultValue);
        Bean bean = JSON.parseObject(jsonString, Bean.class);
        assertFalse(bean.getBoolVal());
        assertEquals((byte) 0, bean.getByteVal());
        assertEquals('\u0000', bean.getCharVal());
        assertEquals((short) 0, bean.getShortVal());
        assertEquals(0, bean.getIntVal());
        assertEquals(0L, bean.getLongVal());
        assertEquals(0.0F, bean.getFloatVal());
        assertEquals(0.0D, bean.getDoubleVal());
        assertEquals("", bean.getStringVal());
        assertEquals("{}", bean.getMapVal().toString());
        assertEquals("[]", bean.getListVal().toString());
        assertEquals(BigInteger.ZERO, bean.getIntegerVal());
        assertEquals(new BigDecimal("0.0"), bean.getDecimalVal());
        assertEquals("{}", bean.getObjectVal().toString());
    }

    @Test
    public void test_x() throws Exception {
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean1.class);
        String jsonString = objectWriter.toJSONString(new Bean(), JSONWriter.Feature.NullAsDefaultValue);
        Bean bean = JSON.parseObject(jsonString, Bean.class);
        assertFalse(bean.getBoolVal());
        assertEquals((byte) 0, bean.getByteVal());
        assertEquals('\u0000', bean.getCharVal());
        assertEquals((short) 0, bean.getShortVal());
        assertEquals(0, bean.getIntVal());
        assertEquals(0L, bean.getLongVal());
        assertEquals(0.0F, bean.getFloatVal());
        assertEquals(0.0D, bean.getDoubleVal());
        assertEquals("", bean.getStringVal());
        assertEquals("{}", bean.getMapVal().toString());
        assertEquals("[]", bean.getListVal().toString());
        assertEquals(BigInteger.ZERO, bean.getIntegerVal());
        assertEquals(new BigDecimal("0.0"), bean.getDecimalVal());
        assertEquals("{}", bean.getObjectVal().toString());
    }

    @Getter
    @Setter
    static class Bean {
        private Boolean boolVal;
        private Byte byteVal;
        private Character charVal;
        private Short shortVal;
        private Integer intVal;
        private Long longVal;
        private Float floatVal;
        private Double doubleVal;
        private String stringVal;
        private Map<?, ?> mapVal;
        private List<?> listVal;
        private BigDecimal decimalVal;
        private BigInteger integerVal;
        private Object objectVal;
    }

    @Test
    public void test1() throws Exception {
        String jsonString = JSON.toJSONString(new Bean1(), JSONWriter.Feature.NullAsDefaultValue);
        Bean1 bean = JSON.parseObject(jsonString, Bean1.class);
        assertFalse(bean.boolVal);
        assertEquals((byte) 0, bean.byteVal);
        assertEquals('\u0000', bean.charVal);
        assertEquals((short) 0, bean.shortVal);
        assertEquals(0, bean.intVal);
        assertEquals(0L, bean.longVal);
        assertEquals(0.0F, bean.floatVal);
        assertEquals(0.0D, bean.doubleVal);
        assertEquals("", bean.stringVal);
        assertEquals("{}", bean.mapVal.toString());
        assertEquals("[]", bean.listVal.toString());
        assertEquals(BigInteger.ZERO, bean.integerVal);
        assertEquals(new BigDecimal("0.0"), bean.decimalVal);
        assertEquals("{}", bean.objectVal.toString());
    }

    @Test
    public void test1_x() throws Exception {
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean1.class);
        String jsonString = objectWriter.toJSONString(new Bean1(), JSONWriter.Feature.NullAsDefaultValue);
        Bean1 bean = JSON.parseObject(jsonString, Bean1.class);
        assertFalse(bean.boolVal);
        assertEquals((byte) 0, bean.byteVal);
        assertEquals('\u0000', bean.charVal);
        assertEquals((short) 0, bean.shortVal);
        assertEquals(0, bean.intVal);
        assertEquals(0L, bean.longVal);
        assertEquals(0.0F, bean.floatVal);
        assertEquals(0.0D, bean.doubleVal);
        assertEquals("", bean.stringVal);
        assertEquals("{}", bean.mapVal.toString());
        assertEquals("[]", bean.listVal.toString());
        assertEquals(BigInteger.ZERO, bean.integerVal);
        assertEquals(new BigDecimal("0.0"), bean.decimalVal);
        assertEquals("{}", bean.objectVal.toString());
    }

    static class Bean1 {
        public Boolean boolVal;
        public Byte byteVal;
        public Character charVal;
        public Short shortVal;
        public Integer intVal;
        public Long longVal;
        public Float floatVal;
        public Double doubleVal;
        public String stringVal;
        public Map<?, ?> mapVal;
        public List<?> listVal;
        public BigDecimal decimalVal;
        public BigInteger integerVal;
        public Object objectVal;
    }
}

package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
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
}

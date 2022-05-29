package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONCreatorTest {
    @Test
    public void test_1() {
        VO1 vo = JSON.parseObject("{\"id8\":8,\"id16\":16,\"id32\":32,\"id64\":64}", VO1.class);
        assertEquals(8, vo.id8);
        assertEquals(16, vo.id16);
        assertEquals(32, vo.id32);
        assertEquals(64, vo.id64);
    }

    @Test
    public void test_2() {
        VO2 vo = JSON.parseObject("{\"id8\":8,\"id16\":16,\"id32\":32,\"id64\":64}", VO2.class);
        assertEquals(8, vo.id8.byteValue());
        assertEquals(16, vo.id16.shortValue());
        assertEquals(32, vo.id32.intValue());
        assertEquals(64, vo.id64.longValue());
    }

    @Test
    public void test_3() {
        VO3 vo = JSON.parseObject("{\"flag\":true,\"floatValue\":32,\"doubleValue\":64}", VO3.class);
        assertEquals(true, vo.flag);
        assertEquals(32F, vo.floatValue);
        assertEquals(64D, vo.doubleValue);
    }

    @Test
    public void test_4() {
        VO4 vo = JSON.parseObject("{\"flag\":true,\"floatValue\":32,\"doubleValue\":64}", VO4.class);
        assertEquals(true, vo.flag.booleanValue());
        assertEquals(32F, vo.floatValue);
        assertEquals(64D, vo.doubleValue);
    }

    @Test
    public void test_5() {
        String str = "{\"flag\":true,\"decimalValue\":32,\"bigIntValue\":64,\"strVal\":\"xx\"}";

        {
            VO5 vo = JSON.parseObject(str, VO5.class);
            assertNull(vo.id);
            assertEquals(BigDecimal.valueOf(32), vo.decimalValue);
            assertEquals(BigInteger.valueOf(64), vo.bigIntValue);
            assertEquals("xx", vo.strValue);
        }

        JSONObject jsonObject = JSON.parseObject(str);
        VO5 vo = jsonObject.toJavaObject(VO5.class);
        assertNull(vo.id);
        assertEquals(BigDecimal.valueOf(32), vo.decimalValue);
        assertEquals(BigInteger.valueOf(64), vo.bigIntValue);
        assertEquals("xx", vo.strValue);
    }

    public static class VO1 {
        byte id8;
        short id16;
        int id32;
        long id64;

        @JSONCreator(parameterNames = {"id8", "id16", "id32", "id64"})
        public VO1(byte id8, short id16, int id32, long id64) {
            this.id8 = id8;
            this.id16 = id16;
            this.id32 = id32;
            this.id64 = id64;
        }
    }

    public static class VO2 {
        Byte id8;
        Short id16;
        Integer id32;
        Long id64;

        @JSONCreator(parameterNames = {"id8", "id16", "id32", "id64"})
        public VO2(byte id8, short id16, int id32, long id64) {
            this.id8 = id8;
            this.id16 = id16;
            this.id32 = id32;
            this.id64 = id64;
        }
    }

    public static class VO3 {
        boolean flag;
        float floatValue;
        double doubleValue;

        @JSONCreator(parameterNames = {"flag", "floatValue", "doubleValue"})
        public static VO3 create(boolean flag, float floatValue, double doubleValue) {
            VO3 vo = new VO3();
            vo.flag = flag;
            vo.floatValue = floatValue;
            vo.doubleValue = doubleValue;
            return vo;
        }
    }

    public static class VO4 {
        Boolean flag;
        Float floatValue;
        Double doubleValue;

        @JSONCreator(parameterNames = {"flag", "floatValue", "doubleValue"})
        public static VO4 create(Boolean flag, Float floatValue, Double doubleValue) {
            VO4 vo = new VO4();
            vo.flag = flag;
            vo.floatValue = floatValue;
            vo.doubleValue = doubleValue;
            return vo;
        }
    }

    public static class VO5 {
        UUID id;
        BigDecimal decimalValue;
        BigInteger bigIntValue;
        String strValue;

        @JSONCreator(parameterNames = {"id", ""})
        public static VO5 create(UUID id, @JSONField(name = "decimalValue") BigDecimal decimalValue, @JSONField(name = "bigIntValue") BigInteger bigIntValue, @JSONField(name = "strVal") String strValue) {
            VO5 vo = new VO5();
            vo.id = id;
            vo.decimalValue = decimalValue;
            vo.bigIntValue = bigIntValue;
            vo.strValue = strValue;
            return vo;
        }
    }
}

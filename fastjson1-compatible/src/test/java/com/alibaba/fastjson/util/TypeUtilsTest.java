package com.alibaba.fastjson.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.ParserConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest {
    @Test
    public void cast() {
        assertNull(TypeUtils.cast("null", HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("null", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("NULL", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast(null, (Type) HashMap.class, ParserConfig.global));

        assertThrows(
                Exception.class,
                () -> TypeUtils.cast(new Object(), (Type) HashMap.class, ParserConfig.global)
        );
    }

    @Test
    public void cast1() {
        assertNull(TypeUtils.castToInt(null));
        assertNull(TypeUtils.castToBoolean(null));
        assertNull(TypeUtils.castToLong(null));
        assertNull(TypeUtils.castToDouble(null));
        assertNull(TypeUtils.castToBigDecimal(null));
        assertNull(TypeUtils.castToTimestamp(null));
        assertNull(TypeUtils.castToSqlDate(null));
        assertNull(TypeUtils.castToJavaBean(null, null));
    }

    @Test
    public void castToString() {
        assertNull(TypeUtils.castToString(null));
        assertEquals("123", TypeUtils.castToString("123"));
    }

    @Test
    public void fnv1a_64_lower() {
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64_lower("abc"));
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64_lower("ABC"));
        assertEquals(-1792535898324117685L, TypeUtils.fnv1a_64("abc"));
    }

    @Test
    public void decimal() {
        assertEquals(0, TypeUtils.byteValue(null));
        assertEquals(0, TypeUtils.shortValue(null));
        assertEquals(0, TypeUtils.intValue(null));
        assertEquals(0, TypeUtils.longValue(null));
        assertEquals(0, TypeUtils.longExtractValue(null));

        BigDecimal decimal = BigDecimal.valueOf(123);
        assertEquals(decimal.byteValue(), TypeUtils.byteValue(decimal));
        assertEquals(decimal.shortValue(), TypeUtils.shortValue(decimal));
        assertEquals(decimal.intValue(), TypeUtils.intValue(decimal));
        assertEquals(decimal.longValue(), TypeUtils.longValue(decimal));
        assertEquals(decimal.longValue(), TypeUtils.longExtractValue(decimal));
    }

    @Test
    public void testGetClass() {
        assertNull(TypeUtils.getClass(null));
        assertEquals(Bean.class, TypeUtils.getClass(Bean.class));
    }

    @Test
    public void getAnnotation() throws Exception {
        assertNull(
                TypeUtils.getAnnotation(
                        Bean.class.getMethod("getId"),
                        JSONField.class
                )
        );

        assertNull(
                TypeUtils.getAnnotation(
                        Bean.class.getField("id"),
                        JSONField.class
                )
        );

        assertNull(
                TypeUtils.getAnnotation(
                        Bean.class,
                        JSONField.class
                )
        );
    }

    public static class Bean {
        public int id;

        public int getId() {
            return id;
        }
    }
}

package com.alibaba.fastjson.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.util.BeanUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest {
    @Test
    public void cast() {
        assertNull(TypeUtils.cast("null", HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("null", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("NULL", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast("", (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.cast(null, (Type) HashMap.class, ParserConfig.global));
        assertNull(TypeUtils.castToChar(null));
        assertNull(TypeUtils.castToShort(null));
        assertNull(TypeUtils.castToByte(null));
        assertNull(TypeUtils.castToFloat(null));
        assertNull(TypeUtils.castToDate(null));
        assertNull(TypeUtils.getGenericParamType(null));
        assertEquals('A', TypeUtils.castToChar('A'));
        assertEquals('A', TypeUtils.castToChar("A"));

        assertThrows(
                Exception.class,
                () -> TypeUtils.cast(new Object(), (Type) HashMap.class, ParserConfig.global)
        );
    }

    @Test
    public void castToShort() {
        assertNull(TypeUtils.castToShort(null));
        assertEquals((short) 1, TypeUtils.castToShort(1));
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

    @Test
    public void test_for_computeGetters() {
        List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(Bean.class, null);
        assertEquals(1, fieldInfoList.size());
        assertEquals("id", fieldInfoList.get(0).name);
    }

    public static class Bean {
        public int id;

        public int getId() {
            return id;
        }
    }

    @Test
    public void test_for_computeGetters1() {
        List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(Bean1.class, null);
        assertEquals(2, fieldInfoList.size());
        assertEquals("ID", fieldInfoList.get(0).name);
        assertEquals("values", fieldInfoList.get(1).name);
    }

    public static class Bean1<T> {
        @JSONField(name = "ID")
        public int getId() {
            return 0;
        }

        public List<T> getValues() {
            return null;
        }
    }

    @Test
    public void test() throws Exception {
        assertFalse(TypeUtils.isKotlin(Bean1.class));
        assertEquals(0, TypeUtils.getKoltinConstructorParameters(Bean1.class).length);
        assertFalse(TypeUtils.isKotlinIgnore(Bean1.class, "aa"));
        assertEquals(0, TypeUtils.getParameterAnnotations(Bean1.class.getConstructor()).length);
    }

    @Test
    public void checkPrimitiveArray() {
        assertNotNull(TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(Integer.class)));
        assertEquals(
                int[][].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(int[].class))
        );
        assertEquals(
                byte[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(byte.class))
        );
        assertEquals(
                short[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(short.class))
        );
        assertEquals(
                int[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(int.class))
        );
        assertEquals(
                long[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(long.class))
        );
        assertEquals(
                float[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(float.class))
        );
        assertEquals(
                double[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(double.class))
        );
        assertEquals(
                char[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(char.class))
        );
        assertEquals(
                boolean[].class,
                TypeUtils.checkPrimitiveArray(new BeanUtils.GenericArrayTypeImpl(boolean.class))
        );
    }

    @Test
    public void castToJavaBean() {
        Map map = new HashMap<>();
        map.put("className", Bean.class.getName());
        map.put("methodName", "m");
        StackTraceElement element = TypeUtils.castToJavaBean(map, StackTraceElement.class, ParserConfig.global);
        assertNotNull(element);
    }

    @Test
    public void castToJavaBean1() {
        Map map = new HashMap<>();
        map.put("cCountry", Locale.US.getCountry());
        map.put("language", Locale.US.getLanguage());
        Locale element = TypeUtils.castToJavaBean(map, Locale.class, ParserConfig.global);
        assertNotNull(element);
    }
}

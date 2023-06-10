package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * note
 *
 * @author kon, created on 2022/4/27T11:04.
 * @version 1.0.0-SNAPSHOT
 */
public class BeanUtilsTest {
    @Test
    public void declaredFields() {
        BeanUtils.declaredFields(JSONArray.class, Assertions::assertNotNull);
    }

    @Test
    public void fieldName() {
        assertEquals("userName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.CamelCase.name()));
        assertEquals("userName", BeanUtils.fieldName("userName", PropertyNamingStrategy.CamelCase.name()));
        assertEquals("userName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue.name()));

        assertEquals("UserName", BeanUtils.fieldName("UserName", PropertyNamingStrategy.PascalCase.name()));
        assertEquals("UserName", BeanUtils.fieldName("userName", PropertyNamingStrategy.PascalCase.name()));

        assertEquals("user-name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDashes.name()));
        assertEquals("user-name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.LowerCaseWithDashes.name()));

        assertEquals("User-Name", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCamelCaseWithDashes.name()));
        assertEquals("User-Name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.UpperCamelCaseWithDashes.name()));

        assertEquals("user-name", BeanUtils.fieldName("UserName", PropertyNamingStrategy.LowerCaseWithDashes.name()));
        assertEquals("user-name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDashes.name()));

        assertEquals("USER-NAME", BeanUtils.fieldName("UserName", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("USER-NAME", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("A-U-R-L", BeanUtils.fieldName("aURL", PropertyNamingStrategy.UpperCaseWithDashes.name()));
        assertEquals("_SOME-FIELD-NAME", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.UpperCaseWithDashes.name()));

        assertEquals("some.field.name", BeanUtils.fieldName("someFieldName", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("user.name", BeanUtils.fieldName("userName", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("a.u.r.l", BeanUtils.fieldName("aURL", PropertyNamingStrategy.LowerCaseWithDots.name()));
        assertEquals("_some.field.name", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.LowerCaseWithDots.name()));

        assertEquals("SOME.FIELD.NAME", BeanUtils.fieldName("someFieldName", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("USER.NAME", BeanUtils.fieldName("userName", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("A.U.R.L", BeanUtils.fieldName("aURL", PropertyNamingStrategy.UpperCaseWithDots.name()));
        assertEquals("_SOME.FIELD.NAME", BeanUtils.fieldName("_someFieldName", PropertyNamingStrategy.UpperCaseWithDots.name()));

        assertEquals("id", BeanUtils.setterName("id", "PascalCase"));
        assertEquals("Id", BeanUtils.setterName("setId", "PascalCase"));

        assertEquals("USERID", BeanUtils.setterName("setUserId", "UpperCase"));
        assertEquals("USER_ID", BeanUtils.setterName("setUserId", "UpperCaseWithUnderScores"));

        assertThrows(Exception.class, () -> BeanUtils.setterName("setUserId", "x"));
        assertThrows(Exception.class, () -> BeanUtils.getterName("setUserId", "x"));
        assertThrows(Exception.class, () -> BeanUtils.fieldName("setUserId", "x"));

        assertEquals("USER_ID", BeanUtils.getterName("getUserId", "UpperCaseWithUnderScores"));
        assertEquals("better", BeanUtils.getterName("isBetter", "KebabCase"));
        assertEquals("user-id", BeanUtils.getterName("getUserId", "KebabCase"));
        assertEquals("id", BeanUtils.getterName("id", "KebabCase"));
        assertEquals("Id", BeanUtils.getterName("id", "PascalCase"));

        TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, null);
        assertEquals("user_id", BeanUtils.getterName("getUserId", "SnakeCase"));

        TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, null);
        assertEquals("User Id", BeanUtils.getterName("getUserId", "UpperCamelCaseWithSpaces"));

        TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, null);
        assertEquals("USER-ID", BeanUtils.getterName("getUserId", "UpperCaseWithDashes"));
        TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, null);
        assertEquals("USER.ID", BeanUtils.getterName("getUserId", "UpperCaseWithDots"));
    }

    @Test
    public void subtypeOf() {
        assertEquals(Number.class, BeanUtils.subtypeOf(Number.class).getUpperBounds()[0]);
        assertEquals(Number.class, BeanUtils.subtypeOf(BeanUtils.subtypeOf(Number.class)).getUpperBounds()[0]);
    }

    @Test
    public void supertypeOf() {
        assertEquals(Number.class, BeanUtils.supertypeOf(Number.class).getLowerBounds()[0]);
        assertEquals(Number.class, BeanUtils.supertypeOf(BeanUtils.supertypeOf(Number.class)).getLowerBounds()[0]);
    }

    @Test
    public void getRecordFieldNames() {
        try {
            assertArrayEquals(new String[0], BeanUtils.getRecordFieldNames(Integer.class));
        } catch (Throwable ignored) {
            // ignored
        }
    }

    @Test
    public void object1() {
        JSONObject1O object1O = new JSONObject1O();
        assertTrue(object1O.isEmpty());
    }

    @Test
    public void typeEquals() throws Exception {
        Method f = BeanUtilsTest.class.getMethod("f");
        Method f1 = BeanUtilsTest.class.getMethod("f1");
        Method f2 = BeanUtilsTest.class.getMethod("f2");
        Method f3 = BeanUtilsTest.class.getMethod("f3");
        Method f4 = BeanUtilsTest.class.getMethod("f4");

        Type actualTypeArgument0 = ((ParameterizedType) f.getGenericReturnType()).getActualTypeArguments()[0];
        Type actualTypeArgument1 = ((ParameterizedType) f1.getGenericReturnType()).getActualTypeArguments()[0];
        assertTrue(BeanUtils.equals(actualTypeArgument0, actualTypeArgument1));
        assertFalse(BeanUtils.equals(actualTypeArgument0, Object.class));
        assertFalse(BeanUtils.equals(f.getGenericReturnType(), Object.class));
        assertFalse(BeanUtils.equals(new BeanUtils.GenericArrayTypeImpl(Object.class), Object.class));
        assertFalse(BeanUtils.equals(f2.getGenericReturnType(), f3.getGenericReturnType()));
        assertFalse(BeanUtils.equals(f2.getGenericReturnType(), Object.class));
        assertFalse(BeanUtils.equals(f2.getGenericReturnType(), f4.getGenericReturnType()));
    }

    public static List<?> f() {
        return null;
    }

    public static List<? extends Object> f1() {
        return null;
    }

    public static <T> T f2() {
        return null;
    }

    public static <V> V f3() {
        return null;
    }

    public static <T> T f4() {
        return null;
    }

    @Test
    public void test5() {
        WildcardType wildcardType0 = new BeanUtils.WildcardTypeImpl(
                new Type[]{Object.class},
                new Type[]{Object.class}
        );
        WildcardType wildcardType1 = new BeanUtils.WildcardTypeImpl(
                new Type[]{Object.class},
                new Type[]{Object.class}
        );
        assertEquals(wildcardType0, wildcardType1);

        assertEquals("? super java.lang.Object", wildcardType0.toString());
        assertThrows(Exception.class, () -> new BeanUtils.WildcardTypeImpl(
                new Type[]{null},
                new Type[]{Object.class}
        ));
        assertEquals(
                "?",
                new BeanUtils.WildcardTypeImpl(
                        new Type[]{Object.class},
                        new Type[]{}
                ).toString()
        );
    }

    @Test
    public void test6() {
        ParameterizedType parameterizedType = new BeanUtils.ParameterizedTypeImpl(List.class, Object.class);
        assertEquals("java.lang.Object", parameterizedType.toString());
    }

    @Test
    public void getGenericSupertype() {
        assertEquals("java.util.List<E>",
                BeanUtils.getGenericSupertype(ArrayList.class, ArrayList.class, List.class).toString()
        );
    }

    @Test
    public void getRawType() {
        WildcardType wildcardType = new BeanUtils.WildcardTypeImpl(
                new Type[]{Object.class},
                new Type[]{Object.class}
        );
        assertEquals(Object.class, BeanUtils.getRawType(wildcardType));
    }

    @Test
    public void setters() {
        ArrayList list = new ArrayList();
        BeanUtils.setters(Bean.class, e -> list.add(e));
        assertEquals(1, list.size());
    }

    public static class Bean {
        public void setId(int val) {
        }
    }

    @Test
    public void setters1() {
        ArrayList list = new ArrayList();
        BeanUtils.setters(Bean1.class, true, e -> list.add(e));
        assertEquals(4, list.size());
    }

    public static class Bean1 {
        public void setId(int val) {
        }

        public void id(int val) {
        }

        public AtomicBoolean getValue() {
            return null;
        }

        public AtomicInteger getValue1() {
            return null;
        }

        public AtomicLong getValue2() {
            return null;
        }

        public static void setId2(int val) {
        }
    }

    @Test
    public void getters() {
        List list = new ArrayList();
        BeanUtils.getters(Bean2.class, e -> list.add(e));
        assertEquals(0, list.size());
    }

    public static class Bean2 {
        public Void getA() {
            return null;
        }
    }

    @Test
    public void declaredFieldsNull() {
        BeanUtils.declaredFields(null, o -> {
        });
        BeanUtils.declaredFields(Object.class, null);
    }

    @Test
    public void igoreTest() throws Throwable {
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass("a");
        CtConstructor constructor = new CtConstructor(new CtClass[]{}, clazz);
        clazz.addConstructor(constructor);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(clazz);
        String str = jsonArray.toString();
        assertEquals("[{}]", str);
    }

    @Test
    public void buildIgnores() {
        String[] names = new String[]{
                "javassist.CtNewClass",
                "javassist.CtNewNestedClass",
                "javassist.CtClass",
                "javassist.CtConstructor",
                "javassist.CtMethod",
                "org.apache.ibatis.javassist.CtNewClass",
                "org.apache.ibatis.javassist.CtClass",
                "org.apache.ibatis.javassist.CtConstructor",
                "org.apache.ibatis.javassist.CtMethod",
                "com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                "org.apache.wicket.util.io.DeferredFileOutputStream",
                "org.apache.xalan.xsltc.trax.TemplatesImpl",
                "org.apache.xalan.xsltc.runtime.AbstractTranslet",
                "org.apache.xalan.xsltc.trax.TransformerFactoryImpl",
                "org.apache.commons.collections.functors.ChainedTransformer"
        };

        long[] hashCodes = new long[names.length];
        for (int i = 0; i < names.length; i++) {
            hashCodes[i] = Fnv.hashCode64(names[i]);
        }
        Arrays.sort(hashCodes);
        for (int i = 0; i < hashCodes.length; i++) {
            // System.out.println(hashCodes[i] + "L,");
        }
    }

    @Test
    public void testGetField() throws Exception {
        {
            Method getId = Bean3.class.getMethod("getId");
            Field field = BeanUtils.getField(Bean3.class, getId);
            assertNotNull(field);
        }
        {
            Method setId = Bean3.class.getMethod("setId", int.class);
            Field field = BeanUtils.getField(Bean3.class, setId);
            assertNotNull(field);
        }
        {
            Method isFlag = Bean3.class.getMethod("isFlag");
            Field field = BeanUtils.getField(Bean3.class, isFlag);
            assertNotNull(field);
        }
        {
            Method setFlag = Bean3.class.getMethod("setFlag", boolean.class);
            Field field = BeanUtils.getField(Bean3.class, setFlag);
            assertNotNull(field);
        }
        {
            Method getName = Bean3.class.getMethod("getName");
            Field field = BeanUtils.getField(Bean3.class, getName);
            assertNotNull(field);
        }
        {
            Method setName = Bean3.class.getMethod("setName", String.class);
            Field field = BeanUtils.getField(Bean3.class, setName);
            assertNotNull(field);
        }
    }

    public static class Bean3 {
        private int id;
        private boolean flag;
        private String Name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }
    }
}

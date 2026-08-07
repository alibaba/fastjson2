package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.introspect.PropertyAccessor;
import com.alibaba.fastjson2.introspect.PropertyAccessorFactoryLambda;
import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7691 {
    public static class People {
        private String name;
        private Integer age;
        private Date birthday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }
    }

    public static class Bean {
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    static class NonPublicBean {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * Reproduces the degraded lookup of #7691: {@code JDKUtils.trustedLookup} fell back to
     * {@code IMPL_LOOKUP.in(beanClass)}, which drops the PRIVATE bit, and LambdaMetafactory then
     * rejects the call with {@code LambdaConversionException: Invalid caller}.
     */
    static class NoPrivateAccessFactory
            extends PropertyAccessorFactoryLambda {
        @Override
        protected MethodHandles.Lookup lookup(Class<?> declaringClass) {
            return MethodHandles.publicLookup().in(declaringClass);
        }
    }

    /**
     * Has a lookup with private access, so the lambda path is entered, but the lambda creation itself
     * fails the way LambdaMetafactory does on an affected JVM.
     */
    static class LambdaFailingFactory
            extends PropertyAccessorFactoryLambda {
        @Override
        protected MethodHandles.Lookup lookup(Class<?> declaringClass) {
            return MethodHandles.lookup().in(declaringClass);
        }

        @Override
        public Function<Object, Object> getObject(Method method) {
            throw lambdaFailure(method);
        }

        @Override
        public ToIntFunction<Object> getInt(Method method) {
            throw lambdaFailure(method);
        }

        private static RuntimeException lambdaFailure(Method method) {
            return new RuntimeException(
                    "Failed to create lambda for method: " + method,
                    new LambdaConversionException("Invalid caller: " + method.getDeclaringClass().getName()));
        }
    }

    /**
     * The invariant the whole lambda path depends on: trustedLookup must have private access,
     * otherwise LambdaMetafactory rejects every getter/setter. It was violated in #7691 because
     * IMPL_LOOKUP was read before MethodHandles.Lookup was initialized, which only happens when
     * nothing has used MethodHandles before fastjson2 is loaded - never the case in a test JVM.
     */
    @Test
    public void testTrustedLookupHasPrivateAccess() {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(People.class);
        assertTrue((lookup.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0, "lookupModes=" + lookup.lookupModes());
    }

    @Test
    public void testFallbackWithoutPrivateAccess() throws Exception {
        assertFallback(new NoPrivateAccessFactory());
    }

    @Test
    public void testFallbackOnLambdaConversionFailure() throws Exception {
        assertFallback(new LambdaFailingFactory());
    }

    /**
     * The factory must return a working accessor instead of throwing
     * {@code RuntimeException: Failed to create lambda for method}.
     */
    private static void assertFallback(PropertyAccessorFactoryLambda factory) throws Exception {
        Method getName = People.class.getMethod("getName");
        Method setName = People.class.getMethod("setName", String.class);
        PropertyAccessor name = factory.create("name", String.class, String.class, getName, setName, null);
        assertNotNull(name);

        People people = new People();
        name.setObject(people, "lis");
        assertEquals("lis", people.getName());
        assertEquals("lis", name.getObject(people));

        Method getAge = Bean.class.getMethod("getAge");
        Method setAge = Bean.class.getMethod("setAge", int.class);
        PropertyAccessor age = factory.create("age", int.class, int.class, getAge, setAge, null);
        assertNotNull(age);

        Bean bean = new Bean();
        age.setIntValue(bean, 42);
        assertEquals(42, bean.getAge());
        assertEquals(42, age.getIntValue(bean));
    }

    @Test
    public void testToJSONString() {
        People people = new People();
        people.setName("lis");
        people.setAge(20);
        people.setBirthday(new Date());

        String json = JSON.toJSONString(people);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"lis\""));
        assertTrue(json.contains("\"age\":20"));
        assertTrue(json.contains("\"birthday\":"));
    }

    @Test
    public void testRoundTrip() {
        People people = new People();
        people.setName("lis");
        people.setAge(20);
        people.setBirthday(new Date());

        String json = JSON.toJSONString(people);
        People p2 = JSON.parseObject(json, People.class);
        assertEquals("lis", p2.getName());
        assertEquals(20, p2.getAge());
        assertNotNull(p2.getBirthday());
    }

    @Test
    public void testToJSONStringPrimitiveInt() {
        Bean bean = new Bean();
        bean.setAge(1);

        String json = JSON.toJSONString(bean);
        assertEquals("{\"age\":1}", json);
    }

    @Test
    public void testRoundTripPrimitiveInt() {
        Bean bean = new Bean();
        bean.setAge(42);

        String json = JSON.toJSONString(bean);
        Bean b2 = JSON.parseObject(json, Bean.class);
        assertEquals(42, b2.getAge());
    }

    @Test
    public void testNonPublicBean() {
        NonPublicBean bean = new NonPublicBean();
        bean.setName("test");
        bean.setValue(99);

        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":99"));

        NonPublicBean b2 = JSON.parseObject(json, NonPublicBean.class);
        assertEquals("test", b2.getName());
        assertEquals(99, b2.getValue());
    }

    @Test
    public void testJSONObjectToJSONString() {
        People people = new People();
        people.setName("lis");
        people.setAge(20);

        String json = JSONObject.toJSONString(people);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"lis\""));
    }
}

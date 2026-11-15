package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Method and Field values resolve their class names under SupportClassForName; they must apply the
 * same autoType checks as a Class value does, so that a deny class is rejected.
 */
public class MethodFieldClassForNameTest {
    @Test
    public void testClassDenied() {
        String str = "{\"type\":\"java.lang.ClassLoader\"}";
        assertThrows(JSONException.class,
                () -> JSON.parseObject(str, ClassBean.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void testMethodDeclaringClassDenied() {
        String str = "{\"method\":{\"declaringClass\":\"java.lang.ClassLoader\",\"name\":\"defineClass\","
                + "\"parameterTypes\":[\"java.lang.String\",\"[B\",\"int\",\"int\"]}}";
        assertThrows(JSONException.class,
                () -> JSON.parseObject(str, MethodBean.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void testMethodDeclaringClassDeniedArrayMapping() {
        String str = "[[\"java.lang.ClassLoader\",\"getParent\",[]]]";
        assertThrows(JSONException.class,
                () -> JSON.parseObject(str, MethodBean.class,
                        JSONReader.Feature.SupportClassForName, JSONReader.Feature.SupportArrayToBean));
    }

    @Test
    public void testMethodParameterTypeDenied() {
        String str = "{\"method\":{\"declaringClass\":\"" + Service.class.getName() + "\",\"name\":\"f3\","
                + "\"parameterTypes\":[\"java.lang.ClassLoader\"]}}";
        assertThrows(JSONException.class,
                () -> JSON.parseObject(str, MethodBean.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void testMethodDeclaringClassDeniedJSONB() {
        byte[] bytes = JSONB.toBytes(JSON.parseObject(
                "{\"method\":{\"declaringClass\":\"java.lang.ClassLoader\",\"name\":\"getParent\",\"parameterTypes\":[]}}"));
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(bytes, MethodBean.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void testFieldDeclaringClassDenied() {
        String str = "{\"field\":{\"declaringClass\":\"com.sun.rowset.JdbcRowSetImpl\",\"name\":\"conn\"}}";
        assertThrows(JSONException.class,
                () -> JSON.parseObject(str, FieldBean.class, JSONReader.Feature.SupportClassForName));
    }

    @Test
    public void testMethodRoundTrip() throws Exception {
        MethodBean bean = new MethodBean();
        bean.method = Service.class.getMethod("f2", int.class, long[].class, String[].class, Object.class);
        String str = JSON.toJSONString(bean);
        MethodBean bean1 = JSON.parseObject(str, MethodBean.class, JSONReader.Feature.SupportClassForName);
        assertEquals(bean.method, bean1.method);

        byte[] bytes = JSONB.toBytes(bean);
        MethodBean bean2 = JSONB.parseObject(bytes, MethodBean.class, JSONReader.Feature.SupportClassForName);
        assertEquals(bean.method, bean2.method);
    }

    @Test
    public void testFieldRoundTrip() throws Exception {
        FieldBean bean = new FieldBean();
        bean.field = Service.class.getField("value");
        String str = JSON.toJSONString(bean);
        FieldBean bean1 = JSON.parseObject(str, FieldBean.class, JSONReader.Feature.SupportClassForName);
        assertEquals(bean.field, bean1.field);
    }

    @Test
    public void testFilterAccepted() throws Exception {
        String str = "{\"method\":{\"declaringClass\":\"" + Service.class.getName() + "\",\"name\":\"f0\"}}";
        MethodBean bean = JSON.parseObject(str, MethodBean.class,
                JSONReader.autoTypeFilter(Service.class.getName()),
                JSONReader.Feature.SupportClassForName);
        assertEquals(Service.class.getMethod("f0"), bean.method);
    }

    public static class ClassBean {
        public Class type;
    }

    public static class MethodBean {
        public Method method;
    }

    public static class FieldBean {
        public Field field;
    }

    public static class Service {
        public int value;

        public void f0() {
        }

        public void f1(Object value) {
        }

        public void f3(ClassLoader loader) {
        }

        public void f2(int i, long[] values, String[] names, Object value) {
        }
    }
}

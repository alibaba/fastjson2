package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONObjectTest3 {
    @Test
    public void test_0() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);
        assertEquals("123", bean.getValue());
        assertEquals(false, bean.isBig());
        assertEquals(123, bean.getIntValue());

        bean.setBig(true);
        assertEquals(true, bean.isBig());

        bean.setID(567);
        assertEquals(567, bean.getID());
    }

    @Test
    public void test_error_0() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.f();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_1() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.f(1);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_2() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.get();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_3() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.is();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_4() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.f(1, 2);
        } catch (UnsupportedOperationException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_5() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.getA();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_6() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.f1(1);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_7() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.set(1);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_error_8() {
        if (TestUtils.GRAALVM || TestUtils.ANDROID) {
            return;
        }

        String text = "{'value':'123','big':false}";
        Bean bean = JSON.parseObject(text, Bean.class);

        Exception error = null;
        try {
            bean.xx();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    public interface Bean {
        String getValue();

        void setValue(String value);

        boolean isBig();

        @JSONField
        void setBig(boolean value);

        @JSONField(name = "value")
        int getIntValue();

        @JSONField(name = "id")
        void setID(int value);

        @JSONField(name = "id")
        int getID();

        Object get();

        Object xx();

        void set(int i);

        boolean is();

        void getA();

        void f();

        Object f(int a);

        void f1(int a);

        void f(int a, int b);
    }
}

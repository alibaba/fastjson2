package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

public class Issue756 {
    @Test
    public void test() {
        NoneSerializable noneSerializable = new NoneSerializable();
        noneSerializable.setParam("Test");
        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.IgnoreNoneSerializable);
        Object o = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNull(o);
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        NoneSerializable noneSerializable = new NoneSerializable();
        noneSerializable.setParam("Test");
        bean.value = noneSerializable;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.IgnoreNoneSerializable);
        Bean bean1 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertNull(bean1.value);

        byte[] bytes2 = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean bean2 = (Bean) JSONB.parseObject(bytes2, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(bean2.value);
        Bean bean3 = (Bean) JSONB.parseObject(bytes2, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.IgnoreNoneSerializable);
        assertNull(bean3.value);
    }

    @Test
    public void test_error() {
        NoneSerializable noneSerializable = new NoneSerializable();
        noneSerializable.setParam("Test");
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    public static class NoneSerializable {
        private String param;

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }

    public static class Bean
            implements Serializable {
        public NoneSerializable value;
    }

    @Test
    public void test_error_x1() {
        NoneSerializable_X1 noneSerializable = new NoneSerializable_X1();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X1 {
        public String param;
    }

    @Test
    public void test_error_x2() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X2 {
        public String param;
        public String param2;
    }

    @Test
    public void test_error_x3() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X3 {
        public String param;
        public String param2;
        public String param3;
    }

    @Test
    public void test_error_x4() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X4 {
        public String param;
        public String param2;
        public String param3;
        public String param4;
    }

    @Test
    public void test_error_x5() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X5 {
        public String param;
        public String param2;
        public String param3;
        public String param4;
        public String param5;
    }

    @Test
    public void test_error_x6() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X6 {
        public String param;
        public String param2;
        public String param3;
        public String param4;
        public String param5;
        public String param6;
    }

    @Test
    public void test_error_x7() {
        NoneSerializable_X2 noneSerializable = new NoneSerializable_X2();
        noneSerializable.param = "Test";
        assertThrows(
                JSONException.class,
                () -> JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.ErrorOnNoneSerializable)
        );

        byte[] bytes = JSONB.toBytes(noneSerializable, JSONWriter.Feature.WriteClassName);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.ErrorOnNoneSerializable)
        );
    }

    static class NoneSerializable_X7 {
        public String param;
        public String param2;
        public String param3;
        public String param4;
        public String param5;
        public String param6;
        public String param7;
    }
}

package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONBWriteNullEnumTest {
    public enum Status {
        OK, FAIL
    }

    public static class Bean {
        public Status status;
        public String name = "x";
    }

    public static class BeanGetter {
        private Status status;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    @Test
    public void writeNullEnumWithClassName() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertEquals("x", back.name);
    }

    @Test
    public void writeNullEnumWithoutClassName() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
    }

    @Test
    public void writeNullEnumGetterBean() {
        BeanGetter bean = new BeanGetter();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        BeanGetter back = JSONB.parseObject(bytes, BeanGetter.class);
        assertNull(back.getStatus());
    }

    @Test
    public void writeNullEnumMapNullValue() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteMapNullValue);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
    }

    @Test
    public void nonNullEnumUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullEnumUsingName() {
        // The name/toString path dereferences the enum inside IO.enumCapacity, so
        // this is the only round trip that actually gates the null guard there.
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
        assertEquals("x", back.name);
    }

    @Test
    public void nonNullEnumUsingNameUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteEnumsUsingName);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullEnumUsingToString() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteEnumUsingToString);
        Bean back = JSONB.parseObject(bytes, Bean.class);
        assertNull(back.status);
    }

    @Test
    public void writeNullEnumBeanToArray() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNull(back.status);
    }

    @Test
    public void writeNullEnumBeanToArrayWithNulls() {
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteNulls);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNull(back.status);
    }

    @Test
    public void nonNullEnumBeanToArrayUnchanged() {
        Bean bean = new Bean();
        bean.status = Status.FAIL;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean back = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(Status.FAIL, back.status);
    }

    @Test
    public void writeNullsEmitsStatusKeyAsBcNull() {
        // WriteNulls must emit the status field as BC_NULL, not skip it silently.
        Bean bean = new Bean();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteNulls);
        String str = JSONB.toJSONString(bytes);
        assertTrue(str.contains("\"status\":null"), "BC_NULL emission missing, got: " + str);
    }
}

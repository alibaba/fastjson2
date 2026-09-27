package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}

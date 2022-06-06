package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteClassNameBasicTypeTest {
    @Test
    public void test() {
        assertEquals("1B", JSON.toJSONString((byte) 1, JSONWriter.Feature.WriteClassName));
        assertEquals("1S", JSON.toJSONString((short) 1, JSONWriter.Feature.WriteClassName));
        assertEquals("1L", JSON.toJSONString((long) 1, JSONWriter.Feature.WriteClassName));
        assertEquals("2147483648", JSON.toJSONString(2147483648L, JSONWriter.Feature.WriteClassName));
    }

    @Test
    public void testByte() {
        BeanByte bean = new BeanByte();
        bean.value = 1;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"value\":1}", str);
    }

    @Test
    public void testShort() {
        BeanShort bean = new BeanShort();
        bean.value = 1;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"value\":1}", str);
    }

    @Test
    public void testLong() {
        BeanLong bean = new BeanLong();
        bean.value = 1L;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"value\":1}", str);
    }

    @Test
    public void testFloat() {
        BeanFloat bean = new BeanFloat();
        bean.value = 1F;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"value\":1.0}", str);
    }

    @Test
    public void testDouble() {
        BeanDouble bean = new BeanDouble();
        bean.value = 1F;
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
        assertEquals("{\"value\":1.0}", str);
    }

    @Test
    public void testObject() {
        assertEquals("{\"value\":1B}", JSON.toJSONString(new BeanObject((byte) 1), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":1S}", JSON.toJSONString(new BeanObject((short) 1), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":1}", JSON.toJSONString(new BeanObject(1), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":1}", JSON.toJSONString(new BeanObject(1), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":2147483647L}", JSON.toJSONString(new BeanObject(2147483647L), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":2147483648}", JSON.toJSONString(new BeanObject(2147483648L), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":-2147483649}", JSON.toJSONString(new BeanObject(-2147483649L), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":-2147483648L}", JSON.toJSONString(new BeanObject(-2147483648L), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":1.0F}", JSON.toJSONString(new BeanObject(1F), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
        assertEquals("{\"value\":1.0D}", JSON.toJSONString(new BeanObject(1D), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName));
    }

    public static class BeanObject {
        public Object value;

        public BeanObject() {
        }

        public BeanObject(Object value) {
            this.value = value;
        }
    }

    public static class BeanByte {
        public Byte value;
    }

    public static class BeanShort {
        public Short value;
    }

    public static class BeanLong {
        public Long value;
    }

    public static class BeanFloat {
        public Float value;
    }

    public static class BeanDouble {
        public Float value;
    }
}

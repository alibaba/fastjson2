package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue412 {
    @Test
    public void testLong() {
        Bean bean = new Bean();
        bean.setAa(1L);
        assertTrue(bean.getAa() instanceof Long);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Bean object = JSON.parseObject(str, Bean.class);
        assertTrue(object.getAa() instanceof Long);
        Bean object1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(object1.getAa() instanceof Long);
    }

    @Test
    public void testShort() {
        Bean bean = new Bean();
        bean.setAa((short) 1);
        assertTrue(bean.getAa() instanceof Short);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Bean object = JSON.parseObject(str, Bean.class);
        assertTrue(object.getAa() instanceof Short);
        Bean object1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(object1.getAa() instanceof Short);
    }

    @Test
    public void testByte() {
        Bean bean = new Bean();
        bean.setAa((byte) 1);
        assertTrue(bean.getAa() instanceof Byte);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Bean object = JSON.parseObject(str, Bean.class);
        assertTrue(object.getAa() instanceof Byte);
        Bean object1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(object1.getAa() instanceof Byte);
    }

    @Test
    public void testFloat() {
        Bean bean = new Bean();
        bean.setAa((float) 1);
        assertTrue(bean.getAa() instanceof Float);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Bean object = JSON.parseObject(str, Bean.class);
        assertTrue(object.getAa() instanceof Float);
        Bean object1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(object1.getAa() instanceof Float);
    }

    @Test
    public void testDouble() {
        Bean bean = new Bean();
        bean.setAa((double) 1);
        assertTrue(bean.getAa() instanceof Double);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Bean object = JSON.parseObject(str, Bean.class);
        assertTrue(object.getAa() instanceof Double);
        Bean object1 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
        assertTrue(object1.getAa() instanceof Double);
    }

    public static class Bean {
        public Object getAa() {
            return aa;
        }

        public void setAa(Object aa) {
            this.aa = aa;
        }

        private Object aa;
    }
}

package com.alibaba.fastjson2.modules;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ModulesTest {
    @Test
    public void test() {
        ObjectReaderAnnotationProcessor processor = new ObjectReaderAnnotationProcessor() {};
        processor.getBeanInfo(null, null);
        processor.getFieldInfo(null, null, (Field) null);
        processor.getFieldInfo(null, null, (Method) null);
        processor.getFieldInfo(null, null, (Method) null, 0, null);
        processor.getFieldInfo(null, null, (Constructor) null, 0, null);
    }

    @Test
    public void test1() {
        ObjectWriterAnnotationProcessor processor = new ObjectWriterAnnotationProcessor() {};
        processor.getBeanInfo(null, null);
        processor.getFieldInfo(null, null, null, (Field) null);
        processor.getFieldInfo(null, null, null, (Method) null);
    }

    @Test
    public void test2() {
        ObjectReaderModule module = new ObjectReaderModule() {};
        assertNull(module.getAnnotationProcessor());
        assertNull(module.getProvider());
        assertNull(module.getObjectReader(null, null));
    }

    @Test
    public void test3() {
        ObjectWriterModule module = new ObjectWriterModule() {};
        assertNull(module.getAnnotationProcessor());
        assertNull(module.getObjectWriter(null, null));
    }
}

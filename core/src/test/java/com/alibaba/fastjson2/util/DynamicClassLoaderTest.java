package com.alibaba.fastjson2.util;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertSame;

public class DynamicClassLoaderTest {
    @Test
    public void definePackage() throws Exception {
        DynamicClassLoader classLoader = new DynamicClassLoader();
        classLoader.definePackage("com.alibaba.fastjson2.util.x1");

        byte[] bytes;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/VO.clazz");
        bytes = IOUtils.toByteArray(is);
        is.close();

        Class<?> class0 = classLoader.loadClass("external.VO", bytes, 0, bytes.length);
        Class<?> class1 = classLoader.loadClass("external.VO");
        assertSame(class0, class1);
    }
}

package com.alibaba.fastjson2.benchmark.eishay.gen;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.utf8.UTF8Encode;
import com.alibaba.fastjson2.util.DynamicClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EishayClassGenTest {
    public void test() {
        DynamicClassLoader classLoader = DynamicClassLoader.getInstance();
        EishayClassGen gen = new EishayClassGen();

        long start = System.currentTimeMillis();
        Class[] classes = new Class[10_000];
        for (int i = 0; i < classes.length; i++) {
            Class objectClass = gen.genMedia(classLoader, "com/alibaba/fastjson2/benchmark/eishay" + i);
            classes[i] = objectClass;
        }
        System.out.println("millis " + (System.currentTimeMillis() - start));
        Class objectClass = gen.genMedia(classLoader, "com/alibaba/fastjson2/benchmark/eishay");
        String str = UTF8Encode.readFromClasspath("data/eishay.json");

        Object o = JSON.parseObject(str, objectClass);
        String str1 = JSON.toJSONString(o);
        assertNotNull(str1);
    }

    public static void main(String[] args) throws Exception {
        EishayClassGenTest gen = new EishayClassGenTest();
        gen.test();
    }
}

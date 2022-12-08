package com.alibaba.fastjson2.benchmark.eishay.gen;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.benchmark.eishay.EishayFuryParse;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EishayClassGenTest {
    @Test
    public void test() throws Exception {
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
        InputStream is = EishayFuryParse.class.getClassLoader().getResourceAsStream("data/eishay.json");
        String str = IOUtils.toString(is, "UTF-8");

        Object o = JSON.parseObject(str, objectClass);
        String str1 = JSON.toJSONString(o);
        assertNotNull(str1);
    }
}

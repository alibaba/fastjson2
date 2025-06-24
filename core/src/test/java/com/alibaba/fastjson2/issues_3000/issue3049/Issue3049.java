package com.alibaba.fastjson2.issues_3000.issue3049;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author 张治保
 * @since 2024/10/8
 */
public class Issue3049 {
    @Test
    @SneakyThrows
    void testDouble() {
        DoubleClazz c = new DoubleClazz();
        c.setD(1D);
        JSONAssert.assertEquals("{\"d\":1.0}", JSON.toJSONString(c), true);
        c.setD(null);
        JSONAssert.assertEquals("{\"d\":null}", JSON.toJSONString(c), true);

        DoubleClazz2 c2 = new DoubleClazz2();
        c2.setD(1D);
        JSONAssert.assertEquals("{\"d\":1.0}", JSON.toJSONString(c2), true);
        c2.setD(null);
        JSONAssert.assertEquals("{}", JSON.toJSONString(c2), true);
    }

    @Test
    @SneakyThrows
    void testLong() {
        LongClazz c = new LongClazz();
        c.setD(1L);
        JSONAssert.assertEquals("{\"d\":1.0}", JSON.toJSONString(c), true);
        c.setD(null);
        JSONAssert.assertEquals("{\"d\":null}", JSON.toJSONString(c), true);

        LongClazz2 c2 = new LongClazz2();
        c2.setD(1L);
        JSONAssert.assertEquals("{\"d\":1.0}", JSON.toJSONString(c2), true);
        c2.setD(null);
        JSONAssert.assertEquals("{}", JSON.toJSONString(c2), true);
    }
}

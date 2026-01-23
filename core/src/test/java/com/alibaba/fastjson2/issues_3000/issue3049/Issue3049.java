package com.alibaba.fastjson2.issues_3000.issue3049;

import com.alibaba.fastjson2.JSON;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("{\"d\":1.0}", JSON.toJSONString(c));
        c.setD(null);
        assertEquals("{\"d\":null}", JSON.toJSONString(c));

        DoubleClazz2 c2 = new DoubleClazz2();
        c2.setD(1D);
        assertEquals("{\"d\":1.0}", JSON.toJSONString(c2));
        c2.setD(null);
        assertEquals("{}", JSON.toJSONString(c2));
    }

    @Test
    @SneakyThrows
    void testLong() {
        LongClazz c = new LongClazz();
        c.setD(1L);
        assertEquals("{\"d\":1}", JSON.toJSONString(c));
        c.setD(null);
        assertEquals("{\"d\":null}", JSON.toJSONString(c));

        LongClazz2 c2 = new LongClazz2();
        c2.setD(1L);
        assertEquals("{\"d\":1}", JSON.toJSONString(c2));
        c2.setD(null);
        assertEquals("{}", JSON.toJSONString(c2));
    }
}

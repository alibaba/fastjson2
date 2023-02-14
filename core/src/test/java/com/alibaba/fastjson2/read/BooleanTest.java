package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanTest {
    @Test
    public void test() {
        assertFalse(JSON.parseObject("{\"value\":0}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertFalse(JSON.parseObject("{\"value\":false}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":1}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":3}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":true}", Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
    }

    @Test
    public void test1() {
        assertFalse(JSON.parseObject("{\"value\":0}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertFalse(JSON.parseObject("{\"value\":false}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":1}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":3}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
        assertTrue(JSON.parseObject("{\"value\":true}").toJavaObject(Bean.class, NonZeroNumberCastToBooleanAsTrue).value);
    }

    public static class Bean {
        public boolean value;
    }
}

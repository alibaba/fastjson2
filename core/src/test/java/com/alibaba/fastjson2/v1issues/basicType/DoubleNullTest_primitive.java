package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by wenshao on 10/08/2017.
 */
public class DoubleNullTest_primitive {
    @Test
    public void test_null() {
        Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}", Model.class);
        assertNotNull(model);
        assertEquals(0D, model.v1);
        assertEquals(0D, model.v2);
    }

    @Test
    public void test_null_1() {
        Model model = JSON.parseObject("{\"v1\":null ,\"v2\":null }", Model.class);
        assertNotNull(model);
        assertEquals(0D, model.v1);
        assertEquals(0D, model.v2);
    }

    @Test
    public void test_null_2() {
        Model model = JSON.parseObject("{\"v1\":\"null\",\"v2\":\"null\" }", Model.class);
        assertNotNull(model);
        assertEquals(0D, model.v1);
        assertEquals(0D, model.v2);
    }

    public static class Model {
        public double v1;
        public double v2;
    }
}

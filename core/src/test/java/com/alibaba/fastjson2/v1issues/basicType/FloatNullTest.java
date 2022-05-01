package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 10/08/2017.
 */
public class FloatNullTest {
    @Test
    public void test_null() {
        Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_quote() {
        Model model = JSON.parseObject("{\"v1\":\"null\",\"v2\":\"null\"}", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_1() {
        Model model = JSON.parseObject("{\"v1\":null ,\"v2\":null }", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_1_quote() {
        Model model = JSON.parseObject("{\"v1\":\"null\" ,\"v2\":\"null\" }", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_array() {
        Model model = JSON.parseObject("[\"null\" ,\"null\"]", Model.class, Feature.SupportArrayToBean);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    public static class Model {
        public Float v1;
        public Float v2;
    }
}

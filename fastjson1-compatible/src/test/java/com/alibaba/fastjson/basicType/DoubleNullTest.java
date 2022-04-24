package com.alibaba.fastjson.basicType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 10/08/2017.
 */
public class DoubleNullTest {
    @Test
    public void test_null() throws Exception {
        Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_quote() throws Exception {
        Model model = JSON.parseObject("{\"v1\":\"null\",\"v2\":\"null\"}", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_1() throws Exception {
        Model model = JSON.parseObject("{\"v1\":null ,\"v2\":null }", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_1_quote() throws Exception {
        Model model = JSON.parseObject("{\"v1\":\"null\" ,\"v2\":\"null\" }", Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_array() throws Exception {
        Model model = JSON.parseObject("[\"null\" ,\"null\"]", Model.class, Feature.SupportArrayToBean);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    @Test
    public void test_null_array_reader() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("[\"null\" ,\"null\"]"), Feature.SupportArrayToBean);
        Model model = reader.readObject(Model.class);
        assertNotNull(model);
        assertNull(model.v1);
        assertNull(model.v2);
    }

    public static class Model {
        public Double v1;
        public Double v2;
    }
}

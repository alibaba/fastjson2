package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader.Feature;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 10/08/2017.
 */
public class LongNullTest {
    @Test
    public void test_null() throws Exception {
        {
            Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}", Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}".getBytes(StandardCharsets.UTF_8), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject("{\"v1\":null,\"v2\":null}".toCharArray(), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
    }

    @Test
    public void test_null_0() throws Exception {
        String str = "{\"v1\":null,\"v2\":null,\"v3\":\"中\"}";
        {
            Model model = JSON.parseObject(str, Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.toCharArray(), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
    }

    @Test
    public void test_null_quote() throws Exception {
        String str = "{\"v1\":\"null\",\"v2\":\"null\"}";
        {
            Model model = JSON.parseObject(str, Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.toCharArray(), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
    }

    @Test
    public void test_null_empty() throws Exception {
        String str = "{\"v1\":\"\",\"v2\":\"\"}";
        {
            Model model = JSON.parseObject(str, Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
        {
            Model model = JSON.parseObject(str.toCharArray(), Model.class);
            assertNotNull(model);
            assertNull(model.v1);
            assertNull(model.v2);
        }
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

    public static class Model {
        public Long v1;
        public Long v2;
    }
}

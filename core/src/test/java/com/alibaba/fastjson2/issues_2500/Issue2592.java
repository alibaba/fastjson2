package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
public class Issue2592 {
    String str = "\"name\":\"Huang\",\"minVersion\":\"1.1\",\"maxVersion\":\"1.2\"}";

    @Test
    void test() {
        Exception error = null;
        try {
            JSONObject.parseObject(str, TestBean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("illegal fieldName input"));
    }

    @Test
    void testUTF8() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);
        Exception error = null;
        try {
            JSON.parseObject(utf8Bytes, TestBean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("illegal fieldName input"));
    }

    @Test
    void testUTF8Stream() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        Exception error = null;
        try {
            JSON.parseObject(byteArrayInputStream, StandardCharsets.UTF_8);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    @Test
    void testUTF8Stream2() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream);
        JSONReader jsonReader = JSONReader.of(inputStreamReader);
        Exception error = null;
        try {
            jsonReader.read(TestBean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("illegal fieldName input"));
    }

    @Test
    void testUTF16() {
        String str1 = "\"name\": \"张三\", \"minVersion\": 30, \"maxVersion\": \"北京\" }";
        byte[] utf16Bytes = str1.getBytes(StandardCharsets.UTF_16);
        JSONReader jsonReader = JSONReader.of(utf16Bytes, 0, utf16Bytes.length, StandardCharsets.UTF_16);
        Exception error = null;
        try {
            jsonReader.read(TestBean.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        assertTrue(error.getMessage().contains("illegal fieldName input"));
    }
    @Test
    void testValid() {
        boolean isValid = JSON.isValid(str);
        assertFalse(isValid);
    }

    public static class TestBean {
        String name;
        String minVersion;
        String maxVersion;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(String minVersion) {
            this.minVersion = minVersion;
        }

        public String getMaxVersion() {
            return maxVersion;
        }

        public void setMaxVersion(String maxVersion) {
            this.maxVersion = maxVersion;
        }
    }
}

package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1291 {
    @Test
    public void test() {
        String str = "{\"obj\":{\"unitCode\":\"755H\"}}";
        Result<List> result = JSON.parseObject(str, new TypeReference<Result<List>>() {});
        assertEquals(1, result.obj.size());
        JSONObject object = (JSONObject) result.obj.get(0);
        assertNotNull(object);
        assertEquals("755H", object.get("unitCode"));
    }

    @Test
    public void test_1() {
        String info = "{\"obj\":{\"unitCode\":\"755H\"}}";
        Result<List<String>> result =  JSONObject.parseObject(info, new TypeReference<Result<List<String>>>() {});
        assertEquals(1, result.obj.size());
        String string = result.obj.get(0);
        assertEquals("{\"unitCode\":\"755H\"}", string);
    }

    @Test
    public void test_2() {
        String info = "{\"obj\":{\"unitCode\":\"755H\"}}";
        Result<List<Info>> result =  JSONObject.parseObject(info, new TypeReference<Result<List<Info>>>() {});
        assertEquals(1, result.obj.size());
        Info object = result.obj.get(0);
        assertNotNull(object);
        assertEquals("755H", object.unitCode);
    }

    public static class Result<T> {
        public Date date;
        public String errorCode;
        public String errorMessage;
        public T obj;
        public boolean success;
    }

    public static class Info{
        public String unitCode;
    }

}

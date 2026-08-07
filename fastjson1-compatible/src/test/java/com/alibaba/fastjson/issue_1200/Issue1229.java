package com.alibaba.fastjson.issue_1200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by wenshao on 30/05/2017.
 */
public class Issue1229 {
    @Test
    public void test_for_issue() throws Exception {
        final Object parsed = JSON.parse("{\"data\":{}}");
        assertTrue(parsed instanceof JSONObject);
        assertEquals(JSONObject.class, ((JSONObject) parsed).get("data").getClass());

        Type type = new TypeReference<Result<Data>>() {
        }.getType();
        final Result<Data> result = JSON.parseObject("{\"data\":{}}", type);
        assertNotNull(result.data);
        assertTrue(result.data instanceof Data);

        final Result<List<Data>> result2 = JSON.parseObject("{\"data\":[]}", new TypeReference<Result<List<Data>>>() {
        }.getType());
        assertNotNull(result2.data);
        assertTrue(result2.data instanceof List);
        assertEquals(0, result2.data.size());
    }

    public void parseErr() throws Exception {
        JSON.parseObject("{\"data\":{}}", new TypeReference<Result<List<Data>>>() {
        }.getType());
        fail("should be failed due to error json");
    }

    public static class Result<T> {
        T data;

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    public static class Data {
    }
}

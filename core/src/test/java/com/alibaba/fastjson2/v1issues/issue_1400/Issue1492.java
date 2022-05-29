package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1492 {
    @Test
    public void test_for_issue() throws Exception {
        DubboResponse resp = new DubboResponse();

        // test for JSONObject
        JSONObject obj = new JSONObject();
        obj.put("key1", "value1");
        obj.put("key2", "value2");
        resp.setData(obj);

        String str = JSON.toJSONString(resp);
        System.out.println(str);
        DubboResponse resp1 = JSON.parseObject(str, DubboResponse.class);
        assertEquals(str, JSON.toJSONString(resp1));

        // test for JSONArray
        JSONArray arr = new JSONArray();
        arr.add("key1");
        arr.add("key2");
        resp.setData(arr);

        String str2 = JSON.toJSONString(resp);
        System.out.println(str2);
        DubboResponse resp2 = JSON.parseObject(str2, DubboResponse.class);
        assertEquals(str2, JSON.toJSONString(resp2));
    }

    public static final class DubboResponse
            implements Serializable {
        private String message;

        private String error;

        private Object data;

        private boolean success;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}

package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1909 {
    @Test
    public void test_for_issue() {
        JSONArray params = new JSONArray();
        params.add("val1");
        params.add(2);
        ParamRequest pr = new ParamRequest("methodName", "stringID", params);
        String str = JSON.toJSONString(pr);
        Request paramRequest = JSON.parseObject(str, ParamRequest.class);
        assertNotNull(paramRequest);
    }

    public static class ParamRequest
            extends Request {
        private String methodName;

        @JSONField(name = "id", ordinal = 3, serialize = true, deserialize = true)
        private Object id;

        private List<Object> params;

        public ParamRequest(String methodName, Object id, List<Object> params) {
            this.methodName = methodName;
            this.id = id;
            this.params = params;
        }

        public String getMethodName() {
            return methodName;
        }

        public Object getId() {
            return id;
        }

        public List<Object> getParams() {
            return params;
        }
    }

    public static class Request {
    }
}

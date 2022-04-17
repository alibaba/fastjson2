package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import junit.framework.TestCase;

public class Issue1739 extends TestCase {
    public void test_for_issue() throws Exception {
        M0 model = new M0();
        model.data = new JSONObject();

        String json = JSON.toJSONString(model);
        assertEquals("{\"data\":{}}", json);
    }

    public void test_for_issue_1() throws Exception {
        M1 model = new M1();
        model.data = new JSONObject();

        String json = JSON.toJSONString(model);
        assertEquals("{}", json);
    }

    public static class M0 {
        private JSONObject data;

        @JSONField(read = false)
        public JSONObject getData() {
            return data;
        }

        public void setData(JSONObject data) {
            this.data = data;
        }
    }

    public static class M1 {
        private JSONObject data;

        @JSONField(write = false)
        public JSONObject getData() {
            return data;
        }

        public void setData(JSONObject data) {
            this.data = data;
        }
    }
}

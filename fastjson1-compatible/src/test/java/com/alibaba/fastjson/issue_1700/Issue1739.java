package com.alibaba.fastjson.issue_1700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1739 {
    @Test
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

        @JSONField(deserialize = false)
        public JSONObject getData() {
            return data;
        }

        public void setData(JSONObject data) {
            this.data = data;
        }
    }

    public static class M1 {
        private JSONObject data;

        @JSONField(serialize = false)
        public JSONObject getData() {
            return data;
        }

        public void setData(JSONObject data) {
            this.data = data;
        }
    }
}

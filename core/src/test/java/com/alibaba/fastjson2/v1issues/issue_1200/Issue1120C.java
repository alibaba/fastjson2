package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 01/04/2017.
 */
public class Issue1120C {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.setReqNo("123");

        assertEquals("{\"REQ_NO\":\"123\"}", JSON.toJSONString(model));
    }

    public static class Model {
        @JSONField(name = "REQ_NO")
        private String reqNo;

        public String getReqNo() {
            return reqNo;
        }

        public void setReqNo(String reqNo) {
            this.reqNo = reqNo;
        }
    }
}

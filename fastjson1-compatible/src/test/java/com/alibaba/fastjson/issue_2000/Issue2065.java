package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2065 {
    @Test
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"code\":1}", Model.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        error.printStackTrace();
    }

    @Test
    public void test_for_issue_01() {
        Exception error = null;
        try {
            JSON.parseObject("1", EnumClass.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
        error.printStackTrace();
    }

    @Test
    public void test_for_issue_02() {
        JSON.parseObject("0", EnumClass.class);
    }

    @Test
    public void test_for_issue_03() {
        JSON.parseObject("{\"code\":0}", Model.class);
    }

    public static class Model {
        @JSONField(name = "code")
        private EnumClass code;

        public Model() {
        }

        public EnumClass getCode() {
            return code;
        }

        public void setCode(EnumClass code) {
            this.code = code;
        }
    }

    public static enum EnumClass {
        A(1);

        @JSONField
        private int code;

        EnumClass(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}

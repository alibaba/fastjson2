package com.alibaba.fastjson2.v1issues.issue_2000;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2065C {
    @Test
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"code\":0}", Model.class, JSONReader.Feature.ErrorOnEnumNotMatch);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    @Test
    public void test_for_issue_01() {
        Exception error = null;
        try {
            JSON.parseObject("0", EnumClass.class, JSONReader.Feature.ErrorOnEnumNotMatch);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    @Test
    public void test_for_issue_02() {
        assertSame(EnumClass.A, JSON.parseObject("1", EnumClass.class));
        assertSame(EnumClass.B, JSON.parseObject("2", EnumClass.class));
    }

    @Test
    public void test_for_issue_03() {
        assertSame(EnumClass.A, JSON.parseObject("{\"code\":1}", Model.class).code);
        assertSame(EnumClass.B, JSON.parseObject("{\"code\":2}", Model.class).code);
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
        A(1),
        B(2);

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

package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1710 {
    @Test
    public void test() {
        Result result = new Result();
        result.success = false;

        String str = JSON.toJSONString(result, JSONWriter.Feature.NotWriteDefaultValue);
        assertEquals("{\"success\":false}", str);
    }

    private static class Result {
        private boolean success = true;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    @Test
    public void test1() {
        Result1 result = new Result1();
        result.success = false;

        String str = JSON.toJSONString(result, JSONWriter.Feature.NotWriteDefaultValue);
        assertEquals("{\"success\":false}", str);
    }

    public static class Result1 {
        private boolean success = true;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}

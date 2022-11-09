package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue878 {
    @Test
    public void test() {
        Response response = new Response();
        response.setSuccess(true);

        assertEquals(
                "{\"success\":true}",
                JSON.toJSONString(response, JSONWriter.Feature.WriteNullListAsEmpty)
        );

        assertEquals("{\"success\":true}", JSON.toJSONString(response));

        assertEquals(
                "{\"success\":true}",
                JSON.toJSONString(response, JSONWriter.Feature.BrowserCompatible)
        );
    }

    public static class Response
            implements Serializable {
        /**
         * successful
         */
        private boolean success;

        /**
         * error code
         */
        private String errCode;

        /**
         * error message
         */
        private String errMessage;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrCode() {
            return errCode;
        }

        public void setErrCode(String errCode) {
            this.errCode = errCode;
        }

        public String getErrMessage() {
            return errMessage;
        }

        public void setErrMessage(String errMessage) {
            this.errMessage = errMessage;
        }
    }
}

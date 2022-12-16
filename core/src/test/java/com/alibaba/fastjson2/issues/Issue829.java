package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue829 {
    @Test
    public void test() {
        ServiceCode statusCode = ServiceCode.SERVER_ERROR;
        statusCode.setMsg("1111");
        String s = JSON.toJSONString(statusCode);
        ServiceCode statusCode1 = JSON.parseObject(s, ServiceCode.class);
        assertSame(statusCode, statusCode1);
    }

    @JSONType(writeEnumAsJavaBean = true)
    @Getter
    public enum ServiceCode {
        /**
         * SUCCESS
         */
        SUCCESS("00", "SUCCESS"),
        /**
         * SERVER_ERROR
         */
        SERVER_ERROR("01", "ERROR");

        ServiceCode(String code, String msgId) {
            this.code = code;
            this.msgId = msgId;
        }

        private String code;
        private String msgId;
        private String msg;

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @JSONCreator
        public static ServiceCode valueOf(String code, String msg) {
            ServiceCode status;
            switch (code) {
                case "0000":
                    status = ServiceCode.SUCCESS;
                    break;
                default:
                    status = ServiceCode.SERVER_ERROR;
                    break;
            }
            Optional.ofNullable(msg).ifPresent(msgVal -> status.setMsg(msgVal));
            return status;
        }
    }
}

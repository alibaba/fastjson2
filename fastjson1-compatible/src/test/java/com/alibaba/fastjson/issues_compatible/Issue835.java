package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue835 {
    @Test
    public void test() {
        Result genericResult = Result.ofCode(ServiceCode.SUCCESS);
        assertEquals("{\"code\":{\"code\":\"00\",\"msgId\":\"SUCCESS\"}}", JSON.toJSONString(genericResult));
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor(staticName = "ofCode")
    public static class Result<T> {
        private Object others;
        private T bo;
        @NotNull
        private ServiceCode code;
    }

    @JSONType(serializeEnumAsJavaBean = true)
    @ToString
    @Getter
    public enum ServiceCode {
        /**
         * SUCCESS
         */
        SUCCESS("00", "SUCCESS"),
        /**
         * ERROR
         */
        ERROR("01", "SERVER_ERROR");

        ServiceCode(String code, String msgId) {
            this.code = code;
            this.msgId = msgId;
        }

        private String code;
        private String msgId;
        @Setter
        private String msg;
    }
}

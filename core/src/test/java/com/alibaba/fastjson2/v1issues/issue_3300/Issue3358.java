package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @Author ：Nanqi
 * @Date ：Created in 19:07 2020/7/21
 */
public class Issue3358 {
    @Test
    public void test_for_issue() throws Exception {
        Model validateCode = new Model("111", 600);
        String jsonString = JSON.toJSONString(validateCode);
        Model backModel = JSON.parseObject(jsonString, Model.class);
        assertEquals(validateCode.getExpireTime(), backModel.getExpireTime());

        jsonString = "{\"code\":\"111\"}";
        backModel = JSON.parseObject(jsonString, Model.class);
        assertNull(backModel.getExpireTime());
    }

    public static class Model {
        private String code;

        private LocalDateTime expireTime;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        public Model(String code, int expireIn) {
            this.code = code;
            this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
        }

        @JSONCreator
        public Model(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public boolean isExpried() {
            return LocalDateTime.now().isAfter(getExpireTime());
        }
    }
}

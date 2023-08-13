package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1731 {
    @Test
    public void test() {
        UserRecord userRecord = new UserRecord("test-id", "test-name", 999);
        UserRecord userRecord1 = JSONObject.from(userRecord).to(UserRecord.class);
        assertEquals("{\"age\":999,\"id\":\"test-id\",\"name\":\"test-name\"}", JSONObject.toJSONString(userRecord1));
    }

    public record UserRecord(String id, String name, Integer age) {
        public UserRecord() {
            this(null, null, 37);
        }
    }
}

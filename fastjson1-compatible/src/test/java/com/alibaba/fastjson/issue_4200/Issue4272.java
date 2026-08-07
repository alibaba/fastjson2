package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue4272 {
    @Test
    public void test() {
        String str = "{\"dicId\":13+3,\"status\":false}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str));
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Bean.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Bean.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class));
    }

    public static class Bean {
        public int dicId;
        public boolean status;
    }
}

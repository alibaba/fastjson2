package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue993 {
    @Test
    public void test() {
        LoginUser user = new LoginUser();
        assertEquals("{}", JSON.toJSONString(user, JSONWriter.Feature.IgnoreNonFieldGetter));
    }

    @Data
    public static class LoginUser
            implements Serializable {
        public int getGrantType() {
            return 1;
        }
    }
}

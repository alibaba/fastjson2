package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3724 {
    @Test
    public void test() {
        String str = "{\"isSuccess\":true,\"isSuccess2\":true}";
        BooleanDTO booleanDTO = JSON.parseObject(str, BooleanDTO.class);
        assertEquals(str, JSON.toJSONString(booleanDTO));
    }

    @Data
    public class BooleanDTO {
        @JSONField(name = "isSuccess")
        private boolean isSuccess;
        @JSONField(name = "isSuccess2")
        private boolean isSuccess2;
    }
}

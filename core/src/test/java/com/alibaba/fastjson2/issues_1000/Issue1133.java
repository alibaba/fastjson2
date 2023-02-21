package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class Issue1133 {
    @Test
    public void test() {
        JSON.toJSONString(new Bean());
    }

    @Data
    public static class Bean {
        private Boolean success;
        public Boolean isSuccess() {
            throw new IllegalStateException();
        }
    }
}

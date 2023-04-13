package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1357 {
    @Test
    public void test() {
        String t1 = "{\"flag\":false,\"pSsrc\":6666665678858,\"pUserId\":\"\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(t1, Bean.class));
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static class Bean {
        private Integer pSsrc;
        private String pUserId;
        private boolean flag;
        private Map<String, String> map;

        public Bean(int pSsrc, String pUserId, boolean flag, Map<String, String> map) {
            super();
            this.pSsrc = pSsrc;
            this.pUserId = pUserId;
            this.flag = flag;
            this.map = map;
        }
    }
}

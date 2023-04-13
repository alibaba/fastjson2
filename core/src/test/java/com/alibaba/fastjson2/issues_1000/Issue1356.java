package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1356 {
    @Test
    public void test() {
        String str = "{\"flag\":false,\"pSsrc\":1,\"pUserId\":\"\",\"map\":\"\"}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertNull(bean.map);
    }

    @Data
    @Builder
    public static class Bean {
        private int pSsrc;
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

    @Test
    public void test1() {
        String str = "{\"map\":\"\"}";
        Bean1 bean = JSON.parseObject(str, Bean1.class);
        assertTrue(bean.map.isEmpty());
    }

    public static class Bean1 {
        public Map map;
    }

    @Test
    public void test2() {
        String str = "{\"map\":\"\"}";
        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertNull(bean.map);
    }

    public static class Bean2 {
        public Map<String, Integer> map;
    }
}

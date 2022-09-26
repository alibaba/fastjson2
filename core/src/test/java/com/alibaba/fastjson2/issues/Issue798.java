package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue798 {
    @Test
    public void test() {
        Config config = new Config();
        assertEquals("{}", JSON.toJSONString(config));
    }

    @Data
    public static class Config {
        public String[] deviceCodes;
        public String barkPushUrl;
        public String barkPushToken;
        public String location;
        public List<String> storeNameWhiteList;
    }
}

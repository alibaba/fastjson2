package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Issue1457 {
    @Test
    public void test() {
        String jsonStr = "{\"maps\":{\"1\":{\"id\":1}}}"; //失败
        Test2 test3 = JSON.parseObject(jsonStr, Test2.class, JSONReader.Feature.SupportAutoType);
        System.out.println(test3);
    }

    static class Test2
            implements Serializable {
        public Map<Integer, MapItem> maps = new HashMap<>();
    }

    static class MapItem {
        public int id;
    }
}

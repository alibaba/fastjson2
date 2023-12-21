package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;

public class Issue2103 {
    @Test
    public void test() throws Exception {
        ResolvableType t = ResolvableType.forField(Bean.class.getDeclaredField("myMap"));
        String str = JSON.toJSONString(t);
        System.out.printf(str);
    }

    public static class Bean {
        private HashMap<Integer, List<String>> myMap;

        public HashMap<Integer, List<String>> getMyMap() {
            return myMap;
        }

        public void setMyMap(HashMap<Integer, List<String>> myMap) {
            this.myMap = myMap;
        }
    }
}

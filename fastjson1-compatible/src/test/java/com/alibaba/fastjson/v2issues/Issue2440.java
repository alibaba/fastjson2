package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Issue2440 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lines", "1,2");
        Bean sample = jsonObject.toJavaObject(Bean.class);
        System.out.println(sample);
    }

    public static class Bean {
        private List<String> lines;

        public List<String> getLines() {
            return lines;
        }

        public void setLines(List<String> lines) {
            this.lines = lines;
        }
    }
}

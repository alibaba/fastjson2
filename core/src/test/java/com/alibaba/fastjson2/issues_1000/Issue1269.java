package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1269 {
    @Test
    public void test() {
        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);

        JavaObj o = new JavaObj(map);
        byte[] bt = JSON.toJSONBytes(o, JSONWriter.Feature.WriteNonStringKeyAsString); // 兼容其他语言
        JSONObject jsonObj = JSON.parseObject(bt);
        assertEquals("{\"map\":{\"1\":1,\"2\":2}}", jsonObj.toString());

        o = jsonObj.to(JavaObj.class, JSONReader.Feature.FieldBased);
        assertEquals(1, o.getMap().get(1)); // 这里泛型不匹配, String的key没有自动转为int 仍是String类型。
    }

    static class JavaObj {
        HashMap<Integer, Integer> map = new HashMap<>();

        public JavaObj() {
        }

        public JavaObj(HashMap<Integer, Integer> map) {
            super();
            this.map = map;
        }

        public HashMap<Integer, Integer> getMap() {
            return map;
        }

        public void setMap(HashMap<Integer, Integer> map) {
            this.map = map;
        }
    }
}

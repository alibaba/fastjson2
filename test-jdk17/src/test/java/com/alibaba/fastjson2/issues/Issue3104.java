package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3104 {
    record Transform(Bridge bridge) {
        public Transform(String from, String to) {
            this(new Bridge(from, to));
        }

        @JSONCreator
        public Transform(Bridge bridge) {
            this.bridge = bridge;
        }
    }

    record Bridge(String from, String to) {
    }

    @Test
    public void test() {
        String json = JSON.toJSONString(new Transform("zhangsan", "lisi"));
        assertEquals("{\"bridge\":{\"from\":\"zhangsan\",\"to\":\"lisi\"}}", json);
        Transform transform = JSON.parseObject(json, Transform.class);
        System.out.println(transform);
    }
}

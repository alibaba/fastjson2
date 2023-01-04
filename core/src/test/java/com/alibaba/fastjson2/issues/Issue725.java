package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue725 {
    @Test
    public void test() {
        String sourceJsonStr = JSON.toJSONString(ReturnWay.EXPRESS);
        ReturnWay parsed = JSON.parseObject(sourceJsonStr, ReturnWay.class);
        assertSame(ReturnWay.EXPRESS, parsed);
    }

    public enum ReturnWay {
        OTHER(0, "其他"),
        EXPRESS(1, "快递");

        private Integer type;

        private String desc;

        ReturnWay(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        private static Map<String, ReturnWay> returnWayMap = new HashMap<>();

        static {
            Arrays.stream(ReturnWay.values()).forEach(
                    t -> returnWayMap.put(t.getType().toString(), t)
            );
        }

        @JsonCreator
        public static ReturnWay forValue(Map<String, String> map) {
            return returnWayMap.get(map.keySet().iterator().next());
        }

        @JsonValue
        public Map<String, String> toValue() {
            Map<String, String> map = new HashMap<>();
            map.put(this.getType().toString(), this.getDesc());
            return map;
        }

        public Integer getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }

        public static Map<String, ReturnWay> getReturnWayMap() {
            return returnWayMap;
        }
    }
}

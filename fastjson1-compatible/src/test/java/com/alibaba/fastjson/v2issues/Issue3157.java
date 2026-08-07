package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3157 {
    public static class People {
        private Integer age;

        private Long hairNums;

        private String name;

        public People(Integer age, String name, Long hairNums) {
            this.age = age;
            this.name = name;
            this.hairNums = hairNums;
        }

        public Integer getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public Long getHairNums() {
            return hairNums;
        }
    }

    @Test
    public void test() {
        SerializeConfig stringSerializeConfig = new SerializeConfig();
        stringSerializeConfig.put(Integer.class, ToStringSerializer.instance);
        stringSerializeConfig.put(Long.class, ToStringSerializer.instance);

        People one = new People(10, "aaa", 10000000L);
        assertEquals(
                "{\"age\":\"10\",\"hairNums\":\"10000000\",\"name\":\"aaa\"}",
                JSON.toJSONString(one, stringSerializeConfig));
    }
}

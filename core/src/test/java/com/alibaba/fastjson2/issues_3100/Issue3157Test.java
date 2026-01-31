package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue3157测试类
 * @author changyi.ccy
 */
public class Issue3157Test {
    /**
     * 测试: Issue3157
     */
    @Test
    public void testIssue3157() {
        SerializeConfig stringSerializeConfig = new SerializeConfig();
        stringSerializeConfig.put(Integer.class, ToStringSerializer.instance);
        stringSerializeConfig.put(Long.class, ToStringSerializer.instance);
        People people = new People("Test", 10, 10000000L);
        String jsonText = "{\"age\":\"10\",\"hairNums\":\"10000000\",\"name\":\"Test\"}";
        assertEquals(jsonText, JSON.toJSONString(people, stringSerializeConfig, new SerializerFeature[0]), "JSON文本不一致");
    }

    /**
     * People类
     */
    public static class People {
        private String name;
        private Integer age;
        private Long hairNums;

        public People(String name, Integer age, Long hairNums) {
            this.name = name;
            this.age = age;
            this.hairNums = hairNums;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public Long getHairNums() {
            return hairNums;
        }
    }
}

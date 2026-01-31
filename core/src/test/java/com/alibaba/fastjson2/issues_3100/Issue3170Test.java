package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue3170测试类
 * @author changyi.ccy
 */
public class Issue3170Test {
    /**
     * 测试: Issue3170
     */
    @Test
    public void testIssue3170() {
        IntegerToStringFilter integerToStringFilter = new IntegerToStringFilter();
        SerializeConfig stringSerializeConfig = new SerializeConfig();
        stringSerializeConfig.put(Integer.class, ToStringSerializer.instance);
        stringSerializeConfig.put(Long.class, ToStringSerializer.instance);

        People one = new People(10, "aaa", 10000000L);
        People two = new People(40, "bbb", 0L);
        two.setSon(one);
        List<People> peopleList = new ArrayList<>();
        peopleList.add(one);
        peopleList.add(two);
        String jsonText = "[{\"age\":\"10\",\"hairNums\":\"10000000\",\"name\":\"aaa\"},"
                + "{\"age\":\"40\",\"hairNums\":\"0\",\"name\":\"bbb\",\"son\":"
                + "{\"age\":\"10\",\"hairNums\":\"10000000\",\"name\":\"aaa\"}}]";
        assertEquals(jsonText, JSON.toJSONString(peopleList,
                stringSerializeConfig, integerToStringFilter), "JSON文本不一致");
    }

    /**
     * People类
     */
    public static class People {
        private Long hairNums;
        private String name;
        private Integer age;
        @JSONField(serialzeFeatures = SerializerFeature.DisableCircularReferenceDetect)
        private People son;
        @JSONField(format = "yyyy-MM-dd")
        private Date applyStartTime;

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

        public People getSon() {
            return son;
        }

        public void setSon(People son) {
            this.son = son;
        }

        public Date getApplyStartTime() {
            return applyStartTime;
        }

        public void setApplyStartTime(Date applyStartTime) {
            this.applyStartTime = applyStartTime;
        }
    }

    /**
     * 整数转字符串过滤器类
     */
    public static class IntegerToStringFilter
            implements ValueFilter {
        @Override
        public Object process(Object o, String s, Object v) {
            if (Objects.isNull(v)) {
                return null;
            }
            if (v instanceof Integer) {
                return v.toString();
            }
            return v;
        }
    }
}

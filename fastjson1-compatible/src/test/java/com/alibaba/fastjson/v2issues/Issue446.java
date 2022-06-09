package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.NameFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue446 {
    @Test
    public void test() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCaseWithUnderScores);
        String res = JSON.toJSONString(test, nameFilter, SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"AGE_B1\":12,\"NAME_V1\":\"z\"}", res);
    }

    @Test
    public void test1() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithUnderScores);
        String res = JSON.toJSONString(test, nameFilter, SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"Age_B1\":12,\"Name_V1\":\"z\"}", res);
    }

    @Test
    public void test2() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithDashes);
        String res = JSON.toJSONString(test, nameFilter, SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"Age-B1\":12,\"Name-V1\":\"z\"}", res);
    }

    @Test
    public void test3() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithDots);
        String res = JSON.toJSONString(test, nameFilter, SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"Age.B1\":12,\"Name.V1\":\"z\"}", res);
    }

    @Test
    public void test4() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithSpaces);
        String res = JSON.toJSONString(test, nameFilter, SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"Age B1\":12,\"Name V1\":\"z\"}", res);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Student{
        private String nameV1;
        private Integer ageB1;
        private String emptyC1;
    }
}

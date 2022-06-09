package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
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
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"AGE_B1\":12,\"NAME_V1\":\"z\"}", res);
    }

    @Test
    public void test1() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithUnderScores);
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"Age_B1\":12,\"Name_V1\":\"z\"}", res);
    }

    @Test
    public void test2() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithDashes);
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"Age-B1\":12,\"Name-V1\":\"z\"}", res);
    }

    @Test
    public void test3() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithDots);
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"Age.B1\":12,\"Name.V1\":\"z\"}", res);
    }

    @Test
    public void test4() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.of(PropertyNamingStrategy.UpperCamelCaseWithSpaces);
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"Age B1\":12,\"Name V1\":\"z\"}", res);
    }

    @Test
    public void test5() {
        Student test = new Student("z", 12, null);
        NameFilter nameFilter = NameFilter.compose(
                NameFilter.of(PropertyNamingStrategy.SnakeCase),
                (o12, s, o1) -> {
                    String res = s.substring(0, 1).toUpperCase();
                    if (s.length() > 1) {
                        res += s.substring(1);
                    }
                    return res;
                });
        String res = JSON.toJSONString(test, nameFilter, JSONWriter.Feature.WriteNullStringAsEmpty);
        assertEquals("{\"Age_b1\":12,\"Name_v1\":\"z\"}", res);
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

package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.util.BeanUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2459 {
    @Test
    public void test() {
        User user = new User();
        user.setFirstName("first");
        user.setLastName("last");
        List<String> getMethodNames = Arrays.stream(User.class.getDeclaredMethods()).sequential().filter(m -> m.getName().startsWith("get")).map(s2 -> s2.getName()).collect(Collectors.toList());
        List<String> setMethodNames = Arrays.stream(User.class.getDeclaredMethods()).sequential().filter(m -> m.getName().startsWith("set")).map(s2 -> s2.getName()).collect(Collectors.toList());
        Arrays.stream(PropertyNamingStrategy.values()).sequential().forEach(s -> {
            String expected = getExpected(user, s);
            assertEquals(expected, getterTest(getMethodNames, s));
            assertEquals(expected, setterTest(setMethodNames, s));
        });
    }

    private static String getExpected(User user, PropertyNamingStrategy strategy) {
        String jsonString = JSON.toJSONString(user, NameFilter.of(strategy));
        return (String) JSON.parseObject(jsonString, Map.class).keySet().stream().sorted().collect(Collectors.joining(","));
    }

    private static String getterTest(List<String> methodNames, PropertyNamingStrategy strategy) {
        return methodNames.stream().map(m -> BeanUtils.getterName(m, strategy.name())).collect(Collectors.toSet()).stream().sorted().collect(Collectors.joining(","));
    }

    private static String setterTest(List<String> methodNames, PropertyNamingStrategy strategy) {
        return methodNames.stream().map(m -> BeanUtils.setterName(m, strategy.name())).collect(Collectors.toSet()).stream().sorted().collect(Collectors.joining(","));
    }

    @Data
    static class User {
        private String firstName;
        private String lastName;
    }
}

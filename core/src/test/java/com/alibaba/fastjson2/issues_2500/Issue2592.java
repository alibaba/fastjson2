package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2592 {
    @Test
    public void test() {
        String str = "{\"name\":\"A\",\"minVersion\":\"1-3.1.3\",\"maxVersion\":\"1-3.1.3\"}";
        TestBean result = JSONObject.parseObject(str, TestBean.class);
        System.out.println(result.getName());

        String str1 = "\"name\":\"A\",\"minVersion\":\"1-3.1.3\",\"maxVersion\":\"1-3.1.3\"}";
        TestBean result1 = JSONObject.parseObject(str1, TestBean.class);
        System.out.println(result1.getName());

        String str2 = "{\"name\":\"A\",\"minVersion\":\"1-3.1.3\",\"maxVersion\":\"1-3.1.3\"";
        assertThrows(
                JSONException.class,
                () -> JSONObject.parseObject(str2, TestBean.class));
    }

    @Getter
    @Setter
    public static class TestBean {
        String name;
        String minVersion;
        String maxVersion;
    }
}

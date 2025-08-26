package com.alibaba.fastjson2.issue_3700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3739 {
    @Test
    public void test() {
        String jsonStr = "{\n" +
                "    \"fa\": \"abc\",\n" +
                "    \"b\": {\n" +
                "        \"f_b\": \"xyz\"\n" +
                "    }\n" +
                "}";

        A a1 = JSON.parseObject(jsonStr, A.class);

        JSONObject jsonObject = JSON.parseObject(jsonStr);
        A a2 = jsonObject.toJavaObject(A.class);
        assertEquals(a1.toString(), a2.toString());
    }

    @Test
    public void test_2() {
        String jsonArrStr = "[{\n" +
                "    \"fa\": \"abc\",\n" +
                "    \"b\": {\n" +
                "        \"f_b\": \"xyz\"\n" +
                "    }\n" +
                "}]";

        List<A> list1 = JSON.parseArray(jsonArrStr, A.class);

        JSONArray jsonArray = JSON.parseArray(jsonArrStr);
        List<A> list2 = jsonArray.toJavaList(A.class);
        assertEquals(list1.toString(), list2.toString());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class A {
        public String fa;
        public B b;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class B {
        private String fb;
    }
}

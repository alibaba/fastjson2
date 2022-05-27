package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class Issue367 {
    @Test
    public void test() {
        String json = "{\n" +
                "    \"name\": \"123\",\n" +
                "    \"age\": 3,\n" +
                "    \"subDTO\": {\n" +
                "        \"id\": \"222\",\n" +
                "        \"thiDTO\": {\n" +
                "            \"h\": \"444\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String s = "{\"name\":\"123\",\"age\":3,\"subDTO\":{\"id\":\"222\",\"thiDTO\":{\"h\":\"444\"}}}";
        TestDTO testDTO2 = JSON.parseObject(s, TestDTO.class, JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.UseDefaultConstructorAsPossible);
        TestDTO testDTO1 = com.alibaba.fastjson.JSON.parseObject(s, TestDTO.class);
        System.out.println("testDTO2 = " + testDTO2);
        System.out.println("testDTO1 = " + testDTO1);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestDTO {

        private String name;
        private int age;
        private SubDTO subDTO = new SubDTO();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubDTO {
        private String id;
        private String nickName;
        private ThiDTO thiDTO = new ThiDTO();
        private FouDTO fouDTO = new FouDTO();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ThiDTO {
        private String h;
        private String w;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FouDTO {
        private String p;
        private String t;
    }
}

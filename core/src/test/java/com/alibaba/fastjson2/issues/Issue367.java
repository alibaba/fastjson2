package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(testDTO2.subDTO.fouDTO);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestDTO {

        private String name;
        private int age;
        private SubDTO subDTO = new SubDTO();

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SubDTO {
            private String id;
            private String nickName;
            private ThiDTO thiDTO = new ThiDTO();
            private FouDTO fouDTO = new FouDTO();
        }
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

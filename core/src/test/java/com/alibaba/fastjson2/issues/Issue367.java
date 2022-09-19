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
    public void test0() {
        String text = "{\"name\":\"123\",\"age\":3,\"subDTO\":{\"id\":\"222\",\"thiDTO\":{\"h\":\"444\"}}}";
        TestDTO testDTO2 = JSON.parseObject(
                text, TestDTO.class,
                JSONReader.Feature.SupportSmartMatch
        );

        assertNotNull(testDTO2.subDTO.fouDTO);
        assertNotNull(testDTO2.subDTO2.fouDTO);
    }

    @Test
    public void test1() {
        String text = "{\"name\":\"123\",\"age\":3,\"subDTO2\":{\"id\":\"222\",\"thiDTO\":{\"h\":\"444\"}}}";
        TestDTO testDTO2 = JSON.parseObject(
                text, TestDTO.class,
                JSONReader.Feature.SupportSmartMatch
        );

        assertNotNull(testDTO2.subDTO.fouDTO);
        assertNotNull(testDTO2.subDTO2.fouDTO);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestDTO {
        private String name;
        private int age;
        private SubDTO subDTO = new SubDTO();
        private SubDTO2 subDTO2 = new SubDTO2();

        @Data
        @NoArgsConstructor
        public class SubDTO {
            private String id;
            private String nickName;
            private ThiDTO thiDTO = new ThiDTO();
            private FouDTO fouDTO = new FouDTO();
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public class SubDTO2 {
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

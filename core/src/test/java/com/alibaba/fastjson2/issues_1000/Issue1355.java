package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class Issue1355 {
    @Test
    public void test() {
        System.out.println(
                JSON.toJSONString(TestDTO.builder().testEnum(TestEnum.E1).build())
        );
        System.out.println(
                com.alibaba.fastjson.JSON.toJSONString(TestDTO.builder().testEnum(TestEnum.E1).build())
        );
        System.out.println(
                JSON.toJSONString(TestEnum.E1)
        );
    }

    @Data
    @Builder
    public static class TestDTO {
        private TestEnum testEnum;
    }

    public enum TestEnum {
        E1, E2;
    }
}

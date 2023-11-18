package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2027 {
    @Test
    public void test2() {
        assertEquals(
                "{\"test\":\"bcccc\"}",
                JSON.toJSONString(new Bean().setTest(TestEnum.BB))
        );
    }

    @Getter
    @RequiredArgsConstructor
    public enum TestEnum
            implements DescEnum {
        AA("asss"),
        BB("bcccc");

        private final String desc;
    }

    public interface DescEnum {
        @JSONField(value = true)
        String getDesc();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private TestEnum test;
    }
}

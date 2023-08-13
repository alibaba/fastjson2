package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1701 {
    @Test
    public void test() {
        FluentModel fluentModel = FluentModel.builder().name("test").order(1).build();
        String json = "{\"name\":\"test\",\"order\":1}";
        FluentModel model1 = JSON.parseObject(json, FluentModel.class);
        assertEquals(fluentModel.order(), model1.order());
        assertEquals(fluentModel.name(), model1.name());
        assertEquals("{\"name\":\"test\",\"order\":1}", JSON.toJSONString(fluentModel));
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(fluent = true)
    @Data
    public static class FluentModel {
        private String name;
        private Integer order;
    }
}

package com.alibaba.fastjson;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONAwareTest {
    @Test
    public void test() {
        Attribute attribute = new Attribute();
        String jsonArray = Optional.ofNullable(attribute.getJsonArray())
                .map(JSONAware::toJSONString)
                .orElse("null");
        assertEquals("null", jsonArray);
    }

    public static class Attribute {
        @Getter
        @Setter
        private JSONArray jsonArray;
    }
}
